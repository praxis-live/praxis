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
package net.neilcsmith.praxis.audio.factory;

import net.neilcsmith.praxis.audio.code.AudioCodeFactory;
import net.neilcsmith.praxis.code.AbstractComponentFactory;
import net.neilcsmith.praxis.core.ComponentFactory;
import net.neilcsmith.praxis.core.ComponentFactoryProvider;

/**
 *
 * @author Neil C Smith (http://neilcsmith.net)
 */
public class AudioComponents implements ComponentFactoryProvider {

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
            add(data(new AudioCodeFactory("audio:custom")));
            
            add("audio:clock", "resources/audio_clock.pxj");
        }
        
        private void add(String type, String sourceFile) {
            add(data(type, sourceFile));
        }
        
        private Data data(String type, String sourceFile) {
            return data(new AudioCodeFactory(type, source(sourceFile)));
        }
        
    }
    
}
