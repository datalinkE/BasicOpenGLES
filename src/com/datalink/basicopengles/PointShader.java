package com.datalink.basicopengles;

import android.content.Context;
import android.opengl.GLES20;
import android.opengl.Matrix;

public class PointShader
{
    private static int mProgramHandle = -1;
    
    private float[] mViewMatrix;
    private float[] mProjectionMatrix;
    
    private float[] mMVPMatrix = new float[16];
     
    public static void init(Context activityContext)
    {
            
    	String vertexShader = AssetHelpers.loadText("PointShader.vertex", activityContext);
        String fragmentShader = AssetHelpers.loadText("PointShader.fragment", activityContext);
    	final int vertexShaderHandle = ShaderHelpers.shaderBoilerplate(GLES20.GL_VERTEX_SHADER, vertexShader);
        final int fragmentShaderHandle = ShaderHelpers.shaderBoilerplate(GLES20.GL_FRAGMENT_SHADER, fragmentShader);
        
    	mProgramHandle = ShaderHelpers.programBoilerplate(vertexShaderHandle, fragmentShaderHandle, new String[] { "a_Position", "a_Color", "a_Normal", "a_TexCoordinate"});
    }
    
    public PointShader(float[] viewMatrix, float[] projectionMatrix)
    {
            
    	if(mProgramHandle == -1)
    	{
    		throw new RuntimeException("TexLightShader class used before init() call.");
    	}
    	
    	mViewMatrix = viewMatrix;
    	mProjectionMatrix = projectionMatrix;
    }
    
    public void drawPoint(float[] modelMatrix)
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
