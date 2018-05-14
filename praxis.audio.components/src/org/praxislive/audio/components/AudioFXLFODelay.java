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
@GenerateTemplate(AudioFXLFODelay.TEMPLATE_PATH)
public class AudioFXLFODelay extends AudioCodeDelegate {
    
    final static String TEMPLATE_PATH = "resources/fx_lfo_delay.pxj";

    // PXJ-BEGIN:body
    
    @In(1) AudioIn in1;
    @In(2) AudioIn in2;
    @Out(1) AudioOut out1;
    @Out(2) AudioOut out2;
    
    @UGen LFODelay dl1, dl2;
    
    @P(1) @Type.Number(min=0, max=1)
    Property time;
    @P(2) @Type.Number(min=0, max=1)
    Property range;
    @P(3) @Type.Number(min=0, max=40, skew=2)
    Property rate;
    @P(4) @Type.Number(min=0, max=1)
    Property feedback;
    
    @Override
    public void init() {
        time.link(dl1::time, dl2::time);
        range.link(dl1::range, dl2::range);
        rate.link(dl1::rate, dl2::rate);
        feedback.link(dl1::feedback, dl2::feedback);
        link(in1, dl1, out1);
        link(in2, dl2, out2);
    }
    
    // PXJ-END:body
    
}
