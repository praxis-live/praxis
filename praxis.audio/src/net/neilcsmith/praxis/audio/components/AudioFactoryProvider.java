/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2013 Neil C Smith.
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
            addComponent("audio:sine", Sine.class);
            addComponent("audio:gain", Gain.class);
            addComponent("audio:sampleplayer", SamplePlayer.class);
            addComponent("audio:analysis:level", Level.class);
            addComponent("audio:filter:comb", CombFilter.class);
            addComponent("audio:filter:iir", IIRFilter.class);
            addComponent("audio:delay:mono-delay", MonoDelay2s.class);
            addComponent("audio:distortion:overdrive", SimpleOverdrive.class);
            addComponent("audio:mix:xfader", XFader.class);
            addComponent("audio:modulation:chorus", MonoChorus.class);
            addComponent("audio:modulation:lfo-delay", LFODelay.class);
            addComponent("audio:reverb:freeverb", Freeverb.class);
            addComponent("audio:sampling:looper", Looper.class);
            
            // test
            //addTestComponent("audio:test:analysis:level", Level.class, "audio:analysis:level");
            addComponent("audio:test:analysis:level", data(Level.class).test().replacement("audio:analysis:level"));
            addComponent("audio:sampling:player", data(StereoPlayer.class).test());

            addComponent("audio:container:input", data(AudioContainerInput.class).test());
            addComponent("audio:container:output", data(AudioContainerOutput.class).test());
            
        }
    }
}
