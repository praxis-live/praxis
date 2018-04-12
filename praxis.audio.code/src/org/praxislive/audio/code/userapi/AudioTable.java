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
 *
 */
package org.praxislive.audio.code.userapi;

/**
 *
 * @author Neil C Smith - http://www.neilcsmith.net
 */
public class AudioTable {
    
    private final float[] data;
    private final double sampleRate;
    private final int channels;
    private final int size;
    
    private AudioTable(float[] data, double sampleRate, int channels, int size) {
        this.data = data;
        this.sampleRate = sampleRate;
        this.channels= channels;
        this.size = size;
    }
    
    public float[] data() {
        return data;
    }
    
    public boolean hasSampleRate() {
        return sampleRate > 0.5;
    }
    
    public double sampleRate() {
        return sampleRate;
    }
    
    public int channels() {
        return channels;
    }
    
    public int size() {
        return size;
    }
    
    public void set(int channel, int index, double value) {
            data[(index * channels) + channel] = (float) value;
        }
    
    public double get(int channel, int idx) {
        return data[(idx * channels) + channel];
    }
    
    public double get(int channel, double pos) {
        int iPos = (int) pos;
        double frac;
        double a,b,c,d;
        double cminusb;
        if (iPos < 1) {
            iPos = 1;
            frac = 0;
        } else if (iPos > (size() - 3)) {
            iPos = size() - 3;
            frac = 1;
        } else {
            frac = pos - iPos;
        }
        a = get(channel, iPos - 1);
        b = get(channel, iPos);
        c = get(channel, iPos + 1);
        d = get(channel, iPos + 2);
        cminusb = c - b;
        return b + frac * (cminusb - 0.5f * (frac - 1) * ((a - d + 3.0f * cminusb) * frac + (b - a - cminusb)));
    }
    
    public static AudioTable generate(int size, int channels) {
        return new AudioTable(new float[size * channels], 0, channels, size);
    }
    
    public static AudioTable wrap(AudioTable original, int size) {
        if (size < 0 || size > original.size) {
            throw new IllegalArgumentException();
        }
        return new AudioTable(original.data, original.sampleRate, original.channels, size);
    }
    
    public static AudioTable wrap(float[] data, double sampleRate, int channels) {
        return new AudioTable(data, sampleRate, channels, data.length / channels);
    }
    
}
