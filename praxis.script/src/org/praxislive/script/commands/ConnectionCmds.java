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
package org.praxislive.script.commands;

import org.praxislive.script.impl.AbstractSingleCallFrame;
import java.util.Map;
import org.praxislive.core.Call;
import org.praxislive.core.CallArguments;
import org.praxislive.core.ComponentAddress;
import org.praxislive.core.ControlAddress;
import org.praxislive.core.PortAddress;
import org.praxislive.core.protocols.ContainerProtocol;
import org.praxislive.core.types.PString;
import org.praxislive.script.Command;
import org.praxislive.script.CommandInstaller;
import org.praxislive.script.Env;
import org.praxislive.script.ExecutionException;
import org.praxislive.script.Namespace;
import org.praxislive.script.StackFrame;

/**
 *
 * @author Neil C Smith (http://neilcsmith.net)
 */
public class ConnectionCmds implements CommandInstaller {

    private final static ConnectionCmds instance = new ConnectionCmds();
    private final static Connect CONNECT = new Connect();
    private final static Disconnect DISCONNECT = new Disconnect();

    private ConnectionCmds() {
    }

    public void install(Map<String, Command> commands) {
        commands.put("connect", CONNECT);
        commands.put("~", CONNECT);
        commands.put("disconnect", DISCONNECT);
        commands.put("!~", DISCONNECT);
    }

    public final static ConnectionCmds getInstance() {
        return instance;
    }

    private static class Connect implements Command {

        public StackFrame createStackFrame(Namespace namespace, CallArguments args) throws ExecutionException {
            return new ConnectionStackFrame(namespace, args, true);
        }
    }
    
    private static class Disconnect implements Command {
        
        public StackFrame createStackFrame(Namespace namespace, CallArguments args) throws ExecutionException {
            return new ConnectionStackFrame(namespace, args, false);
        }
        
    }

    private static class ConnectionStackFrame extends AbstractSingleCallFrame {

        private boolean connect;

        private ConnectionStackFrame(Namespace namespace, CallArguments args, boolean connect) {
            super(namespace, args);
            this.connect = connect;
        }

        @Override
        protected Call createCall(Env env, CallArguments args) throws Exception {
            PortAddress p1 = PortAddress.coerce(args.get(0));
            PortAddress p2 = PortAddress.coerce(args.get(1));
            ComponentAddress c1 = p1.component();
            ComponentAddress c2 = p2.component();
            ComponentAddress container = c1.parent();
            if (container == null || !c2.parent().equals(container)) {
                throw new IllegalArgumentException("Ports don't share a common parent");
            }
            CallArguments sendArgs = CallArguments.create(
                    PString.of(c1.componentID(c1.depth() - 1)),
                    PString.of(p1.portID()),
                    PString.of(c2.componentID(c1.depth() - 1)),
                    PString.of(p2.portID()));
            ControlAddress to = ControlAddress.of(container,
                    connect ? ContainerProtocol.CONNECT : ContainerProtocol.DISCONNECT);
            return Call.createCall(to, env.getAddress(), env.getTime(), sendArgs);

        }
    }
}
