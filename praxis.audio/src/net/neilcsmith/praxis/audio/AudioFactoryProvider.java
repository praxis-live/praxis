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
package net.neilcsmith.praxis.audio;

import net.neilcsmith.praxis.audio.components.AudioInput;
import net.neilcsmith.praxis.audio.components.AudioOutput;
import net.neilcsmith.praxis.audio.components.Gain;
import net.neilcsmith.praxis.audio.components.SamplePlayer;
import net.neilcsmith.praxis.audio.components.distortion.SimpleOverdrive;
import net.neilcsmith.praxis.audio.components.filter.CombFilter;
import net.neilcsmith.praxis.audio.components.filter.IIRFilter;
import net.neilcsmith.praxis.audio.components.mix.XFader;
import net.neilcsmith.praxis.audio.components.test.Sine;
import net.neilcsmith.praxis.audio.components.time.MonoDelay2s;
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
            addComponent("audio:filter:comb", CombFilter.class);
            addComponent("audio:filter:iir", IIRFilter.class);
            addComponent("audio:delay:mono-delay", MonoDelay2s.class);
            addComponent("audio:distortion:simple-overdrive", SimpleOverdrive.class);
            addComponent("audio:mix:xfader", XFader.class);

        }
    }
}
