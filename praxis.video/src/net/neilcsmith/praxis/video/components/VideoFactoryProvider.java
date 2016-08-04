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
import net.neilcsmith.praxis.video.components.analysis.blobs.BlobTracker;
import net.neilcsmith.praxis.video.components.container.VideoContainerInput;
import net.neilcsmith.praxis.video.components.container.VideoContainerOutput;
import net.neilcsmith.praxis.video.components.mix.Composite;
import net.neilcsmith.praxis.video.components.mix.XFader;
import net.neilcsmith.praxis.video.components.test.ImageSave;
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
            addComponent("video:composite", Composite.class);
            addComponent("video:xfader", XFader.class);

            // ANALYSIS
            addComponent("video:analysis:simple-tracker", BlobTracker.class);

            // FX
            addComponent("video:fx:ripple", data(Ripple.class));


            // MIX
            addComponent("video:mix:xfader", data(XFader.class).deprecated()
                    .replacement("video:xfader").add(TypeRewriter.getIdentity()));
            addComponent("video:mix:composite", data(Composite.class).deprecated()
                    .replacement("video:composite").add(TypeRewriter.getIdentity()));

            // TEST COMPONENTS
            addComponent("video:test:save", data(ImageSave.class).test());
            
            /// CONTAINER
            addComponent("video:container:input", data(VideoContainerInput.class));
            addComponent("video:container:output", data(VideoContainerOutput.class));

        }
    }
}
