/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2017 Neil C Smith.
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
package net.neilcsmith.praxis.code.services;

import java.util.Map;
import net.neilcsmith.praxis.code.CodeCompilerService;
import net.neilcsmith.praxis.core.Call;
import net.neilcsmith.praxis.core.CallArguments;
import net.neilcsmith.praxis.core.ComponentAddress;
import net.neilcsmith.praxis.core.ControlAddress;
import net.neilcsmith.praxis.core.interfaces.Services;
import net.neilcsmith.praxis.core.types.PArray;
import net.neilcsmith.praxis.core.types.Value;
import net.neilcsmith.praxis.script.Command;
import net.neilcsmith.praxis.script.CommandInstaller;
import net.neilcsmith.praxis.script.Env;
import net.neilcsmith.praxis.script.ExecutionException;
import net.neilcsmith.praxis.script.Namespace;
import net.neilcsmith.praxis.script.StackFrame;
import net.neilcsmith.praxis.script.impl.AbstractSingleCallFrame;

/**
 *
 * @author Neil C Smith - http://www.neilcsmith.net
 */
public class LibraryCommandInstaller implements CommandInstaller {

    private final static AddLibsCmd ADD_LIB = new AddLibsCmd(false);
    private final static AddLibsCmd ADD_LIBS = new AddLibsCmd(true);
    
    
    @Override
    public void install(Map<String, Command> commands) {
        commands.put("add-lib", ADD_LIB);
        commands.put("add-libs", ADD_LIBS);
    }
    
    private static class AddLibsCmd implements Command {

        private final boolean array;

        AddLibsCmd(boolean array) {
            this.array = array;
        }
        
        @Override
        public StackFrame createStackFrame(Namespace namespace, CallArguments args) throws ExecutionException {
            return new AddLibsStackFrame(namespace, args, array);
        }
        
    }
    
    private static class AddLibsStackFrame extends AbstractSingleCallFrame {
        
        private final boolean array;
        
        AddLibsStackFrame(Namespace namespace, CallArguments args, boolean array) {
            super(namespace, args);
            this.array = array;
        }

        @Override
        protected Call createCall(Env env, CallArguments args) throws Exception {
            PArray libs = array ? PArray.coerce(args.get(0)) : PArray.valueOf((Value)args.get(0));
            ComponentAddress service = env.getLookup().get(Services.class).locate(CodeCompilerService.class).get();
            ControlAddress addLibsControl = ControlAddress.create(service, "add-libs");
            return Call.createCall(addLibsControl, env.getAddress(), env.getTime(), libs);
        }
        
    }
    
}
