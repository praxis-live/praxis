/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 - Neil C Smith. All rights reserved.
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
package net.neilcsmith.praxis.video.janino;

import java.util.logging.Level;
import java.util.logging.Logger;
import net.neilcsmith.praxis.core.Argument;
import net.neilcsmith.praxis.core.ControlPort;
import net.neilcsmith.praxis.core.types.PNumber;
import net.neilcsmith.praxis.core.types.PString;
import net.neilcsmith.ripl.Surface;
import net.neilcsmith.ripl.SurfaceOp;
import net.neilcsmith.ripl.delegates.CompositeDelegate;
import net.neilcsmith.ripl.delegates.Delegate;

/**
 *
 * @author Neil C Smith (http://neilcsmith.net)
 */
public class JaninoVideoDelegate implements Delegate, CompositeDelegate {

    // directly accessible fields reset per frame
    public Surface surface;
    public Surface[] sources;
    public long time;
    // private fields
    private double[] floats;
    private Argument[] params;
    private boolean[] triggers;
    private Argument[] outs;

    void install(double[] floats, Argument[] params,
            boolean[] triggers, Argument[] outs) {
        this.floats = floats;
        this.params = params;
        this.triggers = triggers;
        this.outs = outs;
    }

    void dispose() {}

    public final void process(Surface surface) {
        this.surface = surface;
        try {
            process();
        } catch (Exception ex) {
            Logger.getLogger(JaninoVideoDelegate.class.getName()).
                    log(Level.WARNING, "JaninoVideoDelegate error", ex);
        }
        this.surface = null;
        // reset triggers

    }

    public final void process(Surface surface, Surface... sources) {
        this.sources = sources;
        process(surface);
        this.sources = null;
    }

    public final void update(long time) {
        this.time = time;
        try {
            update();
        } catch (Exception ex) {
            Logger.getLogger(JaninoVideoDelegate.class.getName()).
                    log(Level.WARNING, "JaninoVideoDelegate error", ex);
        }
    }

    public boolean forceRender() {
        return false;
    }

    public boolean usesInput() {
        return true;
    }

    public final double f(int i) {
        return floats[i - 1];
    }

    public final double f(int i, double f) {
        floats[i - 1] = f;
        return f;
    }

    public final Argument p(int i) {
        return params[i - 1];
    }

    public final Argument p(int i, Argument p) {
        if (p == null) {
            p = PString.EMPTY;
        }
        params[i - 1] = p;
        return p;
    }

    public final boolean t(int i) {
        return triggers[i - 1];
    }

    public final void send(int i, double value) {
        outs[i-1] = PNumber.valueOf(value);
    }

    public final void send(int i, String value) {
        outs[i-1] = PString.valueOf(value);
    }

    public final void op(SurfaceOp op) {
        surface.process(op);
    }

    public final void op(SurfaceOp op, Surface src1) {
        surface.process(op, src1);
    }

    public final void op(SurfaceOp op, Surface[] sources) {
        surface.process(op, sources);
    }

    public void update() {
    }

    public void process() {
    }
}
