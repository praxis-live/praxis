/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2018 Neil C Smith.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License version 3 only, as
 * published by the Free Software Foundation.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser General Public License
 * version 3 for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License version 3
 * along with this work; if not, see http://www.gnu.org/licenses/
 *
 *
 * Please visit https://www.praxislive.org if you need additional information or
 * have any questions.
 *
 */
package org.praxislive.audio.code.userapi;

import org.praxislive.audio.code.Resettable;
import org.jaudiolibs.pipes.Buffer;
import org.jaudiolibs.pipes.impl.MultiInOut;

/**
 *
 * @author Neil C Smith (http://neilcsmith.net)
 */
public final class Looper extends MultiInOut implements Resettable {

    private AudioTable table;
    private double in;
    private double out;
    private double speed;
    private boolean playing;
    private boolean recording;
    private boolean looping;

    private double cursor;
    private double realSpeed;
    private int iIn, iOut;

    private Channel[] channels;

    public Looper() {
        super(16, 16);
        channels = new Channel[]{new Channel()};
        reset();
    }

    public Looper table(AudioTable table) {
        if (table != this.table) {
            this.table = table;
            triggerSmoothing();
        }
        return this;
    }

    public AudioTable table() {
        return table;
    }

    public Looper in(double in) {
        if (in < 0) {
            in = 0;
        } else if (in > 1) {
            in = 1;
        }
        this.in = in;
        return this;
    }

    public double in() {
        return in;
    }

    public Looper out(double out) {
        if (out < 0) {
            out = 0;
        } else if (out > 1) {
            out = 1;
        }
        this.out = out;
        return this;
    }

    public double out() {
        return out;
    }

    public Looper position(double position) {
        if (position < 0) {
            position = 0;
        } else if (position > 1) {
            position = 1;
        }
        if (table != null) {
            cursor = position * table.size();
            triggerSmoothing();
        }
        return this;
    }

    public double position() {
        return table == null ? 0 : cursor / table.size();
    }

    public Looper speed(double speed) {
        this.speed = speed;
        return this;
    }

    public double speed() {
        return speed;
    }

    public Looper playing(boolean playing) {
        if (this.playing != playing) {
            triggerSmoothing();
        }
        this.playing = playing;
        this.recording = false;
        return this;
    }

    public boolean playing() {
        return playing;
    }

    public Looper recording(boolean recording) {
        if (this.recording != recording) {
            triggerSmoothing();
        }
        this.recording = recording;
        return this;
    }

    public boolean recording() {
        return recording;
    }

    public Looper looping(boolean looping) {
        this.looping = looping;
        return this;
    }

    public boolean looping() {
        return looping;
    }

    public Looper play() {
        if (speed < 0) {
            position(1);
        } else {
            position(0);
        }
        playing = true;
        return this;
    }

    public Looper stop() {
        playing(false);
        return this;
    }

    @Override
    public void reset() {
        table = null;
        in = 0;
        out = 1;
        speed = 1;
        looping = false;
        recording = false;
    }

    @Override
    protected void process(Buffer[] buffers, boolean rendering) {
        if (buffers.length != channels.length) {
            configureChannels(buffers.length);
        }
        if (table != null) {
            iIn = (int) (in * table.size());
            iOut = (int) (out * table.size());
        } else {
            iIn = iOut = 0;
        }
        int loopLength = iOut - iIn;
        if (playing && loopLength > 0) {
            if (recording) {
                realSpeed = 1;
                for (int i = 0; i < buffers.length; i++) {
                    channels[i].processRecording(buffers[i], i % table.channels());
                }
            } else {
                realSpeed = speed;
                for (int i = 0; i < buffers.length; i++) {
                    channels[i].processPlaying(buffers[i], i % table.channels(), rendering);
                }
            }
            cursor += (realSpeed * buffers[0].getSize());
            if (cursor > iOut) {
                if (looping) {
                    while (cursor > iOut) {
                        cursor -= loopLength;
                    }
                } else {
                    cursor = iIn;
                    playing = false;
                }
            } else if (cursor < iIn) {
                if (looping) {
                    while (cursor < iIn) {
                        cursor += loopLength;
                    }
                } else {
                    cursor = iOut - 1;
                    playing = false;
                }
            }
        } else {
            for (int i = 0; i < buffers.length; i++) {
                channels[i].processStopped(buffers[i], 0, rendering);
            }
        }

    }

    private void configureChannels(int count) {
        Channel[] old = channels;
        channels = new Channel[count];
        int copy = Math.min(old.length, channels.length);
        if (copy > 0) {
            System.arraycopy(old, 0, channels, 0, copy);
        }
        for (int i = copy; i < channels.length; i++) {
            channels[i] = new Channel();
        }
    }

    private void triggerSmoothing() {
        for (Channel c : channels) {
            c.smoothIndex = SMOOTH_AMOUNT;
        }
    }

    private final static int SMOOTH_AMOUNT = 256;

    private class Channel {

        private boolean wasRecording = false;
        private double previousSample = 0;
        private int smoothIndex = 0;

