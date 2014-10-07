package com.datalink.basicopengles;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import android.opengl.GLES20;
import android.opengl.Matrix;
import android.opengl.GLSurfaceView.Renderer;
import android.os.SystemClock;

public class BasicRenderer implements Renderer{

    private final FloatBuffer mPolygonVertices;
    private final int mBytesPerFloat = 4;
    
    private float[] mViewMatrix = new float[16];
    private float[] mProjectionMatrix = new float[16];
    private float[] mModelMatrix = new float[16];
    private float[] mMVPMatrix = new float[16];

    private int mMVPMatrixHandle;
    private int mPositionHandle;
    private int mColorHandle;

    private final int mPositionOffset = 0;
    private final int mPositionDataSize = 3;

    private final int mColorOffset = 3;
    private final int mColorDataSize = 4;
    
    private final int mStrideBytes = (mPositionDataSize + mColorDataSize) * mBytesPerFloat;
    
    // lesson 2
    
	private final FloatBuffer mCubePositions;
	private final FloatBuffer mCubeColors;
	private final FloatBuffer mCubeNormals;
	
	private final float[] mLightPosInModelSpace = {0.0f, 0.0f, 0.0f, 1.0f};
	/** Used to hold the current position of the light in world space (after transformation via model matrix). */
	private final float[] mLightPosInWorldSpace = new float[4];
	/** Used to hold the transformed position of the light in eye space (after transformation via modelview matrix) */
	private final float[] mLightPosInEyeSpace = new float[4];
	
	private int mProgramHandle;
	private int mPointProgramHandle;
    
	float[] crossProduct(float[] fst, float[] sec, boolean normalize)
	{
	    //TODO: proper signs
        float new0 = -fst[1]*sec[2] + fst[2]*sec[1];
        float new1 = -fst[2]*sec[0] + fst[0]*sec[2];
        float new2 = fst[1]*sec[0] - fst[0]*sec[1];
        if( normalize )
        {
            float length = Matrix.length(new0, new1, new2);
            float[] result = { new0/length, new1/length, new2/length};
            return result;
        }
        
        float[] result = { new0, new1, new2 };
        return result;
    }
	
	float[] normals(float[] vertices, int strideSize)
	{
	    int verticesInGroup = 3;
	    
	    int len = vertices.length / (strideSize*verticesInGroup);
	    int size = len*verticesInGroup*mPositionDataSize;
	    float[] result = new float[size];
	    float[] fst = new float[mPositionDataSize];
	    float[] sec = new float[mPositionDataSize];
	    for (int i = 0; i < size; i+=strideSize*verticesInGroup)
	    {
	        for (int j = 0; j < mPositionDataSize; j++)
	        {
	            //TODO: proper signs
	            fst[j] = vertices[i+j] - vertices[i+strideSize+j];
	            sec[j] = vertices[i+strideSize*2+j] - vertices[i+strideSize+j];
	        }
	        
	        float[] cross = crossProduct(fst, sec, true);
	        for (int k = 0; k < verticesInGroup; k++)
	        {
	            System.arraycopy(cross, 0, result, i+k*strideSize,  mPositionDataSize);
	        }
	    }
	    return result;
	}

	FloatBuffer bufferBoilerplate(final float[] values)
	{
	    FloatBuffer result = ByteBuffer.allocateDirect(values.length * mBytesPerFloat)
                .order(ByteOrder.nativeOrder()).asFloatBuffer();
        result.put(values).position(0);
        return result;
	}
	
    public BasicRenderer()
    {
        // This triangle is red, green, and blue.
        final float[] polygonVertices = 
        {
            // X, Y, Z,
            // R, G, B, A
            -0.5f, -0.25f, 0.0f,
            1.0f, 0.0f, 0.0f, 1.0f,

            0.0f, 0.0f, 0.0f,
            0.0f, 1.0f, 0.0f, 1.0f,

            0.0f, 0.559016994f, 0.0f,
            0.0f, 1.0f, 0.0f, 1.0f,

            0.5f, -0.25f, 0.0f,
            0.0f, 0.0f, 1.0f, 1.0f,

            0.0f, 0.559016994f, 0.0f,
            0.0f, 1.0f, 0.0f, 1.0f,

            0.0f, 0.0f, 0.0f,
            0.0f, 1.0f, 0.0f, 1.0f
        };
        
        mPolygonVertices = bufferBoilerplate(polygonVertices);
		mCubePositions = bufferBoilerplate(CubeData.positionArray);	
		mCubeColors = bufferBoilerplate(CubeData.collorArray);
		mCubeNormals = bufferBoilerplate( normals(CubeData.positionArray, mPositionDataSize) );
        
    }
 
