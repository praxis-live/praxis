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
package net.neilcsmith.praxis.audio.components;

import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Mixer;
import net.neilcsmith.audioservers.AudioClient;
import net.neilcsmith.audioservers.AudioConfiguration;
import net.neilcsmith.audioservers.AudioServer;
import net.neilcsmith.audioservers.jack.JackAudioServer;
import net.neilcsmith.audioservers.javasound.JavasoundAudioServer;
import net.neilcsmith.praxis.core.Lookup;
import net.neilcsmith.praxis.core.types.PMap;

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
//        Mixer mixer = getMixer(device);
//        Mixer mixer = AudioSystem.getMixer(AudioSystem.getMixerInfo()[0]);
//        return JavasoundAudioServer.create(mixer, context,
//                JavasoundAudioServer.TimingMode.Estimated, client);
        return JavasoundAudioServer.create(device, context, JavasoundAudioServer.TimingMode.Blocking, client);
    }

    private Mixer getMixer(String device) {
        Mixer.Info[] infos = AudioSystem.getMixerInfo();
        Mixer mixer = null;
        for (Mixer.Info info : infos) {
            if (info.getName().equals(device)) {
                mixer = AudioSystem.getMixer(info);
                break;
            }
        }
        if (mixer == null) {
            for (Mixer.Info info : infos) {
                if (info.getName().contains(device)) {
                    mixer = AudioSystem.getMixer(info);
                    break;
                }
            }
        }
        if (mixer == null) {
            mixer = AudioSystem.getMixer(infos[0]);
        }
        return mixer;
    }


    public static AudioServerLoader getInstance() {
        return instance;
    }
}
