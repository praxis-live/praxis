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
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL15;

/** <p>
 * A {@link VertexData} implementation based on OpenGL vertex buffer objects.
 * </p>
 * 
 * <p>
 * If the OpenGL ES context was lost you can call {@link #invalidate()} to recreate a new OpenGL vertex buffer object. This class
 * can be used seamlessly with OpenGL ES 1.x and 2.0.
 * </p>
 * 
 * <p>
 * In case OpenGL ES 2.0 is used in the application the data is bound via glVertexAttribPointer() according to the attribute
 * aliases specified via {@link VertexAttributes} in the constructor.
 * </p>
 * 
 * <p>
 * Uses indirect Buffers on Android 1.5/1.6 to fix GC invocation due to leaking PlatformAddress instances.
 * </p>
 * 
 * <p>
 * VertexBufferObjects must be disposed via the {@link #dispose()} method when no longer needed
 * </p>
 * 
 * @author mzechner, Dave Clayton <contact@redskyforge.com> */
public class VertexBufferObject {

    final static IntBuffer tmpHandle = BufferUtils.createIntBuffer(1);
    final VertexAttributes attributes;
    final FloatBuffer buffer;
    final ByteBuffer byteBuffer;
    int bufferHandle;
    final boolean isDirect;
    final boolean isStatic;
    final int usage;
    boolean isDirty = false;
    boolean isBound = false;

    /** Constructs a new interleaved VertexBufferObject.
     * 
     * @param isStatic whether the vertex data is static.
     * @param numVertices the maximum number of vertices
     * @param attributes the {@link VertexAttribute}s. */
    public VertexBufferObject(boolean isStatic, int numVertices, VertexAttribute... attributes) {
        this(isStatic, numVertices, new VertexAttributes(attributes));
    }

    /** Constructs a new interleaved VertexBufferObject.
     * 
     * @param isStatic whether the vertex data is static.
     * @param numVertices the maximum number of vertices
     * @param attributes the {@link VertexAttributes}. */
    public VertexBufferObject(boolean isStatic, int numVertices, VertexAttributes attributes) {
        this.isStatic = isStatic;
        this.attributes = attributes;

        byteBuffer = ByteBuffer.allocateDirect(this.attributes.vertexSize * numVertices);
        byteBuffer.order(ByteOrder.nativeOrder());
        isDirect = true;
        buffer = byteBuffer.asFloatBuffer();
        buffer.flip();
        byteBuffer.flip();
        bufferHandle = createBufferObject();
        usage = isStatic ? GL15.GL_STATIC_DRAW : GL15.GL_DYNAMIC_DRAW;
    }

    private int createBufferObject() {
        GL15.glGenBuffers(tmpHandle);
        return tmpHandle.get(0);
    }

    /** {@inheritDoc} */
    public VertexAttributes getAttributes() {
        return attributes;
    }

    /** {@inheritDoc} */
    public int getNumVertices() {
        return buffer.limit() * 4 / attributes.vertexSize;
    }

    /** {@inheritDoc} */
    public int getNumMaxVertices() {
        return byteBuffer.capacity() / attributes.vertexSize;
    }

    /** {@inheritDoc} */
    public FloatBuffer getBuffer() {
        isDirty = true;
        return buffer;
    }

