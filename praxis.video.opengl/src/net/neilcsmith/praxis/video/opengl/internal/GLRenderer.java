/**
 * *****************************************************************************
 * Copyright 2011 See AUTHORS file.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 * ****************************************************************************
 */
package net.neilcsmith.praxis.video.opengl.internal;

import java.nio.IntBuffer;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.neilcsmith.praxis.video.opengl.internal.VertexAttributes.Usage;
import net.neilcsmith.praxis.video.render.Surface;
import net.neilcsmith.praxis.video.render.utils.PixelArrayCache;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;
import org.lwjgl.opengl.GL14;

/**
 * <p>
 *
 * @author mzechner
 */
// derived from SpriteBatch in libgdx
public class GLRenderer implements Disposable {

    private final static Logger LOGGER = Logger.getLogger(GLRenderer.class.getName());
//    private final static Map<GLSurface, GLRenderer> renderers =
//            new HashMap<GLSurface, GLRenderer>();
//    private static GLRenderer active;
    private GLContext context;
    static final int VERTEX_SIZE = 2 + 1 + 2;
    static final int SPRITE_SIZE = 4 * VERTEX_SIZE;
    private Mesh mesh;
    private Mesh[] buffers;
    private Texture tex0 = null;
    private int idx = 0;
    private int currBufferIdx = 0;
    private final float[] vertices;
    private final Matrix4 transformMatrix = new Matrix4();
    private final Matrix4 projectionMatrix = new Matrix4();
    private final Matrix4 combinedMatrix = new Matrix4();
    private boolean blendingDisabled = false;
    private int blendSrcFunc = GL11.GL_ONE;
    private int blendDstFunc = GL11.GL_ZERO;
    private int blendSrcAlphaFunc = GL11.GL_ONE;
    private int blendDstAlphaFunc = GL11.GL_ZERO;
    private ShaderProgram shader;
    private float color = Color.WHITE.toFloatBits();
    private ShaderProgram customShader = null;
    private GLSurface surface;
    private Texture target;
    private boolean active;
    private IntBuffer scratchBuffer;
    private Texture emptyTexture;

    GLRenderer(GLContext context) {
        this(context, 1000, 1);
    }

    /**
     * <p> Constructs a new TextureRenderer. Sets the projection matrix to an
     * orthographic projection with y-axis point upwards, x-axis point to the
     * right and the origin being in the bottom left corner of the screen. The
     * projection will be pixel perfect with respect to the screen resolution.
     * </p>
     *
     * <p> The size parameter specifies the maximum size of a single batch in
     * number of sprites </p>
     *
     * @param size the batch size in number of sprites
     * @param buffers the number of buffers to use. only makes sense with VBOs.
     * This is an expert function.
     */
    private GLRenderer(GLContext context, int size, int buffers) {

        this.context = context;

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

        createShader();

        createEmptyTexture();

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

    private void createEmptyTexture() {
        GLSurfaceData sd = new GLSurfaceData(32, 32, true);
        sd.pixels = PixelArrayCache.acquire(32 * 32, false);
        sd.texture = new Texture(32, 32);
        int white = 0xFFFFFFFF;
        Arrays.fill(sd.pixels, white);
        syncPixelsToTexture(sd);
        PixelArrayCache.release(sd.pixels);
        emptyTexture = sd.texture;
    }

    public void clear() {
        activate();
        tex0 = null;
        idx = 0;
        if (surface == null) {
            LOGGER.finest("Clearing screen");
            GL11.glClearColor(0, 0, 0, 1);
            GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);
        } else {
            LOGGER.finest("Clearing surface");
            if (surface.hasAlpha()) {
                GL11.glClearColor(0, 0, 0, 0);
            } else {
                GL11.glClearColor(0, 0, 0, 1);
            }
            GL11.glClear(GL11.GL_COLOR_BUFFER_BIT);
        }
    }

    public void target(GLSurface surface) {
        if (surface == this.surface) {
            return;
        }
        flush();
        this.surface = surface;
    }

    void invalidate(GLSurface surface) {
        if (this.surface == surface) {
            target(null);
        }
    }

