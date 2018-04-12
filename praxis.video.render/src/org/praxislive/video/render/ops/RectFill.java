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

package org.praxislive.video.render.ops;

import java.awt.Color;
import java.awt.Rectangle;
import java.util.Arrays;
import org.praxislive.video.render.PixelData;
import org.praxislive.video.render.SurfaceOp;
import org.praxislive.video.render.utils.RGBMath;

/**
 *
 * @author Neil C Smith (http://neilcsmith.net)
 */
public class RectFill implements SurfaceOp {

    private final Rectangle bounds;
    private Color color;
    private BlendMode blendMode;
    private double opacity;

    public RectFill() {
        bounds = new Rectangle();
        color = new Color(0, 0, 0, 0);
        blendMode = BlendMode.Normal;
        opacity = 1;
    }
    
    public RectFill setBounds(int x, int y, int width, int height) {
        bounds.setBounds(x, y, width, height);
        return this;
    }
    
    public RectFill setBounds(Rectangle rect) {
        bounds.setBounds(rect);
        return this;
    }
    
    public Rectangle getBounds(Rectangle rect) {
        if (rect == null) {
            return new Rectangle(bounds);
        } else {
            rect.setBounds(bounds);
            return rect;
        }
    }
    
    public RectFill setColor(Color color) {
        if (color == null) {
            throw new NullPointerException();
        }
        this.color = color;
        return this;
    }
    
    public Color getColor() {
        return color;
    }
    
    public RectFill setBlendMode(BlendMode blendMode) {
        if (blendMode == null) {
            throw new NullPointerException();
        }
        this.blendMode = blendMode;
        return this;
    }
    
    public BlendMode getBlendMode() {
        return blendMode;
    }
    
    public RectFill setOpacity(double opacity) {
        if (opacity < 0) {
            opacity = 0;
        } else if (opacity > 1) {
            opacity = 1;
        }
        this.opacity = opacity;
        return this;
    }
    
    public double getOpacity() {
        return opacity;
    }

    @Override
    public void process(PixelData output, PixelData... inputs) {
        Rectangle out = new Rectangle(output.getWidth(), output.getHeight());
        Rectangle intersection = out.intersection(bounds);
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
//        blend.process(tmp, dst);
        BlendUtil.process(tmp, dst, blendMode, opacity);
        tmp.release();
    }


}
