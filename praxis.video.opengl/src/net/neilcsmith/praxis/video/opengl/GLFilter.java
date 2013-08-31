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
import net.neilcsmith.praxis.core.ExecutionContext;
import net.neilcsmith.praxis.core.Port;
import net.neilcsmith.praxis.impl.AbstractExecutionContextComponent;
import net.neilcsmith.praxis.impl.BooleanProperty;
import net.neilcsmith.praxis.impl.NumberProperty;
import net.neilcsmith.praxis.impl.StringProperty;
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
    private final static String DEFAULT_VERTEX_SHADER =
            "attribute vec4 " + ShaderProgram.POSITION_ATTRIBUTE + ";\n" //
            + "attribute vec4 " + ShaderProgram.COLOR_ATTRIBUTE + ";\n" //
            + "attribute vec2 " + ShaderProgram.TEXCOORD_ATTRIBUTE + "0;\n" //
            + "uniform mat4 u_projectionViewMatrix;\n" //
            + "varying vec4 v_color;\n" //
            + "varying vec2 v_texCoords;\n" //
            + "\n" //
            + "void main()\n" //
            + "{\n" //
            + "  v_color = " + ShaderProgram.COLOR_ATTRIBUTE + ";\n" //
            + "  v_texCoords = " + ShaderProgram.TEXCOORD_ATTRIBUTE + "0;\n" //
            + "  gl_Position = u_projectionViewMatrix * " + ShaderProgram.POSITION_ATTRIBUTE + ";\n" //
            + "}\n";
    private final static String DEFAULT_FRAGMENT_SHADER =
            "varying vec2 v_texCoords;\n" //
            + "uniform sampler2D u_texture;\n" //
            + "void main() {\n"//
            + "  gl_FragColor = texture2D(u_texture, v_texCoords);\n" //
            + "}";
    private final double[] uniforms;
    private final String[] uniformIDs;
    private final GLDelegate delegate;
    private ShaderProgram shader;
    private boolean dirty;
    private String vertex;
    private String fragment;
    private BooleanProperty clear;

    public GLFilter() {
        uniforms = new double[UNIFORM_COUNT];
        uniformIDs = new String[UNIFORM_COUNT];
        delegate = new GLDelegate();
        dirty = true;
        vertex = "";
        fragment = "";
        init();
    }

    private void init() {
        try {
            Placeholder in = new Placeholder();
            delegate.addSource(in);
            registerPort(Port.IN, new DefaultVideoInputPort(in));
            registerPort(Port.OUT, new DefaultVideoOutputPort(delegate));
//            registerControl("fragment", ArgumentProperty.create(
//                    ArgumentInfo.create(PString.class,
//                    PMap.create(PString.KEY_MIME_TYPE, "text/x-glsl-frag",
//                    ArgumentInfo.KEY_TEMPLATE, DEFAULT_FRAGMENT_SHADER)),
//                    new FragmentBinding(),
//                    PString.EMPTY));
            StringProperty v = StringProperty.builder()
                    .mimeType("text/x-glsl-vert")
                    .template(DEFAULT_VERTEX_SHADER)
                    .emptyIsDefault()
                    .binding(new VertexBinding())
                    .build();
            registerControl("vertex", v);
            StringProperty f = StringProperty.builder()
                    .mimeType("text/x-glsl-frag")
                    .template(DEFAULT_FRAGMENT_SHADER)
                    .emptyIsDefault()
                    .binding(new FragmentBinding())
                    .build();
            registerControl("fragment", f);
            for (int i = 0; i < UNIFORM_COUNT; i++) {
//                NumberProperty u = NumberProperty.create(new UniformBinding(i), 0, 1, 0);
                NumberProperty u = NumberProperty.builder()
                        .minimum(0)
                        .maximum(1)
                        .defaultValue(0)
                        .binding(new UniformBinding(i))
                        .build();
                String ID = "u" + (i + 1);
                registerControl(ID, u);
                registerPort(ID, u.createPort());
            }
            clear = BooleanProperty.create(true);
            registerControl("clear-screen", clear);
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

    private class VertexBinding implements StringProperty.Binding {

        @Override
        public void setBoundValue(long time, String value) {
            if (vertex.equals(value)) {
                return;
            }
            vertex = value;
            dirty = true;
        }

        @Override
        public String getBoundValue() {
            return vertex;
        }
    }

    private class FragmentBinding implements StringProperty.Binding {

        @Override
        public void setBoundValue(long time, String value) {
            if (fragment.equals(value)) {
                return;
            }
            fragment = value;
            dirty = true;
        }

        @Override
        public String getBoundValue() {
            return fragment;
        }
    }

    private class UniformBinding implements NumberProperty.Binding {

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
            super(1, 1);
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
                if (clear.getValue()) {
                    r.clear();
                }
                r.setBlendFunction(GL11.GL_ONE, GL11.GL_ZERO);
                r.setColor(Color.WHITE);
                r.bind(shader);
                for (int i = 0; i < uniformIDs.length; i++) {
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
            String vert = vertex.isEmpty() ? DEFAULT_VERTEX_SHADER : vertex;
            String frag = fragment.isEmpty() ? DEFAULT_FRAGMENT_SHADER : fragment;
            shader = new ShaderProgram(vert, frag);
            LOG.log(Level.FINEST, "Compiled shader :\n{0}", frag);

            for (int i = 0; i < uniformIDs.length; i++) {
                uniformIDs[i] = "u" + (i + 1);
            }
            if (!shader.isCompiled()) {
                LOG.warning("Shader not compiled");
            }
            LOG.log(Level.FINEST, "Shader log\n{0}", shader.getLog());
        }
    }
}
