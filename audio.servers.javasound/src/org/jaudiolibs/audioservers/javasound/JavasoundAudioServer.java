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
package org.jaudiolibs.audioservers.javasound;

import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.Line;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.Mixer;
import javax.sound.sampled.SourceDataLine;
import javax.sound.sampled.TargetDataLine;
import org.jaudiolibs.audioservers.AudioClient;
import org.jaudiolibs.audioservers.AudioConfiguration;
import org.jaudiolibs.audioservers.AudioServer;

/**
 * Implementation of an AudioServer using Javasound.
 *
 * @author Neil C Smith
 */
public class JavasoundAudioServer implements AudioServer {

    private final static Logger LOG = Logger.getLogger(JavasoundAudioServer.class.getName());

    /**
     * Timing mode used by the server.
     *
     * For lowest latency try {@link #FramePosition} or {@link #Estimated}.
     */
    public static enum TimingMode {

        /**
         * Blocking timing mode. Block on write to output line. Javasound output
         * buffer is same size as internal buffers.
         */
        Blocking,
        /**
         * FramePosition timing mode. Use large Javasound output buffer.
         * Determine when to write to output line via getLongFramePosition().
         */
        FramePosition,
        /**
         * Estimated timing mode. Use large Javasound output buffer. Determine
         * when to write to output line by estimating position using
         * System.nanotime().
         */
        // @TODO Investigate whether xruns in underlying library are causing latency to increase.
        Estimated
    };

    private enum State {

        New, Initialising, Active, Closing, Terminated
    };
    private final static int NON_BLOCKING_MIN_BUFFER = 16384;
    // JS line defaults - need way to make these settable.
    private int nonBlockingOutputRatio = 16;
    private int lineBitSize = 16;
    private boolean signed = true;
    private boolean bigEndian = false;
    //
    private AtomicReference<State> state;
    private AudioConfiguration context;
//    private Mixer mixer;
    private Mixer inputMixer;
    private Mixer outputMixer;
    private AudioClient client;
    private TimingMode mode;
    private TargetDataLine inputLine;
    private SourceDataLine outputLine;
    private byte[] inputByteBuffer;
    private float[] inputFloatBuffer;
    private byte[] outputByteBuffer;
    private float[] outputFloatBuffer;
    private List<FloatBuffer> inputBuffers;
    private List<FloatBuffer> outputBuffers;
    private AudioFloatConverter converter;

