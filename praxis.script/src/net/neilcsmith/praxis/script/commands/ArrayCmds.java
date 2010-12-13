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

import java.util.Map;
import net.neilcsmith.praxis.core.CallArguments;
import net.neilcsmith.praxis.core.types.PArray;
import net.neilcsmith.praxis.script.Command;
import net.neilcsmith.praxis.script.CommandInstaller;
import net.neilcsmith.praxis.script.Env;
import net.neilcsmith.praxis.script.ExecutionException;
import net.neilcsmith.praxis.script.Namespace;

/**
 *
 * @author Neil C Smith (http://neilcsmith.net)
 */
public class ArrayCmds implements CommandInstaller {
    
    private final static ArrayCmds INSTANCE = new ArrayCmds();

    private final static Array ARRAY = new Array();

    private ArrayCmds() {}

    public void install(Map<String, Command> commands) {
        commands.put("array", ARRAY);
    }

    public final static ArrayCmds getInstance() {
        return INSTANCE;
    }

    private static class Array extends AbstractInlineCommand {

        public CallArguments process(Env context, Namespace namespace, CallArguments args) throws ExecutionException {
            return CallArguments.create(PArray.valueOf(args));
        }

    }

}