    @Override
    public void onSurfaceCreated(GL10 glUnused, EGLConfig config)
    {
        // Set the background clear color to gray.
        GLES20.glClearColor(0.5f, 0.5f, 0.5f, 0.5f);
        
        // Use culling to remove back faces.
        GLES20.glEnable(GLES20.GL_CULL_FACE);
         
        // Enable depth testing
        GLES20.glEnable(GLES20.GL_DEPTH_TEST);

        // Position the eye behind the origin.
        final float eyeX = 0.0f;
        final float eyeY = 0.0f;
        final float eyeZ = 1.5f;

        // We are looking toward the distance
        final float lookX = 0.0f;
        final float lookY = 0.0f;
        final float lookZ = -5.0f;

        // Set our up vector. This is where our head would be pointing were we holding the camera.
        final float upX = 0.0f;
        final float upY = 1.0f;
        final float upZ = 0.0f;

        // Set the view matrix. This matrix can be said to represent the camera position.
        // NOTE: In OpenGL 1, a ModelView matrix is used, which is a combination of a model and
        // view matrix. In OpenGL 2, we can keep track of these matrices separately if we choose.
        Matrix.setLookAtM(mViewMatrix, 0, eyeX, eyeY, eyeZ, lookX, lookY, lookZ, upX, upY, upZ);

        final int vertexShaderHandle = shaderBoilerplate(GLES20.GL_VERTEX_SHADER, vertexShader);
        final int fragmentShaderHandle = shaderBoilerplate(GLES20.GL_FRAGMENT_SHADER, fragmentShader);

        mProgramHandle = programBoilerplate(vertexShaderHandle, fragmentShaderHandle, new String[] { "a_Position", "a_Color"});
        
        final int pointVertexShaderHandle = shaderBoilerplate(GLES20.GL_VERTEX_SHADER, pointVertexShader);
        final int pointFragmentShaderHandle = shaderBoilerplate(GLES20.GL_FRAGMENT_SHADER, pointFragmentShader);
        
        mPointProgramHandle = programBoilerplate(pointVertexShaderHandle, pointFragmentShaderHandle, new String[] { "a_Position" });
    }

    @Override
    public void onSurfaceChanged(GL10 glUnused, int width, int height)
    {
        // Set the OpenGL viewport to the same size as the surface.
        GLES20.glViewport(0, 0, width, height);

        // Create a new perspective projection matrix. The height will stay the same
        // while the width will vary as per aspect ratio.
        final float ratio = (float) width / height;
        final float left = -ratio;
        final float right = ratio;
        final float bottom = -1.0f;
        final float top = 1.0f;
        final float near = 1.0f;
        final float far = 10.0f;

        Matrix.frustumM(mProjectionMatrix, 0, left, right, bottom, top, near, far);
    }

    @Override
    public void onDrawFrame(GL10 glUnused)
    {
        GLES20.glClear(GLES20.GL_DEPTH_BUFFER_BIT | GLES20.GL_COLOR_BUFFER_BIT);
        
        mMVPMatrixHandle = GLES20.glGetUniformLocation(mProgramHandle, "u_MVPMatrix");
        mPositionHandle = GLES20.glGetAttribLocation(mProgramHandle, "a_Position");
        mColorHandle = GLES20.glGetAttribLocation(mProgramHandle, "a_Color");

        // Do a complete rotation every 10 seconds.
        long time = SystemClock.uptimeMillis() % 10000L;
        float angleInDegrees = (360.0f / 10000.0f) * ((int) time);

        // Draw the triangle facing straight on.
        Matrix.setIdentityM(mModelMatrix, 0);
        Matrix.rotateM(mModelMatrix, 0, angleInDegrees, 0.0f, 0.0f, 1.0f);
        drawVertices(mPolygonVertices);
        
        // Draw some cubes.        
        Matrix.setIdentityM(mModelMatrix, 0);
        Matrix.translateM(mModelMatrix, 0, 4.0f, 0.0f, -7.0f);
        Matrix.rotateM(mModelMatrix, 0, angleInDegrees, 1.0f, 0.0f, 0.0f);        
        drawVertices(mCubePositions, mCubeColors);
                        
        Matrix.setIdentityM(mModelMatrix, 0);
        Matrix.translateM(mModelMatrix, 0, -4.0f, 0.0f, -7.0f);
        Matrix.rotateM(mModelMatrix, 0, angleInDegrees, 0.0f, 1.0f, 0.0f);        
        drawVertices(mCubePositions, mCubeColors);
        
        Matrix.setIdentityM(mModelMatrix, 0);
        Matrix.translateM(mModelMatrix, 0, 0.0f, 4.0f, -7.0f);
        Matrix.rotateM(mModelMatrix, 0, angleInDegrees, 0.0f, 0.0f, 1.0f);        
        drawVertices(mCubePositions, mCubeColors);
        
        Matrix.setIdentityM(mModelMatrix, 0);
        Matrix.translateM(mModelMatrix, 0, 0.0f, -4.0f, -7.0f);
        drawVertices(mCubePositions, mCubeColors);
        
        Matrix.setIdentityM(mModelMatrix, 0);
        Matrix.translateM(mModelMatrix, 0, 0.0f, 0.0f, -5.0f);
        Matrix.rotateM(mModelMatrix, 0, angleInDegrees, 1.0f, 1.0f, 0.0f);        
        drawVertices(mCubePositions, mCubeColors);
        
        Matrix.setIdentityM(mModelMatrix, 0);
        Matrix.translateM(mModelMatrix, 0, 0.0f, 0.0f, -5.0f);      
        Matrix.rotateM(mModelMatrix, 0, angleInDegrees, 0.0f, 1.0f, 0.0f);
        Matrix.translateM(mModelMatrix, 0, 0.0f, 0.0f, 2.0f);
        drawLight();
    }

