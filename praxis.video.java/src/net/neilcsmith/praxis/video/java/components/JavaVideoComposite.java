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

package net.neilcsmith.praxis.video.java.components;

import net.neilcsmith.praxis.core.Port;
import net.neilcsmith.praxis.video.impl.DefaultVideoInputPort;
import net.neilcsmith.praxis.video.impl.DefaultVideoOutputPort;
import net.neilcsmith.praxis.video.pipes.impl.MultiInOut;
import net.neilcsmith.praxis.video.pipes.impl.Placeholder;
import net.neilcsmith.praxis.video.render.Surface;


/**
 *
 * @author Neil C Smith (http://neilcsmith.net)
 */
public class JavaVideoComposite extends AbstractJavaVideoComponent {

    private Delegator delegator;
    private Placeholder dst;
    private Placeholder src;

    public JavaVideoComposite() {
        setupDelegator();
        setupCodeControl();
        buildImageControls("img", 4, 0);
        buildParams("p", 16, 8);
        buildTriggers("t", 4, 4);
        buildOutputs("out-", 2);
    }

    private void setupDelegator() {
//        try {
            delegator = new Delegator();
            dst = new Placeholder();
            src = new Placeholder();
            delegator.addSource(dst);
            delegator.addSource(src);
            registerPort(Port.IN, new DefaultVideoInputPort(this, dst));
            registerPort("src", new DefaultVideoInputPort(this, src));
            registerPort(Port.OUT, new DefaultVideoOutputPort(this, delegator));          
//        } catch (SinkIsFullException ex) {
//            Logger.getLogger(JavaVideoComposite.class.getName()).log(Level.SEVERE, null, ex);
//        } catch (SourceIsFullException ex) {
//            Logger.getLogger(JavaVideoComposite.class.getName()).log(Level.SEVERE, null, ex);
//        }
    }

    @Override
    protected void installToDelegator(ControllerDelegate delegate) {
        delegator.delegate = delegate;
    }
    
    private class Delegator extends MultiInOut {
        
        private ControllerDelegate delegate;
        
        private Delegator() {
            super(2,1);
        }

        @Override
        protected void process(Surface[] inputs, Surface output, int outputIndex, boolean rendering) {
            if (!rendering) {
                return;
            }
            output.copy(inputs[0]);
            inputs[0].release();
            if (delegate != null) {
                delegate.process(output, inputs[1]);
            }
            inputs[1].release();
        }
        
        
    }

}
