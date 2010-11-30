/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Neil C Smith.
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
package net.neilcsmith.audioservers.javasound;

import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.Mixer;
import javax.sound.sampled.SourceDataLine;
import javax.sound.sampled.TargetDataLine;
import net.neilcsmith.audioservers.AudioClient;
import net.neilcsmith.audioservers.AudioConfiguration;
import net.neilcsmith.audioservers.AudioServer;

/**
 * Implementation of an AudioServer using Javasound.
 * @author Neil C Smith
 */
public class JavasoundAudioServer implements AudioServer {

    /**
     * Timing mode used by the server.
     *
     * For lowest latency try {@link #FramePosition} or {@link #Estimated}.
     */
    public static enum TimingMode {

        /**
         * Block on write to output line. Javasound output buffer is same size
         * as internal buffers.
         */
        Blocking,
        /**
         * Use large Javasound output buffer. Determine when to write to output
         * line via getLongFramePosition().
         */
        FramePosition,
        /**
         * Use large Javasound output buffer.  Determine when to write to output
         * line by estimating position using System.nanotime().
         *
         * @TODO Investigate whether xruns in underlying library are causing
         * latency to increase.
         */
        Estimated
    };

    private enum State {

        New, Initialising, Active, Closing, Terminated
    };

    // JS line defaults - need way to make these settable.
    private int nonBlockingOutputRatio = 16;
    private int lineBitSize = 16;
    private boolean signed = true;
    private boolean bigEndian = false;
    //
    private AtomicReference<State> state;
    private AudioConfiguration context;
    private Mixer mixer;
    private AudioClient client;
    private TimingMode mode;
    private TargetDataLine inputLine;
    private SourceDataLine outputLine;
//    private int inputByteFrameSize;
    private byte[] inputByteBuffer;
    private float[] inputFloatBuffer;
//    private int outputByteFrameSize;
    private byte[] outputByteBuffer;
    private float[] outputFloatBuffer;
    private List<FloatBuffer> inputBuffers;
    private List<FloatBuffer> outputBuffers;
    private AudioFloatConverter converter;

    private JavasoundAudioServer(Mixer mixer, TimingMode mode,
            AudioConfiguration context, AudioClient client) {
        this.mixer = mixer;
        this.context = context;
        this.mode = mode;
        this.client = client;
        state = new AtomicReference<State>(State.New);
    }

    public void run() throws Exception {
        if (!state.compareAndSet(State.New, State.Initialising)) {
            throw new IllegalStateException();
        }
        try {
            initialise();
            client.configure(context);
        } catch (Exception ex) {
            state.set(State.Terminated);
            closeAll();
            client.shutdown();
            throw ex;
        }
        if (state.compareAndSet(State.Initialising, State.Active)) {
            runImpl();
        }
        closeAll();
        client.shutdown();
        state.set(State.Terminated);
    }

    public AudioConfiguration getAudioContext() {
        return context;
    }

    public boolean isActive() {
        State st = state.get();
        return (st == State.Active || st == State.Closing);
    }

    public void shutdown() {
        State st;
        do {
            st = state.get();
            if (st == State.Terminated || st == State.Closing) {
                break;
            }
        } while (!state.compareAndSet(st, State.Closing));
    }

    private void initialise() throws Exception {
        float srate = (float) context.getSampleRate();
        int buffersize = context.getMaxBufferSize();
        int inputChannels = context.getInputChannelCount();
        int outputChannels = context.getOutputChannelCount();
        // open input line and create internal buffers
        if (inputChannels > 0) {
            AudioFormat inputFormat = new AudioFormat(srate, lineBitSize,
                    inputChannels, signed, bigEndian);
            inputLine = (TargetDataLine) mixer.getLine(
                    new DataLine.Info(TargetDataLine.class, inputFormat));
            inputFloatBuffer = new float[buffersize * inputChannels];
            int byteBufferSize = buffersize * inputFormat.getFrameSize();
            inputByteBuffer = new byte[byteBufferSize];
            byteBufferSize *= nonBlockingOutputRatio;
            inputLine.open(inputFormat, byteBufferSize);

        }
        // open output line and create internal buffers
        AudioFormat outputFormat = new AudioFormat(srate, lineBitSize,
                outputChannels, signed, bigEndian);
        outputLine = (SourceDataLine) mixer.getLine(
                new DataLine.Info(SourceDataLine.class, outputFormat));
        outputFloatBuffer = new float[buffersize * outputChannels];
        int byteBufferSize = buffersize * outputFormat.getFrameSize();
        outputByteBuffer = new byte[byteBufferSize];
        if (mode != TimingMode.Blocking) {
            byteBufferSize *= nonBlockingOutputRatio;
        }
        outputLine.open(outputFormat, byteBufferSize);

        // create audio converter
        converter = AudioFloatConverter.getConverter(outputFormat);

        // create client buffers
        List<FloatBuffer> ins = new ArrayList<FloatBuffer>(inputChannels);
        for (int i = 0; i < inputChannels; i++) {
            ins.add(FloatBuffer.allocate(buffersize));
        }
        inputBuffers = Collections.unmodifiableList(ins);
        List<FloatBuffer> outs = new ArrayList<FloatBuffer>(outputChannels);
        for (int i = 0; i < outputChannels; i++) {
            outs.add(FloatBuffer.allocate(buffersize));
        }
        outputBuffers = Collections.unmodifiableList(outs);
    }

