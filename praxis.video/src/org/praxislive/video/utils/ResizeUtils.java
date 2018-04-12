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

package org.praxislive.video.utils;

import java.awt.Dimension;
import java.awt.Rectangle;

/**
 *
 * @author Neil C Smith
 */
public class ResizeUtils {
    
    private ResizeUtils() {}
    
    public static void calculateBounds(Dimension src, Dimension dest, ResizeMode mode,
            Rectangle srcBounds, Rectangle destBounds) {
        switch (mode.getType()) {
            case Crop :
                calculateCrop(src, dest, mode, srcBounds, destBounds);
                break;
            case Scale :
                calculateScale(src, dest, mode, srcBounds, destBounds);
                break;
//            case ScaleWidth :
//                calculateScaleWidth(src, dest, mode, srcBounds, destBounds);
//                break;
//            case ScaleHeight :
//                calculateScaleHeight(src, dest, mode, srcBounds, destBounds);
            case Stretch :
                calculateStretch(src, dest, mode, srcBounds, destBounds);
                break;
            default :
                throw new UnsupportedOperationException();
        }  
    }

    private static void calculateCrop(Dimension src, Dimension dest,
            ResizeMode mode, Rectangle srcBounds, Rectangle destBounds) {
        int diff = src.width - dest.width;
        if (diff == 0) {
            srcBounds.x = 0;
            destBounds.x = 0;
            srcBounds.width = src.width;
            destBounds.width = dest.width;
        } else if (diff > 0) {
            // src wider than dest
            double hAlign = mode.getHorizontalAlignment();
            srcBounds.x = (int) Math.round(diff * hAlign);
            srcBounds.width = dest.width;
            destBounds.x = 0;
            destBounds.width = dest.width;
        } else {
            // src narrower than dest
            double hAlign = mode.getHorizontalAlignment();
            destBounds.x = - (int) Math.round(diff * hAlign);
            destBounds.width = src.width;
            srcBounds.x = 0;
            srcBounds.width = src.width;
        }
        diff = src.height - dest.height;
        if (diff == 0) {
            srcBounds.y = 0;
            destBounds.y = 0;
            srcBounds.height = src.height;
            destBounds.height = dest.height;
        } else if (diff > 0) {
            // src wider than dest
            double vAlign = mode.getVerticalAlignment();
            srcBounds.y = (int) Math.round(diff * vAlign);
            srcBounds.height = dest.height;
            destBounds.y = 0;
            destBounds.height = dest.height;
        } else {
            // src narrower than dest
            double vAlign = mode.getVerticalAlignment();
            destBounds.y = - (int) Math.round(diff * vAlign);
            destBounds.height = src.height;
            srcBounds.y = 0;
            srcBounds.height = src.height;
        }
    }

    private static void calculateScale(Dimension src, Dimension dest,
            ResizeMode mode, Rectangle srcBounds, Rectangle destBounds) {
        srcBounds.x = 0;
        srcBounds.y = 0;
        srcBounds.width = src.width;
        srcBounds.height = src.height;
        double ratio = Math.min((double) dest.width / (double) src.width,
                (double) dest.height / (double) src.height);
        destBounds.width = (int) Math.round(ratio * src.width);
        destBounds.height = (int) Math.round(ratio * src.height);
        int diff = dest.width - destBounds.width;
        if (diff > 0) {
            destBounds.x = (int) Math.round(mode.getHorizontalAlignment() * diff);
        } else {
            destBounds.x = 0;
        }
        diff = dest.height - destBounds.height;
        if (diff > 0) {
            destBounds.y = (int) Math.round(mode.getVerticalAlignment() * diff);
        } else {
            destBounds.y = 0;
        }
    }

    private static void calculateScaleHeight(Dimension src, Dimension dest,
            ResizeMode mode, Rectangle srcBounds, Rectangle destBounds) {
        double ratio = (double) dest.height / (double) src.height;
        
        throw new UnsupportedOperationException("Not yet implemented");
    }

    private static void calculateScaleWidth(Dimension src, Dimension dest,
            ResizeMode mode, Rectangle srcBounds, Rectangle destBounds) {
        throw new UnsupportedOperationException("Not yet implemented");
    }


    
    // probably quicker for classes just to calculate this themselves!
    private static void calculateStretch(Dimension src, Dimension dest,
            ResizeMode mode, Rectangle srcBounds, Rectangle destBounds) {
        srcBounds.x = 0;
        srcBounds.y = 0;
        srcBounds.width = src.width;
        srcBounds.height = src.height;
        destBounds.x = 0;
        destBounds.y = 0;
        destBounds.width = dest.width;
        destBounds.height = dest.height;
        
    }

}
