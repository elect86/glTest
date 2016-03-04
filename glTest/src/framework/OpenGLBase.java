/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package framework;

import com.jogamp.newt.Display;
import com.jogamp.newt.NewtFactory;
import com.jogamp.newt.Screen;
import com.jogamp.newt.opengl.GLWindow;
import static com.jogamp.opengl.GL.GL_BLEND;
import static com.jogamp.opengl.GL.GL_COLOR_BUFFER_BIT;
import static com.jogamp.opengl.GL.GL_CULL_FACE;
import static com.jogamp.opengl.GL.GL_DEPTH_BUFFER_BIT;
import static com.jogamp.opengl.GL.GL_DEPTH_TEST;
import static com.jogamp.opengl.GL.GL_DONT_CARE;
import static com.jogamp.opengl.GL.GL_FRONT;
import static com.jogamp.opengl.GL.GL_LESS;
import static com.jogamp.opengl.GL.GL_RENDERER;
import static com.jogamp.opengl.GL.GL_SCISSOR_TEST;
import static com.jogamp.opengl.GL.GL_VENDOR;
import static com.jogamp.opengl.GL.GL_VERSION;
import static com.jogamp.opengl.GL2ES2.GL_DEBUG_SEVERITY_HIGH;
import static com.jogamp.opengl.GL2ES2.GL_DEBUG_SEVERITY_MEDIUM;
import static com.jogamp.opengl.GL2ES2.GL_SHADING_LANGUAGE_VERSION;
import com.jogamp.opengl.GL3;
import com.jogamp.opengl.GLAutoDrawable;
import com.jogamp.opengl.GLCapabilities;
import com.jogamp.opengl.GLContext;
import com.jogamp.opengl.GLEventListener;
import com.jogamp.opengl.GLProfile;
import com.jogamp.opengl.util.Animator;
import glm.vec._4.Vec4;

/**
 *
 * @author GBarbieri
 */
public abstract class OpenGLBase implements GLEventListener {

    protected String titleRoot;
    protected int width;
    protected int height;
    public static GLWindow glWindow;
    public static Animator animator;
    public static boolean DEBUG = true;

    protected OpenGLBase(String title, int x, int y, int width, int height) {
        this(title, x, y, width, height, false);
    }

    public OpenGLBase(String title, int x, int y, int width, int height, boolean core) {

        titleRoot = title;
        this.width = width;
        this.height = height;

        Display display = NewtFactory.createDisplay(null);
        Screen screen = NewtFactory.createScreen(display, 0);
        GLProfile glProfile = GLProfile.get(core ? GLProfile.GL3 : GLProfile.GL3bc);
        GLCapabilities glCapabilities = new GLCapabilities(glProfile);
        glWindow = GLWindow.create(screen, glCapabilities);

        glWindow.setSize(1024, 768);
        glWindow.setPosition(50, 50);
        glWindow.setUndecorated(false);
        glWindow.setAlwaysOnTop(false);
        glWindow.setFullscreen(false);
        glWindow.setPointerVisible(true);
        glWindow.confinePointer(false);
        glWindow.setTitle("Hello Triangle");
        glWindow.setContextCreationFlags(GLContext.CTX_OPTION_DEBUG);

        glWindow.setVisible(true);

        glWindow.addGLEventListener(this);
//        glWindow.addKeyListener(glTest);

        System.out.println("GL created successfully! Info follows.");

        animator = new Animator(glWindow);
        animator.start();
    }

    protected abstract int getApiType();

    @Override
    public void init(GLAutoDrawable drawable) {

        GL3 gl3 = this instanceof OpenGLCore ? drawable.getGL().getGL3() : drawable.getGL().getGL3bc();

        System.out.println("Vendor: " + gl3.glGetString(GL_VENDOR));
        System.out.println("Renderer: " + gl3.glGetString(GL_RENDERER));
        System.out.println("Version: " + gl3.glGetString(GL_VERSION));
        System.out.println("Shading Language Version: " + gl3.glGetString(GL_SHADING_LANGUAGE_VERSION));

        gl3.setSwapInterval(0);

        // Default GL State
        gl3.glCullFace(GL_FRONT);
        gl3.glEnable(GL_CULL_FACE);
        gl3.glDisable(GL_SCISSOR_TEST);
        gl3.glEnable(GL_DEPTH_TEST);
        gl3.glDepthMask(true);
        gl3.glDepthFunc(GL_LESS);
        gl3.glDisable(GL_BLEND);
        gl3.glColorMask(true, true, true, true);

        if (DEBUG) {
            gl3.glDebugMessageControl(GL_DONT_CARE, GL_DONT_CARE, GL_DONT_CARE, 0, null, false);
            gl3.glDebugMessageControl(GL_DONT_CARE, GL_DONT_CARE, GL_DEBUG_SEVERITY_HIGH, 0, null, true);
            gl3.glDebugMessageControl(GL_DONT_CARE, GL_DONT_CARE, GL_DEBUG_SEVERITY_MEDIUM, 0, null, true);
            glWindow.getContext().addGLDebugListener(new GlDebugOutput());
        }
    }

    @Override
    public void display(GLAutoDrawable drawable) {

    }

    @Override
    public void reshape(GLAutoDrawable drawable, int x, int y, int width, int height) {

        GL3 gl3 = this instanceof OpenGLCore ? drawable.getGL().getGL3() : drawable.getGL().getGL3bc();
        
        gl3.glViewport(0, 0, width, height);
    }

    @Override
    public void dispose(GLAutoDrawable drawable) {

    }
}
