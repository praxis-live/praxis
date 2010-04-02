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

import java.util.ArrayList;
import java.util.List;
import net.neilcsmith.ripl.core.Sink;
import net.neilcsmith.ripl.core.Source;
import net.neilcsmith.ripl.core.SourceIsFullException;

/**
 *
 * @author Neil C Smith
 */
public abstract class AbstractSource implements Source {

    
    int maxSinks;
    List<Sink> sinks;
    
    
    protected AbstractSource(int maxSinks) {
        if (maxSinks < 1) {
            throw new IllegalArgumentException();
        }
        this.maxSinks = maxSinks;
        sinks = new ArrayList<Sink>(maxSinks);
    }

    public void registerSink(Sink sink) throws SourceIsFullException {
        if (sink == null) {
            throw new NullPointerException();
        }
        if (sinks.contains(sink)) {
            return;
        }
        if (sinks.size() == maxSinks) {
            throw new SourceIsFullException();
        }
        sinks.add(sink);
    }

    public void unregisterSink(Sink sink) {
        sinks.remove(sink);
    }

    public Sink[] getSinks() {
        return sinks.toArray(new Sink[sinks.size()]);
    }
    
    protected boolean validateSink(Sink sink) {
        return sinks.contains(sink);
    }
    
    protected int getSinkCount() {
        return sinks.size();
    }
    
    protected Sink getSink(int index) {
        return sinks.get(index);
    }
    
    protected int getIndexOf(Sink sink) {
        return sinks.indexOf(sink);
    }

}
