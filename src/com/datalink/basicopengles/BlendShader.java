package com.datalink.basicopengles;

import java.nio.FloatBuffer;

import android.content.Context;
import android.opengl.GLES20;
import android.opengl.Matrix;

public class BlendShader extends Shader
{   
    public BlendShader(float[] viewMatrix, float[] projectionMatrix, Context activityContext)
    {
        super(viewMatrix, projectionMatrix, activityContext);
    }
	
	@Override
    protected String vertexShaderAsset() { return "PassShader.vertex"; }
	
	@Override
    protected String fragmentShaderAsset() { return "PassShader.fragment"; }
	
	@Override
    protected String[] attributes()
    { 
    	return new String[] { "a_Position", "a_Color"};
    }
	
	@Override
	public void draw(float[] modelMatrix, Mesh mesh)
	{
        draw(modelMatrix, mesh.positions(), mesh.colors());
	}
	
    public void draw(float[] modelMatrix, FloatBuffer positions, FloatBuffer colors)
    {       
        GLES20.glUseProgram(mProgramHandle);

        // No culling of back faces
        GLES20.glDisable(GLES20.GL_CULL_FACE);
         
        // No depth testing
        GLES20.glDisable(GLES20.GL_DEPTH_TEST);
         
        // Enable blending
        GLES20.glEnable(GLES20.GL_BLEND);
        //GLES20.glBlendFunc(GLES20.GL_SRC_ALPHA, GLES20.GL_ONE_MINUS_SRC_ALPHA);
        //GLES20.glBlendFunc(GLES20.GL_ONE, GLES20.GL_ONE);
        GLES20.glBlendFunc(GLES20.GL_DST_COLOR, GLES20.GL_ZERO);
        
        final int mvpMatrixHandle = GLES20.glGetUniformLocation(mProgramHandle, "u_MVPMatrix");
        final int positionHandle = GLES20.glGetAttribLocation(mProgramHandle, "a_Position");
        final int colorHandle = GLES20.glGetAttribLocation(mProgramHandle, "a_Color");
        
         // Pass in the position information
        positions.position(0);		
        GLES20.glVertexAttribPointer(positionHandle, ShaderHelpers.mPositionDataSize, GLES20.GL_FLOAT, false, 0, positions);        
        GLES20.glEnableVertexAttribArray(positionHandle);        

        colors.position(0);		
        GLES20.glVertexAttribPointer(colorHandle, ShaderHelpers.mColorDataSize, GLES20.GL_FLOAT, false, 0, colors);        
        GLES20.glEnableVertexAttribArray(colorHandle);  
        
        // Pass in the transformation matrix.
        Matrix.multiplyMM(mMVPMatrix, 0, mViewMatrix, 0, modelMatrix, 0);
        Matrix.multiplyMM(mMVPMatrix, 0, mProjectionMatrix, 0, mMVPMatrix, 0);
        GLES20.glUniformMatrix4fv(mvpMatrixHandle, 1, false, mMVPMatrix, 0);

        // Draw the points.
        GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, positions.capacity() / ShaderHelpers.mPositionDataSize);
    }    
}
