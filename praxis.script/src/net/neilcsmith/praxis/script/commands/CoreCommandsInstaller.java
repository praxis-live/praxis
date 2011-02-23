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
import net.neilcsmith.praxis.script.Command;
import net.neilcsmith.praxis.script.CommandInstaller;

/**
 *
 * @author Neil C Smith (http://neilcsmith.net)
 */
public class CoreCommandsInstaller implements CommandInstaller {

    public void install(Map<String, Command> commands) {
        ScriptCmds.getInstance().install(commands);
//        ComponentCmds.getInstance().install(commands);
        ConnectionCmds.getInstance().install(commands);
        FileCmds.getInstance().install(commands);
        VariableCmds.getInstance().install(commands);
        AtCmds.getInstance().install(commands);
        ArrayCmds.getInstance().install(commands);
    }

}
