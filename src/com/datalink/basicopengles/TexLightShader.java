package com.datalink.basicopengles;

import java.nio.FloatBuffer;

import android.content.Context;
import android.opengl.GLES20;
import android.opengl.Matrix;

public class TexLightShader
{	
private static int mProgramHandle = -1;

private static int mPositionHandle;
private static int mColorHandle;
private static int mNormalHandle;
private static int mTextureCoordinateHandle;

private static int mTextureUniformHandle;
private int mTextureDataHandle;

private float[] mViewMatrix;
private float[] mProjectionMatrix;
private float[] mMVPMatrix = new float[16];

private static int mMVMatrixHandle;
private static int mMVPMatrixHandle;

private float[] mLightPosInEyeSpace;
private static int mLightPositionHandle;

static void init(Context activityContext)
{
	String vertexShader = AssetHelpers.loadText("BasicShader.vertex", activityContext);
    String fragmentShader = AssetHelpers.loadText("BasicShader.fragment", activityContext);
	final int vertexShaderHandle = ShaderHelpers.shaderBoilerplate(GLES20.GL_VERTEX_SHADER, vertexShader);
    final int fragmentShaderHandle = ShaderHelpers.shaderBoilerplate(GLES20.GL_FRAGMENT_SHADER, fragmentShader);
    
	mProgramHandle = ShaderHelpers.programBoilerplate(vertexShaderHandle, fragmentShaderHandle, new String[] { "a_Position", "a_Color", "a_Normal", "a_TexCoordinate"});

	// Set program handles for cube drawing.
    mMVPMatrixHandle = GLES20.glGetUniformLocation(mProgramHandle, "u_MVPMatrix");
    mMVMatrixHandle = GLES20.glGetUniformLocation(mProgramHandle, "u_MVMatrix"); 
    mLightPositionHandle = GLES20.glGetUniformLocation(mProgramHandle, "u_LightPos");
    mPositionHandle = GLES20.glGetAttribLocation(mProgramHandle, "a_Position");
    mColorHandle = GLES20.glGetAttribLocation(mProgramHandle, "a_Color");
    mNormalHandle = GLES20.glGetAttribLocation(mProgramHandle, "a_Normal"); 
    
    mTextureUniformHandle = GLES20.glGetUniformLocation(mProgramHandle, "u_Texture");
    mTextureCoordinateHandle = GLES20.glGetAttribLocation(mProgramHandle, "a_TexCoordinate");     
}

public TexLightShader(float[] viewMatrix, float[] projectionMatrix, int textureDataHandle, float lightPosInEyeSpace[])
{
	if(mProgramHandle == -1)
	{
		throw new RuntimeException("TexLightShader class used before init() call.");
	}
	
	mViewMatrix = viewMatrix;
	mProjectionMatrix = projectionMatrix;
	mTextureDataHandle = textureDataHandle;
	mLightPosInEyeSpace = lightPosInEyeSpace;
}

public void drawVertices(float[] modelMatrix, final FloatBuffer positionsBuffer, final FloatBuffer colorsBuffer, final FloatBuffer normalsBuffer, final FloatBuffer textureCoordinatesBuffer)
    {	
        GLES20.glUseProgram(mProgramHandle);
        
        // Set the active texture unit to texture unit 0.
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        // Bind the texture to this unit.
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mTextureDataHandle);
        // Tell the texture uniform sampler to use this texture in the shader by binding to texture unit 0.
        GLES20.glUniform1i(mTextureUniformHandle, 0);

        // Pass in the position information
        positionsBuffer.position(0);		
        GLES20.glVertexAttribPointer(mPositionHandle, ShaderHelpers.mPositionDataSize, GLES20.GL_FLOAT, false, 0, positionsBuffer);        
        GLES20.glEnableVertexAttribArray(mPositionHandle);        

        // Pass in the color information
        colorsBuffer.position(0);
        GLES20.glVertexAttribPointer(mColorHandle, ShaderHelpers.mColorDataSize, GLES20.GL_FLOAT, false, 0, colorsBuffer);        
        GLES20.glEnableVertexAttribArray(mColorHandle);
        
        // Pass in the normal information
        normalsBuffer.position(0);
        GLES20.glVertexAttribPointer(mNormalHandle, ShaderHelpers.mNormalDataSize, GLES20.GL_FLOAT, false, 0, normalsBuffer);        
        GLES20.glEnableVertexAttribArray(mNormalHandle);
        
        textureCoordinatesBuffer.position(0);
        GLES20.glVertexAttribPointer(mTextureCoordinateHandle, ShaderHelpers.mTextureCoordinateDataSize, GLES20.GL_FLOAT, false, 0, textureCoordinatesBuffer);        
        GLES20.glEnableVertexAttribArray(mTextureCoordinateHandle);
        
		// This multiplies the view matrix by the model matrix, and stores the result in the MVP matrix
        // (which currently contains model * view).
        Matrix.multiplyMM(mMVPMatrix, 0, mViewMatrix, 0, modelMatrix, 0);
        GLES20.glUniformMatrix4fv(mMVMatrixHandle, 1, false, mMVPMatrix, 0);                
        
        // This multiplies the modelview matrix by the projection matrix, and stores the result in the MVP matrix
        // (which now contains model * view * projection).
        Matrix.multiplyMM(mMVPMatrix, 0, mProjectionMatrix, 0, mMVPMatrix, 0);
        GLES20.glUniformMatrix4fv(mMVPMatrixHandle, 1, false, mMVPMatrix, 0);   
        
        GLES20.glUniform3f(mLightPositionHandle, mLightPosInEyeSpace[0], mLightPosInEyeSpace[1], mLightPosInEyeSpace[2]);
            
        GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, positionsBuffer.capacity() / ShaderHelpers.mPositionDataSize);                               
    }
}
