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
package net.neilcsmith.praxis.video.components.filters;

import net.neilcsmith.praxis.core.Port;
import net.neilcsmith.praxis.impl.AbstractComponent;
import net.neilcsmith.praxis.impl.IntProperty;
import net.neilcsmith.praxis.video.impl.DefaultVideoInputPort;
import net.neilcsmith.praxis.video.impl.DefaultVideoOutputPort;
import net.neilcsmith.praxis.video.pipes.impl.SingleInOut;
import net.neilcsmith.praxis.video.render.Surface;
import net.neilcsmith.praxis.video.render.SurfaceOp;

/**
 *
 * @author Neil C Smith (http://neilcsmith.net)
 */
public class Blur extends AbstractComponent {

    private BlurPipe filter;

    public Blur() {
        filter = new BlurPipe();
        IntProperty radius = IntProperty.create(new RadiusBinding(), 0, 256, 0);
        registerPort(Port.IN, new DefaultVideoInputPort(this, filter));
        registerPort(Port.OUT, new DefaultVideoOutputPort(this, filter));
        registerControl("radius", radius);
        registerPort("radius", radius.createPort());

    }

    private class RadiusBinding implements IntProperty.Binding {

        public void setBoundValue(long time, int value) {
            filter.setRadius(value);
        }

        public int getBoundValue() {
            return filter.getRadius();
        }
    }

    private class BlurPipe extends SingleInOut {

        private SurfaceOp op;
        private int radius;

        public int getRadius() {
            return radius;
        }

        public void setRadius(int radius) {
            if (radius < 0) {
                throw new IllegalArgumentException();
            }
            this.op = null;
            this.radius = radius;
        }

        @Override
        protected void process(Surface surface, boolean rendering) {
            if (!rendering) {
                return;
            }
            if (op == null) {
                op = net.neilcsmith.praxis.video.render.ops.Blur.op(radius);
            }
            surface.process(op);
        }
    }
}
