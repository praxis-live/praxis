/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2012 Neil C Smith.
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

package net.neilcsmith.praxis.video.components.source;

import net.neilcsmith.praxis.core.Port;
import net.neilcsmith.praxis.impl.AbstractComponent;
import net.neilcsmith.praxis.video.impl.DefaultVideoInputPort;
import net.neilcsmith.praxis.video.impl.DefaultVideoOutputPort;
import net.neilcsmith.praxis.video.pipes.impl.SingleInOut;
import net.neilcsmith.praxis.video.render.Surface;
import net.neilcsmith.praxis.video.render.SurfaceOp;

/**
 *
 * @author Neil C Smith
 */
public class Noise extends AbstractComponent {
    
    private NoisePipe noisePipe;
    
    public Noise() {
        noisePipe = new NoisePipe();
        registerPort(Port.IN, new DefaultVideoInputPort(this, noisePipe));
        registerPort(Port.OUT, new DefaultVideoOutputPort(this, noisePipe));
    }
    
    private class NoisePipe extends SingleInOut {

        private SurfaceOp op =
                net.neilcsmith.praxis.video.render.ops.Noise.op();
        
        @Override
        protected void process(Surface surface, boolean rendering) {
            if (rendering) {
                surface.process(op);
            }
            
        }
        
    }
    
}
