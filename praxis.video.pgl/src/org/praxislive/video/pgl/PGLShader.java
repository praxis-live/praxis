/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2019 Neil C Smith.
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

package org.praxislive.video.pgl;

import processing.core.PImage;
import processing.core.PMatrix3D;
import processing.opengl.PShader;
import processing.opengl.Texture;

/**
 *
 * @author Neil C Smith <http://neilcsmith.net>
 */
public class PGLShader extends PShader {

    private PMatrix3D matrix;
    
    public PGLShader(PGLContext context, String vertex, String fragment) {
        super(context.primary().parent,
                context.getPGL().preprocessVertexSource(new String[]{vertex}),
                context.getPGL().preprocessFragmentSource(new String[]{fragment}));
    }

    @Override
    public void set(String name, PImage texture) {
        super.set(name, texture);
        Texture tex = primaryPG.getTexture(texture);
        
        float scaleu = 1;
        float scalev = 1;
        float dispu  = 0;
        float dispv  = 0;

        if (tex != null) {
            if (tex.invertedX()) {
                scaleu = -1;
                dispu  = 1;
            }

            if (tex.invertedY()) {
                scalev = -1;
                dispv  = 1;
            }

            scaleu *= tex.maxTexcoordU();
            dispu  *= tex.maxTexcoordU();
            scalev *= tex.maxTexcoordV();
            dispv  *= tex.maxTexcoordV();

            set(name + "Offset", 1.0f / tex.width, 1.0f / tex.height);

        }
        
        if (matrix == null) {
            matrix = new PMatrix3D();
        }
           
        if (tcmat == null) {
            tcmat = new float[16];
        }
            
        tcmat[0] = scaleu; tcmat[4] = 0;      tcmat[ 8] = 0; tcmat[12] = dispu;
        tcmat[1] = 0;      tcmat[5] = scalev; tcmat[ 9] = 0; tcmat[13] = dispv;
        tcmat[2] = 0;      tcmat[6] = 0;      tcmat[10] = 0; tcmat[14] = 0;
        tcmat[3] = 0;      tcmat[7] = 0;      tcmat[11] = 0; tcmat[15] = 0;
            
        matrix.set(tcmat);
            
        set(name + "Matrix", matrix);
        
    }

    
    @Override
    public void dispose() {
        super.dispose();
    }
    
    
    
    
}
