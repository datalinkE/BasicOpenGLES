package com.datalink.basicopengles;

import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.opengl.GLES20;
import android.opengl.GLUtils;
import android.opengl.Matrix;
import android.opengl.GLSurfaceView.Renderer;
import android.os.SystemClock;

public class BasicRenderer implements Renderer{

    private final Context mActivityContext;
    
    private final int mBytesPerFloat = 4;

    private float[] mViewMatrix = new float[16];
    private float[] mProjectionMatrix = new float[16];
    private float[] mModelMatrix = new float[16];
    private float[] mMVPMatrix = new float[16];
    private float[] mLightModelMatrix = new float[16];

    private int mMVPMatrixHandle;
    private int mMVMatrixHandle;
    private int mLightPositionHandle;
    private int mPositionHandle;
    private int mColorHandle;
    private int mNormalHandle;

    /** Store our model data in a float buffer. */
    private final FloatBuffer mCubeTextureCoordinates;
    /** This will be used to pass in the texture. */
    private int mTextureUniformHandle; 
    /** This will be used to pass in model texture coordinate information. */
    private int mTextureCoordinateHandle; 
    /** Size of the texture coordinate data in elements. */
    private final int mTextureCoordinateDataSize = 2; 
    /** This is a handle to our texture data. */
    private int mTextureDataHandle;

    private final int mPositionDataSize = 3;
    private final int mColorDataSize = 4;
    private final int mNormalDataSize = 3;

    private final FloatBuffer mCubePositions;
    private final FloatBuffer mCubeColors;
    private final FloatBuffer mCubeNormals;

    private final float[] mLightPosInModelSpace = {0.0f, 0.0f, 0.0f, 1.0f};
    private final float[] mLightPosInWorldSpace = new float[4];
    private final float[] mLightPosInEyeSpace = new float[4];

    private int mProgramHandle;
    private int mPointProgramHandle;

    float[] crossProduct(float[] fst, float[] sec, boolean normalize)
    {
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
    
    public static int loadTexture(final String assetPath, Context context)
    {
        final int[] textureHandle = new int[1];
     
        GLES20.glGenTextures(1, textureHandle, 0);
     
        if (textureHandle[0] != 0)
        {
            try
            {
                InputStream is = context.getAssets().open(assetPath);
                final Bitmap bitmap = BitmapFactory.decodeStream(is);
         
                // Bind to the texture in OpenGL
                GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureHandle[0]);
         
                // Set filtering
                GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_NEAREST);
                GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_NEAREST);
         
                // Load the bitmap into the bound texture.
                GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, bitmap, 0);
         
