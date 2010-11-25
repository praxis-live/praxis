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

package net.neilcsmith.praxis.gui;

import javax.swing.JComponent;
import net.neilcsmith.praxis.core.Component;

/**
 * Extension of standard Praxis Component interface for components that wrap
 * a Swing component.
 *
 * @author Neil C Smith
 */
public interface GuiComponent extends Component {
    
    /**
     * Return Swing component (JComponent). The JComponent can be lazily created
     * but this method should always return the same component whenever called.
     *
     * May return null if not called on the Event Dispatch Thread.
     *
     * @return JComponent
     */
    public JComponent getSwingComponent();

}
