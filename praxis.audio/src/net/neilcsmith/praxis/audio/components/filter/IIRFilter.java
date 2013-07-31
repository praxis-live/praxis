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
 *
 */

package net.neilcsmith.praxis.audio.components.filter;

import net.neilcsmith.praxis.audio.impl.DefaultAudioInputPort;
import net.neilcsmith.praxis.audio.impl.DefaultAudioOutputPort;
import net.neilcsmith.praxis.core.Port;
import net.neilcsmith.praxis.core.types.PMap;
import net.neilcsmith.praxis.impl.AbstractComponent;
import net.neilcsmith.praxis.impl.NumberProperty;
import net.neilcsmith.praxis.impl.LinkPort;
import net.neilcsmith.praxis.impl.StringProperty;
import org.jaudiolibs.audioops.impl.ContainerOp;
import org.jaudiolibs.audioops.impl.IIRFilterOp;
import org.jaudiolibs.audioops.impl.IIRFilterOp.Type;
import org.jaudiolibs.pipes.impl.OpHolder;

/**
 *
 * @author Neil C Smith
 */
public class IIRFilter extends AbstractComponent {

    private IIRFilterOp filter;
    private ContainerOp container;
    private NumberProperty frequency;
    private NumberProperty resonance;
    private NumberProperty mix;
    private StringProperty type;
    private LinkPort<IIRFilter> link;

    public IIRFilter() {
        filter = new IIRFilterOp();
        container = new ContainerOp(filter);
        OpHolder holder = new OpHolder(container);
        registerPort(Port.IN, new DefaultAudioInputPort(this, holder));
        registerPort(Port.OUT, new DefaultAudioOutputPort(this, holder));
        type = createTypeControl();
        registerControl("type", type);
        frequency =  NumberProperty.create( new FrequencyBinding(),
                20, 20000, 20, PMap.create("scale-hint", "Exponential"));
        registerControl("frequency", frequency);
        registerPort("frequency", frequency.createPort());
        resonance = NumberProperty.create( new ResonanceBinding(),
                0, 30, 0);
        registerControl("resonance", resonance);
        registerPort("resonance", resonance.createPort());
        mix = NumberProperty.create( new MixBinding(), 0, 1, 0);
        registerControl("mix", mix);
        registerPort("mix", mix.createPort());
        link = new LinkPort<IIRFilter>(IIRFilter.class, new LinkHandler(), this);
        registerPort(LinkPort.ID, link);
    }

    private StringProperty createTypeControl() {
        Type[] types = Type.values();
        String[] allowed = new String[types.length];
        for (int i=0; i < types.length; i++) {
            allowed[i] = types[i].name();
        }
        StringProperty.Binding binding = new StringProperty.Binding() {

            @Override
            public void setBoundValue(long time, String value) {
                filter.setFilterType(Type.valueOf(value));
                link.fireUpdate();
            }

            @Override
            public String getBoundValue() {
                return filter.getFilterType().name();
            }
        };
        return StringProperty.create(binding, allowed, filter.getFilterType().name());
    }

    private class MixBinding implements NumberProperty.Binding {

        public void setBoundValue(long time, double value) {
            container.setMix((float) value);
            link.fireUpdate();
        }

        public double getBoundValue() {
            return container.getMix();
        }

    }

    private class FrequencyBinding implements NumberProperty.Binding {

        public void setBoundValue(long time, double value) {
            filter.setFrequency((float) value);
            link.fireUpdate();
        }

        public double getBoundValue() {
            return filter.getFrequency();
        }

    }

    private class ResonanceBinding implements NumberProperty.Binding {

        public void setBoundValue(long time, double value) {
            filter.setResonance((float) value);
            link.fireUpdate();
        }

        public double getBoundValue() {
            return filter.getResonance();
        }

    }
    
    private class LinkHandler implements LinkPort.Handler<IIRFilter> {

        public void update(IIRFilter source) {
            filter.setFilterType(source.filter.getFilterType());
            filter.setFrequency(source.filter.getFrequency());
            filter.setResonance(source.filter.getResonance());
            container.setMix(source.container.getMix());
        }
        
    }
}
