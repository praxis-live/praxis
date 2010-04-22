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

import net.neilcsmith.praxis.core.Argument;
import net.neilcsmith.praxis.core.Task;
import net.neilcsmith.praxis.core.info.ArgumentInfo;
import net.neilcsmith.praxis.core.info.ControlInfo;
import net.neilcsmith.praxis.core.types.PReference;

/**
 *
 * @author Neil C Smith (http://neilcsmith.net)
 */
public class AuxillaryThreadService extends InterfaceDefinition {

    public final static String SUBMIT = "submit";

    private final static AuxillaryThreadService instance = new AuxillaryThreadService();
    private ControlInfo submitInfo;

    private AuxillaryThreadService() {
        ArgumentInfo input = PReference.info(Task.class);
        ArgumentInfo output = Argument.info();
        submitInfo = ControlInfo.create(
                new ArgumentInfo[]{input},
                new ArgumentInfo[]{output},
                null);
    }

    @Override
    public String[] getControls() {
        return new String[]{SUBMIT};
    }

    @Override
    public ControlInfo getControlInfo(String control) {
        if (SUBMIT.equals(control)) {
            return submitInfo;
        }
        throw new IllegalArgumentException();
    }

    public static AuxillaryThreadService getInstance() {
        return instance;
    }
}
