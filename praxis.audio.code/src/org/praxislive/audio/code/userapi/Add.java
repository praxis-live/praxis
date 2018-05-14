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

import org.jaudiolibs.pipes.Buffer;
import org.jaudiolibs.pipes.impl.MultiInOut;

/**
 *
 * @author Neil C Smith (http://neilcsmith.net)
 */
public class Add extends MultiInOut {

    public Add() {
        super(32, 1);
    }

    @Override
    protected void writeOutput(Buffer[] inputs, Buffer output, int index) {
        if (inputs.length == 0) {
            output.clear();
            return;
        }
        float[] out = output.getData();
        for (int i = 0; i < inputs.length; i++) {
            float[] in = inputs[i].getData();
            if (i == 0) {
                for (int k = 0,  z = output.getSize(); k < z; k++) {
                    out[k] = in[k];
                }
            } else {
                for (int k = 0,  z = output.getSize(); k < z; k++) {
                    out[k] += in[k];
                }
            }

        }
    }
    
}
