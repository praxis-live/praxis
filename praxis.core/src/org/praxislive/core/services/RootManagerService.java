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

package org.praxislive.core.services;

import java.util.stream.Stream;
import org.praxislive.core.ComponentType;
import org.praxislive.core.ArgumentInfo;
import org.praxislive.core.ComponentInfo;
import org.praxislive.core.ControlInfo;
import org.praxislive.core.Info;
import org.praxislive.core.types.PArray;
import org.praxislive.core.types.PMap;
import org.praxislive.core.types.PString;

/**
 * @author Neil C Smith (http://neilcsmith.net)
 */
public class RootManagerService implements Service {

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

    public final static ComponentInfo API_INFO = Info.component(cmp -> cmp
            .protocol(RootManagerService.class)
            .control(ADD_ROOT, ADD_ROOT_INFO)
            .control(REMOVE_ROOT, REMOVE_ROOT_INFO)
            .control(ROOTS, ROOTS_INFO)
    );
    

    @Override
    public Stream<String> controls() {
        return Stream.of(ADD_ROOT, REMOVE_ROOT, ROOTS);
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
