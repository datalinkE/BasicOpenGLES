package com.datalink.basicopengles;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import android.opengl.Matrix;

public class Drawable
{
    private final Mesh mMesh;
    private final List<Shader> mShaders;       
    public final float[] mModelMatrix = new float[16];
    
    public Drawable(Mesh mesh, Shader shader)
    {
        mMesh = mesh;
        mShaders = new ArrayList<Shader>(Arrays.asList(shader));
        Matrix.setIdentityM(mModelMatrix, 0);
    }   
    
    public Drawable(Mesh mesh, List<Shader> shaders)
    {
        mMesh = mesh;
        mShaders = shaders;
        Matrix.setIdentityM(mModelMatrix, 0);
    }
    
    public void draw()
    {
        for(Shader shader : mShaders)
        {
            shader.draw(mModelMatrix, mMesh);
        }
    }
}
