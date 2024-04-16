package main;

import de.javagl.obj.*;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class OBJParser {
    public Optional<List<Object3D>> getObjectsByMaterial (String path){
        List<Object3D> result = new ArrayList<>();
        try{
            InputStream objInputStream = new FileInputStream(path);
            Obj originalObj = ObjReader.read(objInputStream);

            // Convert the OBJ into a "renderable" OBJ.
            // (See ObjUtils#convertToRenderable for details)
            Obj obj = ObjUtils.convertToRenderable(originalObj);

            // The OBJ may refer to multiple MTL files using the "mtllib"
            // directive. Each MTL file may contain multiple materials.
            // Here, all materials (in form of Mtl objects) are collected.

            List<Mtl> allMtls = new ArrayList<>();
            for (String mtlFileName : obj.getMtlFileNames())
            {
                InputStream mtlInputStream =
                        new FileInputStream("res/data/material/" + mtlFileName);
                List<Mtl> mtls = MtlReader.read(mtlInputStream);
                allMtls.addAll(mtls);
            }

            // Split the OBJ into multiple parts. Each key of the resulting
            // map will be the name of one material. Each value will be
            // an OBJ that contains the OBJ data that has to be rendered
            // with this material.
            Map<String, Obj> materialGroups =
                    ObjSplitting.splitByMaterialGroups(obj);

            for (Map.Entry<String, Obj> entry : materialGroups.entrySet())
            {
                String materialName = entry.getKey();
                Obj materialGroup = entry.getValue();

                System.out.println("Material name  : " + materialName);
                System.out.println("Material group : " + materialGroup);

                // Find the MTL that defines the material with the current name
                Mtl mtl = findMtlForName(allMtls, materialName);

                // Render the current material group with this material:
                result.add(new Object3D(mtl, materialGroup));
            }

            return Optional.of(result);
        }catch(IOException exception){
            System.out.println("Resource not found");
            return Optional.empty();
        }

    }

    private static Mtl findMtlForName(Iterable<? extends Mtl> mtls, String name)
    {
        for (Mtl mtl : mtls)
        {
            if (mtl.getName().equals(name))
            {
                return mtl;
            }
        }
        return null;
    }
}