    private JavasoundAudioServer(Mixer inputMixer, Mixer outputMixer, TimingMode mode,
            AudioConfiguration context, AudioClient client) {
        this.inputMixer = inputMixer;
        this.outputMixer = outputMixer;
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
            inputLine = (TargetDataLine) inputMixer.getLine(
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
        outputLine = (SourceDataLine) outputMixer.getLine(
                new DataLine.Info(SourceDataLine.class, outputFormat));
        outputFloatBuffer = new float[buffersize * outputChannels];
        int byteBufferSize = buffersize * outputFormat.getFrameSize();
        outputByteBuffer = new byte[byteBufferSize];
        if (mode != TimingMode.Blocking) {
            byteBufferSize *= nonBlockingOutputRatio;
            byteBufferSize = Math.min(byteBufferSize, NON_BLOCKING_MIN_BUFFER);
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
        long now = startTime;
        double bufferTime = ((double) context.getMaxBufferSize()
                / context.getSampleRate());
        TimeFilter dll = new TimeFilter(bufferTime, 0.5);
        bufferTime *= 1e9;
        long bufferCount = 0;
        int bufferSize = context.getMaxBufferSize();
        final boolean debug = LOG.isLoggable(Level.FINEST);

        try {
            while (state.get() == State.Active) {
                bufferCount++;
                now = System.nanoTime();
                readInput();
                if (client.process((long) (dll.update(now / 1e9) * 1e9), inputBuffers, outputBuffers, bufferSize)) {
                    writeOutput();
                    switch (mode) {
                        case Estimated:
//                            while (((now - startTime) / bufferTime) < bufferCount) {
                            while (((System.nanoTime() - startTime) / bufferTime) < bufferCount) {
                                try {
                                    Thread.sleep(1);
//                                    now = System.nanoTime();
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
//                            now = System.nanoTime();
                            break;
                        default:
//                            now = System.nanoTime();
                    }
                } else {
                    shutdown();
                }
                if (debug) {
                    processDebug(dll);
                }
            }
        } catch (Exception ex) {
            Logger.getLogger(JavasoundAudioServer.class.getName()).log(Level.SEVERE, "", ex);
        }
    }

    private void processDebug(TimeFilter dll) {
        long x = dll.ncycles - 1;
        if (x == 0) {
            LOG.finest("| audiotime drift | filter drift  | systime jitter | filter jitter  |");
        }
        if (x % 1000 == 0) {
            double device_drift = (dll.device_time - dll.system_time) * 1000.0;
            double filter_drift = (dll.filter_time - dll.system_time) * 1000.0;
            double device_rate_error = device_drift / dll.ncycles;
            double filter_jitter = dll.filter_period_error - device_rate_error;
            double system_jitter = dll.system_period_error - device_rate_error;

            LOG.finest(String.format("| %15.6f | %13.6f | %14.6f | %14.6f |",
                    device_drift,
                    filter_drift,
                    system_jitter,
                    filter_jitter));
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

    /**
     * Create a JavasoundAudioServer.
     *
     *
     * @param inputMixer Javasound mixer to use for audio input. Can be null as
     * long as the context passed in requests no audio input channels.
     * @param outputMixer Javasound mixer to use for audio output.
     * @param context Requested audio configuration. Variable buffer size
     * property is ignored.
     * @param mode Timing mode to use. For lowest latency use
     * TimingMode.Estimated or TimingMode.FramePosition.
     * @param client Audio client to process every callback.
     * @return server
     * @throws IllegalArgumentException if requested context has no output
     * channels.
     */
    public static JavasoundAudioServer create(Mixer inputMixer, Mixer outputMixer,
            AudioConfiguration context, TimingMode mode, AudioClient client) {
        if (outputMixer == null || mode == null
                || context == null || client == null) {
            throw new NullPointerException();
        }
        if (inputMixer == null && context.getInputChannelCount() > 0) {
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
        return new JavasoundAudioServer(inputMixer, outputMixer, mode, context, client);
    }

    /**
     * Create a JavasoundAudioServer.
     *
     *
     * @param device Name of the device to use for audio input and output. If
     * null or an empty String, the first (default) device that provides the
     * required number of lines will be chosen. Otherwise a device will be
     * searched for that contains the provided String in its name.
     * @param context Requested audio configuration. Variable buffer size
     * property is ignored.
     * @param mode Timing mode to use. For lowest latency use
     * TimingMode.Estimated or TimingMode.FramePosition.
     * @param client Audio client to process every callback.
     * @return server
     * @throws Exception if requested context has no output channels, or if
     * suitable mixers cannot be found to satisfy the device name.
     */
    public static JavasoundAudioServer create(String device, AudioConfiguration context,
            TimingMode mode, AudioClient client) throws Exception {
        if (mode == null || client == null) {
            throw new NullPointerException();
        }
        if (device == null) {
            device = "";
        }
        // must have real time output
        if (context.getOutputChannelCount() == 0) {
            throw new IllegalArgumentException();
        }
        Mixer in = null;
        if (context.getInputChannelCount() > 0) {
            in = findInputMixer(device);
        }
        Mixer out = findOutputMixer(device);
        // always fixed buffer size
        if (!context.isFixedBufferSize()) {
            context = new AudioConfiguration(context.getSampleRate(),
                    context.getInputChannelCount(),
                    context.getOutputChannelCount(),
                    context.getMaxBufferSize(),
                    true);
        }
        return new JavasoundAudioServer(in, out, mode, context, client);
    }

    private static Mixer findInputMixer(String device) throws Exception {
        Mixer.Info[] infos = AudioSystem.getMixerInfo();
        Mixer mixer;
        Line.Info[] lines;
        for (int i = 0; i < infos.length; i++) {
            mixer = AudioSystem.getMixer(infos[i]);
            lines = mixer.getTargetLineInfo();
            if (lines.length > 0) {
                if (infos[i].getName().indexOf(device) >= 0) {
                    LOG.finest("Found input mixer :\n" + infos[i]);
                    return mixer;
                }
            }
        }
        throw new Exception();
    }

    private static Mixer findOutputMixer(String device) throws LineUnavailableException {
        Mixer.Info[] infos = AudioSystem.getMixerInfo();
        Mixer mixer;
        Line.Info[] lines;
        for (int i = 0; i < infos.length; i++) {
            mixer = AudioSystem.getMixer(infos[i]);
            lines = mixer.getSourceLineInfo();
            if (lines.length > 0) {
                if (infos[i].getName().indexOf(device) >= 0) {
                    LOG.finest("Found output mixer :\n" + infos[i]);
                    return mixer;
                }
            }
        }
        throw new LineUnavailableException();
    }
}
