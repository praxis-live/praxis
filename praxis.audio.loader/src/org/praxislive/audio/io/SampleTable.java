/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012 Neil C Smith.
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

import java.io.IOException;
import java.net.URL;
import java.util.Arrays;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.UnsupportedAudioFileException;

/**
 *
 * @author Neil C Smith
 */
@Deprecated
public abstract class SampleTable {

    private float sampleRate;
    private int channelCount;
    private int bufferSize;

    protected SampleTable(float sampleRate, int channelCount, int bufferSize) {
        if (sampleRate < 1 || channelCount < 1 || bufferSize < 1) {
            throw new IllegalArgumentException();
        }
        this.sampleRate = sampleRate;
        this.channelCount = channelCount;
        this.bufferSize = bufferSize;
    }

    public float getSampleRate() {
        return sampleRate;
    }

    public int getChannelCount() {
        return channelCount;
    }

    public int getSize() {
        return bufferSize;
    }

    public abstract void set(int channel, int index, float value);

    public abstract float get(int channel, int index);

    public static class Interleaved extends SampleTable {

        private float[] data;
        private int channels;

        private Interleaved(float sampleRate, int channelCount, int bufferSize, float[] data) {
            super(sampleRate, channelCount, bufferSize);
            this.data = data;
            this.channels = channelCount;
        }

        @Override
        public void set(int channel, int index, float value) {
            data[(index * channels) + channel] = value;
        }

        @Override
        public float get(int channel, int index) {
            return data[(index * channels) + channel];
        }
    }

    public static class NonInterleaved extends SampleTable {

        private float[][] data;

        private NonInterleaved(float sampleRate, int channelCount, int bufferSize, float[][] data) {
            super(sampleRate, channelCount, bufferSize);
            this.data = data;
        }

        @Override
        public void set(int channel, int index, float value) {
            data[channel][index] = value;
        }

        @Override
        public float get(int channel, int index) {
            return data[channel][index];
        }
    }

    public static SampleTable allocate(float sampleRate, int channelCount, int bufferSize) {
        if (sampleRate < 1 || channelCount < 1 || bufferSize < 1) {
            throw new IllegalArgumentException();
        }
        float[][] data = new float[channelCount][bufferSize];
        return new NonInterleaved(sampleRate, channelCount, bufferSize, data);
    }

    public static SampleTable wrap(float sampleRate, int channelCount, float[] data) {
        if (sampleRate < 1 || channelCount < 1 || data == null || data.length == 0) {
            throw new IllegalArgumentException();
        }
        if (channelCount == 1) {
            return new NonInterleaved(sampleRate, channelCount, data.length, new float[][]{data});
        } else {
            if (data.length % channelCount != 0) {
                throw new IllegalArgumentException();
            }
            int bufferSize = data.length / channelCount;
            return new Interleaved(sampleRate, channelCount, bufferSize, data);
        }
    }

    public static SampleTable wrap(float sampleRate, float[]... data) {
        if (sampleRate < 1 || data.length < 1) {
            throw new IllegalArgumentException();
        }
        float[][] d = Arrays.copyOf(data, data.length);
        int bufferSize = d[0].length;
        if (bufferSize < 1) {
            throw new IllegalArgumentException();
        }
        for (float[] buf : d) {
            if (buf.length != bufferSize) {
                throw new IllegalArgumentException();
            }
        }
        return new NonInterleaved(sampleRate, d.length, bufferSize, data);
    }

    public static SampleTable fromURL(URL source) throws UnsupportedAudioFileException, IOException {
        byte[] byteData = null;
        AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(source);
        AudioFormat format = audioInputStream.getFormat();
        int channelCount = format.getChannels();
        float sampleRate = format.getSampleRate();
        if (channelCount < 1 || sampleRate < 1) {
            throw new UnsupportedAudioFileException();
        }
        int byteDataSize = (int) audioInputStream.getFrameLength() * format.getFrameSize();
        byteData = new byte[byteDataSize];
        int totalBytesRead = 0;
        int bytesRead;
        int offset = 0;
        int bytesToRead = 0;
        do {
            bytesToRead = audioInputStream.available();
            bytesRead = audioInputStream.read(byteData, offset, bytesToRead);
            offset += bytesRead;
            totalBytesRead += bytesRead;
        } while (totalBytesRead < byteDataSize);
        audioInputStream.close();
        float[] data = new float[byteDataSize / (format.getFrameSize() / channelCount)];
        AudioFloatConverter conv = AudioFloatConverter.getConverter(format);
        conv.toFloatArray(byteData, data);

        if (channelCount == 1) {
            return new NonInterleaved(sampleRate, channelCount, data.length, new float[][]{data});
        } else {
            return new Interleaved(sampleRate, channelCount, data.length / channelCount, data);
        }
    }
}
