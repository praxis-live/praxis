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

import java.util.logging.Logger;
import net.neilcsmith.praxis.video.pgl.PGLSurface;
import net.neilcsmith.praxis.video.render.Surface;
import net.neilcsmith.praxis.video.render.SurfaceOp;

/**
 *
 * @author Neil C Smith
 */
public abstract class PGLOp {
    
    final static Logger LOG = Logger.getLogger(PGLOp.class.getName());
    
    private final Class<? extends SurfaceOp> opClass;
    
    protected PGLOp(Class<? extends SurfaceOp> opClass) {
        if (opClass == null) {
            throw new NullPointerException();
        }
        this.opClass = opClass;
    }
    
    public final Class<? extends SurfaceOp> getOpClass() {
        return opClass;
    }
    
    public abstract void process(SurfaceOp op, PGLSurface output, Bypass bypass, Surface ... inputs);
   
    public interface Bypass {
        
        public void process(SurfaceOp op, Surface ... inputs);
        
    }
   
    
}
