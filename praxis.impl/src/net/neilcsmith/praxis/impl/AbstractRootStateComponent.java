/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2008 - Neil C Smith. All rights reserved.
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

package net.neilcsmith.praxis.impl;

import net.neilcsmith.praxis.core.ComponentAddress;
import net.neilcsmith.praxis.core.Root;
import net.neilcsmith.praxis.core.Root.State;

/**
 *
 * @author Neil C Smith
 */
public abstract class AbstractRootStateComponent extends AbstractComponent implements RootStateListener {


    @Override
    public void hierarchyChanged() {
        Root root = getRoot();
        if (root instanceof AbstractRoot) {
            ((AbstractRoot)root).removeRootStateListener(this);
        }
        super.hierarchyChanged();
        root = getRoot();
        if (root instanceof AbstractRoot) {
            ((AbstractRoot)root).addRootStateListener(this);
        }
    }



    public int getDepth() {
        ComponentAddress ad = getAddress();
        if (ad == null) {
            return 0;
        } else {
            return ad.getDepth();
        }
    }

    public int getPriority() {
        return 0;
    }

}
