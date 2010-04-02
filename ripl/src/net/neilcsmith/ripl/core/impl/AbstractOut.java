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
package net.neilcsmith.ripl.core.impl;

import net.neilcsmith.ripl.core.Sink;
import net.neilcsmith.ripl.core.Surface;

/**
 *
 * @author Neil C Smith
 */
public abstract class AbstractOut extends AbstractSource {

    private long time = 0;
    private long renderReqTime = 0L;
    private boolean renderReqCache = true;
    private boolean ensureClear;

    public AbstractOut(int maxSinks) {
        this(maxSinks, true);
    }
    
    public AbstractOut(int maxSinks, boolean ensureClear) {
        super(maxSinks);
        this.ensureClear = ensureClear;
    }

    public void process(Surface surface, Sink sink, long time) {
        if (!validateSink(sink)) {
            return;
        }
        this.time = time;
//        process(surface, isRendering(time));
        if (isRendering(time)) {
            if (ensureClear) {
                surface.clear();
            }
            process(surface, true);
        } else {
            process(surface, false);
        }

    }

    public long getTime() {
        return time;
    }

    public void setTime(long time, boolean recurse) {
        this.time = time;
    }

    protected boolean isRendering(long time) {
        if (time != renderReqTime) {
            renderReqTime = time;
            if (sinks.size() == 1) {
                renderReqCache = sinks.get(0).isRenderRequired(this, time);
            } else {
                renderReqCache = false;
                for (int i = 0; i < sinks.size(); i++) {
                    if (sinks.get(i).isRenderRequired(this, time)) {
                        renderReqCache = true;
                        break;
                    }
                }
            }
        }
        return renderReqCache;
    }

    protected abstract void process(Surface surface, boolean rendering);
}
