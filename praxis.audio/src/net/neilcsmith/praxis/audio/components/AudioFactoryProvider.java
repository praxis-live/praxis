/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2016 Neil C Smith.
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
package net.neilcsmith.praxis.audio.components;

import net.neilcsmith.praxis.audio.components.analysis.Level;
import net.neilcsmith.praxis.audio.components.container.AudioContainerInput;
import net.neilcsmith.praxis.audio.components.container.AudioContainerOutput;
import net.neilcsmith.praxis.audio.components.delay.MonoDelay2s;
import net.neilcsmith.praxis.audio.components.distortion.SimpleOverdrive;
import net.neilcsmith.praxis.audio.components.filter.CombFilter;
import net.neilcsmith.praxis.audio.components.filter.IIRFilter;
import net.neilcsmith.praxis.audio.components.mix.XFader;
import net.neilcsmith.praxis.audio.components.modulation.LFODelay;
import net.neilcsmith.praxis.audio.components.modulation.MonoChorus;
import net.neilcsmith.praxis.audio.components.reverb.Freeverb;
import net.neilcsmith.praxis.audio.components.sampling.Looper;
import net.neilcsmith.praxis.audio.components.test.Sine;
import net.neilcsmith.praxis.core.ComponentFactory;
import net.neilcsmith.praxis.core.ComponentFactoryProvider;
import net.neilcsmith.praxis.impl.AbstractComponentFactory;
import net.neilcsmith.praxis.meta.TypeRewriter;

/**
 *
 * @author Neil C Smith (http://neilcsmith.net)
 */
public class AudioFactoryProvider implements ComponentFactoryProvider {
    
    private final static ComponentFactory factory = new Factory();
    
    public ComponentFactory getFactory() {
        return factory;
    }
    
    private static class Factory extends AbstractComponentFactory {
        
        private Factory() {
            build();
        }
        
        private void build() {
            //ROOT
            addRoot("root:audio", DefaultAudioRoot.class);

            //COMPONENTS
            addComponent("audio:input", AudioInput.class);
            addComponent("audio:output", AudioOutput.class);
            addComponent("audio:sine", data(Sine.class).deprecated()
                    .replacement("audio:osc").add(TypeRewriter.getIdentity()));
//            addComponent("audio:gain", Gain.class);
            addComponent("audio:sampleplayer", data(SamplePlayer.class).deprecated());
            addComponent("audio:analysis:level", Level.class);
            addComponent("audio:filter:comb", data(CombFilter.class).deprecated());
            addComponent("audio:filter:iir", data(IIRFilter.class).deprecated());
            addComponent("audio:delay:mono-delay", data(MonoDelay2s.class).deprecated());
            addComponent("audio:distortion:overdrive", data(SimpleOverdrive.class).deprecated());
            addComponent("audio:mix:xfader", XFader.class);
            addComponent("audio:modulation:chorus", data(MonoChorus.class).deprecated());
            addComponent("audio:modulation:lfo-delay", data(LFODelay.class).deprecated());
            addComponent("audio:reverb:freeverb", data(Freeverb.class).deprecated());
            addComponent("audio:sampling:looper", Looper.class);
            addComponent("audio:sampling:player", data(StereoPlayer.class).deprecated()
                    .replacement("audio:player").add(TypeRewriter.getIdentity()));
            
            addComponent("audio:container:input", data(AudioContainerInput.class)
                    .replacement("audio:container:in").add(TypeRewriter.getIdentity()));
            addComponent("audio:container:output", data(AudioContainerOutput.class)
                    .replacement("audio:container:out").add(TypeRewriter.getIdentity()));
            addComponent("audio:container:in", data(AudioContainerInput.class));
            addComponent("audio:container:out", data(AudioContainerOutput.class));
            
        }
    }
}
