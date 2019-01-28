/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package callOverhead.gl3;

import com.jogamp.newt.Display;
import com.jogamp.newt.NewtFactory;
import com.jogamp.newt.Screen;
import com.jogamp.newt.event.KeyEvent;
import com.jogamp.newt.event.KeyListener;
import com.jogamp.newt.opengl.GLWindow;
import static com.jogamp.opengl.GL.GL_ARRAY_BUFFER;
import static com.jogamp.opengl.GL.GL_CLAMP_TO_EDGE;
import static com.jogamp.opengl.GL.GL_COLOR_ATTACHMENT0;
import static com.jogamp.opengl.GL.GL_DEPTH_ATTACHMENT;
import static com.jogamp.opengl.GL.GL_DYNAMIC_DRAW;
import static com.jogamp.opengl.GL.GL_FLOAT;
import static com.jogamp.opengl.GL.GL_FRAMEBUFFER;
import static com.jogamp.opengl.GL.GL_NEAREST;
import static com.jogamp.opengl.GL.GL_POINTS;
import static com.jogamp.opengl.GL.GL_RGBA;
import static com.jogamp.opengl.GL.GL_RGBA8;
import static com.jogamp.opengl.GL.GL_STATIC_DRAW;
import static com.jogamp.opengl.GL.GL_TEXTURE0;
import static com.jogamp.opengl.GL.GL_TEXTURE_2D;
import static com.jogamp.opengl.GL.GL_TEXTURE_MAG_FILTER;
import static com.jogamp.opengl.GL.GL_TEXTURE_MIN_FILTER;
import static com.jogamp.opengl.GL.GL_TEXTURE_WRAP_S;
import static com.jogamp.opengl.GL.GL_TEXTURE_WRAP_T;
import static com.jogamp.opengl.GL2ES2.GL_DEPTH_COMPONENT;
import static com.jogamp.opengl.GL2ES2.GL_FRAGMENT_SHADER;
import static com.jogamp.opengl.GL2ES2.GL_VERTEX_SHADER;
import static com.jogamp.opengl.GL2ES3.GL_DEPTH_COMPONENT32F;
import static com.jogamp.opengl.GL2ES3.GL_TEXTURE_BASE_LEVEL;
import static com.jogamp.opengl.GL2ES3.GL_TEXTURE_MAX_LEVEL;
import static com.jogamp.opengl.GL2ES3.GL_UNIFORM_BUFFER;
import static com.jogamp.opengl.GL2GL3.GL_TEXTURE_RECTANGLE;
import com.jogamp.opengl.GL3;
import com.jogamp.opengl.GLAutoDrawable;
import com.jogamp.opengl.GLCapabilities;
import com.jogamp.opengl.GLContext;
import com.jogamp.opengl.GLEventListener;
import com.jogamp.opengl.GLProfile;
import com.jogamp.opengl.util.Animator;
import com.jogamp.opengl.util.AnimatorBase;
import com.jogamp.opengl.util.GLBuffers;
import com.jogamp.opengl.util.glsl.ShaderCode;
import com.jogamp.opengl.util.glsl.ShaderProgram;
import common.BufferUtils;
import common.GlDebugOutput;
import common.TimeHack6435126;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import callOverhead.Resource;
import callOverhead.Semantic;
import glm.mat._4.Mat4;
import glm.vec._2.Vec2;
import glm.vec._4.Vec4;
import java.nio.ByteBuffer;

/**
 *
 * @author elect
 */
public class Jogl implements GLEventListener, KeyListener {

    private static Animator animator;
    private static GLWindow glWindow;
    private static final boolean DEBUG = true;

    /**
     * @param args the command line arguments
     */
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
        glWindow.setTitle("JOGL - GL Call Overhead");
        if (DEBUG) {
            glWindow.setContextCreationFlags(GLContext.CTX_OPTION_DEBUG);
        }
        glWindow.setVisible(true);
        if (DEBUG) {
            glWindow.getContext().addGLDebugListener(new GlDebugOutput());
        }

