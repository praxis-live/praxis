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

import java.util.ArrayList;
import java.util.List;
import org.jaudiolibs.pipes.Sink;
import org.jaudiolibs.pipes.SinkIsFullException;
import org.jaudiolibs.pipes.Source;
import org.jaudiolibs.pipes.SourceIsFullException;
import org.jaudiolibs.pipes.Buffer;

/**
 *
 * @author Neil C Smith
 */
abstract class AbstractInOut extends AbstractSource implements Sink {

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

    @Override
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

//    @Override
//    public long getTime() {
//        return time;
//    }
//
//    @Override
//    public void setTime(long time, boolean recurse) {
//        this.time = time;
//        if (recurse) {
//            for (Source source : sources) {
//                source.setTime(time, true);
//            }
//        }
//    }


    @Override
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

    @Override
    public void removeSource(Source source) {
        if (sources.contains(source)) {
            source.unregisterSink(this);
            sources.remove(source);
        }
    }

    @Override
    public boolean isRenderRequired(Source source, long time) {
        return isRendering(time);
    }

    @Override
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
