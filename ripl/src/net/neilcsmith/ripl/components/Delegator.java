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

package net.neilcsmith.ripl.components;

import java.awt.Dimension;
import net.neilcsmith.ripl.Source;
import net.neilcsmith.ripl.Surface;
import net.neilcsmith.ripl.delegates.AbstractDelegate;
import net.neilcsmith.ripl.impl.SingleInOut;
import net.neilcsmith.ripl.delegates.Delegate;

/**
 *
 * @author Neil C Smith
 * @TODO implement full delegate API (update, inPlace checking, etc)
 */
public class Delegator extends SingleInOut {
    
    private Delegate delegate;
    private int currentWidth;
    private int currentHeight;
    
    public Delegator() {
        this.delegate = new EmptyDelegate();
    }
    
    public Delegator(Delegate delegate) {
        setDelegate(delegate);
    }
    
    public void setDelegate(Delegate delegate) {
        if (delegate == null) {
            this.delegate = new EmptyDelegate();
        } else {
            this.delegate = delegate;
        }
        
    }
    
    public Delegate getDelegate() {
        return delegate;
    }


    // @TODO remove this method - it's a hack!
    public Dimension getCurrentDimensions() {
        if (currentWidth < 1 || currentHeight < 1) {
            return null;
        } else {
            return new Dimension(currentWidth, currentHeight);
        }
    }
    
    @Override
    protected void process(Surface surface, boolean rendering) {
        if (rendering) {
            currentWidth = surface.getWidth();
            currentHeight = surface.getHeight();
            delegate.process(surface);
        }
        
    }

    @Override
    public boolean isRenderRequired(Source source, long time) {
        return delegate.forceRender() || super.isRenderRequired(source, time);
    }
    
    private class EmptyDelegate extends AbstractDelegate {

        @Override
        public void process( Surface output) {
            // no op
        }
        
    }


}
