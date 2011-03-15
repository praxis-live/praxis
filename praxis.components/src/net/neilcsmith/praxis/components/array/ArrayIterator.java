/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2010 Neil C Smith.
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
package net.neilcsmith.praxis.components.array;

import java.util.Random;
import net.neilcsmith.praxis.core.ControlPort;
//import net.neilcsmith.praxis.core.impl.MultiArgProperty;
import net.neilcsmith.praxis.core.Port;
import net.neilcsmith.praxis.core.types.PArray;
import net.neilcsmith.praxis.core.types.PString;
import net.neilcsmith.praxis.impl.AbstractComponent;
import net.neilcsmith.praxis.impl.ArrayProperty;
import net.neilcsmith.praxis.impl.BooleanProperty;
import net.neilcsmith.praxis.impl.DefaultControlOutputPort;
import net.neilcsmith.praxis.impl.IntProperty;
import net.neilcsmith.praxis.impl.TriggerControl;

/**
 *
 * @author Neil C Smith
 */
//@TODO add reset control / port
public class ArrayIterator extends AbstractComponent {

    private static enum LoopMode {

        None, Forward, Backward, BiDi
    };
    private PArray values;
    private ControlPort.Output output;
    private Random random = new Random();
    private int index = 0;
    private IntProperty minSkip;
    private IntProperty maxSkip;
    private BooleanProperty pingPong;
    private boolean forwards;

    public ArrayIterator() {
        build();
    }

    private void build() {
        values = PArray.EMPTY;
        ArrayProperty vals =  ArrayProperty.create( new ArrayProperty.Binding() {

            public void setBoundValue(long time, PArray value) {
                values = value;
                index = 0;
                forwards = true;
            }

            public PArray getBoundValue() {
                return values;
            }
        }, values);
        registerControl("values", vals);
        registerPort("values", vals.createPort());
        minSkip = IntProperty.create( 0, 1024, 1);
        registerControl("min-skip", minSkip);
        maxSkip = IntProperty.create( 0, 1024, 1);
        registerControl("max-skip", maxSkip);
        pingPong = BooleanProperty.create(this, false);
        registerControl("ping-pong", pingPong);
        TriggerControl trigger = TriggerControl.create( new TriggerControl.Binding() {

            public void trigger(long time) {
                send(time);
            }
        });
        registerControl("trigger", trigger);
        registerPort("trigger", trigger.createPort());
        output = new DefaultControlOutputPort(this);
        registerPort(Port.OUT, output);
    }

    private void send(long time) {
        int count = values.getSize();
        if (count == 0) {
            output.send(time, PString.EMPTY);
        } else if (count == 1) {
            output.send(time, values.get(0));
        } else {
            output.send(time, values.get(index));
            nextIdx();
        }
    }

    private int nextIdx() {

        boolean pp = pingPong.getValue();
        if (!pp) {
            forwards = true;
        }

        int min = minSkip.getValue();
        int max = Math.max(min, maxSkip.getValue());
        int idx = index;
        int oldIdx = idx;
        int count = values.getSize();
        
        int delta;
        
        if (min == max) {
            delta = min;
        } else {
            delta = random.nextInt(max + 1 - min) + min;
        }

        if (forwards) {
            idx += delta;
        } else {
            idx -= delta;
        }

        
        while (idx < 0 || idx >= count) {
            if (pp) {
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
                } else if (pp) {
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
        
        index = idx;
        
        return index;
    }
}
