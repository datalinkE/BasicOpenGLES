package com.datalink.basicopengles;

import java.util.ArrayList;
import java.util.Arrays;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import android.content.Context;
import android.opengl.GLES20;
import android.opengl.Matrix;
import android.opengl.GLSurfaceView.Renderer;
import android.os.SystemClock;

public class BasicRenderer implements Renderer{

    private final Context mActivityContext;
    
    private float[] mViewMatrix = new float[16];
    private float[] mProjectionMatrix = new float[16];
    private float[] mModelMatrix = new float[16];
    private float[] mLightModelMatrix = new float[16];

    private int mTextureDataHandle;

    private final float[] mLightPosInModelSpace = {0.0f, 0.0f, 0.0f, 1.0f};
    private final float[] mLightPosInWorldSpace = new float[4];
    private final float[] mLightPosInEyeSpace = new float[4];

    private Shader mTexLightShader;
    private PointShader mPointShader;
    private BlendShader mBlendShader;
    
    public BasicRenderer(final Context activityContext)
    {
        mActivityContext = activityContext;
    }

    @Override
    public void onSurfaceCreated(GL10 glUnused, EGLConfig config)
    {
        GLES20.glClearColor(0.0f, 0.0f, 0.0f, 0.1f);

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

        mTextureDataHandle = AssetHelpers.loadTexture("stone.png", mActivityContext);  
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
        
        //TODO: find out why eye space
        mTexLightShader = new TexLightShader(mViewMatrix, mProjectionMatrix, mTextureDataHandle, mLightPosInEyeSpace, mActivityContext);
        mPointShader = new PointShader(mViewMatrix, mProjectionMatrix, mActivityContext);
        mBlendShader = new BlendShader(mViewMatrix, mProjectionMatrix, mActivityContext);
    }

    @Override
    public void onDrawFrame(GL10 glUnused)
    {
        // Do a complete rotation every 10 seconds.
        long time = SystemClock.uptimeMillis() % 10000L;
        float angleInDegrees = (360.0f / 10000.0f) * ((int) time);
        GLES20.glClear(GLES20.GL_DEPTH_BUFFER_BIT | GLES20.GL_COLOR_BUFFER_BIT);
        
        // Calculate position of the light. Rotate and then push into the distance.
        Matrix.setIdentityM(mLightModelMatrix, 0);
        Matrix.translateM(mLightModelMatrix, 0, 0.0f, 0.0f, -5.0f);      
        Matrix.rotateM(mLightModelMatrix, 0, angleInDegrees, 0.0f, 1.0f, 0.0f);
        Matrix.translateM(mLightModelMatrix, 0, 0.0f, 0.0f, 2.0f);
               
        Matrix.multiplyMV(mLightPosInWorldSpace, 0, mLightModelMatrix, 0, mLightPosInModelSpace, 0);
        Matrix.multiplyMV(mLightPosInEyeSpace, 0, mViewMatrix, 0, mLightPosInWorldSpace, 0);

        Mesh cube = new CubeMesh();
        
        // Draw some cubes.        
        //Matrix.setIdentityM(mModelMatrix, 0);
        Drawable rightCube = new Drawable(cube, new ArrayList<Shader>(Arrays.asList(mTexLightShader, mPointShader)));
        Matrix.translateM(rightCube.mModelMatrix, 0, 4.0f, 0.0f, -7.0f);
        Matrix.rotateM(rightCube.mModelMatrix, 0, angleInDegrees, 1.0f, 0.0f, 0.0f);        
        rightCube.draw();

        Matrix.setIdentityM(mModelMatrix, 0);
        Matrix.translateM(mModelMatrix, 0, -4.0f, 0.0f, -7.0f);
        Matrix.rotateM(mModelMatrix, 0, angleInDegrees, 0.0f, 1.0f, 0.0f);        
        mTexLightShader.draw(mModelMatrix, cube);

        Matrix.setIdentityM(mModelMatrix, 0);
        Matrix.translateM(mModelMatrix, 0, 0.0f, 4.0f, -7.0f);
        Matrix.rotateM(mModelMatrix, 0, angleInDegrees, 0.0f, 0.0f, 1.0f);        
        mTexLightShader.draw(mModelMatrix, cube);

        Matrix.setIdentityM(mModelMatrix, 0);
        Matrix.translateM(mModelMatrix, 0, 0.0f, -4.0f, -7.0f);  
        Matrix.rotateM(mModelMatrix, 0, angleInDegrees, 1.0f, 1.0f, 0.0f);         
        mTexLightShader.draw(mModelMatrix, cube);

        Matrix.setIdentityM(mModelMatrix, 0);
        Matrix.translateM(mModelMatrix, 0, 0.0f, 0.0f, -5.0f);     
        mTexLightShader.draw(mModelMatrix, cube);
        
        mPointShader.draw(mLightModelMatrix);
        
        Mesh rectangle = new RectangleMesh();
        
        Matrix.setIdentityM(mModelMatrix, 0);
        Matrix.translateM(mModelMatrix, 0, 0.0f, 0.0f, -3.0f);
        mBlendShader.draw(mModelMatrix, rectangle);
    }
}
