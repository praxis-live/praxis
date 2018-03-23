/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2018 Neil C Smith.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 3 only, as
 * published by the Free Software Foundation.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 3 for more details.
 *
 * You should have received a copy of the GNU General Public License version 3
 * along with this work; if not, see http://www.gnu.org/licenses/
 *
 *
 * Please visit http://neilcsmith.net if you need additional information or
 * have any questions.
 */
package org.praxislive.tracker.impl;

import org.praxislive.code.GenerateTemplate;

import org.praxislive.core.code.CoreCodeDelegate;

// default imports
import java.util.*;
import java.util.function.*;
import java.util.stream.*;
import org.praxislive.core.*;
import org.praxislive.core.types.*;
import org.praxislive.code.userapi.*;
import static org.praxislive.code.userapi.Constants.*;

// PXJ-BEGIN:imports
import org.praxislive.tracker.*;
// PXJ-END:imports

/**
 *
 * @author Neil C Smith - http://www.neilcsmith.net
 */
@GenerateTemplate(CoreTracker.TEMPLATE_PATH)
public class CoreTracker extends CoreCodeDelegate {
    
    final static String TEMPLATE_PATH = "resources/core_tracker.pxj";

    // PXJ-BEGIN:body
    
    @P(1) Patterns patterns;
    @P(2) @Type.Integer(min = 0)
    int pattern;
    @P(3) @Type.Integer(min = 0) @Transient
    int position;

    @Out(1) Output out1;
    @Out(2) Output out2;
    @Out(3) Output out3;
    @Out(4) Output out4;
    @Out(5) Output out5;
    @Out(6) Output out6;
    @Out(7) Output out7;
    @Out(8) Output out8;

    Output[] outs;

    @Override
    public void init() {
        outs = new Output[]{out1, out2, out3, out4, out5, out6, out7, out8};
    }

    @Override
    public void starting() {
        position = 0;
    }

    @T(1)
    void trigger() {
        Pattern p;
        if (pattern >= patterns.getPatternCount()) {
            position = 0;
            return;
        }
        p = patterns.getPattern(pattern);
        position %= p.getRowCount();
        int max = min(p.getColumnCount(), outs.length);
        for (int i = 0; i < max; i++) {
            Value arg = p.getValueAt(position, i);
            if (arg != null) {
                outs[i].send(arg);
            }
        }
        position++;
        position %= p.getRowCount();
    }

    @T(2)
    void reset() {
        position = 0;
    }
    
    // PXJ-END:body
    
}
