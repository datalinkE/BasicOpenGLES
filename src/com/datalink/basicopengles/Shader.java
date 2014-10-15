package com.datalink.basicopengles;


import android.content.Context;
import android.opengl.GLES20;

public abstract class Shader
{   
    protected float[] mViewMatrix;
    protected float[] mProjectionMatrix;
    
    protected float[] mMVPMatrix = new float[16];
    
    protected abstract String vertexShaderAsset();
    protected abstract String fragmentShaderAsset();
    protected abstract String[] attributes();
    
    protected int mProgramHandle;
    
    public Shader(float[] viewMatrix, float[] projectionMatrix, Context activityContext)
    {
    	mViewMatrix = viewMatrix;
    	mProjectionMatrix = projectionMatrix;
    	
    	String vertexShader = AssetHelpers.loadText(vertexShaderAsset(), activityContext);
        String fragmentShader = AssetHelpers.loadText(fragmentShaderAsset(), activityContext);
    	final int vertexShaderHandle = ShaderHelpers.shaderBoilerplate(GLES20.GL_VERTEX_SHADER, vertexShader);
        final int fragmentShaderHandle = ShaderHelpers.shaderBoilerplate(GLES20.GL_FRAGMENT_SHADER, fragmentShader);
        
    	mProgramHandle = ShaderHelpers.programBoilerplate(vertexShaderHandle, fragmentShaderHandle, attributes());
    }
    
    public abstract void draw(float[] modelMatrix, Mesh mesh);
}

