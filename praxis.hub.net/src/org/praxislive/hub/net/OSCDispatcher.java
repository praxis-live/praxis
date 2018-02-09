/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2016 Neil C Smith.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 3 only, as
 * published by the Free Software Foundation.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 3 for more details.
 *
 * You should have received a copy of the GNU General Public License version 3
 * along with this work; if not, see http://www.gnu.org/licenses/
 * 
 *
 * Please visit http://neilcsmith.net if you need additional information or
 * have any questions.
 */
package org.praxislive.hub.net;

import de.sciss.net.OSCBundle;
import de.sciss.net.OSCMessage;
import de.sciss.net.OSCPacket;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.praxislive.core.Value;
import org.praxislive.core.Call;
import org.praxislive.core.CallArguments;
import org.praxislive.core.ComponentType;
import org.praxislive.core.ControlAddress;
import org.praxislive.core.types.PString;

/**
 *
 * @author Neil C Smith <http://neilcsmith.net>
 */
abstract class OSCDispatcher {

    private final static Logger LOG = Logger.getLogger(OSCDispatcher.class.getName());
    
    final static String SND = "/SND";
    final static String RES = "/RES";
    final static String ERR = "/ERR";
    final static String ADD = "/ADD";
    final static String DEL = "/DEL";
    
    final static String SYS_PREFIX = "/_sys";
//    final static String REMOTE_SYS_PREFIX = "/_remote";

    private final PraxisPacketCodec codec;
    private final Map<Integer, SentCallInfo> sentCalls;
    private final Map<Integer, Integer> localToRemoteID;

    OSCDispatcher(PraxisPacketCodec codec) {
        this.codec = codec;
        sentCalls = new LinkedHashMap<>();
        localToRemoteID = new HashMap<>();
    }
    
    

    void handleMessage(OSCMessage msg, long timeTag) {
        String address = msg.getName();
        try {
            switch (address) {
                case SND:
                    handleSND(msg, timeTagToNanos(timeTag));
                    break;
                case RES:
                    handleRES(msg);
                    break;
                case ERR:
                    handleERR(msg);
                    break;
                case ADD:
                    handleADD(msg, timeTagToNanos(timeTag));
                    break;
                case DEL:
                    handleDEL(msg, timeTagToNanos(timeTag));
                    break;
            }
        } catch (Exception e) {
            if (!RES.equals(address) && !ERR.equals(address)) {
                try {
                    send(new OSCMessage(ERR, new Object[]{extractID(msg), e.getClass().getSimpleName()}));
                } catch (Exception ex) {

                }
            }
        }
    }

    void handleSND(OSCMessage msg, long time) throws Exception {
        int id = extractID(msg);
        ControlAddress to = ControlAddress.valueOf(msg.getArg(1).toString());
        String fromString = msg.getArg(2).toString();
        if (fromString.startsWith(SYS_PREFIX)) {
            fromString = getRemoteSysPrefix() + fromString;
        }
        ControlAddress from = ControlAddress.valueOf(fromString);
        CallArguments args = extractCallArguments(msg, 3);
        Call call = Call.createCall(to, from, time, args);
        send(call);
        localToRemoteID.put(call.getMatchID(), id);
    }

    void handleRES(OSCMessage msg) throws Exception {
        int id = extractID(msg);
        SentCallInfo info = sentCalls.remove(id);
        if (info == null) {
            throw new IllegalArgumentException("Unexpected response");
        }
        CallArguments args = extractCallArguments(msg, 1);
        Call call = Call.createReturnCall(info.localCall, args);
        send(call);
    }

    void handleERR(OSCMessage msg) throws Exception {
        int id = extractID(msg);
        SentCallInfo info = sentCalls.remove(id);
        if (info == null) {
            throw new IllegalArgumentException("Unexpected response");
        }
        CallArguments args = extractCallArguments(msg, 1);
        Call call = Call.createErrorCall(info.localCall, args);
        send(call);
    }

    void handleADD(OSCMessage msg, long time) throws Exception {
        int id = extractID(msg);
        ControlAddress to = getAddRootAddress();
        String fromString = msg.getArg(1).toString();
        if (fromString.startsWith(SYS_PREFIX)) {
            fromString = getRemoteSysPrefix() + fromString;
        }
        ControlAddress from = ControlAddress.valueOf(fromString);
        PString rootID = PString.valueOf(msg.getArg(2));
        ComponentType rootType = ComponentType.valueOf(msg.getArg(3).toString());     
        Call call = Call.createCall(to, from, time, CallArguments.create(rootID, rootType));
        send(call);
        localToRemoteID.put(call.getMatchID(), id);
    }

    void handleDEL(OSCMessage msg, long time) throws Exception {
        int id = extractID(msg);
        ControlAddress to = getRemoveRootAddress();
        String fromString = msg.getArg(1).toString();
        if (fromString.startsWith(SYS_PREFIX)) {
            fromString = getRemoteSysPrefix() + fromString;
        }
        ControlAddress from = ControlAddress.valueOf(fromString);
        PString rootID = PString.valueOf(msg.getArg(2));
        Call call = Call.createCall(to, from, time, rootID);
        send(call);
        localToRemoteID.put(call.getMatchID(), id);
    }

