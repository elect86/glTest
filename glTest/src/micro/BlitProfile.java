/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package micro;

import com.jogamp.newt.Display;
import com.jogamp.newt.NewtFactory;
import com.jogamp.newt.Screen;
import com.jogamp.newt.event.KeyEvent;
import com.jogamp.newt.event.KeyListener;
import com.jogamp.newt.opengl.GLWindow;
import com.jogamp.opengl.GL;
import static com.jogamp.opengl.GL.GL_ELEMENT_ARRAY_BUFFER;
import static com.jogamp.opengl.GL.GL_STATIC_DRAW;
import static com.jogamp.opengl.GL.GL_TRIANGLES;
import static com.jogamp.opengl.GL.GL_UNSIGNED_SHORT;
import static com.jogamp.opengl.GL2ES2.GL_QUERY_COUNTER_BITS;
import static com.jogamp.opengl.GL2ES2.GL_TIME_ELAPSED;
import static com.jogamp.opengl.GL3.*;
import com.jogamp.opengl.GL3;
import com.jogamp.opengl.GLAutoDrawable;
import com.jogamp.opengl.GLCapabilities;
import com.jogamp.opengl.GLEventListener;
import com.jogamp.opengl.GLProfile;
import com.jogamp.opengl.math.FloatUtil;
import com.jogamp.opengl.util.Animator;
import com.jogamp.opengl.util.GLBuffers;
import com.jogamp.opengl.util.glsl.ShaderCode;
import com.jogamp.opengl.util.glsl.ShaderProgram;
import glTest.framework.BufferUtils;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.nio.ShortBuffer;

/**
 *
 * @author gbarbieri
 */
public class BlitProfile implements GLEventListener, KeyListener {

    public static GLWindow glWindow;
    public static Animator animator;

    public static void main(String[] args) {

        Display display = NewtFactory.createDisplay(null);
        Screen screen = NewtFactory.createScreen(display, 0);
        GLProfile glProfile = GLProfile.get(GLProfile.GL3);
        GLCapabilities glCapabilities = new GLCapabilities(glProfile);
        glWindow = GLWindow.create(screen, glCapabilities);

        glWindow.setSize(1024, 768);
        glWindow.setPosition(50, 50);
        glWindow.setUndecorated(false);
        glWindow.setAlwaysOnTop(false);
        glWindow.setFullscreen(false);
        glWindow.setPointerVisible(true);
        glWindow.confinePointer(false);

        glWindow.setVisible(true);

        BlitProfile blitProfile = new BlitProfile();
        glWindow.addGLEventListener(blitProfile);
        glWindow.addKeyListener(blitProfile);

        animator = new Animator(glWindow);
        animator.start();
    }

    private IntBuffer textureName = GLBuffers.newDirectIntBuffer(2), queryName = GLBuffers.newDirectIntBuffer(1),
            framebufferName = GLBuffers.newDirectIntBuffer(2);

    @Override
    public void init(GLAutoDrawable drawable) {

        GL3 gl3 = drawable.getGL().getGL3();

        IntBuffer buffer = GLBuffers.newDirectIntBuffer(1);

        gl3.glGenQueries(1, queryName);

        gl3.glGetQueryiv(GL_TIME_ELAPSED, GL_QUERY_COUNTER_BITS, buffer);
        System.out.println("GL_QUERY_COUNTER_BITS: " + buffer.get(0));

        gl3.glGenTextures(2, textureName);
        gl3.glGenFramebuffers(2, framebufferName);

        for (int i = 0; i < 2; i++) {

            gl3.glBindTexture(GL_TEXTURE_2D, textureName.get(i));
            gl3.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_BASE_LEVEL, 0);
            gl3.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAX_LEVEL, 0);
            gl3.glTexImage2D(GL_TEXTURE_2D, 0, GL_DEPTH24_STENCIL8, glWindow.getWidth(), glWindow.getHeight(), 0,
                    GL_DEPTH_COMPONENT, GL_FLOAT, null);

            gl3.glBindFramebuffer(GL_FRAMEBUFFER, framebufferName.get(i));
            gl3.glFramebufferTexture(GL_FRAMEBUFFER, GL_DEPTH_STENCIL_ATTACHMENT, textureName.get(i), 0);

            boolean complete = gl3.glCheckFramebufferStatus(GL_FRAMEBUFFER) == GL_FRAMEBUFFER_COMPLETE;
            System.out.println("Framebuffer status: " + (complete ? "complete" : "incomplete"));
        }

        checkError(gl3, "a");

        gl3.glBeginQuery(GL_TIME_ELAPSED, queryName.get(0));

        gl3.glBindFramebuffer(GL_READ_FRAMEBUFFER, framebufferName.get(0));
        gl3.glBindFramebuffer(GL_DRAW_FRAMEBUFFER, framebufferName.get(1));

        gl3.glBlitFramebuffer(
                0, 0, glWindow.getWidth(), glWindow.getHeight(),
                0, 0, glWindow.getWidth(), glWindow.getHeight(),
                GL_DEPTH_STENCIL_ATTACHMENT, GL_LINEAR);

        gl3.glEndQuery(GL_TIME_ELAPSED);

        gl3.glGetQueryObjectuiv(queryName.get(0), GL_QUERY_RESULT, buffer);
        System.out.println("Time: " + (buffer.get(0) / 1000.f / 1000.f) + " ms");

        BufferUtils.destroyDirectBuffer(buffer);
    }

    @Override
    public void dispose(GLAutoDrawable drawable) {
        System.exit(0);
    }

    @Override
    public void display(GLAutoDrawable drawable) {
        animator.stop();
    }

    protected void checkError(GL gl, String location) {

        int error = gl.glGetError();
        if (error != GL_NO_ERROR) {
            String errorString;
            switch (error) {
                case GL_INVALID_ENUM:
                    errorString = "GL_INVALID_ENUM";
                    break;
                case GL_INVALID_VALUE:
                    errorString = "GL_INVALID_VALUE";
                    break;
                case GL_INVALID_OPERATION:
                    errorString = "GL_INVALID_OPERATION";
                    break;
                case GL_INVALID_FRAMEBUFFER_OPERATION:
                    errorString = "GL_INVALID_FRAMEBUFFER_OPERATION";
                    break;
                case GL_OUT_OF_MEMORY:
                    errorString = "GL_OUT_OF_MEMORY";
                    break;
                default:
                    errorString = "UNKNOWN";
                    break;
            }
            System.out.println("OpenGL Error(" + errorString + "): " + location);
            throw new Error();
        }
    }

    @Override
    public void reshape(GLAutoDrawable drawable, int x, int y, int width, int height) {
    }

    @Override
    public void keyPressed(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
            BlitProfile.animator.stop();
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {

    }
}
