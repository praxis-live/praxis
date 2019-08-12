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
package org.praxislive.audio.components;

import org.praxislive.audio.code.AudioCodeDelegate;
import org.praxislive.audio.code.AudioCodeFactory;
import org.praxislive.code.AbstractComponentFactory;
import org.praxislive.core.ComponentType;
import org.praxislive.core.services.ComponentFactory;
import org.praxislive.core.services.ComponentFactoryProvider;

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
            add("audio:custom", AudioCustom.class, AudioCustom.TEMPLATE_PATH);
            
            add("audio:clock", AudioClock.class, AudioClock.TEMPLATE_PATH);
            add("audio:gain", AudioGain.class, AudioGain.TEMPLATE_PATH);
            add("audio:osc", AudioOsc.class, AudioOsc.TEMPLATE_PATH);
            add("audio:player", AudioPlayer.class, AudioPlayer.TEMPLATE_PATH);
            add("audio:looper", AudioLooper.class, AudioLooper.TEMPLATE_PATH);
            
            add("audio:fx:chorus", AudioFXChorus.class, AudioFXChorus.TEMPLATE_PATH);
            add("audio:fx:comb-filter", AudioFXCombFilter.class, AudioFXCombFilter.TEMPLATE_PATH);
            add("audio:fx:delay", AudioFXDelay.class, AudioFXDelay.TEMPLATE_PATH);
            add("audio:fx:filter", AudioFXFilter.class, AudioFXFilter.TEMPLATE_PATH);
            add("audio:fx:lfo-delay", AudioFXLFODelay.class, AudioFXLFODelay.TEMPLATE_PATH);
            add("audio:fx:overdrive", AudioFXOverdrive.class, AudioFXOverdrive.TEMPLATE_PATH);
            add("audio:fx:reverb", AudioFXReverb.class, AudioFXReverb.TEMPLATE_PATH);
            
        }
        
        
        private void add(String type, Class<? extends AudioCodeDelegate> cls, String path) {
            add(data(
                    new AudioCodeFactory(ComponentType.of(type), cls, source(path))
            ));
        }
        
    }
    
}
