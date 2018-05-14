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
@GenerateTemplate(AudioLooper.TEMPLATE_PATH)
public class AudioLooper extends AudioCodeDelegate {
    
    final static String TEMPLATE_PATH = "resources/looper.pxj";

    // PXJ-BEGIN:body
    
    final double MAX_BUFFER_SECONDS = 5.0;
    
    @In(1) AudioIn in1;
    @In(2) AudioIn in2;
    
    @Out(1) AudioOut out1;
    @Out(2) AudioOut out2;
    
    @UGen Looper looper;
    
    @P(1) @OnChange("updateState")
    boolean recording;
    @P(2) @Type.Number(min=0, max=1) @Transient @OnChange("updatePosition")
    double position;
    @P(3) @Type.Number(min=0, max=1)
    Property start;
    @P(4) @Type.Number(min=0, max=1, def=1)
    Property end;
    @P(5) @Type.Number(min=-4, max=4, def=1)
    Property speed;
    @P(6) @Type.Boolean(def=true)
    Property loop;
    @P(7) @Transient @OnChange("updateState")
    boolean playing;
    @P(8) @Type.Number(min=0, max=MAX_BUFFER_SECONDS, def=MAX_BUFFER_SECONDS)
    Property loopSize;
    
    
    @Inject Ref<AudioTable> table;
    
    @Override
    public void init() {
        table.init( () -> AudioTable.generate((int) (MAX_BUFFER_SECONDS * sampleRate), 2));
        loopSize.values().link(s -> looper.table(AudioTable.wrap(table.get(), (int) (s * sampleRate))));
        start.link(looper::in);
        end.link(looper::out);
        speed.link(looper::speed);
        loop.linkAs(arg -> PBoolean.from(arg).get().value(), looper::looping);
        link(in1, looper, out1);
        link(in2, looper, out2);
        updateState();
    }

    @Override
    public void update() {
        position = looper.position();
        playing = looper.playing();
    }
    
    @T(1) void play() {
        looper.play();
    }
    
    @T(2) void stop() {
        looper.stop();
    }
    
    void updatePosition() {
        looper.position(position);
    }
    
    void updateState() {
        looper.playing(playing);
        looper.recording(recording);
    }
    
    // PXJ-END:body
    
}
