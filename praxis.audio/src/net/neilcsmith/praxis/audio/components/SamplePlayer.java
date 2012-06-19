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
package net.neilcsmith.praxis.audio.components;

import java.util.logging.Level;
import java.util.logging.Logger;
import net.neilcsmith.praxis.audio.impl.DefaultAudioInputPort;
import net.neilcsmith.praxis.audio.impl.DefaultAudioOutputPort;
import net.neilcsmith.praxis.audio.io.SampleTable;
import net.neilcsmith.praxis.core.ControlPort;
import net.neilcsmith.praxis.core.Port;
import net.neilcsmith.praxis.core.info.ControlInfo;
import net.neilcsmith.praxis.core.types.PMap;
import net.neilcsmith.praxis.impl.AbstractComponent;
import net.neilcsmith.praxis.impl.BooleanProperty;
import net.neilcsmith.praxis.impl.DefaultControlOutputPort;
import net.neilcsmith.praxis.impl.FloatProperty;
import net.neilcsmith.praxis.impl.FloatRangeProperty;
import net.neilcsmith.praxis.impl.TriggerControl;
import org.jaudiolibs.pipes.Buffer;
import org.jaudiolibs.pipes.Pipe;
import org.jaudiolibs.pipes.impl.SingleInOut;

/**
 *
 * @author Neil C Smith
 */
public class SamplePlayer extends AbstractComponent {

    private static Logger logger = Logger.getLogger(SamplePlayer.class.getName());
    private SamplePlayerUG player;
//    private BooleanProperty playing;
    private int tablesize;
    private ControlPort.Output readyPort;
    private ControlPort.Output errorPort;

    public SamplePlayer() {
        player = new SamplePlayerUG();

//        registerPort(Port.IN, new DefaultAudioInputPort(this, player));
        registerPort(Port.OUT, new DefaultAudioOutputPort(this, player));
        buildControls();
    }

    private void buildControls() {
        SampleTableLoader sample = new SampleTableLoader(new LoaderListener());
        registerControl("sample", sample);
        registerPort("sample", sample.createPort());
        FloatProperty position = FloatProperty.create(new PositionBinding(), 0, 1, 0,
                PMap.create(ControlInfo.KEY_TRANSIENT, true));
        registerControl("position", position);
        registerPort("position", position.createPort());
        FloatProperty in = FloatProperty.create(new InBinding(), 0, 1, 0);
        registerControl("start", in);
        registerPort("start", in.createPort());
        FloatProperty out = FloatProperty.create(new OutBinding(), 0, 1, 1);
        registerControl("end", out);
        registerPort("end", out.createPort());
        FloatRangeProperty range = FloatRangeProperty.create(new RangeBinding(),
                0, 1, 0, 1);
        registerControl("range", range);
        FloatProperty speed = FloatProperty.create(new SpeedBinding(), -4, 4, 1);
        registerControl("speed", speed);
        registerPort("speed", speed.createPort());
        registerControl("loop", BooleanProperty.create(this, new LoopingBinding(), false));
        TriggerControl play = TriggerControl.create(new PlayBinding());
        registerControl("play", play);
        registerPort("play", play.createPort());
        TriggerControl stop = TriggerControl.create(new StopBinding());
        registerControl("stop", stop);
        registerPort("stop", stop.createPort());
        BooleanProperty playing = BooleanProperty.create(this, new PlayingBinding(), false,
                PMap.create(ControlInfo.KEY_TRANSIENT, true));
        registerControl("playing", playing);
        readyPort = new DefaultControlOutputPort(this);
        registerPort("ready", readyPort);
        errorPort = new DefaultControlOutputPort(this);
        registerPort("error", errorPort);
    }

    private class PlayingBinding implements BooleanProperty.Binding {

        public void setBoundValue(long time, boolean value) {
            player.setPlaying(value);
        }

        public boolean getBoundValue() {
            return player.getPlaying();
        }
    }

    private class PlayBinding implements TriggerControl.Binding {

        public void trigger(long time) {
            player.setPlaying(true);
        }
    }

