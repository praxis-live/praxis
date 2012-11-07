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
package net.neilcsmith.praxis.video.opengl.internal;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;
import org.lwjgl.opengl.GL13;

/** <p>
 * A Texture wraps a standard OpenGL ES texture.
 * </p>
 * 
 * <p>
 * A Texture can be managed. If the OpenGL context is lost all managed textures get invalidated. This happens when a user switches
 * to another application or receives an incoming call. Managed textures get reloaded automatically.
 * </p>
 * 
 * <p>
 * A Texture has to be bound via the {@link Texture#bind()} method in order for it to be applied to geometry. The texture will be
 * bound to the currently active texture unit specified via {@link GLCommon#glActiveTexture(int)}.
 * </p>
 * 
 * <p>
 * You can draw {@link Pixmap}s to a texture at any time. The changes will be automatically uploaded to texture memory. This is of
 * course not extremely fast so use it with care. It also only works with unmanaged textures.
 * </p>
 * 
 * <p>
 * A Texture must be disposed when it is no longer used
 * </p>
 * 
 * @author badlogicgames@gmail.com */
public class Texture implements Disposable {

    private static int dstImageFormat = GL11.GL_RGBA8;

    public enum TextureFilter {

        Nearest(GL11.GL_NEAREST), Linear(GL11.GL_LINEAR), MipMap(GL11.GL_LINEAR_MIPMAP_LINEAR), MipMapNearestNearest(
        GL11.GL_NEAREST_MIPMAP_NEAREST), MipMapLinearNearest(GL11.GL_LINEAR_MIPMAP_NEAREST), MipMapNearestLinear(
        GL11.GL_NEAREST_MIPMAP_LINEAR), MipMapLinearLinear(GL11.GL_LINEAR_MIPMAP_LINEAR);
        final int glEnum;

        TextureFilter(int glEnum) {
            this.glEnum = glEnum;
        }

        public boolean isMipMap() {
            return glEnum != GL11.GL_NEAREST && glEnum != GL11.GL_LINEAR;
        }

        public int getGLEnum() {
            return glEnum;
        }
    }

    public enum TextureWrap {

        ClampToEdge(GL12.GL_CLAMP_TO_EDGE), Repeat(GL11.GL_REPEAT);
        final int glEnum;

        TextureWrap(int glEnum) {
            this.glEnum = glEnum;
        }

        public int getGLEnum() {
            return glEnum;
        }
    }
    private static final IntBuffer buffer = BufferUtils.createIntBuffer(1);
    TextureFilter minFilter = TextureFilter.Linear;
    TextureFilter magFilter = TextureFilter.Linear;
    TextureWrap uWrap = TextureWrap.ClampToEdge;
    TextureWrap vWrap = TextureWrap.ClampToEdge;
    int glHandle;
//	TextureData data;
    private int width;
    private int height;
    
    private GLContext context;
    private FrameBuffer frameBuffer;

    public Texture(int width, int height) {
        this.width = width;
        this.height = height;
//        init();
    }

    private void init() {
        glHandle = createGLHandle();
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, glHandle);
        GL11.glPixelStorei(GL11.GL_UNPACK_ALIGNMENT, 1);
        GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, dstImageFormat, width, height, 0,
                GL12.GL_BGRA, GL12.GL_UNSIGNED_INT_8_8_8_8_REV, BufferUtils.createIntBuffer(width*height));
        setFilter(minFilter, magFilter);
        setWrap(uWrap, vWrap);
        getFrameBuffer();
    }

    private static int createGLHandle() {
        buffer.position(0);
        buffer.limit(buffer.capacity());
        GL11.glGenTextures(buffer);
        return buffer.get(0);
    }


    /** Binds this texture. The texture will be bound to the currently active texture unit specified via
     * {@link GLCommon#glActiveTexture(int)}. */
    public void bind() {
        GLContext cur = GLContext.getCurrent();
        if (context != cur) {
            context = cur;
            init();   
        }
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, glHandle);
    }

//    /** Binds the texture to the given texture unit. Sets the currently active texture unit via
//     * {@link GLCommon#glActiveTexture(int)}.
//     * @param unit the unit (0 to MAX_TEXTURE_UNITS). */
//    public void bind(int unit) {
//        GL13.glActiveTexture(GL13.GL_TEXTURE0 + unit);
//        GL11.glBindTexture(GL11.GL_TEXTURE_2D, glHandle);
//    }


    /** @return the width of the texture in pixels */
    public int getWidth() {
        return width;
    }

    /** @return the height of the texture in pixels */
    public int getHeight() {
        return height;
    }

    public TextureFilter getMinFilter() {
        return minFilter;
    }

    public TextureFilter getMagFilter() {
        return magFilter;
    }

    public TextureWrap getUWrap() {
        return uWrap;
    }

    public TextureWrap getVWrap() {
        return vWrap;
    }

    public int getTextureObjectHandle() {
        return glHandle;
    }

    /** Sets the {@link TextureWrap} for this texture on the u and v axis. This will bind this texture!
     * 
     * @param u the u wrap
     * @param v the v wrap */
    public void setWrap(TextureWrap u, TextureWrap v) {
        this.uWrap = u;
        this.vWrap = v;
        bind();
        GL11.glTexParameterf(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S, u.getGLEnum());
        GL11.glTexParameterf(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T, v.getGLEnum());
    }

    public void setFilter(TextureFilter minFilter, TextureFilter magFilter) {
        this.minFilter = minFilter;
        this.magFilter = magFilter;
        bind();
        GL11.glTexParameterf(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, minFilter.getGLEnum());
        GL11.glTexParameterf(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, magFilter.getGLEnum());
    }
    
    public FrameBuffer getFrameBuffer() {
        if (frameBuffer == null) {
            frameBuffer = new FrameBuffer(this);
        }
        return frameBuffer;
    }

    /** Disposes all resources associated with the texture */
    public void dispose() {
        // this is a hack. reason: we have to set the glHandle to 0 for textures that are
        // reloaded through the asset manager as we first remove (and thus dispose) the texture
        // and then reload it. the glHandle is set to 0 in invalidateAllTextures prior to
        // removal from the asset manager.
        if (glHandle == 0) {
            return;
        }
        if (frameBuffer != null) {
            frameBuffer.dispose();
        }
        buffer.put(0, glHandle);
        GL11.glDeleteTextures(buffer);
        glHandle = 0;
    }

}
