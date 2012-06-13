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

import net.neilcsmith.ripl.render.opengl.internal.Disposable;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.neilcsmith.ripl.render.opengl.internal.GLContext;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL21;

//import com.badlogic.gdx.Application;
//import com.badlogic.gdx.Gdx;
//import com.badlogic.gdx.graphics.GL20;
//import com.badlogic.gdx.graphics.Mesh;
//import com.badlogic.gdx.math.Matrix3;
//import com.badlogic.gdx.math.Matrix4;
//import com.badlogic.gdx.utils.BufferUtils;
//import com.badlogic.gdx.utils.Disposable;
//import com.badlogic.gdx.utils.ObjectMap;

/** <p>
 * A shader program encapsulates a vertex and fragment shader pair linked to form a shader program useable with OpenGL ES 2.0.
 * </p>
 * 
 * <p>
 * After construction a ShaderProgram can be used to draw {@link Mesh}. To make the GPU use a specific ShaderProgram the programs
 * {@link ShaderProgram#begin()} method must be used which effectively binds the program.
 * </p>
 * 
 * <p>
 * When a ShaderProgram is bound one can set uniforms, vertex attributes and attributes as needed via the respective methods.
 * </p>
 * 
 * <p>
 * A ShaderProgram can be unbound with a call to {@link ShaderProgram#end()}
 * </p>
 * 
 * <p>
 * A ShaderProgram must be disposed via a call to {@link ShaderProgram#dispose()} when it is no longer needed
 * </p>
 * 
 * <p>
 * ShaderPrograms are managed. In case the OpenGL context is lost all shaders get invalidated and have to be reloaded. This
 * happens on Android when a user switches to another application or receives an incoming call. Managed ShaderPrograms are
 * automatically reloaded when the OpenGL context is recreated so you don't have to do this manually.
 * </p>
 * 
 * @author mzechner */
public class ShaderProgram implements Disposable {
	/** default name for position attributes **/
	public static final String POSITION_ATTRIBUTE = "a_position";
	/** default name for normal attribtues **/
	public static final String NORMAL_ATTRIBUTE = "a_normal";
	/** default name for color attributes **/
	public static final String COLOR_ATTRIBUTE = "a_color";
	/** default name for texcoords attributes, append texture unit number **/
	public static final String TEXCOORD_ATTRIBUTE = "a_texCoord";
	/** default name for tangent attribute **/
	public static final String TANGENT_ATTRIBUTE = "a_tangent";
	/** default name for binormal attribute **/
	public static final String BINORMAL_ATTRIBUTE = "a_binormal";

	/** flag indicating whether attributes & uniforms must be present at all times **/
	public static boolean pedantic = true;

	/** the list of currently available shaders **/
//	private final static Map<Application, List<ShaderProgram>> shaders = new HashMap<Application, List<ShaderProgram>>();

	/** the log **/
	private String log = "";

	/** whether this program compiled succesfully **/
	private boolean isCompiled;

	/** uniform lookup **/
//	private final ObjectMap<String, Integer> uniforms = new ObjectMap<String, Integer>();
        private final Map<String, Integer> uniforms = new HashMap<String, Integer>();

	/** uniform types **/
//	private final ObjectMap<String, Integer> uniformTypes = new ObjectMap<String, Integer>();
        private final Map<String, Integer> uniformTypes = new HashMap<String, Integer>();

	/** uniform names **/
	private String[] uniformNames;

	/** attribute lookup **/
//	private final ObjectMap<String, Integer> attributes = new ObjectMap<String, Integer>();
        private final Map<String, Integer> attributes = new HashMap<String, Integer>();

	/** attribute types **/
//	private final ObjectMap<String, Integer> attributeTypes = new ObjectMap<String, Integer>();
        private final Map<String, Integer> attributeTypes = new HashMap<String, Integer>();

	/** attribute names **/
	private String[] attributeNames;

	/** program handle **/
	public int program;

	/** vertex shader handle **/
	private int vertexShaderHandle;

	/** fragment shader handle **/
	private int fragmentShaderHandle;

	/** matrix float buffer **/
	private final FloatBuffer matrix;

	/** vertex shader source **/
	private final String vertexShaderSource;

	/** fragment shader source **/
	private final String fragmentShaderSource;

	/** whether this shader was invalidated **/
	private boolean invalidated;

	/** direct buffer for passing float and int uniform arrays **/
	private ByteBuffer buffer = null;
	private FloatBuffer floatBuffer = null;
	private IntBuffer intBuffer = null;

