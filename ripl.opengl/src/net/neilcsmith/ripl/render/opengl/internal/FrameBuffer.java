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

import java.nio.IntBuffer;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.lwjgl.BufferUtils;
import static org.lwjgl.opengl.EXTFramebufferObject.*;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL14;

/** <p>
 * Encapsulates OpenGL ES 2.0 frame buffer objects. This is a simple helper class which should cover most FBO uses. It will
 * automatically create a texture for the color attachment and a renderbuffer for the depth buffer. You can get a hold of the
 * texture by {@link FrameBuffer#getColorBufferTexture()}. This class will only work with OpenGL ES 2.0.
 * </p>
 * 
 * <p>
 * FrameBuffers are managed. In case of an OpenGL context loss, which only happens on Android when a user switches to another
 * application or receives an incoming call, the framebuffer will be automatically recreated.
 * </p>
 * 
 * <p>
 * A FrameBuffer must be disposed if it is no longer needed
 * </p>
 * 
 * @author mzechner */
public class FrameBuffer implements Disposable {
    
    private final static Logger LOGGER = Logger.getLogger(FrameBuffer.class.getName());

    /** the frame buffers **/
//    private final static Map<Application, List<FrameBuffer>> buffers = new HashMap<Application, List<FrameBuffer>>();
    /** the color buffer texture **/
    private Texture colorTexture;
    /** the framebuffer handle **/
    private int framebufferHandle;
    /** the depthbuffer render object handle **/
    private int depthbufferHandle;
    /** width **/
    private final int width;
    /** height **/
    private final int height;
    /** depth **/
    private final boolean hasDepth;
    
    private GLContext context;

    public FrameBuffer(Texture texture) {
        this(texture, false);
    }

    /** Creates a new FrameBuffer having the given dimensions and potentially a depth buffer attached.
     * 
     * @param format the format of the color buffer
     * @param width the width of the framebuffer in pixels
     * @param height the height of the framebuffer in pixels
     * @param hasDepth whether to attach a depth buffer
     * @throws GdxRuntimeException in case the FraeBuffer could not be created */
    public FrameBuffer(Texture texture, boolean hasDepth) {
        this.width = texture.getWidth();
        this.height = texture.getHeight();
        this.hasDepth = hasDepth;
        colorTexture = texture;
//        build();

    }

    private void build() {
        LOGGER.fine("Building FBO");
//        ByteBuffer tmp = ByteBuffer.allocateDirect(4);
//        tmp.order(ByteOrder.nativeOrder());
//        IntBuffer handle = tmp.asIntBuffer();
        IntBuffer handle = BufferUtils.createIntBuffer(1);

        glGenFramebuffersEXT(handle);
        framebufferHandle = handle.get(0);

        if (hasDepth) {
//            gl.glGenRenderbuffers(1, handle);
            glGenRenderbuffersEXT(handle);
            depthbufferHandle = handle.get(0);
        }

//        GL11.glBindTexture(GL11.GL_TEXTURE_2D, colorTexture.getTextureObjectHandle());
        colorTexture.bind();

        if (hasDepth) {
            glBindRenderbufferEXT(GL_RENDERBUFFER_EXT, depthbufferHandle);
            glRenderbufferStorageEXT(GL_RENDERBUFFER_EXT, GL14.GL_DEPTH_COMPONENT16, colorTexture.getWidth(),
                    colorTexture.getHeight());
        }

//        gl.glBindFramebuffer(GL20.GL_FRAMEBUFFER, framebufferHandle);
        glBindFramebufferEXT(GL_FRAMEBUFFER_EXT, framebufferHandle);
        glFramebufferTexture2DEXT(GL_FRAMEBUFFER_EXT, GL_COLOR_ATTACHMENT0_EXT, GL11.GL_TEXTURE_2D,
                colorTexture.getTextureObjectHandle(), 0);
        if (hasDepth) {
            glFramebufferRenderbufferEXT(GL_FRAMEBUFFER_EXT, GL_DEPTH_ATTACHMENT_EXT, GL_RENDERBUFFER_EXT, depthbufferHandle);
        }
        int result = glCheckFramebufferStatusEXT(GL_FRAMEBUFFER_EXT);

        glBindRenderbufferEXT(GL_RENDERBUFFER_EXT, 0);
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, 0);
        glBindFramebufferEXT(GL_FRAMEBUFFER_EXT, 0);

        if (result != GL_FRAMEBUFFER_COMPLETE_EXT) {
            LOGGER.log(Level.WARNING, "Unable to build FBO");
            colorTexture.dispose();
            if (hasDepth) {
                handle.put(depthbufferHandle);
                handle.flip();
                glDeleteRenderbuffersEXT(handle);
            }

            colorTexture.dispose();
            handle.put(framebufferHandle);
            handle.flip();
            glDeleteFramebuffersEXT(handle);

            if (result == GL_FRAMEBUFFER_INCOMPLETE_ATTACHMENT_EXT) {
                throw new IllegalStateException("frame buffer couldn't be constructed: incomplete attachment");
            }
            if (result == GL_FRAMEBUFFER_INCOMPLETE_DIMENSIONS_EXT) {
                throw new IllegalStateException("frame buffer couldn't be constructed: incomplete dimensions");
            }
            if (result == GL_FRAMEBUFFER_INCOMPLETE_MISSING_ATTACHMENT_EXT) {
                throw new IllegalStateException("frame buffer couldn't be constructed: missing attachment");
            }
        }
    }

    /** Releases all resources associated with the FrameBuffer. */
    public void dispose() {

//        ByteBuffer tmp = ByteBuffer.allocateDirect(4);
//        tmp.order(ByteOrder.nativeOrder());
//        IntBuffer handle = tmp.asIntBuffer();
        IntBuffer handle = BufferUtils.createIntBuffer(1);

//        colorTexture.dispose();
        if (hasDepth) {
            handle.put(depthbufferHandle);
            handle.flip();
            glDeleteRenderbuffersEXT(handle);
        }

        handle.put(framebufferHandle);
        handle.flip();
        glDeleteFramebuffersEXT(handle);

    }

    /** Makes the frame buffer current so everything gets drawn to it. */
    public void begin() {
        GL11.glViewport(0, 0, colorTexture.getWidth(), colorTexture.getHeight());
        bind();
    }

    /** Unbinds the framebuffer, all drawing will be performed to the normal framebuffer from here on. */
    public void end() {
        GL11.glViewport(0, 0, GLContext.getCurrent().getWidth(), GLContext.getCurrent().getHeight());
        glBindFramebufferEXT(GL_FRAMEBUFFER_EXT, 0);
    }
    
    public void bind() {
        GLContext cur = GLContext.getCurrent();
        if (context != cur) {
            build();
            context = cur;
        }
        glBindFramebufferEXT(GL_FRAMEBUFFER_EXT, framebufferHandle);
    }


    /** @return the color buffer texture */
    public Texture getColorBufferTexture() {
        return colorTexture;
    }

    /** @return the height of the framebuffer in pixels */
    public int getHeight() {
        return colorTexture.getHeight();
    }

    /** @return the width of the framebuffer in pixels */
    public int getWidth() {
        return colorTexture.getWidth();
    }
    
    public static void unbind() {
        glBindFramebufferEXT(GL_FRAMEBUFFER_EXT, 0);
    }
    
}
