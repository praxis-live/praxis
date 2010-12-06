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
package net.neilcsmith.praxis.video.janino;

import net.neilcsmith.praxis.video.impl.DefaultVideoInputPort;
import net.neilcsmith.praxis.video.impl.DefaultVideoOutputPort;
import net.neilcsmith.ripl.components.Delegator;

/**
 *
 * @author Neil C Smith (http://neilcsmith.net)
 */
public class JaninoVideoProcessor extends AbstractJaninoComponent {

    private Delegator delegator;

    public JaninoVideoProcessor() {
        setupDelegator();     
    }

    private void setupDelegator() {
        this.delegator = new Delegator();
        registerPort("in", new DefaultVideoInputPort(this, delegator));
        registerPort("out", new DefaultVideoOutputPort(this, delegator));
    }

    void installDelegate(JaninoVideoDelegate delegate) {
        delegator.setDelegate(delegate);
    }

   
}
