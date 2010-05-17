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

package net.neilcsmith.praxis.video.components.test;

import java.util.logging.Level;
import java.util.logging.Logger;
import net.neilcsmith.praxis.impl.AbstractComponent;
import net.neilcsmith.praxis.video.DefaultVideoInputPort;
import net.neilcsmith.praxis.video.DefaultVideoOutputPort;
import net.neilcsmith.ripl.components.Placeholder;
import net.neilcsmith.ripl.SinkIsFullException;
import net.neilcsmith.ripl.SourceIsFullException;

/**
 *
 * @author Neil C Smith
 */
public class Ripple extends AbstractComponent {

    private net.neilcsmith.ripl.components.temporal.Ripple rip;
    private Placeholder input;
    private Placeholder disturbance;
    
    public Ripple() {
        try {
            rip = new net.neilcsmith.ripl.components.temporal.Ripple();
            input = new Placeholder();
            disturbance = new Placeholder();
            rip.addSource(input);
            rip.addSource(disturbance);
            registerPort("input", new DefaultVideoInputPort(this, input));
            registerPort("disturbance", new DefaultVideoInputPort(this, disturbance));
            registerPort("output", new DefaultVideoOutputPort(this, rip));
        } catch (SinkIsFullException ex) {
            Logger.getLogger(Ripple.class.getName()).log(Level.SEVERE, null, ex);
        } catch (SourceIsFullException ex) {
            Logger.getLogger(Ripple.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
}