    void handleCall(Call call) {
        switch (call.getType()) {
            case INVOKE:
            case INVOKE_QUIET:
                handleInvoke(SND, call);
                break;
            case RETURN:
                handleResponse(RES, call);
                break;
            case ERROR:
                handleResponse(ERR, call);
                break;
        }
    }
    
    void handleAddRoot(Call call) {
        Object[] oscArgs = new Object[4];
        oscArgs[0] = call.getMatchID();
        oscArgs[1] = call.getFromAddress().toString();
        oscArgs[2] = codec.toOSCObject(call.getArgs().get(0));
        oscArgs[3] = codec.toOSCObject(call.getArgs().get(1));
        send(ADD, call.getTimecode(), oscArgs);
        sentCalls.put(call.getMatchID(), new SentCallInfo(System.nanoTime(), call));
    }
    
    void handleRemoveRoot(Call call) {
        Object[] oscArgs = new Object[3];
        oscArgs[0] = call.getMatchID();
        oscArgs[1] = call.getFromAddress().toString();
        oscArgs[2] = codec.toOSCObject(call.getArgs().get(0));
        send(DEL, call.getTimecode(), oscArgs);
        sentCalls.put(call.getMatchID(), new SentCallInfo(System.nanoTime(), call));
    }

    void handleInvoke(String target, Call call) {
        CallArguments callArgs = call.getArgs();
        Object[] oscArgs = new Object[callArgs.getSize() + 3];
        oscArgs[0] = call.getMatchID();
        String to = call.getToAddress().toString();
        if (to.startsWith(getRemoteSysPrefix())) {
            to = to.substring(getRemoteSysPrefix().length());
        }
        oscArgs[1] = to;
        oscArgs[2] = call.getFromAddress().toString();
        for (int i=3, k=0; i<oscArgs.length; i++, k++) {
            oscArgs[i] = codec.toOSCObject(callArgs.get(k));
        }
        send(target, call.getTimecode(), oscArgs);
        sentCalls.put(call.getMatchID(), new SentCallInfo(System.nanoTime(), call));
    }

    void handleResponse(String target, Call call) {
        Integer remoteID = localToRemoteID.remove(call.getMatchID());
        if (remoteID == null) {
            LOG.log(Level.FINE, "Unexpected call response\n{0}", call);
            return;
        }
        CallArguments callArgs = call.getArgs();
        Object[] oscArgs = new Object[callArgs.getSize() + 1];
        oscArgs[0] = remoteID;
        for (int i=1, k=0; i<oscArgs.length; i++, k++) {
            oscArgs[i] = codec.toOSCObject(callArgs.get(k));
        }
        send(target, call.getTimecode(), oscArgs);
    }

    void send(String target, long nanos, Object[] args) {
        OSCBundle b = new OSCBundle();
        b.setTimeTagRaw(nanosToTimeTag(nanos));
        b.addPacket(new OSCMessage(target, args));
        if (LOG.isLoggable(Level.FINE)) {
            LOG.log(Level.FINE, "{0} : {1}", new Object[]{target, Arrays.toString(args)});
        }
        send(b);
    }
    
    void purge(long time, TimeUnit unit) {
        long ago = unit.toNanos(time);
        long now = System.nanoTime();
        Iterator<SentCallInfo> itr = sentCalls.values().iterator();
        while (itr.hasNext()) {
            SentCallInfo info = itr.next();
            if ( (now - info.sentTime) < ago) {
                LOG.fine("No calls to purge");
                break;
            }
            itr.remove();
            LOG.log(Level.FINE, "Purging call\n{0}", info.localCall);
            Call err = Call.createErrorCall(info.localCall, CallArguments.EMPTY);
            send(err);
        }
    }

    ControlAddress getAddRootAddress() {
        throw new UnsupportedOperationException();
    }

    ControlAddress getRemoveRootAddress() {
        throw new UnsupportedOperationException();
    }

    long timeTagToNanos(long timeTag) {
        return System.nanoTime();
    }

    long nanosToTimeTag(long nanos) {
        return OSCBundle.NOW;
    }

    abstract void send(OSCPacket packet);

    abstract void send(Call call);
    
    abstract String getRemoteSysPrefix();

    int extractID(OSCMessage msg) throws Exception {
        Object o = msg.getArg(0);
        if (o instanceof Number) {
            return ((Number) o).intValue();
        } else {
            return Integer.valueOf(o.toString());
        }
    }

    CallArguments extractCallArguments(OSCMessage msg, int fromIndex) {
        int argCount = msg.getArgCount() - fromIndex;
        if (argCount == 0) {
            return CallArguments.EMPTY;
        } else if (argCount == 1) {
            return CallArguments.create(codec.toArgument(msg.getArg(fromIndex)));
        } else {
            Value[] args = new Value[argCount];
            for (int i = 0; i < argCount; i++) {
                args[i] = codec.toArgument(msg.getArg(i + fromIndex));
            }
            return CallArguments.create(args);
        }
    }
    

    

    private static class SentCallInfo {

        final long sentTime;
        final Call localCall;

        SentCallInfo(long sentTime, Call localCall) {
            this.sentTime = sentTime;
            this.localCall = localCall;
        }

    }

}
