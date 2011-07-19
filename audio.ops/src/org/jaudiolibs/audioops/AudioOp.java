/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Neil C Smith.
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
package org.jaudiolibs.audioops;

/**
 * This interface defines an operation on audio data, expected to be a series of
 * float[] buffers that are contiguous elements of an audio data stream. The
 * initialize(float samplerate, int maxBufferSize) method should always be
 * called before either of the process... methods is called. An IllegalStateException
 * may be thrown if a process method is called without the Op being initialized.
 *
 * The reset() method should always be called in case of a break in the stream data.
 *
 * Implementors of this interface should specify whether they can process data in
 * place - ie. whether the input and output data sent to the process methods can
 * be the same. They should also specify the minimum and maximum number of input
 * and output channels they expect, and are free to throw Exceptions (eg.
 * ArrayIndexOutOfBoundsException) if the data is not arranged as expected.
 *
 * @author Neil C Smith
 */
public interface AudioOp {

    /**
     * Process input buffers and replace data in output buffers
     *
     * @param buffersize size of buffer to process, data arrays may be bigger
     * @param outputs array of audio channels
     * @param inputs array of audio channels. May be null only if isInputRequired()
     * returns false.
     */
    public void processReplace(int buffersize, float[][] outputs, float[][] inputs);

    /**
     * Process input buffers and add to existing data in output buffers
     *
     * @param buffersize size of buffer to process, data arrays may be bigger
     * @param outputs array of audio channels
     * @param inputs array of audio channels. May be null only if isInputRequired()
     * returns false.
     */
    public void processAdd(int buffersize, float[][] outputs, float[][] inputs);

    /**
     * Initialize AudioOp with required sample rate and maximum buffer size.
     *
     * The buffer size is the maximum size that will be passed to one of the
     * process methods. Ops should use this method to allocate resources.
     *
     * This method may be called more than once on an Op if the system is
     * reconfigured.
     *
     * @param samplerate
     * @param maxBufferSize
     */
    public void initialize(float samplerate, int maxBufferSize);

    /**
     * Reset AudioOp in case of break in stream.
     */
    public void reset();

    /**
     * Whether this AudioOp requires input for the next buffer of audio. This
     * should be checked immediately prior to calling one of the process
     * methods, as any changes to the AudioOp's state may affect the return value
     * of this method.
     *
     * If, and only if, this method returns false, then a null value may be sent
     * as the input buffer data to either of the process methods.
     *
     * @return true if AudioOp requires input data to work on.
     */
    public boolean isInputRequired();
}
