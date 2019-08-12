/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2018 Neil C Smith.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License version 3 only, as
 * published by the Free Software Foundation.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser General Public License
 * version 3 for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License version 3
 * along with this work; if not, see http://www.gnu.org/licenses/
 * 
 *
 * Please visit https://www.praxislive.org if you need additional information or
 * have any questions.
 */
package org.praxislive.video.gstreamer.components;

import org.praxislive.code.AbstractComponentFactory;
import org.praxislive.core.ComponentType;
import org.praxislive.core.services.ComponentFactory;
import org.praxislive.core.services.ComponentFactoryProvider;
import org.praxislive.video.code.VideoCodeDelegate;
import org.praxislive.video.code.VideoCodeFactory;

/**
 *
 * @author Neil C Smith (http://neilcsmith.net)
 */
public class GStreamerComponents implements ComponentFactoryProvider {

    private final static ComponentFactory factory = new Factory();

    @Override
    public ComponentFactory getFactory() {
        return factory;
    }

    private static class Factory extends AbstractComponentFactory {

        private Factory() {
            build();
        }

        private void build() {
            add("video:player", VideoPlayerComponent.class, VideoPlayerComponent.TEMPLATE_PATH);
            add("video:capture", VideoCaptureComponent.class, VideoCaptureComponent.TEMPLATE_PATH);
        }
        
        private void add(String type, Class<? extends VideoCodeDelegate> cls, String path) {
            add(data(
                    new VideoCodeFactory(ComponentType.of(type), cls, source(path))
            ));
        }
        
    }
}
