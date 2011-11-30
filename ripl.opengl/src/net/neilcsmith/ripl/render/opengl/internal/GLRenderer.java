/*******************************************************************************
 * Copyright 2011 See AUTHORS file.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
package net.neilcsmith.ripl.render.opengl.internal;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.neilcsmith.ripl.render.opengl.internal.VertexAttributes.Usage;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL14;

/** <p>
 * 
 * @author mzechner */
// derived from SpriteBatch in libgdx
public class GLRenderer implements Disposable {

    private final static Logger LOGGER = Logger.getLogger(GLRenderer.class.getName());
    private final static Map<GLSurface, GLRenderer> renderers =
            new HashMap<GLSurface, GLRenderer>();
    private static GLRenderer active;
    static final int VERTEX_SIZE = 2 + 1 + 2;
    static final int SPRITE_SIZE = 4 * VERTEX_SIZE;
    private Mesh mesh;
    private Mesh[] buffers;
    private Texture lastTexture = null;
    private float invTexWidth = 0;
    private float invTexHeight = 0;
    private int idx = 0;
    private int currBufferIdx = 0;
    private final float[] vertices;
    private final Matrix4 transformMatrix = new Matrix4();
    private final Matrix4 projectionMatrix = new Matrix4();
    private final Matrix4 combinedMatrix = new Matrix4();
//    private boolean drawing = false;
    private boolean blendingDisabled = false;
    private int blendSrcFunc = GL11.GL_ONE;
    private int blendDstFunc = GL11.GL_ZERO;
    private int blendSrcAlphaFunc = GL11.GL_ONE;
    private int blendDstAlphaFunc = GL11.GL_ZERO;
    private ShaderProgram shader;
    private float color = Color.WHITE.toFloatBits();
    private Color tempColor = new Color(1, 1, 1, 1);
    /** number of render calls **/
    public int renderCalls = 0;
    /** the maximum number of sprites rendered in one batch so far **/
    public int maxSpritesInBatch = 0;
    private ShaderProgram customShader = null;
    private GLSurface surface;

    private GLRenderer(GLSurface surface) {
        this(surface, 1000, 1);
    }

    /** <p>
     * Constructs a new TextureRenderer. Sets the projection matrix to an orthographic projection with y-axis point upwards, x-axis
     * point to the right and the origin being in the bottom left corner of the screen. The projection will be pixel perfect with
     * respect to the screen resolution.
     * </p>
     * 
     * <p>
     * The size parameter specifies the maximum size of a single batch in number of sprites
     * </p>
     * 
     * @param size the batch size in number of sprites
     * @param buffers the number of buffers to use. only makes sense with VBOs. This is an expert function. */
    private GLRenderer(GLSurface surface, int size, int buffers) {

        this.surface = surface;

        this.buffers = new Mesh[buffers];

        for (int i = 0; i < buffers; i++) {
            this.buffers[i] = new Mesh(false, size * 4, size * 6, new VertexAttribute(Usage.Position, 2,
                    ShaderProgram.POSITION_ATTRIBUTE), new VertexAttribute(Usage.ColorPacked, 4, ShaderProgram.COLOR_ATTRIBUTE),
                    new VertexAttribute(Usage.TextureCoordinates, 2, ShaderProgram.TEXCOORD_ATTRIBUTE + "0"));
        }

        vertices = new float[size * SPRITE_SIZE];

        int len = size * 6;
        short[] indices = new short[len];
        short j = 0;
        for (int i = 0; i < len; i += 6, j += 4) {
            indices[i + 0] = (short) (j + 0);
            indices[i + 1] = (short) (j + 1);
            indices[i + 2] = (short) (j + 2);
            indices[i + 3] = (short) (j + 2);
            indices[i + 4] = (short) (j + 3);
            indices[i + 5] = (short) (j + 0);
        }
        for (int i = 0; i < buffers; i++) {
            this.buffers[i].setIndices(indices);
        }
        mesh = this.buffers[0];

        createShader(); // make static?

    }

