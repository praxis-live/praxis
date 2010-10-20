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
import net.neilcsmith.praxis.core.info.ComponentInfo;
import net.neilcsmith.praxis.core.info.ControlInfo;

/**
 *
 * @author Neil C Smith (http://neilcsmith.net)
 */
public class ComponentInterface {
    
    public final static String INFO = "info";
    public final static Definition DEFINITION = new Definition();

    private ComponentInterface() {}


    public static class Definition extends InterfaceDefinition {

        private ControlInfo infoInfo;

        private Definition() {
            infoInfo = ControlInfo.createFunctionInfo(
                    new ArgumentInfo[0],
                    new ArgumentInfo[] {ComponentInfo.info()},
                    null);
        }

        @Override
        public String[] getControls() {
            return new String[] {INFO};
        }

        @Override
        public ControlInfo getControlInfo(String control) {
            if (INFO.equals(control)) {
                return infoInfo;
            }
            throw new IllegalArgumentException();
        }

    }

}