        private void processPlaying(Buffer buffer, int channel, boolean rendering) {
            
            if (wasRecording) {
                fadeFrom(channel, (int) cursor);
                fadeTo(channel, (int) (cursor + 0.5));
                smoothIndex = 0;
                wasRecording = false;
            }
            
            if (!rendering) {
                smoothIndex = 0;
                return;
            }

            int bSize = buffer.getSize();
            float[] data = buffer.getData();
            double p = cursor;
            int loopLength = iOut - iIn;
            if (loopLength > 0 && table != null) {
                for (int i = 0; i < bSize; i++) {
                    if (p >= iOut) {
                        smoothIndex = SMOOTH_AMOUNT;
                        if (looping) {
                            while (p >= iOut) {
                                p -= loopLength;
                            }
                        } else {
                            processStopped(buffer, i, rendering);
                            return;
                        }
                    } else if (p < iIn) {
                        smoothIndex = SMOOTH_AMOUNT;
                        if (looping) {
                            while (p < iIn) {
                                p += loopLength;
                            }
                        } else {
                            processStopped(buffer, i, rendering);
                            return;
                        }
                    }
                    double sample = table.get(channel, p);
                    if (smoothIndex > 0) {
                        sample = smooth(sample);
                        smoothIndex--;
                    }
                    data[i] = (float) sample;
                    previousSample = sample;
                    p += realSpeed;

                }
            } else {
                if (previousSample != 0) {
                    smoothIndex = SMOOTH_AMOUNT;
                }
                processStopped(buffer, 0, rendering);
            }
        }

        private void processRecording(Buffer buffer, int channel) {
            if (!wasRecording) {
                smoothIndex = 0;
            }
            
            int bSize = buffer.getSize();
            float[] data = buffer.getData();
            double p = cursor;
            int loopLength = iOut - iIn;
            if (loopLength > 0 && table != null) {
                for (int i = 0; i < bSize; i++) {
                    if (p >= iOut) {
                        smoothEnds(channel);
                        smoothIndex = SMOOTH_AMOUNT;
                        if (looping) {
                            while (p >= iOut) {
                                p -= loopLength;
                            }
                        } else {
                            processStopped(buffer, i, true);
                            return;
                        }
                    } else if (p < iIn) {
                        smoothEnds(channel);
                        smoothIndex = SMOOTH_AMOUNT;
                        if (looping) {
                            while (p < iIn) {
                                p += loopLength;
                            }
                        } else {
                            processStopped(buffer, i, true);
                            return;
                        }
                    }
                    double sample = data[i];
                    table.set(channel, (int) p, sample);
                    if (smoothIndex > 0) {
                        data[i] = (float) smooth(sample);
                        smoothIndex--;
                    }
                    previousSample = sample;
                    p += realSpeed; // 1

                }
            } else {
                if (previousSample != 0) {
                    smoothIndex = SMOOTH_AMOUNT;
                }
                processStopped(buffer, 0, true);
            }
            
        }
        
        
        private void processStopped(Buffer buffer, int offset, boolean rendering) {
            if (!rendering) {
                smoothIndex = 0;
                return;
            }

            int bSize = buffer.getSize();
            float[] data = buffer.getData();
            if (smoothIndex > 0) {
                for (int i = offset; i < bSize; i++) {
                    double sample = smooth(0);
                    previousSample = sample;
                    data[i] = (float) sample;
                    smoothIndex--;
                    if (sample == 0 || smoothIndex == 0) {
                        smoothIndex = 0;
                        offset = i;
                        break;
                    }
                }
            }
            for (int i = offset; i < bSize; i++) {
                data[i] = 0;
            }
        }

        private double smooth(double sample) {
            sample = sample - ((sample - previousSample)
                    * (double) smoothIndex / SMOOTH_AMOUNT);
            return sample;
        }

        private void smoothEnds(int channel) {
            fadeFrom(channel, iIn);
            fadeTo(channel, iOut);
        }
        
        private void fadeTo(int channel, int idx) {
            if (idx < SMOOTH_AMOUNT) {
                for (int i = 0; i < idx; i++) {
                    table.set(0, i, 0);
                }
            } else {
                double mult = 1;
                double decrement = 1.0 / SMOOTH_AMOUNT;
                for (int i = idx - SMOOTH_AMOUNT; i < idx; i++) {
                    mult -= decrement;
                    table.set(channel, i, mult * table.get(channel, i));
                }
            }

        }

        private void fadeFrom(int channel, int idx) {
            int size = table.size();
            if (idx + SMOOTH_AMOUNT > size) {
                for (int i = idx; i < size; i++) {
                    table.set(0, i, 0);
                }
            } else {
                float mult = 0;
                float increment = 1.0f / SMOOTH_AMOUNT;
                size = idx + SMOOTH_AMOUNT;
                for (int i = idx; i < size; i++) {
                    table.set(channel, i, mult * table.get(channel, i));
                    mult += increment;
                }
            }
        }
        
        
    }

}
