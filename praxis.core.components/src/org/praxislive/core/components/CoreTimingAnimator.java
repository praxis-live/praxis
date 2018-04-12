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
package org.praxislive.core.components;

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

/**
 *
 * @author Neil C Smith - http://www.neilcsmith.net
 */
@GenerateTemplate(CoreTimingAnimator.TEMPLATE_PATH)
public class CoreTimingAnimator extends CoreCodeDelegate {
    
    final static String TEMPLATE_PATH = "resources/timing_animator.pxj";

    // PXJ-BEGIN:body

    @P(1) @Type.Number @Transient @OnChange("toChanged")
    double to;
    @P(2) @Type.Number @OnChange("valueChanged")
    Property value;
    @P(3) @Type.Number(min = 0, max = 60, def = 0)
    double time;
    
    @Out(1) Output out;
    boolean active;

    @Override
    public void update() {
        if (value.isAnimating()) {
            out.send(d(value));
            active = true;
        } else if (active) {
            out.send(to);
            active = false;
        }
    }
    
    void toChanged() {
        value.to(to).in(time);
        active = true;
    }
    
    void valueChanged() {
        to = d(value);
        active = false;
        out.send(to);
    }
    
    // PXJ-END:body
    
}
