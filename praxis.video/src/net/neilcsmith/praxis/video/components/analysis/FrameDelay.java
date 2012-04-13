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
package net.neilcsmith.praxis.video.components.analysis;

import net.neilcsmith.praxis.core.ExecutionContext;
import net.neilcsmith.praxis.core.Port;
import net.neilcsmith.praxis.impl.AbstractExecutionContextComponent;
import net.neilcsmith.praxis.video.impl.DefaultVideoInputPort;
import net.neilcsmith.praxis.video.impl.DefaultVideoOutputPort;
import net.neilcsmith.ripl.Surface;
import net.neilcsmith.ripl.components.Delegator;
import net.neilcsmith.ripl.delegates.AbstractDelegate;

/**
 *
 * @author Neil C Smith <http://neilcsmith.net>
 */
public class FrameDelay extends AbstractExecutionContextComponent {
    
    private Delegator delegator;
    private DelegateImpl delegate;
    
    public FrameDelay() {
        delegate = new DelegateImpl();
        delegator = new Delegator(delegate);
        registerPort(Port.IN, new DefaultVideoInputPort(this, delegator));
        registerPort(Port.OUT, new DefaultVideoOutputPort(this, delegator));
    }

    public void stateChanged(ExecutionContext source) {
       delegate.reset();
    }

    
    private class DelegateImpl extends AbstractDelegate {
        
        private Surface previous;
        private Surface tmp;

        public void process(Surface surface) {
            if (previous == null || !surface.checkCompatible(surface, true, true)) {
                reset();
                previous = surface.createSurface(null);
                tmp = surface.createSurface(null);
                previous.copy(surface);
                surface.clear();
            } else {
                tmp.copy(surface);
                surface.copy(previous);
                Surface s = previous;
                previous = tmp;
                tmp = s;
                tmp.release();
            }
        }
        
        private void reset() {
            if (previous != null) {
                previous.release();
                previous = null;
            }
            if (tmp != null) {
                tmp.release();
                tmp = null;
            }
        }
        
    }
    
}
