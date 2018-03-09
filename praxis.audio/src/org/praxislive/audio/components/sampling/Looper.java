/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2012 Neil C Smith.
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
package org.praxislive.audio.components.sampling;

import java.util.logging.Logger;
import org.praxislive.audio.impl.AudioInputPortEx;
import org.praxislive.audio.impl.AudioOutputPortEx;
import org.praxislive.audio.io.SampleTable;
import org.praxislive.core.Port;
import org.praxislive.core.ControlInfo;
import org.praxislive.core.types.PMap;
import org.praxislive.impl.AbstractComponent;
import org.praxislive.impl.BooleanProperty;
import org.praxislive.impl.NumberProperty;
import org.praxislive.impl.FloatRangeProperty;
import org.praxislive.impl.TriggerControl;
import org.jaudiolibs.pipes.Buffer;
import org.jaudiolibs.pipes.Pipe;
import org.jaudiolibs.pipes.impl.SingleInOut;

/**
 *
 * @author Neil C Smith
 */
public class Looper extends AbstractComponent {

    private static Logger logger = Logger.getLogger(Looper.class.getName());
    private LooperUG looper;

    public Looper() {
        looper = new LooperUG();
        looper.setLoopSize(1);
        registerPort(PortEx.IN, new AudioInputPortEx(looper));
        registerPort(PortEx.OUT, new AudioOutputPortEx(looper));
        buildControls();
    }

    private void buildControls() {
        NumberProperty loopSize = NumberProperty.create(new LoopSizeBinding(), 0, 60, 1);
        registerControl("loop-size", loopSize);
        NumberProperty position = NumberProperty.create(new PositionBinding(), 0, 1, 0,
                PMap.create(ControlInfo.KEY_TRANSIENT, true));
        registerControl("position", position);
        registerPort("position", position.createPort());
        NumberProperty in = NumberProperty.create(new InBinding(), 0, 1, 0);
        registerControl("start", in);
        registerPort("start", in.createPort());
        NumberProperty out = NumberProperty.create(new OutBinding(), 0, 1, 1);
        registerControl("end", out);
        registerPort("end", out.createPort());
        FloatRangeProperty range = FloatRangeProperty.create(new RangeBinding(),
                0, 1, 0, 1);
        registerControl("range", range);
        NumberProperty speed = NumberProperty.create(new SpeedBinding(), -4, 4, 1);
        registerControl("speed", speed);
        registerPort("speed", speed.createPort());
//        registerControl("loop", BooleanProperty.create(this, new LoopingBinding(), false));
        TriggerControl play = TriggerControl.create(new PlayBinding());
        registerControl("play", play);
        registerPort("play", play.createPort());
        TriggerControl stop = TriggerControl.create(new StopBinding());
        registerControl("stop", stop);
        registerPort("stop", stop.createPort());
        TriggerControl record = TriggerControl.create(new RecordBinding());
        registerControl("record", record);
        registerPort("record", record.createPort());
        BooleanProperty playing = BooleanProperty.create(this, new PlayingBinding(), false,
                PMap.create(ControlInfo.KEY_TRANSIENT, true));
        registerControl("playing", playing);
        BooleanProperty recording = BooleanProperty.create(this, new RecordingBinding(), false,
                PMap.create(ControlInfo.KEY_TRANSIENT, true));
        registerControl("recording", recording);
    }

    private class PlayingBinding implements BooleanProperty.Binding {

        public void setBoundValue(long time, boolean value) {
            looper.setPlaying(value);
        }

        public boolean getBoundValue() {
            return looper.getPlaying();
        }
    }

    private class PlayBinding implements TriggerControl.Binding {

        public void trigger(long time) {
            looper.setPlaying(true);
            looper.setRecording(false);
        }
    }

    private class StopBinding implements TriggerControl.Binding {

        public void trigger(long time) {
            looper.setPlaying(false);
            looper.setRecording(false);
        }
    }

    private class RecordBinding implements TriggerControl.Binding {

        public void trigger(long time) {
            looper.setRecording(true);
        }
    }

    private class RecordingBinding implements BooleanProperty.Binding {

        public void setBoundValue(long time, boolean value) {
            looper.setRecording(value);
        }

        public boolean getBoundValue() {
            return looper.getRecording();
        }
    }

    private class SpeedBinding implements NumberProperty.Binding {

        public void setBoundValue(long time, double value) {
            looper.setSpeed((float) value);
        }

        public double getBoundValue() {
            return looper.getSpeed();
        }
    }

    private class PositionBinding implements NumberProperty.Binding {

        public void setBoundValue(long time, double value) {
            looper.setPosition((float) value);
        }

