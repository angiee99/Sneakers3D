package main;

import de.javagl.obj.*;
import global.AbstractRenderer;
import org.lwjgl.glfw.GLFWCursorPosCallback;
import org.lwjgl.glfw.GLFWScrollCallback;
import org.lwjgl.glfw.GLFWWindowSizeCallback;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static global.GluUtils.gluLookAt;
import static global.GluUtils.gluPerspective;
import static org.lwjgl.opengl.GL11.*;

/**
 * Simple scene rendering
 *
 * @author PGRF FIM UHK
 * @version 3.1
 * @since 2020-01-20
 */
public class Renderer extends AbstractRenderer {
    private float angle;
    private float step;
    private boolean toUp;
    private List<Object3D> scene;

    public Renderer() {
        super();
        angle = 15;
        step = 0.05f;
        toUp = true;

        glfwWindowSizeCallback = new GLFWWindowSizeCallback() {
            @Override
            public void invoke(long window, int w, int h) {
                if (w > 0 && h > 0) {
                    width = w;
                    height = h;
                }
            }
        };

        /*used default glfwKeyCallback */

        glfwMouseButtonCallback = null; //glfwMouseButtonCallback do nothing

        glfwCursorPosCallback = new GLFWCursorPosCallback() {
            @Override
            public void invoke(long window, double x, double y) {
                System.out.println("glfwCursorPosCallback");
            }
        };

        glfwScrollCallback = new GLFWScrollCallback() {
            @Override
            public void invoke(long window, double dx, double dy) {
                //do nothing
            }
        };
    }

    @Override
    public void init()  {
        glClearColor(0.0f, 0.0f, 0.0f, 1.0f); // background color
        scene = new ArrayList<>();
        // load OBJ
        try{
            InputStream objInputStream =
                    new FileInputStream("res/data/obj/sneakers.obj");
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
                scene.add(new Object3D(mtl, materialGroup));
            }

        }catch(IOException exception){
            System.out.println("Resource not found");
        }

        glEnable(GL_DEPTH_TEST);
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

    @Override
    public void display() {
        glViewport(0, 0, width*2, height*2); // *2 only for MacOS
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT); // clear the framebuffer


        glMatrixMode(GL_PROJECTION);
        glLoadIdentity();
        gluPerspective(45, width / (float) height, 0.1f, 100.0f);

        glMatrixMode(GL_MODELVIEW);
        glLoadIdentity();
        gluLookAt(0.5, -0.5, 0.5,
                0, 0, 0,
                0, 0, 1);
        glPushMatrix();

        glBegin(GL_LINES);
        glColor3f(1f, 0f, 0f);
        glVertex3f(0f, 0f, 0f);
        glVertex3f(1f, 0f, 0f);

        glColor3f(0f, 1f, 0f);
        glVertex3f(0f, 0f, 0f);
        glVertex3f(0f, 1f, 0f);

        glColor3f(0f, 0f, 1f);
        glVertex3f(0f, 0f, 0f);
        glVertex3f(0f, 0f, 1f);

        glEnd(); // !! end after each primitive

        if(toUp){
            if(step < 1){
                glTranslatef(0f, step, 0f);
                step += 0.01f;
            }
            else{
               toUp = false;
            }

        }else {
            if(step > 0){
                glTranslatef(0f, step, 0f);
                step -= 0.01f;
            }else{
                toUp = true;
            }
        }


        glBegin(GL_TRIANGLES);
        glColor3f(1f, 1f, 0f);

        glVertex3f(0f, 0f, 0f);

        glVertex3f(0.15f, 0f, 0f);

        glVertex3f(0f, 0.15f, 0f);
        glEnd();

        glPopMatrix();
        glTranslatef(-.195f, .08f, 0f);
        glRotatef(-angle, 0, 0,1);
        angle = (angle + 1) % 360;
        glTranslatef(.195f, -0.08f, 0f);
        glTranslatef(-.15f, 0, 0);

        glBegin(GL_TRIANGLES);
        glColor3f(0f, 0.5f, 0.5f);

        glVertex3f(0f, 0f, 0f);
        glVertex3f(0f, 0.15f, 0f);
        glVertex3f(-0.15f, 0f, 0f);
        glEnd();

        
    }

}
