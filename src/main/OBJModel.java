package main;

import lwjglutils.OGLModelOBJ;
import java.nio.FloatBuffer;

import static org.lwjgl.opengl.GL11.GL_FLOAT;
import static org.lwjgl.opengl.GL11.glVertexPointer;
import static org.lwjgl.opengl.GL15.*;

public class OBJModel {
    private int pointer;
    OGLModelOBJ model;
    public void loadObject(String filename){
        this.model= new OGLModelOBJ(filename);
    }
    public void bind(){
        glBindBuffer(GL_ARRAY_BUFFER, pointer);

        FloatBuffer fb = model.getVerticesBuffer();
        if (fb != null) {
            pointer = glGenBuffers();
            glBindBuffer(GL_ARRAY_BUFFER, pointer);
            fb.rewind();
            glBufferData(GL_ARRAY_BUFFER, fb, GL_STATIC_DRAW);
            glVertexPointer(4, GL_FLOAT, 0, 0);
        }
        fb = model.getNormalsBuffer();
        if (fb != null) {
            pointer = glGenBuffers();
            glBindBuffer(GL_ARRAY_BUFFER, pointer);
            fb.rewind();
            glBufferData(GL_ARRAY_BUFFER, fb, GL_STATIC_DRAW);
            glColorPointer(3, GL_FLOAT, 3 * 4, 0);
            glNormalPointer(GL_FLOAT, 0, 0);
        }
        fb = model.getTexCoordsBuffer();
        if (fb != null) {
            pointer = glGenBuffers();
            glBindBuffer(GL_ARRAY_BUFFER, pointer);
            fb.rewind();
            glBufferData(GL_ARRAY_BUFFER, fb, GL_STATIC_DRAW);
            glTexCoordPointer(2, GL_FLOAT, 0, 0);
        }

        glBindBuffer(GL_ARRAY_BUFFER, 0);
    }

    public OGLModelOBJ getModel() {
        return model;
    }
}
