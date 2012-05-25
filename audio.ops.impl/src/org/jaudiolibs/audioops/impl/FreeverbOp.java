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
 *  
 * 
 * Derived from code in RasmsuDSP
 * Copyright (c) 2006, Karl Helgason
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 * 
 *    1. Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *    2. Redistributions in binary form must reproduce the above
 *       copyright notice, this list of conditions and the following
 *       disclaimer in the documentation and/or other materials
 *       provided with the distribution.
 *    3. The name of the author may not be used to endorse or promote
 *       products derived from this software without specific prior
 *       written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS OR
 * IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE
 * GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER
 * IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR
 * OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN
 * IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package org.jaudiolibs.audioops.impl;

import java.util.Arrays;
import org.jaudiolibs.audioops.AudioOp;

public final class FreeverbOp implements AudioOp {

    private static final float muted = 0;
    private static final float fixedgain = 0.015f * 2;
    private static final float scalewet = 3;
    private static final float scaledry = 2;
    private static final float scaledamp = 0.4f;
    private static final float scaleroom = 0.28f;
    private static final float offsetroom = 0.7f;
    private static final float initialroom = 0.5f;
    private static final float initialdamp = 0.5f;
    private static final float initialwet = 1 / scalewet;
    private static final float initialdry = 1;//0;
    private static final float initialwidth = 1f;
//    private static final float initialmode = 0.0f;
    private static final float freezemode = 0.5f;
    
    private float gain;
    private float roomsize, roomsize1;
    private float damp, damp1;
    private float wet, wet1, wet2;
    private float dry;
    private float width;
    private float mode;
    private boolean dirty;
    // Comb filters
    private int numcombs;
    private Comb[] combL;
    private Comb[] combR;
    // Allpass filters
    private int numallpasses;
    private Allpass[] allpassL;
    private Allpass[] allpassR;
    //scratch buffers
    private float[] inScratch = null;
    private float[] outScratchL = null;
    private float[] outScratchR = null;

    public FreeverbOp() {
        setWet(initialwet);
        setRoomSize(initialroom);
        setDry(initialdry);
        setDamp(initialdamp);
        setWidth(initialwidth);
    }