	/** reference count **/
	private int refCount = 0;
        
        private GLContext context;

	/** Construcs a new JOglShaderProgram and immediatly compiles it.
	 * 
	 * @param vertexShader the vertex shader
	 * @param fragmentShader the fragment shader */

	public ShaderProgram (String vertexShader, String fragmentShader) {
		if (vertexShader == null) throw new IllegalArgumentException("vertex shader must not be null");
		if (fragmentShader == null) throw new IllegalArgumentException("fragment shader must not be null");

		this.vertexShaderSource = vertexShader;
		this.fragmentShaderSource = fragmentShader;
//		this.matrix = BufferUtils.newFloatBuffer(16);
                this.matrix = BufferUtils.createFloatBuffer(16);
                
                context = GLContext.getCurrent();
                
                if (context != null) {
                    compileShaders(vertexShader, fragmentShader);
                }
	}

	/** Loads and compiles the shaders, creates a new program and links the shaders.
	 * 
	 * @param vertexShader
	 * @param fragmentShader */
	private void compileShaders (String vertexShader, String fragmentShader) {
		vertexShaderHandle = loadShader(GL20.GL_VERTEX_SHADER, vertexShader);
		fragmentShaderHandle = loadShader(GL20.GL_FRAGMENT_SHADER, fragmentShader);

		if (vertexShaderHandle == -1 || fragmentShaderHandle == -1) {
			isCompiled = false;
			return;
		}

		program = linkProgram();
		if (program == -1) {
			isCompiled = false;
			return;
		}

		isCompiled = true;
                fetchAttributes();
                fetchUniforms();
	}

	private int loadShader (int type, String source) {
//		GL20 gl = Gdx.graphics.getGL20();
		ByteBuffer tmp = ByteBuffer.allocateDirect(4);
		tmp.order(ByteOrder.nativeOrder());
		IntBuffer intbuf = tmp.asIntBuffer();

		int shader = GL20.glCreateShader(type);
		if (shader == 0) return -1;

		GL20.glShaderSource(shader, source);
		GL20.glCompileShader(shader);
		GL20.glGetShader(shader, GL20.GL_COMPILE_STATUS, intbuf);

		int compiled = intbuf.get(0);
		if (compiled == 0) {
			GL20.glGetShader(shader, GL20.GL_INFO_LOG_LENGTH, intbuf);
			int infoLogLength = intbuf.get(0);
			if (infoLogLength > 1) {
				String infoLog = getShaderInfoLog(shader);
				log += infoLog;
			}
			return -1;
		}

		return shader;
	}
        
        private String getShaderInfoLog (int shader) {
		ByteBuffer buffer = ByteBuffer.allocateDirect(1024 * 10);
		buffer.order(ByteOrder.nativeOrder());
		ByteBuffer tmp = ByteBuffer.allocateDirect(4);
		tmp.order(ByteOrder.nativeOrder());
		IntBuffer intBuffer = tmp.asIntBuffer();

		GL20.glGetShaderInfoLog(shader, intBuffer, buffer);
		int numBytes = intBuffer.get(0);
		byte[] bytes = new byte[numBytes];
		buffer.get(bytes);
		return new String(bytes);
	}

	private int linkProgram () {
		int program = GL20.glCreateProgram();
		if (program == 0) return -1;

		GL20.glAttachShader(program, vertexShaderHandle);
		GL20.glAttachShader(program, fragmentShaderHandle);
		GL20.glLinkProgram(program);

//		ByteBuffer tmp = ByteBuffer.allocateDirect(4);
//		tmp.order(ByteOrder.nativeOrder());
//		IntBuffer intbuf = tmp.asIntBuffer();

		GL20.glGetProgram(program, GL20.GL_LINK_STATUS, intbuf);
		int linked = intbuf.get(0);
		if (linked == 0) {
			return -1;
		}

		return program;
	}

	final IntBuffer intbuf = BufferUtils.createIntBuffer(1);

	/** @return the log info for the shader compilation and program linking stage. The shader needs to be bound for this method to
	 *         have an effect. */
	public String getLog () {
		if (isCompiled) {
			GL20.glGetProgram(program, GL20.GL_INFO_LOG_LENGTH, intbuf);
			int infoLogLength = intbuf.get(0);
			if (infoLogLength > 1) log = GL20.glGetProgramInfoLog(program, infoLogLength);
			return log;
		} else {
			return log;
		}
	}

	/** @return whether this ShaderProgram compiled successfully. */
	public boolean isCompiled () {
		return isCompiled;
	}

