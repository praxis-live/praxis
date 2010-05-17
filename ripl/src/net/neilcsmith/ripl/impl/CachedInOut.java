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

package net.neilcsmith.ripl.impl;

import java.util.ArrayList;
import java.util.List;
import net.neilcsmith.ripl.SinkIsFullException;
import net.neilcsmith.ripl.Source;
import net.neilcsmith.ripl.SourceIsFullException;
import net.neilcsmith.ripl.Surface;

/**
 *
 * @author Neil C Smith
 */
public abstract class CachedInOut extends AbstractInOut {
    
    private List<Surface> inputSurfaces;

    
    public CachedInOut(int maxSources, int maxSinks) {
        this(maxSources, maxSinks, true);
    }
    
    public CachedInOut(int maxSources, int maxSinks, boolean ensureClear) {
        super(maxSources, maxSinks, ensureClear);
        inputSurfaces = new ArrayList<Surface>(maxSources);
    }
   

    @Override
    public void addSource(Source source) throws SinkIsFullException, SourceIsFullException {
        super.addSource(source);
        inputSurfaces.add(null);
    }

    @Override
    public void removeSource(Source source) {
        int idx = getIndexOf(source);
        super.removeSource(source);
        if (idx > -1) {
            inputSurfaces.remove(idx);
        }
    }
    
    protected Surface getInputSurface(int index) {
        return inputSurfaces.get(index);
    }

//    
//    protected void setInputSurface(int index, Surface surface) {
//        inputSurfaces.set(index, surface);
//    }
    
    
    
    @Override
    protected void callSources(Surface surface, long time, boolean rendering) {
        for (int i=0, k=sources.size(); i < k; i++) {
            Surface oldInput = inputSurfaces.get(i);
            Surface input = validateInputSurface(oldInput, surface, i);
            if (input != oldInput) {
                inputSurfaces.set(i, input);
            }
            sources.get(i).process(input, this, time);
        }
    }

    protected Surface validateInputSurface(Surface input, Surface output, int index) {
        if (input == null ||
                !output.checkCompatible(input, true, true) ) {
            return output.createSurface(output.getWidth(), output.getHeight(), output.hasAlpha(), null);
        }
        return input;
    }
    
    protected void releaseSurfaces() {
        for (int i=0, k=inputSurfaces.size(); i < k; i++) {
            Surface s = inputSurfaces.get(i);
            if (s != null) {
                s.release();
            }
        }
    }
    

}