    public void initialize(float samplerate, int maxBufferSize) {
        float freqscale = samplerate / 44100.0f;
        int stereospread = 23;

        /*
         * Init Comb filters
         */

        int combtuningL1 = (int) (freqscale * (1116));
        int combtuningR1 = (int) (freqscale * (1116 + stereospread));
        int combtuningL2 = (int) (freqscale * (1188));
        int combtuningR2 = (int) (freqscale * (1188 + stereospread));
        int combtuningL3 = (int) (freqscale * (1277));
        int combtuningR3 = (int) (freqscale * (1277 + stereospread));
        int combtuningL4 = (int) (freqscale * (1356));
        int combtuningR4 = (int) (freqscale * (1356 + stereospread));
        int combtuningL5 = (int) (freqscale * (1422));
        int combtuningR5 = (int) (freqscale * (1422 + stereospread));
        int combtuningL6 = (int) (freqscale * (1491));
        int combtuningR6 = (int) (freqscale * (1491 + stereospread));
        int combtuningL7 = (int) (freqscale * (1557));
        int combtuningR7 = (int) (freqscale * (1557 + stereospread));
        int combtuningL8 = (int) (freqscale * (1617));
        int combtuningR8 = (int) (freqscale * (1617 + stereospread));

        numcombs = 8;
        combL = new Comb[numcombs];
        combR = new Comb[numcombs];
        combL[0] = new Comb(combtuningL1);
        combR[0] = new Comb(combtuningR1);
        combL[1] = new Comb(combtuningL2);
        combR[1] = new Comb(combtuningR2);
        combL[2] = new Comb(combtuningL3);
        combR[2] = new Comb(combtuningR3);
        combL[3] = new Comb(combtuningL4);
        combR[3] = new Comb(combtuningR4);
        combL[4] = new Comb(combtuningL5);
        combR[4] = new Comb(combtuningR5);
        combL[5] = new Comb(combtuningL6);
        combR[5] = new Comb(combtuningR6);
        combL[6] = new Comb(combtuningL7);
        combR[6] = new Comb(combtuningR7);
        combL[7] = new Comb(combtuningL8);
        combR[7] = new Comb(combtuningR8);

        /*
         * Init Allpass filters
         */

        int allpasstuningL1 = (int) (freqscale * (556));
        int allpasstuningR1 = (int) (freqscale * (556 + stereospread));
        int allpasstuningL2 = (int) (freqscale * (441));
        int allpasstuningR2 = (int) (freqscale * (441 + stereospread));
        int allpasstuningL3 = (int) (freqscale * (341));
        int allpasstuningR3 = (int) (freqscale * (341 + stereospread));
        int allpasstuningL4 = (int) (freqscale * (225));
        int allpasstuningR4 = (int) (freqscale * (225 + stereospread));

        numallpasses = 4;
        allpassL = new Allpass[numallpasses];
        allpassR = new Allpass[numallpasses];
        allpassL[0] = new Allpass(allpasstuningL1);
        allpassR[0] = new Allpass(allpasstuningR1);
        allpassL[1] = new Allpass(allpasstuningL2);
        allpassR[1] = new Allpass(allpasstuningR2);
        allpassL[2] = new Allpass(allpasstuningL3);
        allpassR[2] = new Allpass(allpasstuningR3);
        allpassL[3] = new Allpass(allpasstuningL4);
        allpassR[3] = new Allpass(allpasstuningR4);

        for (int i = 0; i < numallpasses; i++) {
            allpassL[i].setfeedback(0.5f);
            allpassR[i].setfeedback(0.5f);
        }

        /*
         * Init scratch buffers
         */
        inScratch = new float[maxBufferSize];
        outScratchL = new float[maxBufferSize];
        outScratchR = new float[maxBufferSize];


        /*
         * Init other settings
         */

        

        /*
         * Prepare all buffers
         */
        dirty = true;
//        mute();
    }

    public void setRoomSize(float value) {
        roomsize = (value * scaleroom) + offsetroom;
        dirty = true;
    }

    public float getRoomSize() {
        return (roomsize - offsetroom) / scaleroom;
    }

    public void setDamp(float value) {
        damp = value * scaledamp;
        dirty = true;
    }

    public float getDamp() {
        return damp / scaledamp;
    }

    public void setWet(float value) {
        wet = value * scalewet;
        dirty = true;
    }

    public float getWet() {
        return wet / scalewet;
    }

    public void setDry(float value) {
        dry = value * scaledry;
        dirty = true;
    }

    public float getDry() {
        return dry / scaledry;
    }

    public void setWidth(float value) {
        width = value;
        dirty = true;
    }

    public float getWidth() {
        return width;
    }

    public void processReplace(int buffersize, float[][] outputs, float[][] inputs) {
        processImpl(buffersize, outputs, inputs, true);
    }

    public void processAdd(int buffersize, float[][] outputs, float[][] inputs) {
        processImpl(buffersize, outputs, inputs, false);
    }

