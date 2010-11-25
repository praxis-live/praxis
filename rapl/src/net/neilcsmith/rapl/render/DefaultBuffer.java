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
package net.neilcsmith.rapl.render;

import net.neilcsmith.rapl.core.Buffer;

/**
 *
 * @author Neil C Smith
 */
public class DefaultBuffer implements Buffer {//extends Buffer { // implements Buffer {
    
    private float[] data;
    private float sampleRate;
    private int bufferSize;
    
    public DefaultBuffer(float sampleRate, int bufferSize) {
//        super(sampleRate, bufferSize);
        
        if (sampleRate < 1 || bufferSize < 1) {
            throw new IllegalArgumentException();
        }
        this.sampleRate = sampleRate;
        this.bufferSize = bufferSize;
        this.data = new float[bufferSize];
        
    }

    public float[] getData() {
        return data;
    }

    public boolean isCompatible(Buffer buffer) {
//        return buffer instanceof DefaultBuffer;
        return buffer instanceof DefaultBuffer &&
                buffer.getSampleRate() == sampleRate &&
                buffer.getSize() == bufferSize;
    }

    public Buffer createBuffer() {
        return new DefaultBuffer(getSampleRate(), getSize());
    }

    public float getSampleRate() {
        return this.sampleRate;
    }

    public int getSize() {
        return this.bufferSize;
    }

    public void clear() {
        for (int i = 0; i < data.length; i++) {
            data[i] = 0;
        }
    }

    public void release() {
        // no op
    }
}
