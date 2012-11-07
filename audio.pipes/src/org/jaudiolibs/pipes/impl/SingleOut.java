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

import org.jaudiolibs.pipes.Buffer;
import org.jaudiolibs.pipes.Pipe;
import org.jaudiolibs.pipes.SourceIsFullException;

/**
 *
 * @author Neil C Smith
 */
public abstract class SingleOut extends Pipe {

    Pipe sink;
    private long time;

    @Override
    public final void process(Pipe sink, Buffer buffer, long time) {
        processImpl(sink, buffer, time);
    }

    void processImpl(Pipe sink, Buffer buffer, long time) {
        if (this.sink == sink) {
            this.time = time;
            process(buffer, sinkRequiresRender(sink, time));
        }
    }

    @Override
    protected void registerSource(Pipe source) {
        throw new UnsupportedOperationException();
    }

    @Override
    protected void unregisterSource(Pipe source) {
    }

    @Override
    public final void registerSink(Pipe sink) throws SourceIsFullException {
        if (sink == null) {
            throw new NullPointerException();
        }
        if (this.sink != null) {
            throw new SourceIsFullException();
        }
        this.sink = sink;
    }

    @Override
    public final void unregisterSink(Pipe sink) {
        if (this.sink == sink) {
            this.sink = null;
        }
    }

    protected abstract void process(Buffer buffer, boolean rendering);
    
    protected long getTime() {
        return time;
    }

    @Override
    public int getSourceCount() {
        return 0;
    }

    @Override
    public int getSourceCapacity() {
        return 0;
    }

    @Override
    public Pipe getSource(int idx) {
        throw new IndexOutOfBoundsException();
    }

    @Override
    public final int getSinkCount() {
        return sink == null ? 0 : 1;
    }

    @Override
    public final int getSinkCapacity() {
        return 1;
    }

    @Override
    public Pipe getSink(int idx) {
        if (idx == 0 && sink != null) {
            return sink;
        }
        throw new IndexOutOfBoundsException();
    }

    @Override
    protected boolean isRenderRequired(Pipe source, long time) {
        return false;
    }
}
