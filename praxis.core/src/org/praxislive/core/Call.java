/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2019 Neil C Smith.
 * 
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License version 3 only, as
 * published by the Free Software Foundation.
 * 
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser General Public License
 * version 3 for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License version 3
 * along with this work; if not, see http://www.gnu.org/licenses/
 * 
 * 
 * Please visit https://www.praxislive.org if you need additional information or
 * have any questions.
 */
package org.praxislive.core;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import org.praxislive.core.types.PError;

/**
 *
 * @author Neil C Smith
 */
@SuppressWarnings("deprecation")
public final class Call extends Packet {

    /**
     *
     */
    @Deprecated
    public static enum Type {

        /**
         * Invoke control - response is mandatory.
         */
        INVOKE,
        /**
         * Invoke call - response is not required except in case of error.
         */
        INVOKE_QUIET,
        /**
         * Return call.
         */
        RETURN,
        /**
         * Error call.
         */
        ERROR
    };

    private final Type type;
    private final List<Value> args;
    private final ControlAddress toAddress;
    private final ControlAddress fromAddress;
    private final int matchID;

    private Call(
            String root,
            ControlAddress toAddress,
            ControlAddress fromAddress,
            long timeCode,
            List<Value> args,
            Type type,
            int matchID) {
        super(root, timeCode);
        this.toAddress = toAddress;
        this.fromAddress = fromAddress;
        this.args = args;
        this.type = type;
        this.matchID = matchID;
    }

    private Call(
            String root,
            ControlAddress toAddress,
            ControlAddress fromAddress,
            long timeCode,
            List<Value> args,
            Type type) {
        super(root, timeCode);
        this.toAddress = toAddress;
        this.fromAddress = fromAddress;
        this.args = args;
        this.type = type;
        this.matchID = id();
    }

    /**
     * Get the type of this call, either INVOKE, INVOKE_QUIET, RETURN or ERROR
     *
     * @return Call.Type
     */
    @Deprecated
    public Type getType() {
        return this.type;
    }

    /**
     * Query whether this Call is a request message.
     *
     * @return Call is a request
     */
    public boolean isRequest() {
        return type == Type.INVOKE || type == Type.INVOKE_QUIET;
    }

    /**
     * Query whether this Call is a reply message. Use {@link #matchID()} to
     * link up with a request Call.
     *
     * @return Call is a reply
     */
    public boolean isReply() {
        return type == Type.RETURN;
    }

    /**
     * Query whether this Call is an error message. Use {@link #matchID()} to
     * link up with a request Call.
     *
     * @return Call is an error
     */
    public boolean isError() {
        return type == Type.ERROR;
    }

    /**
     * Query whether this Call requires a reply. This implies the Call is also a
     * request, although not all request require replies.
     * <p>
     * Error messages should usually be sent even if a reply is not required.
     *
     * @return Call requires a reply
     */
    public boolean isReplyRequired() {
        return type == Type.INVOKE;
    }

    /**
     * Get the argument list of this Call. The returned list is unmodifiable.
     *
     * @return list of arguments
     */
    public List<Value> args() {
        return args;
    }

    /**
     * Get the Arguments of this Call.
     *
     * @return CallArguments
     */
    @Deprecated
    public CallArguments getArgs() {
        return CallArguments.create(args);
    }

    /**
     * Get the ControlAddress that this Call should be sent to.
     *
     * @return ControlAddress
     */
    public ControlAddress to() {
        return this.toAddress;
    }

    /**
     * Get the ControlAddress that this Call should be sent to.
     *
     * @return ControlAddress
     */
    @Deprecated
    public ControlAddress getToAddress() {
        return this.toAddress;
    }

    /**
     * Get the ControlAddress that this Call is being sent from, and if of type
     * INVOKE or INVOKE_QUIET, where RETURN and ERROR calls should be sent.
     *
     * @return
     */
    public ControlAddress from() {
        return this.fromAddress;
    }

    /**
     * Get the ControlAddress that this Call is being sent from, and if of type
     * INVOKE or INVOKE_QUIET, where RETURN and ERROR calls should be sent.
     *
     * @return
     */
    @Deprecated
    public ControlAddress getFromAddress() {
        return this.fromAddress;
    }

    /**
     * ID to match up calls and responses.
     *
     * For INVOKE and INVOKE_QUIET calls, this will return the same as getID().
     * For RETURN and ERROR calls, this ID will match the ID of the invoking
     * call.
     *
     * @return long ID
     */
    public int matchID() {
        return this.matchID;
    }

