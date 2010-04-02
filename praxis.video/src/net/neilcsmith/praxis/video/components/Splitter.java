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

package net.neilcsmith.praxis.video.components;

import java.util.logging.Level;
import java.util.logging.Logger;
import net.neilcsmith.praxis.impl.AbstractComponent;
import net.neilcsmith.praxis.video.DefaultVideoInputPort;
import net.neilcsmith.praxis.video.DefaultVideoOutputPort;
import net.neilcsmith.ripl.components.Placeholder;
import net.neilcsmith.ripl.core.SinkIsFullException;
import net.neilcsmith.ripl.core.SourceIsFullException;

/**
 *
 * @author Neil C Smith
 */
public class Splitter extends AbstractComponent {
    
    private net.neilcsmith.ripl.components.Splitter spl;
    private Placeholder out1;
    private Placeholder out2;
    
    public Splitter() {
        try {
            spl = new net.neilcsmith.ripl.components.Splitter();
            out1 = new Placeholder();
            out2 = new Placeholder();
            out1.addSource(spl);
            out2.addSource(spl);
            registerPort("input", new DefaultVideoInputPort(this, spl));
            registerPort("output-1", new DefaultVideoOutputPort(this, out1));
            registerPort("output-2", new DefaultVideoOutputPort(this, out2));
            
        } catch (SinkIsFullException ex) {
            Logger.getLogger(Splitter.class.getName()).log(Level.SEVERE, null, ex);
        } catch (SourceIsFullException ex) {
            Logger.getLogger(Splitter.class.getName()).log(Level.SEVERE, null, ex);
        }
        
    }

}
