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

/**
 *
 * @author GBarbieri
 */
public class ApplicationState implements GLEventListener, KeyListener {

    public static final Vec2i RESOLUTION = new Vec2i(1024, 768);
    private ProblemFactory factory;
    private ArrayList<Problem> problems;
    private Solution[] solutions;
    private Problem problem;
    private Solution solution;
    public GLWindow glWindow;
    public static Animator animator;
    public static boolean DEBUG = true;
    private IntBuffer vertexArrayObject = GLBuffers.newDirectIntBuffer(1);
    private final String rootTitle = "gltest";
    private int offsetProblem = 0;
    private int offsetSolution = 0;

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
        animator.setUpdateFPSFrames(FPSCounter.DEFAULT_FRAMES_PER_INTERVAL, System.out);
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

        factory = new ProblemFactory();
        problems = factory.getProblems();
        assert (problems.size() > 0);

        setInitialProblemAndSolution(gl4, "NullProblem", "NullSolution");
    }

    private void setInitialProblemAndSolution(GL4 gl4, String probName, String solnName) {

        for (int i = 0; i < problems.size(); i++) {
            if (problems.get(i).getName().equals(probName)) {
                problem = problems.get(i);
                break;
            }
        }

        solutions = factory.getSolutions(problem);
        for (Solution sol : solutions) {
            if (sol.getName().equals(solnName)) {
                solution = sol;
                break;
            }
        }

        initProblem(gl4);

        initSolution(gl4, 0);

        onProblemOrSolutionSet();
    }

    @Override
    public void display(GLAutoDrawable drawable) {

        GL4 gl4 = drawable.getGL().getGL4();

        if (offsetProblem != 0) {
            changeProblem(gl4);
        }
        if (offsetSolution != 0) {
            changeSolution(gl4);
        }

        if (problem == null) {
            return;
        }

        // This is the main entry point shared by all tests. 
        problem.render(gl4);

        // Present the results.
        // included in the animator
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

    @Override
    public void keyPressed(KeyEvent e) {
        switch (e.getKeyCode()) {
            case KeyEvent.VK_ESCAPE:
                animator.stop();
                break;
            case KeyEvent.VK_LEFT:
                offsetProblem = -1;
                break;
            case KeyEvent.VK_RIGHT:
                offsetProblem = 1;
                break;
            case KeyEvent.VK_UP:
                offsetSolution = -1;
                break;
            case KeyEvent.VK_DOWN:
                offsetSolution = 1;
                break;
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {

    }

    private void changeProblem(GL4 gl4) {

        shutdownSolution(gl4);

        shutdownProblem(gl4);

        int problemCount = problems.size();
        int problemId = problems.indexOf(problem);
        problemId = (problemId + problemCount + offsetProblem) % problemCount;

        problem = problems.get(problemId);

        initProblem(gl4);

        solutions = factory.getSolutions(problem);

        solution = solutions[problem.getSolutionId()];

        initSolution(gl4, problem.getSolutionId());

        offsetProblem = 0;

        onProblemOrSolutionSet();
    }

    private void changeSolution(GL4 gl4) {

        shutdownSolution(gl4);

        int solutionCount = solutions.length;
        if (solutionCount == 0) {
            return;
        }

        int solutionId = problem.getSolutionId();
        solutionId = (solutionId + solutionCount + offsetSolution) % solutionCount;
        
        solution = solutions[solutionId];

        initSolution(gl4, solutionId);

        offsetSolution = 0;

        onProblemOrSolutionSet();
    }

    private void initSolution(GL4 gl4, int solutionId) {

        System.out.print("Solution " + solution.getName() + " init... ");
        System.out.println(solution.init(gl4) ? "Ok" : "Fail");

        problem.setSolution(gl4, solution);
        problem.setSolutionId(solutionId);
    }

    private void shutdownSolution(GL4 gl4) {

        System.out.print("Solution " + solution.getName() + " shutdown... ");
        System.out.println(solution.shutdown(gl4) ? "Ok" : "Fail");

        problem.setSolution(gl4, null);
    }

    private void initProblem(GL4 gl4) {

        System.out.print("Problem " + problem.getName() + " - init... ");
        System.out.println(problem.init(gl4) ? "Ok" : "Fail");
    }

    private void shutdownProblem(GL4 gl4) {

        System.out.print("Problem " + problem.getName() + " shutdown... ");
        System.out.println(problem.shutdown(gl4) ? "Ok" : "Fail");
    }

    private void onProblemOrSolutionSet() {

        System.gc();
        
        String newTitle = rootTitle + " - " + problem.getName();

        if (solution != null) {
            newTitle += " - " + solution.getName();
        }
        glWindow.setTitle(newTitle);
        
        System.gc();

        animator.resetFPSCounter();
    }
}