    private void createShader() {
        String vertexShader =
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
        String fragmentShader =
                "varying vec4 v_color;\n" //
                + "varying vec2 v_texCoords;\n" //
                + "uniform sampler2D u_texture;\n" //
                + "void main()\n"//
                + "{\n" //
                + "  gl_FragColor = v_color * texture2D(u_texture, v_texCoords);\n" //
                + "}";

        shader = new ShaderProgram(vertexShader, fragmentShader);
        shader.begin();
        if (shader.isCompiled() == false) {
            throw new IllegalArgumentException("couldn't compile shader: " + shader.getLog());
        }
    }

    public void clear() {
//        checkActive();
//        beginDrawing();
        activate();
        lastTexture = null;
        idx = 0;
        if (surface == null) {
            GL11.glClearColor(0, 0, 0, 1);
            GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);
        } else {
            if (surface.hasAlpha()) {
                GL11.glClearColor(0, 0, 0, 0);
            } else {
                GL11.glClearColor(0, 0, 0, 1);
            }
            GL11.glClear(GL11.GL_COLOR_BUFFER_BIT);
        }
    }

    private void activate() {
        if (active == this) {
            return;
        }

        FrameBuffer fbo = null;
        if (surface != null) {
            // this might switch to a different renderer - wait to flush until afterwards.
            fbo = surface.getWritableData().getTexture().getFrameBuffer();
        }

        if (active != null) {
            active.flush();
        }

        GL11.glDepthMask(false);
        GL11.glEnable(GL11.GL_TEXTURE_2D);
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, 0);

        if (fbo == null) {
            LOGGER.finest("Setting up render to screen");
            int w = GLContext.getCurrent().getWidth();
            int h = GLContext.getCurrent().getHeight();
            FrameBuffer.unbind();
            GL11.glViewport(0, 0, w, h);
            projectionMatrix.setToOrtho2D(0, 0, w, h);
        } else {
            LOGGER.finest("Setting up render to texture");
            fbo.bind();
            int w = surface.getWidth();
            int h = surface.getHeight();
            GL11.glViewport(0, 0, w, h);
            projectionMatrix.setToOrtho2D(0, h, w, -h);
        }

        if (customShader != null) {
            customShader.begin();
        } else {
            shader.begin();
        }
        setupMatrices();

        active = this;

    }

    /** Finishes off rendering. Enables depth writes, disables blending and texturing. Must always be called after a call to
     * {@link #beginDrawing()} */
    private void flush() {

        if (active != this) {
            LOGGER.fine("flush() called but we're not the active renderer");
        }

        renderMesh();

        lastTexture = null;
        idx = 0;
//        drawing = false;

        GL11.glDepthMask(true);
        if (isBlendingEnabled()) {
            GL11.glDisable(GL11.GL_BLEND);
        }
        GL11.glDisable(GL11.GL_TEXTURE_2D);

        if (customShader != null) {
            customShader.end();
        } else {
            shader.end();
        }

        active = null;

    }

    /** Sets the color used to tint images when they are added to the TextureRenderer. Default is {@link Color#WHITE}. */
    public void setColor(Color tint) {
        color = tint.toFloatBits();
    }

    /** @see #setColor(Color) */
    public void setColor(float r, float g, float b, float a) {
        int intBits = (int) (255 * a) << 24 | (int) (255 * b) << 16 | (int) (255 * g) << 8 | (int) (255 * r);
        color = Float.intBitsToFloat(intBits & 0xfeffffff);
    }

    /** @see #setColor(Color)
     * @see Color#toFloatBits() */
    public void setColor(float color) {
        this.color = color;
    }

    /** @return the rendering color of this TextureRenderer. Manipulating the returned instance has no effect. */
    public Color getColor() {
        int intBits = Float.floatToRawIntBits(color);
        Color color = this.tempColor;
        color.r = (intBits & 0xff) / 255f;
        color.g = ((intBits >>> 8) & 0xff) / 255f;
        color.b = ((intBits >>> 16) & 0xff) / 255f;
        color.a = ((intBits >>> 24) & 0xff) / 255f;
        return color;
    }
    private TextureRegion region = new TextureRegion();

    public void draw(GLSurface src, float x, float y) {
        draw(src, 0, 0, src.getWidth(), src.getHeight(), x, y);
    }

    public void draw(GLSurface src, float x, float y, float width, float height) {
    }

    public void draw(GLSurface src, float srcX, float srcY, float srcWidth, float srcHeight, float x, float y) {
        Texture tex = src.getReadableData().getTexture();
        activate();
        region.setTexture(tex);
        region.setRegion((int) srcX, (int) srcY, (int) srcWidth, (int) srcHeight);
        draw(region, x, y);
    }

    public void draw(GLSurface src, float srcX, float srcY, float srcWidth, float srcHeight,
            float x, float y, float width, float height) {
    }

    /** Draws a rectangle with the bottom left corner at x,y having the width and height of the region. */
    private void draw(TextureRegion region, float x, float y) {
        draw(region, x, y, Math.abs(region.getRegionWidth()), Math.abs(region.getRegionHeight()));
    }

    /** Draws a rectangle with the bottom left corner at x,y and stretching the region to cover the given width and height. */
    private void draw(TextureRegion region, float x, float y, float width, float height) {
//        if (!drawing) {
//            throw new IllegalStateException("SpriteBatch.begin must be called before draw.");
//        }
        Texture texture = region.texture;

        if (texture != lastTexture) {
            renderMesh();
            lastTexture = texture;
            invTexWidth = 1f / texture.getWidth();
            invTexHeight = 1f / texture.getHeight();
        } else if (idx == vertices.length) {
//            System.out.println("idx == vertices.length");
            renderMesh();
        }

        final float fx2 = x + width;
        final float fy2 = y + height;
        final float u = region.u;
        final float v = region.v2;
        final float u2 = region.u2;
        final float v2 = region.v;

        vertices[idx++] = x;
        vertices[idx++] = y;
        vertices[idx++] = color;
        vertices[idx++] = u;
        vertices[idx++] = v;

        vertices[idx++] = x;
        vertices[idx++] = fy2;
        vertices[idx++] = color;
        vertices[idx++] = u;
        vertices[idx++] = v2;

        vertices[idx++] = fx2;
        vertices[idx++] = fy2;
        vertices[idx++] = color;
        vertices[idx++] = u2;
        vertices[idx++] = v2;

        vertices[idx++] = fx2;
        vertices[idx++] = y;
        vertices[idx++] = color;
        vertices[idx++] = u2;
        vertices[idx++] = v;
    }

    private void renderMesh() {

        if (idx == 0) {
            return;
        }

        renderCalls++;

        int spritesInBatch = idx / 20;

        if (lastTexture == null) {
            LOGGER.log(Level.WARNING, "Texture is null - returning : idx = {0}", idx);
            return;
        }

        lastTexture.bind();
        mesh.setVertices(vertices, 0, idx);

        if (blendingDisabled) {
            GL11.glDisable(GL11.GL_BLEND);
        } else {
            GL11.glEnable(GL11.GL_BLEND);
            if (blendSrcFunc == blendSrcAlphaFunc
                    && blendDstFunc == blendDstAlphaFunc) {
                GL11.glBlendFunc(blendSrcFunc, blendDstFunc);
            } else {
                GL14.glBlendFuncSeparate(blendSrcFunc, blendDstFunc,
                        blendSrcAlphaFunc, blendDstAlphaFunc);
            }
        }

        if (customShader != null) {
            mesh.render(customShader, GL11.GL_TRIANGLES, 0, spritesInBatch * 6);
        } else {
            mesh.render(shader, GL11.GL_TRIANGLES, 0, spritesInBatch * 6);
        }


        idx = 0;
        currBufferIdx++;
        if (currBufferIdx == buffers.length) {
            currBufferIdx = 0;
        }
        mesh = buffers[currBufferIdx];
    }

    /** Disables blending for drawing sprites. Does not disable blending for text rendering */
    public void disableBlending() {
        if (blendingDisabled) {
            return;
        }
        renderMesh();
        blendingDisabled = true;
    }

    /** Enables blending for sprites */
    public void enableBlending() {
        if (!blendingDisabled) {
            return;
        }
        renderMesh();
        blendingDisabled = false;
    }

    /** Sets the blending function to be used when rendering sprites.
     * 
     * @param srcFunc the source function, e.g. GL11.GL_SRC_ALPHA
     * @param dstFunc the destination function, e.g. GL11.GL_ONE_MINUS_SRC_ALPHA */
    public void setBlendFunction(int srcFunc, int dstFunc) {
        setBlendFunction(srcFunc, dstFunc, srcFunc, dstFunc);
    }

    public void setBlendFunction(int srcRGBFunc, int dstRGBFunc, int srcAlphaFunc, int dstAlphaFunc) {
        if (blendSrcFunc != srcRGBFunc || blendDstFunc != dstRGBFunc
                || blendSrcAlphaFunc != srcAlphaFunc || blendDstAlphaFunc != dstAlphaFunc) {
            renderMesh();
            blendSrcFunc = srcRGBFunc;
            blendDstFunc = dstRGBFunc;
            blendSrcAlphaFunc = srcAlphaFunc;
            blendDstAlphaFunc = dstAlphaFunc;
        }

    }

    /** Disposes all resources associated with this TextureRenderer */
    public void dispose() {
        for (int i = 0; i < buffers.length; i++) {
            buffers[i].dispose();
        }
        if (shader != null) {
            shader.dispose();
        }
    }

    private void setupMatrices() {
        combinedMatrix.set(projectionMatrix).mul(transformMatrix);
        if (customShader != null) {
            customShader.setUniformMatrix("u_proj", projectionMatrix);
            customShader.setUniformMatrix("u_trans", transformMatrix);
            customShader.setUniformMatrix("u_projTrans", combinedMatrix);
            customShader.setUniformi("u_texture", 0);
        } else {
            shader.setUniformMatrix("u_projectionViewMatrix", combinedMatrix);
            shader.setUniformi("u_texture", 0);
        }

    }

    /** Sets the shader to be used in a GLES 2.0 environment. Vertex position attribute is called "a_position", the texture
     * coordinates attribute is called called "a_texCoords0", the color attribute is called "a_color". See
     * {@link ShaderProgram#POSITION_ATTRIBUTE}, {@link ShaderProgram#COLOR_ATTRIBUTE} and {@link ShaderProgram#TEXCOORD_ATTRIBUTE}
     * which gets "0" appened to indicate the use of the first texture unit. The projection matrix is uploaded via a mat4 uniform
     * called "u_proj", the transform matrix is uploaded via a uniform called "u_trans", the combined transform and projection
     * matrx is is uploaded via a mat4 uniform called "u_projTrans". The texture sampler is passed via a uniform called
     * "u_texture".
     * 
     * Call this method with a null argument to use the default shader.
     * 
     * @param shader the {@link ShaderProgram} or null to use the default shader. */
    public void setShader(ShaderProgram shader) {
        customShader = shader;
    }

    /** @return whether blending for sprites is enabled */
    public boolean isBlendingEnabled() {
        return !blendingDisabled;
    }
