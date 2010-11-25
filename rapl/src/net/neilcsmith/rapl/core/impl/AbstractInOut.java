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
package net.neilcsmith.rapl.core.impl;

import java.util.ArrayList;
import java.util.List;
import net.neilcsmith.rapl.core.Sink;
import net.neilcsmith.rapl.core.SinkIsFullException;
import net.neilcsmith.rapl.core.Source;
import net.neilcsmith.rapl.core.SourceIsFullException;
import net.neilcsmith.rapl.core.Buffer;

/**
 *
 * @author Neil C Smith
 */
public abstract class AbstractInOut extends AbstractSource implements Sink {

    int maxSources;
    List<Source> sources;
    long time = 0;
    private long renderReqTime;
    private boolean renderReqCache;

    public AbstractInOut(int maxSources, int maxSinks) {
        super(maxSinks);
        if (maxSources < 1) {
            throw new IllegalArgumentException();
        }
        this.maxSources = maxSources;
        this.sources = new ArrayList<Source>(maxSources);
    }

    public void process(Buffer buffer, Sink sink, long time) {
        if (!validateSink(sink)) {
            return;
        }
        boolean rendering = isRendering(time);
        if (this.time != time) {
            this.time = time;
            callSources(buffer, time, rendering);
        }
        process(buffer, rendering);
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time, boolean recurse) {
        this.time = time;
        if (recurse) {
            for (Source source : sources) {
                source.setTime(time, true);
            }
        }
    }


    public void addSource(Source source) throws SinkIsFullException, SourceIsFullException {
        if (source == null) {
            throw new NullPointerException();
        }
        if (sources.contains(source)) {
            return;
        }
        if (sources.size() == maxSources) {
            throw new SourceIsFullException();
        }
        source.registerSink(this);
        sources.add(source);
    }

    public void removeSource(Source source) {
        if (sources.contains(source)) {
            source.unregisterSink(this);
            sources.remove(source);
        }
    }

    public boolean isRenderRequired(Source source, long time) {
        return isRendering(time);
    }

    public Source[] getSources() {
        return sources.toArray(new Source[sources.size()]);
    }

    protected boolean isRendering(long time) {
        if (sinks.size() == 1) {
            return simpleRenderingCheck(time);
        } else {
            return protectedRenderingCheck(time);
        }
    }

    private boolean simpleRenderingCheck(long time) {
        if (time != renderReqTime) {
            renderReqTime = time;
            renderReqCache = sinks.get(0).isRenderRequired(this, time);
        }
        return renderReqCache;
    }
    private int renderIdx = 0;

    private boolean protectedRenderingCheck(long time) {
        if (renderIdx > 0) {
            while (renderIdx < sinks.size()) {
                if (sinks.get(renderIdx++).isRenderRequired(this, time)) {
                    renderIdx = 0;
                    return true;
                }
            }
            return false;
        } else {
            if (renderReqTime != time) {
                renderReqTime = time;
                renderReqCache = false;
                while (renderIdx < sinks.size()) {
                    if (sinks.get(renderIdx++).isRenderRequired(this, time)) {
                        renderReqCache = true;
                        break;
                    }
                }
                renderIdx = 0;
            }
            return renderReqCache;
        }
    }

    protected int getSourceCount() {
        return sources.size();
    }

    protected Source getSource(int index) {
        return sources.get(index);
    }

    protected int getIndexOf(Source source) {
        return sources.indexOf(source);
    }

    protected abstract void callSources(Buffer buffer, long time, boolean rendering);

    protected abstract void process(Buffer buffer, boolean rendering);
}
