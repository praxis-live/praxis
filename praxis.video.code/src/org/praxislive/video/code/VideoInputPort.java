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

import java.lang.reflect.Field;
import java.util.Objects;
import java.util.function.UnaryOperator;
import org.praxislive.code.CodeContext;
import org.praxislive.code.PortDescriptor;
import org.praxislive.core.Port;
import org.praxislive.core.PortInfo;
import org.praxislive.core.types.PMap;
import org.praxislive.video.DefaultVideoInputPort;
import org.praxislive.video.VideoPort;
import org.praxislive.video.pipes.VideoPipe;
import org.praxislive.video.pipes.impl.Placeholder;
import org.praxislive.video.render.Surface;

/**
 *
 * @author Neil C Smith <http://neilcsmith.net>
 */
class VideoInputPort extends DefaultVideoInputPort {
    
    private final static UnaryOperator<Boolean> DEFAULT_QUERY = b -> b; 
    
    private QueryPlaceholder pipe;
    
    private VideoInputPort() {
        this(new QueryPlaceholder());
    }
    
    private VideoInputPort(QueryPlaceholder pipe) {
        super(pipe);
        this.pipe = pipe;
    }
     
    VideoPipe getPipe() {
        return pipe;
    }
    
    static class QueryPlaceholder extends Placeholder {
        
        UnaryOperator<Boolean> query = DEFAULT_QUERY;

        @Override
        protected boolean isRenderRequired(VideoPipe source, long time) {
            return query.apply(super.isRenderRequired(source, time));
        }
        
    }
       
    static class Descriptor extends PortDescriptor {
        
        private final static PortInfo INFO = PortInfo.create(VideoPort.class, PortInfo.Direction.IN, PMap.EMPTY);
        
        private VideoInputPort port;
        private Field field;
        private UnaryOperator<Boolean> alphaQuery = DEFAULT_QUERY;
        
        Descriptor(String id, int index) {
            this(id, index, null);
        }
        
        Descriptor(String id, int index, Field field) {
            super(id, Category.In, index);
            this.field = field;
            field.setAccessible(true);
        }

        @Override
        public void attach(CodeContext<?> context, Port previous) {
            if (previous instanceof VideoInputPort) {
                VideoInputPort vip = (VideoInputPort) previous;
                if (vip.pipe.getSinkCount() == 1) {
                    vip.pipe.getSink(0).removeSource(vip.pipe);
                }
                port = vip;
            } else {
                if (previous != null) {
                    previous.disconnectAll();
                }
                port = new VideoInputPort();
            }
        }

        @Override
        public VideoInputPort getPort() {
            return port;
        }

        @Override
        public PortInfo getInfo() {
            return INFO;
        }

        @Override
        public void reset(boolean full) {
            alphaQuery = DEFAULT_QUERY;
            port.pipe.query = DEFAULT_QUERY;
        }
        
        Field getField() {
            return field;
        }
        
        Surface validateSurface(Surface in, Surface out) {
            boolean requiresAlpha = alphaQuery.apply(out.hasAlpha());
            if (in == null ||
                    !out.checkCompatible(in, true, false) ||
                    in.hasAlpha() != requiresAlpha) {
                return out.createSurface(out.getWidth(), out.getHeight(), requiresAlpha);
            } else {
                return in;
            }
        }
        
        void attachAlphaQuery(UnaryOperator<Boolean> alphaQuery) {
            this.alphaQuery = Objects.requireNonNull(alphaQuery);
        }
        
        void attachRenderQuery(UnaryOperator<Boolean> renderQuery) {
            port.pipe.query = Objects.requireNonNull(renderQuery);
        }
        
    }
    
}
