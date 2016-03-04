/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package framework;

import static com.jogamp.opengl.GL.GL_RENDERER;
import static com.jogamp.opengl.GL.GL_VENDOR;
import static com.jogamp.opengl.GL.GL_VERSION;
import static com.jogamp.opengl.GL2ES2.GL_SHADING_LANGUAGE_VERSION;
import com.jogamp.opengl.GL3;
import com.jogamp.opengl.GLAutoDrawable;

/**
 *
 * @author GBarbieri
 */
public class OpenGLGeneric extends OpenGLBase {

    public OpenGLGeneric(String title, int x, int y, int width, int height) {
        super(title, x, y, width, height);
    }

    @Override
    protected int getApiType() {
        return GLApi.OpenGLGeneric;
    }

    @Override
    public void init(GLAutoDrawable drawable) {
        
        super.init(drawable);
        
        GL3 gl3 = drawable.getGL().getGL3bc();
        
        System.out.println("Vendor: "+gl3.glGetString(GL_VENDOR));
        System.out.println("Renderer: "+gl3.glGetString(GL_RENDERER));
        System.out.println("Version: "+gl3.glGetString(GL_VERSION));
        System.out.println("Shading Language Version: "+gl3.glGetString(GL_SHADING_LANGUAGE_VERSION));
    }

    @Override
    public void display(GLAutoDrawable drawable) {

    }

    @Override
    public void reshape(GLAutoDrawable drawable, int x, int y, int width, int height) {

    }

    @Override
    public void dispose(GLAutoDrawable drawable) {

    }
}
