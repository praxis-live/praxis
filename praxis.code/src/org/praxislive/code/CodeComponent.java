/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2018 Neil C Smith.
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
package org.praxislive.code;

import org.praxislive.core.Component;
import org.praxislive.core.ComponentAddress;
import org.praxislive.core.Container;
import org.praxislive.core.Control;
import org.praxislive.core.ControlAddress;
import org.praxislive.core.ExecutionContext;
import org.praxislive.core.Lookup;
import org.praxislive.core.PacketRouter;
import org.praxislive.core.Port;
import org.praxislive.core.VetoException;
import org.praxislive.core.ComponentInfo;
import org.praxislive.core.services.ServiceUnavailableException;
import org.praxislive.core.services.Services;
import org.praxislive.logging.LogLevel;
import org.praxislive.logging.LogService;

/**
 *
 * @author Neil C Smith <http://neilcsmith.net>
 */
public final class CodeComponent<D extends CodeDelegate> implements Component {

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
        for (String portID : codeCtxt.getPortIDs()) {
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
        codeCtxt.handleHierarchyChanged();
        if (parent == null) {
            codeCtxt.handleDispose();
        }
    }

    @Override
    public Control getControl(String id) {
        return codeCtxt.getControl(id);
    }

    @Override
    public Port getPort(String id) {
        return codeCtxt.getPort(id);
    }

    @Override
    public ComponentInfo getInfo() {
        return codeCtxt.getInfo();
    }

    Lookup getLookup() {
        if (parent != null) {
            return parent.getLookup();
        } else {
            return Lookup.EMPTY;
        }
    }

    void install(CodeContext<D> cc) {
        cc.setComponent(this);
        cc.handleConfigure(this, codeCtxt);
        if (codeCtxt != null) {
            codeCtxt.handleDispose();
        }
        codeCtxt = cc;
        codeCtxt.handleHierarchyChanged();
    }

    ComponentAddress getAddress() {
        return address;
    }
    
    ExecutionContext getExecutionContext() {
        if (execCtxt == null) {
            execCtxt = getLookup().find(ExecutionContext.class)
                    .orElse(null);
        }
        return execCtxt;
    }

    PacketRouter getPacketRouter() {
        if (router == null) {
            router = getLookup().find(PacketRouter.class)
                    .orElse(null);
        }
        return router;
    }
    
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
        ControlAddress toAddress = getLookup().find(Services.class)
                .flatMap(srvs -> srvs.locate(LogService.class))
                .map(srv -> ControlAddress.of(srv, LogService.LOG))
                .orElse(null);
        
        LogLevel level = getLookup().find(LogLevel.class).orElse(LogLevel.ERROR);
        
        if (toAddress == null) {
            level = LogLevel.ERROR;
        }
        
        ControlAddress fromAddress = ControlAddress.of(address, "_log");
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
