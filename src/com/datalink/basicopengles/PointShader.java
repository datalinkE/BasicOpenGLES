package com.datalink.basicopengles;

import android.content.Context;
import android.opengl.GLES20;
import android.opengl.Matrix;

public class PointShader extends Shader
{   
    public PointShader(float[] viewMatrix, float[] projectionMatrix, Context activityContext)
    {
        super(viewMatrix, projectionMatrix, activityContext);
    }
	
	@Override
    protected String vertexShaderAsset() { return "PointShader.vertex"; }
	
	@Override
    protected String fragmentShaderAsset() { return "PointShader.fragment"; }
	
	@Override
    protected String[] attributes()
    { 
    	return new String[] { "a_Position", "a_Color", "a_Normal", "a_TexCoordinate"};
    }
	
	@Override
	public void draw(float[] modelMatrix, Mesh mesh) {
		draw(modelMatrix);
	}
    
    public void draw(float[] modelMatrix)
    {
        float[] mLightPosInModelSpace = {0.0f, 0.0f, 0.0f, 1.0f};
        
        GLES20.glUseProgram(mProgramHandle);

        final int pointMVPMatrixHandle = GLES20.glGetUniformLocation(mProgramHandle, "u_MVPMatrix");
        final int pointPositionHandle = GLES20.glGetAttribLocation(mProgramHandle, "a_Position");

        // Pass in the position.
        GLES20.glVertexAttrib3f(pointPositionHandle, mLightPosInModelSpace[0], mLightPosInModelSpace[1], mLightPosInModelSpace[2]);

        // Since we are not using a buffer object, disable vertex arrays for this attribute.
        GLES20.glDisableVertexAttribArray(pointPositionHandle);  

        // Pass in the transformation matrix.
        Matrix.multiplyMM(mMVPMatrix, 0, mViewMatrix, 0, modelMatrix, 0);
        Matrix.multiplyMM(mMVPMatrix, 0, mProjectionMatrix, 0, mMVPMatrix, 0);
        GLES20.glUniformMatrix4fv(pointMVPMatrixHandle, 1, false, mMVPMatrix, 0);

        // Draw the point.
        GLES20.glDrawArrays(GLES20.GL_POINTS, 0, 1);
    }    
}
