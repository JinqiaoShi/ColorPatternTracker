package com.samsung.dtl.colorpatterntracker.camera;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;

import android.content.Context;
import android.graphics.Point;
import android.graphics.SurfaceTexture;
import android.opengl.GLES11Ext;
import android.opengl.GLES20;
import android.os.Environment;
import android.util.Log;

// TODO: Auto-generated Javadoc
/**
 * The Class ShaderManager.
 */
public class ShaderManager {
	
	public int[] hTex; /*!< The texture to store camera image. */
	public int[] glTextures; /*!< The input and output opengl textures. */
	public IntBuffer targetFramebuffer; /*!< The target framebuffer. */

	private FloatBuffer vertexCoord; /*!< The vertex coordinates. */
	private FloatBuffer cameraTexCoord; /*!< The camera texture coordinates. */
	private FloatBuffer openclTexCoord; /*!< The opencl texture coordinates. */
	
	private int hProgram; /*!< The shader program to get camera texture. */
	private int displayTextureProgram; /*!< The shader program to display texture. */
	
	/** The vertex shader source. */
	private final String vss =
			"attribute vec2 vPosition;\n" +
			"attribute vec2 vTexCoord;\n" +
			"varying vec2 texCoord;\n" +
			"void main() {\n" +
			"  texCoord = vTexCoord;\n" +
			"  gl_Position = vec4 ( vPosition.x, vPosition.y, 0.0, 1.0 );\n" +
			"}";

	/** The camera texture shader source. */
	private final String camera_fss =
			"#extension GL_OES_EGL_image_external : require\n" +
			"precision mediump float;\n" +
			"uniform samplerExternalOES sTexture;\n" +
			"varying vec2 texCoord;\n" +
			"void main() {\n" +
			"  gl_FragColor = texture2D(sTexture, texCoord);\n" +
			"}";

	/** The texture shader source. */ 
	private final String texture_fss =
			"precision mediump float;\n" +
			"uniform sampler2D sTexture;\n" +
			"varying vec2 texCoord;\n" +
			"void main() {\n" +
			"  gl_FragColor = texture2D(sTexture, texCoord);\n" +
			"}";

	public Context context;
	
	/**
	 * Initialize texture coordinates.
	 */
	public void initializeCoords(){
		float[] vertexCoordTmp = {
				-1.0f, -1.0f, 0.0f,
				-1.0f,  1.0f, 0.0f,
				 1.0f, -1.0f, 0.0f,
				 1.0f,  1.0f, 0.0f};
			float[] textureCoordTmp = {
				 0.0f, 1.0f,
				 0.0f, 0.0f,
				 1.0f, 1.0f,
				 1.0f, 0.0f };
			float[] openclCoordTmp = {
				 0.0f, 0.0f,
				 0.0f, 1.0f,
				 1.0f, 0.0f,
				 1.0f, 1.0f
			};

			vertexCoord = ByteBuffer.allocateDirect(12*4).order(ByteOrder.nativeOrder()).asFloatBuffer();
		vertexCoord.put(vertexCoordTmp);
		vertexCoord.position(0);
		cameraTexCoord = ByteBuffer.allocateDirect(8*4).order(ByteOrder.nativeOrder()).asFloatBuffer();
		cameraTexCoord.put(textureCoordTmp);
		cameraTexCoord.position(0);
		openclTexCoord = ByteBuffer.allocateDirect(8*4).order(ByteOrder.nativeOrder()).asFloatBuffer();
		openclTexCoord.put(openclCoordTmp);
		openclTexCoord.position(0);
	}
	
	/**
	 * Get texture from Camera.
	 *
	 * @param mSTexture the camera surface texture
	 * @param camera_res the camera resolution
	 * @return the capture time
	 */
	public long cameraToTexture(SurfaceTexture mSTexture, Point camera_res) {
		long captureTime=0;
		int error0 = GLES20.glGetError();
		GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, targetFramebuffer.get(0));
		int error1 = GLES20.glGetError();
		int fbret = GLES20.glCheckFramebufferStatus(GLES20.GL_FRAMEBUFFER);
		int error2 = GLES20.glGetError();
		if (fbret != GLES20.GL_FRAMEBUFFER_COMPLETE) {
		  Log.d("", "unable to bind fbo" + fbret);
		}
		GLES20.glViewport(0, 0, camera_res.x, camera_res.y);
		int error3 = GLES20.glGetError();
		synchronized(this) {
			mSTexture.updateTexImage();
			captureTime=mSTexture.getTimestamp();
		}

