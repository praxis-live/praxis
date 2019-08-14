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
package org.praxislive.code.services;

import java.util.Map;
import org.praxislive.code.CodeCompilerService;
import org.praxislive.core.Call;
import org.praxislive.core.CallArguments;
import org.praxislive.core.ComponentAddress;
import org.praxislive.core.ControlAddress;
import org.praxislive.core.services.Services;
import org.praxislive.core.types.PArray;
import org.praxislive.core.Value;
import org.praxislive.core.services.ServiceUnavailableException;
import org.praxislive.script.Command;
import org.praxislive.script.CommandInstaller;
import org.praxislive.script.Env;
import org.praxislive.script.ExecutionException;
import org.praxislive.script.Namespace;
import org.praxislive.script.StackFrame;
import org.praxislive.script.impl.AbstractSingleCallFrame;

/**
 *
 * @author Neil C Smith - http://www.neilcsmith.net
 */
public class CompilerCommandInstaller implements CommandInstaller {

    private final static AddLibsCmd ADD_LIB = new AddLibsCmd(false);
    private final static AddLibsCmd ADD_LIBS = new AddLibsCmd(true);
    private final static JavaReleaseCmd RELEASE = new JavaReleaseCmd();
    
    
    @Override
    public void install(Map<String, Command> commands) {
        commands.put("add-lib", ADD_LIB);
        commands.put("add-libs", ADD_LIBS);
        commands.put("java-compiler-release", RELEASE);
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
            PArray libs = array ? PArray.coerce(args.get(0)) : PArray.of((Value)args.get(0));
            ComponentAddress service = env.getLookup().find(Services.class)
                    .flatMap(sm -> sm.locate(CodeCompilerService.class))
                    .orElseThrow(ServiceUnavailableException::new);
            ControlAddress addLibsControl = ControlAddress.of(service, "add-libs");
            return Call.create(addLibsControl, env.getAddress(), env.getTime(), libs);
        }
        
    }
    
    private static class JavaReleaseCmd implements Command {

        @Override
        public StackFrame createStackFrame(Namespace namespace, CallArguments args) throws ExecutionException {
            return new JavaReleaseStackFrame(namespace, args);
        }
        
    }
    
    private static class JavaReleaseStackFrame extends AbstractSingleCallFrame {
        
        JavaReleaseStackFrame(Namespace namespace, CallArguments args) {
            super(namespace, args);
        }

        @Override
        protected Call createCall(Env env, CallArguments args) throws Exception {
            ComponentAddress service = env.getLookup().find(Services.class)
                    .flatMap(sm -> sm.locate(CodeCompilerService.class))
                    .orElseThrow(ServiceUnavailableException::new);
            ControlAddress releaseControl = ControlAddress.of(service, "release");
            return Call.createCall(releaseControl, env.getAddress(), env.getTime(), args);
        }
        
    }
    
}
