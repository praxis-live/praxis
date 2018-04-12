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

// PXJ-BEGIN:imports
import org.praxislive.video.render.ops.DifferenceOp;
// PXJ-END:imports

/**
 *
 * @author Neil C Smith - http://www.neilcsmith.net
 */
@GenerateTemplate(VideoAnalysisDifference.TEMPLATE_PATH)
public class VideoAnalysisDifference extends VideoCodeDelegate {
    
    final static String TEMPLATE_PATH = "resources/analysis_difference.pxj";

    // PXJ-BEGIN:body
    
    final String COLOR = "Color";
    final String MONO = "Mono";
    final String THRESHOLD = "Threshold";
    
    @In(1) PImage in1;
    @In(2) PImage in2;

    @P(1) @Type.String(allowed = {COLOR, MONO, THRESHOLD}) @Config.Port(false)
    String mode;
    @P(2) @Type.Number(min = 0, max = 1)
    double threshold;
    
    DifferenceOp diff = new DifferenceOp();
    
    @Override
    public void draw() {
        switch (mode) {
            case COLOR:
                diff.setMode(DifferenceOp.Mode.Color);
                break;
            case MONO:
                diff.setMode(DifferenceOp.Mode.Mono);
                break;
            case THRESHOLD:
                diff.setMode(DifferenceOp.Mode.Threshold);
                break;
        }
        diff.setThreshold(threshold);
        
        copy(in1);
        release(in1);
        op(diff, in2);
        release(in2);
    }
    
    // PXJ-END:body
    
}
