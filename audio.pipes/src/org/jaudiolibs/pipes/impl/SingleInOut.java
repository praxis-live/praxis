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

import org.jaudiolibs.pipes.*;

/**
 *
 * @author Neil C Smith
 */
public abstract class SingleInOut extends SingleOut implements Sink {

    private Source source;
    private long renderReqTime;
    private boolean renderReqCache;

    @Override
    public final void addSource(Source source) throws SinkIsFullException, SourceIsFullException {
        if (source == null) {
            throw new NullPointerException();
        }
        if (this.source != null) {
            throw new SinkIsFullException();
        }
        source.registerSink(this);
        this.source = source;
    }

    @Override
    public final void removeSource(Source source) {
        if (this.source == source) {
            source.unregisterSink(this);
            this.source = null;
        }
    }

    @Override
    public Source[] getSources() {
        if (source == null) {
            return new Source[0];
        } else {
            return new Source[]{source};
        }
    }

    @Override
    public boolean isRenderRequired(Source source, long time) {
        return isRendering(time);
    }

    boolean isRendering(long time) {
        if (time != renderReqTime) {
            if (sink == null) {
                renderReqCache = false;
            } else {
                renderReqCache = sink.isRenderRequired(this, time);
            }
            renderReqTime = time;
        }
        return renderReqCache;
    }

    @Override
    void processImpl(Buffer buffer, Sink sink, long time) {
        if (this.sink == sink) {
            if (source == null) {
                buffer.clear();
            } else {
                source.process(buffer, this, time);
            }
            process(buffer, isRendering(time));
        }
    }
}
