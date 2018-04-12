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
package org.praxislive.video.gstreamer.components;

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
import org.praxislive.video.gstreamer.*;
// PXJ-END:imports

/**
 *
 * @author Neil C Smith - http://www.neilcsmith.net
 */
@GenerateTemplate(VideoCaptureComponent.TEMPLATE_PATH)
public class VideoCaptureComponent extends VideoCodeDelegate {

    final static String TEMPLATE_PATH = "resources/video_capture.pxj";

    // PXJ-BEGIN:body

    enum ResizeMode {Stretch, Scale, Crop};
    
    @T(1) Trigger play;
    @T(2) Trigger stop;
    
    @P(1) @Type.String(emptyIsDefault = true, suggested = {"", "1", "2", "3", "4"})
    Property device;
    @P(2)
    ResizeMode resizeMode;
    @P(3) @Type.Number(min = 0, max = 1, def = 0.5)
    double alignX;
    @P(4) @Type.Number(min = 0, max = 1, def = 0.5)
    double alignY;
    @P(5) @Type.Number(min = 0, max = 8, def = 1, skew = 4)
    double zoom;
    @P(6) @Config.Port(false)
    Property sourceWidth;
    @P(7) @Config.Port(false)
    Property sourceHeight;
    @P(8) @Config.Port(false)
    Property sourceFps;
    @P(9) @ReadOnly
    String state;
    
    @Inject VideoCapture capture;

    @AuxOut(1) Output ready;
    @AuxOut(2) Output error;
    @AuxOut(3) Output eos;
    

    @Override
    public void init() {
        device.linkAs(Value::toString, capture::device);
        play.link(() -> {
            capture.requestFrameSize(i(sourceWidth), i(sourceHeight))
                    .requestFrameRate(d(sourceFps))
                    .play();
        });
        stop.link(capture::stop);
        capture.onReady(ready::send);
        capture.onError(msg -> {
            log(ERROR, msg);
            error.send(msg);
        });
        capture.onEOS(eos::send);
    }

    @Override
    public void draw() {
        capture.render(this::drawFrame);
        state = capture.state().toString();
    }

    void drawFrame(PImage frame) {
        double outWidth = zoom * frame.width;
        double outHeight = zoom * frame.height;
        if (resizeMode == ResizeMode.Stretch) {
            outWidth *= (double) width / frame.width;
            outHeight *= (double) height / frame.height;
        } else if (resizeMode == ResizeMode.Scale) {
            double r = min((double) width / frame.width, (double) height / frame.height);
            outWidth *= r;
            outHeight *= r;
        }
        image(frame, alignX * (width - outWidth),
                alignY * (height - outHeight),
                outWidth,
                outHeight);
    }
    
    // PXJ-END:body
}