		GLES20.glUseProgram(hProgram);
		int error4 = GLES20.glGetError();

		int ph = GLES20.glGetAttribLocation(hProgram, "vPosition");
		int tch = GLES20.glGetAttribLocation (hProgram, "vTexCoord");
		int th = GLES20.glGetUniformLocation (hProgram, "sTexture");

		GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
		int error5 = GLES20.glGetError();
		GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, hTex[0]);
		int error6 = GLES20.glGetError();
		GLES20.glUniform1i(th, 0);
		int error7 = GLES20.glGetError();

		GLES20.glVertexAttribPointer(ph, 2, GLES20.GL_FLOAT, false, 4*3, vertexCoord);
		int error8 = GLES20.glGetError();
		GLES20.glVertexAttribPointer(tch, 2, GLES20.GL_FLOAT, false, 4*2, cameraTexCoord);
		int error9 = GLES20.glGetError();
		GLES20.glEnableVertexAttribArray(ph);
		int error10 = GLES20.glGetError();
		GLES20.glEnableVertexAttribArray(tch);
		int error11 = GLES20.glGetError();

		GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4);
		int error12 = GLES20.glGetError();
		GLES20.glFinish();


		/*
		{
			ByteBuffer byteBuffer = ByteBuffer.allocate(1920 * 1080 * 4);
			byteBuffer.order(ByteOrder.nativeOrder());

			GLES20.glReadPixels(0, 0, 1920, 1080, GLES20.GL_RGBA, GLES20.GL_UNSIGNED_BYTE, byteBuffer);
			int error14 = GLES20.glGetError();
			byte[] array = byteBuffer.array();

			java.io.FileOutputStream outputStream = null;
			try {
				String diskstate = Environment.getExternalStorageState();
				if(diskstate.equals("mounted")){
					java.io.File picFolder = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
					java.io.File picFile = new java.io.File(picFolder,"imgc.bin");
					outputStream = new java.io.FileOutputStream(picFile);
				}
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}

			try {
				outputStream.write(array);
			} catch (IOException e) {
				e.printStackTrace();
			}

			try {
				outputStream.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		*/
		return captureTime;
	}
	
	/**
	 * Renders screen from texture.
	 *
	 * @param displayTexture the display texture
	 * @param display_dim the display dimension
	 */
	public void renderFromTexture(int displayTexture, Point display_dim) {
		GLES20.glViewport(0, 0, display_dim.x, display_dim.y);

		GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, 0);
		GLES20.glUseProgram(displayTextureProgram);

		int ph = GLES20.glGetAttribLocation(displayTextureProgram, "vPosition");
		int tch = GLES20.glGetAttribLocation(displayTextureProgram, "vTexCoord");
		int th = GLES20.glGetUniformLocation(displayTextureProgram, "sTexture");

		GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
		GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, displayTexture);
		GLES20.glUniform1i(th, 0);

		GLES20.glVertexAttribPointer(ph, 2, GLES20.GL_FLOAT, false, 4*3, vertexCoord);
		GLES20.glVertexAttribPointer(tch, 2, GLES20.GL_FLOAT, true, 4*2, openclTexCoord);
		GLES20.glEnableVertexAttribArray(ph);
		GLES20.glEnableVertexAttribArray(tch);
		GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4);
		GLES20.glFinish();
	}

	/**
	 * Initializes the textures.
	 *
	 * @param camera_res the camera resolution
	 * @return the surface texture
	 */
	public SurfaceTexture initTex(Point camera_res) {
		hTex = new int[1];
		glTextures = new int[2];
		int error = GLES20.glGetError();
		GLES20.glGenTextures ( 1, hTex, 0 );error = GLES20.glGetError();
		GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, hTex[0]);error = GLES20.glGetError();
		GLES20.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE);error = GLES20.glGetError();
		GLES20.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE);error = GLES20.glGetError();
		//GLES20.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR_MIPMAP_LINEAR);error = GLES20.glGetError();
		//GLES20.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR);error = GLES20.glGetError();
		//GLES20.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);error = GLES20.glGetError();

		GLES20.glGenTextures ( 2, glTextures, 0 );error = GLES20.glGetError();
		GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, glTextures[0]);error = GLES20.glGetError();
		GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D,GLES20.GL_TEXTURE_MIN_FILTER,GLES20.GL_LINEAR_MIPMAP_NEAREST);error = GLES20.glGetError();
		GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D,GLES20.GL_TEXTURE_MAG_FILTER,GLES20.GL_LINEAR);error = GLES20.glGetError();
		GLES20.glTexImage2D(GLES20.GL_TEXTURE_2D, 0, GLES20.GL_RGBA, camera_res.x, camera_res.y, 0, GLES20.GL_RGBA, GLES20.GL_UNSIGNED_BYTE, null);error = GLES20.glGetError();
		GLES20.glGenerateMipmap(GLES20.GL_TEXTURE_2D);error = GLES20.glGetError();
		GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0);error = GLES20.glGetError();

		GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, glTextures[1]);error = GLES20.glGetError();
		GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D,GLES20.GL_TEXTURE_MIN_FILTER,GLES20.GL_LINEAR);error = GLES20.glGetError();
		GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D,GLES20.GL_TEXTURE_MAG_FILTER,GLES20.GL_LINEAR);error = GLES20.glGetError();
		GLES20.glTexImage2D(GLES20.GL_TEXTURE_2D, 0, GLES20.GL_RGBA, camera_res.x, camera_res.y, 0, GLES20.GL_RGBA, GLES20.GL_UNSIGNED_BYTE, null);error = GLES20.glGetError();
		GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0);error = GLES20.glGetError();
		
		targetFramebuffer = IntBuffer.allocate(1);error = GLES20.glGetError();
		GLES20.glGenFramebuffers(1, targetFramebuffer);error = GLES20.glGetError();
		GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, targetFramebuffer.get(0));error = GLES20.glGetError();
		GLES20.glFramebufferTexture2D(GLES20.GL_FRAMEBUFFER, GLES20.GL_COLOR_ATTACHMENT0, GLES20.GL_TEXTURE_2D, glTextures[0], 0);error = GLES20.glGetError();
		GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, 0);error = GLES20.glGetError();
		
		GLES20.glClearColor (1.0f, 1.0f, 0.0f, 1.0f);error = GLES20.glGetError();
		hProgram = loadShader(vss, camera_fss);error = GLES20.glGetError();
		displayTextureProgram = loadShader(vss, texture_fss);error = GLES20.glGetError();
		
		SurfaceTexture mSTexture = new SurfaceTexture (hTex[0]);error = GLES20.glGetError();
		return mSTexture;
	}

	/**
	 * Loads shaders.
	 *
	 * @param vertex_shader the vertex shader
	 * @param fragment_shader the fragment shader
	 * @return the program ID
	 */
	private static int loadShader ( String vertex_shader, String fragment_shader ) {
		int vshader = GLES20.glCreateShader(GLES20.GL_VERTEX_SHADER);
		GLES20.glShaderSource(vshader, vertex_shader);
		GLES20.glCompileShader(vshader);
		int[] compiled = new int[1];
		GLES20.glGetShaderiv(vshader, GLES20.GL_COMPILE_STATUS, compiled, 0);
		if (compiled[0] == 0) {
			Log.e("Shader", "Could not compile vshader");
			Log.v("Shader", "Could not compile vshader:"+GLES20.glGetShaderInfoLog(vshader));
			GLES20.glDeleteShader(vshader);
			vshader = 0;
		}

		int fshader = GLES20.glCreateShader(GLES20.GL_FRAGMENT_SHADER);
		GLES20.glShaderSource(fshader, fragment_shader);
		GLES20.glCompileShader(fshader);
		GLES20.glGetShaderiv(fshader, GLES20.GL_COMPILE_STATUS, compiled, 0);
		if (compiled[0] == 0) {
			Log.e("Shader", "Could not compile fshader");
			Log.v("Shader", "Could not compile fshader:"+GLES20.glGetShaderInfoLog(fshader));
			GLES20.glDeleteShader(fshader);
			fshader = 0;
		}

		int program = GLES20.glCreateProgram();
		GLES20.glAttachShader(program, vshader);
		GLES20.glAttachShader(program, fshader);
		GLES20.glLinkProgram(program);
			 
		return program;
	}

	/**
	 * Deletes textures.
	 */
	public void deleteTex() {
		GLES20.glDeleteTextures (1, hTex, 0);
	}
}
