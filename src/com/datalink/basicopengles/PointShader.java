package com.datalink.basicopengles;

import java.nio.FloatBuffer;

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
    	return new String[] { "a_Position", "a_Color"};
    }
	
	@Override
	public void draw(float[] modelMatrix, Mesh mesh)
	{
        draw(modelMatrix, mesh.positions());
	}
	
	public void draw(float[] modelMatrix)
	{
	    float[] positionsArray = {0.0f, 0.0f, 0.0f, 1.0f};
        FloatBuffer positions = ShaderHelpers.bufferBoilerplate(positionsArray);
	    draw(modelMatrix, positions);
	}
    
    public void draw(float[] modelMatrix, FloatBuffer positions)
    {       
        GLES20.glUseProgram(mProgramHandle);
        
        // Use culling to remove back faces.
        GLES20.glEnable(GLES20.GL_CULL_FACE);
        // Enable depth testing
        GLES20.glEnable(GLES20.GL_DEPTH_TEST);
        
        GLES20.glDisable(GLES20.GL_BLEND);

        final int pointMVPMatrixHandle = GLES20.glGetUniformLocation(mProgramHandle, "u_MVPMatrix");
        final int pointPositionHandle = GLES20.glGetAttribLocation(mProgramHandle, "a_Position");
        
         // Pass in the position information
        positions.position(0);		
        GLES20.glVertexAttribPointer(pointPositionHandle, ShaderHelpers.mPositionDataSize, GLES20.GL_FLOAT, false, 0, positions);        
        GLES20.glEnableVertexAttribArray(pointPositionHandle);        

        // Pass in the transformation matrix.
        Matrix.multiplyMM(mMVPMatrix, 0, mViewMatrix, 0, modelMatrix, 0);
        Matrix.multiplyMM(mMVPMatrix, 0, mProjectionMatrix, 0, mMVPMatrix, 0);
        GLES20.glUniformMatrix4fv(pointMVPMatrixHandle, 1, false, mMVPMatrix, 0);

        // Draw the points.
        GLES20.glDrawArrays(GLES20.GL_POINTS, 0, positions.capacity() / ShaderHelpers.mPositionDataSize);
    }    
}