    private class StopBinding implements TriggerControl.Binding {

        public void trigger(long time) {
            player.setPlaying(false);
        }
    }

    private class SpeedBinding implements FloatProperty.Binding {

        public void setBoundValue(long time, double value) {
            player.setSpeed((float) value);
        }

        public double getBoundValue() {
            return player.getSpeed();
        }
    }

    private class PositionBinding implements FloatProperty.Binding {

        public void setBoundValue(long time, double value) {
            player.setPosition((float) (value * tablesize));
        }

        public double getBoundValue() {
            return player.getPosition() / tablesize;
        }
    }

    private class InBinding implements FloatProperty.Binding {

        public void setBoundValue(long time, double value) {
            int pos = (int) Math.round(value * tablesize);
            player.setIn(pos);
            if (logger.isLoggable(Level.FINEST)) {
                logger.finest("Input : " + value + " Player Input = " + player.getIn()
                        + " Player Output = " + player.getOut());
            }
        }

        public double getBoundValue() {
            return (double) player.getIn() / tablesize;
        }
    }

    private class OutBinding implements FloatProperty.Binding {

        public void setBoundValue(long time, double value) {
            int pos = (int) Math.round(value * tablesize);
            player.setOut(pos);
        }

        public double getBoundValue() {
            return (double) player.getOut() / tablesize;
        }
    }

    private class RangeBinding implements FloatRangeProperty.Binding {

        public void setBoundLowValue(long time, double low) {
            int pos = (int) Math.round(low * tablesize);
            player.setIn(pos);
        }

        public void setBoundHighValue(long time, double high) {
            int pos = (int) Math.round(high * tablesize);
            player.setOut(pos);
        }

        public double getBoundLowValue() {
            double ret = (double) player.getIn() / tablesize;
            if (logger.isLoggable(Level.FINEST)) {
                logger.finest("Returning Low = " + ret);
            }
            return ret;
        }

        public double getBoundHighValue() {
            double ret = (double) player.getOut() / tablesize;
            if (logger.isLoggable(Level.FINEST)) {
                logger.finest("Returning High = " + ret);
            }
            return ret;
        }
    }

    private class LoopingBinding implements BooleanProperty.Binding {

        public void setBoundValue(long time, boolean value) {
            player.setLooping(value);
        }

        public boolean getBoundValue() {
            return player.getLooping();
        }
    }

    private class LoaderListener implements SampleTableLoader.Listener {

        public void tableLoaded(SampleTableLoader loader, long time) {
            SampleTable table = loader.getTable();
            if (table == null) {
                tablesize = 1;
            } else {
                tablesize = table.getSize();
            }
            player.setSampleTable(table);
            readyPort.send(time);
        }

        public void tableError(SampleTableLoader loader, long time) {
            errorPort.send(time);
        }
    }

    private class SamplePlayerUG extends SingleInOut {

        private SampleTable table;
        private int tableSize;
        private int in;
        private int out;
        private boolean playing;
        private boolean looping;
        private boolean recording;
        private boolean wasRecording;
        private float position;
        private float speed = 1;

        public void setSampleTable(SampleTable table) {
            this.table = table;
            if (table == null) {
                tableSize = 0;
                in = 0;
                out = 0;
                position = 0;
                playing = recording = wasRecording = false;
            } else {
                tableSize = table.getSize();
                in = 0;
                out = tableSize;
                wasRecording = false;
            }
        }

        public SampleTable getSampleTable() {
            return table;
        }

        public void setRecording(boolean recording) {
            if (recording && table != null) {
                this.recording = true;
            } else {
                this.recording = false;
            }
        }

        public boolean getRecording() {
            return recording;
        }

        public void setPlaying(boolean playing) {
            if (this.playing != playing) {
                this.playing = playing;
                smoothIndex = 0;
            }
            if (speed < 0) {
                setPosition(out - 1);
            } else {
                setPosition(in);
            }
        }

        public boolean getPlaying() {
            return playing;
        }

