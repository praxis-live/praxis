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

package net.neilcsmith.rapl.ops;

/**
 * This interface defines an operation on audio data, expected to be a series of
 * float[] buffers that are contiguous elements of an audio data stream. The
 * initialize(float samplerate) method should always be called before either of
 * the process... methods is called. The reset() method should always be called
 * in case of a break in the stream data.
 *
 * Implementors of this interface should specify whether they can process data in
 * place - ie. whether the input and output data sent to the process methods can
 * be the same. They should also specify the minimum and maximum number of input
 * and output channels they exArrayIndexOutOfBoundsExceptionpect, and are free to throw Exceptions (eg.
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
     * Initialize AudioOp with required sample rate
     *
     * @param samplerate
     */
    public void initialize(float samplerate);



    /**
     * Reset AudioOp in case of break in stream.
     */
    public void reset();


    /**
     * Whether this AudioOp requires input for the next buffer of audio. This
     * should be the checked immediately prior to calling one of the process
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