	private int fetchAttributeLocation (String name) {
		Integer location;
		if ((location = attributes.get(name)) == null) {
			location = GL20.glGetAttribLocation(program, name);
			if (location != -1) attributes.put(name, location);
		}
		return location;
	}

	private int fetchUniformLocation (String name) {
		Integer location;
		if ((location = uniforms.get(name)) == null) {
			location = GL20.glGetUniformLocation(program, name);
			if (location == -1 && pedantic) throw new IllegalArgumentException("no uniform with name '" + name + "' in shader");
			uniforms.put(name, location);
		}
		return location;
	}

	/** Sets the uniform with the given name. Throws an IllegalArgumentException in case it is not called in between a
	 * {@link #begin()}/{@link #end()} block.
	 * 
	 * @param name the name of the uniform
	 * @param value the value */
	public void setUniformi (String name, int value) {
		checkContext();
		int location = fetchUniformLocation(name);
		GL20.glUniform1i(location, value);
	}

	/** Sets the uniform with the given name. Throws an IllegalArgumentException in case it is not called in between a
	 * {@link #begin()}/{@link #end()} block.
	 * 
	 * @param name the name of the uniform
	 * @param value1 the first value
	 * @param value2 the second value */
	public void setUniformi (String name, int value1, int value2) {
		checkContext();
		int location = fetchUniformLocation(name);
		GL20.glUniform2i(location, value1, value2);
	}

	/** Sets the uniform with the given name. Throws an IllegalArgumentException in case it is not called in between a
	 * {@link #begin()}/{@link #end()} block.
	 * 
	 * @param name the name of the uniform
	 * @param value1 the first value
	 * @param value2 the second value
	 * @param value3 the third value */
	public void setUniformi (String name, int value1, int value2, int value3) {
		checkContext();
		int location = fetchUniformLocation(name);
		GL20.glUniform3i(location, value1, value2, value3);
	}

	/** Sets the uniform with the given name. Throws an IllegalArgumentException in case it is not called in between a
	 * {@link #begin()}/{@link #end()} block.
	 * 
	 * @param name the name of the uniform
	 * @param value1 the first value
	 * @param value2 the second value
	 * @param value3 the third value
	 * @param value4 the fourth value */
	public void setUniformi (String name, int value1, int value2, int value3, int value4) {
		checkContext();
		int location = fetchUniformLocation(name);
		GL20.glUniform4i(location, value1, value2, value3, value4);
	}

	/** Sets the uniform with the given name. Throws an IllegalArgumentException in case it is not called in between a
	 * {@link #begin()}/{@link #end()} block.
	 * 
	 * @param name the name of the uniform
	 * @param value the value */
	public void setUniformf (String name, float value) {
		checkContext();
		int location = fetchUniformLocation(name);
		GL20.glUniform1f(location, value);
	}

	/** Sets the uniform with the given name. Throws an IllegalArgumentException in case it is not called in between a
	 * {@link #begin()}/{@link #end()} block.
	 * 
	 * @param name the name of the uniform
	 * @param value1 the first value
	 * @param value2 the second value */
	public void setUniformf (String name, float value1, float value2) {
		checkContext();
		int location = fetchUniformLocation(name);
		GL20.glUniform2f(location, value1, value2);
	}

	/** Sets the uniform with the given name. Throws an IllegalArgumentException in case it is not called in between a
	 * {@link #begin()}/{@link #end()} block.
	 * 
	 * @param name the name of the uniform
	 * @param value1 the first value
	 * @param value2 the second value
	 * @param value3 the third value */
	public void setUniformf (String name, float value1, float value2, float value3) {
		checkContext();
		int location = fetchUniformLocation(name);
		GL20.glUniform3f(location, value1, value2, value3);
	}

	/** Sets the uniform with the given name. Throws an IllegalArgumentException in case it is not called in between a
	 * {@link #begin()}/{@link #end()} block.
	 * 
	 * @param name the name of the uniform
	 * @param value1 the first value
	 * @param value2 the second value
	 * @param value3 the third value
	 * @param value4 the fourth value */
	public void setUniformf (String name, float value1, float value2, float value3, float value4) {
		checkContext();
		int location = fetchUniformLocation(name);
		GL20.glUniform4f(location, value1, value2, value3, value4);
	}

