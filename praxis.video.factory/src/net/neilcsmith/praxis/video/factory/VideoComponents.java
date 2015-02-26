/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2015 Neil C Smith.
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
package net.neilcsmith.praxis.video.factory;

import net.neilcsmith.praxis.code.AbstractComponentFactory;
import net.neilcsmith.praxis.core.ComponentFactory;
import net.neilcsmith.praxis.core.ComponentFactoryProvider;
import net.neilcsmith.praxis.meta.TypeRewriter;
import net.neilcsmith.praxis.video.code.VideoCodeFactory;

/**
 *
 * @author Neil C Smith <http://neilcsmith.net>
 */
public class VideoComponents implements ComponentFactoryProvider {
    private final static Factory instance = new Factory();

    @Override
    public ComponentFactory getFactory() {
        return instance;
    }

    private static class Factory extends AbstractComponentFactory {

        private Factory() {
            build();
        }

        private void build() {
            
            // custom
            add(data(new VideoCodeFactory("video:custom")));
            
            // built-in
            
            // CORE VIDEO
            add("video:snapshot", "resources/snapshot.pxj");
            add("video:still", "resources/still.pxj");
            
            // ANALYSIS
            add("video:analysis:frame-delay", "resources/analysis_framedelay.pxj");
            add("video:analysis:difference", "resources/analysis_difference.pxj");
            
            // FX
            add("video:fx:blur", "resources/fx_blur.pxj");
            
            // SOURCE
            add("video:source:noise", "resources/source_noise.pxj");
            
            
            // DEPRECATED COMPONENTs
            add(data("video:filter:blur", "resources/fx_blur.pxj")
                    .deprecated().replacement("video:fx:blur").add(TypeRewriter.getIdentity()));
            
            
        }

        private void add(String type, String sourceFile) {
            add(data(type, sourceFile));
        }
        
        private Data data(String type, String sourceFile) {
            return data(new VideoCodeFactory(type, source(sourceFile)));
        }
        
    }
}