    /**
     * ID to match up calls and responses.
     *
     * For INVOKE and INVOKE_QUIET calls, this will return the same as getID().
     * For RETURN and ERROR calls, this ID will match the ID of the invoking
     * call.
     *
     * @return long ID
     */
    @Deprecated
    public int getMatchID() {
        return this.matchID;
    }

    /**
     * Create a return Call for this call having no arguments.
     *
     * @return return call
     */
    public Call reply() {
        return Call.createResponseCall(this, Collections.emptyList(), Type.RETURN);
    }

    /**
     * Create a return Call for this call with the given argument.
     *
     * @param arg single argument
     * @return return call
     */
    public Call reply(Value arg) {
        return Call.createResponseCall(this, Collections.singletonList(arg), Type.RETURN);
    }

    /**
     * Create a return Call for this call with the given arguments.
     *
     * @param args arguments
     * @return return call
     */
    public Call reply(List<Value> args) {
        return Call.createResponseCall(this, args, Type.RETURN);
    }

    /**
     * Create an error return Call for this call, with the given error argument.
     *
     * @param error
     * @return error return call
     */
    public Call error(PError error) {
        return Call.createResponseCall(this, Collections.singletonList(error), Type.ERROR);
    }

    /**
     * Create an error return Call for this call, with the given arguments.
     *
     * @param args error arguments
     * @return error return call
     */
    public Call error(List<Value> args) {
        return Call.createResponseCall(this, args, Type.ERROR);
    }

    /**
     * String representation of this Call. Only to be used for debugging
     * purposes. It is not guaranteed to retain the same format.
     *
     * @return String
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("Call " + super.toString());
        sb.append("\nTo : ").append(toAddress);
        sb.append("\nFrom : ").append(fromAddress);
        sb.append("\nType : ").append(type);
        sb.append("\nID : ").append(id());
        sb.append("\nMatch ID : ").append(matchID);
        sb.append("\nArguments {");
        int count = args.size();
        if (count > 0) {
            for (int i = 0; i < count; i++) {
                sb.append("\n    ").append(args.get(i));
            }
            sb.append("\n}");
        } else {
            sb.append("}\n");
        }

        return sb.toString();

    }

    /**
     * Create a Call with Type INVOKE and empty arguments.
     *
     * @param toAddress ControlAddress of receiving Control.
     * @param fromAddress ControlAddress for response.
     * @param timeCode long nanosecond time relative to hub clock
     * @return Call
     */
    public static Call create(
            ControlAddress toAddress,
            ControlAddress fromAddress,
            long timeCode) {

        return createCall(toAddress, fromAddress, timeCode,
                Collections.emptyList(), Type.INVOKE);
    }

    /**
     * Create a Call with Type INVOKE and empty arguments.
     *
     * @param toAddress ControlAddress of receiving Control.
     * @param fromAddress ControlAddress for response.
     * @param timeCode long nanosecond time relative to hub clock
     * @return Call
     */
    @Deprecated
    public static Call createCall(
            ControlAddress toAddress,
            ControlAddress fromAddress,
            long timeCode) {

        return createCall(toAddress, fromAddress, timeCode,
                Collections.emptyList(), Type.INVOKE);
    }

    /**
     * Create a Call with Type INVOKE.
     *
     * @param toAddress ControlAddress of receiving Control.
     * @param fromAddress ControlAddress for response.
     * @param timeCode long nanosecond time relative to hub clock
     * @param arg single Value which will be automatically wrapped in a
     * CallArguments object.
     * @return Call
     */
    public static Call create(
            ControlAddress toAddress,
            ControlAddress fromAddress,
            long timeCode,
            Value arg) {
        return createCall(toAddress, fromAddress, timeCode,
                Collections.singletonList(arg), Type.INVOKE);
    }

    /**
     * Create a Call with Type INVOKE.
     *
     * @param toAddress ControlAddress of receiving Control.
     * @param fromAddress ControlAddress for response.
     * @param timeCode long nanosecond time relative to hub clock
     * @param arg single Value which will be automatically wrapped in a
     * CallArguments object.
     * @return Call
     */
    @Deprecated
    public static Call createCall(
            ControlAddress toAddress,
            ControlAddress fromAddress,
            long timeCode,
            Value arg) {
        return createCall(toAddress, fromAddress, timeCode,
                Collections.singletonList(arg), Type.INVOKE);
    }

