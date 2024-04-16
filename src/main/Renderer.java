package main;

import global.AbstractRenderer;
import global.GLCamera;
import org.lwjgl.glfw.*;

import java.util.List;
import java.util.Optional;

import static global.GluUtils.gluLookAt;
import static global.GluUtils.gluPerspective;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL15.*;


/**
 * Simple scene rendering
 *
 * @author PGRF FIM UHK
 * @version 3.1
 * @since 2020-01-20
 */
public class Renderer extends AbstractRenderer {
    private List<Object3D> scene;
    private int VBOVertices;
    private int VBOIndices;

    private GLCamera camera;
    private float trans, deltaTrans = 0;
    private float px, py, pz;
    private double ex, ey, ez;
    private float zenit, azimut;



    public Renderer() {
        super();

        glfwWindowSizeCallback = new GLFWWindowSizeCallback() {
            @Override
            public void invoke(long window, int w, int h) {
                if (w > 0 && h > 0) {
                    width = w;
                    height = h;
                }
            }
        };

        glfwKeyCallback = new GLFWKeyCallback() {
            @Override
            public void invoke(long window, int key, int scancode, int action, int mods) {
                if (key == GLFW_KEY_ESCAPE && action == GLFW_RELEASE)
                    // We will detect this in our rendering loop
                    glfwSetWindowShouldClose(window, true);
                if (action == GLFW_RELEASE) {
//                    trans = 0;
//                    deltaTrans = 0;
                }
                if (action == GLFW_PRESS) {
                    switch (key) {
                        case GLFW_KEY_W:
                            pz -= trans;
                            camera.forward(trans);
                            if (deltaTrans < 0.001f)
                                deltaTrans = 0.001f;
                            else
                                deltaTrans *= 1.01;
                            break;

                        case GLFW_KEY_S:
                            pz += trans;
                            camera.backward(trans);
                            if (deltaTrans < 0.001f)
                                deltaTrans = 0.001f;
                            else
                                deltaTrans *= 1.01;
                            break;

                        case GLFW_KEY_A:
                            px -= trans;
                            camera.left(trans);
                            if (deltaTrans < 0.001f)
                                deltaTrans = 0.001f;
                            else
                                deltaTrans *= 1.01;
                            break;

                        case GLFW_KEY_D:
                            px += trans;
                            camera.right(trans);
                            if (deltaTrans < 0.001f)
                                deltaTrans = 0.001f;
                            else
                                deltaTrans *= 1.01;
                            break;
                        case GLFW_KEY_LEFT_SHIFT:

                            py -= trans;
                            camera.up(trans);
                            if (deltaTrans < 0.001f)
                                deltaTrans = 0.001f;
                            else
                                deltaTrans *= 1.01;
                            break;

                        case GLFW_KEY_LEFT_CONTROL:

                            py += trans;

                            camera.down(trans);
                            if (deltaTrans < 0.001f)
                                deltaTrans = 0.001f;
                            else
                                deltaTrans *= 1.01;
                            break;
                    }
                }
            }
        };

        /*used default glfwKeyCallback */
        glfwMouseButtonCallback = new GLFWMouseButtonCallback() {

            @Override
            public void invoke(long window, int button, int action, int mods) {

            }

        };


        glfwCursorPosCallback = new GLFWCursorPosCallback() {
            @Override
            public void invoke(long window, double x, double y) {

            }
        };

        glfwScrollCallback = new GLFWScrollCallback() {
            @Override
            public void invoke(long window, double dx, double dy) {
                camera.forward(0.1);
            }
        };
    }

    @Override
    public void init()  {
        glClearColor(0.0f, 0.0f, 0.0f, 1.0f); // background color

        // load OBJ
        OBJParser objParser = new OBJParser();
        Optional<List<Object3D>> result = objParser.getObjectsByMaterial("res/data/obj/sneakers.obj");

        result.ifPresent(object3DS -> scene = object3DS);
        scene.remove(3); // remove some redundant planes from obj

        glEnable(GL_DEPTH_TEST);

        camera = new GLCamera();
    }


    @Override
    public void display() {
        glViewport(0, 0, width*2, height*2); // *2 only for MacOS
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT); // clear the framebuffer

        trans += deltaTrans;

        double a_rad = azimut * Math.PI / 180;
        double z_rad = zenit * Math.PI / 180;
        ex = Math.sin(a_rad) * Math.cos(z_rad);
        ey = Math.sin(z_rad);
        ez = -Math.cos(a_rad) * Math.cos(z_rad);
        double ux = Math.sin(a_rad) * Math.cos(z_rad + Math.PI / 2);
        double uy = Math.sin(z_rad + Math.PI / 2);
        double uz = -Math.cos(a_rad) * Math.cos(z_rad + Math.PI / 2);

        glMatrixMode(GL_PROJECTION);
        glLoadIdentity();
        gluPerspective(45, width / (float) height, 0.1f, 100.0f);

        glMatrixMode(GL_MODELVIEW);
        glLoadIdentity();

        gluLookAt(px, py, pz, ex + px, ey + py, ez + pz, ux, uy, uz);
//        gluLookAt(1.5, -0.5, 1.0,
//                0, 0, 0,
//                0, 1, 0);
        glPushMatrix();


        for (Object3D object : scene){

            int[] temp = new int[2];
            glGenBuffers(temp);

            VBOVertices = temp[0];
            glBindBuffer(GL_ARRAY_BUFFER, VBOVertices);
            glBufferData(GL_ARRAY_BUFFER, object.getVertices(), GL_STATIC_DRAW);
            glBindBuffer(GL_ARRAY_BUFFER, 0); //?

            VBOIndices = temp[1];
            glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, VBOIndices);
            glBufferData(GL_ELEMENT_ARRAY_BUFFER, object.getIndices(), GL_STATIC_DRAW);
            glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, 0); //?

            glEnableClientState(GL_VERTEX_ARRAY);

            glBindBuffer(GL_ARRAY_BUFFER, VBOVertices);
            glVertexPointer(3, GL_FLOAT, 0, 0);
            glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, VBOIndices);
            glDrawArrays(GL_TRIANGLES, 0, object.getIndices().capacity());

            glDisableClientState(GL_VERTEX_ARRAY);

        }
        
    }

}
