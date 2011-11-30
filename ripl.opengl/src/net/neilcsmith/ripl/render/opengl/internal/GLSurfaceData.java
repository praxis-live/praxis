/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Neil C Smith.
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
package net.neilcsmith.ripl.render.opengl.internal;

import java.nio.IntBuffer;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.neilcsmith.ripl.PixelData;
import net.neilcsmith.ripl.utils.PixelArrayCache;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;

/**
 *
 * @author Neil C Smith (http://neilcsmith.net)
 */
public class GLSurfaceData implements PixelData {

    private final static Logger LOGGER = Logger.getLogger(GLSurfaceData.class.getName());
    private static IntBuffer buffer;
    private int width;
    private int height;
    private boolean alpha;
    private int[] pixels;
    private Texture texture;
    private int usage;
    boolean clearPending;

    private GLSurfaceData(int w, int h, boolean a, boolean clear) {
        width = w;
        height = h;
        alpha = a;
        clearPending = clear;
        usage = 1;
    }

    @Override
    public int[] getData() {
        if (pixels == null) {
            pixels = PixelArrayCache.acquire(width * height, false);
            if (texture != null) {
                try {
                    copy(texture, pixels);
                    TextureCache.release(texture);
                    texture = null;
                } catch (Exception ex) {
                    LOGGER.log(Level.WARNING, "Failed to copy texture to pixels", ex);
                }
            } else if (clearPending) {
                Arrays.fill(pixels, 0, width * height, 0);
                clearPending = false;
            }
        }
        return pixels;
    }

    @Override
    public int getOffset() {
        return 0;
    }

    @Override
    public int getScanline() {
        return width;
    }

    @Override
    public int getWidth() {
        return width;
    }

    @Override
    public int getHeight() {
        return height;
    }

    @Override
    public boolean hasAlpha() {
        return alpha;
    }

    GLSurfaceData acquire() {
        usage++;
        return this;
    }

    void release() {
        usage--;
        if (usage <= 0) {
            if (pixels != null) {
                PixelArrayCache.release(pixels);
                pixels = null;
            }
            if (texture != null) {
                TextureCache.release(texture);
                texture = null;
            }
        }
    }

    GLSurfaceData getUnshared() {
        if (usage > 1) {
            GLSurfaceData copy = new GLSurfaceData(width, height, alpha, false);
            if (pixels != null) {
                System.arraycopy(pixels, 0, copy.getData(), 0, width * height);
            } else if (texture != null) {
                GLRenderer.safe();
                texture.getFrameBuffer().bind();
                GL11.glEnable(GL11.GL_TEXTURE_2D);
                copy.getTexture().bind();        
                GL11.glCopyTexSubImage2D(GL11.GL_TEXTURE_2D, 0, 0, 0, 0, 0, width, height);
                FrameBuffer.unbind();
            }
            release();
            return copy;

        } else if (usage == 1) {
            return this;
        } else {
            throw new IllegalStateException();
        }
    }

    Texture getTexture() {
        if (texture == null) {
            texture = TextureCache.acquire(width, height);
            if (pixels != null) {
                copy(pixels, texture);
                PixelArrayCache.release(pixels);
                pixels = null;
            } else if (clearPending) {
                GLRenderer.safe();
                texture.getFrameBuffer().bind();
                if (alpha) {
                    GL11.glClearColor(0, 0, 0, 0);
                } else {
                    GL11.glClearColor(0, 0, 0, 1);
                }
                GL11.glClear(GL11.GL_COLOR_BUFFER_BIT);
                clearPending = false;
                FrameBuffer.unbind();
            }
        }
        return texture;
    }

    private void copy(Texture texture, int[] pixels) {
//        System.out.println("Copying texture to pixels");
        LOGGER.fine("Copying texture to pixels");
        GLRenderer.safe();
        int tWidth = texture.getWidth();
        int size = tWidth * texture.getHeight();
        if (buffer == null || buffer.capacity() < size) {
            buffer = BufferUtils.createIntBuffer(size);
        }
        buffer.rewind();
        texture.bind();
        GL11.glGetTexImage(GL11.GL_TEXTURE_2D,
                0,
                GL12.GL_BGRA,
                GL12.GL_UNSIGNED_INT_8_8_8_8_REV,
                buffer);
        buffer.rewind();
//        if (tWidth == width) {
//            buffer.get(pixels, 0, size);
//        } else {
            int offset = 0;
            for (int y = 0; y < height; y++) {
                buffer.position(offset);
                buffer.get(pixels, y * width, width);
                offset += tWidth;
            }
//        }

    }

    private void copy(int[] pixels, Texture texture) {
        
//        System.out.println("Copying pixels to texture");
        LOGGER.fine("Copying pixels to texture");

        GLRenderer.safe();
        int size = width * height;
        if (buffer == null || buffer.capacity() < size) {
            buffer = BufferUtils.createIntBuffer(size);
        }
        texture.bind();
        buffer.rewind();
        buffer.put(pixels, 0, size);
        buffer.rewind();
        if (!alpha) {
            GL11.glPixelTransferf(GL11.GL_ALPHA_BIAS, 1);
        }
        GL11.glTexSubImage2D(GL11.GL_TEXTURE_2D,
                0,
                0,
                0,
                width,
                height,
                GL12.GL_BGRA,
                GL12.GL_UNSIGNED_INT_8_8_8_8_REV,
                buffer);
        if (!alpha) {
            GL11.glPixelTransferf(GL11.GL_ALPHA_BIAS, 0);
        }

    }

    static GLSurfaceData createSurfaceData(int width, int height,
            boolean alpha, boolean clear) {
        return new GLSurfaceData(width, height, alpha, clear);

    }
}