                // Recycle the bitmap, since its data has been loaded into OpenGL.
                bitmap.recycle();
            }
            catch (Exception e)
            {
                throw new RuntimeException("Error loading texture file " + assetPath);
            }
        }
     
        if (textureHandle[0] == 0)
        {
            throw new RuntimeException("Error loading texture.");
        }
     
        return textureHandle[0];
    }

    FloatBuffer bufferBoilerplate(final float[] values)
    {
        FloatBuffer result = ByteBuffer.allocateDirect(values.length * mBytesPerFloat)
                .order(ByteOrder.nativeOrder()).asFloatBuffer();
        result.put(values).position(0);
        return result;
    }

    public BasicRenderer(final Context activityContext)
    {
        mActivityContext = activityContext;
        mCubePositions = bufferBoilerplate(CubeData.positionArray);	
        mCubeColors = bufferBoilerplate(CubeData.collorArray);
        mCubeNormals = bufferBoilerplate( normals(CubeData.positionArray, mPositionDataSize) );
        mCubeTextureCoordinates = bufferBoilerplate(CubeData.textureCoordinatesArray );
    }

    @Override
    public void onSurfaceCreated(GL10 glUnused, EGLConfig config)
    {
        GLES20.glClearColor(0.0f, 0.0f, 0.0f, 0.1f);

        // Use culling to remove back faces.
        GLES20.glEnable(GLES20.GL_CULL_FACE);
        // Enable depth testing
        GLES20.glEnable(GLES20.GL_DEPTH_TEST);

        // Position the eye behind the origin.
        final float eyeX = 0.0f;
        final float eyeY = 0.0f;
        final float eyeZ = -0.5f;

        // We are looking toward the distance
        final float lookX = 0.0f;
        final float lookY = 0.0f;
        final float lookZ = -5.0f;

        // Set our up vector. This is where our head would be pointing were we holding the camera.
        final float upX = 0.0f;
        final float upY = 1.0f;
        final float upZ = 0.0f;

        // Set the view matrix. This matrix can be said to represent the camera position.
        Matrix.setLookAtM(mViewMatrix, 0, eyeX, eyeY, eyeZ, lookX, lookY, lookZ, upX, upY, upZ);

        final int vertexShaderHandle = shaderBoilerplate(GLES20.GL_VERTEX_SHADER, vertexShader);
        final int fragmentShaderHandle = shaderBoilerplate(GLES20.GL_FRAGMENT_SHADER, fragmentShader);
        mProgramHandle = programBoilerplate(vertexShaderHandle, fragmentShaderHandle, new String[] { "a_Position", "a_Color", "a_Normal", "a_TexCoordinate"});

        final int pointVertexShaderHandle = shaderBoilerplate(GLES20.GL_VERTEX_SHADER, pointVertexShader);
        final int pointFragmentShaderHandle = shaderBoilerplate(GLES20.GL_FRAGMENT_SHADER, pointFragmentShader);
        mPointProgramHandle = programBoilerplate(pointVertexShaderHandle, pointFragmentShaderHandle, new String[] { "a_Position" });
        
        mTextureDataHandle = loadTexture("stone.png", mActivityContext);
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
        // Do a complete rotation every 10 seconds.
        long time = SystemClock.uptimeMillis() % 10000L;
        float angleInDegrees = (360.0f / 10000.0f) * ((int) time);
        GLES20.glClear(GLES20.GL_DEPTH_BUFFER_BIT | GLES20.GL_COLOR_BUFFER_BIT);

        // Set program handles for cube drawing.
        mMVPMatrixHandle = GLES20.glGetUniformLocation(mProgramHandle, "u_MVPMatrix");
        mMVMatrixHandle = GLES20.glGetUniformLocation(mProgramHandle, "u_MVMatrix"); 
        mLightPositionHandle = GLES20.glGetUniformLocation(mProgramHandle, "u_LightPos");
        mPositionHandle = GLES20.glGetAttribLocation(mProgramHandle, "a_Position");
        mColorHandle = GLES20.glGetAttribLocation(mProgramHandle, "a_Color");
        mNormalHandle = GLES20.glGetAttribLocation(mProgramHandle, "a_Normal"); 
        
        mTextureUniformHandle = GLES20.glGetUniformLocation(mProgramHandle, "u_Texture");
        mTextureCoordinateHandle = GLES20.glGetAttribLocation(mProgramHandle, "a_TexCoordinate");     
        // Set the active texture unit to texture unit 0.
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        // Bind the texture to this unit.
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mTextureDataHandle);
        // Tell the texture uniform sampler to use this texture in the shader by binding to texture unit 0.
        GLES20.glUniform1i(mTextureUniformHandle, 0);
        
        // Calculate position of the light. Rotate and then push into the distance.
        Matrix.setIdentityM(mLightModelMatrix, 0);
        Matrix.translateM(mLightModelMatrix, 0, 0.0f, 0.0f, -5.0f);      
        Matrix.rotateM(mLightModelMatrix, 0, angleInDegrees, 0.0f, 1.0f, 0.0f);
        Matrix.translateM(mLightModelMatrix, 0, 0.0f, 0.0f, 2.0f);
               
        Matrix.multiplyMV(mLightPosInWorldSpace, 0, mLightModelMatrix, 0, mLightPosInModelSpace, 0);
        Matrix.multiplyMV(mLightPosInEyeSpace, 0, mViewMatrix, 0, mLightPosInWorldSpace, 0);

        // Draw some cubes.        
        Matrix.setIdentityM(mModelMatrix, 0);
        Matrix.translateM(mModelMatrix, 0, 4.0f, 0.0f, -7.0f);
        Matrix.rotateM(mModelMatrix, 0, angleInDegrees, 1.0f, 0.0f, 0.0f);        
        drawVertices(mCubePositions, mCubeColors, mCubeNormals, mCubeTextureCoordinates);

        Matrix.setIdentityM(mModelMatrix, 0);
        Matrix.translateM(mModelMatrix, 0, -4.0f, 0.0f, -7.0f);
        Matrix.rotateM(mModelMatrix, 0, angleInDegrees, 0.0f, 1.0f, 0.0f);        
        drawVertices(mCubePositions, mCubeColors, mCubeNormals, mCubeTextureCoordinates);

        Matrix.setIdentityM(mModelMatrix, 0);
        Matrix.translateM(mModelMatrix, 0, 0.0f, 4.0f, -7.0f);
        Matrix.rotateM(mModelMatrix, 0, angleInDegrees, 0.0f, 0.0f, 1.0f);        
        drawVertices(mCubePositions, mCubeColors, mCubeNormals, mCubeTextureCoordinates);

        Matrix.setIdentityM(mModelMatrix, 0);
        Matrix.translateM(mModelMatrix, 0, 0.0f, -4.0f, -7.0f);        
        drawVertices(mCubePositions, mCubeColors, mCubeNormals, mCubeTextureCoordinates);

        Matrix.setIdentityM(mModelMatrix, 0);
        Matrix.translateM(mModelMatrix, 0, 0.0f, 0.0f, -5.0f);
        Matrix.rotateM(mModelMatrix, 0, angleInDegrees, 1.0f, 1.0f, 0.0f);        
        drawVertices(mCubePositions, mCubeColors, mCubeNormals, mCubeTextureCoordinates);

        Matrix.setIdentityM(mModelMatrix, 0);
        Matrix.translateM(mModelMatrix, 0, 0.0f, 0.0f, -5.0f);      
        Matrix.rotateM(mModelMatrix, 0, angleInDegrees, 0.0f, 1.0f, 0.0f);
        Matrix.translateM(mModelMatrix, 0, 0.0f, 0.0f, 2.0f);
        drawLight();
    }


    private void drawVertices(final FloatBuffer positionsBuffer, final FloatBuffer colorsBuffer, final FloatBuffer normalsBuffer, final FloatBuffer textureCoordinatesBuffer)
    {	
        GLES20.glUseProgram(mProgramHandle);

        // Pass in the position information
        positionsBuffer.position(0);		
        GLES20.glVertexAttribPointer(mPositionHandle, mPositionDataSize, GLES20.GL_FLOAT, false, 0, positionsBuffer);        
        GLES20.glEnableVertexAttribArray(mPositionHandle);        

        // Pass in the color information
        colorsBuffer.position(0);
        GLES20.glVertexAttribPointer(mColorHandle, mColorDataSize, GLES20.GL_FLOAT, false, 0, colorsBuffer);        
        GLES20.glEnableVertexAttribArray(mColorHandle);
        
        // Pass in the normal information
        normalsBuffer.position(0);
        GLES20.glVertexAttribPointer(mNormalHandle, mNormalDataSize, GLES20.GL_FLOAT, false, 0, normalsBuffer);        
        GLES20.glEnableVertexAttribArray(mNormalHandle);
        
        textureCoordinatesBuffer.position(0);
        GLES20.glVertexAttribPointer(mTextureCoordinateHandle, mTextureCoordinateDataSize, GLES20.GL_FLOAT, false, 0, textureCoordinatesBuffer);        
        GLES20.glEnableVertexAttribArray(mTextureCoordinateHandle);
        
		// This multiplies the view matrix by the model matrix, and stores the result in the MVP matrix
        // (which currently contains model * view).
        Matrix.multiplyMM(mMVPMatrix, 0, mViewMatrix, 0, mModelMatrix, 0);
        GLES20.glUniformMatrix4fv(mMVMatrixHandle, 1, false, mMVPMatrix, 0);                
        
        // This multiplies the modelview matrix by the projection matrix, and stores the result in the MVP matrix
        // (which now contains model * view * projection).
        Matrix.multiplyMM(mMVPMatrix, 0, mProjectionMatrix, 0, mMVPMatrix, 0);
        GLES20.glUniformMatrix4fv(mMVPMatrixHandle, 1, false, mMVPMatrix, 0);   
        
        GLES20.glUniform3f(mLightPositionHandle, mLightPosInEyeSpace[0], mLightPosInEyeSpace[1], mLightPosInEyeSpace[2]);
            
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
              "uniform mat4 u_MVPMatrix;      \n"		// A constant representing the combined model/view/projection matrix.
            + "uniform mat4 u_MVMatrix;       \n"		// A constant representing the combined model/view matrix.
            + "uniform mediump vec3 u_LightPos;       \n"	    // The position of the light in eye space.
            
            + "attribute vec4 a_Position;     \n"		// Per-vertex position information we will pass in.
            + "attribute vec4 a_Color;        \n"		// Per-vertex color information we will pass in.
            + "attribute vec3 a_Normal;       \n"		// Per-vertex normal information we will pass in.
            + "attribute vec2 a_TexCoordinate;\n"       // Per-vertex texture coordinate information we will pass in.
            
            + "varying vec4 v_Color;          \n"		// This will be passed into the fragment shader.
            + "varying vec3 v_Normal;         \n"
            + "varying vec3 v_Position;       \n"
            + "varying float v_Distance;      \n"
            + "varying vec2 v_TexCoordinate;  \n"
            
            + "void main()                    \n" 	// The entry point for our vertex shader.
            + "{                              \n"		
            + "   v_Position = vec3(u_MVMatrix * a_Position);            \n"
            + "   v_Normal = vec3(u_MVMatrix * vec4(a_Normal, 0.0));     \n"
            + "   v_Distance = length(u_LightPos - v_Position);     \n"
            + "   v_Color = a_Color;                                     \n"
            + "   v_TexCoordinate = a_TexCoordinate;"
            + "   gl_Position = u_MVPMatrix * a_Position;                \n"     
            + "}                                                         \n"; 

    final String fragmentShader =
            "precision mediump float;       \n"
            + "uniform vec3 u_LightPos;       \n"	    // The position of the light in eye space.
            + "uniform sampler2D u_Texture;   \n"
            
            + "varying vec4 v_Color;          \n"
            + "varying vec3 v_Normal;         \n"
            + "varying vec3 v_Position;       \n"
            + "varying float v_Distance;      \n"
            + "varying vec2 v_TexCoordinate;  \n"
            
            + "void main()                    \n"     // The entry point for our fragment shader.
            + "{                              \n"
            + "   vec3 lightVector = normalize(u_LightPos - v_Position);    \n"
            + "   float diffuse = max(dot(v_Normal, lightVector), 0.1);     \n"
            + "   diffuse = diffuse * (1.0 / (1.0 + (0.1 * v_Distance * v_Distance)));\n"
            + "   diffuse = diffuse + 0.3; \n" //some ambient lightning
            + "   gl_FragColor = v_Color * diffuse * texture2D(u_Texture, v_TexCoordinate);     \n"
            + "}                              \n";

    // Define a simple shader program for our point.
    final String pointVertexShader =
            "uniform mat4 u_MVPMatrix;      \n"		
            +	"attribute vec4 a_Position;     \n"		
            + "void main()                    \n"
            + "{                              \n"
            + "   gl_Position = u_MVPMatrix   \n"
            + "               * a_Position;   \n"
            + "   gl_PointSize = 3.0;         \n"
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