//    static public final int X1 = 0;
//    static public final int Y1 = 1;
//    static public final int C1 = 2;
//    static public final int U1 = 3;
//    static public final int V1 = 4;
//    static public final int X2 = 5;
//    static public final int Y2 = 6;
//    static public final int C2 = 7;
//    static public final int U2 = 8;
//    static public final int V2 = 9;
//    static public final int X3 = 10;
//    static public final int Y3 = 11;
//    static public final int C3 = 12;
//    static public final int U3 = 13;
//    static public final int V3 = 14;
//    static public final int X4 = 15;
//    static public final int Y4 = 16;
//    static public final int C4 = 17;
//    static public final int U4 = 18;
//    static public final int V4 = 19;

    public static GLRenderer get(GLSurface surface) {
        GLRenderer renderer = renderers.get(surface);
        if (renderer == null) {
            LOGGER.log(Level.FINE, "Creating renderer for {0}", surface);
            renderer = new GLRenderer(surface);
            renderers.put(surface, renderer);
        }
        return renderer;
    }

    public static void safe() {
        GLRenderer r = get(null);
        r.activate();
        r.flush();
        active = null;
    }

    public static void flushActive() {
        if (active != null) {
            active.flush();
        }
    }

    public static void clearAll() {
        safe();
        for (GLRenderer r : renderers.values()) {
            r.dispose();
        }
        renderers.clear();
    }
}
