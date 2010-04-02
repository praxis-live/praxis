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

package net.neilcsmith.rapl.core.impl;

import java.util.ArrayList;
import java.util.List;
import net.neilcsmith.rapl.core.SinkIsFullException;
import net.neilcsmith.rapl.core.Source;
import net.neilcsmith.rapl.core.SourceIsFullException;
import net.neilcsmith.rapl.core.Buffer;

/**
 *
 * @author Neil C Smith
 */
public abstract class CachedInOut extends AbstractInOut {
    
    List<Buffer> inputBuffers;
    boolean ensureClear;

    
    public CachedInOut(int maxSources, int maxSinks) {
        super(maxSources, maxSinks);
        inputBuffers = new ArrayList<Buffer>(maxSources);
    }
   

    @Override
    public void addSource(Source source) throws SinkIsFullException, SourceIsFullException {
        super.addSource(source);
        inputBuffers.add(null);
    }

    @Override
    public void removeSource(Source source) {
        int idx = getIndexOf(source);
        super.removeSource(source);
        if (idx > -1) {
            inputBuffers.remove(idx);
        }
    }
    
    protected Buffer getInputBuffer(int index) {
        return inputBuffers.get(index);
    }

//    
//    protected void setInputSurface(int index, Surface surface) {
//        inputBuffers.set(index, surface);
//    }
    
    
    
    @Override
    protected void callSources(Buffer buffer, long time, boolean rendering) {
        for (int i=0; i < sources.size(); i++) {
            Buffer oldInput = inputBuffers.get(i);
            Buffer input = validateInputBuffer(oldInput, buffer, i);
            if (input != oldInput) {
                inputBuffers.set(i, input);
            }
            sources.get(i).process(input, this, time);
        }
    }

    protected Buffer validateInputBuffer(Buffer input, Buffer output, int index) {
        if (input == null ||
                !output.isCompatible(input)) {
            return output.createBuffer();
        }
        return input;
    }
    

}
