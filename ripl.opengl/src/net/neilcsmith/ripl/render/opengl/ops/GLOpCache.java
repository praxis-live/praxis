/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2011 Neil C Smith.
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
package net.neilcsmith.ripl.render.opengl.ops;

import java.util.HashMap;
import java.util.Map;
import net.neilcsmith.ripl.SurfaceOp;
import net.neilcsmith.ripl.ops.Blit;

/**
 *
 * @author Neil C Smith <http://neilcsmith.net>
 */
public class GLOpCache {
    
    private final static GLOpCache INSTANCE = new GLOpCache();
    
    private Map<Class<? extends SurfaceOp>, GLOp> cache;
    
    private GLOpCache() {
        init();
    }
    
    private void init() {
        cache = new HashMap<Class<? extends SurfaceOp>, GLOp>();
        cache.put(Blit.class, new GLBlit());
    }
    
//    public GLOp find(Class<? extends SurfaceOp> op) {
//        return cache.get(op);
//    }
    
    public GLOp find(SurfaceOp op) {
        GLOp glop = cache.get(op.getClass());
        if (glop != null && glop.canProcess(op)) {
            return glop;
        } else {
            return null;
        }
    }
    
    public static GLOpCache getInstance() {
        return INSTANCE;
    }
    
    
}