    /**
     * Create a Call with Type INVOKE.
     *
     * @param toAddress ControlAddress of receiving Control.
     * @param fromAddress ControlAddress for response.
     * @param timeCode long nanosecond time relative to hub clock
     * @param args List of arguments
     * @return Call
     */
    public static Call create(
            ControlAddress toAddress,
            ControlAddress fromAddress,
            long timeCode,
            List<Value> args) {
        return createCall(toAddress, fromAddress, timeCode,
                Collections.unmodifiableList(new ArrayList<>(args)),
                Type.INVOKE);
    }

    /**
     * Create a Call with Type INVOKE.
     *
     * @param toAddress ControlAddress of receiving Control.
     * @param fromAddress ControlAddress for response.
     * @param timeCode long nanosecond time relative to hub clock
     * @param args CallArguments (use CallArguments.EMPTY for no arguments)
     * @return Call
     */
    @Deprecated
    public static Call createCall(
            ControlAddress toAddress,
            ControlAddress fromAddress,
            long timeCode,
            CallArguments args) {
        return createCall(toAddress, fromAddress, timeCode,
                callArgsToListArgs(args),
                Type.INVOKE);
    }

    /**
     * Create a Call with Type INVOKE_QUIET and empty empty arguments. This
     * indicates that the sender does not require a response (though it might
     * still get one), except in case of error.
     *
     * @param toAddress ControlAddress of receiving Control.
     * @param fromAddress ControlAddress for response.
     * @param timeCode long nanosecond time relative to hub clock
     * @return Call
     */
    public static Call createQuiet(
            ControlAddress toAddress,
            ControlAddress fromAddress,
            long timeCode) {
        return createCall(toAddress, fromAddress, timeCode,
                Collections.emptyList(), Type.INVOKE_QUIET);
    }

    /**
     * Create a Call with Type INVOKE_QUIET and empty empty arguments. This
     * indicates that the sender does not require a response (though it might
     * still get one), except in case of error.
     *
     * @param toAddress ControlAddress of receiving Control.
     * @param fromAddress ControlAddress for response.
     * @param timeCode long nanosecond time relative to hub clock
     * @return Call
     */
    @Deprecated
    public static Call createQuietCall(
            ControlAddress toAddress,
            ControlAddress fromAddress,
            long timeCode) {
        return createCall(toAddress, fromAddress, timeCode,
                Collections.emptyList(), Type.INVOKE_QUIET);
    }

    /**
     * Create a Call with Type INVOKE_QUIET. This indicates that the sender does
     * not require a response (though it might still get one), except in case of
     * error.
     *
     * @param toAddress ControlAddress of receiving Control.
     * @param fromAddress ControlAddress for response.
     * @param timeCode long nanosecond time relative to hub clock
     * @param arg single Value which will be automatically wrapped in a
     * CallArguments object.
     * @return Call
     */
    public static Call createQuiet(
            ControlAddress toAddress,
            ControlAddress fromAddress,
            long timeCode,
            Value arg) {
        return createCall(toAddress, fromAddress, timeCode,
                Collections.singletonList(arg), Type.INVOKE_QUIET);
    }

    /**
     * Create a Call with Type INVOKE_QUIET. This indicates that the sender does
     * not require a response (though it might still get one), except in case of
     * error.
     *
     * @param toAddress ControlAddress of receiving Control.
     * @param fromAddress ControlAddress for response.
     * @param timeCode long nanosecond time relative to hub clock
     * @param arg single Value which will be automatically wrapped in a
     * CallArguments object.
     * @return Call
     */
    @Deprecated
    public static Call createQuietCall(
            ControlAddress toAddress,
            ControlAddress fromAddress,
            long timeCode,
            Value arg) {
        return createCall(toAddress, fromAddress, timeCode,
                Collections.singletonList(arg), Type.INVOKE_QUIET);
    }

    /**
     * Create a Call with Type INVOKE_QUIET. This indicates that the sender does
     * not require a response (though it might still get one), except in case of
     * error.
     *
     * @param toAddress ControlAddress of receiving Control.
     * @param fromAddress ControlAddress for response.
     * @param timeCode long nanosecond time relative to hub clock
     * @param args CallArguments
     * @return Call
     */
    @Deprecated
    public static Call createQuietCall(
            ControlAddress toAddress,
            ControlAddress fromAddress,
            long timeCode,
            CallArguments args) {
        return createCall(toAddress, fromAddress, timeCode,
                callArgsToListArgs(args),
                Type.INVOKE_QUIET);
    }

