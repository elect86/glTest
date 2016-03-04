/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jogl;

import com.jogamp.newt.event.KeyEvent;
import com.jogamp.newt.event.KeyListener;
import com.jogamp.newt.opengl.GLWindow;
import com.jogamp.opengl.GL4;
import com.jogamp.opengl.GLAutoDrawable;
import com.jogamp.opengl.GLCapabilities;
import com.jogamp.opengl.GLEventListener;
import com.jogamp.opengl.GLProfile;
import com.jogamp.opengl.util.Animator;
import com.jogamp.opengl.util.GLBuffers;
import common.enums.Objects;
import common.enums.Program;
import common.enums.StateChange;
import static common.enums.StateChange.RenderTarget;
import static common.enums.StateChange.TextureBinding;
import static common.enums.StateChange.UboBindings;
import static common.enums.StateChange.UniformUpdates;
import static common.enums.StateChange.VertexBindings;
import static common.enums.StateChange.VertexFormat;
import common.enums.Texture;
import glsl.GLSLProgramObject;
import java.nio.FloatBuffer;
import jglm.Mat4;
import jogl.glsl.ProgramBase;
import jogl.glsl.ProgramTexture;
import jogl.glsl.ProgramUbo;
import jogl.glsl.ProgramUniform;

/**
 *
 * @author elect
 */
public class GlCallsOverhead implements GLEventListener, KeyListener {

    private static Animator animator;

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {

        GLProfile glProfile = GLProfile.get(GLProfile.GL4);
        GLCapabilities caps = new GLCapabilities(glProfile);

        caps.setBackgroundOpaque(false);
        GLWindow glWindow = GLWindow.create(caps);

        glWindow.setTitle("Micro gl calls overhead benchmark");
        glWindow.setSize(1024, 768);
        glWindow.setUndecorated(false);
        glWindow.setPointerVisible(true);
        glWindow.setVisible(true);

        GlCallsOverhead glCallsOverhead = new GlCallsOverhead(glWindow);
        glWindow.addGLEventListener(glCallsOverhead);
        glWindow.addKeyListener(glCallsOverhead);
        animator = new Animator();
        animator.add(glWindow);
        animator.setRunAsFastAsPossible(true);
        animator.setExclusiveContext(true);
        animator.setUpdateFPSFrames(10, System.out);
        animator.start();
    }

    private GLSLProgramObject[] programs;
    private int[] fbos;
    private int[] textures;
    private final GLWindow glWindow;
    private int repeat;
    private int[] objects;
    private StateChange stateChange;
    private boolean update;
    private FloatBuffer modelToClip;

    public GlCallsOverhead(GLWindow glWindow) {
        this.glWindow = glWindow;
    }

    @Override
    public void init(GLAutoDrawable glad) {

        GL4 gl4 = glad.getGL().getGL4();

        initPrograms(gl4);

        initTextures(gl4);

        initTargets(gl4);

        repeat = 10_000;

        objects = new int[Objects.size.ordinal()];
        initVbo(gl4);
        initVao(gl4);

        initUbos(gl4);

        stateChange = StateChange.RenderTarget;

        update = true;

        glad.setAutoSwapBufferMode(false);

        printHelp();
        printStateChange();
    }