    private void processImpl(int buffersize, float[][] outputs, float[][] inputs, boolean replace) {

        if (dirty) {
            update();
            dirty = false;
        }
        
        float[] inL, inR, outL, outR;

        int channels = outputs.length;
        if (channels == 1) {
            inL = inputs[0];
            inR = null;
            outL = outputs[0];
            outR = null;
        } else if (channels == 2) {
            inL = inputs[0];
            inR = inputs[1];
            outL = outputs[0];
            outR = outputs[1];
        } else {
            throw new IllegalArgumentException("FreeverbOp can only work with 1 or 2 channels");
        }

        float again = gain / channels;
        for (int i = 0; i < buffersize; i++) {
            inScratch[i] = inL[i] * again;
        }
        if (channels == 2) {
            for (int i = 0; i < buffersize; i++) {
                inScratch[i] += inR[i] * again;
            }
        }

        Arrays.fill(outScratchL, 0);
        Arrays.fill(outScratchR, 0);

        for (int i = 0; i < numcombs; i++) {
            combL[i].processMix(inScratch, outScratchL, buffersize);
            combR[i].processMix(inScratch, outScratchR, buffersize);
        }

        for (int i = 0; i < numallpasses; i++) {
            allpassL[i].processReplace(outScratchL, outScratchL, buffersize);
            allpassR[i].processReplace(outScratchR, outScratchR, buffersize);
        }

        if (channels == 2) {
            if (dry == 0) {
                for (int i = 0; i < buffersize; i++) {
                    outL[i] = outScratchL[i] * wet1 + outScratchR[i] * wet2;
                    outR[i] = outScratchR[i] * wet1 + outScratchL[i] * wet2;
                }
            } else {
                for (int i = 0; i < buffersize; i++) {
                    outL[i] = outScratchL[i] * wet1 + outScratchR[i] * wet2 + inL[i] * dry;
                    outR[i] = outScratchR[i] * wet1 + outScratchL[i] * wet2 + inR[i] * dry;
                }
            }
        } else if (channels == 1) {
            if (dry == 0) {
                for (int i = 0; i < buffersize; i++) {
                    outL[i] = outScratchL[i] * wet1 + outScratchR[i] * wet2;
                }
            } else {
                for (int i = 0; i < buffersize; i++) {
                    outL[i] = outScratchL[i] * wet1 + outScratchR[i] * wet2 + inL[i] * dry;
                }
            }
        }


    }

    public void reset(int skipped) {
        mute();
    }

    public boolean isInputRequired(boolean outputRequired) {
        return outputRequired;
    }

    private void mute() {
//        if (getmode() >= freezemode) {
//            return;
//        }

        for (int i = 0; i < numcombs; i++) {
            combL[i].mute();
            combR[i].mute();
        }
        for (int i = 0; i < numallpasses; i++) {
            allpassL[i].mute();
            allpassR[i].mute();
        }
    }

    private void update() {

        int i;

        wet1 = wet * (width / 2 + 0.5f);
        wet2 = wet * ((1 - width) / 2);

        if (mode >= freezemode) {
            roomsize1 = 1;
            damp1 = 0;
            gain = muted;
        } else {
            roomsize1 = roomsize;
            damp1 = damp;
            gain = fixedgain;
        }

        for (i = 0; i < numcombs; i++) {
            combL[i].setfeedback(roomsize1);
            combR[i].setfeedback(roomsize1);
        }

        for (i = 0; i < numcombs; i++) {
            combL[i].setdamp(damp1);
            combR[i].setdamp(damp1);
        }
    }

//    public void setMode(float value) {
//        mode = value;
//        update();
//    }
//
//    public float getMode() {
//        if (mode >= freezemode) {
//            return 1;
//        } else {
//            return 0;
//        }
//    }
    private class Comb {

        float feedback;
        float filterstore = 0;
        float damp1;
        float damp2;
        float[] buffer;
        int bufsize;
        int bufidx = 0;

        public Comb(int size) {
            buffer = new float[size];
            bufsize = size;
        }

