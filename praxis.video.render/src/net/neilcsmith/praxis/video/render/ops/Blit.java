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
package net.neilcsmith.praxis.video.render.ops;

import java.awt.Rectangle;
import net.neilcsmith.praxis.video.render.PixelData;
import net.neilcsmith.praxis.video.render.SurfaceOp;

/**
 *
 * @author Neil C Smith (http://neilcsmith.net)
 */
public final class Blit implements SurfaceOp {
    
    private final Rectangle srcRegion = new Rectangle();

    private BlendMode blendMode = BlendMode.Normal;
    private double opacity = 1;
    private int x = 0;
    private int y = 0;
    private boolean hasRegion = false;


    public Blit setX(int x) {
        this.x = x;
        return this;
    }

    public int getX() {
        return x;
    }

    public Blit setY(int y) {
        this.y = y;
        return this;
    }

    public int getY() {
        return y;
    }

    public Blit setSourceRegion(Rectangle rect) {
        if (rect == null) {
            hasRegion = false;
        } else {
            hasRegion = true;
            srcRegion.setBounds(rect);
        }
        return this;
    }

    public Blit setSourceRegion(int x, int y, int width, int height) {
        hasRegion = true;
        srcRegion.setBounds(x, y, width, height);
        return this;
    }
    
    public Rectangle getSourceRegion(Rectangle rect) {
        if (hasRegion) {
            if (rect == null) {
                rect = new Rectangle(srcRegion);
            } else {
                rect.setBounds(srcRegion);
            }
            return rect;
        } else {
            return null;
        }
    }
    
    public boolean hasSourceRegion() {
        return hasRegion;
    }

    public Blit setBlendMode(BlendMode blendMode) {
        if (blendMode == null) {
            throw new NullPointerException();
        }
        this.blendMode = blendMode;
        return this;
    }

    public BlendMode getBlendMode() {
        return blendMode;
    }

    public Blit setOpacity(double opacity) {
        if (opacity < 0 || opacity > 1) {
            throw new IllegalArgumentException();
        }
        this.opacity = opacity;
        return this;
    }

    public double getOpacity() {
        return opacity;
    }

    @Override
    public void process(PixelData output, PixelData... inputs) {
        if (inputs.length < 1) {
            return;
        }
        if (!hasRegion && x == 0 && y == 0) {
//            blend.process(inputs[0], output);
            BlendUtil.process(inputs[0], output, blendMode, opacity);
        } else {
//            PixelData dst = SubPixels.create(output, x, y, output.getWidth() - x, output.getHeight() - y);
//            blend.process(inputs[0], dst);
            processComplex(inputs[0], output);
        }
    }

    private void processComplex(PixelData src, PixelData dst) {
        Rectangle sRct = new Rectangle(0, 0, src.getWidth(), src.getHeight());
        int srcX = 0, srcY = 0;
        if (hasRegion) {
            sRct = sRct.intersection(srcRegion);
            if (sRct.isEmpty()) {
                return;
            }
            srcX = sRct.x;
            srcY = sRct.y;
        }
        Rectangle dRct = new Rectangle(0, 0, dst.getWidth(), dst.getHeight());
        sRct.translate(x - srcX, y - srcY);
        Rectangle intersection = dRct.intersection(sRct);
        if (intersection.isEmpty()) {
            return;
        }
        sRct.setBounds(intersection);
        dRct.setBounds(intersection);
        sRct.translate(srcX - x, srcY - y);
        SubPixels srcPD = SubPixels.create(src, sRct.x, sRct.y, sRct.width, sRct.height);
        SubPixels dstPD = SubPixels.create(dst, dRct.x, dRct.y, dRct.width, dRct.height);
//        blend.process(srcPD, dstPD);
        BlendUtil.process(srcPD, dstPD, blendMode, opacity);
    }
    
    @Deprecated
    public static SurfaceOp op() {
        return new Blit();
    }

    @Deprecated
    public static SurfaceOp op(BlendFunction blend) {
        Blit blit = new Blit();
        blit.setBlendMode(extractBlendMode(blend));
        blit.setOpacity(((Blend) blend).getExtraAlpha());
        return blit;
    }

    @Deprecated
    public static SurfaceOp op(int x, int y) {
        return new Blit().setX(x).setY(y);
    }

    @Deprecated
    public static SurfaceOp op(BlendFunction blend, int x, int y) {
        Blit blit = new Blit().setX(x).setY(y);
        blit.setBlendMode(extractBlendMode(blend));
        blit.setOpacity(((Blend) blend).getExtraAlpha());
        return blit;
    }

    @Deprecated
    public static SurfaceOp op(BlendFunction blend, Bounds srcRegion, int x, int y) {
        return new Blit().setX(x).setY(y)
                .setBlendMode(extractBlendMode(blend))
                .setOpacity(((Blend)blend).getExtraAlpha())
                .setSourceRegion(srcRegion.asRectangle());
    }

    static BlendMode extractBlendMode(BlendFunction blend) {
        Blend.Type type = ((Blend) blend).getType();
        switch (type) {
            case Add:
                return BlendMode.Add;
            case BitXor:
                return BlendMode.BitXor;
            case Difference:
                return BlendMode.Difference;
            case Mask:
                return BlendMode.Mask;
            case Multiply:
                return BlendMode.Multiply;
            case Normal:
                return BlendMode.Normal;
            case Screen:
                return BlendMode.Screen;
            case Sub:
                return BlendMode.Sub;
        }
        throw new UnsupportedOperationException("Can't convert blend function");
    }
}
