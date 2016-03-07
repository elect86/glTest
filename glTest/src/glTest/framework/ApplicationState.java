/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package glTest.framework;

import com.jogamp.newt.Display;
import com.jogamp.newt.NewtFactory;
import com.jogamp.newt.Screen;
import com.jogamp.newt.event.KeyEvent;
import com.jogamp.newt.event.KeyListener;
import com.jogamp.newt.opengl.GLWindow;
import com.jogamp.opengl.FPSCounter;
import static com.jogamp.opengl.GL.GL_BLEND;
import static com.jogamp.opengl.GL.GL_CULL_FACE;
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
import com.jogamp.opengl.GL4;
import com.jogamp.opengl.GLAutoDrawable;
import com.jogamp.opengl.GLCapabilities;
import com.jogamp.opengl.GLContext;
import com.jogamp.opengl.GLEventListener;
import com.jogamp.opengl.GLProfile;
import com.jogamp.opengl.util.Animator;
import com.jogamp.opengl.util.GLBuffers;
import glm.vec._2.i.Vec2i;
import java.util.ArrayList;
import glTest.problems.Problem;
import glTest.solutions.Solution;
import java.nio.IntBuffer;
import jogamp.opengl.FPSCounterImpl;

/**
 *
 * @author GBarbieri
 */
public class ApplicationState implements GLEventListener, KeyListener {

    private Vec2i position = new Vec2i(0, 0);
    private Vec2i resolution = new Vec2i(1024, 768);
    private ProblemFactory factory;
    private ArrayList<Problem> problems;
    private Solution[] solutions;
    private final int inactiveProblem = -1;
    private final int inactiveSolution = -1;
    private int activeProblem;
    private int activeSolution;
    public GLWindow glWindow;
    public Animator animator;
    public static boolean DEBUG = true;
    private IntBuffer vertexArrayObject = GLBuffers.newDirectIntBuffer(1);
    private String rootTitle = "gltest";
    private int seconds;

    public void setup() {

        Display display = NewtFactory.createDisplay(null);
        Screen screen = NewtFactory.createScreen(display, 0);
        GLProfile glProfile = GLProfile.get(GLProfile.GL4);
        GLCapabilities glCapabilities = new GLCapabilities(glProfile);
        glWindow = GLWindow.create(screen, glCapabilities);

        glWindow.setSize(1024, 768);
        glWindow.setPosition(50, 50);
        glWindow.setUndecorated(false);
        glWindow.setAlwaysOnTop(false);
        glWindow.setFullscreen(false);
        glWindow.setPointerVisible(true);
        glWindow.confinePointer(false);
        glWindow.setTitle(rootTitle);
        if (DEBUG) {
            glWindow.setContextCreationFlags(GLContext.CTX_OPTION_DEBUG);
        }

        glWindow.setVisible(true);

        if (DEBUG) {
            glWindow.getContext().addGLDebugListener(new GlDebugOutput());
        }

        glWindow.addGLEventListener(this);
        glWindow.addKeyListener(this);

        System.out.println("GL created successfully! Info follows.");

        animator = new Animator(glWindow);
        animator.setRunAsFastAsPossible(true);
        animator.setUpdateFPSFrames(10_000, System.out);
        animator.start();
    }

    @Override
    public void init(GLAutoDrawable drawable) {

        GL4 gl4 = drawable.getGL().getGL4();
        System.out.println("" + GLContext.getCurrent().getGLVersion());
        System.out.println("Vendor: " + gl4.glGetString(GL_VENDOR));
        System.out.println("Renderer: " + gl4.glGetString(GL_RENDERER));
        System.out.println("Version: " + gl4.glGetString(GL_VERSION));
        System.out.println("Shading Language Version: " + gl4.glGetString(GL_SHADING_LANGUAGE_VERSION));

        gl4.setSwapInterval(0);

        // Default GL State
        gl4.glCullFace(GL_FRONT);
        gl4.glEnable(GL_CULL_FACE);
        gl4.glDisable(GL_SCISSOR_TEST);
        gl4.glEnable(GL_DEPTH_TEST);
        gl4.glDepthMask(true);
        gl4.glDepthFunc(GL_LESS);
        gl4.glDisable(GL_BLEND);
        gl4.glColorMask(true, true, true, true);

        if (DEBUG) {
            gl4.glDebugMessageControl(GL_DONT_CARE, GL_DONT_CARE, GL_DONT_CARE, 0, null, false);
            gl4.glDebugMessageControl(GL_DONT_CARE, GL_DONT_CARE, GL_DEBUG_SEVERITY_HIGH, 0, null, true);
            gl4.glDebugMessageControl(GL_DONT_CARE, GL_DONT_CARE, GL_DEBUG_SEVERITY_MEDIUM, 0, null, true);
        }

        // Now that we have something valid, create our VAO and bind it. Ugh! So lame that this is required.
        gl4.glGenVertexArrays(1, vertexArrayObject);
        gl4.glBindVertexArray(vertexArrayObject.get(0));

        factory = new ProblemFactory(gl4, false);
        problems = factory.getProblems();
        assert (problems.size() > 0);

        setInitialProblemAndSolution("NullProblem", "NullSolution");
    }

