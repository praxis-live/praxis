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

package net.neilcsmith.ripl.ops;

import java.awt.Rectangle;
import net.neilcsmith.ripl.ops.impl.SubPixels;
import net.neilcsmith.ripl.PixelData;
import net.neilcsmith.ripl.SurfaceOp;

/**
 *
 * @author Neil C Smith (http://neilcsmith.net)
 */
public final class Blit implements SurfaceOp {

    private static Blit defaultBlit = new Blit(Blend.NORMAL);

    private boolean identity;
    private BlendFunction blend;
    private int x;
    private int y;
    private Bounds srcRegion;

    private Blit() {
        this(Blend.NORMAL);
    }

    private Blit(BlendFunction blend) {
        if (blend == null) {
            throw new NullPointerException();
        }
        this.blend = blend;
        identity = true;
    }

    private Blit(BlendFunction blend, int x, int y) {
        this(blend, null, x, y);
    }

    private Blit(BlendFunction blend, Bounds srcRegion, int x, int y) {
        if (blend == null) {
            throw new NullPointerException();
        }
        this.blend = blend;
        this.srcRegion = srcRegion;
        this.x = x;
        this.y = y;
        identity = false;
    }

    public BlendFunction getBlendFunction() {
        return blend;
    }

    public void process(PixelData output, PixelData... inputs) {
        if (inputs.length < 1) {
            return;
        }
        if (identity) {
            blend.process(inputs[0], output);
        } else {
//            PixelData dst = SubPixels.create(output, x, y, output.getWidth() - x, output.getHeight() - y);
//            blend.process(inputs[0], dst);
            processComplex(inputs[0], output);
        }       
    }

    private void processComplex(PixelData src, PixelData dst) {
        Rectangle sRct = new Rectangle(0, 0, src.getWidth(), src.getHeight());
        int srcX = 0, srcY = 0;
        if (srcRegion != null) {
            sRct = sRct.intersection(srcRegion.asRectangle());
            if (sRct.isEmpty()) {
                return;
            }
            srcX = sRct.x;
            srcY = sRct.y;
        }
        Rectangle dRct = new Rectangle(0, 0, dst.getWidth(), dst.getHeight());
        sRct.translate(x-srcX, y-srcY);
        Rectangle intersection = dRct.intersection(sRct);
        if (intersection.isEmpty()) {
            return;
        }
        sRct.setBounds(intersection);
        dRct.setBounds(intersection);
        sRct.translate(srcX - x, srcY - y);
        SubPixels srcPD = SubPixels.create(src, sRct.x, sRct.y, sRct.width, sRct.height);
        SubPixels dstPD = SubPixels.create(dst, dRct.x, dRct.y, dRct.width, dRct.height);
        blend.process(srcPD, dstPD);
    }

    public static SurfaceOp op() {
        return defaultBlit;
    }

    public static SurfaceOp op(BlendFunction blend) {
        return new Blit(blend);
    }
    
    public static SurfaceOp op(int x, int y) {
        return new Blit(Blend.NORMAL,x,y);
    }

    public static SurfaceOp op(BlendFunction blend, int x, int y) {
        return new Blit(blend, x, y);
    }
    
    public static SurfaceOp op(BlendFunction blend, Bounds srcRegion, int x, int y) {
        return new Blit(blend, srcRegion, x, y);
    }

}
