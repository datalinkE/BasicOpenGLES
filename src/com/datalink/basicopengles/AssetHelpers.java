package com.datalink.basicopengles;

import java.io.InputStream;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.opengl.GLES20;
import android.opengl.GLUtils;

public class AssetHelpers
{
    
    public static int loadTexture(final String assetPath, Context context)
    {
        final int[] textureHandle = new int[1];
     
        GLES20.glGenTextures(1, textureHandle, 0);
     
        if (textureHandle[0] != 0)
        {
            try
            {
                InputStream is = context.getAssets().open(assetPath);
                final Bitmap bitmap = BitmapFactory.decodeStream(is);
         
                // Bind to the texture in OpenGL
                GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureHandle[0]);
         
                // Set filtering
                GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_NEAREST);
                GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_NEAREST);
         
                // Load the bitmap into the bound texture.
                GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, bitmap, 0);
         
                // Recycle the bitmap, since its data has been loaded into OpenGL.
                bitmap.recycle();
            }
            catch (Exception e)
            {
                throw new RuntimeException("Error loading texture file " + assetPath);
            }
        }
     
        if (textureHandle[0] == 0)
        {
            throw new RuntimeException("Error loading texture.");
        }
     
        return textureHandle[0];
    }
    
    public static String loadText(final String assetPath, Context context)
    {   
        try
        {
            InputStream stream = context.getAssets().open(assetPath);
            int size = stream.available();
            byte[] buffer = new byte[size];
            stream.read(buffer);
            stream.close();
            return new String(buffer);
        }
        catch (Exception e)
        {
            throw new RuntimeException("Error loading text file " + assetPath);
        }
    }
}
