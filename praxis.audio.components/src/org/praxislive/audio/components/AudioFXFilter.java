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
@GenerateTemplate(AudioFXFilter.TEMPLATE_PATH)
public class AudioFXFilter extends AudioCodeDelegate {
    
    final static String TEMPLATE_PATH = "resources/fx_filter.pxj";

    // PXJ-BEGIN:body
    
    @In(1) AudioIn in1;
    @In(2) AudioIn in2;
    @Out(1) AudioOut out1;
    @Out(2) AudioOut out2;
    
    @UGen IIRFilter f1, f2;
    
    @P(1) @Type.String(allowed = {"LP6", "LP12", "HP12", "BP12", "NP12", "LP24", "HP24"})
    Property type;
    @P(2) @Type.Number(min=20, max=20000, def=20000, skew=2)
    Property frequency;
    @P(3) @Type.Number(min=0, max=30, skew=2)
    Property resonance;
    
    @Override
    public void init() {
        type.linkAs(arg -> IIRFilter.Type.valueOf(arg.toString()), type -> {
            f1.type(type);
            f2.type(type);
        });
        frequency.link(f1::frequency, f2::frequency);
        resonance.link(f1::resonance, f2::resonance);
        link(in1, f1, out1);
        link(in2, f2, out2);
    }
    
    // PXJ-END:body
    
}
