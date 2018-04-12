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
@GenerateTemplate(CoreMathScale.TEMPLATE_PATH)
public class CoreMathScale extends CoreCodeDelegate {
    
    final static String TEMPLATE_PATH = "resources/math_scale.pxj";

    // PXJ-BEGIN:body
    
    @P(1) @Type.Number(def = 0) @ID("x1")
    double x1;
    @P(2) @Type.Number(def = 1) @ID("x2")
    double x2;
    @P(3) @Type.Number(def = 0) @ID("y1")
    double y1;
    @P(4) @Type.Number(def = 1) @ID("y2")
    double y2;
    
    @Out(1) Output out;
    
    @In(1) void in(double value) {
        double xMin, xMax;
        xMin = min(x1, x2);
        if (value < xMin) {
            value = xMin;
        }
        xMax = max(x1, x2);
        if (value > xMax) {
            value = xMax;
        }
        value = (value - x1) / (x2 - x1);
        value = value * (y2 - y1) + y1;
        out.send(value);
    }
    
    // PXJ-END:body
    
}
