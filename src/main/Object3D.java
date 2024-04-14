package main;

import de.javagl.obj.Mtl;
import de.javagl.obj.Obj;
import de.javagl.obj.ObjData;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;

public class Object3D {
    private Mtl mtl;
    private Obj objectPart;

    public Object3D(Mtl mtl, Obj objectPart) {
        this.mtl = mtl;
        this.objectPart = objectPart;
    }

    public Mtl getMtl() {
        return mtl;
    }

    public void setMtl(Mtl mtl) {
        this.mtl = mtl;
    }

    public Obj getObjectPart() {
        return objectPart;
    }

    public void setObjectPart(Obj objectPart) {
        this.objectPart = objectPart;
    }

    public IntBuffer getIndices(){
        return ObjData.getFaceVertexIndices(objectPart);
    }
    public FloatBuffer getVertices(){
        return ObjData.getVertices(objectPart);
    }
    public FloatBuffer getTexCoords(){
        return ObjData.getTexCoords(objectPart, 2);
    }
    public FloatBuffer getNormals(){
        return ObjData.getNormals(objectPart);
    }
}
