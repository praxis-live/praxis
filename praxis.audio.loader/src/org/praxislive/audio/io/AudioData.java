/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2017 Neil C Smith.
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
 * along with this work; if not, see http://www.gnu.org/licenses/
 * 
 *
 * Linking this work statically or dynamically with other modules is making a
 * combined work based on this work. Thus, the terms and conditions of the GNU
 * General Public License cover the whole combination.
 *
 * As a special exception, the copyright holders of this work give you permission
 * to link this work with independent modules to produce an executable,
 * regardless of the license terms of these independent modules, and to copy and
 * distribute the resulting executable under terms of your choice, provided that
 * you also meet, for each linked independent module, the terms and conditions of
 * the license of that module. An independent module is a module which is not
 * derived from or based on this work. If you modify this work, you may extend
 * this exception to your version of the work, but you are not obligated to do so.
 * If you do not wish to do so, delete this exception statement from your version.
 *
 * Please visit http://neilcsmith.net if you need additional information or
 * have any questions.
 */
package org.praxislive.audio.io;

import java.io.ByteArrayOutputStream;
import java.net.URL;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.UnsupportedAudioFileException;

/**
 *
 * @author Neil C Smith (http://neilcsmith.net)
 */
public class AudioData {

    public final float[] data;
    public final float sampleRate;
    public final int channels;

    private AudioData(float[] data, float sampleRate, int channels) {
        this.data = data;
        this.sampleRate = sampleRate;
        this.channels = channels;
    }

    public static AudioData fromURL(URL source) throws Exception {
        try (AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(source)) {
            AudioFormat format = audioInputStream.getFormat();
            int channelCount = format.getChannels();
            float sampleRate = format.getSampleRate();
            if (channelCount < 1 || sampleRate < 1) {
                throw new UnsupportedAudioFileException();
            }
            int estSize = Math.max(4096, audioInputStream.available());
            BAOS baos = new BAOS(estSize);
            
            int bytesRead;
            byte[] data = new byte[4096];
            while ((bytesRead = audioInputStream.read(data, 0, data.length)) > -1) {
                baos.write(data, 0, bytesRead);
            }
            byte[] byteData = baos.toByteArray();
            float[] floatData = new float[byteData.length / (format.getFrameSize() / channelCount)];
            AudioFloatConverter conv = AudioFloatConverter.getConverter(format);
            conv.toFloatArray(byteData, floatData);
            return new AudioData(floatData, sampleRate, channelCount);
        }

    }

    private static class BAOS extends ByteArrayOutputStream {

        private BAOS(int size) {
            super(size);
        }
        
        @Override
        public synchronized byte[] toByteArray() {
            if (buf.length == count) {
                return buf;
            } else {
                return super.toByteArray();
            }
        }

    }

}