	public void setUniform1fv (String name, float[] values, int offset, int length) {
		checkContext();
		int location = fetchUniformLocation(name);
		ensureBufferCapacity(length << 2);
		floatBuffer.clear();
//		BufferUtils.copy(values, floatBuffer, length, offset);
                floatBuffer.put(values, offset, length);
                floatBuffer.rewind();
                
//		GL20.glUniform1fv(location, length, floatBuffer);
                GL20.glUniform1(location, floatBuffer);
	}

	public void setUniform2fv (String name, float[] values, int offset, int length) {
		checkContext();
		int location = fetchUniformLocation(name);
		ensureBufferCapacity(length << 2);
		floatBuffer.clear();
//		BufferUtils.copy(values, floatBuffer, length, offset);
                floatBuffer.put(values, offset, length);
                floatBuffer.rewind();
//		gl.glUniform2fv(location, length / 2, floatBuffer);
                GL20.glUniform2(location, floatBuffer);
	}

	public void setUniform3fv (String name, float[] values, int offset, int length) {
		checkContext();
		int location = fetchUniformLocation(name);
		ensureBufferCapacity(length << 2);
		floatBuffer.clear();
//		BufferUtils.copy(values, floatBuffer, length, offset);
                floatBuffer.put(values, offset, length);
                floatBuffer.rewind();
		GL20.glUniform3(location, floatBuffer);
	}

	public void setUniform4fv (String name, float[] values, int offset, int length) {
		checkContext();
		int location = fetchUniformLocation(name);
		ensureBufferCapacity(length << 2);
		floatBuffer.clear();
//		BufferUtils.copy(values, floatBuffer, length, offset);
                floatBuffer.put(values, offset, length);
                floatBuffer.rewind();
		GL20.glUniform4(location, floatBuffer);
	}

	/** Sets the uniform matrix with the given name. Throws an IllegalArgumentException in case it is not called in between a
	 * {@link #begin()}/{@link #end()} block.
	 * 
	 * @param name the name of the uniform
	 * @param matrix the matrix */
	public void setUniformMatrix (String name, Matrix4 matrix) {
		setUniformMatrix(name, matrix, false);
	}

	/** Sets the uniform matrix with the given name. Throws an IllegalArgumentException in case it is not called in between a
	 * {@link #begin()}/{@link #end()} block.
	 * 
	 * @param name the name of the uniform
	 * @param matrix the matrix
	 * @param transpose whether the matrix shouls be transposed */
	public void setUniformMatrix (String name, Matrix4 matrix, boolean transpose) {
		checkContext();
		int location = fetchUniformLocation(name);
		this.matrix.clear();
//		BufferUtils.copy(matrix.val, this.matrix, matrix.val.length, 0);
                this.matrix.put(matrix.val);
                this.matrix.rewind();
                
		GL20.glUniformMatrix4(location, transpose, this.matrix);
	}

	/** Sets the uniform matrix with the given name. Throws an IllegalArgumentException in case it is not called in between a
	 * {@link #begin()}/{@link #end()} block.
	 * 
	 * @param name the name of the uniform
	 * @param matrix the matrix */
	public void setUniformMatrix (String name, Matrix3 matrix) {
		setUniformMatrix(name, matrix, false);
	}

	/** Sets the uniform matrix with the given name. Throws an IllegalArgumentException in case it is not called in between a
	 * {@link #begin()}/{@link #end()} block.
	 * 
	 * @param name the name of the uniform
	 * @param matrix the matrix
	 * @param transpose whether the uniform matrix should be transposed */
	public void setUniformMatrix (String name, Matrix3 matrix, boolean transpose) {
		checkContext();
		int location = fetchUniformLocation(name);
		float[] vals = matrix.getValues();
		this.matrix.clear();
//		BufferUtils.copy(vals, this.matrix, vals.length, 0);
                this.matrix.put(vals);
                this.matrix.rewind();
		GL20.glUniformMatrix3(location, transpose, this.matrix);
	}

//	/** Sets the vertex attribute with the given name. Throws an IllegalArgumentException in case it is not called in between a
//	 * {@link #begin()}/{@link #end()} block.
//	 * 
//	 * @param name the attribute name
//	 * @param size the number of components, must be >= 1 and <= 4
//	 * @param type the type, must be one of GL20.GL_BYTE, GL20.GL_UNSIGNED_BYTE, GL20.GL_SHORT,
//	 *           GL20.GL_UNSIGNED_SHORT,GL20.GL_FIXED, or GL20.GL_FLOAT. GL_FIXED will not work on the desktop
//	 * @param normalize whether fixed point data should be normalized. Will not work on the desktop
//	 * @param stride the stride in bytes between successive attributes
//	 * @param buffer the buffer containing the vertex attributes. */
//	public void setVertexAttribute (String name, int size, int type, boolean normalize, int stride, FloatBuffer buffer) {
//		checkManaged();
//		int location = fetchAttributeLocation(name);
//		GL20.glVertexAttribPointer(location, size, type, normalize, stride, buffer);
//        NOT IMPLEMENTED
//	}

