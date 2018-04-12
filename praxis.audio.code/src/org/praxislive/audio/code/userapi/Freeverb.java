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

import org.praxislive.audio.code.Resettable;
import org.jaudiolibs.audioops.impl.FreeverbOp;
import org.jaudiolibs.pipes.impl.MultiChannelOpHolder;

/**
 *
 * @author Neil C Smith (http://neilcsmith.net)
 */
public class Freeverb extends MultiChannelOpHolder<FreeverbOp>
        implements Resettable {

    private final static float INITIAL_DAMP = 0.5f;
    private final static float INITIAL_DRY = 0.5f;
    private final static float INITIAL_ROOM_SIZE = 0.5f;
    private final static float INITIAL_WET = 0;
    private final static float INITIAL_WIDTH = 0.5f;
    
    private final FreeverbOp op;

    public Freeverb() {
        this(new FreeverbOp(), 2);
    }

    private Freeverb(FreeverbOp op, int channels) {
        super(op, channels);
        this.op = op;
    }

    public Freeverb damp(double damp) {
        op.setDamp((float) Utils.constrain(damp, 0, 1));
        return this;
    }
    
    public double damp() {
        return op.getDamp();
    }
    
    public Freeverb dry(double dry) {
        op.setDry((float) Utils.constrain(dry, 0, 1));
        return this;
    }
    
    public double dry() {
        return op.getDry();
    }
    
    public Freeverb roomSize(double size) {
        op.setRoomSize((float) Utils.constrain(size, 0, 1));
        return this;
    }
    
    public double roomSize() {
        return op.getRoomSize();
    }
    
    public Freeverb wet(double wet) {
        op.setWet((float) Utils.constrain(wet, 0, 1));
        return this;
    }
    
    public double wet() {
        return op.getWet();
    }
    
    public Freeverb width(double width) {
        op.setWidth((float) Utils.constrain(width, 0, 1));
        return this;
    }
    
    public double width() {
        return op.getWidth();
    }
    
    @Override
    public void reset() {
        op.setDamp(INITIAL_DAMP);
        op.setDry(INITIAL_DRY);
        op.setRoomSize(INITIAL_ROOM_SIZE);
        op.setWet(INITIAL_WET);
        op.setWidth(INITIAL_WIDTH);
    }

}
