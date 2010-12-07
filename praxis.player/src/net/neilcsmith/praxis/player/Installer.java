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
package net.neilcsmith.praxis.player;

import java.util.logging.Level;
import java.util.logging.Logger;
import net.neilcsmith.praxis.core.IllegalRootStateException;
import net.neilcsmith.praxis.hub.TaskServiceImpl;
import net.neilcsmith.praxis.hub.DefaultHub;
import net.neilcsmith.praxis.laf.PraxisLAFManager;
import net.neilcsmith.praxis.script.impl.ScriptServiceImpl;
import org.openide.modules.ModuleInstall;

/**
 * Manages a module's lifecycle. Remember that an installer is optional and
 * often not needed at all.
 */
public class Installer extends ModuleInstall {

    @Override
    public void restored() {

        
//        try {
//            PraxisLAFManager.getInstance().installUI();
//            DefaultHub hub = new DefaultHub(new ScriptServiceImpl(), new TaskServiceImpl(), new Player());
//            hub.activate();
//        } catch (IllegalRootStateException ex) {
//            Logger.getLogger(Installer.class.getName()).log(Level.SEVERE, null, ex);
//        }
    }
}
