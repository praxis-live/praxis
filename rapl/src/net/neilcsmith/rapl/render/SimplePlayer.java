/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2010 Neil C Smith.
 * 
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 3 only, as
 * published by the Free Software Foundation.
 * 
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 3 for more details.
 * 
 * You should have received a copy of the GNU General Public License version 3
 * along with this work; if not, see http://www.gnu.org/licenses/
 * 
 * 
 * Please visit http://neilcsmith.net if you need additional information or
 * have any questions.
 */
package net.neilcsmith.rapl.render;

import net.neilcsmith.rapl.core.BufferRateSource;
import net.neilcsmith.rapl.core.BufferRateListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.Mixer;
import javax.sound.sampled.SourceDataLine;
import javax.sound.sampled.TargetDataLine;
import net.neilcsmith.rapl.core.Buffer;
import net.neilcsmith.rapl.core.Bus;
import net.neilcsmith.rapl.core.Sink;
import net.neilcsmith.rapl.core.SinkIsFullException;
import net.neilcsmith.rapl.core.Source;
import net.neilcsmith.rapl.core.SourceIsFullException;
import net.neilcsmith.rapl.util.AudioFloatConverter;

/**
 *
 * @author Neil C Smith
 */
public class SimplePlayer implements Bus, Runnable {

    private float sampleRate;
    private int bufferSize;
    private int nChannels;
    private long time;
    private volatile boolean running;
    private InputSource[] sources;
    private OutputSink[] sinks;
//    private float[][] outputBuffers;
    private DefaultBuffer[] outputBuffers;
    private List<BufferRateListener> listeners = new ArrayList<BufferRateListener>();
    private TargetDataLine inputLine;
    private SourceDataLine outputLine;
    private float[] inputData;
    private float[] outputData;
    private byte[] inputByteData;
    private byte[] outputByteData;
    private AudioFloatConverter audioConverter;

    /**
     * 
     * @param sampleRate
     * @param bufferSize
     * @param nChannels
     */
    public SimplePlayer(float sampleRate, int bufferSize, int nChannels) {
        if (sampleRate < 1 || bufferSize < 1 || nChannels < 1) {
            throw new IllegalArgumentException();
        }
        this.sampleRate = sampleRate;
        this.bufferSize = bufferSize;
        this.nChannels = nChannels;
        sinks = new OutputSink[nChannels];
        for (int i = 0; i < nChannels; i++) {
            sinks[i] = new OutputSink();
        }
        sources = new InputSource[nChannels];
        for (int i = 0; i < nChannels; i++) {
            sources[i] = new InputSource(i, nChannels);
        }


    }

    public void addBufferRateListener(BufferRateListener listener) {
        if (listener == null) {
            throw new NullPointerException();
        }
        if (listeners.contains(listener)) {
            return;
        }
        listeners.add(listener);
    }

    public void removeBufferRateListener(BufferRateListener listener) {
        listeners.remove(listener);
    }

    public long getTime() {
        return time;
    }

    public Sink getSink(int index) {
        return sinks[index];
    }
    
    public int getSinkCount() {
        return sinks.length;
    }

    public Source getSource(int index) {
        return sources[index];
    }
    
    public int getSourceCount() {
        return sources.length;
    }

    public void run() {
        running = true;
        try {
            init();
        } catch (Exception ex) {
            running = false;
            return;
        }

        // @TODO Remove all this and log properly!
        System.out.println("Init complete...");
        System.out.println("Input = " + inputLine);
        System.out.println("Output = " + outputLine);
        System.out.println("Input min bufferSize = " + ((DataLine.Info) (inputLine.getLineInfo())).getMinBufferSize());
        System.out.println("Input max bufferSize = " + ((DataLine.Info) (inputLine.getLineInfo())).getMaxBufferSize());
        System.out.println("Input buffersize = " + inputLine.getBufferSize());
        System.out.println("Output min bufferSize = " + ((DataLine.Info) (outputLine.getLineInfo())).getMinBufferSize());
        System.out.println("Output max bufferSize = " + ((DataLine.Info) (outputLine.getLineInfo())).getMaxBufferSize());
        System.out.println("Output buffersize = " + outputLine.getBufferSize());


        long period = (long) ((bufferSize / sampleRate) * 1000000000);

        System.out.println("Period = " + period);

        this.time = System.nanoTime();
        while (running) {
            fireListeners();
            readLine();
            callSources();
            writeToLine();
//            readLine();
//            time += period;
            this.time = System.nanoTime();
        }
        
        dispose();

    }
    
    private void dispose() {
        inputLine.stop();
        inputLine.flush();
        inputLine.close();
        outputLine.stop();
        outputLine.flush();
        outputLine.close();
        for (Source source : sources) {
            for (Sink sink : source.getSinks()) {
                sink.removeSource(source);
            }
        }
        for (Sink sink : sinks) {
            for (Source source : sink.getSources()) {
                sink.removeSource(source);
            }
        }
    }
    
    public void terminate() {
        running = false;
    }

    private void fireListeners() {
        int count = listeners.size();
        for (int i = 0; i < count; i++) {
            listeners.get(i).nextBuffer(this);
        }
    }
    private int skipCount = 0;

    private void readLine() {
//        if (inputLine.available() >= inputByteData.length) {
//            inputLine.read(inputByteData, 0, inputByteData.length);
//            audioConverter.toFloatArray(inputByteData, inputData);
//        } else {
//            System.out.println("Buffer skip " + skipCount);
//            Arrays.fill(inputData, 0);
//            skipCount++;
//        }
          inputLine.read(inputByteData, 0, inputByteData.length);
          audioConverter.toFloatArray(inputByteData, inputData);
        
    }