    private void activate() {
        if (active) {
            return;
        }

        GL11.glDepthMask(false);
        GL11.glEnable(GL11.GL_TEXTURE_2D);

        FrameBuffer fbo = null;
        boolean clear = false;
        target = null;

        if (surface != null) {
            GLSurfaceData data = surface.getData();
            if (data == null) {
                LOGGER.finest("Setting up render to empty surface");
                data = new GLSurfaceData(surface.getWidth(), surface.getHeight(), surface.hasAlpha());
                data.texture = context.getTextureManager().acquire(data.width, data.height);
                surface.setData(data);
                clear = true;
            } else if (data.usage > 1) {
                LOGGER.finest("Setting up render to shared surface");
                data.usage--;
                if (data.texture == null) {
                    data.texture = context.getTextureManager().acquire(data.width, data.height);
                    syncPixelsToTexture(data);
                }
                data.texture.getFrameBuffer().bind();
                data = new GLSurfaceData(surface.getWidth(), surface.getHeight(), surface.hasAlpha());
                data.texture = context.getTextureManager().acquire(data.width, data.height);
                data.texture.bind();
                GL11.glCopyTexSubImage2D(GL11.GL_TEXTURE_2D, 0, 0, 0, 0, 0, data.width, data.height);
                surface.setData(data);
            } else if (data.texture == null) {
                LOGGER.finest("Setting up render to pixel backed surface");
                data.texture = context.getTextureManager().acquire(data.width, data.height);
                syncPixelsToTexture(data);
            }
            data.pixels = null;
            target = data.texture;
            fbo = data.texture.getFrameBuffer();
        }


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

        active = true;

        if (clear) {
            clear();
        }

    }

    public void flush() {

        renderMesh();

        tex0 = null;

        if (customShader != null) {
            customShader.end();
        } else {
            shader.end();
        }

        active = false;

    }

    /**
     * Sets the color used to tint images when they are added to the
     * TextureRenderer. Default is {@link Color#WHITE}.
     */
    public void setColor(Color tint) {
        color = tint.toFloatBits();
    }

    /**
     * @see #setColor(Color)
     */
    public void setColor(float r, float g, float b, float a) {
        int intBits = (int) (255 * a) << 24 | (int) (255 * b) << 16 | (int) (255 * g) << 8 | (int) (255 * r);
        color = Float.intBitsToFloat(intBits & 0xfeffffff);
    }

    /**
     * @see #setColor(Color)
     * @see Color#toFloatBits()
     */
    public void setColor(float color) {
        this.color = color;
    }
//    /**
//     * @return the rendering color of this TextureRenderer. Manipulating the
//     * returned instance has no effect.
//     */
//    public Color getColor() {
//        int intBits = Float.floatToRawIntBits(color);
//        Color color = this.tempColor;
//        color.r = (intBits & 0xff) / 255f;
//        color.g = ((intBits >>> 8) & 0xff) / 255f;
//        color.b = ((intBits >>> 16) & 0xff) / 255f;
//        color.a = ((intBits >>> 24) & 0xff) / 255f;
//        return color;
//    }
    private TextureRegion region = new TextureRegion();

    public void draw(Surface src, float x, float y) {
        draw(src, 0, 0, src.getWidth(), src.getHeight(), x, y);
    }

    public void draw(Surface src, float x, float y, float width, float height) {
        draw(src, 0, 0, src.getWidth(), src.getHeight(), x, y, width, height);
    }

    public void draw(Surface src, float srcX, float srcY, float srcWidth, float srcHeight, float x, float y) {
        draw(src, srcX, srcY, srcWidth, srcHeight, x, y, srcWidth, srcHeight);
    }

    public void draw(Surface src, float srcX, float srcY, float srcWidth, float srcHeight,
            float x, float y, float width, float height) {
        activate();
        TextureRegion tr = initRegion(region, src, (int) srcX, (int) srcY, (int) (srcWidth + 0.5f), (int) (srcHeight + 0.5f));
//        GLSurfaceData data = src.data;
        if (tr == null) {
            LOGGER.finest("Drawing empty Surface - using empty texture");
            float col = color;
            if (src.hasAlpha()) {
                setColor(0, 0, 0, 0);
            } else {
                float a = ((Float.floatToRawIntBits(col) >>> 24) & 0xff) / 255f;
                setColor(0, 0, 0, a);
            }
            region.setRegion(emptyTexture);
            draw(region, x, y, width, height);
            color = col;
        } else {
//            if (data.texture == null) {
//                LOGGER.finest("Surface texture is null - uploading pixels");
//                data.texture = context.getTextureManager().acquire(data.width, data.height);
//                syncPixelsToTexture(data);
//            }
//            region.setTexture(data.texture);
//            region.setRegion((int) srcX, (int) srcY, (int) srcWidth, (int) srcHeight);
            draw(region, x, y, width, height);
        }
    }

