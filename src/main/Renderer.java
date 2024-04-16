package main;

import global.AbstractRenderer;
import org.lwjgl.glfw.GLFWCursorPosCallback;
import org.lwjgl.glfw.GLFWScrollCallback;
import org.lwjgl.glfw.GLFWWindowSizeCallback;

import java.util.List;
import java.util.Optional;

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
    private List<Object3D> scene;

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

        // load OBJ
        OBJParser objParser = new OBJParser();
        Optional<List<Object3D>> result = objParser.getObjectsByMaterial("res/data/obj/sneakers.obj");

        result.ifPresent(object3DS -> scene = object3DS);

        glEnable(GL_DEPTH_TEST);
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

        
    }

}
