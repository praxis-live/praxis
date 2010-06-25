/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 - Neil C Smith. All rights reserved.
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

package net.neilcsmith.praxis.video.components.filters;

import net.neilcsmith.praxis.core.Port;
import net.neilcsmith.praxis.impl.AbstractComponent;
import net.neilcsmith.praxis.impl.IntProperty;
import net.neilcsmith.praxis.video.DefaultVideoInputPort;
import net.neilcsmith.praxis.video.DefaultVideoOutputPort;
import net.neilcsmith.ripl.components.filters.BlurFilter;

/**
 *
 * @author Neil C Smith (http://neilcsmith.net)
 */
public class Blur extends AbstractComponent {
    
    private BlurFilter filter;

    public Blur() {
        filter = new BlurFilter();
        IntProperty radius = IntProperty.create(this, new RadiusBinding(), 0, 256, 0);
        registerControl("radius", radius);
        registerPort("radius", radius.createPort());
        registerPort(Port.IN, new DefaultVideoInputPort(this, filter));
        registerPort(Port.OUT, new DefaultVideoOutputPort(this, filter));
    }

    private class RadiusBinding implements IntProperty.Binding {

        public void setBoundValue(long time, int value) {
            filter.setRadius(value);
        }

        public int getBoundValue() {
            return filter.getRadius();
        }

    }


}
