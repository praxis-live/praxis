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

import net.neilcsmith.ripl.Surface;
import net.neilcsmith.ripl.SurfaceOp;
import net.neilcsmith.ripl.ops.Blend;
import net.neilcsmith.ripl.ops.BlendFunction;
import net.neilcsmith.ripl.ops.Blit;
import net.neilcsmith.ripl.ops.Bounds;
import net.neilcsmith.ripl.render.opengl.internal.Color;
import net.neilcsmith.ripl.render.opengl.GLException;
import net.neilcsmith.ripl.render.opengl.internal.Matrix4;
import net.neilcsmith.ripl.render.opengl.internal.GLRenderer;
import net.neilcsmith.ripl.render.opengl.internal.GLSurface;
import net.neilcsmith.ripl.render.opengl.internal.Texture;
import net.neilcsmith.ripl.render.opengl.internal.TextureRegion;
import net.neilcsmith.ripl.render.opengl.internal.TextureRenderer;
import org.lwjgl.opengl.EXTFramebufferObject;
import org.lwjgl.opengl.GL11;

/**
 *
 * @author Neil C Smith <http://neilcsmith.net>
 */
public class GLBlit extends GLOp {

//    private TextureRenderer renderer = new TextureRenderer(1);
    GLBlit() {
        super(Blit.class);
    }

    @Override
    public boolean canProcess(SurfaceOp op) {
        if (op instanceof Blit) {
            Blit blit = (Blit) op;
            BlendFunction bf = blit.getBlendFunction();
            if (bf instanceof Blend) {
                Blend bl = (Blend) bf;
                return bl.getType() == Blend.Type.Normal
                        || bl.getType() == Blend.Type.Add;
            }
        }
        return false;
    }

    @Override
    public void process(SurfaceOp op, GLSurface output, GLSurface... inputs) {
        GLSurface src = inputs[0];
        GLRenderer renderer = GLRenderer.get(output);
        Blit blit = (Blit) op;
        setupBlending(renderer, (Blend) blit.getBlendFunction(), src.hasAlpha(), output.hasAlpha());
        Bounds bounds = blit.getSourceRegion();
               
        if (bounds == null) {
            int x = blit.getX();
            int y = output.getHeight() - (blit.getY() + src.getHeight());
            renderer.draw(src, x, y);
        } else {
            int x = blit.getX();
            int sh = bounds.getHeight();
            int y = output.getHeight() - (blit.getY() + sh);       
            int sy = src.getHeight() - (bounds.getY() + sh);
            renderer.draw(src, bounds.getX(), bounds.getY(), bounds.getWidth(), sh, x, y);
        }
        
        
    }

    private void setupBlending(GLRenderer renderer, Blend blend, boolean srcAlpha, boolean dstAlpha) {
        float extraAlpha = (float) blend.getExtraAlpha();
        renderer.enableBlending();
        if (blend.getType() == Blend.Type.Add) {
            renderer.setBlendFunction(GL11.GL_ONE, GL11.GL_ONE);
        } else {
            renderer.setBlendFunction(GL11.GL_ONE, GL11.GL_ONE_MINUS_SRC_ALPHA);
        }

        renderer.setColor(new Color(extraAlpha, extraAlpha, extraAlpha, extraAlpha));


    }
}
