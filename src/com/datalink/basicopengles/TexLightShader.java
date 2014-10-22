package com.datalink.basicopengles;

import android.content.Context;
import android.opengl.GLES20;
import android.opengl.Matrix;

public class TexLightShader extends Shader
{

private int mPositionHandle;
private int mColorHandle;
private int mNormalHandle;
private int mTextureCoordinateHandle;

private int mTextureUniformHandle;
private int mTextureDataHandle;

private int mMVMatrixHandle;
private int mMVPMatrixHandle;

private float[] mLightPosInEyeSpace;
private int mLightPositionHandle;

public TexLightShader(float[] viewMatrix, float[] projectionMatrix, int textureDataHandle, float lightPosInEyeSpace[], Context activityContext)
{
    super(viewMatrix, projectionMatrix, activityContext);

	if(mProgramHandle == -1)
	{
		throw new RuntimeException("TexLightShader class used before init() call.");
	}
	
	mViewMatrix = viewMatrix;
	mProjectionMatrix = projectionMatrix;
	mTextureDataHandle = textureDataHandle;
	mLightPosInEyeSpace = lightPosInEyeSpace;

	mMVPMatrixHandle = GLES20.glGetUniformLocation(mProgramHandle, "u_MVPMatrix");
	mMVMatrixHandle = GLES20.glGetUniformLocation(mProgramHandle, "u_MVMatrix"); 
	mLightPositionHandle = GLES20.glGetUniformLocation(mProgramHandle, "u_LightPos");
	mPositionHandle = GLES20.glGetAttribLocation(mProgramHandle, "a_Position");
	mColorHandle = GLES20.glGetAttribLocation(mProgramHandle, "a_Color");
	mNormalHandle = GLES20.glGetAttribLocation(mProgramHandle, "a_Normal"); 

	mTextureUniformHandle = GLES20.glGetUniformLocation(mProgramHandle, "u_Texture");
	mTextureCoordinateHandle = GLES20.glGetAttribLocation(mProgramHandle, "a_TexCoordinate");
}

public void draw(float[] modelMatrix, Mesh mesh)
    {	
        GLES20.glUseProgram(mProgramHandle);
        
        // Use culling to remove back faces.
        GLES20.glEnable(GLES20.GL_CULL_FACE);
        // Enable depth testing
        GLES20.glEnable(GLES20.GL_DEPTH_TEST);
        
        GLES20.glDisable(GLES20.GL_BLEND);
        
        // Set the active texture unit to texture unit 0.
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        // Bind the texture to this unit.
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mTextureDataHandle);
        // Tell the texture uniform sampler to use this texture in the shader by binding to texture unit 0.
        GLES20.glUniform1i(mTextureUniformHandle, 0);

        // Pass in the position information
        mesh.positions().position(0);		
        GLES20.glVertexAttribPointer(mPositionHandle, ShaderHelpers.mPositionDataSize, GLES20.GL_FLOAT, false, 0, mesh.positions());        
        GLES20.glEnableVertexAttribArray(mPositionHandle);        

        // Pass in the color information
        mesh.colors().position(0);
        GLES20.glVertexAttribPointer(mColorHandle, ShaderHelpers.mColorDataSize, GLES20.GL_FLOAT, false, 0, mesh.colors());        
        GLES20.glEnableVertexAttribArray(mColorHandle);
        
        // Pass in the normal information
        mesh.normals().position(0);
        GLES20.glVertexAttribPointer(mNormalHandle, ShaderHelpers.mNormalDataSize, GLES20.GL_FLOAT, false, 0, mesh.normals());        
        GLES20.glEnableVertexAttribArray(mNormalHandle);
        
        mesh.textureCoordinates().position(0);
        GLES20.glVertexAttribPointer(mTextureCoordinateHandle, ShaderHelpers.mTextureCoordinateDataSize, GLES20.GL_FLOAT, false, 0, mesh.textureCoordinates());        
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
            
        GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, mesh.positions().capacity() / ShaderHelpers.mPositionDataSize);                               
    }

@Override
protected String vertexShaderAsset()
{
    return "BasicShader.vertex";
}

@Override
protected String fragmentShaderAsset()
{
    return "BasicShader.fragment";
}

@Override
protected String[] attributes()
{
    return new String[] { "a_Position", "a_Color", "a_Normal", "a_TexCoordinate"};
}
}
