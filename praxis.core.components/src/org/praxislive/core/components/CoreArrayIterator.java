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
@GenerateTemplate(CoreArrayIterator.TEMPLATE_PATH)
public class CoreArrayIterator extends CoreCodeDelegate {
    
    final static String TEMPLATE_PATH = "resources/array_iterator.pxj";

    // PXJ-BEGIN:body
    
    @P(1) @Type(cls = PArray.class) @OnChange("valuesChanged")
    Property values;
    @P(2) @ReadOnly
    int index;
    @P(3) @Type.Integer(min = 1, max = 1024, def = 1) @Config.Port(false)
    int minSkip;
    @P(4) @Type.Integer(min = 1, max = 1024, def = 1) @Config.Port(false)
    int maxSkip;
    @P(5) @Config.Port(false)
    boolean pingPong;
    @P(6) @Config.Port(false) @Type.Boolean(def = true)
    boolean resetOnChange;
    
    @T(2) boolean reset;
    
    @Out(1) Output out;
    
    PArray array;
    boolean forwards;
    
    @Override
    public void init() {
        extractArray();
    }

    @T(1) void trigger() {
        int count = array.size();
        boolean r = reset;
        reset = false;
        if (count == 0) {
            index = -1;
            out.send();
        } else if (count == 1) {
            index = 0;
            out.send(array.get(0));
        } else {
            if (r) {
                index = 0;
            } else {
                index = nextIdx();
            }
            out.send(array.get(index));
        }
    }
    
    void valuesChanged() {
        extractArray();
        if (resetOnChange) {
            reset = true;
        }
    }
    
    void extractArray() {
        try {
            array = PArray.coerce(values.get());
        } catch (ValueFormatException ex) {
            log(ERROR, ex, "values isn't an array");
            array = PArray.EMPTY;
        }
    }
    
    int nextIdx() {

        if (!pingPong) {
            forwards = true;
        }

        int min = minSkip;
        int max = max(min, maxSkip);
        int idx = index;
        int oldIdx = idx;
        int count = array.size();
        
        int delta;
        
        if (min == max) {
            delta = min;
        } else {
            delta = (int) random(max + 1 - min);
            delta += min;
        }

        if (forwards) {
            idx += delta;
        } else {
            idx -= delta;
        }

        while (idx < 0 || idx >= count) {
            if (pingPong) {
                if (idx < 0) {
                    idx = 0 - idx;
                    forwards = true;
                } else {
                    int hi = count - 1;
                    idx = hi - (idx - hi);
                    forwards = false;
                }
            } else {
                if (idx < 0) {
                    idx = 0 - idx;                    
                } else {
                    idx %= count;
                }
            }
        }

        // don't allow duplicates at change of direction.
        if (idx == oldIdx && min > 0) {
            if (forwards) {
                if (idx < count - 1) {
                    idx++;
                } else if (pingPong) {
                    idx--;
                    forwards = false;
                } else {
                    idx = 0;
                }
            } else {
                if (idx > 0) {
                    idx--;
                } else {
                    idx++;
                    forwards = true;
                }
            }
        }
        
        return idx;
    }
    
    // PXJ-END:body
    
}
