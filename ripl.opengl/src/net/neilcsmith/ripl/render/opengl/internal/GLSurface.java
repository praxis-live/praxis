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
package net.neilcsmith.ripl.render.opengl.internal;

import java.util.logging.Level;
import java.util.logging.Logger;
import net.neilcsmith.ripl.PixelData;
import net.neilcsmith.ripl.Surface;
import net.neilcsmith.ripl.SurfaceCapabilities;
import net.neilcsmith.ripl.SurfaceOp;
import net.neilcsmith.ripl.ops.Blit;
import net.neilcsmith.ripl.ops.Reverse;
import net.neilcsmith.ripl.render.opengl.ops.GLOp;
import net.neilcsmith.ripl.render.opengl.ops.GLOpCache;

/**
 *
 * @author Neil C Smith
 */
public class GLSurface extends Surface {

    private final static Logger LOGGER = Logger.getLogger(GLSurface.class.getName());
    private final static SurfaceCapabilities CAPS = new SurfaceCapabilities(true);
    private final static PixelData[] EMPTY_DATA = new PixelData[0];
    private GLSurfaceData sd;
    private boolean clear = true;
    private GLSurface parent;
    private boolean accelerated;

    public GLSurface(int width, int height, boolean alpha) {
        super(width, height, alpha);
    }

    private GLSurface(GLSurface parent, int width, int height, boolean alpha) {
        this(width, height, alpha);
        this.parent = parent;
    }

    @Override
    public void process(SurfaceOp op, Surface... inputs) {
        GLOp glop = null;
        if (parent == null || parent.accelerated) {
            // @TODO - need better mechanism for checking parent state - parent may change!
            // fix snapshot mix to not check compatible on non-parent???
//            System.out.println("Looking for GLOp");
            glop = GLOpCache.getInstance().find(op);
        }
        if (glop != null) {
            try {
                switch (inputs.length) {
                    case 0:
                        processHW(glop, op);
                        break;
                    case 1:
                        processHW(glop, op, inputs[0]);
                        break;
                    default:
                        processHW(glop, op, inputs);
                }
                accelerated = true;
                clear = false;
                return;
            } catch (Exception ex) {
                LOGGER.log(Level.SEVERE, null, ex);
            }
        }
        switch (inputs.length) {
            case 0:
                processSW(op);
                break;
            case 1:
                processSW(op, inputs[0]);
                break;
            default:
                processSW(op, inputs);
        }
        clear = false;
        accelerated = false;
    }

    private void processHW(GLOp glop, SurfaceOp op) {
        glop.process(op, this);
    }
    
    private void processHW(GLOp glop, SurfaceOp op, Surface input) {
        GLSurface in;
        if (input instanceof GLSurface) {
            in = (GLSurface) input;
            glop.process(op, this, in);
        } else {
            SurfaceOp rev = Reverse.op(op, getWritableData());
            input.process(rev);
        }
    }
    
    
    private void processHW(GLOp glop, SurfaceOp op, Surface[] inputs) {
        throw new UnsupportedOperationException();
    }
    
    GLSurfaceData getReadableData() {
        if (sd == null) {
            sd = GLSurfaceData.createSurfaceData(getWidth(), getHeight(), hasAlpha(), true);
        }
        return sd;
    }
    
    GLSurfaceData getWritableData() {
        if (sd == null) {
            sd = GLSurfaceData.createSurfaceData(getWidth(), getHeight(), hasAlpha(), true);
        } else {
            sd = sd.getUnshared();
        }
        return sd;
    }

    private void processSW(SurfaceOp op) {
        op.process(getWritableData(), EMPTY_DATA);
    }

    private void processSW(SurfaceOp op, Surface input) {
        if (input instanceof GLSurface) {
            GLSurface in = (GLSurface) input;
            op.process(getWritableData(), in.getReadableData());
        } else {
            SurfaceOp rev = Reverse.op(op, getWritableData());
            input.process(rev);
        }
    }

    private void processSW(SurfaceOp op, Surface[] inputs) {
        PixelData[] pixelInputs = new PixelData[inputs.length];
        for (int i = 0; i < inputs.length; i++) {
            if (inputs[i] instanceof GLSurface) {
                pixelInputs[i] = ((GLSurface) inputs[i]).getReadableData();
            } else {
                throw new UnsupportedOperationException("not yet implemented");
            }
        }
        op.process(getWritableData(), pixelInputs);
    }

    @Override
    public void clear() {
        release();
        clear = true;
    }

    @Override
    public boolean isClear() {
        return clear;
    }

    @Override
    public void release() {
        if (sd != null) {
//            GLRenderer.flushActive(); // is this needed now?);
            sd.release();
            sd = null;
        }
    }

    @Override
    public void copy(Surface source) {
        if (checkCompatible(source, true, true)) {
            release();
            GLSurface src = (GLSurface) source;
            if (src.sd != null) {
                sd = src.sd.acquire();
                clear = false;
            } else {
                LOGGER.fine("Copied surface has no data");
                sd = null;
                clear = true;
            }
        } else {
            process(Blit.op(), source);
        }
    }

    @Override
    public boolean checkCompatible(Surface surface, boolean checkDimensions, boolean checkAlpha) {
        if (!(surface instanceof GLSurface)) {
            return false;
        }
        if (checkDimensions && (surface.getWidth() != getWidth()
                || surface.getHeight() != getHeight())) {
            return false;
        }
        if (checkAlpha && (surface.hasAlpha() != hasAlpha())) {
            return false;
        }
//        ((GLSurface) surface).parent = this;
        return true;
    }

    @Override
    public Surface createSurface(int width, int height, boolean alpha, SurfaceCapabilities caps) {
        return new GLSurface(this, width, height, alpha);
    }
    
    
}
