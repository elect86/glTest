/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package micro.pick;

import com.jogamp.newt.Display;
import com.jogamp.newt.NewtFactory;
import com.jogamp.newt.Screen;
import com.jogamp.newt.event.KeyEvent;
import com.jogamp.newt.event.KeyListener;
import com.jogamp.newt.event.MouseEvent;
import com.jogamp.newt.event.MouseListener;
import com.jogamp.newt.opengl.GLWindow;
import static com.jogamp.opengl.GL.GL_CLAMP_TO_EDGE;
import static com.jogamp.opengl.GL.GL_COLOR_ATTACHMENT0;
import static com.jogamp.opengl.GL.GL_DEPTH_ATTACHMENT;
import static com.jogamp.opengl.GL.GL_DEPTH_COMPONENT32;
import static com.jogamp.opengl.GL.GL_FLOAT;
import static com.jogamp.opengl.GL.GL_FRAMEBUFFER;
import static com.jogamp.opengl.GL.GL_FRAMEBUFFER_COMPLETE;
import static com.jogamp.opengl.GL.GL_NEAREST;
import static com.jogamp.opengl.GL.GL_TEXTURE0;
import static com.jogamp.opengl.GL.GL_TEXTURE_2D;
import static com.jogamp.opengl.GL.GL_TEXTURE_MAG_FILTER;
import static com.jogamp.opengl.GL.GL_TEXTURE_MIN_FILTER;
import static com.jogamp.opengl.GL.GL_TEXTURE_WRAP_S;
import static com.jogamp.opengl.GL.GL_TEXTURE_WRAP_T;
import static com.jogamp.opengl.GL.GL_TRIANGLES;
import static com.jogamp.opengl.GL.GL_UNPACK_ALIGNMENT;
import static com.jogamp.opengl.GL2ES2.GL_COLOR_ATTACHMENT1;
import static com.jogamp.opengl.GL2ES2.GL_DEPTH_COMPONENT;
import static com.jogamp.opengl.GL2ES2.GL_INT;
import static com.jogamp.opengl.GL2ES3.GL_COLOR;
import static com.jogamp.opengl.GL2ES3.GL_R32I;
import static com.jogamp.opengl.GL2ES3.GL_RED_INTEGER;
import static com.jogamp.opengl.GL2ES3.GL_TEXTURE_BASE_LEVEL;
import static com.jogamp.opengl.GL2ES3.GL_TEXTURE_MAX_LEVEL;
import com.jogamp.opengl.GL3;
import com.jogamp.opengl.GLAutoDrawable;
import com.jogamp.opengl.GLCapabilities;
import com.jogamp.opengl.GLEventListener;
import com.jogamp.opengl.GLProfile;
import com.jogamp.opengl.util.Animator;
import com.jogamp.opengl.util.GLBuffers;
import glm.vec._2.i.Vec2i;
import glsl.Program;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;

/**
 *
 * @author GBarbieri
 */
public class Pick implements GLEventListener, MouseListener, KeyListener {

    static GLWindow glWindow;
    static Animator animator;
    static Vec2i size = new Vec2i(1024, 768);

    public static void main(String[] args) {

        Display display = NewtFactory.createDisplay(null);
        Screen screen = NewtFactory.createScreen(display, 0);
        GLProfile glProfile = GLProfile.get(GLProfile.GL3);
        GLCapabilities glCapabilities = new GLCapabilities(glProfile);
        glWindow = GLWindow.create(screen, glCapabilities);

        glWindow.setUndecorated(false);
        glWindow.setAlwaysOnTop(false);
        glWindow.setFullscreen(false);
        glWindow.setPointerVisible(true);
        glWindow.confinePointer(false);
        glWindow.setSize(size.x, size.y);
        glWindow.setResizable(false);
        glWindow.setVisible(true);

        Pick pick = new Pick();

        glWindow.addGLEventListener(pick);
        glWindow.addMouseListener(pick);
        glWindow.addKeyListener(pick);

        animator = new Animator();
        animator.add(glWindow);
        animator.setRunAsFastAsPossible(false);
        animator.setExclusiveContext(true);
        animator.start();
    }

    Program program;

    interface Texture {

        public static final int DEPTH = 0;
        public static final int ID = 1;
        public static final int MAX = 2;
    }

    IntBuffer textureName = GLBuffers.newDirectIntBuffer(Texture.MAX),
            framebufferName = GLBuffers.newDirectIntBuffer(1), clearId = GLBuffers.newDirectIntBuffer(1),
            vertexArrayName = GLBuffers.newDirectIntBuffer(1);
    FloatBuffer clearColor = GLBuffers.newDirectFloatBuffer(4);
    boolean read = false;
    MouseEvent mouseClicked;