        public void processMix(float inputs[], float outputs[], int buffersize) {
            for (int i = 0; i < buffersize; i++) {
                float output = buffer[bufidx];

                //undenormalise(output);
                if (output > 0.0) {
                    if (output < 1.0E-10) {
                        output = 0;
                    }
                }
                if (output < 0.0) {
                    if (output > -1.0E-10) {
                        output = 0;
                    }
                }

                filterstore = (output * damp2) + (filterstore * damp1);
                //undenormalise(filterstore);
                if (filterstore > 0.0) {
                    if (filterstore < 1.0E-10) {
                        filterstore = 0;
                    }
                }
                if (filterstore < 0.0) {
                    if (filterstore > -1.0E-10) {
                        filterstore = 0;
                    }
                }

                buffer[bufidx] = inputs[i] + (filterstore * feedback);

                if (++bufidx >= bufsize) {
                    bufidx = 0;
                }

                outputs[i] += output;

            }
        }

//        public void processReplace(float inputs[], float outputs[], int from, int to, int channels) {
//
//            for (int c = 0; c < channels; c++) {
//                for (int i = from + c; i < to; i += channels) {
//
//
//
//                    float output = buffer[bufidx];
//
//                    //undenormalise(output);
//                    if (output > 0.0) {
//                        if (output < 1.0E-10) {
//                            output = 0;
//                        }
//                    }
//                    if (output < 0.0) {
//                        if (output > -1.0E-10) {
//                            output = 0;
//                        }
//                    }
//
//                    filterstore = (output * damp2) + (filterstore * damp1);
//                    //undenormalise(filterstore);
//                    if (filterstore > 0.0) {
//                        if (filterstore < 1.0E-10) {
//                            filterstore = 0;
//                        }
//                    }
//                    if (filterstore < 0.0) {
//                        if (filterstore > -1.0E-10) {
//                            filterstore = 0;
//                        }
//                    }
//
//                    buffer[bufidx] = inputs[i] + (filterstore * feedback);
//
//                    if (++bufidx >= bufsize) {
//                        bufidx = 0;
//                    }
//
//                    outputs[i] = output;
//
//                }
//            }
//        }
        public void setbuffer(float[] buf, int size) {
            buffer = buf;
            bufsize = size;
        }

        public void mute() {
            Arrays.fill(buffer, 0);
        }

        public void setdamp(float val) {
            damp1 = val;
            damp2 = 1 - val;
        }

        public float getdamp() {
            return damp1;
        }

        public void setfeedback(float val) {
            feedback = val;
        }

        public float getfeedback() {
            return feedback;
        }
    }

    private class Allpass {

        float feedback;
        float[] buffer;
        int bufsize;
        int bufidx = 0;

        public Allpass(int size) {
            buffer = new float[size];
            bufsize = size;
        }

        public void setbuffer(float[] buf, int size) {
            buffer = buf;
            bufsize = size;
        }

        public void mute() {
            Arrays.fill(buffer, 0);
        }

        public void setfeedback(float value) {
            feedback = value;
        }

        public float getfeedback() {
            return feedback;
        }

        public void processReplace(float inputs[], float outputs[], int buffersize) {

            for (int i = 0; i < buffersize; i++) {

                float bufout = buffer[bufidx];

                //undenormalise 
                if (bufout > 0.0) {
                    if (bufout < 1.0E-10) {
                        bufout = 0;
                    }
                }
                if (bufout < 0.0) {
                    if (bufout > -1.0E-10) {
                        bufout = 0;
                    }
                }

                float input = inputs[i];
                outputs[i] = -input + bufout;
                buffer[bufidx] = input + bufout * feedback;
                if (++bufidx >= bufsize) {
                    bufidx = 0;
                }
            }

        }
//        public void processMix(float inputs[], float outputs[], int from, int to, int channels) {
//            for (int c = 0; c < channels; c++) {
//                for (int i = from + c; i < to; i += channels) {
//
//                    float bufout = buffer[bufidx];
//
//                    //undernormalise
//                    if (bufout > 0.0) {
//                        if (bufout < 1.0E-10) {
//                            bufout = 0;
//                        }
//                    }
//                    if (bufout < 0.0) {
//                        if (bufout > -1.0E-10) {
//                            bufout = 0;
//                        }
//                    }
//
//                    float input = inputs[i];
//                    outputs[i] += -input + bufout;
//                    buffer[bufidx] = input + bufout * feedback;
//                    if (++bufidx >= bufsize) {
//                        bufidx = 0;
//                    }
//                }
//            }
//        }
    }
}