    private void initPrograms(GL4 gl4) {

        String shaderFilepath = "/common/shaders/";

        modelToClip = GLBuffers.newDirectFloatBuffer(new Mat4(1f).toFloatArray());

        programs = new GLSLProgramObject[Program.size.ordinal()];
        for (int i = 0; i < 2; i++) {
            programs[i] = new ProgramBase(gl4, shaderFilepath, "VS.glsl", "FS.glsl");
            gl4.glUseProgram(programs[i].getProgramId());
            {
                gl4.glUniformMatrix4fv(((ProgramBase) programs[i]).getModelToClipUL(), 1, false, modelToClip);
            }
            gl4.glUseProgram(0);
        }

        programs[Program.texture.ordinal()] = new ProgramTexture(gl4, shaderFilepath, "VS.glsl", "FStexture.glsl");
        gl4.glUseProgram(programs[Program.texture.ordinal()].getProgramId());
        {
            gl4.glUniformMatrix4fv(((ProgramTexture) programs[Program.texture.ordinal()]).getModelToClipUL(), 1, false, 
                    modelToClip);
        }
        gl4.glUseProgram(0);

        programs[Program.vertexFormat.ordinal()] = new ProgramBase(gl4, shaderFilepath,
                "VSvertexFormat.glsl", "FS.glsl");
        gl4.glUseProgram(programs[Program.vertexFormat.ordinal()].getProgramId());
        {
            gl4.glUniformMatrix4fv(((ProgramBase) programs[Program.vertexFormat.ordinal()]).getModelToClipUL(),
                    1, false, modelToClip);
        }
        gl4.glUseProgram(0);

        programs[Program.ubo.ordinal()] = new ProgramUbo(gl4, shaderFilepath, "VSubo.glsl", "FS.glsl");

        programs[Program.uniform.ordinal()] = new ProgramUniform(gl4, shaderFilepath, "VSuniform.glsl", "FS.glsl");
        gl4.glUseProgram(programs[Program.uniform.ordinal()].getProgramId());
        {
            gl4.glUniformMatrix4fv(((ProgramUniform) programs[Program.uniform.ordinal()]).getModelToClipUL(),
                    1, false, modelToClip);
        }
        gl4.glUseProgram(0);
    }

    private void initTextures(GL4 gl4) {

        textures = new int[Texture.size.ordinal()];
        gl4.glGenTextures(Texture.size.ordinal(), textures, 0);

        float[] color = new float[]{0f, .66f, .33f, 1f};
        FloatBuffer colorBuffer = GLBuffers.newDirectFloatBuffer(glWindow.getWidth() * glWindow.getHeight() * 4);
        for (int p = 0; p < (glWindow.getWidth() * glWindow.getHeight()); p++) {
            colorBuffer.put(color);
        }
        colorBuffer.flip();

        for (int t = 0; t < 2; t++) {

            gl4.glBindTexture(GL4.GL_TEXTURE_RECTANGLE, textures[t]);
            {
                gl4.glTexImage2D(GL4.GL_TEXTURE_RECTANGLE, 0, GL4.GL_RGBA, glWindow.getWidth(), glWindow.getHeight(),
                        0, GL4.GL_RGBA, GL4.GL_FLOAT, colorBuffer);

                gl4.glTexParameteri(GL4.GL_TEXTURE_RECTANGLE, GL4.GL_TEXTURE_BASE_LEVEL, 0);
                gl4.glTexParameteri(GL4.GL_TEXTURE_RECTANGLE, GL4.GL_TEXTURE_MAX_LEVEL, 0);

                gl4.glTexParameteri(GL4.GL_TEXTURE_RECTANGLE, GL4.GL_TEXTURE_WRAP_S, GL4.GL_CLAMP_TO_EDGE);
                gl4.glTexParameteri(GL4.GL_TEXTURE_RECTANGLE, GL4.GL_TEXTURE_WRAP_T, GL4.GL_CLAMP_TO_EDGE);
                gl4.glTexParameteri(GL4.GL_TEXTURE_RECTANGLE, GL4.GL_TEXTURE_MIN_FILTER, GL4.GL_NEAREST);
                gl4.glTexParameteri(GL4.GL_TEXTURE_RECTANGLE, GL4.GL_TEXTURE_MAG_FILTER, GL4.GL_NEAREST);
            }
            gl4.glBindTexture(GL4.GL_TEXTURE_RECTANGLE, 0);
        }
    }

