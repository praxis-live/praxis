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
import net.neilcsmith.praxis.core.ComponentType;
import net.neilcsmith.praxis.core.info.ArgumentInfo;
import net.neilcsmith.praxis.core.info.ControlInfo;
import net.neilcsmith.praxis.core.types.PArray;
import net.neilcsmith.praxis.core.types.PMap;
import net.neilcsmith.praxis.core.types.PString;

/**
 * @author Neil C Smith (http://neilcsmith.net)
 */
public class RootManagerService extends InterfaceDefinition {

    public final static RootManagerService INSTANCE = new RootManagerService();

    public final static String ADD_ROOT = "add-root";
    public final static String REMOVE_ROOT = "remove-root";
    public final static String ROOTS = "roots";
    public final static ControlInfo ADD_ROOT_INFO =
            ControlInfo.createFunctionInfo(
                new ArgumentInfo[] {PString.info(), ComponentType.info()},
                new ArgumentInfo[0],
                PMap.EMPTY);
    public final static ControlInfo REMOVE_ROOT_INFO =
            ControlInfo.createFunctionInfo(
            new ArgumentInfo[]{PString.info()},
            new ArgumentInfo[0],
            PMap.EMPTY);
    public final static ControlInfo ROOTS_INFO =
            ControlInfo.createReadOnlyPropertyInfo(
            new ArgumentInfo[]{PArray.info()},
            PMap.EMPTY);


    @Override
    public String[] getControls() {
        return new String[] {ADD_ROOT, REMOVE_ROOT, ROOTS};
    }

    @Override
    public ControlInfo getControlInfo(String control) {
        if (ADD_ROOT.equals(control)) {
            return ADD_ROOT_INFO;
        }
        if (REMOVE_ROOT.equals(control)) {
            return REMOVE_ROOT_INFO;
        }
        if (ROOTS.equals(control)) {
            return ROOTS_INFO;
        }
        throw new IllegalArgumentException();
    }

}
