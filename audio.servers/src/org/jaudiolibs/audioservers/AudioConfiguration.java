/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012 Neil C Smith.
 *
 * Copying and distribution of this file, with or without modification,
 * are permitted in any medium without royalty provided the copyright
 * notice and this notice are preserved.  This file is offered as-is,
 * without any warranty.
 *
 * Please visit http://neilcsmith.net if you need additional information or
 * have any questions.
 *
 */
package org.jaudiolibs.audioservers;

/**
 * Provides details of the configuration of the server from which an AudioClient
 * will be called.
 *
 * @author Neil C Smith
 */
public class AudioConfiguration {

    private final float sampleRate;
    private final int inputChannelCount;
    private final int outputChannelCount;
    private final int maxBufferSize;
    private final boolean fixedBufferSize;
    private final ObjectLookup lookup;

    /**
     * Create an AudioConfiguration.
     *
     * @param sampleRate
     * @param inputChannelCount
     * @param outputChannelCount
     * @param maxBufferSize
     * @param fixedBufferSize
     */
    public AudioConfiguration(float sampleRate,
            int inputChannelCount,
            int outputChannelCount,
            int maxBufferSize,
            boolean fixedBufferSize) {
        this(sampleRate, inputChannelCount, outputChannelCount, maxBufferSize, fixedBufferSize, (Object[]) null);
    }
    
    public AudioConfiguration(float sampleRate,
            int inputChannelCount,
            int outputChannelCount,
            int bufferSize,
            Object ... exts) {
        this(sampleRate, inputChannelCount, outputChannelCount, bufferSize, true, exts);
    }

    public AudioConfiguration(float sampleRate,
            int inputChannelCount,
            int outputChannelCount,
            int maxBufferSize,
            boolean fixedBufferSize,
            Object ... exts) {
        this.sampleRate = validate(sampleRate, 1);
        this.inputChannelCount = validate(inputChannelCount, 0);
        this.outputChannelCount = validate(outputChannelCount, 0);
        this.maxBufferSize = validate(maxBufferSize, 1);
        this.fixedBufferSize = fixedBufferSize;
        if (exts == null || exts.length == 0) {
            lookup = ObjectLookup.EMPTY;
        } else {
            lookup = new ObjectLookup(exts);
        }
    }
    
    private static float validate(float value, float minimum) {
        if (value < minimum) {
            throw new IllegalArgumentException();
        }
        return value;
    }
    
    private static int validate(int value, int minimum) {
        if (value < minimum) {
            throw new IllegalArgumentException();
        }
        return value;
    }

    /**
     * Is the buffer size fixed. If variable, the buffer size will always be
     * between 1 and getMaxBufferSize().
     *
     * @return true if fixed, otherwise variable.
     */
    public boolean isFixedBufferSize() {
        return fixedBufferSize;
    }

    /**
     * Get the number of input channels.
     *
     * @return int ( >=0 )
     */
    public int getInputChannelCount() {
        return inputChannelCount;
    }

    /**
     * Get the maximum buffer size. This is the buffer size in samples per
     * channel.
     *
     * @return int ( >=1 )
     */
    public int getMaxBufferSize() {
        return maxBufferSize;
    }

    /**
     * Get the number of output channels.
     *
     * @return int ( >=0 )
     */
    public int getOutputChannelCount() {
        return outputChannelCount;
    }

    /**
     * Get the sample rate.
     *
     * @return float ( >=1 )
     */
    public float getSampleRate() {
        return sampleRate;
    }
    
    
    public <T> T find(Class<T> type) {
        return lookup.find(type);
    }
    
    public <T> Iterable<T> findAll(Class<T> type) {
        return lookup.findAll(type);
    }
    

    @Override
    public String toString() {
        return "Audio Configuration --- \n"
                + "Sample Rate : " + sampleRate + "\n"
                + "Input Channels : " + inputChannelCount + "\n"
                + "Output Channels : " + outputChannelCount + "\n"
                + "Max Buffer Size : " + maxBufferSize + "\n"
                + "Fixed Buffer Size : " + fixedBufferSize;
    }
}
