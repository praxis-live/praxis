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
@GenerateTemplate(VideoPlayerComponent.TEMPLATE_PATH)
public class VideoPlayerComponent extends VideoCodeDelegate {

    final static String TEMPLATE_PATH = "resources/video_player.pxj";

    // PXJ-BEGIN:body

    enum ResizeMode {Stretch, Scale, Crop};
    
    @T(1) Trigger play;
    @T(2) Trigger pause;
    @T(3) Trigger stop;
    
    @P(1) @Type(value = PResource.class, properties = {PResource.KEY_ALLOW_EMPTY, "true"})
    Property video;
    @P(2) @OnChange("seek") @Type.Number(min = 0, max = 1)
    double position;
    @P(3) @Type.Number(def = 1)
    Property rate;
    @P(4) @Type.Boolean(def = true)
    Property loop;
    @P(5)
    ResizeMode resizeMode;
    @P(6) @Type.Number(min = 0, max = 1, def = 0.5)
    double alignX;
    @P(7) @Type.Number(min = 0, max = 1, def = 0.5)
    double alignY;
    @P(8) @Type.Number(min = 0, max = 8, def = 1, skew = 4)
    double zoom;
    @P(9) @Type.String(suggested = "autoaudiosink") @Config.Port(false)
    Property audioSink;
    @P(10) @ReadOnly
    String state;
    
    @Inject VideoPlayer player;

    @AuxOut(1) Output ready;
    @AuxOut(2) Output error;
    @AuxOut(3) Output eos;
    

    @Override
    public void init() {
        video.linkAs(PResource::from, player::location);
        rate.link(player::rate);
        loop.valuesAs(PBoolean.class).link(b -> player.looping(b.value()));
        play.link(player::play);
        pause.link(player::pause);
        stop.link(player::stop);
        audioSink.linkAs(Value::toString, player::audioSink);
        player.onReady(ready::send);
        player.onError(msg -> {
            log(ERROR, msg);
            error.send(msg);
        });
        player.onEOS(eos::send);
    }

    @Override
    public void draw() {
        if (player.render(this::drawFrame)) {
            position = player.position();
        } else {
            position = 0;
        }
        state = player.state().toString();
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
    
    void seek() {
        player.position(position);
    }

    // PXJ-END:body
}
