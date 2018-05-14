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

import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;
import org.praxislive.video.render.PixelData;
import org.praxislive.video.render.SurfaceOp;
import org.praxislive.video.render.utils.ImageUtils;


/**
 *
 * @author Neil C Smith (http://neilcsmith.net)
 */
public class GraphicsOp implements SurfaceOp {

    private static final Image[] EMPTY = new Image[0];

    private Callback callback;

    public GraphicsOp(Callback callback) {
        if (callback == null) {
            throw new NullPointerException();
        }
        this.callback = callback;
    }

    public void process(PixelData output, PixelData... inputs) {
        BufferedImage dst = ImageUtils.toImage(output);
        Image[] srcs;
        if (inputs.length == 0) {
            srcs = EMPTY;
        } else {
            srcs = new Image[inputs.length];
            for (int i=0; i < inputs.length; i++) {
                srcs[i] = ImageUtils.toImage(inputs[i]);
            }
        }
        Graphics2D g2d = dst.createGraphics();
        callback.draw(g2d, srcs);
        g2d.dispose();
    }

    public Callback getCallback() {
        return callback;
    }

    public static interface Callback {

        public void draw(Graphics2D g2d, Image[] images);

    }

}
