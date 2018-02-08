/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2016 Neil C Smith.
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
import org.praxislive.core.Root;
import org.praxislive.core.ArgumentInfo;
import org.praxislive.core.ControlInfo;
import org.praxislive.core.types.PMap;
import org.praxislive.core.types.PReference;

/**
 *
 * @author Neil C Smith (http://neilcsmith.net)
 */
public class RootFactoryService extends Service {

    @Deprecated
    public final static RootFactoryService INSTANCE = new RootFactoryService();
    public final static String NEW_ROOT_INSTANCE = "new-root-instance";
    public final static ControlInfo NEW_ROOT_INSTANCE_INFO =
            ControlInfo.createFunctionInfo(
            new ArgumentInfo[]{ComponentType.info()},
            new ArgumentInfo[]{PReference.info(Root.class)},
            PMap.EMPTY);

    @Override
    public String[] getControls() {
        return new String[]{NEW_ROOT_INSTANCE};
    }

    @Override
    public ControlInfo getControlInfo(String control) {
        if (NEW_ROOT_INSTANCE.equals(control)) {
            return NEW_ROOT_INSTANCE_INFO;
        }
        throw new IllegalArgumentException();
    }
}


