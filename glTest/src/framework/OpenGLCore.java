/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package framework;

import com.jogamp.opengl.GL3;
import com.jogamp.opengl.GLAutoDrawable;
import com.jogamp.opengl.util.GLBuffers;
import java.nio.IntBuffer;

/**
 *
 * @author GBarbieri
 */
public class OpenGLCore extends OpenGLGeneric {
    
    private IntBuffer vertexArrayObject = GLBuffers.newDirectIntBuffer(1);

    public OpenGLCore(String title, int x, int y, int width, int height) {
        super(title, x, y, width, height);
    }

    @Override
    public void init(GLAutoDrawable drawable) {

        super.init(drawable);
        
        GL3 gl3 = drawable.getGL().getGL3();
        
        // Now that we have something valid, create our VAO and bind it. Ugh! So lame that this is required.
        gl3.glGenVertexArrays(1, vertexArrayObject);
        gl3.glBindVertexArray(vertexArrayObject.get(0));
    }
    
    
}