    private void runImpl() {
        if (inputLine != null) {
            inputLine.start();
        }
        outputLine.start();

        long startTime = System.nanoTime();
        double bufferTime = ((double) context.getMaxBufferSize() /
                context.getSampleRate()) * 1000000000;
        long bufferCount = 0;
        int bufferSize = context.getMaxBufferSize();
        try {
            while (state.get() == State.Active) {
                readInput();
                if (client.process(System.nanoTime(), inputBuffers, outputBuffers, bufferSize)) {
                    writeOutput();
                    switch (mode) {
                        case Estimated:
                            while (((System.nanoTime() - startTime) / bufferTime) < bufferCount) {
                                try {
                                    Thread.sleep(1);
                                } catch (Exception ex) {
                                }
                            }
                            break;
                        case FramePosition:
                            while (outputLine.getLongFramePosition() < (bufferCount * bufferSize)) {
                                try {
                                    Thread.sleep(1);
                                } catch (Exception ex) {
                                }
                            }
                            break;
                    }
                    bufferCount++;
                } else {
                    shutdown();
                }
            }
        } catch (Exception ex) {
            Logger.getLogger(JavasoundAudioServer.class.getName()).log(Level.SEVERE, "", ex);
        }


    }

    private void readInput() {
        TargetDataLine tdl = inputLine;
        if (tdl != null) {
            int bsize = inputByteBuffer.length;
            if (tdl.available() < bsize) {
                int fsize = inputFloatBuffer.length;
                for (int i = 0; i < fsize; i++) {
                    inputFloatBuffer[i] = 0;
                }
            } else {
                tdl.read(inputByteBuffer, 0, bsize);
                converter.toFloatArray(inputByteBuffer, inputFloatBuffer);
            }
            int channels = inputBuffers.size();
            // deinterleave into buffers
            for (int channel = 0; channel < channels; channel++) {
                FloatBuffer inBuf = inputBuffers.get(channel);
                float[] input = inBuf.array();
                for (int i = 0, x = channel; i < input.length; i++) {
                    input[i] = inputFloatBuffer[x];
                    x += channels;
                }
                inBuf.rewind();
            }
        }
    }

    private void writeOutput() {
        // interleave outputs
        int channels = outputBuffers.size();
        for (int channel = 0; channel < channels; channel++) {
            FloatBuffer outBuf = outputBuffers.get(channel);
            float[] output = outBuf.array();
            for (int i = 0, x = channel; i < output.length; i++) {
                outputFloatBuffer[x] = output[i];
                x += channels;
            }
            outBuf.rewind();
        }
        // convert audio
        converter.toByteArray(outputFloatBuffer, outputByteBuffer);
        // write to output
        outputLine.write(outputByteBuffer, 0, outputByteBuffer.length);

    }

    private void closeAll() {
        SourceDataLine sdl = outputLine;
        if (sdl != null) {
            sdl.close();
        }
        TargetDataLine tdl = inputLine;
        if (tdl != null) {
            tdl.close();
        }
    }

    public static JavasoundAudioServer create(Mixer mixer, AudioConfiguration context,
            TimingMode mode, AudioClient client) {
        if (mixer == null || mode == null ||
                context == null || client == null) {
            throw new NullPointerException();
        }
        // must have real time output
        if (context.getOutputChannelCount() == 0) {
            throw new IllegalArgumentException();
        }
        // always fixed buffer size
        if (!context.isFixedBufferSize()) {
            context = new AudioConfiguration(context.getSampleRate(),
                    context.getInputChannelCount(),
                    context.getOutputChannelCount(),
                    context.getMaxBufferSize(),
                    true);
        }
        return new JavasoundAudioServer(mixer, mode, context, client);
    }
}
