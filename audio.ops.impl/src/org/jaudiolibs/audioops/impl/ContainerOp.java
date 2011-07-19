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
package org.jaudiolibs.audioops.impl;

import org.jaudiolibs.audioops.AudioOp;

/**
 * An op that wraps another op with a mix parameter. When mix is zero then the op
 * is bypassed.
 *
 * @TODO implement non-bypass mode for wrapping ops that need to process even
 * when not outputting (eg. long delays).
 * @author Neil C Smith
 */
public class ContainerOp implements AudioOp {

    private AudioOp op;
    private float mix;
    private float oldMix;
    private boolean resetNeeded;
    private float[][] scratchContainer;
    private float[] scratchBuffer;

    public ContainerOp(AudioOp op) {
        if (op == null) {
            throw new NullPointerException();
        }
        this.op = op;
        this.scratchContainer = new float[1][];
    }

    public void setMix(float mix) {
        if (mix < 0 || mix > 1) {
            throw new IllegalArgumentException();
        }
        this.mix = mix;
    }

    public float getMix() {
        return mix;
    }

    public void processReplace(int buffersize, float[][] outputs, float[][] inputs) {
        if (mix != 0 || oldMix != 0) {
            processOp(buffersize, inputs);
            float[] in = inputs[0];
            float[] out = outputs[0];
            float[] scr = scratchBuffer;
            float m = oldMix;
            float delta = (mix - m) / buffersize;
            for (int i=0; i < buffersize; i++) {
                out[i] = (m * scr[i]) + ((1 - m) * in[i]);
                m += delta;
            }
            oldMix = mix;
        } else {
            float[] in = inputs[0];
            float[] out = outputs[0];
            for (int i=0; i < buffersize; i++) {
                out[i] = in[i];
            }
            resetNeeded = true;
        }
    }

    public void processAdd(int buffersize, float[][] outputs, float[][] inputs) {
        if (mix != 0 || oldMix != 0) {
            processOp(buffersize, inputs);
            float[] in = inputs[0];
            float[] out = outputs[0];
            float[] scr = scratchBuffer;
            float m = oldMix;
            float delta = (mix - m) / buffersize;
            for (int i=0; i < buffersize; i++) {
                out[i] += (m * scr[i]) + ((1 - m) * in[i]);
                m += delta;
            }
            oldMix = mix;
        } else {
            float[] in = inputs[0];
            float[] out = outputs[0];
            for (int i=0; i < buffersize; i++) {
                out[i] += in[i];
            }
            resetNeeded = true;
        }
    }

    private void processOp(int buffersize, float[][] inputs) {
        if (resetNeeded) {
            op.reset();
            resetNeeded = false;
        }
        scratchContainer[0] = scratchBuffer;
        op.processReplace(buffersize, scratchContainer, inputs);
    }

    public void initialize(float samplerate, int maxBufferSize) {
        op.initialize(samplerate, maxBufferSize);
        resetNeeded = false;
        scratchBuffer = new float[maxBufferSize];
    }

    public void reset() {
        op.reset();
        resetNeeded = false;
    }

    public boolean isInputRequired() {
        return true;
    }
}