    public void draw(Surface src, float srcX, float srcY, float srcWidth, float srcHeight,
            float[] vts) {
        activate();
        TextureRegion tr = initRegion(region, src, (int) srcX, (int) srcY, (int) (srcWidth + 0.5f), (int) (srcHeight + 0.5f));
        if (tr == null) {
            LOGGER.finest("Drawing empty Surface - using empty texture");
            float col = color;
            if (src.hasAlpha()) {
                setColor(0, 0, 0, 0);
            } else {
                float a = ((Float.floatToRawIntBits(col) >>> 24) & 0xff) / 255f;
                setColor(0, 0, 0, a);
            }
            region.setRegion(emptyTexture);
            draw(region, vts);
            color = col;
        } else {
            draw(region, vts);
        }

    }

    /**
     * Draws a rectangle with the bottom left corner at x,y and stretching the
     * region to cover the given width and height.
     */
    private void draw(TextureRegion region, float x, float y, float width, float height) {
//        if (!drawing) {
//            throw new IllegalStateException("SpriteBatch.begin must be called before draw.");
//        }
        Texture texture = region.texture;

        if (texture != tex0) {
            renderMesh();
            tex0 = texture;
//            invTexWidth = 1f / texture.getWidth();
//            invTexHeight = 1f / texture.getHeight();
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

    private void draw(TextureRegion region, float[] vts) {
        Texture texture = region.texture;

        if (texture != tex0) {
            renderMesh();
            tex0 = texture;
        } else if (idx == vertices.length) {
            renderMesh();
        }

//        final float fx2 = x + width;
//        final float fy2 = y + height;
        final float u = region.u;
        final float v = region.v2;
        final float u2 = region.u2;
        final float v2 = region.v;

        vertices[idx++] = vts[0];
        vertices[idx++] = vts[1];
        vertices[idx++] = color;
        vertices[idx++] = u;
        vertices[idx++] = v;

        vertices[idx++] = vts[2];
        vertices[idx++] = vts[3];
        vertices[idx++] = color;
        vertices[idx++] = u;
        vertices[idx++] = v2;

        vertices[idx++] = vts[4];
        vertices[idx++] = vts[5];
        vertices[idx++] = color;
        vertices[idx++] = u2;
        vertices[idx++] = v2;

        vertices[idx++] = vts[6];
        vertices[idx++] = vts[7];
        vertices[idx++] = color;
        vertices[idx++] = u2;
        vertices[idx++] = v;
    }

    private TextureRegion initRegion(TextureRegion region, Surface src, int x, int y, int width, int height) {
        if (src instanceof GLSurface) {
            GLSurfaceData data = ((GLSurface) src).getData();
            if (data == null) {
                return null;
            }
            if (data.texture == null) {
                LOGGER.finest("Surface texture is null - uploading pixels");
                data.texture = context.getTextureManager().acquire(data.width, data.height);
                syncPixelsToTexture(data);
            }
            region.setTexture(data.texture);
            region.setRegion(x, y, width, height);
            return region;
        } else {
            context.getTextureManager().initTextureRegion(region, src, x, y, width, height);
            return region;
        }
    }

    private void renderMesh() {

        if (idx == 0) {
            return;
        }

        int spritesInBatch = idx / 20;

        if (tex0 == null) {
            LOGGER.log(Level.WARNING, "Texture is null - returning : idx = {0}", idx);
            return;
        }

        tex0.bind();
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
            LOGGER.finest("Rendering with custom shader");
            mesh.render(customShader, GL11.GL_TRIANGLES, 0, spritesInBatch * 6);
        } else {
            LOGGER.finest("Rendering with default shader");
            mesh.render(shader, GL11.GL_TRIANGLES, 0, spritesInBatch * 6);
        }


        idx = 0;
        currBufferIdx++;
        if (currBufferIdx == buffers.length) {
            currBufferIdx = 0;
        }
        mesh = buffers[currBufferIdx];
    }

    /**
     * Sets the blending function to be used when rendering sprites.
     *
     * @param srcFunc the source function, e.g. GL11.GL_SRC_ALPHA
     * @param dstFunc the destination function, e.g. GL11.GL_ONE_MINUS_SRC_ALPHA
     */
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

    /**
     * Disposes all resources associated with this TextureRenderer
     */
    public void dispose() {
        for (int i = 0; i < buffers.length; i++) {
            buffers[i].dispose();
        }
        if (shader != null) {
            shader.dispose();
        }
        if (emptyTexture != null) {
            emptyTexture.dispose();
        }
    }

    private void setupMatrices() {
        combinedMatrix.set(projectionMatrix).mul(transformMatrix);
        ShaderProgram prog = customShader == null ? shader : customShader;
        if (prog.hasUniform("u_projectionViewMatrix")) {
            prog.setUniformMatrix("u_projectionViewMatrix", combinedMatrix);
        }
        if (prog.hasUniform("u_texture")) {
            prog.setUniformi("u_texture", 0);
        }


    }

    private void setShader(ShaderProgram shader) {
        activate();
        renderMesh();
        if (customShader != null) {
            LOGGER.log(Level.FINE, "Unbinding custom shader");
            customShader.end();
        } else {
            LOGGER.log(Level.FINE, "Unbinding default shader");
            this.shader.end();
        }

        customShader = shader;

        if (customShader != null) {
            customShader.begin();
            LOGGER.log(Level.FINE, "Binding custom shader");
        } else {
            this.shader.begin();
            LOGGER.log(Level.FINE, "Binding default shader");
        }
        setupMatrices();


    }

    public void bind(ShaderProgram shader) {
        setShader(shader);
    }

    public void unbind(ShaderProgram shader) {
        if (customShader == shader) {
            setShader(null);
        }
    }

    void syncTextureToPixels(GLSurfaceData data) {
        LOGGER.fine("Copying texture to pixels");
        if (data.texture == target) {
            LOGGER.fine("Texture is current render target");
            flush();
        }
        int tWidth = data.texture.getWidth();
        int size = tWidth * data.texture.getHeight();
        if (scratchBuffer == null || scratchBuffer.capacity() < size) {
            scratchBuffer = BufferUtils.createIntBuffer(size);
        }
        scratchBuffer.rewind();
        data.texture.bind();
        GL11.glGetTexImage(GL11.GL_TEXTURE_2D,
                0,
                GL12.GL_BGRA,
                GL12.GL_UNSIGNED_INT_8_8_8_8_REV,
                scratchBuffer);
        scratchBuffer.rewind();
        int offset = 0;
        for (int y = 0; y < data.height; y++) {
            scratchBuffer.position(offset);
            scratchBuffer.get(data.pixels, y * data.width, data.width);
            offset += tWidth;
        }
    }

    void syncPixelsToTexture(GLSurfaceData data) {
        LOGGER.fine("Copying pixels to texture");
        if (data.texture == target) {
            LOGGER.log(Level.FINE, "Texture is current render target");
            flush();
        }
        int size = data.width * data.height;
        if (scratchBuffer == null || scratchBuffer.capacity() < size) {
            scratchBuffer = BufferUtils.createIntBuffer(size);
        }
        scratchBuffer.rewind();
        scratchBuffer.put(data.pixels, 0, size);
        scratchBuffer.rewind();
        syncPixelBufferToTexture(scratchBuffer, data.texture, data.alpha, 0, 0, data.width, data.height);

    }
    
    void syncPixelBufferToTexture(IntBuffer buffer, 
            Texture texture, 
            boolean alpha, 
            int x, int y, int width, int height ) {
        texture.bind();
        if (!alpha) {
            GL11.glPixelTransferf(GL11.GL_ALPHA_BIAS, 1);
        }
        GL11.glTexSubImage2D(GL11.GL_TEXTURE_2D,
                0,
                x,
                y,
                width,
                height,
                GL12.GL_BGRA,
                GL12.GL_UNSIGNED_INT_8_8_8_8_REV,
                buffer);
        if (!alpha) {
            GL11.glPixelTransferf(GL11.GL_ALPHA_BIAS, 0);
        }
    }


}
