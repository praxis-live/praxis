/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2015 Neil C Smith.
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
package net.neilcsmith.praxis.audio.code;

import java.lang.reflect.Field;
import net.neilcsmith.praxis.audio.AudioPort;
import net.neilcsmith.praxis.audio.code.userapi.AudioOut;
import net.neilcsmith.praxis.audio.impl.DefaultAudioOutputPort;
import net.neilcsmith.praxis.code.CodeConnector;
import net.neilcsmith.praxis.code.CodeContext;
import net.neilcsmith.praxis.code.PortDescriptor;
import net.neilcsmith.praxis.code.userapi.AuxOut;
import net.neilcsmith.praxis.code.userapi.Out;
import net.neilcsmith.praxis.core.Port;
import net.neilcsmith.praxis.core.info.PortInfo;
import net.neilcsmith.praxis.core.types.PMap;
import net.neilcsmith.praxis.logging.LogLevel;
import org.jaudiolibs.pipes.Buffer;

/**
 *
 * @author Neil C Smith (http://neilcsmith.net)
 */
class AudioOutPort extends DefaultAudioOutputPort {

    private final AudioOutPipe out;

    private AudioOutPort(AudioOutPipe out) {
        super(out);
        this.out = out;
    }
    
    AudioOutPipe getPipe() {
        return out;
    }

    static Descriptor createDescriptor(CodeConnector<?> connector,
            Out ann, Field field) {
        return createDescriptor(connector, PortDescriptor.Category.Out, ann.value(), field);
    }

    static Descriptor createDescriptor(CodeConnector<?> connector,
            AuxOut ann, Field field) {
        return createDescriptor(connector, PortDescriptor.Category.AuxOut, ann.value(), field);
    }

    private static Descriptor createDescriptor(CodeConnector<?> connector,
            PortDescriptor.Category category, int index, Field field) {
        if (!AudioOut.class.isAssignableFrom(field.getType())) {
            return null;
        }
        String id = connector.findID(field);
        return new Descriptor(id, category, index, field);
    }

    static class AudioOutPipe extends AudioOut {

        private float last;
        private boolean switchAndRamp;
        
        @Override
        protected void process(Buffer buffer, boolean rendering) {
            if (rendering) {
                float[] data = buffer.getData();
                int bsize = buffer.getSize();
                if (switchAndRamp) {
                    float factor = last - data[0];
                    float delta = factor / bsize;
                    for (int i = 0; i < bsize; i++) {
                        data[i] = data[i] + factor;
                        factor -= delta;
                    }
                    switchAndRamp = false;
                } else {
                    last = data[bsize - 1];
                }
            } else {
                last = 0;
                switchAndRamp = false;
            }
        }
        
        void triggerSwitch() {
            switchAndRamp = true;
        }
        
        void resetSwitch() {
            switchAndRamp = false;
            last = 0;
        }

    }

    static class Descriptor extends PortDescriptor {

        private final static PortInfo INFO = PortInfo.create(AudioPort.class, PortInfo.Direction.OUT, PMap.EMPTY);

        private final Field field;
        private AudioOutPort port;

        private Descriptor(String id,
                PortDescriptor.Category category,
                int index,
                Field field) {
            super(id, category, index);
            field.setAccessible(true);
            this.field = field;
        }

        @Override
        public void attach(CodeContext<?> context, Port previous) {
            if (previous instanceof AudioOutPort) {
                port = (AudioOutPort) previous;
            } else {
                if (previous != null) {
                    previous.disconnectAll();
                }
                port = new AudioOutPort(new AudioOutPipe());
            }
            try {
                field.set(context.getDelegate(), port.out);
            } catch (Exception ex) {
                context.getLog().log(LogLevel.ERROR, ex);
            }
        }

        @Override
        public AudioOutPort getPort() {
            return port;
        }

        @Override
        public PortInfo getInfo() {
            return INFO;
        }

    }

}
