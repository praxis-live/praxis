/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2014 Neil C Smith.
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
package net.neilcsmith.praxis.video.pgl.ops;

import java.util.HashMap;
import java.util.Map;
import net.neilcsmith.praxis.video.render.SurfaceOp;
import net.neilcsmith.praxis.video.render.ops.Blit;
import net.neilcsmith.praxis.video.render.ops.RectFill;
import net.neilcsmith.praxis.video.render.ops.ScaledBlit;
import net.neilcsmith.praxis.video.render.ops.ShapeRender;
import net.neilcsmith.praxis.video.render.ops.TransformBlit;

/**
 *
 * @author Neil C Smith <http://neilcsmith.net>
 */
public class PGLOpCache {
    
    private final static PGLOpCache INSTANCE = new PGLOpCache();
    
    private Map<Class<? extends SurfaceOp>, PGLOp> cache;
    
    private PGLOpCache() {
        init();
    }
    
    private void init() {
        cache = new HashMap<Class<? extends SurfaceOp>, PGLOp>();
        cache.put(Blit.class, new PGLBlitOp());
        cache.put(ScaledBlit.class, new PGLScaledBlitOp());
        cache.put(TransformBlit.class, new PGLTransformBlitOp());
        cache.put(RectFill.class, new PGLRectFillOp());
        cache.put(ShapeRender.class, new PGLShapeRenderOp());
    }
    
    
    public PGLOp find(SurfaceOp op) {
        return cache.get(op.getClass());
    }
    
    public static PGLOpCache getInstance() {
        return INSTANCE;
    }
    
    
}
