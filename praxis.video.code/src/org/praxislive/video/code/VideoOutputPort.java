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
 *
 */
package org.praxislive.video.code;

import org.praxislive.code.CodeContext;
import org.praxislive.code.PortDescriptor;
import org.praxislive.core.Port;
import org.praxislive.core.PortInfo;
import org.praxislive.core.types.PMap;
import org.praxislive.video.DefaultVideoOutputPort;
import org.praxislive.video.VideoPort;
import org.praxislive.video.pipes.VideoPipe;
import org.praxislive.video.pipes.impl.Placeholder;

/**
 *
 * @author Neil C Smith <http://neilcsmith.net>
 */
class VideoOutputPort extends DefaultVideoOutputPort {
    
    private Placeholder pipe;
    
    private VideoOutputPort(Placeholder pipe) {
        super(pipe);
        this.pipe = pipe;
    }
     
    VideoPipe getPipe() {
        return pipe;
    }
    
    
    static class Descriptor extends PortDescriptor {
        
        private final static PortInfo INFO = PortInfo.create(VideoPort.class, PortInfo.Direction.OUT, PMap.EMPTY);
        
        private VideoOutputPort port;
        
        Descriptor(String id, int index) {
            super(id, Category.Out, index);
        }

        @Override
        public void attach(CodeContext<?> context, Port previous) {
            if (previous instanceof VideoOutputPort) {
                VideoOutputPort vip = (VideoOutputPort) previous;
                if (vip.pipe.getSourceCount() == 1) {
                    vip.pipe.removeSource(vip.pipe.getSource(0));
                }
                port = vip;
            } else {
                if (previous != null) {
                    previous.disconnectAll();
                }
                port = new VideoOutputPort(new Placeholder());
            }
        }

        @Override
        public VideoOutputPort getPort() {
            return port;
        }

        @Override
        public PortInfo getInfo() {
            return INFO;
        }
        
    }
    
}
