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

package net.neilcsmith.rapl.components;

import net.neilcsmith.rapl.core.Buffer;
import net.neilcsmith.rapl.core.impl.SingleOut;
import net.neilcsmith.rapl.util.SampleTable;

/**
 *
 * @author Neil C Smith
 */
public class SimpleLooper extends SingleOut {
    
    private SampleTable sample = null;
    private int sampleSize = 0;
    private int index = 0;

    public void setSampleBuffer(SampleTable sample) {
        this.sample = sample;
        if (sample == null) {
            sampleSize = 0;
        } else {
            sampleSize = sample.getSize();
        }
        index = 0;
    }
    
    @Override
    protected void process(Buffer buffer, boolean rendering) {
        if (rendering && sample != null) {
            int length = buffer.getSize();
            float[] out = buffer.getData();
            for (int i=0; i < length; i++) {
                if (index >= sampleSize) {
                    index %= sampleSize;
                }
//                buffer.set(i, sample.get(0, index));
                out[i] = sample.get(0, index);
                index++;
            }
        } else {
            if (sample != null) {
                index += buffer.getSize();
                if (index >= sampleSize) {
                    index %= sampleSize;
                }
            }
        }
    }

}