    private void drawVertices(final FloatBuffer coloredPositionsBuffer)
    {
        // Tell OpenGL to use this program when rendering.
        GLES20.glUseProgram(mProgramHandle);
        
        // Pass in the position information
        coloredPositionsBuffer.position(mPositionOffset);
        GLES20.glVertexAttribPointer(mPositionHandle, mPositionDataSize, GLES20.GL_FLOAT, false,
                mStrideBytes, coloredPositionsBuffer);
    
        GLES20.glEnableVertexAttribArray(mPositionHandle);
    
        // Pass in the color information
        coloredPositionsBuffer.position(mColorOffset);
        GLES20.glVertexAttribPointer(mColorHandle, mColorDataSize, GLES20.GL_FLOAT, false,
                mStrideBytes, coloredPositionsBuffer);
    
        GLES20.glEnableVertexAttribArray(mColorHandle);
    
        // This multiplies the view matrix by the model matrix, and stores the result in the MVP matrix
        // (which currently contains model * view).
        Matrix.multiplyMM(mMVPMatrix, 0, mViewMatrix, 0, mModelMatrix, 0);
    
        // This multiplies the modelview matrix by the projection matrix, and stores the result in the MVP matrix
        // (which now contains model * view * projection).
        Matrix.multiplyMM(mMVPMatrix, 0, mProjectionMatrix, 0, mMVPMatrix, 0);
    
        GLES20.glUniformMatrix4fv(mMVPMatrixHandle, 1, false, mMVPMatrix, 0);
        
        GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, coloredPositionsBuffer.capacity() / (mPositionDataSize + mColorDataSize));
    }
    	
	private void drawVertices(final FloatBuffer positionsBuffer, final FloatBuffer colorsBuffer)
	{	
	    // Tell OpenGL to use this program when rendering.
        GLES20.glUseProgram(mProgramHandle);
        
		// Pass in the position information
		positionsBuffer.position(0);		
        GLES20.glVertexAttribPointer(mPositionHandle, mPositionDataSize, GLES20.GL_FLOAT, false,
        		0, positionsBuffer);        
                
        GLES20.glEnableVertexAttribArray(mPositionHandle);        
        
        // Pass in the color information
        colorsBuffer.position(0);
        GLES20.glVertexAttribPointer(mColorHandle, mColorDataSize, GLES20.GL_FLOAT, false,
        		0, colorsBuffer);        
        
        GLES20.glEnableVertexAttribArray(mColorHandle);
        
        // This multiplies the view matrix by the model matrix, and stores the result in the MVP matrix
        // (which currently contains model * view).
        Matrix.multiplyMM(mMVPMatrix, 0, mViewMatrix, 0, mModelMatrix, 0);
    
        // This multiplies the modelview matrix by the projection matrix, and stores the result in the MVP matrix
        // (which now contains model * view * projection).
        Matrix.multiplyMM(mMVPMatrix, 0, mProjectionMatrix, 0, mMVPMatrix, 0);
    
        GLES20.glUniformMatrix4fv(mMVPMatrixHandle, 1, false, mMVPMatrix, 0);
        
        // Draw the cube.
        GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, positionsBuffer.capacity() / mPositionDataSize);                               
	}
	
	
	private void drawLight()
	{
	    GLES20.glUseProgram(mPointProgramHandle);
	
		final int pointMVPMatrixHandle = GLES20.glGetUniformLocation(mPointProgramHandle, "u_MVPMatrix");
        final int pointPositionHandle = GLES20.glGetAttribLocation(mPointProgramHandle, "a_Position");
        
		// Pass in the position.
		GLES20.glVertexAttrib3f(pointPositionHandle, mLightPosInModelSpace[0], mLightPosInModelSpace[1], mLightPosInModelSpace[2]);

		// Since we are not using a buffer object, disable vertex arrays for this attribute.
        GLES20.glDisableVertexAttribArray(pointPositionHandle);  
		
		// Pass in the transformation matrix.
		Matrix.multiplyMM(mMVPMatrix, 0, mViewMatrix, 0, mModelMatrix, 0);
		Matrix.multiplyMM(mMVPMatrix, 0, mProjectionMatrix, 0, mMVPMatrix, 0);
		GLES20.glUniformMatrix4fv(pointMVPMatrixHandle, 1, false, mMVPMatrix, 0);
		
		// Draw the point.
		GLES20.glDrawArrays(GLES20.GL_POINTS, 0, 1);
	}

    final String vertexShader =
            "uniform mat4 u_MVPMatrix;      \n"     // A constant representing the combined model/view/projection matrix.

            + "attribute vec4 a_Position;     \n"     // Per-vertex position information we will pass in.
            + "attribute vec4 a_Color;        \n"     // Per-vertex color information we will pass in.

            + "varying vec4 v_Color;          \n"     // This will be passed into the fragment shader.

            + "void main()                    \n"     // The entry point for our vertex shader.
            + "{                              \n"
            + "   v_Color = a_Color;          \n"     // Pass the color through to the fragment shader.
            // It will be interpolated across the triangle.
            + "   gl_Position = u_MVPMatrix   \n"     // gl_Position is a special variable used to store the final position.
            + "               * a_Position;   \n"     // Multiply the vertex by the matrix to get the final point in
            + "}                              \n";    // normalized screen coordinates.

    final String fragmentShader =
            "precision mediump float;       \n"     // Set the default precision to medium. We don't need as high of a
            // precision in the fragment shader.
            + "varying vec4 v_Color;          \n"     // This is the color from the vertex shader interpolated across the
            // triangle per fragment.
            + "void main()                    \n"     // The entry point for our fragment shader.
            + "{                              \n"
            + "   gl_FragColor = v_Color;     \n"     // Pass the color directly through the pipeline.
            + "}                              \n";
    
        // Define a simple shader program for our point.
    final String pointVertexShader =
    	"uniform mat4 u_MVPMatrix;      \n"		
      +	"attribute vec4 a_Position;     \n"		
      + "void main()                    \n"
      + "{                              \n"
      + "   gl_Position = u_MVPMatrix   \n"
      + "               * a_Position;   \n"
      + "   gl_PointSize = 5.0;         \n"
      + "}                              \n";
    
    final String pointFragmentShader = 
    	"precision mediump float;       \n"					          
      + "void main()                    \n"
      + "{                              \n"
      + "   gl_FragColor = vec4(1.0,    \n" 
      + "   1.0, 1.0, 1.0);             \n"
      + "}                              \n";
        
    int shaderBoilerplate(int shaderType, String shaderProgram)
    {
        int shaderHandle = GLES20.glCreateShader(shaderType);
        String why = "Error creating shader. ";
        
        if (shaderHandle != 0)
        {
            GLES20.glShaderSource(shaderHandle, shaderProgram);
            GLES20.glCompileShader(shaderHandle);

            // Get the compilation status.
            // If the compilation failed, delete the shader.
            final int[] compileStatus = new int[1];
            GLES20.glGetShaderiv(shaderHandle, GLES20.GL_COMPILE_STATUS, compileStatus, 0);
            if (compileStatus[0] == 0)
            {
                why += GLES20.glGetShaderInfoLog(shaderHandle);
                GLES20.glDeleteShader(shaderHandle);
                throw new RuntimeException(why);
            }
        }
        else
        {
            throw new RuntimeException(why);
        }
        
        return shaderHandle;
    }
    
    int programBoilerplate(final int vertexShaderHandle, final int fragmentShaderHandle, final String[] attributes)
    {
        int programHandle = GLES20.glCreateProgram();
        String why = "Error creating shader program. ";
         
        if (programHandle != 0)
        {
            GLES20.glAttachShader(programHandle, vertexShaderHandle);
            GLES20.glAttachShader(programHandle, fragmentShaderHandle);
            
			if (attributes != null)
			{
				for (int i = 0; i < attributes.length; i++)
				{
					GLES20.glBindAttribLocation(programHandle, i, attributes[i]);
				}						
			}
            
            GLES20.glBindAttribLocation(programHandle, 0, "a_Position");
            GLES20.glBindAttribLocation(programHandle, 1, "a_Color");
            
            GLES20.glLinkProgram(programHandle);
         
            // Get the link status.
            // If the link failed, delete the program.
            final int[] linkStatus = new int[1];
            GLES20.glGetProgramiv(programHandle, GLES20.GL_LINK_STATUS, linkStatus, 0);
            if (linkStatus[0] == 0)
            {
                why += GLES20.glGetProgramInfoLog(programHandle);
                GLES20.glDeleteProgram(programHandle);
                throw new RuntimeException(why);          
            }
        }
        else
        {
            throw new RuntimeException(why);
        }
        
        return programHandle;
    }
}