	/** Sets the vertex attribute with the given name. Throws an IllegalArgumentException in case it is not called in between a
	 * {@link #begin()}/{@link #end()} block.
	 * 
	 * @param name the attribute name
	 * @param size the number of components, must be >= 1 and <= 4
	 * @param type the type, must be one of GL20.GL_BYTE, GL20.GL_UNSIGNED_BYTE, GL20.GL_SHORT,
	 *           GL20.GL_UNSIGNED_SHORT,GL20.GL_FIXED, or GL20.GL_FLOAT. GL_FIXED will not work on the desktop
	 * @param normalize whether fixed point data should be normalized. Will not work on the desktop
	 * @param stride the stride in bytes between successive attributes
	 * @param offset byte offset into the vertex buffer object bound to GL20.GL_ARRAY_BUFFER. */
	public void setVertexAttribute (String name, int size, int type, boolean normalize, int stride, int offset) {
		checkContext();
		int location = fetchAttributeLocation(name);
		if (location == -1) return;
		GL20.glVertexAttribPointer(location, size, type, normalize, stride, offset);
	}

	/** Makes OpenGL ES 2.0 use this vertex and fragment shader pair. When you are done with this shader you have to call
	 * {@link ShaderProgram#end()}. */
	public void begin () {
		checkContext();
		GL20.glUseProgram(program);
	}

	/** Disables this shader. Must be called when one is done with the shader. Don't mix it with dispose, that will release the
	 * shader resources. */
	public void end () {
		GL20.glUseProgram(0);
	}

	/** Disposes all resources associated with this shader. Must be called when the shader is no longer used. */
	public void dispose () {
		GL20.glUseProgram(0);
		GL20.glDeleteShader(vertexShaderHandle);
		GL20.glDeleteShader(fragmentShaderHandle);
		GL20.glDeleteProgram(program);
//		if (shaders.get(Gdx.app) != null) shaders.get(Gdx.app).remove(this);
	}

	/** Disables the vertex attribute with the given name
	 * 
	 * @param name the vertex attribute name */
	public void disableVertexAttribute (String name) {
		checkContext();
		int location = fetchAttributeLocation(name);
		if (location == -1) return;
		GL20.glDisableVertexAttribArray(location);
	}

	/** Enables the vertex attribute with the given name
	 * 
	 * @param name the vertex attribute name */
	public void enableVertexAttribute (String name) {
		checkContext();
		int location = fetchAttributeLocation(name);
		if (location == -1) return;
		GL20.glEnableVertexAttribArray(location);
	}

	private void checkContext () {
            GLContext ctxt = GLContext.getCurrent();
            if (ctxt != context) {
                compileShaders(vertexShaderSource, fragmentShaderSource);
                context = ctxt;
            }
	}

//	private void addManagedShader (Application app, ShaderProgram shaderProgram) {
//		List<ShaderProgram> managedResources = shaders.get(app);
//		if (managedResources == null) managedResources = new ArrayList<ShaderProgram>();
//		managedResources.add(shaderProgram);
//		shaders.put(app, managedResources);
//	}

//	/** Invalidates all shaders so the next time they are used new handles are generated
//	 * @param app */
//	public static void invalidateAllShaderPrograms (Application app) {
//		if (Gdx.graphics.getGL20() == null) return;
//
//		List<ShaderProgram> shaderList = shaders.get(app);
//		if (shaderList == null) return;
//
//		for (int i = 0; i < shaderList.size(); i++) {
//			shaderList.get(i).invalidated = true;
//			shaderList.get(i).checkManaged();
//		}
//	}

//	public static void clearAllShaderPrograms (Application app) {
//		shaders.remove(app);
//	}

//	public static String getManagedStatus () {
//		StringBuilder builder = new StringBuilder();
//		int i = 0;
//		builder.append("Managed shaders/app: { ");
//		for (Application app : shaders.keySet()) {
//			builder.append(shaders.get(app).size());
//			builder.append(" ");
//		}
//		builder.append("}");
//		return builder.toString();
//	}

