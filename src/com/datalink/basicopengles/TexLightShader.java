package com.datalink.basicopengles;

import java.nio.FloatBuffer;

import android.opengl.GLES20;

public class TexLightShader
{
	/*
private void drawVertices(final FloatBuffer positionsBuffer, final FloatBuffer colorsBuffer, final FloatBuffer normalsBuffer, final FloatBuffer textureCoordinatesBuffer)
    {	
        GLES20.glUseProgram(mProgramHandle);

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
        Matrix.multiplyMM(mMVPMatrix, 0, mViewMatrix, 0, mModelMatrix, 0);
        GLES20.glUniformMatrix4fv(mMVMatrixHandle, 1, false, mMVPMatrix, 0);                
        
        // This multiplies the modelview matrix by the projection matrix, and stores the result in the MVP matrix
        // (which now contains model * view * projection).
        Matrix.multiplyMM(mMVPMatrix, 0, mProjectionMatrix, 0, mMVPMatrix, 0);
        GLES20.glUniformMatrix4fv(mMVPMatrixHandle, 1, false, mMVPMatrix, 0);   
        
        GLES20.glUniform3f(mLightPositionHandle, mLightPosInEyeSpace[0], mLightPosInEyeSpace[1], mLightPosInEyeSpace[2]);
            
        GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, positionsBuffer.capacity() / mPositionDataSize);                               
    }*/
}
