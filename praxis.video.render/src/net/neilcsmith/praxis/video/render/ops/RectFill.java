/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012 Neil C Smith.
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

import java.awt.Color;
import java.awt.Rectangle;
import java.util.Arrays;
import net.neilcsmith.praxis.video.render.PixelData;
import net.neilcsmith.praxis.video.render.SurfaceOp;
import net.neilcsmith.praxis.video.render.rgbmath.RGBMath;

/**
 *
 * @author Neil C Smith (http://neilcsmith.net)
 */
public class RectFill implements SurfaceOp {

    private int x;
    private int y;
    private int width;
    private int height;
    private Color color;
    private BlendFunction blend;

    private RectFill(Color color, BlendFunction blend,
            int x, int y, int width, int height) {
        this.color = color;
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.blend = blend;
    }

    public void process(PixelData output, PixelData... inputs) {
        Rectangle in = new Rectangle(x, y, width, height);
        Rectangle out = new Rectangle(output.getWidth(), output.getHeight());
        Rectangle intersection = out.intersection(in);
        if (intersection.isEmpty()) {
            return;
        }
        boolean forceAlpha = false;
        int alpha = color.getAlpha();
        if (alpha == 0) {
            return;
        } else if (alpha < 255) {
            forceAlpha = true;
        }
        int c = RGBMath.premultiply(color.getRGB());
        TempData tmp = TempData.create(intersection.width, intersection.height,
                forceAlpha || output.hasAlpha());
        Arrays.fill(tmp.getData(), 0, intersection.width * intersection.height, c);
        SubPixels dst = SubPixels.create(output, intersection);
        blend.process(tmp, dst);
        tmp.release();
    }

    public static SurfaceOp op(Color color, BlendFunction blend,
            int x, int y, int width, int height) {
        if (color == null || blend == null) {
            throw new NullPointerException();
        }
        return new RectFill(color, blend, x, y, width, height);
    }

}
