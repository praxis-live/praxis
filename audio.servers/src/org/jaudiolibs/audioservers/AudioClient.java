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

package org.jaudiolibs.audioservers;

import java.nio.FloatBuffer;
import java.util.List;

/**
 * The basic interface that will be used by server implementations. This interface
 * is deliberately decoupled from any particular audio server implementation. All
 * necessary information will be passed from the server in the configure method
 * before any call to process().
 *
 *
 * @author Neil C Smith
 */
public interface AudioClient {

    /**
     * This method will be called by the server implementation prior to any call
     * to process(). The supplied AudioConfiguration object provides information on
     * sample rate, buffer size, etc. required by the client. An Exception may be
     * thrown if the client is unable to be configured to match the requirements of
     * the server. The type of the Exception is deliberately unspecified and left to
     * the implementation, but will commonly be an IllegalArgumentException or
     * IllegalStateException.
     * @param context encapsulates information required for configuring the client
     * @throws Exception
     */
    public void configure(AudioConfiguration context) throws Exception;

    /**
     * The method that actually processes the audio. The client is provided with
     * the time for the current buffer, measured in nanoseconds and relative to
     * System.nanotime(). The client should always use the time provided by the
     * server.
     *
     * The server will provide the client with unmodifiable lists of input and
     * output audio buffers as FloatBuffers. In the case of there being no input
     * channels, a zero length list rather than null will be passed in. Input buffers
     * should be treated as read-only.
     *
     * The server will provide the number of frames of audio in each buffer. The count
     * will always be the same across each input and output buffer. If the
     * context passed to configure returns true for isFixedBufferSize() then the
     * number of frames will always be equal to getMaxBufferSize(). Otherwise, the
     * number of frames may be between 1 and getMaxBufferSize().
     *
     * The client should return a boolean value - true if the audio has been processed
     * OK, false to disconnect the client from the server.
     *
     * @param time
     * @param inputs
     * @param outputs
     * @param nframes
     * @return boolean (OK / disconnect)
     */
    public boolean process(long time, List<FloatBuffer> inputs,
           List<FloatBuffer> outputs, int nframes);

    /**
     * Signal that the client is being shut down. It is up to implementors of this
     * interface whether they allow themselves to be reconfigured ready to process
     * audio again.
     */
    public void shutdown();

}
