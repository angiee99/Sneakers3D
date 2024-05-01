package main;

import static org.lwjgl.opengl.GL11.*;

public class RotationUtil {
    private boolean isEnabled = false;
    private long oldmils, oldFPSmils;
    private float angle, step = 0;

    public void applyRotation(float[] modelRotateMatrix){
        if(isEnabled){
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
    }

    public void switchEnabled(){
        isEnabled = !isEnabled;
    }

    public boolean isEnabled() {
        return isEnabled;
    }

    public void setEnabled(boolean enabled) {
        isEnabled = enabled;
    }
}