	/** Sets the given attribute
	 * 
	 * @param name the name of the attribute
	 * @param value1 the first value
	 * @param value2 the second value
	 * @param value3 the third value
	 * @param value4 the fourth value */
	public void setAttributef (String name, float value1, float value2, float value3, float value4) {
		int location = fetchAttributeLocation(name);
		GL20.glVertexAttrib4f(location, value1, value2, value3, value4);
	}

	private void ensureBufferCapacity (int numBytes) {
		if (buffer == null || buffer.capacity() != numBytes) {
//			buffer = BufferUtils.newByteBuffer(numBytes);
                        buffer = BufferUtils.createByteBuffer(numBytes);
			floatBuffer = buffer.asFloatBuffer();
			intBuffer = buffer.asIntBuffer();
		}
	}

	IntBuffer params = BufferUtils.createIntBuffer(1);
	IntBuffer type = BufferUtils.createIntBuffer(1);

	private void fetchUniforms () {
		params.clear();
		GL20.glGetProgram(program, GL20.GL_ACTIVE_UNIFORMS, params);
		int numUniforms = params.get(0);

		uniformNames = new String[numUniforms];

		for (int i = 0; i < numUniforms; i++) {
			params.clear();
			params.put(0, 256);
			type.clear();
			String name = glGetActiveUniform(program, i, params, type);
			int location = GL20.glGetUniformLocation(program, name);
			uniforms.put(name, location);
			uniformTypes.put(name, type.get(0));
			uniformNames[i] = name;
		}
	}
        
        private String glGetActiveUniform (int program, int index, IntBuffer size, Buffer type) {
		// FIXME this is less than ideal of course...
		IntBuffer typeTmp = BufferUtils.createIntBuffer(2);
		String name = GL20.glGetActiveUniform(program, index, 256, typeTmp);
		if (type instanceof IntBuffer) ((IntBuffer)type).put(typeTmp.get(0));
		return name;
	}

	private void fetchAttributes () {
		params.clear();
		GL20.glGetProgram(program, GL20.GL_ACTIVE_ATTRIBUTES, params);
		int numAttributes = params.get(0);

		attributeNames = new String[numAttributes];

		for (int i = 0; i < numAttributes; i++) {
			params.clear();
			params.put(0, 256);
			type.clear();
			String name = glGetActiveAttrib(program, i, params, type);
			int location = GL20.glGetAttribLocation(program, name);
			attributes.put(name, location);
			attributeTypes.put(name, type.get(0));
			attributeNames[i] = name;
		}
	}

        private String glGetActiveAttrib (int program, int index, IntBuffer size, Buffer type) {
		// FIXME this is less than ideal of course...
		IntBuffer typeTmp = BufferUtils.createIntBuffer(2);
		String name = GL20.glGetActiveAttrib(program, index, 256, typeTmp);
		if (type instanceof IntBuffer) ((IntBuffer)type).put(typeTmp.get(0));
		return name;
	}
        
	/** @param name the name of the attribute
	 * @return whether the attribute is available in the shader */
	public boolean hasAttribute (String name) {
		return attributes.containsKey(name);
	}

	/** @param name the name of the attribute
	 * @return the type of the attribute, one of {@link GL20#GL_FLOAT}, {@link GL20#GL_FLOAT_VEC2} etc. */
	public int getAttributeType (String name) {
		Integer type = attributes.get(name);
		if (type == null)
			return 0;
		else
			return type;
	}

	/** @param name the name of the attribute
	 * @return the location of the attribute or -1. */
	public int getAttributeLocation (String name) {
		Integer location = attributes.get(name);
		if (location == null)
			return -1;
		else
			return location;
	}

	/** @param name the name of the uniform
	 * @return whether the uniform is available in the shader */
	public boolean hasUniform (String name) {
		return uniforms.containsKey(name);
	}

	/** @param name the name of the uniform
	 * @return the type of the uniform, one of {@link GL20#GL_FLOAT}, {@link GL20#GL_FLOAT_VEC2} etc. */
	public int getUniformType (String name) {
		Integer type = uniformTypes.get(name);
		if (type == null)
			return 0;
		else
			return type;
	}

	/** @param name the name of the uniform
	 * @return the location of the uniform or -1. */
	public int getUniformLocation (String name) {
		Integer location = uniforms.get(name);
		if (location == null)
			return -1;
		else
			return location;
	}

	/** @return the attributes */
	public String[] getAttributes () {
		return attributeNames;
	}

	/** @return the uniforms */
	public String[] getUniforms () {
		return uniformNames;
	}
}