        public double getBoundValue() {
            return looper.getPosition();
        }
    }

    private class InBinding implements NumberProperty.Binding {

        public void setBoundValue(long time, double value) {
            looper.setLoopStart((float) value);
        }

        public double getBoundValue() {
            return (double) looper.getLoopStart();
        }
    }

    private class OutBinding implements NumberProperty.Binding {

        public void setBoundValue(long time, double value) {
            looper.setLoopEnd((float) value);
        }

        public double getBoundValue() {
            return looper.getLoopEnd();
        }
    }

    private class RangeBinding implements FloatRangeProperty.Binding {

        public void setBoundLowValue(long time, double low) {
            looper.setLoopStart((float) low);
        }

        public void setBoundHighValue(long time, double high) {
            looper.setLoopEnd((float) high);
        }

        public double getBoundLowValue() {
            return looper.getLoopStart();
        }

        public double getBoundHighValue() {
            return looper.getLoopEnd();
        }
    }

//    private class LoopingBinding implements BooleanProperty.Binding {
//
//        public void setBoundValue(long time, boolean value) {
//            looper.setLooping(value);
//        }
//
//        public boolean getBoundValue() {
//            return looper.getLooping();
//        }
//        
//    }
    private class LoopSizeBinding implements NumberProperty.Binding {

        public void setBoundValue(long time, double value) {
            looper.setLoopSize((float) value);
        }

        public double getBoundValue() {
            return looper.getLoopSize();
        }
    }

    private class LooperUG extends SingleInOut {

        private SampleTable table;
        private float start = 0;
        private float end = 1;
        private int tableSize;
        private int in;
        private int out;
        private boolean playing;
        private boolean looping = true;
        private boolean recording;
        private boolean wasRecording;
        private float ptr;
        private float speed = 1;
        private float loopSize;

        public void setLoopSize(float loopSize) {
            if (loopSize < 0 || loopSize > 60) {
                throw new IllegalArgumentException("Loop size out of range");
            }
            this.loopSize = loopSize;
            table = null;
        }