    private void setInitialProblemAndSolution(String probName, String solnName) {

        for (int i = 0; i < problems.size(); i++) {
            if (problems.get(i).getName().equals(probName)) {
                activeProblem = i;
                break;
            }
        }

        solutions = factory.getSolutions(problems.get(activeProblem));
        for (int i = 0; i < solutions.length; i++) {
            if (solutions[i].getName().equals(solnName)) {
                activeSolution = i;
                break;
            }
        }
        onProblemOrSolutionSet();
    }

    @Override
    public void display(GLAutoDrawable drawable) {
//        System.out.println("display "+animator.getTotalFPSDuration());
        Problem activeProblem_ = getActiveProblem();

        if (activeProblem_ == null) {
            return;
        }

        GL4 gl4 = drawable.getGL().getGL4();

        activeProblem_.clear(gl4);

        // This is the main entry point shared by all tests. 
        activeProblem_.render(gl4);

        // Present the results.
        if (animator.getTotalFPSDuration() / 1_000 == seconds && !activeProblem_.getName().equals("NullProblem")) {
            String fpsLastS = String.valueOf(animator.getLastFPS());
            fpsLastS = fpsLastS.substring(0, fpsLastS.indexOf('.') + 2);
            String fpsTotalS = String.valueOf(animator.getTotalFPS());
            fpsTotalS = fpsTotalS.substring(0, fpsTotalS.indexOf('.') + 2);
            System.out.println(animator.getTotalFPSDuration() / 1_000 + " s: " + animator.getUpdateFPSFrames() + " f / "
                    + animator.getLastFPSPeriod() + " ms, " + fpsLastS + " fps, " + animator.getLastFPSPeriod()
                    / animator.getUpdateFPSFrames() + " ms/f; " + "total: " + animator.getTotalFPSFrames() + " f, "
                    + fpsTotalS + " fps, " + animator.getTotalFPSDuration() / animator.getTotalFPSFrames() + " ms/f");

            seconds++;
        }
    }

    @Override
    public void reshape(GLAutoDrawable drawable, int x, int y, int width, int height) {

        GL4 gl4 = drawable.getGL().getGL4();

        gl4.glViewport(0, 0, width, height);
    }

    @Override
    public void dispose(GLAutoDrawable drawable) {

        GL4 gl4 = drawable.getGL().getGL4();

        // Must cleanup before we call base class.
        gl4.glBindVertexArray(0);
        gl4.glDeleteVertexArrays(1, vertexArrayObject);

        System.exit(0);
    }

    private void onProblemOrSolutionSet() {

        String newTitle = rootTitle + " - " + getActiveProblem().getName();

        if (getActiveSolution() != null) {
            newTitle += " - " + solutions[activeSolution].getName();
        }
        glWindow.setTitle(newTitle);

        seconds = 1;
        animator.resetFPSCounter();
    }

    private Problem getActiveProblem() {
        return activeProblem != inactiveProblem ? problems.get(activeProblem) : null;
    }

    private Solution getActiveSolution() {
        return activeSolution != inactiveSolution ? solutions[activeSolution] : null;
    }

    @Override
    public void keyPressed(KeyEvent e) {
        switch (e.getKeyCode()) {
            case KeyEvent.VK_ESCAPE:
                animator.stop();
                break;
            case KeyEvent.VK_LEFT:
                animator.stop();
                break;
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {

    }
    
    private void changeProblem(int offset) {
        
    }
}
