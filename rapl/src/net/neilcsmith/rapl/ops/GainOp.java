/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2008 - Neil C Smith. All rights reserved.
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
 */
package net.neilcsmith.rapl.ops;

import net.neilcsmith.rapl.core.Buffer;

/**
 *
 * @author Neil C Smith
 */
public class GainOp implements AudioOpOld {

    private float gain = 1.0f;
    private float oldGain = 0;
    

    public void setGain(float gain) {
        if (gain < 0) {
            throw new IllegalArgumentException();
        }
        this.gain = gain;
    }

    public float getGain() {
        return gain;
    }

    public void processReplace(Buffer bufferIn, Buffer bufferOut) {
        process(bufferIn, bufferOut, true);
//        if (gain == 0) {
//            bufferOut.clear();
//        } else if (gain == 1) {
//            if (bufferIn == bufferOut) {
//                return;
//            } else {
//                float[] in = bufferIn.getData();
//                float[] out = bufferOut.getData();
//                for (int i = 0,  k = bufferIn.getSize(); i < k; i++) {
////                    bufferOut.set(i, bufferIn.get(i));
//                    out[i] = in[i];
//                }
//            }
//        } else {
//            float g = gain;
//            float[] in = bufferIn.getData();
//            float[] out = bufferOut.getData();
//            for (int i = 0,  k = bufferIn.getSize(); i < k; i++) {
////                bufferOut.set(i, g * bufferIn.get(i));
//                out[i] = g * in[i];
//            }
//        }
    }

    public void processAdd(Buffer bufferIn, Buffer bufferOut) {
        process(bufferIn, bufferOut, false);
//        if (gain == 0) {
//            return;
//        } else if (gain == 1) {
//            float[] in = bufferIn.getData();
//            float[] out = bufferOut.getData();
//                for (int i = 0,  k = bufferIn.getSize(); i < k; i++) {
////                    bufferOut.set(i, bufferOut.get(i) + bufferIn.get(i));
//                    out[i] = out[i] + in[i];
//                }
//        } else {
//            float g = gain;
//            float[] in = bufferIn.getData();
//            float[] out = bufferOut.getData();
//            for (int i = 0,  k = bufferIn.getSize(); i < k; i++) {
////                bufferOut.set(i, bufferOut.get(i) + (g * bufferIn.get(i)));
//                out[i] = out[i] + g * in[i];
//            }
//        }
    }
    
    private void process(Buffer bufferIn, Buffer bufferOut, boolean replace) {
        float[] in = bufferIn.getData();
        float[] out = bufferOut.getData();
        int len = out.length;
        float g = oldGain;
        float delta = (gain - g) / len;
        
        if (replace) {
            for (int i=0; i < len; i++) {
                out[i] = g * in[i];
                g += delta;
            }
        } else {
            for (int i=0; i < len; i++) {
                out[i] += g * in[i];
                g += delta;
            }
        }
        
        oldGain = gain;
        
    }
    

    public void reset() {
        oldGain = 0;
    // no op
    }
}
