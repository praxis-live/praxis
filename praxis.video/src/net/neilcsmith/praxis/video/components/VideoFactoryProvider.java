/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2014 Neil C Smith.
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
package net.neilcsmith.praxis.video.components;

import net.neilcsmith.praxis.core.ComponentFactory;
import net.neilcsmith.praxis.core.ComponentFactoryProvider;
import net.neilcsmith.praxis.impl.AbstractComponentFactory;
import net.neilcsmith.praxis.meta.TypeRewriter;
import net.neilcsmith.praxis.video.components.analysis.Difference;
import net.neilcsmith.praxis.video.components.analysis.FrameDelay;
import net.neilcsmith.praxis.video.components.analysis.blobs.BlobTracker;
import net.neilcsmith.praxis.video.components.container.VideoContainerInput;
import net.neilcsmith.praxis.video.components.container.VideoContainerOutput;
import net.neilcsmith.praxis.video.components.filters.Blur;
import net.neilcsmith.praxis.video.components.mix.Composite;
import net.neilcsmith.praxis.video.components.mix.XFader;
import net.neilcsmith.praxis.video.components.source.Noise;
import net.neilcsmith.praxis.video.components.test.DifferenceCalc;
import net.neilcsmith.praxis.video.components.test.ImageSave;
import net.neilcsmith.praxis.video.components.timefx.FrameDifference;
import net.neilcsmith.praxis.video.components.timefx.Ripple;

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
            addComponent("video:splitter", data(Splitter.class).deprecated());
            addComponent("video:composite", Composite.class);
            addComponent("video:xfader", XFader.class);

            // ANALYSIS
            addComponent("video:analysis:frame-delay", FrameDelay.class);
            addComponent("video:analysis:difference", Difference.class);
            addComponent("video:analysis:simple-tracker", BlobTracker.class);

            // FX
            addComponent("video:fx:blur", data(Blur.class));
            addComponent("video:fx:difference", data(FrameDifference.class));
            addComponent("video:fx:ripple", data(Ripple.class));

            // FILTER
            addComponent("video:filter:blur", data(Blur.class).deprecated()
                    .replacement("video:fx:blur").add(TypeRewriter.getIdentity()));

            // MIX
            addComponent("video:mix:xfader", data(XFader.class).deprecated()
                    .replacement("video:xfader").add(TypeRewriter.getIdentity()));
            addComponent("video:mix:composite", data(Composite.class).deprecated()
                    .replacement("video:composite").add(TypeRewriter.getIdentity()));

            // SOURCE
            addComponent("video:source:noise", Noise.class);

            // TIME-FX
            addComponent("video:time-fx:ripple", data(Ripple.class).deprecated()
                    .replacement("video:fx:ripple").add(TypeRewriter.getIdentity()));
            addComponent("video:time-fx:difference", data(FrameDifference.class).deprecated()
                    .replacement("video:fx:difference").add(TypeRewriter.getIdentity()));

            // TEST COMPONENTS
            addComponent("video:test:save", data(ImageSave.class).test());
            addComponent("video:test:difference-calc", data(DifferenceCalc.class).test());
            
            
            addComponent("video:test:noise", data(Noise.class).test()
                    .replacement("video:source:noise").add(TypeRewriter.getIdentity()));
            addComponent("video:test:analysis:frame-delay", data(FrameDelay.class).test()
                    .replacement("video:analysis:frame-delay").add(TypeRewriter.getIdentity()));
            addComponent("video:test:analysis:difference", data(Difference.class).test()
                    .replacement("video:analysis:difference").add(TypeRewriter.getIdentity()));
            addComponent("video:test:analysis:blob-tracker", data(BlobTracker.class).test()
                    .replacement("video:analysis:simple-tracker").add(TypeRewriter.getIdentity()));

            addComponent("video:container:input", data(VideoContainerInput.class));
            addComponent("video:container:output", data(VideoContainerOutput.class));

        }
    }
}