        public float getLoopSize() {
            return loopSize;
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

        public void setLoopStart(float start) {
            if (start < 0) {
                start = 0;
            } else if (start > end) {
                start = end;
            }
            this.start = start;
            if (table != null) {
                in = (int) (start * table.getSize());
            }
        }

        public float getLoopStart() {
            return start;
        }

        public void setLoopEnd(float end) {
            if (end < start) {
                end = start;
            } else if (end > 1) {
                end = 1;
            }
            this.end = end;
            if (table != null) {
                out = (int) (end * table.getSize());
            }
        }

        public float getLoopEnd() {
            return end;
        }

        public boolean getPlaying() {
            return playing;
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
            if (table == null) {
                return;
            }
            value = value * table.getSize();
            if (value < in || value >= out) {
                ptr = in;
            } else {
                ptr = value;
            }
            if (playing) {
                smoothIndex = 0;
            }
        }

        public float getPosition() {
            if (table != null) {
                return ptr / table.getSize();
            } else {
                return 0;
            }
        }

        public void setSpeed(float value) {
            speed = value;
        }

        public float getSpeed() {
            return speed;
        }

        public int getTableSize() {
            return tableSize;
        }

        @Override
        protected void process(Buffer buffer, boolean rendering) {
//        System.out.println("Process called");
            if (table == null || table.getSampleRate() != buffer.getSampleRate()) {
                initTable(buffer.getSampleRate());
            }
            float[] data = buffer.getData();
            if (recording) {
                processRecording(data, 0, rendering);
            } else if (playing) {
                processPlaying(data, 0, rendering);
            } else {
                processStopped(data, 0, rendering);
            }

        }

        private void initTable(float srate) {
            tableSize = Math.round(srate * loopSize);
            if (tableSize < 1) {
                tableSize = 1;
            }
            table = SampleTable.wrap(srate, new float[tableSize]);
            in = (int) (start * tableSize);
            out = (int) (end * tableSize);
            ptr = 0;
            wasRecording = false;

        }
        private int recordIdx;
        private int recordIn;
        private int recordOut;

        private void processRecording(float[] data, int offset, boolean rendering) {
            if (!wasRecording) {
                smoothIndex = 0;
            }
            int idx = (int) ptr;
            int looplength = out - in;
            if (looplength > 0) {
                while (idx >= out) {
                    idx -= looplength;
                }
                while (idx < in) {
                    idx += looplength;
                }
            } else {
                idx = in;
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

//        if (smoothEnds) {
//            // smooth to out
//            // smooth from in
//            fadeTo(out);
//            fadeFrom(in);
//            if (recordIn != in) {
//                // smooth from out
//                fadeTo(in);
//                recordIn = in;
//            }
//            if (recordOut != out) {
//                // smooth to in
//                fadeFrom(out);
//                recordOut = out;
//            }
//        }
//        if (smoothIdx) {
//            // smooth to and from record idx if was recording
//            if (wasRecording) {
//                fadeTo(recordIdx);
//                fadeFrom(recordIdx);
//            }
//            recordIdx = idx;
//        }
            wasRecording = true;
            ptr = idx;
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
//    private static final float[] DELTA = {0.05f, 0.05f, 0.1f, 0.1f, 0.15f, 0.15f, 0.2f, 0.2f, 0.25f, 0.25f,
//    0.3f, 0.35f, 0.4f, 0.5f, 0.6f, 0.7f, 0.8f, 0.9f, 1f};
//    private static final int MAX_SMOOTH_IDX = DELTA.length;
        private int smoothIndex = -1;

//    public final void setCutOff(float cutOff)
//    {
//        this.cutOff = cutOff;
//    }
        private float smooth(float sample) {
//
            float delta = sample - previousSample;
//        float max = DELTA[smoothIndex];
            float max = MAX_DELTA;
            if (delta > max) {
                sample = previousSample + max;
            } else if (delta < -max) {
                sample = previousSample - max;
            }

            previousSample = sample;
            return sample;

//        float mult = (float) smoothIndex / SMOOTH_SAMPLES;
//        sample = (mult * sample) + ((1 - mult) * previousSample);
//        previousSample = sample;
//        return sample;
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
                        if (ptr >= out) {
                            smoothIndex = 0;
                            if (looping) {
                                while (ptr >= out) {
                                    ptr -= loopLength;
                                }
                            } else {
                                processStopped(data, i, rendering);
                                ptr = in;
                                playing = false;
                                return;
                            }
                        } else if (ptr < in) {
                            smoothIndex = 0;
                            if (looping) {
                                while (ptr < in) {
                                    ptr += loopLength;
                                }
                            } else {
                                processStopped(data, i, rendering);
                                ptr = out - 1;
                                playing = false;
                                return;
                            }
                        }
                        float sample = getSample(ptr);
                        if (smoothIndex >= 0) {
                            sample = smooth(sample);
                            smoothIndex++;
                            if (smoothIndex >= SMOOTH_SAMPLES) {
                                smoothIndex = -1;
                            }
                        }
                        data[i] = sample;

                        previousSample = sample;
                        ptr += speed;

                    }
                } else {
                    ptr += (speed * bSize);
                    if (ptr > out) {
                        if (looping) {
                            while (ptr > out) {
                                ptr -= loopLength;
                            }
                        } else {
                            ptr = in;
                            playing = false;
                        }
                    } else if (ptr < in) {
                        if (looping) {
                            while (ptr < in) {
                                ptr += loopLength;
                            }
                        } else {
                            ptr = out - 1;
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
//        if (table.getSize() < 5) {
//            return 0;
//        }
            int iPos = (int) pos;

            if (iPos == pos) {
                return table.get(0, iPos);
            }

            float frac, a, b, c, d, cminusb;
//
//        if (iPos < (in + 1)) {
////            a = table.get(0, out - 1);
//            b = table.get(0, iPos);
////            b = 0;
//            c = table.get(0, iPos + 1);
//            d = table.get(0, iPos + 2);
//        } else if (iPos > (out - 3)) {
//            a = table.get(0, iPos - 1);
//            b = table.get(0, iPos);
//            c = 0;
//            d = 0;
//        } else {
//            a = table.get(0, iPos - 1);
//            b = table.get(0, iPos);
//            c = table.get(0, iPos + 1);
//            d = table.get(0, iPos + 2);
//        }


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

//        if (iPos < 1) {
//            a = 0;
//            b = table.get(0, iPos);
////            b = 0;
//            c = table.get(0, iPos + 1);
//            d = table.get(0, iPos + 2);
//        } else if ((iPos + 3) > tableSize) {
//            a = table.get(0, iPos - 1);
//            b = table.get(0, iPos);
//            c = b / 2;
//            d = 0;
//        } else {
//            a = table.get(0, iPos - 1);
//            b = table.get(0, iPos);
//            c = table.get(0, iPos + 1);
//            d = table.get(0, iPos + 2);
//        }

            cminusb = c - b;
//        frac = pos - iPos;
            return (b + frac * (cminusb - 0.5f * (frac - 1)
                    * ((a - d + 3.0f * cminusb) * frac + (b - a - cminusb))));

        }

        @Override
        public boolean isRenderRequired(Pipe source, long time) {
            return recording;
        }
    }
}
