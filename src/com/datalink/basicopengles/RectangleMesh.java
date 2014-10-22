package com.datalink.basicopengles;

import java.nio.FloatBuffer;

public class RectangleMesh implements Mesh{

	public final FloatBuffer positions()
	{
		return sPositions;
	}

	public final FloatBuffer colors()
	{
		return sColors;
	}
	
	public static final float[] sPositionArray =
	{
		// Front face
		-1.0f, 1.0f, 0.0f,				
		-1.0f, -1.0f, 0.0f,
		1.0f, 1.0f, 0.0f,
		
		-1.0f, -1.0f, 0.0f, 				
		1.0f, -1.0f, 0.0f,
		1.0f, 1.0f, 0.0f
	};
	
	public static final float[] sColorArray =
	{		
			// Back face (blue)
			0.0f, 0.0f, 1.0f, 0.3f,	
			0.0f, 0.0f, 1.0f, 0.3f,	
			0.0f, 0.0f, 1.0f, 0.3f,	
			0.0f, 0.0f, 1.0f, 0.3f,	
			0.0f, 0.0f, 1.0f, 0.3f,	
			0.0f, 0.0f, 1.0f, 0.3f,	
	};
	
	static final FloatBuffer sPositions = ShaderHelpers.bufferBoilerplate(sPositionArray);
	static final FloatBuffer sColors = ShaderHelpers.bufferBoilerplate(sColorArray);
	
	@Override
	public FloatBuffer normals() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public FloatBuffer textureCoordinates() {
		// TODO Auto-generated method stub
		return null;
	}
	
}
