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
@GenerateTemplate(VideoXFader.TEMPLATE_PATH)
public class VideoXFader extends VideoCodeDelegate {
    
    final static String TEMPLATE_PATH = "resources/xfader.pxj";

    // PXJ-BEGIN:body
    
    enum Mode {Normal, Add, Difference, BitXor}
    
    @In(1) PImage in1;
    @In(2) PImage in2;
    
    @P(1)
    Mode mode;
    @P(2) @Type.Number(min = 0, max = 1)
    double mix;
    
    @Override
    public void init() {
        attachRenderQuery("in-1", rendering -> rendering && mix < 0.999);
        attachRenderQuery("in-2", rendering -> rendering && mix > 0.001);
    }
    
    @Override
    public void draw() {
        if (mix < 0.001) {
            copy(in1);
            release(in1);
        } else if (mix > 0.999) {
            copy(in2);
            release(in2);
        } else if (mode == Mode.Normal) {
            blendMode(ADD, 1 - mix);
            image(in1, 0, 0);
            blendMode(ADD, mix);
            image(in2, 0, 0);
        } else {
            drawBlended();
        }
    }
    
    void drawBlended() {
        PImage fg, bg;
        double opacity;
        if (mix > 0.5) {
            fg = in1;
            bg = in2;
            opacity = (1.0 - mix) * 2;
        } else {
            fg = in2;
            bg = in1;
            opacity = mix * 2;
        }
        copy(bg);
        release(bg);
        switch (mode) {
            case Difference:
                blendMode(DIFFERENCE, opacity);
                break;
            case BitXor:
                blendMode(BITXOR, opacity);
                break;
            default:
                blendMode(ADD, opacity);
                break;
        }
        image(fg, 0, 0);
    }
    
    // PXJ-END:body
    
}
