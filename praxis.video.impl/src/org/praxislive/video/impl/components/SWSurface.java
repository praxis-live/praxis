/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2018 Neil C Smith.
 * 
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License version 3 only, as
 * published by the Free Software Foundation.
 * 
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser General Public License
 * version 3 for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License version 3
 * along with this work; if not, see http://www.gnu.org/licenses/
 * 
 * 
 * Please visit https://www.praxislive.org if you need additional information or
 * have any questions.
 */
package org.praxislive.video.impl.components;

import java.awt.AlphaComposite;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.logging.Logger;
import org.praxislive.video.render.PixelData;
import org.praxislive.video.render.Surface;
import org.praxislive.video.render.SurfaceOp;
import org.praxislive.video.render.ops.Blit;
import org.praxislive.video.render.ops.Reverse;
import org.praxislive.video.render.utils.ImageUtils;

/**
 *
 * @author Neil C Smith
 */
class SWSurface extends Surface {

    private final static Logger LOG = Logger.getLogger(SWSurface.class.getName());
    private final static PixelData[] EMPTY_DATA = new PixelData[0];
    private SWSurfaceData sd;
    private boolean clear = true;
    private int modCount;

    SWSurface(int width, int height, boolean alpha) {
        super(width, height, alpha);
    }

    @Override
    public void process(SurfaceOp op, Surface... inputs) {
        modCount++;
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
        clear = false;
    }

    private SWSurfaceData getReadableData() {
        if (sd == null) {
            sd = SWSurfaceData.createSurfaceData(this, getWidth(), getHeight(), hasAlpha(), clear);
        }
        return sd;
    }

    private SWSurfaceData getWritableData() {
        if (sd == null) {
            sd = SWSurfaceData.createSurfaceData(this, getWidth(), getHeight(), hasAlpha(), clear);
        } else {
            sd = sd.getUnshared(this);
        }
        return sd;
    }

    private void processImpl(SurfaceOp op) {
        op.process(getWritableData(), EMPTY_DATA);
    }

    private void processImpl(SurfaceOp op, Surface input) {
        if (input instanceof SWSurface) {
            SWSurface in = (SWSurface) input;
            op.process(getWritableData(), in.getReadableData());
        } else {
            SurfaceOp rev = Reverse.op(op, getWritableData());
            input.process(rev);
        }
    }

    private void processImpl(SurfaceOp op, Surface[] inputs) {
        PixelData[] pixelInputs = new PixelData[inputs.length];
        for (int i = 0; i < inputs.length; i++) {
            if (inputs[i] instanceof SWSurface) {
                pixelInputs[i] = ((SWSurface) inputs[i]).getReadableData();
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
        modCount++;
        if (sd != null) {
            sd.release();
            sd = null;
        }
    }

    @Override
    public void copy(Surface source) {
        modCount++;
        if (checkCompatible(source, true, true)) {
            release();
            SWSurface src = (SWSurface) source;
            if (src.sd != null) {
                sd = src.sd.acquire();
            } else {
                clear = true;
            }
        } else {
            clear();
            process(Blit.op(), source);
        }
    }

    @Override
    public int getModCount() {
        return modCount;
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
    public Surface createSurface(int width, int height, boolean alpha) {
        return new SWSurface(width, height, alpha);
    }

    void draw(Graphics2D g2d, int x, int y, int w, int h) {
        if (sd == null) {
            g2d.setComposite(AlphaComposite.Clear);
            g2d.fillRect(x, y, w, h);
            return;
        }
        BufferedImage im = ImageUtils.toImage(sd);
        g2d.drawImage(im, x, y, w, h, null);
    }
}
