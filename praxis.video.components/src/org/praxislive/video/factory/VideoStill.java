/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2018 Neil C Smith.
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
package org.praxislive.video.factory;

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
@GenerateTemplate(VideoCustom.TEMPLATE_PATH)
public class VideoStill extends VideoCodeDelegate {
    
    final static String TEMPLATE_PATH = "resources/custom.pxj";

    // PXJ-BEGIN:body

    @In(1) PImage in;
    
    @P(1) @OnChange("imageChanged") @OnError("imageError")
    PImage image;
    @P(2) @Type.String(allowed = {"Stretch", "Scale", "Crop"}, def = "Stretch")
    String resizeMode;
    @P(3) @Type.Number(min = 0, max = 1, def = 0.5)
    double alignX;
    @P(4) @Type.Number(min = 0, max = 1, def = 0.5)
    double alignY;
    
    @AuxOut(1) Output ready;
    @AuxOut(2) Output error;
    
    @Override
    public void draw() {
        copy(in);
        release(in);
        if (image != null) {
            switch (resizeMode) {
                case "Scale" :
                    drawScaled();
                    break;
                case "Crop" :
                    drawCropped();
                    break;
                default:
                    drawStretched();
            }
        }
    }
    
    void drawStretched() {
        image(image, 0, 0, width, height);
    }
    
    void drawScaled() {
        double oX, oY, oW, oH;
        double r = min((double) width / image.width, (double) height / image.height);
        oW = r * image.width;
        oH = r * image.height;
        double diff = width - oW;
        if (diff > 1) {
            oX = alignX * diff;
        } else {
            oX = 0;
        }
        diff = height - oH;
        if (diff > 1) {
            oY = alignY * diff;
        } else {
            oY = 0;
        }
        image(image, oX, oY, oW, oH);
    }
    
    void drawCropped() {
        if (image.width == width && image.height == height) {
            image(image,0,0);
            return;
        }
        
        double iX, iY, oX, oY, w, h;
        double diff = image.width - width;
        if (diff > 0) {
            iX = diff * alignX;
            oX = 0;
            w = width;
        } else {
            oX = -diff * alignX;
            iX = 0;
            w = image.width;
        }
        diff = image.height - height;
        if (diff > 0) {
            iY = diff * alignY;
            oY = 0;
            h = height;
        } else {
            oY = -diff * alignY;
            iY = 0;
            h = image.height;
        }
        image(image, oX, oY, w, h, iX, iY);
    }
    
    void imageChanged() {
        ready.send();
    }
    
    void imageError() {
        error.send();
    }
    
    // PXJ-END:body
    
}
