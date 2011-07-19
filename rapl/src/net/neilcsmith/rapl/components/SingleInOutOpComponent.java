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
 *
 */
package net.neilcsmith.rapl.components;

import org.jaudiolibs.audioops.AudioOp;
import net.neilcsmith.rapl.core.Buffer;
import net.neilcsmith.rapl.core.impl.SingleInOut;

/**
 *
 * @author Neil C Smith
 */
public abstract class SingleInOutOpComponent extends SingleInOut {

    private AudioOp op;
    private float samplerate;
    private int buffersize;
    private boolean resetRequired;
    private float[][] dataHolder;

    public SingleInOutOpComponent() {
        dataHolder = new float[1][];
    }

    @Override
    protected void process(Buffer buffer, boolean rendering) {
        AudioOp o = op;
        if (o == null) {
            return; // passthrough
        }
        if (rendering) {
            if (samplerate != buffer.getSampleRate() ||
                    buffersize < buffer.getSize()) {
                samplerate = buffer.getSampleRate();
                buffersize = buffer.getSize();
                op.initialize(samplerate, buffersize);
                resetRequired = false;
            } else if (resetRequired) {
                op.reset();
            }
            dataHolder[0] = buffer.getData();
            op.processReplace(buffer.getSize(), dataHolder, dataHolder);
            resetRequired = false;
        } else {
            resetRequired = true;
        }

    }

    protected void setOp(AudioOp op) {
        this.op = op;
        samplerate = 0;
        buffersize = 0;
    }
}
