/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2009 Neil C Smith. All rights reserved.
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
 *
 */

package net.neilcsmith.rapl.components.filters;

import net.neilcsmith.audioops.impl.ContainerOp;
import net.neilcsmith.audioops.impl.IIRFilterOp;
import net.neilcsmith.rapl.components.SingleInOutOpComponent;

/**
 *
 * @author Neil C Smith
 */
public class IIRFilter extends SingleInOutOpComponent {

    public static enum Type {

        LP6, LP12, HP12, BP12, NP12, LP24, HP24
    };

    private IIRFilterOp filter;
    private ContainerOp container;
    private Type type;

    public IIRFilter() {
        filter = new IIRFilterOp();
        filter.setFilterType(IIRFilterOp.Type.LP6);
        type = Type.LP6;
        container = new ContainerOp(filter);
        setOp(container);
    }

    public void setFrequency(float frequency) {
        filter.setFrequency(frequency);
    }

    public float getFrequency() {
        return filter.getFrequency();
    }

    public void setResonance(float resonance) {
        filter.setResonance(resonance);
    }

    public float getResonance() {
        return filter.getResonance();
    }

    public void setFilterType(Type type) {
        if (type == null) {
            throw new NullPointerException();
        }
        switch (type) {
            case LP6 :
                filter.setFilterType(IIRFilterOp.Type.LP6);
                break;
            case LP12 :
                filter.setFilterType(IIRFilterOp.Type.LP12);
                break;
            case HP12 :
                filter.setFilterType(IIRFilterOp.Type.HP12);
                break;
            case BP12 :
                filter.setFilterType(IIRFilterOp.Type.BP12);
                break;
            case NP12 :
                filter.setFilterType(IIRFilterOp.Type.NP12);
                break;
            case LP24 :
                filter.setFilterType(IIRFilterOp.Type.LP24);
                break;
            case HP24 :
                filter.setFilterType(IIRFilterOp.Type.HP24);
                break;
        }
        this.type = type;
    }

    public Type getFilterType() {
        return type;
    }

    public void setMix(float mix) {
        container.setMix(mix);
    }

    public float getMix() {
        return container.getMix();
    }
}
