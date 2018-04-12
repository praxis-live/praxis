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
 */
package org.praxislive.video.pipes;

import org.praxislive.video.render.Surface;

/**
 *
 * @author Neil C Smith
 */
public abstract class VideoPipe {
    
    public final void addSource(VideoPipe source) {
        source.registerSink(this);
        try {
            registerSource(source);
        } catch (RuntimeException ex) {
            source.unregisterSink(this);
            throw ex;
        }   
    }
    
    public final void removeSource(VideoPipe source) {
        source.unregisterSink(this);
        unregisterSource(source);
    }
    
    public abstract int getSourceCount();
    
    public abstract int getSourceCapacity();
    
    public abstract VideoPipe getSource(int idx);
    
    public abstract int getSinkCount();
    
    public abstract int getSinkCapacity();
    
    public abstract VideoPipe getSink(int idx);
    
    protected final void callSource(VideoPipe source, Surface buffer, long time) {
        source.process(this, buffer, time);
    }
    
    protected final boolean sinkRequiresRender(VideoPipe sink, long time) {
        return sink.isRenderRequired(this, time);
    }
    
    protected abstract void process(VideoPipe sink, Surface buffer, long time);
    
    protected abstract boolean isRenderRequired(VideoPipe source, long time);
    
    protected abstract void registerSource(VideoPipe source);
    
    protected abstract void unregisterSource(VideoPipe source);
    
    protected abstract void registerSink(VideoPipe sink);
    
    protected abstract void unregisterSink(VideoPipe sink);
    
    
    
}
