/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Neil C Smith. All rights reserved.
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
package net.neilcsmith.rapl.components.mix;

import org.jaudiolibs.audioops.impl.GainOp;
import net.neilcsmith.rapl.core.Buffer;
import net.neilcsmith.rapl.core.Source;
import net.neilcsmith.rapl.core.impl.MultiInputInOut;

/**
 *
 * @author Neil C Smith
 */
public class XFader extends MultiInputInOut {

    private GainOp g1;
    private GainOp g2;
    private float[][] dataIn;
    private float[][] dataOut;

    public XFader() {
        super(2);
        g1 = new GainOp();
        g2 = new GainOp();
        dataIn = new float[1][];
        dataOut = new float[1][];
        setMix(0);
    }

    public void setMix(float mix) {
        mix = mix < 0 ? 0.0f : (mix > 1 ? 1.0f : mix);
        g2.setGain(mix);
        g1.setGain(1 - mix);
    }

    public float getMix() {
        return g2.getGain();
    }

    @Override
    protected void process(Buffer buffer, boolean rendering) {
        if (rendering) {
            int count = getSourceCount();
            if (count > 0) {
                int buffersize = buffer.getSize();
                dataOut[0] = buffer.getData();
                dataIn[0] = getInputBuffer(0).getData();
                g1.processReplace(buffersize, dataOut, dataIn);
                if (count > 1) {
                    dataIn[0] = getInputBuffer(1).getData();
                    g2.processAdd(buffersize, dataOut, dataIn);
                }
            } else {
                buffer.clear();
            }
        }
    }

    @Override
    public boolean isRenderRequired(Source source, long time) {
        switch (getSourceCount()) {
            case 2:
                if (source == getSource(2)) {
                    return g2.isInputRequired() &&
                            super.isRenderRequired(source, time);
                }
            case 1:
                if (source == getSource(1)) {
                    return g1.isInputRequired() &&
                            super.isRenderRequired(source, time);
                }
            default:
                return false;
        }
    }
}
