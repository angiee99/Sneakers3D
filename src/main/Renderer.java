package main;

import global.AbstractRenderer;
import global.GLCamera;
import lwjglutils.OGLTexture2D;
import org.lwjgl.BufferUtils;
import org.lwjgl.glfw.*;
import transforms.Vec3D;

import java.io.IOException;
import java.nio.DoubleBuffer;
import java.util.ArrayList;
import java.util.List;

import static global.GluUtils.gluPerspective;
import static global.GlutUtils.glutWireCube;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL15.GL_ARRAY_BUFFER;
import static org.lwjgl.opengl.GL15.glBindBuffer;


/**
 * Simple scene rendering
 *
 * @author Anhelina Kulkova
 * @version 1.0
 * @since 2024-05-05
 */
public class Renderer extends AbstractRenderer {
    private List<OGLTexture2D> textures;
    private GLCamera camera;
    private float trans, deltaTrans = 0;
    private float px, py, pz;
    private float zenit, azimut;
    private float[] modelMatrix = new float[16];
    private float[] modelRotateMatrix = new float[16];
    private boolean mouseButton1, mouseButton2;
    private boolean isWired, flatShading;
    private boolean perspective, skybox;
    private float dx, dy, ox, oy;
    private List<OBJModel> objList = new ArrayList<>();
    private float mouseX, mouseY;
    private int textureMode = 0;
    private OGLTexture2D[] textureCube;
    private RotationUtil rotationUtil;

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
                                deltaTrans *= 1.01f;
                            break;

                        case GLFW_KEY_S:
                            pz += trans;
                            camera.backward(trans);
                            if (deltaTrans < 0.001f)
                                deltaTrans = 0.001f;
                            else
                                deltaTrans *= 1.01f;
                            break;

                        case GLFW_KEY_A:
                            px += trans;
                            camera.left(trans);
                            if (deltaTrans < 0.001f)
                                deltaTrans = 0.001f;
                            else
                                deltaTrans *= 1.01f;
                            break;

                        case GLFW_KEY_D:
                            px -= trans;
                            camera.right(trans);
                            if (deltaTrans < 0.001f)
                                deltaTrans = 0.001f;
                            else
                                deltaTrans *= 1.01f;
                            break;
                        case GLFW_KEY_LEFT_SHIFT:

                            py -= trans;
                            camera.up(trans);
                            if (deltaTrans < 0.001f)
                                deltaTrans = 0.001f;
                            else
                                deltaTrans *= 1.01f;
                            break;

                        case GLFW_KEY_LEFT_CONTROL:

