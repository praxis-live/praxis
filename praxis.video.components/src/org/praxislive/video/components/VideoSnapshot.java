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
@GenerateTemplate(VideoSnapshot.TEMPLATE_PATH)
public class VideoSnapshot extends VideoCodeDelegate {
    
    final static String TEMPLATE_PATH = "resources/snapshot.pxj";

    // PXJ-BEGIN:body

    final double LOW = 0.000001;
    final double HIGH = 0.999999;
    
    @In(1) PImage in;
    
    @P(1) @Type.Number(min=0, max=60, def=0)
    double fadeTime;
    @P(2) @Type.Number(min=0, max=1, def=1)
    double mix;
    @P(100) @ReadOnly
    Property activeMix;
    
    @T(1) Trigger trigger;
    @T(2) Trigger reset;
    
    @OffScreen PGraphics fg, bg, scratch;

    
    @Override
    public void setup() {

    }

    @Override
    public void draw() {
        if (reset.poll()) {
            reset();
        }
        
        if (trigger.poll()) {
            capture();
        } else {
            render();
        }
        
    }

    void capture() {
        double am = d(activeMix);
        if (am > LOW) {
            if (am > HIGH) {
                swapFgBg();
            } else {
                mix(fg, bg, am);
            }
            activeMix.set(0);
        }
        fg.clear();
        if (fadeTime < LOW) {
            if (mix > HIGH) {
                bg.copy(in);
                copy(in);
                release(in);
            } else {
                mix(in, bg, mix);
                copy(bg);       
            }
        } else {
            fg.copy(in);
            copy(bg);
            activeMix.set(0).to(mix).in(fadeTime);
        }
        
    }
    
    void render() {
        double am = d(activeMix);
        if (am > LOW) {
            if (activeMix.isAnimating()) {
                blendMode(ADD, 1 - am);
                image(bg, 0, 0);
                blendMode(ADD, am);
                image(fg, 0, 0);
            } else {
                if (am > HIGH) {
                    swapFgBg();
                } else {
                    mix(fg, bg, am);
                }
                copy(bg);
                activeMix.set(0);
            }
        } else {
            copy(bg);
        }
    }
    
    void mix(PImage src, PGraphics dstIn, double amount) {
        scratch.beginDraw();
        scratch.blendMode(ADD, 1 - amount);
        scratch.image(dstIn, 0, 0);
        scratch.blendMode(ADD, amount);
        scratch.image(src, 0, 0);
        scratch.endDraw();
        dstIn.copy(scratch);
        scratch.clear();
    }
    
    void reset() {
        fg.clear();
        bg.clear();
        activeMix.set(0);
    }
    
    void swapFgBg() {
        PGraphics tmp = fg;
        fg = bg;
        bg = tmp;
    }
    
    // PXJ-END:body
    
}
