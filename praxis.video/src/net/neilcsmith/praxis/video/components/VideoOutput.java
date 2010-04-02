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
import net.neilcsmith.praxis.core.Container;
import net.neilcsmith.praxis.core.ParentVetoException;
import net.neilcsmith.praxis.impl.AbstractComponent;
import net.neilcsmith.praxis.video.DefaultVideoInputPort;
import net.neilcsmith.praxis.video.VideoOutputClient;
import net.neilcsmith.praxis.video.VideoServer;
import net.neilcsmith.ripl.components.Placeholder;
import net.neilcsmith.ripl.core.Source;

/**
 *
 * @author Neil C Smith
 */
public class VideoOutput extends AbstractComponent implements VideoOutputClient {
    
//    private VideoOutputProxy proxy;
//    private Sink sink;
    private Placeholder placeholder;

    public VideoOutput() {
        placeholder = new Placeholder();
        registerPort("input", new DefaultVideoInputPort(this, placeholder));
    }

    @Override
    public void parentNotify(Container parent) throws ParentVetoException {
        super.parentNotify(parent);
        if (parent instanceof VideoServer) {
            try {
                ((VideoServer) parent).registerVideoOutputClient(this);
            } catch (Exception ex) {
                Logger.getLogger(VideoOutput.class.getName()).log(Level.SEVERE, null, ex);
                throw new ParentVetoException();
            }
        }
    }

    public int getOutputCount() {
        return 1;
    }

    public Source getOutputSource(int index) {
        if (index == 0) {
            return placeholder;
        } else {
            throw new IndexOutOfBoundsException();
        }
    }
    
    
    
}
