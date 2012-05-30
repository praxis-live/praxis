/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012 Neil C Smith.
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
package org.jaudiolibs.pipes.impl;

import org.jaudiolibs.audioops.AudioOp;
import org.jaudiolibs.pipes.Buffer;
import org.jaudiolibs.pipes.Pipe;

/**
 *
 * @author Neil C Smith
 */
public class OpHolder extends SingleInOut {

    private AudioOp op;
    private float samplerate;
    private int buffersize;
    private int skipped;
    private float[][] dataHolder;

    public OpHolder(AudioOp op) {
        if (op == null) {
            throw new NullPointerException();
        }
        this.op = op;
        dataHolder = new float[1][];
    }
    
    @Override
    protected void process(Buffer buffer, boolean rendering) {
        if (rendering) {
            if (samplerate != buffer.getSampleRate() ||
                    buffersize < buffer.getSize()) {
                samplerate = buffer.getSampleRate();
                buffersize = buffer.getSize();
                op.initialize(samplerate, buffersize);
                skipped = 0;
            } else if (skipped != 0) {
                op.reset(skipped);
                skipped = 0;
            }
            dataHolder[0] = buffer.getData();
            op.processReplace(buffer.getSize(), dataHolder, dataHolder);
        } else {
            skipped += buffer.getSize();
        }
    }

    @Override
    protected boolean isRenderRequired(Pipe source, long time) {
        return op.isInputRequired(super.isRenderRequired(source, time));
    }
    
    

}
