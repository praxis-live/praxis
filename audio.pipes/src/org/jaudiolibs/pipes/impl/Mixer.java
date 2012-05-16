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
package org.jaudiolibs.pipes.impl;

import org.jaudiolibs.pipes.Buffer;

/**
 *
 * @author Neil C Smith
 */
public class Mixer extends MultiInputInOut {

    public Mixer(int maxInputs) {
        super(maxInputs);
    }

    @Override
    protected void process(Buffer buffer, boolean rendering) {
        int inputs = getSourceCount();
        if (inputs == 0) {
            buffer.clear();
            return;
        }

        float[] out = buffer.getData();
        for (int i = 0; i < inputs; i++) {
            float[] in = getInputBuffer(i).getData();
            if (i == 0) {
                for (int k = 0,  z = out.length; k < z; k++) {
                    out[k] = in[k];
                }
            } else {
                for (int k = 0,  z = out.length; k < z; k++) {
                    out[k] += in[k];
                }
            }

        }




    }
}