        Jogl glCallsOverhead = new Jogl();
        glWindow.addGLEventListener(glCallsOverhead);
        glWindow.addKeyListener(glCallsOverhead);

        animator = new Animator();
        animator.setRunAsFastAsPossible(true);
        animator.setModeBits(false, AnimatorBase.MODE_EXPECT_AWT_RENDERING_THREAD);
        animator.add(glWindow);
        animator.setExclusiveContext(true);

        TimeHack6435126.enableHighResolutionTimer();
        glWindow.setExclusiveContextThread(animator.getExclusiveContextThread());
        animator.start();
    }

    private int[] programName = new int[Resource.Program.MAX];
    private IntBuffer framebufferName = GLBuffers.newDirectIntBuffer(Resource.Framebuffer.MAX),
            bufferName = GLBuffers.newDirectIntBuffer(Resource.Buffer.MAX),
            vertexArrayName = GLBuffers.newDirectIntBuffer(1),
            textureName = GLBuffers.newDirectIntBuffer(Resource.Texture.MAX);
    private int repeat, mode, frames, updateInterval_ms = 1_000, uniformLocation;
    private long cpuStart_ns, cpuTotal_ns, updateStart_ms;
    private boolean update = true;

    @Override
    public void init(GLAutoDrawable glad) {

        GL3 gl3 = glad.getGL().getGL3();

        initPrograms(gl3);
        initTextures(gl3);
        initFramebuffers(gl3);
        initBuffer(gl3);
        initVertexArray(gl3);

        mode = Semantic.Mode.FRAMEBUFFER;
//        update(gl3);

        Resource.printHelp();
        Resource.printStateChange(mode);
    }

    private void initPrograms(GL3 gl3) {

        for (int i = Resource.Program.A; i < Resource.Program.MAX; i++) {

            ShaderProgram shaderProgram = new ShaderProgram();

            ShaderCode vertShaderCode = ShaderCode.create(gl3, GL_VERTEX_SHADER, this.getClass(), Resource.SHADERS_ROOT,
                    null, Resource.VERT_SHADERS_SOURCE[i], "vert", null, true);
            ShaderCode fragShaderCode = ShaderCode.create(gl3, GL_FRAGMENT_SHADER, this.getClass(), Resource.SHADERS_ROOT,
                    null, Resource.FRAG_SHADERS_SOURCE[i], "frag", null, true);

            shaderProgram.add(vertShaderCode);
            shaderProgram.add(fragShaderCode);

            shaderProgram.link(gl3, System.out);

            programName[i] = shaderProgram.program();

            vertShaderCode.destroy(gl3);
            fragShaderCode.destroy(gl3);
        }

        gl3.glUseProgram(programName[Resource.Program.TEXTURE]);
        gl3.glUniform1i(
                gl3.glGetUniformLocation(programName[Resource.Program.TEXTURE], "texture0"),
                Semantic.Sampler.TEXTURE0);

        gl3.glUniformBlockBinding(programName[Resource.Program.UBO],
                gl3.glGetUniformBlockIndex(programName[Resource.Program.UBO], "Transform0"),
                Semantic.Uniform.TRANSFORM0);

        uniformLocation = gl3.glGetUniformLocation(programName[Resource.Program.UNIFORM], "z");
    }

    private void initTextures(GL3 gl3) {

        gl3.glGenTextures(Resource.Texture.MAX, textureName);

        float[] texColor = new float[]{0f, .5f, 1f, 1f};
        FloatBuffer colorBuffer = GLBuffers.newDirectFloatBuffer(glWindow.getWidth() * glWindow.getHeight() * 4);
        for (int i = 0; i < (glWindow.getWidth() * glWindow.getHeight()); i++) {
            colorBuffer.put(texColor);
        }
        colorBuffer.rewind();

        for (int i = Resource.Texture.A; i <= Resource.Texture.B; i++) {

            gl3.glBindTexture(GL_TEXTURE_RECTANGLE, textureName.get(i));

            gl3.glTexImage2D(GL_TEXTURE_RECTANGLE, 0, GL_RGBA8, glWindow.getWidth(), glWindow.getHeight(), 0, GL_RGBA,
                    GL_FLOAT, colorBuffer);

            gl3.glTexParameteri(GL_TEXTURE_RECTANGLE, GL_TEXTURE_BASE_LEVEL, 0);
            gl3.glTexParameteri(GL_TEXTURE_RECTANGLE, GL_TEXTURE_MAX_LEVEL, 0);

            gl3.glTexParameteri(GL_TEXTURE_RECTANGLE, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
            gl3.glTexParameteri(GL_TEXTURE_RECTANGLE, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);
            gl3.glTexParameteri(GL_TEXTURE_RECTANGLE, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
            gl3.glTexParameteri(GL_TEXTURE_RECTANGLE, GL_TEXTURE_MAG_FILTER, GL_NEAREST);
        }
        gl3.glBindTexture(GL_TEXTURE_2D, textureName.get(Resource.Texture.COLOR));
        gl3.glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA, glWindow.getWidth(), glWindow.getHeight(), 0, GL_RGBA, GL_FLOAT, null);

        gl3.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_BASE_LEVEL, 0);
        gl3.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAX_LEVEL, 0);

        gl3.glBindTexture(GL_TEXTURE_2D, textureName.get(Resource.Texture.DEPTH));
        gl3.glTexImage2D(GL_TEXTURE_2D, 0, GL_DEPTH_COMPONENT32F, glWindow.getWidth(), glWindow.getHeight(), 0,
                GL_DEPTH_COMPONENT, GL_FLOAT, null);

        gl3.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_BASE_LEVEL, 0);
        gl3.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAX_LEVEL, 0);
    }

    private void initFramebuffers(GL3 gl3) {

        gl3.glGenFramebuffers(Resource.Framebuffer.MAX, framebufferName);

        for (int i = 0; i < Resource.Framebuffer.MAX; i++) {

            gl3.glBindFramebuffer(GL_FRAMEBUFFER, framebufferName.get(i));

            gl3.glFramebufferTexture2D(GL_FRAMEBUFFER, GL_DEPTH_ATTACHMENT, GL_TEXTURE_2D,
                    textureName.get(Resource.Texture.DEPTH), 0);
            gl3.glFramebufferTexture2D(GL_FRAMEBUFFER, GL_COLOR_ATTACHMENT0, GL_TEXTURE_2D,
                    textureName.get(Resource.Texture.COLOR), 0);
        }
    }

    private void initBuffer(GL3 gl3) {

        FloatBuffer vertexBuffer = GLBuffers.newDirectFloatBuffer(new float[]{0f, 0f});
        ByteBuffer transformBuffer = GLBuffers.newDirectByteBuffer(Mat4.SIZE);

        gl3.glGenBuffers(Resource.Buffer.MAX, bufferName);

        gl3.glBindBuffer(GL_ARRAY_BUFFER, bufferName.get(Resource.Buffer.ARRAY));
        gl3.glBufferData(GL_ARRAY_BUFFER, vertexBuffer.capacity() * Float.BYTES, vertexBuffer, GL_STATIC_DRAW);

        for (int i = Resource.Buffer.A; i <= Resource.Buffer.B; i++) {

            gl3.glBindBuffer(GL_UNIFORM_BUFFER, bufferName.get(Resource.Buffer.A + i));
            gl3.glBufferData(GL_UNIFORM_BUFFER, Vec4.SIZE, null, GL_DYNAMIC_DRAW);
        }

        new Mat4(1).toDbb(transformBuffer);
        gl3.glBindBuffer(GL_UNIFORM_BUFFER, bufferName.get(Resource.Buffer.TRANSFORM));
        gl3.glBufferData(GL_UNIFORM_BUFFER, Mat4.SIZE, transformBuffer, GL_DYNAMIC_DRAW);

        BufferUtils.destroyDirectBuffer(vertexBuffer);
        BufferUtils.destroyDirectBuffer(transformBuffer);
    }

    private void initVertexArray(GL3 gl3) {

        gl3.glGenVertexArrays(1, vertexArrayName);
        gl3.glBindVertexArray(vertexArrayName.get(0));

        gl3.glBindBuffer(GL_ARRAY_BUFFER, bufferName.get(Resource.Buffer.ARRAY));
        gl3.glEnableVertexAttribArray(Semantic.Attr.POSITION);
        gl3.glVertexAttribPointer(Semantic.Attr.POSITION, 2, GL_FLOAT, false, Vec2.SIZE, 0);
    }

    @Override
    public void display(GLAutoDrawable glad) {

        GL3 gl3 = glad.getGL().getGL3();

        if (update) {
            update = false;
            update(gl3);
        }

        cpuStart_ns = System.nanoTime();

        switch (mode) {

            case Semantic.Mode.FRAMEBUFFER:

                for (int i = 0; i < repeat; i++) {
                    gl3.glBindFramebuffer(GL_FRAMEBUFFER, framebufferName.get(i % 2));
                    gl3.glDrawArrays(GL_POINTS, 0, 1);
//                    gl3.glBindFramebuffer(GL_FRAMEBUFFER, 0);
                }
                break;

            case Semantic.Mode.PROGRAM:

                for (int i = 0; i < repeat; i++) {
                    gl3.glUseProgram(programName[Resource.Program.A + i % 2]);
                    gl3.glDrawArrays(GL_POINTS, 0, 1);
//                    gl3.glUseProgram(0);
                }
                break;
//
            case Semantic.Mode.TEXTURE:

                for (int i = 0; i < repeat; i++) {
                    gl3.glActiveTexture(GL_TEXTURE0 + i % 2);
                    gl3.glBindTexture(GL_TEXTURE_RECTANGLE, textureName.get(Resource.Texture.A + i % 2));
                    gl3.glDrawArrays(GL_POINTS, 0, 1);
//                    gl3.glBindTexture(GL_TEXTURE_RECTANGLE, 0);
                }
                break;

            case Semantic.Mode.VERTEX_FORMAT:

                for (int i = 0; i < repeat; i++) {
                    gl3.glEnableVertexAttribArray(Semantic.Attr.POSITION);
                    gl3.glVertexAttribPointer(Semantic.Attr.POSITION, 2, GL_FLOAT, false, Vec2.SIZE, 0);
                    gl3.glDrawArrays(GL_POINTS, 0, 1);
//                    gl3.glDisableVertexAttribArray(Semantic.Attr.POSITION);
                }
                break;

            case Semantic.Mode.UBO:

                for (int i = 0; i < repeat; i++) {
                    gl3.glBindBuffer(GL_UNIFORM_BUFFER, bufferName.get(Resource.Buffer.TRANSFORM));
                    gl3.glBindBufferRange(GL_UNIFORM_BUFFER, Semantic.Uniform.TRANSFORM0,
                            bufferName.get(Resource.Buffer.TRANSFORM), 0, Mat4.SIZE);
                    gl3.glDrawArrays(GL_POINTS, 0, 1);
//                    gl3.glBindBuffer(GL_UNIFORM_BUFFER, 0);
                }
                break;

            case Semantic.Mode.VERTEX_BINDING:

                for (int i = 0; i < repeat; i++) {
                    gl3.glBindBuffer(GL_ARRAY_BUFFER, bufferName.get(Resource.Buffer.ARRAY));
                    gl3.glVertexAttribPointer(Semantic.Attr.POSITION, 2, GL_FLOAT, false, Vec2.SIZE, 0);
                    gl3.glDrawArrays(GL_POINTS, 0, 1);
//                    gl3.glBindBuffer(GL_ARRAY_BUFFER, 0);
                }
                break;

            case Semantic.Mode.UNIFORM:

                for (int i = 0; i < repeat; i++) {
                    gl3.glUniform1f(uniformLocation, 0);
                    gl3.glDrawArrays(GL_POINTS, 0, 1);
                }
                break;
        }
//        cpuTotal_ns += System.nanoTime() - cpuStart_ns;

        frames++;

        if ((System.currentTimeMillis() - updateStart_ms) > updateInterval_ms) {

            int totalSwitches = repeat * frames * 1;

            long now = System.nanoTime();
            cpuTotal_ns = now - cpuStart_ns;
            cpuStart_ns = now;

            System.out.println("totalSwitches: " + totalSwitches);
            System.out.println("cpuTotal_ns: " + cpuTotal_ns);

            String switchesPerS = String.format("%,.0f", totalSwitches / ((double) cpuTotal_ns / 1_000_000_000));
            System.out.println("switches per seconds: " + switchesPerS);

            resetCounters();
            updateStart_ms = System.currentTimeMillis();
        }
    }

    private void update(GL3 gl3) {

        gl3.glBindVertexArray(vertexArrayName.get(0));
        gl3.glUseProgram(programName[Resource.Program.A]);
        gl3.glBindFramebuffer(GL_FRAMEBUFFER, 0);

        switch (mode) {

            case Semantic.Mode.FRAMEBUFFER:
                repeat = 1_000;
                break;

            case Semantic.Mode.PROGRAM:
                repeat = 10_000;
                break;

            case Semantic.Mode.TEXTURE:
                gl3.glUseProgram(programName[Resource.Program.TEXTURE]);
                repeat = 100_000;
                break;

            case Semantic.Mode.VERTEX_FORMAT:
                repeat = 1_000_000;
                break;

            case Semantic.Mode.UBO:
                gl3.glUseProgram(programName[Resource.Program.UBO]);
                repeat = 1_000_000;
                break;

            case Semantic.Mode.VERTEX_BINDING:
                repeat = 1_000_000;
                break;

            case Semantic.Mode.UNIFORM:
                gl3.glUseProgram(programName[Resource.Program.UNIFORM]);
                repeat = 1_000_000;
                break;
        }
        resetCounters();
    }

    private void resetCounters() {
        frames = 0;
        cpuTotal_ns = 0;
    }

    @Override
    public void reshape(GLAutoDrawable glad, int x, int y, int w, int h) {
        GL3 gl3 = glad.getGL().getGL3();
        gl3.glViewport(x, y, w, h);
    }

    @Override
    public void dispose(GLAutoDrawable glad) {
        if (animator.isAnimating()) {
            animator.stop();
        }
        System.exit(0);
    }

    @Override
    public void keyReleased(KeyEvent e) {
    }

    @Override
    public void keyPressed(KeyEvent e) {

//        printResult();
//        glWindow.getContext().makeCurrent();
        switch (e.getKeyCode()) {

            case KeyEvent.VK_1:
                mode = Semantic.Mode.FRAMEBUFFER;
                break;

            case KeyEvent.VK_2:
                mode = Semantic.Mode.PROGRAM;
                break;

            case KeyEvent.VK_3:
                mode = Semantic.Mode.TEXTURE;
                break;

            case KeyEvent.VK_4:
                mode = Semantic.Mode.VERTEX_FORMAT;
                break;

            case KeyEvent.VK_5:
                mode = Semantic.Mode.UBO;
                break;

            case KeyEvent.VK_6:
                mode = Semantic.Mode.VERTEX_BINDING;
                break;

            case KeyEvent.VK_7:
                mode = Semantic.Mode.UNIFORM;
                break;

            case KeyEvent.VK_ESCAPE:
                animator.stop();
                glWindow.destroy();
                break;

            default:
                Resource.printHelp();
                break;
        }

        Resource.printStateChange(mode);

//        update(glWindow.getContext().getGL().getGL3());
        update = true;

//        glWindow.getContext().release();
    }
}
