/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Neil C Smith.
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
package net.neilcsmith.praxis.script.commands;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Map;
import net.neilcsmith.praxis.core.CallArguments;
import net.neilcsmith.praxis.core.syntax.InvalidSyntaxException;
import net.neilcsmith.praxis.core.types.PResource;
import net.neilcsmith.praxis.script.Command;
import net.neilcsmith.praxis.script.CommandInstaller;
import net.neilcsmith.praxis.script.ExecutionException;
import net.neilcsmith.praxis.script.Namespace;
import net.neilcsmith.praxis.script.StackFrame;
import net.neilcsmith.praxis.script.ast.RootNode;
import net.neilcsmith.praxis.script.ast.ScriptParser;

/**
 *
 * @author Neil C Smith (http://neilcsmith.net)
 */
public class ScriptCmds implements CommandInstaller {

    private final static ScriptCmds instance = new ScriptCmds();
    public final static Command EVAL = new Eval();
    public final static Command INLINE_EVAL = new InlineEval();
    public final static Command INCLUDE = new Include();

    private ScriptCmds() {
    }

    public void install(Map<String, Command> commands) {
        commands.put("eval", EVAL);
        commands.put("include", INCLUDE);
    }

    public static ScriptCmds getInstance() {
        return instance;
    }

    private static class Eval implements Command {

        public StackFrame createStackFrame(Namespace namespace, CallArguments args)
                throws ExecutionException {
            if (args.getSize() != 1) {
                throw new ExecutionException();
            }
            String script = args.get(0).toString();
            try {
                RootNode astRoot = ScriptParser.getInstance().parse(script);
                return new EvalStackFrame(namespace.createChild(), astRoot);
            } catch (InvalidSyntaxException ex) {
                throw new ExecutionException(ex);
            }
        }
    }

    private static class InlineEval implements Command {

        public StackFrame createStackFrame(Namespace namespace, CallArguments args)
                throws ExecutionException {
            if (args.getSize() != 1) {
                throw new ExecutionException();
            }
            String script = args.get(0).toString();
            try {
                RootNode astRoot = ScriptParser.getInstance().parse(script);
                return new EvalStackFrame(namespace, astRoot);
            } catch (InvalidSyntaxException ex) {
                throw new ExecutionException(ex);
            }
        }
    }

    private static class Include implements Command {

        public StackFrame createStackFrame(Namespace namespace, CallArguments args) throws ExecutionException {
            // @TODO - should load in background - call to
            if (args.getSize() != 1) {
                throw new ExecutionException();
            }
            try {
                PResource res = PResource.coerce(args.get(0));
                File file = new File(res.value());
                String script = Utils.loadStringFromFile(file);
                RootNode astRoot = ScriptParser.getInstance().parse(script);
                return new EvalStackFrame(namespace.createChild(), astRoot);
            } catch (Exception ex) {
                throw new ExecutionException(ex);
            }

        }

    }
}
