/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2012 Neil C Smith.
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
package org.praxislive.audio.components;

import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Mixer;
import org.jaudiolibs.audioservers.AudioClient;
import org.jaudiolibs.audioservers.AudioConfiguration;
import org.jaudiolibs.audioservers.AudioServer;
import org.jaudiolibs.audioservers.jack.JackAudioServer;
import org.jaudiolibs.audioservers.javasound.JavasoundAudioServer;
import org.praxislive.core.Lookup;
import org.praxislive.core.types.PMap;

/**
 *
 * @author Neil C Smith
 * @TODO Use AudioServerFactoryProvider mechanism
 */
class AudioServerLoader {

    private static AudioServerLoader instance = new AudioServerLoader();

    private AudioServerLoader() {
    }

    public AudioServer load(Lookup lookup, String libName, String device,
            String id, AudioConfiguration context, AudioClient client, PMap properties) throws Exception {
        if ("Jack".equalsIgnoreCase(libName)) {
            return createJackServer(id, context, client);
        } else {
            return createJavaSoundServer(device, context, client);
        }
    }

    private AudioServer createJackServer(String id, AudioConfiguration context,
            AudioClient client) {
        return JackAudioServer.create(id, context, false, client);
    }

    private AudioServer createJavaSoundServer(String device, AudioConfiguration context,
            AudioClient client) throws Exception {
        return JavasoundAudioServer.create(device, context, JavasoundAudioServer.TimingMode.Estimated, client);
    }

    public static AudioServerLoader getInstance() {
        return instance;
    }
}