    /**
     * Create a Call with Type INVOKE_QUIET. This indicates that the sender does
     * not require a response (though it might still get one), except in case of
     * error.
     *
     * @param toAddress ControlAddress of receiving Control.
     * @param fromAddress ControlAddress for response.
     * @param timeCode long nanosecond time relative to hub clock
     * @param args CallArguments
     * @return Call
     */
    public static Call createQuiet(
            ControlAddress toAddress,
            ControlAddress fromAddress,
            long timeCode,
            List<Value> args) {
        return createCall(toAddress, fromAddress, timeCode,
                Collections.unmodifiableList(new ArrayList<>(args)),
                Type.INVOKE_QUIET);
    }

    private static Call createCall(
            ControlAddress toAddress,
            ControlAddress fromAddress,
            long timeCode,
            List<Value> args,
            Call.Type type) {
        if (toAddress == null || fromAddress == null) {
            throw new NullPointerException();
        }
        String root = toAddress.component().rootID();
        return new Call(root, toAddress, fromAddress, timeCode, args, type);
    }

    /**
     * Create a Call of type RETURN. Addresses, ID and timeCode will
     * automatically be set from the given inward Call.
     *
     * @param inwardCall Call this is a response to.
     * @return Call
     */
    @Deprecated
    public static Call createReturnCall(
            Call inwardCall) {
        return createResponseCall(inwardCall, Collections.emptyList(), Type.RETURN);
    }

    /**
     * Create a Call of type RETURN. Addresses, ID and timeCode will
     * automatically be set from the given inward Call.
     *
     * @param inwardCall Call this is a response to.
     * @param arg Value, will automatically be wrapped in a CallArguments
     * object.
     * @return Call
     */
    @Deprecated
    public static Call createReturnCall(
            Call inwardCall,
            Value arg) {
        return createResponseCall(inwardCall, Collections.singletonList(arg), Type.RETURN);
    }

    /**
     * Create a Call of type RETURN. Addresses, ID and timeCode will
     * automatically be set from the given inward Call.
     *
     * @param inwardCall Call this is a response to.
     * @param args Arguments
     * @return Call
     */
    @Deprecated
    public static Call createReturnCall(
            Call inwardCall,
            CallArguments args) {
        return createResponseCall(inwardCall, callArgsToListArgs(args), Type.RETURN);
    }

    /**
     * Create a Call of type ERROR. Addresses, ID and timeCode will
     * automatically be set from the given inward Call.
     *
     * @param inwardCall Call this is a response to.
     * @return Call
     */
    @Deprecated
    public static Call createErrorCall(
            Call inwardCall) {
        return createResponseCall(inwardCall, Collections.emptyList(), Type.ERROR);
    }

    /**
     * Create a Call of type ERROR. Addresses, ID and timeCode will
     * automatically be set from the given inward Call.
     *
     * @param inwardCall Call this is a response to.
     * @param arg Value, will automatically be wrapped in a CallArguments
     * object.
     * @return Call
     */
    @Deprecated
    public static Call createErrorCall(
            Call inwardCall,
            Value arg) {
        return createResponseCall(inwardCall, Collections.singletonList(arg), Type.ERROR);
    }

    /**
     * Create a Call of type ERROR. Addresses, ID and timeCode will
     * automatically be set from the given inward Call.
     *
     * @param inwardCall Call this is a response to.
     * @param args Arguments
     * @return Call
     */
    @Deprecated
    public static Call createErrorCall(
            Call inwardCall,
            CallArguments args) {
        return createResponseCall(inwardCall, callArgsToListArgs(args), Type.ERROR);
    }

    private static Call createResponseCall(
            Call inwardCall,
            List<Value> args,
            Call.Type type) {
        if (inwardCall == null) {
            throw new NullPointerException();
        }
        ControlAddress toAddress = inwardCall.from();
        ControlAddress fromAddress = inwardCall.to();
        String root = toAddress.component().componentID(0);
        long timeCode = inwardCall.time();
        int matchID = inwardCall.id();
        return new Call(root, toAddress, fromAddress, timeCode, args, type, matchID);
    }

    @SuppressWarnings("deprecation")
    private static List<Value> callArgsToListArgs(CallArguments args) {
        return Collections.unmodifiableList(args.stream().collect(Collectors.toList()));
    }

}
