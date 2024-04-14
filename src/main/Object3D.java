package main;

import de.javagl.obj.Mtl;
import de.javagl.obj.Obj;

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
}
