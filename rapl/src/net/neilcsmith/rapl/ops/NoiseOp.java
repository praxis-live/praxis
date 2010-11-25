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

package net.neilcsmith.rapl.ops;

/**
 *
 * @author Neil C Smith
 */
public class NoiseOp implements AudioOp {
    
    private float gain;
    
    public NoiseOp() {
        this(0.7f);
    }
    
    public NoiseOp(float gain) {
        this.gain = gain;
    }

    public void setGain(float gain) {
        this.gain = gain;
    }
    
    public float getGain() {
        return gain;
    }
    

    public void reset() {
        // no op
    }

//    public void processReplace(Buffer buffer) {
//        float g = gain;
//        float g2 = gain * 2;
//        float[] data = buffer.getData();
//        for (int i=0, k=buffer.getSize(); i<k; i++) {
//            data[i] = (Math.random() * g2) - g;
//        }
//    }

//    public void processAdd(Buffer buffer) {
//        float g = gain;
//        float g2 = gain * 2;
//        float[] data = buffer.getData();
//        for (int i=0, k=buffer.getSize(); i<k; i++) {
//            data[i] += (Math.random() * g2) - g;
//        }
//    }
//
//    public void processReplace(Buffer bufferIn, Buffer bufferOut) {
//        float g = gain;
//        float g2 = gain * 2;
//        float[] data = bufferOut.getData();
//        for (int i=0, k=bufferOut.getSize(); i<k; i++) {
//            data[i] = (Math.random() * g2) - g;
//        }
//    }
//
//    public void processAdd(Buffer bufferIn, Buffer bufferOut) {
//        float g = gain;
//        float g2 = gain * 2;
//        float[] dataIn = bufferIn.getData();
//        float[] dataOut = bufferOut.getData();
//        for (int i=0, k=bufferOut.getSize(); i<k; i++) {
//            dataOut[i] = dataIn[i] + (Math.random() * g2) - g;
    // This is wrong - should add to outputs
//        }
//    }

    public void processReplace(int buffersize, float[][] outputs, float[][] inputs) {
        float g = gain;
        float g2 = g * 2;
        for (float[] out : outputs) {
            for (int i=0; i < buffersize; i++) {
                out[i] = (float) ((Math.random() * g2) - g);
            }
        }
    }

    public void processAdd(int buffersize, float[][] outputs, float[][] inputs) {
        float g = gain;
        float g2 = g * 2;
        for (float[] out : outputs) {
            for (int i=0; i < buffersize; i++) {
                out[i] += (Math.random() * g2) - g;
            }
        }
    }

    public void initialize(float samplerate) {
        // no op
    }

    public boolean isInputRequired() {
        return false;
    }

}
