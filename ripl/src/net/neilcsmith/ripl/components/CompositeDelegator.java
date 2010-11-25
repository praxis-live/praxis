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

import net.neilcsmith.ripl.Surface;
import net.neilcsmith.ripl.delegates.CompositeDelegate;
import net.neilcsmith.ripl.impl.AbstractInOut;

/**
 *
 * @author Neil C Smith
 */
public class CompositeDelegator extends AbstractInOut {

    private CompositeDelegate delegate;
    private Surface[] srcs;

    public CompositeDelegator(int sources) {
        super(sources + 1, 1, false);
        srcs = new Surface[sources];
        delegate = new EmptyDelegate();
    }

    public void setDelegate(CompositeDelegate delegate) {
        if (delegate == null) {
            this.delegate = new EmptyDelegate();
        } else {
            this.delegate = delegate;
        }

    }

    public CompositeDelegate getDelegate() {
        return delegate;
    }

    @Override
    protected void callSources(Surface surface, long time, boolean rendering) {
        int count = getSourceCount();
        if (count == 0) {
            if (rendering) {
                surface.clear();
            }
            return;
        } else {
            getSource(0).process(surface, this, time);
        }
        for (int i = 0; i < srcs.length; i++) {
            Surface src = srcs[i];
            if (src == null || !surface.checkCompatible(src, true, true)) {
                src = surface.createSurface(null);
                srcs[i] = src;
            }
            if ( (i + 1) < count) {
                getSource(i + 1).process(src, this, time);
            } else {
                if (rendering) {
                    src.clear();
                }
            }

        }
    }

    @Override
    protected void process(Surface surface, boolean rendering) {
        if (rendering) {
            delegate.process(surface, srcs);
        }
    }

    private class EmptyDelegate implements CompositeDelegate {

        public void process(Surface surface, Surface... sources) {
            // no op
        }

        public void update(long time) {
            // no op
        }

        public boolean forceRender() {
            return false;
        }

        public boolean usesInput() {
            return true;
        }
    }
}

