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
 *
 */

package org.praxislive.midi.components;

import java.util.logging.Logger;
import javax.sound.midi.ShortMessage;
import org.praxislive.core.Value;
import org.praxislive.core.Call;
import org.praxislive.core.ComponentAddress;
import org.praxislive.core.Control;
import org.praxislive.core.ControlAddress;
import org.praxislive.core.PacketRouter;
import org.praxislive.core.ArgumentInfo;
import org.praxislive.core.ControlInfo;
import org.praxislive.core.types.PMap;
import org.praxislive.core.types.PNumber;
import org.praxislive.core.types.PString;
import org.praxislive.impl.ArgumentProperty;
import org.praxislive.impl.NumberProperty;
import org.praxislive.impl.IntProperty;
import org.praxislive.midi.impl.AbstractMidiInComponent;

/**
 *
 * @author Neil C Smith (http://neilcsmith.net)
 */
public class MidiControlIn extends AbstractMidiInComponent {

    private final static Logger LOG = Logger.getLogger(MidiControlIn.class.getName());
    
    private int channel;
    private int controller;
    private double minimum;
    private double maximum;
    private ControlAddress binding;
    private ControlAddress returnAddress;

    public MidiControlIn() {
        build();
    }

    private void build() {
        channel = 0;
        IntProperty ch = IntProperty.create(new IntProperty.Binding() {

            public void setBoundValue(long time, int value) {
                channel = value - 1;
            }

            public int getBoundValue() {
                return channel + 1;
            }
        }, 1, 16, channel + 1);
        controller = 0;
        IntProperty ctl = IntProperty.create(new IntProperty.Binding() {

            public void setBoundValue(long time, int value) {
                controller = value;
            }

            public int getBoundValue() {
                return controller;
            }
        }, 0, 127, controller);
        minimum = 0;
        NumberProperty min = NumberProperty.create(new NumberProperty.Binding() {

            public void setBoundValue(long time, double value) {
                minimum = value;
            }

            public double getBoundValue() {
                return minimum;
            }
        }, minimum);
        maximum = 1;
        NumberProperty max = NumberProperty.create(new NumberProperty.Binding() {

            public void setBoundValue(long time, double value) {
                maximum = value;
            }

            public double getBoundValue() {
                return maximum;
            }
        }, maximum);
        ArgumentProperty bd = ArgumentProperty.create(
                ArgumentInfo.create(ControlAddress.class,
                    PMap.create(ArgumentInfo.KEY_ALLOW_EMPTY, true)), 
                new AddressBinding(),
                PString.EMPTY);
        registerControl("channel", ch);
        registerControl("controller", ctl);
        registerControl("minimum", min);
        registerControl("maximum", max);
        registerControl("binding", bd);
        registerControl("_log", new LogControl());
    }

    @Override
    public void hierarchyChanged() {
        super.hierarchyChanged();
        ComponentAddress c = getAddress();
        if (c == null) {
            returnAddress = null;
        } else {
            returnAddress = ControlAddress.create(c, "_log");
        }
    }



    public void midiReceived(ShortMessage msg, long time) {
        if (msg.getChannel() == channel && msg.getCommand() == ShortMessage.CONTROL_CHANGE
                && msg.getData1() == controller && binding != null) {
            Call call = Call.createQuietCall(binding, returnAddress, time, parseArgument(msg.getData2()));
            getPacketRouter().route(call);

        }
    }

    private PNumber parseArgument(int value) {
        double min = minimum;
        double max = maximum;
        if (min == 0) {
            if (max == 127) {
                return PNumber.valueOf(value);
            } else {
                double val = (value / 127.0) * max;
                return PNumber.valueOf(val);
            }
        } else {
            double val = ((value / 127.0) * (max - min)) + min;
            return PNumber.valueOf(val);
        }
    }


    private class AddressBinding implements ArgumentProperty.Binding {

        public void setBoundValue(long time, Value value) throws Exception {
            if (value.isEmpty()) {
                binding = null;
            } else {
                binding = ControlAddress.coerce(value);
            }
        }

        public Value getBoundValue() {
            if (binding == null) {
                return PString.EMPTY;
            } else {
                return binding;
            }
        }

    }

    private class LogControl implements Control {

        public void call(Call call, PacketRouter router) throws Exception {
            if (call.getType() != Call.Type.RETURN) {
                LOG.warning(call.toString());
            }
        }

        public ControlInfo getInfo() {
            return null;
        }

    }

}
