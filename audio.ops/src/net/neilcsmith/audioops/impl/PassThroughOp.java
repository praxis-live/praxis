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
package net.neilcsmith.audioops.impl;

import net.neilcsmith.audioops.AudioOp;
import net.neilcsmith.audioops.AudioOp;

/**
 * An op that just passes the signal through.
 * @author Neil C Smith
 */
public class PassThroughOp implements AudioOp {

    public void processReplace(int buffersize, float[][] outputs, float[][] inputs) {
        float[] in = inputs[0];
        float[] out = outputs[0];
        if (in != out) {
            for (int i = 0; i < buffersize; i++) {
                out[i] = in[i];
            }
        }

    }

    public void processAdd(int buffersize, float[][] outputs, float[][] inputs) {
        float[] in = inputs[0];
        float[] out = outputs[0];
        for (int i = 0; i < buffersize; i++) {
            out[i] += in[i];
        }
    }

    public void initialize(float samplerate, int maxBufferSize) {
        // no op
    }

    public void reset() {
        // no op
    }

    public boolean isInputRequired() {
        return true;
    }
}