    private void initTargets(GL4 gl4) {

        fbos = new int[Program.size.ordinal()];
        gl4.glGenFramebuffers(Program.size.ordinal(), fbos, 0);

        for (int framebuffer = 0; framebuffer < Program.size.ordinal(); framebuffer++) {

            gl4.glBindTexture(GL4.GL_TEXTURE_RECTANGLE, textures[Texture.color.ordinal()]);

            gl4.glTexImage2D(GL4.GL_TEXTURE_RECTANGLE, 0, GL4.GL_RGBA, glWindow.getWidth(),
                    glWindow.getHeight(), 0, GL4.GL_RGBA, GL4.GL_FLOAT, null);

            gl4.glTexParameteri(GL4.GL_TEXTURE_RECTANGLE, GL4.GL_TEXTURE_BASE_LEVEL, 0);
            gl4.glTexParameteri(GL4.GL_TEXTURE_RECTANGLE, GL4.GL_TEXTURE_MAX_LEVEL, 0);

            gl4.glBindTexture(GL4.GL_TEXTURE_RECTANGLE, textures[Texture.depth.ordinal()]);

            gl4.glTexImage2D(GL4.GL_TEXTURE_RECTANGLE, 0, GL4.GL_DEPTH_COMPONENT, glWindow.getWidth(),
                    glWindow.getHeight(), 0, GL4.GL_DEPTH_COMPONENT, GL4.GL_FLOAT, null);

            gl4.glTexParameteri(GL4.GL_TEXTURE_RECTANGLE, GL4.GL_TEXTURE_BASE_LEVEL, 0);
            gl4.glTexParameteri(GL4.GL_TEXTURE_RECTANGLE, GL4.GL_TEXTURE_MAX_LEVEL, 0);

            gl4.glBindFramebuffer(GL4.GL_FRAMEBUFFER, fbos[framebuffer]);

            gl4.glFramebufferTexture2D(GL4.GL_FRAMEBUFFER, GL4.GL_DEPTH_ATTACHMENT,
                    GL4.GL_TEXTURE_RECTANGLE, textures[Texture.depth.ordinal()], 0);
            gl4.glFramebufferTexture2D(GL4.GL_FRAMEBUFFER, GL4.GL_COLOR_ATTACHMENT0,
                    GL4.GL_TEXTURE_RECTANGLE, textures[Texture.color.ordinal()], 0);
        }
    }

    private void initVbo(GL4 gl4) {

        float[] vertexAttributes = new float[]{0f, 0f, 0f};

        gl4.glGenBuffers(1, objects, Objects.vbo.ordinal());

        gl4.glBindBuffer(GL4.GL_ARRAY_BUFFER, objects[Objects.vbo.ordinal()]);
        {
            FloatBuffer buffer = GLBuffers.newDirectFloatBuffer(vertexAttributes);

            gl4.glBufferData(GL4.GL_ARRAY_BUFFER, vertexAttributes.length * 4, buffer, GL4.GL_STATIC_DRAW);
        }
        gl4.glBindBuffer(GL4.GL_ARRAY_BUFFER, 0);
    }

    private void initVao(GL4 gl4) {

        gl4.glBindBuffer(GL4.GL_ARRAY_BUFFER, objects[Objects.vbo.ordinal()]);

        gl4.glGenVertexArrays(1, objects, Objects.vao.ordinal());
        gl4.glBindVertexArray(objects[Objects.vao.ordinal()]);
        {
            gl4.glEnableVertexAttribArray(0);
            {
                gl4.glVertexAttribPointer(0, 3, GL4.GL_FLOAT, false, 0, 0);
            }
        }
        gl4.glBindVertexArray(0);
    }

    private void initUbos(GL4 gl4) {

        int size = modelToClip.capacity() * GLBuffers.SIZEOF_FLOAT;

        gl4.glGenBuffers(2, objects, Objects.ubo0.ordinal());

        for (int ubo = 0; ubo < 2; ubo++) {

            gl4.glBindBuffer(GL4.GL_UNIFORM_BUFFER, objects[Objects.ubo0.ordinal() + ubo]);
            {
                gl4.glBufferData(GL4.GL_UNIFORM_BUFFER, size, modelToClip, GL4.GL_DYNAMIC_DRAW);
            }
            gl4.glBindBuffer(GL4.GL_UNIFORM_BUFFER, 0);
        }
    }

    @Override
    public void dispose(GLAutoDrawable glad) {
        dispose();
    }

    private void dispose() {
        animator.stop();
        System.exit(0);
    }

