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

package net.neilcsmith.praxis.video.java;

import java.util.logging.Level;
import java.util.logging.Logger;
import net.neilcsmith.praxis.java.CodeDelegate;
import net.neilcsmith.ripl.Surface;
import net.neilcsmith.ripl.SurfaceOp;
import net.neilcsmith.ripl.delegates.CompositeDelegate;
import net.neilcsmith.ripl.delegates.Delegate;

/**
 *
 * @author Neil C Smith (http://neilcsmith.net)
 */
public class VideoCodeDelegate extends CodeDelegate implements Delegate, CompositeDelegate {

    private final static Logger LOG = Logger.getLogger(VideoCodeDelegate.class.getName());
    private final static Surface[] EMPTY_SOURCES = new Surface[0];

    private PImage dst;
    private PGraphics g;
    private boolean inited;

    public PImage src;
    public Surface[] sources;

    public final void update(long time) {
        // ignore - tick() provides same function earlier in update cycle.
    }
    
    public final void process(Surface surface) {
        validateGraphics(surface);
        sources = EMPTY_SOURCES;
        processImpl();
    }

    public final void process(Surface surface, Surface... sources) {
        validateGraphics(surface);
        if (sources.length > 0) {
            Surface s = sources[0];
            if (src == null || src.getSurface() != s) {
                src = new PImage(s);
            }
        }
        this.sources = sources;
        processImpl();
    }

    private void processImpl() {
        if (!inited) {
            try {
                setup();
            } catch (Exception ex) {
                LOG.log(Level.WARNING, "", ex);
            }
            inited = true;
        }
        try {
            draw();
        } catch (Exception ex) {
            LOG.log(Level.WARNING, "", ex);
        }
    }

    private void validateGraphics(Surface surface) {
        if (dst == null) {
            dst = new PImage(surface);
            g = new PGraphics(dst);
        } else if (dst.getSurface() != surface) {
            dst = new PImage(surface);
            g.setImage(dst);
        }
    }



    public boolean forceRender() {
        return false;
    }

    public boolean usesInput() {
        return true;
    }

    public void setup() {}

    public void draw() {process();} //old API used process()

    public void process() {}


    // PGraphics impl
    public void image(PImage image, double x, double y) {
        g.image(image, x, y);
    }

    public void op(SurfaceOp op) {
        g.op(op);
    }

    public void op(SurfaceOp op, Surface src) {
        g.op(op, src);
    }

}