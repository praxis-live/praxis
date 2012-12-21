/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012 Neil C Smith.
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
package net.neilcsmith.praxis.video.opengl;

import java.util.logging.Level;
import java.util.logging.Logger;
import net.neilcsmith.praxis.core.Argument;
import net.neilcsmith.praxis.core.ExecutionContext;
import net.neilcsmith.praxis.core.Port;
import net.neilcsmith.praxis.core.info.ArgumentInfo;
import net.neilcsmith.praxis.core.types.PMap;
import net.neilcsmith.praxis.core.types.PString;
import net.neilcsmith.praxis.impl.AbstractExecutionContextComponent;
import net.neilcsmith.praxis.impl.ArgumentProperty;
import net.neilcsmith.praxis.impl.FloatProperty;
import net.neilcsmith.praxis.video.impl.DefaultVideoInputPort;
import net.neilcsmith.praxis.video.impl.DefaultVideoOutputPort;
import net.neilcsmith.praxis.video.opengl.internal.Color;
import net.neilcsmith.praxis.video.opengl.internal.GLRenderer;
import net.neilcsmith.praxis.video.opengl.internal.GLSurface;
import net.neilcsmith.praxis.video.opengl.internal.ShaderProgram;
import net.neilcsmith.praxis.video.pipes.impl.MultiInOut;
import net.neilcsmith.praxis.video.pipes.impl.Placeholder;
import net.neilcsmith.praxis.video.render.Surface;
import org.lwjgl.opengl.GL11;

/**
 *
 * @author Neil C Smith (http://neilcsmith.net)
 */
public class GLFilter extends AbstractExecutionContextComponent {
    
    private final static Logger LOG = Logger.getLogger(GLFilter.class.getName());

    private final static int UNIFORM_COUNT = 8;
    private final static String VERTEX_SHADER =
            "attribute vec4 " + ShaderProgram.POSITION_ATTRIBUTE + ";\n" //
            + "attribute vec4 " + ShaderProgram.COLOR_ATTRIBUTE + ";\n" //
            + "attribute vec2 " + ShaderProgram.TEXCOORD_ATTRIBUTE + "0;\n" //
            + "uniform mat4 u_projectionViewMatrix;\n" //
            + "varying vec4 v_color;\n" //
            + "varying vec2 v_texCoords;\n" //
            + "\n" //
            + "void main()\n" //
            + "{\n" //
            + "   v_color = " + ShaderProgram.COLOR_ATTRIBUTE + ";\n" //
            + "   v_texCoords = " + ShaderProgram.TEXCOORD_ATTRIBUTE + "0;\n" //
            + "   gl_Position =  u_projectionViewMatrix * " + ShaderProgram.POSITION_ATTRIBUTE + ";\n" //
            + "}\n";
    private final static String DEFAULT_FRAGMENT_SHADER =
            "varying vec2 v_texCoords;\n" //
            + "uniform sampler2D u_texture;\n" //
            + "void main()\n"//
            + "{\n" //
            + "  gl_FragColor = texture2D(u_texture, v_texCoords);\n" //
            + "}";
    private final double[] uniforms;
    private final String[] uniformIDs;
    private final GLDelegate delegate;
    private ShaderProgram shader;
    private boolean dirty;
    private String fragment;

    public GLFilter() {
        uniforms = new double[UNIFORM_COUNT];
        uniformIDs = new String[UNIFORM_COUNT];
        delegate = new GLDelegate();
        dirty = true;
        fragment = "";
        init();
    }

    private void init() {
        try {
            Placeholder in = new Placeholder();
            delegate.addSource(in);
            registerPort(Port.IN, new DefaultVideoInputPort(this, in));
            registerPort(Port.OUT, new DefaultVideoOutputPort(this, delegate));
            registerControl("fragment", ArgumentProperty.create(ArgumentInfo.create(PString.class, PMap.create(PString.KEY_MIME_TYPE, "text/x-glsl",
                    ArgumentInfo.KEY_TEMPLATE, DEFAULT_FRAGMENT_SHADER)), new FragmentBinding(), PString.EMPTY));
            for (int i = 0; i < UNIFORM_COUNT; i++) {
                FloatProperty u = FloatProperty.create(new UniformBinding(i), 0, 1, 0);
                String ID = "u" + (i + 1);
                registerControl(ID, u);
                registerPort(ID, u.createPort());
            }
        } catch (Exception ex) {
            throw new IllegalStateException(ex);
        }
    }

    @Override
    public void stateChanged(ExecutionContext source) {
//        if (shader != null) {
//            shader.dispose();
//            shader = null;
//        }
        dirty = true;
    }

    private class FragmentBinding implements ArgumentProperty.Binding {

        @Override
        public void setBoundValue(long time, Argument value) {
            String frag = value.toString();
            if (fragment.equals(frag)) {
                return;
            }
            fragment = frag;
            dirty = true;
        }

        @Override
        public Argument getBoundValue() {
            return PString.valueOf(fragment);
        }
    }

    private class UniformBinding implements FloatProperty.Binding {

        private int idx;

        private UniformBinding(int idx) {
            this.idx = idx;
        }

        @Override
        public void setBoundValue(long time, double value) {
            uniforms[idx] = value;
        }

        @Override
        public double getBoundValue() {
            return uniforms[idx];
        }
    }

    private class GLDelegate extends MultiInOut {
        
        private GLDelegate() {
            super(1,1);
        }

        @Override
        protected void process(Surface[] inputs, Surface output, int outputIndex, boolean rendering) {
            if (!rendering) {
                return;
            }
            if (output instanceof GLSurface) {
                boolean first = false;
                if (dirty) {
                    LOG.finest("Initializing shader");
                    initShader();
                    dirty = false;
                    first = true;
                }
                GLSurface src = (GLSurface) inputs[0];
                GLSurface dst = (GLSurface) output;
                GLRenderer r = dst.getGLContext().getRenderer();
                r.target(dst);
                r.setBlendFunction(GL11.GL_ONE, GL11.GL_ZERO);
                r.setColor(Color.WHITE);
                r.bind(shader);
                for (int i=0; i < uniformIDs.length; i++) {
                    String id = uniformIDs[i];
                    if (id == null) {
                        continue;
                    }
                    if (first && !shader.hasUniform(id)) {
                        LOG.log(Level.FINEST, "Removing uniform : {0}", id);
                        uniformIDs[i] = null;
                        continue;
                    }
//                    LOG.log(Level.FINEST, "Setting Uniform {0} to {1}", new Object[]{id, uniforms[i]});
                    shader.setUniformf(id, (float) uniforms[i]);
                }
                r.draw(src, 0, 0);
                r.unbind(shader);
            }
        }

        private void initShader() {
            if (shader != null) {
                shader.dispose();
            }
            String frag = fragment.isEmpty() ? DEFAULT_FRAGMENT_SHADER : fragment;
            shader = new ShaderProgram(VERTEX_SHADER, frag);
            LOG.log(Level.FINEST, "Compiled shader :\n{0}", frag);
            
            for (int i=0; i<uniformIDs.length; i++) {
                uniformIDs[i] = "u" + (i+1);
            }
            if (!shader.isCompiled()) {
                LOG.warning("Shader not compiled");
            }
            LOG.log(Level.FINEST, "Shader log\n{0}", shader.getLog());
        }



    }
}
