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
import net.neilcsmith.praxis.core.services.ComponentManager;
import net.neilcsmith.praxis.core.services.ConnectionManager;
import net.neilcsmith.praxis.script.Command;
import net.neilcsmith.praxis.script.CommandInstaller;

/**
 *
 * @author Neil C Smith (http://neilcsmith.net)
 */
public class ComponentCmds implements CommandInstaller {

    private final static ComponentCmds instance = new ComponentCmds();

    private final static Command CREATE = new Create();
    private final static Command DESTROY = new Destroy();
    private final static Command CONNECT = new Connect();
    private final static Command DISCONNECT = new Disconnect();

    private ComponentCmds() {}

    public void install(Map<String, Command> commands) {
        commands.put("create", CREATE);
        commands.put("destroy", DESTROY);
        commands.put("connect", CONNECT);
        commands.put("~", CONNECT);
        commands.put("disconnect", DISCONNECT);
        commands.put("!~", DISCONNECT);
    }

    public final static ComponentCmds getInstance() {
        return instance;
    }


    private static class Create extends InterfaceCommand {
        private Create() {
            super(ComponentManager.getInstance(), ComponentManager.CREATE);
        }
    }

    private static class Destroy extends InterfaceCommand {
        private Destroy() {
            super(ComponentManager.getInstance(), ComponentManager.DESTROY);
        }
    }

    private static class Connect extends InterfaceCommand {
        private Connect() {
            super(ConnectionManager.getInstance(), ConnectionManager.CONNECT);
        }
    }

    private static class Disconnect extends InterfaceCommand {
        private Disconnect() {
            super(ConnectionManager.getInstance(), ConnectionManager.DISCONNECT);
        }
    }

}