        public void setLooping(boolean looping) {
            this.looping = looping;
        }

        public boolean getLooping() {
            return looping;
        }

        public void setIn(int value) {
            if (value < 0) {
                in = 0;
            } else if (value > out) {
                in = out;
            } else {
                in = value;
            }
        }

        public int getIn() {
            return in;
        }

        public void setOut(int value) {
            if (value < in) {
                out = in;
            } else if (value > tableSize) {
                out = tableSize;
            } else {
                out = value;
            }
        }

        public int getOut() {
            return out;
        }

        public void setPosition(float value) {
            if (value < in || value >= out) {
                position = in;
            } else {
                position = value;
            }

            if (playing) {
                smoothIndex = 0;
            }
        }

        public float getPosition() {
            return position;
        }

        public void setSpeed(float value) {
            speed = value;
        }

        public float getSpeed() {
            return speed;
        }

        @Override
        protected void process(Buffer buffer, boolean rendering) {
//        System.out.println("Process called");
            float[] data = buffer.getData();
            if (recording) {
                processRecording(data, 0, rendering);
            } else if (playing) {
                processPlaying(data, 0, rendering);
            } else {
                processStopped(data, 0, rendering);
            }

        }
        private int recordIdx;
        private int recordIn;
        private int recordOut;

        private void processRecording(float[] data, int offset, boolean rendering) {
            if (!wasRecording) {
                smoothIndex = 0;
            }
            int idx = (int) position;
            int looplength = out - in;
            while (idx >= out) {
                idx -= looplength;
            }
            while (idx < in) {
                idx += looplength;
            }
//        boolean smoothEnds = false;
//        boolean smoothIdx = false;
            if (idx != recordIdx) {
                // smooth old in and out if was recording
                if (wasRecording) {
                    fadeTo(recordIdx);
                    fadeFrom(recordIdx);
                }
//            recordIdx = idx;
//            smoothIdx = true;
            }
            for (int i = offset, k = data.length; i < k; i++) {
                float sample = data[i];
                table.set(0, idx, sample);
                idx++;
                if (idx == out) {
                    idx = in;
//                smoothEnds = true;
                    smoothEnds();
                    if (!looping) {
                        smoothIndex = 0;
                        processStopped(data, i, rendering);
                        return;
                    }
                }
                if (!playing) {
                    sample = 0;
                }
                if (smoothIndex >= 0) {
                    sample = smooth(sample);

                    smoothIndex++;
                    if (smoothIndex >= SMOOTH_SAMPLES) {
                        smoothIndex = -1;
                    }
                }
                data[i] = sample;
                previousSample = sample;


            }


            wasRecording = true;
            position = idx;
            recordIdx = idx;
            recordIn = in;
            recordOut = out;
        }

        private void smoothEnds() {
            // smooth to out
            // smooth from in
            fadeTo(out);
            fadeFrom(in);
            if (recordIn != in) {
                // smooth from out
                fadeTo(in);
                recordIn = in;
            }
            if (recordOut != out) {
                // smooth to in
                fadeFrom(out);
                recordOut = out;
            }
        }

        private void fadeTo(int idx) {
            if (idx < SMOOTH_SAMPLES) {
                for (int i = 0; i < idx; i++) {
                    table.set(0, i, 0);
                }
            } else {
                float mult = 1;
                float decrement = 1.0f / SMOOTH_SAMPLES;
                for (int i = idx - SMOOTH_SAMPLES; i < idx; i++) {
                    mult -= decrement;
                    table.set(0, i, mult * table.get(0, i));
                }
            }

        }

        private void fadeFrom(int idx) {
            int size = tableSize;
            if (idx + SMOOTH_SAMPLES > size) {
                for (int i = idx; i < size; i++) {
                    table.set(0, i, 0);
                }
            } else {
                float mult = 0;
                float increment = 1.0f / SMOOTH_SAMPLES;
                size = idx + SMOOTH_SAMPLES;
                for (int i = idx; i < size; i++) {
                    table.set(0, i, mult * table.get(0, i));
                    mult += increment;
                }
            }
        }

