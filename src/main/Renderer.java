package main;

import global.AbstractRenderer;
import global.GLCamera;
import lwjglutils.OGLModelOBJ;
import lwjglutils.OGLTexture2D;
import org.lwjgl.BufferUtils;
import org.lwjgl.glfw.*;

import java.io.IOException;
import java.nio.DoubleBuffer;
import java.util.ArrayList;
import java.util.List;

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
    private List<OGLModelOBJ> scene;
    private List<OGLTexture2D> textures;
    private GLCamera camera;
    private float trans, deltaTrans = 0;
    private float px, py, pz;
    private float zenit, azimut;
    private float[] modelMatrix = new float[16];
    private boolean mouseButton1, mouseButton2 = false;
    private boolean isWired, flatShading = false;
    private boolean perspective = true;
    private float dx, dy, ox, oy;
    private int[] vboIdList = new int[3];
    private List<OBJLoader> objList = new ArrayList<>();
    private float mouseX, mouseY;

    public Renderer() {
        super();
        // REMOVE IF NOT MACOS USER
        width = width*2;
        height = height*2;

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
                    glfwSetWindowShouldClose(window, true);
                if (action == GLFW_RELEASE) {

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
                            px += trans;
                            camera.left(trans);
                            if (deltaTrans < 0.001f)
                                deltaTrans = 0.001f;
                            else
                                deltaTrans *= 1.01;
                            break;

                        case GLFW_KEY_D:
                            px -= trans;
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

                        case GLFW_KEY_L:
                            flatShading = !flatShading;
                            break;
                        case GLFW_KEY_M:
                            isWired = !isWired;
                            break;
                        case GLFW_KEY_P:
                            perspective = !perspective;
                            break;
                    }
                }
            }
        };


        glfwMouseButtonCallback = new GLFWMouseButtonCallback() {

            @Override
            public void invoke(long window, int button, int action, int mods) {
                DoubleBuffer xBuffer = BufferUtils.createDoubleBuffer(1);
                DoubleBuffer yBuffer = BufferUtils.createDoubleBuffer(1);
                glfwGetCursorPos(window, xBuffer, yBuffer);
                double x = xBuffer.get(0);
                double y = yBuffer.get(0);

                mouseButton1 = glfwGetMouseButton(window, GLFW_MOUSE_BUTTON_1) == GLFW_PRESS;
                mouseButton2 = glfwGetMouseButton(window, GLFW_MOUSE_BUTTON_2) == GLFW_PRESS;

                if (mouseButton1) {
                    ox = (float) x;
                    oy = (float) y;
                }
                else if (mouseButton2) {
                    mouseX = (float) x;
                    mouseY = (float) y;
                }
            }

        };

        glfwCursorPosCallback = new GLFWCursorPosCallback() {
            @Override
            public void invoke(long window, double x, double y) {
                if (mouseButton1) {
                    dx = (float) x - ox;
                    dy = (float) y - oy;
                    ox = (float) x;
                    oy = (float) y;
                    zenit -= dy / width * 180;
                    if (zenit > 90)
                        zenit = 90;
                    if (zenit <= -90)
                        zenit = -90;
                    azimut += dx / height * 180;
                    azimut = azimut % 360;
                    camera.setAzimuth(Math.toRadians(azimut));
                    camera.setZenith(Math.toRadians(zenit));
                    dx = 0;
                    dy = 0;
                }
                else if(mouseButton2){
                    mouseX = (float) x;
                    mouseY = (float) y;
                }
            }
        };

        glfwScrollCallback = new GLFWScrollCallback() {
            @Override
            public void invoke(long window, double dx, double dy) {
                if(dy < 0){
                    pz -= trans;
                    camera.forward(trans);
                    if (deltaTrans < 0.001f)
                        deltaTrans = 0.001f;
                    else
                        deltaTrans *= 1.01;
                }
                else {
                    pz += trans;
                    camera.backward(trans);
                    if (deltaTrans < 0.001f)
                        deltaTrans = 0.001f;
                    else
                        deltaTrans *= 1.01;
                }

            }
        };
    }

    @Override
    public void init()  {
        glClearColor(0.0f, 0.0f, 0.0f, 1.0f); // background color
        glEnable(GL_DEPTH_TEST);

        camera = new GLCamera();
        // setup initial position
        pz = 2;
        py = 0.3f;
        // setup initial light position coord
        mouseX = 620f;
        mouseY = 360f;


        glGenBuffers(vboIdList);
        scene = new ArrayList<>();
        textures = new ArrayList<>();

        OBJLoader obj1 = new OBJLoader();
        OBJLoader obj2 = new OBJLoader();
        OBJLoader obj3 = new OBJLoader();
        objList.addAll(List.of(obj1, obj2, obj3));

        scene.add(obj1.loadObject("/data/obj/custom1.obj"));
        scene.add(obj2.loadObject("/data/obj/custom2.obj"));
        scene.add(obj3.loadObject("/data/obj/custom3.obj"));

        System.out.println("Loading textures...");
        try {
            textures.add(new OGLTexture2D("data/textures/govde_BaseColor.png"));
            textures.add(new OGLTexture2D("data/textures/bacik_BaseColor.png"));
            textures.add(new OGLTexture2D("data/textures/taban_BaseColor.png"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    @Override
    public void display() {
        glViewport(0, 0, width, height);
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT); // clear the framebuffer

        // calculate the view parameters
        trans += deltaTrans;

        glMatrixMode(GL_PROJECTION);
        glLoadIdentity();
        if(perspective)
            gluPerspective(45, width / (float) height, 0.1f, 100.0f);
        else
            glOrtho(-1* width / (float) height,
                    1 * width / (float) height,
                    -1, 1, 0.1f, 100.0f);

        glMatrixMode(GL_MODELVIEW);
        glLoadIdentity();

        glGetFloatv(GL_MODELVIEW_MATRIX, modelMatrix);
        glLoadIdentity();

        glRotatef(-zenit, 1.0f, 0, 0);
        glRotatef(azimut, 0, 1.0f, 0);
        glTranslated(-px, -py, -pz);
        glMultMatrixf(modelMatrix);

        glFrontFace(GL_CCW);
        if(isWired)
            glPolygonMode(GL_FRONT_AND_BACK, GL_LINE);
        else
            glPolygonMode(GL_FRONT_AND_BACK, GL_FILL);


        // back face culling
        glEnable(GL_CULL_FACE);
        glCullFace(GL_BACK);

        setupLight();

        // lightning and shading mode
        glEnable(GL_LIGHTING);
        glEnable(GL_LIGHT0);

        drawScene();

        glDisable(GL_LIGHTING);
        glDisable(GL_LIGHT0);
    }
    public void drawScene(){
        glEnableClientState(GL_VERTEX_ARRAY);
        for(int i = 0; i < scene.size(); i++){
            objList.get(i).bind();
            glEnable(GL_TEXTURE_2D);
            glEnableClientState(GL_TEXTURE_COORD_ARRAY);
            glEnableClientState(GL_NORMAL_ARRAY);

            textures.get(i).bind();


            glDrawArrays(objList.get(i).getModel().getTopology(), 0,
                    objList.get(i).getModel().getVerticesBuffer().limit());

            glBindBuffer(GL_ARRAY_BUFFER, 0);

            glDisableClientState(GL_NORMAL_ARRAY);
            glDisableClientState(GL_TEXTURE_COORD_ARRAY);
            glDisable(GL_TEXTURE_2D);
        }

        glDisableClientState(GL_VERTEX_ARRAY);
    }

    public void setupLight(){
        float[] light_position;

        // bod v prostoru
        light_position = new float[]{ mouseX - width / 2f, height / 2f - mouseY, 25, 1.0f};

        glLightfv(GL_LIGHT0, GL_POSITION, light_position);

        if(flatShading)
            glShadeModel(GL_FLAT);
        else
            glShadeModel(GL_SMOOTH);
    }
}