    private void callSources() {
        for (int i = 0; i < nChannels; i++) {
            sinks[i].process(outputBuffers[i], time);
        }
    }

    private void writeToLine() {
        for (int i = 0; i < nChannels; i++) {
            float[] data = outputBuffers[i].getData();
            for (int j = 0,  k = i; j < data.length; j++, k += nChannels) {
                outputData[k] = data[j];
            }
        }
//        convertToBytes(mbuf, byteData);
        audioConverter.toByteArray(outputData, outputByteData);
        outputLine.write(outputByteData, 0, outputByteData.length);

    }

    private void init() throws Exception {

        AudioFormat format = new AudioFormat(sampleRate, 16, nChannels, true, false);
//        AudioFormat fm2 = new AudioFormat((float)sampleRate, 16, 1, true, false);
        System.out.println("Format frame size = " + format.getFrameSize());
        System.out.println("Format frame rate = " + format.getFrameRate());
//        DataLine.Info outputInfo = new DataLine.Info(SourceDataLine.class, format, bufferSize * 4); //, bufferSize * 2);
//        DataLine.Info inputInfo = new DataLine.Info(TargetDataLine.class, format, bufferSize * 4); //, bufferSize * 2);
//        DataLine.Info outputInfo = new DataLine.Info(SourceDataLine.class, format, bufferSize * format.getFrameSize()); //, bufferSize * 2);
//        DataLine.Info inputInfo = new DataLine.Info(TargetDataLine.class, format, bufferSize * format.getFrameSize());
        DataLine.Info outputInfo = new DataLine.Info(SourceDataLine.class, format);
        DataLine.Info inputInfo = new DataLine.Info(TargetDataLine.class, format);
//        outputLine = (SourceDataLine) AudioSystem.getMixer(AudioSystem.getMixerInfo()[0]).getLine(outputInfo);
//        inputLine = (TargetDataLine) AudioSystem.getMixer(AudioSystem.getMixerInfo()[0]).getLine(inputInfo);

        Mixer.Info mixerInfo = AudioSystem.getMixerInfo()[0];
        System.out.println("Mixer : " + mixerInfo);
        inputLine = AudioSystem.getTargetDataLine(format, mixerInfo);
        outputLine = AudioSystem.getSourceDataLine(format, mixerInfo);

//        outputLine.open(format, bufferSize * 4);
        
        outputLine.open(format, bufferSize * format.getFrameSize() * 2);
//        outputLine.open(format);
        inputLine.open(format, bufferSize * format.getFrameSize() * 2);
//        inputLine.open(format);
        
        audioConverter = AudioFloatConverter.getConverter(format);
        outputBuffers = new DefaultBuffer[nChannels];
        for (int i = 0; i < nChannels; i++) {
            outputBuffers[i] = new DefaultBuffer(sampleRate, bufferSize);
        }
        inputData = new float[nChannels * bufferSize];
        outputData = new float[nChannels * bufferSize];
        inputByteData = new byte[2 * inputData.length];
        outputByteData = new byte[2 * outputData.length];
        inputLine.start();
        outputLine.start();
    }

    private class OutputSink implements Sink {

        private Source source; // only allow one connection
        private long time;

        public void addSource(Source source) throws SinkIsFullException, SourceIsFullException {
            if (source == null) {
                throw new NullPointerException();
            }
            if (this.source != null) {
                throw new SinkIsFullException();
            }
            source.registerSink(this);
            this.source = source;
        }

        public void removeSource(Source source) {
            if (this.source == source) {
                source.unregisterSink(this);
                this.source = null;
            }
        }

        public boolean isRenderRequired(Source source, long time) {
            return true; //(source == this.source && time == this.time);
        }

        public Source[] getSources() {
            if (source == null) {
                return new Source[0];
            } else {
                return new Source[]{source};
            }
        }

        private void process(Buffer buffer, long time) {
            this.time = time;
            if (source != null) {
                source.process(buffer, this, time);
            } else {
                buffer.clear();
            }
        }
    }

    private class InputSource implements Source {

        private Sink sink;
        private int channel;
        private int channelCount;

        private InputSource(int channel, int channelCount) {
            this.channel = channel;
            this.channelCount = channelCount;
        }

        public void process(Buffer buffer, Sink sink, long time) {
            if (sink == this.sink && sink.isRenderRequired(this, time)) {
                int bufferSize = buffer.getSize();
                float[] in = inputData;
                float[] out = buffer.getData();
                int inputSize = in.length;
                int bufIdx = 0;
                int inputIdx = channel;
                while (bufIdx < bufferSize && inputIdx < inputSize) {
//                    buffer.set(bufIdx, in[inputIdx]);
                    out[bufIdx] = in[inputIdx];
                    bufIdx++;
                    inputIdx += channelCount;
                }
            }
        }

        public long getTime() {
            return time;
        }

        public void setTime(long time, boolean recurse) {
        // ignore ??
        }

        public void registerSink(Sink sink) throws SourceIsFullException {
            if (sink == null) {
                throw new NullPointerException();
            }
            if (this.sink != null) {
                throw new SourceIsFullException();
            }
            this.sink = sink;
        }

        public void unregisterSink(Sink sink) {
            if (this.sink == sink) {
                this.sink = null;
            }
        }

        public Sink[] getSinks() {
            if (sink == null) {
                return new Sink[0];
            } else {
                return new Sink[]{sink};
            }
        }
    }

    
}