        private void processStopped(float[] data, int offset, boolean rendering) {
            if (wasRecording) {
                fadeFrom(recordIdx);
                fadeTo(recordIdx);
                smoothIndex = 0;
                wasRecording = false;
            }
            if (!rendering) {
                smoothIndex = -1;
                return;
            }
            if (smoothIndex >= 0) {
                for (int i = offset, k = data.length; i < k; i++) {
                    float sample = smooth(0);
                    data[i] = sample;
                    smoothIndex++;
                    if (sample == 0 || smoothIndex >= SMOOTH_SAMPLES) {
                        smoothIndex = -1;
                        offset = i;
                        break;
                    }
                }
            }
            for (int i = offset, k = data.length; i < k; i++) {
                data[i] = 0;
            }
        }
        private float previousSample = 0;
        private static final float MAX_DELTA = 0.2f;
        private static final int SMOOTH_SAMPLES = 8;

        private int smoothIndex = -1;

        private float smooth(float sample) {
            float delta = sample - previousSample;
            float max = MAX_DELTA;
            if (delta > max) {
                sample = previousSample + max;
            } else if (delta < -max) {
                sample = previousSample - max;
            }

            previousSample = sample;
            return sample;

        }

        private void processPlaying(float[] data, int offset, boolean rendering) {
            if (wasRecording) {
                // smooth tails
                fadeFrom(recordIdx);
                fadeTo(recordIdx);
                smoothIndex = 0;
                wasRecording = false;
            }
            int bSize = data.length;
            int loopLength = out - in;
            if (loopLength > 0 && table != null) {
                if (rendering) {
                    for (int i = 0; i < bSize; i++) {
                        if (position >= out) {
                            smoothIndex = 0;
                            if (looping) {
                                while (position >= out) {
                                    position -= loopLength;
                                }
                            } else {
                                processStopped(data, i, rendering);
                                position = in;
                                playing = false;
                                return;
                            }
                        } else if (position < in) {
                            smoothIndex = 0;
                            if (looping) {
                                while (position < in) {
                                    position += loopLength;
                                }
                            } else {
                                processStopped(data, i, rendering);
                                position = out - 1;
                                playing = false;
                                return;
                            }
                        }
                        float sample = getSample(position);
                        if (smoothIndex >= 0) {
                            sample = smooth(sample);
                            smoothIndex++;
                            if (smoothIndex >= SMOOTH_SAMPLES) {
                                smoothIndex = -1;
                            }
                        }
                        data[i] = sample;

                        previousSample = sample;
                        position += speed;

                    }
                } else {
                    position += (speed * bSize);
                    if (position > out) {
                        if (looping) {
                            while (position > out) {
                                position -= loopLength;
                            }
                        } else {
                            position = in;
                            playing = false;
                        }
                    } else if (position < in) {
                        if (looping) {
                            while (position < in) {
                                position += loopLength;
                            }
                        } else {
                            position = out - 1;
                            playing = false;
                        }
                    }

                }
            } else {
                if (previousSample != 0) {
                    smoothIndex = 0;
                }
                processStopped(data, offset, rendering);
            }


        }

        private float getSample(float pos) {
            int iPos = (int) pos;

            if (iPos == pos) {
                return table.get(0, iPos);
            }

            float frac, a, b, c, d, cminusb;

            if (iPos < (1)) {
                iPos = 1;
                frac = 0;
            } else if (iPos > (tableSize - 3)) {
                iPos = tableSize - 3;
                frac = 1;
            } else {
                frac = pos - iPos;
            }
            a = table.get(0, iPos - 1);
            b = table.get(0, iPos);
            c = table.get(0, iPos + 1);
            d = table.get(0, iPos + 2);

            cminusb = c - b;
            return (b + frac * (cminusb - 0.5f * (frac - 1)
                    * ((a - d + 3.0f * cminusb) * frac + (b - a - cminusb))));

        }

        @Override
        public boolean isRenderRequired(Pipe source, long time) {
            return recording;
        }
    }
}