    /** {@inheritDoc} */
    public void setVertices(float[] vertices, int offset, int count) {
        isDirty = true;
        if (isDirect) {
//            BufferUtils.copy(vertices, byteBuffer, count, offset);
            buffer.clear();
            buffer.put(vertices, offset, count);
            buffer.position(0);
            buffer.limit(count);
        } else {
            buffer.clear();
            buffer.put(vertices, offset, count);
            buffer.flip();
            byteBuffer.position(0);
            byteBuffer.limit(buffer.limit() << 2);
        }

        if (isBound) {
            GL15.glBufferData(GL15.GL_ARRAY_BUFFER, byteBuffer, usage);
            isDirty = false;
        }
    }

//    /** {@inheritDoc} */
//    @Override
//    public void bind() {
//        GL11 gl = Gdx.gl11;
//
//        gl.glBindBuffer(GL11.GL_ARRAY_BUFFER, bufferHandle);
//        if (isDirty) {
//            byteBuffer.limit(buffer.limit() * 4);
//            gl.glBufferData(GL11.GL_ARRAY_BUFFER, byteBuffer.limit(), byteBuffer, usage);
//            isDirty = false;
//        }
//
//        int textureUnit = 0;
//        int numAttributes = attributes.size();
//
//        for (int i = 0; i < numAttributes; i++) {
//            VertexAttribute attribute = attributes.get(i);
//
//            switch (attribute.usage) {
//                case Usage.Position:
//                    gl.glEnableClientState(GL11.GL_VERTEX_ARRAY);
//                    gl.glVertexPointer(attribute.numComponents, GL10.GL_FLOAT, attributes.vertexSize, attribute.offset);
//                    break;
//
//                case Usage.Color:
//                case Usage.ColorPacked:
//                    int colorType = GL10.GL_FLOAT;
//                    if (attribute.usage == Usage.ColorPacked) {
//                        colorType = GL11.GL_UNSIGNED_BYTE;
//                    }
//
//                    gl.glEnableClientState(GL10.GL_COLOR_ARRAY);
//                    gl.glColorPointer(attribute.numComponents, colorType, attributes.vertexSize, attribute.offset);
//                    break;
//
//                case Usage.Normal:
//                    gl.glEnableClientState(GL10.GL_NORMAL_ARRAY);
//                    gl.glNormalPointer(GL10.GL_FLOAT, attributes.vertexSize, attribute.offset);
//                    break;
//
//                case Usage.TextureCoordinates:
//                    gl.glClientActiveTexture(GL10.GL_TEXTURE0 + textureUnit);
//                    gl.glEnableClientState(GL10.GL_TEXTURE_COORD_ARRAY);
//                    gl.glTexCoordPointer(attribute.numComponents, GL10.GL_FLOAT, attributes.vertexSize, attribute.offset);
//                    textureUnit++;
//                    break;
//
//                default:
//                // throw new GdxRuntimeException("unkown vertex attribute type: " + attribute.usage);
//            }
//        }
//
//        isBound = true;
//    }
    /** Binds this VertexBufferObject for rendering via glDrawArrays or glDrawElements
     * 
     * @param shader the shader */
    public void bind(ShaderProgram shader) {

        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, bufferHandle);
        if (isDirty) {
            byteBuffer.limit(buffer.limit() * 4);
            GL15.glBufferData(GL15.GL_ARRAY_BUFFER, byteBuffer, usage);
            isDirty = false;
        }

        int numAttributes = attributes.size();
        for (int i = 0; i < numAttributes; i++) {
            VertexAttribute attribute = attributes.get(i);
            shader.enableVertexAttribute(attribute.alias);
            int colorType = GL11.GL_FLOAT;
            boolean normalize = false;
            if (attribute.usage == VertexAttributes.Usage.ColorPacked) {
                colorType = GL11.GL_UNSIGNED_BYTE;
                normalize = true;
            }
            shader.setVertexAttribute(attribute.alias, attribute.numComponents, colorType, normalize, attributes.vertexSize,
                    attribute.offset);
        }
        isBound = true;
    }

//    /** {@inheritDoc} */
//    @Override
//    public void unbind() {
//        GL11 gl = Gdx.gl11;
//        int textureUnit = 0;
//        int numAttributes = attributes.size();
//
//        for (int i = 0; i < numAttributes; i++) {
//
//            VertexAttribute attribute = attributes.get(i);
//            switch (attribute.usage) {
//                case Usage.Position:
//                    break; // no-op, we also need a position bound in gles
//                case Usage.Color:
//                case Usage.ColorPacked:
//                    gl.glDisableClientState(GL11.GL_COLOR_ARRAY);
//                    break;
//                case Usage.Normal:
//                    gl.glDisableClientState(GL11.GL_NORMAL_ARRAY);
//                    break;
//                case Usage.TextureCoordinates:
//                    gl.glClientActiveTexture(GL11.GL_TEXTURE0 + textureUnit);
//                    gl.glDisableClientState(GL11.GL_TEXTURE_COORD_ARRAY);
//                    textureUnit++;
//                    break;
//                default:
//                // throw new GdxRuntimeException("unkown vertex attribute type: " + attribute.usage);
//            }
//        }
//
//        gl.glBindBuffer(GL11.GL_ARRAY_BUFFER, 0);
//        isBound = false;
//    }
    /** Unbinds this VertexBufferObject.
     * 
     * @param shader the shader */
    public void unbind(ShaderProgram shader) {
        int numAttributes = attributes.size();
        for (int i = 0; i < numAttributes; i++) {
            VertexAttribute attribute = attributes.get(i);
            shader.disableVertexAttribute(attribute.alias);
        }
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);
        isBound = false;
    }

    /** Invalidates the VertexBufferObject so a new OpenGL buffer handle is created. Use this in case of a context loss. */
    public void invalidate() {
        bufferHandle = createBufferObject();
        isDirty = true;
    }

    /** Disposes of all resources this VertexBufferObject uses. */
    public void dispose() {
        tmpHandle.clear();
        tmpHandle.put(bufferHandle);
        tmpHandle.flip();
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);
        GL15.glDeleteBuffers(tmpHandle);
        bufferHandle = 0;


    }
}
