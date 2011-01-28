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
package net.neilcsmith.ripl.render.sw;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.logging.Logger;
import net.neilcsmith.ripl.PixelData;
import net.neilcsmith.ripl.Surface;
import net.neilcsmith.ripl.SurfaceCapabilities;
import net.neilcsmith.ripl.SurfaceOp;
import net.neilcsmith.ripl.ops.Blit;
import net.neilcsmith.ripl.utils.ImageUtils;

/**
 *
 * @author Neil C Smith
 */
class SWSurface extends Surface {
    
    private final static Logger LOG = Logger.getLogger(SWSurface.class.getName());

    private final static SurfaceCapabilities CAPS = new SurfaceCapabilities(true);
    private final static PixelData[] EMPTY_DATA = new PixelData[0];


    private SWSurfaceData data;
    
    SWSurface(int width, int height, boolean alpha) {
        super(width, height, alpha);
    }
    
    @Override
    public void process(SurfaceOp op, Surface... inputs) {
        checkWritableData();
        switch (inputs.length) {
            case 0:
                processImpl(op);
                break;
            case 1:
                processImpl(op, inputs[0]);
                break;
            default:
                processImpl(op, inputs);
        }
    }
    
    private void checkWritableData() {
        if (data == null) {
            data = SWSurfaceData.createSurfaceData(this, getWidth(), getHeight(), hasAlpha());
        } else {
            data = data.getUnshared(this);
        }
    }

    private void processImpl(SurfaceOp op) {
        op.process(data, EMPTY_DATA);
    }

    private void processImpl(SurfaceOp op, Surface input) {
        if (input instanceof SWSurface) {
            SWSurface in = (SWSurface) input;
            if (in.data != null) {
                op.process(data, in.data);
            }
        } else {
            SurfaceOp rev = new ReverseOp(op, data);
            input.process(rev);
        }
    }

    private void processImpl(SurfaceOp op, Surface[] inputs) {
//        PixelData[] pixelInputs = new PixelData[inputs.length];
//        for (int i=0; i<inputs.length; i++) {
//            if (inputs[i] instanceof SWSurface) {
//                SWSurface in = (SWSurface) inputs[i];
//                if (in.data != null) {
//                    pixelInputs[i] = in.data;
//                } else {
//                    pixelInputs[i] = SWSurfaceData.createSurfaceData(in, i, i, true);
//                }
//
//            } else {
                throw new UnsupportedOperationException("not yet implemented");
//            }
//        }
//        op.process(data, pixelInputs);
    }



    @Override
    public void clear() {
        release();
    }

    @Override
    public boolean isClear() {
        return data == null;
    }

    @Override
    public void release() {
        if (data != null) {
            data.release(this);
            data = null;
        }
    }

    @Override
    public void copy(Surface source) {
        if (checkCompatible(source, true, true)) {
            release();
            SWSurface src = (SWSurface) source;
            if (src.data != null) {
                data = src.data.acquire(this);
            }
        } else {
            process(Blit.op(), source);
        }
    }
    

    @Override
    public boolean checkCompatible(Surface surface, boolean checkDimensions, boolean checkAlpha) {
        if (!(surface instanceof SWSurface)) {
            return false;
        }
        if (checkDimensions && (surface.getWidth() != getWidth()
                || surface.getHeight() != getHeight())) {
            return false;
        }
        if (checkAlpha && (surface.hasAlpha() != hasAlpha())) {
            return false;
        }
        return true;
    }

    @Override
    public Surface createSurface(int width, int height, boolean alpha, SurfaceCapabilities caps) {
        return new SWSurface(width, height, alpha);
    }


    void draw(Graphics2D g2d, int x, int y) {
        if (data == null) {
            return;
        }
        BufferedImage im = ImageUtils.toImage(data);
        g2d.drawImage(im, x, y, null);
    }


}
