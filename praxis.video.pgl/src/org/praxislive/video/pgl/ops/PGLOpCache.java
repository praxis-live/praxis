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
package org.praxislive.video.pgl.ops;

import java.util.HashMap;
import java.util.Map;
import org.praxislive.video.pgl.PGLContext;
import org.praxislive.video.render.SurfaceOp;
import org.praxislive.video.render.ops.Blit;
import org.praxislive.video.render.ops.RectFill;
import org.praxislive.video.render.ops.ScaledBlit;
import org.praxislive.video.render.ops.ShapeRender;
import org.praxislive.video.render.ops.TextRender;
import org.praxislive.video.render.ops.TransformBlit;

/**
 *
 * @author Neil C Smith <http://neilcsmith.net>
 */
public class PGLOpCache {
      
    private final PGLContext context;
    private final Map<Class<? extends SurfaceOp>, PGLOp> cache;
    
    public PGLOpCache(PGLContext context) {
        this.context = context;
        cache = new HashMap<>();
        cache.put(Blit.class, new PGLBlitOp(context));
        cache.put(ScaledBlit.class, new PGLScaledBlitOp());
        cache.put(TransformBlit.class, new PGLTransformBlitOp());
        cache.put(RectFill.class, new PGLRectFillOp());
        cache.put(ShapeRender.class, new PGLShapeRenderOp());
        cache.put(TextRender.class, new PGLTextRenderOp());
    }
        
    public PGLOp find(SurfaceOp op) {
        return cache.get(op.getClass());
    }
    
    public void dispose() {
        
    }
    
}
