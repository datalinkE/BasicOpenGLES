package com.datalink.basicopengles;

import java.nio.FloatBuffer;

public interface Mesh {

	// Typically its just a placeholder for easy argument passing of static final buffers.
	// Avoid multithreading since FloatBuffer has mutable state.
	
    FloatBuffer positions();
    FloatBuffer colors();
    FloatBuffer normals();
	FloatBuffer textureCoordinates();
	
}
