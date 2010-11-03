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

package net.neilcsmith.praxis.audio.components.filter;

import net.neilcsmith.praxis.audio.DefaultAudioInputPort;
import net.neilcsmith.praxis.audio.DefaultAudioOutputPort;
import net.neilcsmith.praxis.core.Port;
import net.neilcsmith.praxis.core.types.PMap;
import net.neilcsmith.praxis.impl.AbstractComponent;
import net.neilcsmith.praxis.impl.FloatProperty;
import net.neilcsmith.praxis.impl.StringProperty;
import net.neilcsmith.rapl.components.filters.IIRFilter.Type;

/**
 *
 * @author Neil C Smith
 */
public class IIRFilter extends AbstractComponent {

    private net.neilcsmith.rapl.components.filters.IIRFilter filter;
    private FloatProperty frequency;
    private FloatProperty resonance;
    private FloatProperty mix;
    private StringProperty type;

    public IIRFilter() {
        filter = new net.neilcsmith.rapl.components.filters.IIRFilter();
        type = createTypeControl();
        registerControl("type", type);
        frequency =  FloatProperty.create( new FrequencyBinding(),
                20, 20000, 20, PMap.valueOf("scale-hint", "Exponential"));
        registerControl("frequency", frequency);
        registerPort("frequency", frequency.createPort());
        resonance = FloatProperty.create( new ResonanceBinding(),
                0, 30, 0);
        registerControl("resonance", resonance);
        registerPort("resonance", resonance.createPort());
        mix = FloatProperty.create( new MixBinding(), 0, 1, 0);
        registerControl("mix", mix);
        registerPort("mix", mix.createPort());
        registerPort(Port.IN, new DefaultAudioInputPort(this, filter));
        registerPort(Port.OUT, new DefaultAudioOutputPort(this, filter));
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
            }

            @Override
            public String getBoundValue() {
                return filter.getFilterType().name();
            }
        };
        return StringProperty.create(binding, allowed, filter.getFilterType().name());
    }

    private class MixBinding implements FloatProperty.Binding {

        public void setBoundValue(long time, double value) {
            filter.setMix((float) value);
        }

        public double getBoundValue() {
            return filter.getMix();
        }

    }

    private class FrequencyBinding implements FloatProperty.Binding {

        public void setBoundValue(long time, double value) {
            filter.setFrequency((float) value);
        }

        public double getBoundValue() {
            return filter.getFrequency();
        }

    }

    private class ResonanceBinding implements FloatProperty.Binding {

        public void setBoundValue(long time, double value) {
            filter.setResonance((float) value);
        }

        public double getBoundValue() {
            return filter.getResonance();
        }

    }
}
