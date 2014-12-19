/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2014 Neil C Smith.
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
package net.neilcsmith.praxis.code;

import net.neilcsmith.praxis.core.Component;
import net.neilcsmith.praxis.core.ComponentAddress;
import net.neilcsmith.praxis.core.Container;
import net.neilcsmith.praxis.core.Control;
import net.neilcsmith.praxis.core.ControlAddress;
import net.neilcsmith.praxis.core.ExecutionContext;
import net.neilcsmith.praxis.core.InterfaceDefinition;
import net.neilcsmith.praxis.core.Lookup;
import net.neilcsmith.praxis.core.PacketRouter;
import net.neilcsmith.praxis.core.Port;
import net.neilcsmith.praxis.core.VetoException;
import net.neilcsmith.praxis.core.info.ComponentInfo;
import net.neilcsmith.praxis.core.interfaces.ServiceUnavailableException;
import net.neilcsmith.praxis.core.interfaces.Services;
import net.neilcsmith.praxis.logging.LogBuilder;
import net.neilcsmith.praxis.logging.LogLevel;
import net.neilcsmith.praxis.logging.LogService;

/**
 *
 * @author Neil C Smith <http://neilcsmith.net>
 */
public class CodeComponent<D extends CodeDelegate> implements Component {

    private Container parent;
    private CodeContext<D> codeCtxt;
    private ComponentAddress address;
    private ExecutionContext execCtxt;
    private PacketRouter router;
    private LogInfo logInfo;

    CodeComponent() {

    }

    @Override
    public Container getParent() {
        return parent;
    }

    @Override
    public final void parentNotify(Container parent) throws VetoException {
        if (parent == null) {
            if (this.parent != null) {
                this.parent = null;
                disconnectAll();
            }
        } else {
            if (this.parent != null) {
                throw new VetoException();
            }
            this.parent = parent;
        }
    }

    private void disconnectAll() {
        for (String portID : getPortIDs()) {
            getPort(portID).disconnectAll();
        }
    }

    @Override
    public void hierarchyChanged() {
        if (parent != null) {
            address = parent.getAddress(this);
        } else {
            address = null;
        }
        execCtxt = null;
        router = null;
        logInfo = null;
        codeCtxt.hierarchyChanged();
    }

    @Override
    public Control getControl(String id) {
        return codeCtxt.getControl(id);
    }

    @Override
    public String[] getControlIDs() {
        return codeCtxt.getControlIDs();
    }

    @Override
    public Port getPort(String id) {
        return codeCtxt.getPort(id);
    }

    @Override
    public String[] getPortIDs() {
        return codeCtxt.getPortIDs();
    }

    @Override
    public ComponentInfo getInfo() {
        return codeCtxt.getInfo();
    }

    @Override
    public InterfaceDefinition[] getInterfaces() {
        return codeCtxt.getInterfaces();
    }

    Lookup getLookup() {
        if (parent != null) {
            return parent.getLookup();
        } else {
            return Lookup.EMPTY;
        }
    }

    void install(CodeContext<D> cc) {
        cc.configure(this, codeCtxt);
        if (codeCtxt != null) {
            codeCtxt.dispose();
        }
        codeCtxt = cc;
    }

    ComponentAddress getAddress() {
        return address;
    }
    
    ExecutionContext getExecutionContext() {
        if (execCtxt == null) {
            execCtxt = getLookup().get(ExecutionContext.class);
        }
        return execCtxt;
    }

    PacketRouter getPacketRouter() {
        if (router == null) {
            router = getLookup().get(PacketRouter.class);
        }
        return router;
    }
    
//    LogLevel getLogLevel() {
//        if (logInfo == null) {
//            initLogInfo();
//        }
//        return logInfo.level;
//    }
    
    ControlAddress getLogToAddress() {
        if (logInfo == null) {
            initLogInfo();
        }
        return logInfo.toAddress;
    }
    
    ControlAddress getLogFromAddress() {
         if (logInfo == null) {
            initLogInfo();
        }
        return logInfo.fromAddress;
    }
    
    private void initLogInfo() {
        ControlAddress toAddress = null;
        Services srvs = getLookup().get(Services.class);
        if (srvs != null) {
            ComponentAddress srv;
            try {
                srv = srvs.findService(LogService.class);
                toAddress = ControlAddress.create(srv, LogService.LOG);
            } catch (ServiceUnavailableException ex) {
            }
        }
        LogLevel level = getLookup().get(LogLevel.class);
        if (level == null || toAddress == null) {
            level = LogLevel.ERROR;
        }
        ControlAddress fromAddress = ControlAddress.create(address, "_log");
        logInfo = new LogInfo(level, toAddress, fromAddress);
    }

    private static class LogInfo {

        private final LogLevel level;
        private final ControlAddress toAddress;
        private final ControlAddress fromAddress;

        private LogInfo(LogLevel level,
                ControlAddress toAddress,
                ControlAddress fromAddress) {
            this.level = level;
            this.toAddress = toAddress;
            this.fromAddress = fromAddress;
        }
    }

}