                            py += trans;
                            camera.down(trans);
                            if (deltaTrans < 0.001f)
                                deltaTrans = 0.001f;
                            else
                                deltaTrans *= 1.01f;
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
                        case GLFW_KEY_R:
                            rotationUtil.switchEnabled();
                            break;
                        case GLFW_KEY_B:
                            skybox = !skybox;
                            break;
                        case GLFW_KEY_T:
                            if(textureMode == 6){
                                textureMode = 0;
                            }else {
                                textureMode += 3;
                            }
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
                        deltaTrans *= 1.01f;
                }
                else {
                    pz += trans;
                    camera.backward(trans);
                    if (deltaTrans < 0.001f)
                        deltaTrans = 0.001f;
                    else
                        deltaTrans *= 1.01f;
                }

            }
        };
    }

    @Override
    public void init() {
        glClearColor(0.0f, 0.0f, 0.0f, 1.0f); // background color

        glEnable(GL_DEPTH_TEST);
        glDisable(GL_CULL_FACE);
        glFrontFace(GL_CW);
        glPolygonMode(GL_FRONT, GL_FILL);
        glPolygonMode(GL_BACK, GL_FILL);
        glMatrixMode(GL_MODELVIEW);
        glLoadIdentity();

        // setup initial position
        pz = 2.6f;
        py = 0.7f;
        px = 0.4f;
        // setup initial light position coord
        mouseX = 820f;
        mouseY = 560f;

        skybox = true;
        perspective = true;

        for (int i = 0; i < 4; i++) {
            modelRotateMatrix[i*5] = 1;
        }

        textures = new ArrayList<>();

        // init objects
        OBJModel obj1 = new OBJModel();
        OBJModel obj2 = new OBJModel();
        OBJModel obj3 = new OBJModel();
        objList.addAll(List.of(obj1, obj2, obj3));

        obj1.loadObject("/data/obj/custom1.obj");
        obj2.loadObject("/data/obj/custom2.obj");
        obj3.loadObject("/data/obj/custom3.obj");


        textureCube = new OGLTexture2D[6];
        System.out.println("Loading textures...");
        try {
            textures.add(new OGLTexture2D("data/textures/govde_BaseColor.png"));
            textures.add(new OGLTexture2D("data/textures/bacik_BaseColor.png"));
            textures.add(new OGLTexture2D("data/textures/taban_BaseColor.png"));

            textures.add(new OGLTexture2D("data/textures/govde_BaseColor2.png"));
            textures.add(new OGLTexture2D("data/textures/bacik_BaseColor2.png"));
            textures.add(new OGLTexture2D("data/textures/taban_BaseColor.png"));

            textures.add(new OGLTexture2D("data/textures/govde_BaseColor3.png"));
            textures.add(new OGLTexture2D("data/textures/bacik_BaseColor3.png"));
            textures.add(new OGLTexture2D("data/textures/taban_BaseColor3.png"));

            textureCube[0] = new OGLTexture2D("data/textures/skybox1.jpeg");
            textureCube[1] = new OGLTexture2D("data/textures/skybox4.jpeg");
            textureCube[2] = new OGLTexture2D("data/textures/skybox0.jpeg");
            textureCube[3] = new OGLTexture2D("data/textures/skybox2.jpeg");
            textureCube[4] = new OGLTexture2D("data/textures/skybox5.jpeg");
            textureCube[5] = new OGLTexture2D("data/textures/skybox3.jpeg");
        } catch (IOException e) {
            e.printStackTrace();
        }

        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_REPEAT);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_REPEAT);
        glTexEnvi(GL_TEXTURE_ENV, GL_TEXTURE_ENV_MODE, GL_MODULATE);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);

        camera = new GLCamera();
        camera.setPosition(new Vec3D(10));
        camera.setFirstPerson(true);

        skyBox1();
        rotationUtil = new RotationUtil();
    }
    private void skyBox1() {
        glNewList(1, GL_COMPILE);
        glPushMatrix();
        glColor3d(0.5, 0.5, 0.5);
        int size = 250;
        glutWireCube(size);

        glEnable(GL_TEXTURE_2D);
        glTexEnvi(GL_TEXTURE_ENV, GL_TEXTURE_ENV_MODE, GL_TEXTURE);

        glScalef(0.3f, 0.3f, 1f);

        textureCube[1].bind(); //-x  (left)
        glBegin(GL_QUADS);
        glTexCoord2f(0.0f, 1.0f);
        glVertex3d(-size, -size, -size);
        glTexCoord2f(0.0f, 0.0f);
        glVertex3d(-size, size, -size);
        glTexCoord2f(1.0f, 0.0f);
        glVertex3d(-size, size, size);
        glTexCoord2f(1.0f, 1.0f);
        glVertex3d(-size, -size, size);
        glEnd();

        textureCube[0].bind();//+x  (right)
        glBegin(GL_QUADS);
        glTexCoord2f(1.0f, 1.0f);
        glVertex3d(size, -size, -size);
        glTexCoord2f(0.0f, 1.0f);
        glVertex3d(size, -size, size);
        glTexCoord2f(0.0f, 0.0f);
        glVertex3d(size, size, size);
        glTexCoord2f(1.0f, 0.0f);
        glVertex3d(size, size, -size);
        glEnd();

        textureCube[3].bind(); //-y bottom
        glBegin(GL_QUADS);
        glTexCoord2f(0.0f, 1.0f);
        glVertex3d(-size, -size, -size);
        glTexCoord2f(1.0f, 1.0f);
        glVertex3d(size, -size, -size);
        glTexCoord2f(1.0f, 0.0f);
        glVertex3d(size, -size, size);
        glTexCoord2f(0.0f, 0.0f);
        glVertex3d(-size, -size, size);
        glEnd();

        textureCube[2].bind(); //+y  top
        glBegin(GL_QUADS);
        glTexCoord2f(0.0f, 0.0f);
        glVertex3d(-size, size, -size);
        glTexCoord2f(1.0f, 0.0f);
        glVertex3d(size, size, -size);
        glTexCoord2f(1.0f, 1.0f);
        glVertex3d(size, size, size);
        glTexCoord2f(0.0f, 1.0f);
        glVertex3d(-size, size, size);
        glEnd();

        textureCube[5].bind(); //-z
        glBegin(GL_QUADS);
        glTexCoord2f(0.0f, 1.0f);
        glVertex3d(size, -size, -size);
        glTexCoord2f(1.0f, 1.0f);
        glVertex3d(-size, -size, -size);
        glTexCoord2f(1.0f, 0.0f);
        glVertex3d(-size, size, -size);
        glTexCoord2f(0.0f, 0.0f);
        glVertex3d(size, size, -size);
        glEnd();

        textureCube[4].bind(); //+z
        glBegin(GL_QUADS);
        glTexCoord2f(0.0f, 0.0f);
        glVertex3d(-size, size, size);
        glTexCoord2f(0.0f, 1.0f);
        glVertex3d(-size, -size, size);
        glTexCoord2f(1.0f, 1.0f);
        glVertex3d(size, -size, size);
        glTexCoord2f(1.0f, 0.0f);
        glVertex3d(size, size, size);
        glEnd();

        glDisable(GL_TEXTURE_2D);
        glPopMatrix();

        glEndList();
    }


    @Override
    public void display() {
        glViewport(0, 0, width, height);
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
        glPolygonMode(GL_FRONT_AND_BACK, GL_FILL);

        trans += deltaTrans;

        glMatrixMode(GL_PROJECTION);
        glLoadIdentity();
        if (perspective)
            gluPerspective(45, width / (float) height, 0.1f, 500.0f);
        else
            glOrtho(- width / (float) height,
                    width / (float) height,
                    -1, 1, 0.1f, 500.0f);

        GLCamera cameraSky = new GLCamera(camera);
        cameraSky.setPosition(new Vec3D());

        glMatrixMode(GL_MODELVIEW);
        glLoadIdentity();
        if(skybox){
            cameraSky.setMatrix();
            glCallList(1);
        }

        // rotate the sneakers
        rotationUtil.applyRotation(modelRotateMatrix);

        glGetFloatv(GL_MODELVIEW_MATRIX, modelMatrix);
        glLoadIdentity();

        // camera
        glRotatef(-zenit, 1.0f, 0, 0);
        glRotatef(azimut, 0, 1.0f, 0);
        glTranslated(-px, -py, -pz);

        if(!rotationUtil.isEnabled())
            glMultMatrixf(modelRotateMatrix);
        glMultMatrixf(modelMatrix);

        if(isWired)
            glPolygonMode(GL_FRONT_AND_BACK, GL_LINE);
        else
            glPolygonMode(GL_FRONT_AND_BACK, GL_FILL);


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
        for(int i = 0; i < objList.size(); i++){
            objList.get(i).bind();
            glEnable(GL_TEXTURE_2D);
            glEnableClientState(GL_TEXTURE_COORD_ARRAY);
            glEnableClientState(GL_NORMAL_ARRAY);

            textures.get(i + textureMode).bind();

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
        glPushMatrix();
        glLoadIdentity();
        float[] lightPosition;
        lightPosition = new float[]{ mouseX - width, height - mouseY, 25, 1.0f};

        glLightfv(GL_LIGHT0, GL_POSITION, lightPosition);

        if(flatShading)
            glShadeModel(GL_FLAT);
        else
            glShadeModel(GL_SMOOTH);
        glPopMatrix();
    }
}
