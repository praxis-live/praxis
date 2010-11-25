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
package net.neilcsmith.praxis.video;

import net.neilcsmith.praxis.core.ComponentFactory;
import net.neilcsmith.praxis.core.ComponentFactoryProvider;
import net.neilcsmith.praxis.impl.AbstractComponentFactory;
import net.neilcsmith.praxis.video.components.Snapshot;
import net.neilcsmith.praxis.video.components.Splitter;
import net.neilcsmith.praxis.video.components.Still;
import net.neilcsmith.praxis.video.components.VideoCapture;
import net.neilcsmith.praxis.video.components.VideoOutput;
import net.neilcsmith.praxis.video.components.VideoPlayer;
import net.neilcsmith.praxis.video.components.filters.Blur;
import net.neilcsmith.praxis.video.components.mix.Composite;
import net.neilcsmith.praxis.video.components.mix.XFader;
import net.neilcsmith.praxis.video.components.test.BackgroundDifference;
import net.neilcsmith.praxis.video.components.test.Difference;
import net.neilcsmith.praxis.video.components.test.DifferenceCalc;
import net.neilcsmith.praxis.video.components.test.FrameTimer;
import net.neilcsmith.praxis.video.components.test.Hyp;
import net.neilcsmith.praxis.video.components.test.ImageSave;
import net.neilcsmith.praxis.video.components.test.Noise;
import net.neilcsmith.praxis.video.components.test.Ripple;

/**
 *
 * @author Neil C Smith (http://neilcsmith.net)
 */
public class VideoFactoryProvider implements ComponentFactoryProvider {

    private final static ComponentFactory factory = new Factory();

    public ComponentFactory getFactory() {
        return factory;
    }

    private static class Factory extends AbstractComponentFactory {

        private Factory() {
            build();
        }

        private void build() {
            // ROOT
            addRoot("root:video", DefaultVideoRoot.class);

            // COMPONENTS
            addComponent("video:output", VideoOutput.class);
            addComponent("video:still", Still.class);
            addComponent("video:snapshot", Snapshot.class);
            addComponent("video:splitter", Splitter.class);
            addComponent("video:test:hypnosis", Hyp.class);
            addComponent("video:test:time", FrameTimer.class);
            addComponent("video:test:save", ImageSave.class);
            addComponent("video:test:difference-calc", DifferenceCalc.class);
            addComponent("video:test:bgdiff", BackgroundDifference.class);
            addComponent("video:test:noise", Noise.class);
            addComponent("video:time-fx:ripple", Ripple.class);
            addComponent("video:time-fx:difference", Difference.class);
            addComponent("video:mix:xfader", XFader.class);
            addComponent("video:mix:composite", Composite.class);

            addComponent("video:player", VideoPlayer.class);
            addComponent("video:capture", VideoCapture.class);

            addComponent("video:test:filter:blur", Blur.class);
        }
    }
}
