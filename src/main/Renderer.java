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
 * @author PGRF FIM UHK
 * @version 3.1
 * @since 2020-01-20
 */
public class Renderer extends AbstractRenderer {
    private List<OGLTexture2D> textures;
    private GLCamera camera;
    private float trans, deltaTrans = 0;
    private float px, py, pz;
    private float zenit, azimut;
    private float[] modelMatrix = new float[16];
    private float[] modelRotateMatrix = new float[16];
    private boolean mouseButton1, mouseButton2 = false;
    private boolean isWired, flatShading, rotation = false;
    private boolean perspective = true;
    private float dx, dy, ox, oy;
    private List<OBJModel> objList = new ArrayList<>();
    private float mouseX, mouseY;
    private float angle, step = 0;
    private long oldmils, oldFPSmils;
    private OGLTexture2D[] textureCube;

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
                        case GLFW_KEY_R:
                            rotation = !rotation;
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
    public void init() throws IOException {
//        super.init();
        glClearColor(0.0f, 0.0f, 0.0f, 1.0f); // background color
//        textRenderer = new OGLTextRenderer(width, height);
        /** THROWS WEIRD ERROR
         * New shader program '1' created
         * VERT shader: Creating ... '2' OK,  Compiling '2'... Reading model file /data/obj/custom1.objfailed
         * ERROR: 0:1: '' :  version '330' is not supported
         * ERROR: 0:7: 'f' : syntax error: syntax error
         */

        glEnable(GL_DEPTH_TEST);
        glDisable(GL_CULL_FACE);
        glFrontFace(GL_CW);
        glPolygonMode(GL_FRONT, GL_FILL);
        glPolygonMode(GL_BACK, GL_FILL);
        glMatrixMode(GL_MODELVIEW);
        glLoadIdentity();

        // setup initial position
        pz = 2f;
        py = 1f;
        px = 0.4f;
        // setup initial light position coord
        mouseX = 820f;
        mouseY = 560f;

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

            textureCube[0] = new OGLTexture2D("data/textures/snow_positive_x.jpg");
            textureCube[1] = new OGLTexture2D("data/textures/snow_negative_x.jpg");
            textureCube[2] = new OGLTexture2D("data/textures/snow_positive_y.jpg");
            textureCube[3] = new OGLTexture2D("data/textures/snow_negative_y.jpg");
            textureCube[4] = new OGLTexture2D("data/textures/snow_positive_z.jpg");
            textureCube[5] = new OGLTexture2D("data/textures/snow_negative_z.jpg");
        } catch (IOException e) {
            e.printStackTrace();
        }

        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_REPEAT);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_REPEAT);
        glTexEnvi(GL_TEXTURE_ENV, GL_TEXTURE_ENV_MODE, GL_MODULATE);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);

        camera = new GLCamera();
//        camera.setPosition(new Vec3D(10));
//        camera.setFirstPerson(true);

        skyBox1();
    }
    private void skyBox1() {
        glNewList(1, GL_COMPILE);
        glPushMatrix();
        glColor3d(0.5, 0.5, 0.5);
        int size = 250;
        glutWireCube(size); //neni nutne, pouze pro znazorneni tvaru skyboxu

        glEnable(GL_TEXTURE_2D);
        glTexEnvi(GL_TEXTURE_ENV, GL_TEXTURE_ENV_MODE, GL_REPLACE);

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
            gluPerspective(90, width / (float) height, 0.1f, 500.0f);
        else
            glOrtho(-20 * width / (float) height,
                    20 * width / (float) height,
                    -20, 20, 0.1f, 500.0f);

        GLCamera cameraSky = new GLCamera(camera);
        cameraSky.setPosition(new Vec3D());

        glMatrixMode(GL_MODELVIEW);
        glLoadIdentity();
        cameraSky.setMatrix();
        glCallList(1);

        // rotate the sneakers
        if(rotation){
            long mils = System.currentTimeMillis();
            if ((mils - oldFPSmils) > 300) {
                oldFPSmils = mils;
            }

            float speed = 15; // angles per second
            step = speed * (mils - oldmils) / 1000.0f; // step for 1 render
            oldmils = mils;
            angle = (angle + step) % 360;
            glRotatef(angle, 0, 1, 0);

            glGetFloatv(GL_MODELVIEW_MATRIX, modelRotateMatrix);

        }else{
            oldmils = System.currentTimeMillis();
        }

        glGetFloatv(GL_MODELVIEW_MATRIX, modelMatrix);
        glLoadIdentity();

        // camera
        glRotatef(-zenit, 1.0f, 0, 0);
        glRotatef(azimut, 0, 1.0f, 0);
        glTranslated(-px, -py, -pz);

        if(!rotation)
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

        // text rendering for info
    }
    public void drawScene(){
        glEnableClientState(GL_VERTEX_ARRAY);
        for(int i = 0; i < objList.size(); i++){
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
        glPushMatrix();
        glLoadIdentity();
        float[] light_position;

        // bod v prostoru
        light_position = new float[]{ mouseX - width / 2f, height / 2f - mouseY, 25, 1.0f};

        glLightfv(GL_LIGHT0, GL_POSITION, light_position);

        if(flatShading)
            glShadeModel(GL_FLAT);
        else
            glShadeModel(GL_SMOOTH);
        glPopMatrix();
    }
}
