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
import net.neilcsmith.praxis.core.types.PString;

/**
 *
 * @author Neil C Smith (http://neilcsmith.net)
 */
public class ScriptService extends InterfaceDefinition {

    public final static String EVAL = "eval";
    public final static ScriptService INSTANCE = new ScriptService();
    private final static ControlInfo EVAL_INFO = ControlInfo.createFunctionInfo(
                new ArgumentInfo[]{PString.info()},
                new ArgumentInfo[0],
                null);

    @Override
    public String[] getControls() {
        return new String[]{EVAL};
    }

    @Override
    public ControlInfo getControlInfo(String control) {
        if (EVAL.equals(control)) {
            return EVAL_INFO;
        }
        throw new IllegalArgumentException();
    }
}
