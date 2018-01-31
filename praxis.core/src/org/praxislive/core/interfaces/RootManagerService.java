/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2014 Neil C Smith.
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

package org.praxislive.core.interfaces;

import org.praxislive.core.ComponentType;
import org.praxislive.core.info.ArgumentInfo;
import org.praxislive.core.info.ControlInfo;
import org.praxislive.core.types.PArray;
import org.praxislive.core.types.PMap;
import org.praxislive.core.types.PString;

/**
 * @author Neil C Smith (http://neilcsmith.net)
 */
public class RootManagerService extends Service {

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
