package com.datalink.basicopengles;

import android.opengl.Matrix;

public class ShaderHelpers
{

    float[] crossProduct(float[] fst, float[] sec, boolean normalize)
    {
        float new0 = -fst[1]*sec[2] + fst[2]*sec[1];
        float new1 = -fst[2]*sec[0] + fst[0]*sec[2];
        float new2 = fst[1]*sec[0] - fst[0]*sec[1];
        if( normalize )
        {
            float length = Matrix.length(new0, new1, new2);
            float[] result = { new0/length, new1/length, new2/length};
            return result;
        }

        float[] result = { new0, new1, new2 };
        return result;
    }

    float[] normals(float[] vertices, int strideSize)
    {
        int verticesInGroup = 3;

        int len = vertices.length / (strideSize*verticesInGroup);
        int size = len*verticesInGroup*mPositionDataSize;
        float[] result = new float[size];
        float[] fst = new float[mPositionDataSize];
        float[] sec = new float[mPositionDataSize];
        for (int i = 0; i < size; i+=strideSize*verticesInGroup)
        {
            for (int j = 0; j < mPositionDataSize; j++)
            {
                fst[j] = vertices[i+j] - vertices[i+strideSize+j];
                sec[j] = vertices[i+strideSize*2+j] - vertices[i+strideSize+j];
            }

            float[] cross = crossProduct(fst, sec, true);
            for (int k = 0; k < verticesInGroup; k++)
            {
                System.arraycopy(cross, 0, result, i+k*strideSize,  mPositionDataSize);
            }
        }
        return result;
    }


    FloatBuffer bufferBoilerplate(final float[] values)
    {
        FloatBuffer result = ByteBuffer.allocateDirect(values.length * mBytesPerFloat)
                .order(ByteOrder.nativeOrder()).asFloatBuffer();
        result.put(values).position(0);
        return result;
    }
}
