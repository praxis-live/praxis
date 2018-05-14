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
@GenerateTemplate(AudioClock.TEMPLATE_PATH)
public class AudioClock extends AudioCodeDelegate {
    
    final static String TEMPLATE_PATH = "resources/clock.pxj";

    // PXJ-BEGIN:body

    @P(1)
    @Type.Number(min = 10, max = 300, def = 120)
    @Config.Port(false)
    @OnChange("updatePulse")
    double bpm;
    @P(2)
    @Type.Integer(min=1,max=16,def=4)
    @OnChange("updatePulse")
    int subdivision;
    @P(3)
    @ReadOnly
    double actualBpm;
    @P(4)
    @ReadOnly
    double period;
    @P(5)
    @ReadOnly
    int bufferCount;
    
    @Out(1) Output out;
    
    @Inject int position; 

    @Override
    public void init() {
        updatePulse();
    }

    @Override
    public void update() {
        position++;
        if (position > bufferCount) {
            position = 0;
            out.send();
        }
    }
    
    void updatePulse() {
        double secPerPulse = 1 / ((bpm * subdivision) / 60);
        double bufPerPulse = secPerPulse / (blockSize / sampleRate);
        bufferCount = (int) (bufPerPulse + 0.5);
        period = bufferCount * (blockSize / sampleRate);
        actualBpm = 60 / subdivision * (1 / period);
    }
    
    // PXJ-END:body
    
}
