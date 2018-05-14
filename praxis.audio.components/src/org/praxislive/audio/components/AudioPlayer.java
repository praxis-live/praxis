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
package org.praxislive.audio.components;

import org.praxislive.code.GenerateTemplate;

import org.praxislive.audio.code.AudioCodeDelegate;

// default imports
import java.util.*;
import java.util.function.*;
import java.util.stream.*;
import org.praxislive.core.*;
import org.praxislive.core.types.*;
import org.praxislive.code.userapi.*;
import static org.praxislive.code.userapi.Constants.*;
import org.praxislive.audio.code.userapi.*;
import static org.praxislive.audio.code.userapi.AudioConstants.*;

/**
 *
 * @author Neil C Smith - http://www.neilcsmith.net
 */
@GenerateTemplate(AudioPlayer.TEMPLATE_PATH)
public class AudioPlayer extends AudioCodeDelegate {
    
    final static String TEMPLATE_PATH = "resources/player.pxj";

    // PXJ-BEGIN:body
    
    @Out(1) AudioOut out1;
    @Out(2) AudioOut out2;
    
    @UGen Player pl;
    
    @P(1) @OnChange("sampleChanged") @OnError("sampleError")
    AudioTable sample;
    @P(2) @Type.Number(min=0, max=1) @Transient @OnChange("updatePosition")
    double position;
    @P(3) @Type.Number(min=0, max=1)
    Property start;
    @P(4) @Type.Number(min=0, max=1, def=1)
    Property end;
    @P(5) @Type.Number(min=-4, max=4, def=1)
    Property speed;
    @P(6) @Type.Boolean
    Property loop;
    @P(7) @Transient @OnChange("updatePlaying")
    boolean playing;
    
    @AuxOut(1) Output ready;
    @AuxOut(2) Output error;
    
    @Override
    public void init() {
        pl.table(sample);
        start.link(pl::in);
        end.link(pl::out);
        speed.link(pl::speed);
        loop.linkAs(arg -> PBoolean.from(arg).get().value(), pl::looping);
        link(pl, out1);
        link(pl, out2);
    }

    @Override
    public void update() {
        position = pl.position();
        playing = pl.playing();
    }
    
    @T(1) void play() {
        pl.play();
    }
    
    @T(2) void stop() {
        pl.stop();
    }
    
    void updatePosition() {
        pl.position(position);
    }
    
    void updatePlaying() {
        pl.playing(playing);
    }
    
    void sampleChanged() {
        pl.table(sample);
        ready.send();
    }
    
    void sampleError() {
        error.send();
    }

    
    // PXJ-END:body
    
}
