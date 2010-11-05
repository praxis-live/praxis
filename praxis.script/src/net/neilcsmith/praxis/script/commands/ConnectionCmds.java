/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 - Neil C Smith. All rights reserved.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details.
 *
 * You should have received a copy of the GNU General Public License version 2
 * along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please visit http://neilcsmith.net if you need additional information or
 * have any questions.
 */
package net.neilcsmith.praxis.script.commands;

import java.util.Map;
import net.neilcsmith.praxis.core.Call;
import net.neilcsmith.praxis.core.CallArguments;
import net.neilcsmith.praxis.core.ComponentAddress;
import net.neilcsmith.praxis.core.ControlAddress;
import net.neilcsmith.praxis.core.PortAddress;
import net.neilcsmith.praxis.core.interfaces.ContainerInterface;
import net.neilcsmith.praxis.core.types.PString;
import net.neilcsmith.praxis.script.Command;
import net.neilcsmith.praxis.script.CommandInstaller;
import net.neilcsmith.praxis.script.Env;
import net.neilcsmith.praxis.script.ExecutionException;
import net.neilcsmith.praxis.script.Namespace;
import net.neilcsmith.praxis.script.StackFrame;

/**
 *
 * @author Neil C Smith (http://neilcsmith.net)
 */
public class ConnectionCmds implements CommandInstaller {

    private final static ConnectionCmds instance = new ConnectionCmds();
    private final static Command CONNECT = new Connect();
    private final static Command DISCONNECT = new Disconnect();

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
            PortAddress p1 = PortAddress.coerce(args.getArg(0));
            PortAddress p2 = PortAddress.coerce(args.getArg(1));
            ComponentAddress c1 = p1.getComponentAddress();
            ComponentAddress c2 = p2.getComponentAddress();
            ComponentAddress container = c1.getParentAddress();
            if (container == null || !c2.getParentAddress().equals(container)) {
                throw new IllegalArgumentException("Ports don't share a common parent");
            }
            CallArguments sendArgs = CallArguments.create(
                    PString.valueOf(c1.getComponentID(c1.getDepth() - 1)),
                    PString.valueOf(p1.getID()),
                    PString.valueOf(c2.getComponentID(c1.getDepth() - 1)),
                    PString.valueOf(p2.getID()));
            ControlAddress to = ControlAddress.create(container,
                    connect ? ContainerInterface.CONNECT : ContainerInterface.DISCONNECT);
            return Call.createCall(to, env.getAddress(), env.getTime(), sendArgs);

        }
    }
}
