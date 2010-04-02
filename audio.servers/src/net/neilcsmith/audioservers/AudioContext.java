/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2009 Neil C Smith. All rights reserved.
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
 *
 */

package net.neilcsmith.audioservers;

/**
 * Provides details of the context within which an AudioClient will be called.
 * @author Neil C Smith
 */
public class AudioContext {

    private float sampleRate;
    private int inputChannelCount;
    private int outputChannelCount;
    private int maxBufferSize;
    private boolean fixedBufferSize;

    /**
     * Create an AudioContext.
     * @param sampleRate
     * @param inputChannelCount
     * @param outputChannelCount
     * @param maxBufferSize
     * @param fixedBufferSize
     */
    public AudioContext(float sampleRate, int inputChannelCount, int outputChannelCount, int maxBufferSize, boolean fixedBufferSize) {
        this.sampleRate = sampleRate;
        this.inputChannelCount = inputChannelCount;
        this.outputChannelCount = outputChannelCount;
        this.maxBufferSize = maxBufferSize;
        this.fixedBufferSize = fixedBufferSize;
    }

    /**
     * Is the buffer size fixed, or may it vary. If variable, the buffer size
     * will always be between 1 and getMaxBufferSize().
     * @return true if fixed, otherwise variable.
     */
    public boolean isFixedBufferSize() {
        return fixedBufferSize;
    }

    /**
     * Get the number of input channels.
     * @return int ( >=0 )
     */
    public int getInputChannelCount() {
        return inputChannelCount;
    }

    /**
     * Get the maximum buffer size. This is the buffer size in samples per channel.
     * @return int ( >=1 )
     */
    public int getMaxBufferSize() {
        return maxBufferSize;
    }

    /**
     * Get the number of output channels.
     * @return int ( >=0 )
     */
    public int getOutputChannelCount() {
        return outputChannelCount;
    }

    /**
     * Get the sample rate.
     * @return float ( >=1 )
     */
    public float getSampleRate() {
        return sampleRate;
    }

    @Override
    public String toString() {
        return "AudioContext --- \n" +
                "Sample Rate : " + sampleRate + "\n" +
                "Input Channels : " + inputChannelCount + "\n" +
                "Output Channels : " + outputChannelCount + "\n" +
                "Max Buffer Size : " + maxBufferSize + "\n" +
                "Fixed Buffer Size : " + fixedBufferSize;
    }

    



}