    @Override
    public void display(GLAutoDrawable glad) {

        GL4 gl4 = glad.getGL().getGL4();

        if (update) {
            update = false;
            update(gl4);
        }

        switch (stateChange) {

            case RenderTarget:

                for (int i = 0; i < repeat; i++) {
                    gl4.glBindFramebuffer(GL4.GL_FRAMEBUFFER, fbos[i % 2]);
                    gl4.glDrawArrays(GL4.GL_POINTS, 0, 1);
                    gl4.glBindFramebuffer(GL4.GL_FRAMEBUFFER, 0);
                }
                break;

            case Program:

                for (int i = 0; i < repeat; i++) {
                    gl4.glUseProgram(programs[i % 2].getProgramId());
                    gl4.glDrawArrays(GL4.GL_POINTS, 0, 1);
                    gl4.glUseProgram(0);
                }
                break;

            case TextureBinding:

                for (int i = 0; i < repeat; i++) {
                    gl4.glActiveTexture(GL4.GL_TEXTURE0 + i % 2);
                    gl4.glBindTexture(GL4.GL_TEXTURE_RECTANGLE, textures[i % 2]);
                    gl4.glDrawArrays(GL4.GL_POINTS, 0, 1);
                    gl4.glBindTexture(GL4.GL_TEXTURE_RECTANGLE, 0);
                }
                break;

            case VertexFormat:

                for (int i = 0; i < repeat; i++) {
                    gl4.glEnableVertexAttribArray(i % 2);
                    gl4.glVertexAttribPointer(i % 2, 3, GL4.GL_FLOAT, true, 0, 0);
                    gl4.glDrawArrays(GL4.GL_POINTS, 0, 1);
                    gl4.glDisableVertexAttribArray(i % 2);
                }
                break;

            case UboBindings:

                for (int i = 0; i < repeat; i++) {
                    gl4.glBindBuffer(GL4.GL_UNIFORM_BUFFER, objects[Objects.ubo0.ordinal() + i % 2]);
                    {
                        gl4.glBindBufferRange(GL4.GL_UNIFORM_BUFFER, 0, objects[Objects.ubo0.ordinal() + i % 2],
                                0, 16 * GLBuffers.SIZEOF_FLOAT);

                        gl4.glDrawArrays(GL4.GL_POINTS, 0, 1);
                    }
                    gl4.glBindBuffer(GL4.GL_UNIFORM_BUFFER, 0);
                }
                break;

            case VertexBindings:

                for (int i = 0; i < repeat; i++) {
                    gl4.glBindBuffer(GL4.GL_ARRAY_BUFFER, objects[Objects.vbo.ordinal()]);
                    {
                        gl4.glVertexAttribPointer(0, 3, GL4.GL_FLOAT, true, 0, 0);
                        gl4.glDrawArrays(GL4.GL_POINTS, 0, 1);
                    }
                    gl4.glBindBuffer(GL4.GL_ARRAY_BUFFER, 0);
                }
                break;

            case UniformUpdates:

                for (int i = 0; i < repeat; i++) {

                    gl4.glUniform1i(((ProgramUniform) programs[Program.uniform.ordinal()]).getuUL(), 1);
                    gl4.glDrawArrays(GL4.GL_POINTS, 0, 1);
                }
                break;
        }
        glad.swapBuffers();
    }

    private void update(GL4 gl4) {

        switch (stateChange) {

            case RenderTarget:

                gl4.glBindBuffer(GL4.GL_ARRAY_BUFFER, objects[Objects.vbo.ordinal()]);
                gl4.glBindVertexArray(objects[Objects.vao.ordinal()]);
                programs[Program.one.ordinal()].bind(gl4);
                repeat = 10_000;
                break;

            case Program:

                gl4.glBindBuffer(GL4.GL_ARRAY_BUFFER, objects[Objects.vbo.ordinal()]);
                gl4.glBindVertexArray(objects[Objects.vao.ordinal()]);
                gl4.glBindFramebuffer(GL4.GL_FRAMEBUFFER, fbos[0]);
                repeat = 100_000;
                break;

            case TextureBinding:
                gl4.glBindBuffer(GL4.GL_ARRAY_BUFFER, objects[Objects.vbo.ordinal()]);
                gl4.glBindVertexArray(objects[Objects.vao.ordinal()]);
                gl4.glBindFramebuffer(GL4.GL_FRAMEBUFFER, fbos[0]);
                programs[Program.texture.ordinal()].bind(gl4);
                repeat = 1_000_000;
                break;

            case VertexFormat:
                gl4.glBindBuffer(GL4.GL_ARRAY_BUFFER, objects[Objects.vbo.ordinal()]);
                gl4.glBindVertexArray(objects[Objects.vao.ordinal()]);
                gl4.glBindFramebuffer(GL4.GL_FRAMEBUFFER, fbos[0]);
                programs[Program.vertexFormat.ordinal()].bind(gl4);
                repeat = 1_000_000;
                break;

            case UboBindings:
                gl4.glBindBuffer(GL4.GL_ARRAY_BUFFER, objects[Objects.vbo.ordinal()]);
                gl4.glBindVertexArray(objects[Objects.vao.ordinal()]);
                gl4.glBindFramebuffer(GL4.GL_FRAMEBUFFER, fbos[0]);
                programs[Program.ubo.ordinal()].bind(gl4);
                repeat = 1_000_000;
                break;

            case VertexBindings:
                gl4.glBindVertexArray(objects[Objects.vao.ordinal()]);
                gl4.glBindFramebuffer(GL4.GL_FRAMEBUFFER, fbos[0]);
                programs[Program.one.ordinal()].bind(gl4);
                repeat = 1_000_000;
                break;

            case UniformUpdates:
                gl4.glBindBuffer(GL4.GL_ARRAY_BUFFER, objects[Objects.vbo.ordinal()]);
                gl4.glBindVertexArray(objects[Objects.vao.ordinal()]);
                gl4.glBindFramebuffer(GL4.GL_FRAMEBUFFER, fbos[0]);
                programs[Program.uniform.ordinal()].bind(gl4);
                repeat = 1_000_000;
                break;
        }

    }

