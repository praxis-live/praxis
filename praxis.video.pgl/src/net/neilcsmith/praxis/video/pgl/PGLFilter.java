/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2016 Neil C Smith.
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
package net.neilcsmith.praxis.video.pgl;

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
import net.neilcsmith.praxis.video.pipes.impl.MultiInOut;
import net.neilcsmith.praxis.video.pipes.impl.Placeholder;
import net.neilcsmith.praxis.video.render.Surface;
import processing.core.PConstants;
import processing.core.PImage;
import processing.opengl.PShader;
//import org.lwjgl.opengl.GL11;

/**
 *
 * @author Neil C Smith (http://neilcsmith.net)
 */
public class PGLFilter extends AbstractExecutionContextComponent {

    private final static Logger LOG = Logger.getLogger(PGLFilter.class.getName());
    private final static int UNIFORM_COUNT = 8;
    private final static String DEFAULT_VERTEX_SHADER
            = "uniform mat4 transformMatrix;\n" //
            + "uniform mat4 texMatrix;\n\n" //
            + "attribute vec4 position;\n" //

            + "attribute vec4 color;\n"
            + "attribute vec2 texCoord;\n\n"//

            + "varying vec4 vertColor;\n" //
            + "varying vec4 vertTexCoord;\n\n" //

            + "void main()\n" //
            + "{\n" //
            + "  vertColor = color;\n" //
            + "  vertTexCoord = texMatrix * vec4(texCoord, 1.0, 1.0);\n\n" //
            + "  gl_Position = transformMatrix * position;\n" //
            + "}\n";

    private final static String DEFAULT_FRAGMENT_SHADER
            = "uniform sampler2D texture;\n\n" //

            + "uniform vec2 texOffset;\n\n" //

            + "varying vec4 vertColor;\n" //
            + "varying vec4 vertTexCoord;\n\n" //

            + "void main() {\n"//
            + "  gl_FragColor = texture2D(texture, vertTexCoord.st) * vertColor;\n" //
            + "}";

    private final double[] uniforms;
    private final String[] uniformIDs;
    private final GLDelegate delegate;
    private final boolean convertOldCode;
    private boolean dirty;
    private String vertex;
    private String fragment;
//    private String[] vertexCode;
//    private String[] fragmentCode;
    private BooleanProperty clear;

    public PGLFilter() {
        this(false);
    }
    
    private PGLFilter(boolean convert) {
        uniforms = new double[UNIFORM_COUNT];
        uniformIDs = new String[UNIFORM_COUNT];
        delegate = new GLDelegate();
        dirty = true;
        convertOldCode = convert;
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
//            ShaderProperty v = new ShaderProperty(true);
            registerControl("vertex", v);
            StringProperty f = StringProperty.builder()
                    .mimeType("text/x-glsl-frag")
                    .template(DEFAULT_FRAGMENT_SHADER)
                    .emptyIsDefault()
                    .binding(new FragmentBinding())
                    .build();
//            ShaderProperty f = new ShaderProperty(false);
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
        delegate.dispose();
        dirty = true;
    }

    private class VertexBinding implements StringProperty.Binding {

        @Override
        public void setBoundValue(long time, String value) {
            if (vertex.equals(value)) {
                return;
            }
            if (convertOldCode && !value.isEmpty()) {
                value = ShaderUtils.convertOldVertexCode(value);
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
            if (convertOldCode && !value.isEmpty()) {
                value = ShaderUtils.convertOldFragmentCode(value);
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

        private PGLShader shader;

        private GLDelegate() {
            super(1, 1);
        }

        @Override
        protected void process(Surface[] inputs, Surface output, int outputIndex, boolean rendering) {
            if (!rendering) {
                return;
            }
            if (output instanceof PGLSurface) {
                PGLSurface dst = (PGLSurface) output;
                PGLGraphics g = dst.getGraphics();
                PImage src = dst.getContext().asImage(inputs[0]);
                boolean first = false;
                if (dirty) {
                    LOG.finest("Initializing shader");
                    initShader(dst.getContext());
                    dirty = false;
                    first = true;
                }
                g.beginDraw();
                if (clear.getValue()) {
                    g.clear();
                }
                g.blendMode(PConstants.REPLACE);
                g.tint(255, 255, 255, 255);

                if (shader != null) {
                    try {
                        for (int i = 0; i < uniformIDs.length; i++) {
                            String id = uniformIDs[i];
                            shader.set(id, (float) uniforms[i]);
                        }

                        g.shader(shader);
                        g.image(src, 0, 0);
                        g.resetShader();
                        return;
                    } catch (Exception ex) {
                        LOG.log(Level.SEVERE, "Shader exception", ex);
                        g.resetShader();
                        shader = null;
                    }
                }
                
                // fall through
                g.image(src,0,0);
                
            }
        }

        private void initShader(PGLContext context) {
            dispose();
            if (vertex.isEmpty() && fragment.isEmpty()) {
                shader = null;
            } else {
                String vert = vertex.isEmpty() ? DEFAULT_VERTEX_SHADER : vertex;
                String frag = fragment.isEmpty() ? DEFAULT_FRAGMENT_SHADER : fragment;
                shader = new PGLShader(context, vert, frag);
                LOG.log(Level.FINEST, "Compiled shader :\n{0}", frag);

                for (int i = 0; i < uniformIDs.length; i++) {
                    uniformIDs[i] = "u" + (i + 1);
                }
            }

//            LOG.log(Level.FINEST, "Shader log\n{0}", shader.getLog());
        }

        private void dispose() {
            if (shader != null) {
                shader.dispose();
            }
        }
    }
    
    @Deprecated
    public static class GLFilter extends PGLFilter {
        
        public GLFilter() {
            super(true);
        }
        
    }
}
