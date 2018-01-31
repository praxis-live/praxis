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

package org.praxislive.core.interfaces;

import org.praxislive.core.InterfaceDefinition;
import org.praxislive.core.ComponentAddress;
import org.praxislive.core.ComponentType;
import org.praxislive.core.info.ArgumentInfo;
import org.praxislive.core.info.ControlInfo;

/**
 *
 * @author Neil C Smith (http://neilcsmith.net)
 */
@Deprecated
public class ComponentManager extends InterfaceDefinition {

    public final static String CREATE = "create";
    public final static String DESTROY = "destroy";

    private final static ComponentManager instance = new ComponentManager();

    private ControlInfo createInfo;
    private ControlInfo destroyInfo;

    private ComponentManager() {
        createInfo = ControlInfo.createFunctionInfo(
                new ArgumentInfo[] {ComponentAddress.info(), ComponentType.info()},
                new ArgumentInfo[0], null);
        destroyInfo = ControlInfo.createFunctionInfo(
                new ArgumentInfo[] {ComponentAddress.info()},
                new ArgumentInfo[0], null);

    }


    @Override
    public String[] getControls() {
        return new String[] {CREATE, DESTROY};
    }

    @Override
    public ControlInfo getControlInfo(String control) {
        if (CREATE.equals(control)) {
            return createInfo;
        }
        if (DESTROY.equals(control)) {
            return destroyInfo;
        }
        throw new IllegalArgumentException();
    }

    public static ComponentManager getInstance() {
        return instance;
    }

}
