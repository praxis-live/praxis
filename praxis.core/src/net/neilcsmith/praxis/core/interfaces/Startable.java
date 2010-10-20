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

package net.neilcsmith.praxis.core.interfaces;

import net.neilcsmith.praxis.core.InterfaceDefinition;
import net.neilcsmith.praxis.core.info.ArgumentInfo;
import net.neilcsmith.praxis.core.info.ControlInfo;
import net.neilcsmith.praxis.core.types.PBoolean;

/**
 *
 * @author Neil C Smith (http://neilcsmith.net)
 */
public class Startable {
    
    public final static String START = "start";
    public final static String STOP = "stop";
    public final static String IS_RUNNING = "is-running";
    public final static Definition DEFINITION = new Definition();

    private Startable() {}


    public static class Definition extends InterfaceDefinition {

        private ControlInfo startInfo;
        private ControlInfo stopInfo;
        private ControlInfo isRunningInfo;

        private Definition() {
            startInfo = ControlInfo.createFunctionInfo(
                    new ArgumentInfo[0],
                    new ArgumentInfo[0],
                    null);
            stopInfo = ControlInfo.createFunctionInfo(
                    new ArgumentInfo[0],
                    new ArgumentInfo[0],
                    null);
            isRunningInfo = ControlInfo.createFunctionInfo(
                    new ArgumentInfo[0],
                    new ArgumentInfo[]{PBoolean.info()},
                    null);

        }

        @Override
        public String[] getControls() {
            return new String[] {START, STOP, IS_RUNNING};
        }

        @Override
        public ControlInfo getControlInfo(String control) {
            if (START.equals(control)) {
                return startInfo;
            }
            if (STOP.equals(control)) {
                return stopInfo;
            }
            if (IS_RUNNING.equals(control)) {
                return isRunningInfo;
            }
            throw new IllegalArgumentException();
        }

    }

}