    @Override
    public final void init(GLAutoDrawable drawable) {

        GL3 gl3 = drawable.getGL().getGL3();

        program = new Program(gl3, "/micro/pick", "pick");

        initTargets(gl3);

        
        gl3.glGenVertexArrays(1, vertexArrayName);
    }

    private void initTargets(GL3 gl3) {

        gl3.glGenTextures(Texture.MAX, textureName);

        // depth
        gl3.glActiveTexture(GL_TEXTURE0);
        gl3.glBindTexture(GL_TEXTURE_2D, textureName.get(Texture.DEPTH));

        // TODO hardcoded -> programming
        gl3.glTexImage2D(GL_TEXTURE_2D, 0, GL_DEPTH_COMPONENT32, size.x, size.y, 0, GL_DEPTH_COMPONENT, GL_FLOAT, null);

        // Check if substitute with sampler
        gl3.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_BASE_LEVEL, 0);
        gl3.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAX_LEVEL, 0);

        gl3.glBindTexture(GL_TEXTURE_2D, textureName.get(Texture.ID));
        gl3.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_BASE_LEVEL, 0);
        gl3.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAX_LEVEL, 0);
        gl3.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
        gl3.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);
        gl3.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
        gl3.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);
        gl3.glTexImage2D(GL_TEXTURE_2D, 0, GL_R32I, size.x, size.y, 0, GL_RED_INTEGER, GL_INT, null);

        gl3.glGenFramebuffers(1, framebufferName);

        gl3.glBindFramebuffer(GL_FRAMEBUFFER, framebufferName.get(0));
        gl3.glFramebufferTexture(GL_FRAMEBUFFER, GL_DEPTH_ATTACHMENT, textureName.get(Texture.DEPTH), 0);
        gl3.glFramebufferTexture(GL_FRAMEBUFFER, GL_COLOR_ATTACHMENT1, textureName.get(Texture.ID), 0);

        if (gl3.glCheckFramebufferStatus(GL_FRAMEBUFFER) != GL_FRAMEBUFFER_COMPLETE) {
            System.err.println("incomplete");
        }
    }

    @Override
    public final void display(GLAutoDrawable drawable) {

        GL3 gl3 = drawable.getGL().getGL3();

        gl3.glBindFramebuffer(GL_FRAMEBUFFER, framebufferName.get(0));
        gl3.glDrawBuffer(GL_COLOR_ATTACHMENT1);
        gl3.glClearBufferiv(GL_COLOR, 0, clearId.put(0, 10)); // we care clearing only red        
//        gl3.glBindFramebuffer(GL_FRAMEBUFFER, 0);
//        gl3.glClearBufferfv(GL_COLOR, 0, clearColor.put(0, 1).put(1, 0.5f).put(2, 0).put(3, 1)); 
//        gl3.glClearBufferfv(GL_DEPTH, 0, clearColor.put(0, 1)); 

        gl3.glUseProgram(program.name);

        gl3.glBindVertexArray(vertexArrayName.get(0));

        gl3.glDrawArrays(GL_TRIANGLES, 0, 3);

        if (read) {

            read = false;

            Vec2i point = new Vec2i(mouseClicked.getX(), size.y - mouseClicked.getY() - 1);

            gl3.glFlush();
            gl3.glFinish();
            gl3.glPixelStorei(GL_UNPACK_ALIGNMENT, 1);

            gl3.glReadBuffer(GL_COLOR_ATTACHMENT1);

            gl3.glReadPixels(point.x, point.y, 1, 1, GL_RED_INTEGER, GL_INT, clearId);

            System.out.println("id: " + clearId.get(0));
        }
    }

    @Override
    public final void reshape(GLAutoDrawable drawable, int x, int y, int width, int height) {

        GL3 gl3 = drawable.getGL().getGL3();
        gl3.glViewport(x, y, width, height);
    }

    @Override
    public final void dispose(GLAutoDrawable drawable) {
        System.exit(0);
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        mouseClicked = e;
        read = true;
    }

    @Override
    public void mouseEntered(MouseEvent e) {
    }

    @Override
    public void mouseExited(MouseEvent e) {
    }

    @Override
    public void mousePressed(MouseEvent e) {
    }

    @Override
    public void mouseReleased(MouseEvent e) {
    }

    @Override
    public void mouseMoved(MouseEvent e) {
    }

    @Override
    public void mouseDragged(MouseEvent e) {
    }

    @Override
    public void mouseWheelMoved(MouseEvent e) {
    }

    @Override
    public void keyPressed(KeyEvent e) {
    }

    @Override
    public void keyReleased(KeyEvent e) {

        switch (e.getKeyCode()) {
            case KeyEvent.VK_ESCAPE:
                animator.stop();
                glWindow.destroy();
                break;
        }
    }
}
