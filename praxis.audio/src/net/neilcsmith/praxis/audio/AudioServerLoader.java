/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2008 - Neil C Smith. All rights reserved.
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

import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Mixer;
import net.neilcsmith.audioservers.AudioClient;
import net.neilcsmith.audioservers.AudioContext;
import net.neilcsmith.audioservers.AudioServer;
import net.neilcsmith.audioservers.impl.JackAudioServer;
import net.neilcsmith.audioservers.impl.JavasoundAudioServer;
import net.neilcsmith.praxis.core.Lookup;
import net.neilcsmith.praxis.core.types.PMap;

/**
 *
 * @author Neil C Smith
 * @TODO Use AudioServerFactoryProvider mechanism
 */
public class AudioServerLoader {

    private static AudioServerLoader instance = new AudioServerLoader();

    private AudioServerLoader() {
    }

    public AudioServer load(Lookup lookup, String libName, String device,
            String id, AudioContext context, AudioClient client, PMap properties) {
        if ("Jack".equalsIgnoreCase(libName)) {
            return createJackServer(id, context, client);
        } else {
            return createJavaSoundServer(device, context, client);
        }
    }

    private AudioServer createJackServer(String id, AudioContext context,
            AudioClient client) {
        return JackAudioServer.create(id, context, false, client);
    }

    private AudioServer createJavaSoundServer(String device, AudioContext context,
            AudioClient client) {
//        Mixer mixer = getMixer(device);
        Mixer mixer = AudioSystem.getMixer(AudioSystem.getMixerInfo()[0]);
        return JavasoundAudioServer.create(mixer, context,
                JavasoundAudioServer.TimingMode.Estimated, client);
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
