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
import org.praxislive.core.ArgumentInfo;
import org.praxislive.core.ComponentInfo;
import org.praxislive.core.ControlInfo;

/**
 *
 * @author Neil C Smith (http://neilcsmith.net)
 */
public class ComponentInterface extends InterfaceDefinition {

    public final static ComponentInterface INSTANCE = new ComponentInterface();
    public final static String INFO = "info";
    public final static ControlInfo INFO_INFO = ControlInfo.createReadOnlyPropertyInfo(
                new ArgumentInfo[]{ComponentInfo.info()},
                null);;

    @Override
    public String[] getControls() {
        return new String[]{INFO};
    }

    @Override
    public ControlInfo getControlInfo(String control) {
        if (INFO.equals(control)) {
            return INFO_INFO;
        }
        throw new IllegalArgumentException();
    }
}