    @Override
    public void reshape(GLAutoDrawable glad, int x, int y, int w, int h) {

        GL4 gl4 = glad.getGL().getGL4();

        gl4.glViewport(x, y, w, h);
    }

    @Override
    public void keyPressed(KeyEvent e) {

        switch (e.getKeyCode()) {

            case KeyEvent.VK_1:
                printResult();
                stateChange = StateChange.RenderTarget;
                printStateChange();
                animator.resetFPSCounter();
                update = true;
                break;

            case KeyEvent.VK_2:
                printResult();
                stateChange = StateChange.Program;
                printStateChange();
                animator.resetFPSCounter();
                update = true;
                break;

            case KeyEvent.VK_3:
                printResult();
                stateChange = StateChange.TextureBinding;
                printStateChange();
                animator.resetFPSCounter();
                update = true;
                break;

            case KeyEvent.VK_4:
                printResult();
                stateChange = StateChange.VertexFormat;
                printStateChange();
                animator.resetFPSCounter();
                update = true;
                break;

            case KeyEvent.VK_5:
                printResult();
                stateChange = StateChange.UboBindings;
                printStateChange();
                animator.resetFPSCounter();
                update = true;
                break;

            case KeyEvent.VK_6:
                printResult();
                stateChange = StateChange.VertexBindings;
                printStateChange();
                animator.resetFPSCounter();
                update = true;
                break;

            case KeyEvent.VK_7:
                printResult();
                stateChange = StateChange.UniformUpdates;
                printStateChange();
                animator.resetFPSCounter();
                update = true;
                break;

            case KeyEvent.VK_ESCAPE:
                dispose();
                break;

            default:
                printHelp();
                break;
        }
    }

    private void printStateChange() {
        switch (stateChange) {
            case RenderTarget:
                System.out.println("State change: Render Target, factor 10k");
                break;
            case Program:
                System.out.println("State change: Program, factor 100k");
                break;
            case TextureBinding:
                System.out.println("State change: Texture Bindings, factor 1M");
                break;
            case VertexFormat:
                System.out.println("State change: Vertex Format, factor 1M");
                break;
            case UboBindings:
                System.out.println("State change: UBO Bindings, factor 1M");
                break;
            case VertexBindings:
                System.out.println("State change: Vertex Bindings, factor 1M");
                break;
            case UniformUpdates:
                System.out.println("State change: Uniform Updates, factor 1M");
                break;
        }
    }

    private void printResult() {
        animator.pause();
        if (animator.getTotalFPS() != 0) {
            System.out.println("average glCalls per second " + (animator.getTotalFPS() * repeat));
        }
        animator.resume();
    }

    private void printHelp() {
        System.out.println("1 - Render Target");
        System.out.println("2 - Program");
        System.out.println("3 - Texture Bindings");
        System.out.println("4 - Vertex Format");
        System.out.println("5 - UBO Bindings");
        System.out.println("6 - Vertex Bindings");
        System.out.println("7 - Uniform Updates");
    }

    @Override
    public void keyReleased(KeyEvent e) {

    }
}
