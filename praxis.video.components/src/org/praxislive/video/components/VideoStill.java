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
package org.praxislive.video.components;

import org.praxislive.code.GenerateTemplate;

import org.praxislive.video.code.VideoCodeDelegate;

// default imports
import java.util.*;
import java.util.function.*;
import java.util.stream.*;
import org.praxislive.core.*;
import org.praxislive.core.types.*;
import org.praxislive.code.userapi.*;
import static org.praxislive.code.userapi.Constants.*;
import org.praxislive.video.code.userapi.*;
import static org.praxislive.video.code.userapi.VideoConstants.*;

/**
 *
 * @author Neil C Smith - http://www.neilcsmith.net
 */
@GenerateTemplate(VideoStill.TEMPLATE_PATH)
public class VideoStill extends VideoCodeDelegate {
    
    final static String TEMPLATE_PATH = "resources/still.pxj";

    // PXJ-BEGIN:body

    enum ResizeMode {Stretch, Scale, Crop};
    
    @In(1) PImage in;
    
    @P(1) @OnChange("imageChanged") @OnError("imageError")
    PImage image;
    @P(2)
    ResizeMode resizeMode;
    @P(3) @Type.Number(min = 0, max = 1, def = 0.5)
    double alignX;
    @P(4) @Type.Number(min = 0, max = 1, def = 0.5)
    double alignY;
    @P(5) @Type.Number(min = 0, max = 8, def = 1, skew = 4)
    double zoom;
    
    @AuxOut(1) Output ready;
    @AuxOut(2) Output error;
    
    @Override
    public void draw() {
        copy(in);
        release(in);
        if (image != null) {
            draw(image);
        }
    }
    
    void draw(PImage image) {
        double outWidth = zoom * image.width;
        double outHeight = zoom * image.height;
        if (resizeMode == ResizeMode.Stretch) {
            outWidth *= (double) width / image.width;
            outHeight *= (double) height / image.height;
        } else if (resizeMode == ResizeMode.Scale) {
            double r = min((double) width / image.width, (double) height / image.height);
            outWidth *= r;
            outHeight *= r;
        }
        image(image, alignX * (width - outWidth),
                alignY * (height - outHeight),
                outWidth,
                outHeight);
    }
    
    void imageChanged() {
        ready.send();
    }
    
    void imageError() {
        error.send();
    }
    
    // PXJ-END:body
    
}
