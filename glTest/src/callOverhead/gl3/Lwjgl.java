package callOverhead.gl3;

import common.enums.Objects;
import common.enums.Program;
import common.enums.StateChange;
import common.enums.Texture;
import org.lwjgl.glfw.*;
import org.lwjgl.opengl.*;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import jglm.Mat4;
import jglm.Vec2i;
import callOverhead.gl4.lwjgl.glsl.ProgramBase;
import org.lwjgl.BufferUtils;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.system.MemoryUtil.*;

public class Lwjgl {

    // We need to strongly reference callback instances.
    private GLFWErrorCallback errorCallback;
    private GLFWKeyCallback keyCallback;

    // The window handle
    private long window;
    private Vec2i windowSize;
    
    private StateChange stateChange;
    private FloatBuffer modelToClip;
    private ProgramBase[] programs;
    private IntBuffer fbos;
    private IntBuffer textures;
    private IntBuffer objects;
    private int repeat, fpsSout, totalFrames, deltaFrames;
    private long startTime, lastTime;
    
    public void run() {
//        System.out.println("GlCallsOverheads " + Sys.getVersion() + "!");
        
        try {
            init();
            loop();

            // Release window and window callbacks
            glfwDestroyWindow(window);
            keyCallback.release();
        } finally {
            // Terminate GLFW and release the GLFWerrorfun
            glfwTerminate();
            errorCallback.release();
        }
    }
    
    private void init() {
        // Setup an error callback. The default implementation
        // will print the error message in System.err.
//        glfwSetErrorCallback(errorCallback = errorCallbackPrint(System.err));

        // Initialize GLFW. Most GLFW functions will not work before doing this.
        if (glfwInit() != GL11.GL_TRUE) {
            throw new IllegalStateException("Unable to initialize GLFW");
        }

        // Configure our window
        glfwDefaultWindowHints(); // optional, the current window hints are already the default
        glfwWindowHint(GLFW_VISIBLE, GL_FALSE); // the window will stay hidden after creation
        glfwWindowHint(GLFW_RESIZABLE, GL_TRUE); // the window will be resizable

        windowSize = new Vec2i(1024, 768);

        // Create the window
        window = glfwCreateWindow(windowSize.x, windowSize.y, "Micro gl calls overhead benchmark", NULL, NULL);
        if (window == NULL) {
            throw new RuntimeException("Failed to create the GLFW window");
        }

        // Setup a key callback. It will be called every time a key is pressed, repeated or released.
        glfwSetKeyCallback(window, keyCallback = new GLFWKeyCallback() {
            @Override
            public void invoke(long window, int key, int scancode, int action, int mods) {
                if (key == GLFW_KEY_ESCAPE && action == GLFW_RELEASE) {
                    glfwSetWindowShouldClose(window, GL_TRUE); // We will detect this in our rendering loop
                }
            }
        });

        // Get the resolution of the primary monitor
//        ByteBuffer vidmode = glfwGetVideoMode(glfwGetPrimaryMonitor());
//        // Center our window
//        glfwSetWindowPos(window, (GLFWvidmode.width(vidmode) - windowSize.x) / 2,
//                (GLFWvidmode.height(vidmode) - windowSize.y) / 2);
//
//        // Make the OpenGL context current
//        glfwMakeContextCurrent(window);
//        GLContext.createFromCurrent();
        // Enable v-sync
        glfwSwapInterval(0);

        // Make the window visible
        glfwShowWindow(window);
        
        initPrograms();
        initTextures();
        initTargets();
        
        objects = BufferUtils.createIntBuffer(Objects.size.ordinal());
        
        initVbo();
        initVao();
    }
    
    private void initPrograms() {
        
        String shaderFilepath = "src/common/shaders/";
        
        modelToClip = BufferUtils.createFloatBuffer(16);
        modelToClip.put(new Mat4(1f).toFloatArray());
        modelToClip.flip();
        
        programs = new ProgramBase[Program.size.ordinal()];
        for (int i = 0; i < 2; i++) {
            
            programs[i] = new ProgramBase(shaderFilepath, "VS.glsl", "FS.glsl");
            
            GL20.glUseProgram(programs[i].getId());
            {
                GL20.glUniformMatrix4fv(programs[i].getModelToClipUL(), false, modelToClip);
            }
            GL20.glUseProgram(0);
        }
//
//        programs[Program.texture.ordinal()] = new ProgramTexture(gl4, shaderFilepath, "VS.glsl", "FStexture.glsl");
//        gl4.glUseProgram(programs[Program.texture.ordinal()].getProgramId());
//        {
//            gl4.glUniformMatrix4fv(((ProgramTexture) programs[Program.texture.ordinal()]).getModelToClipUL(),
//                    1, false, modelToClip);
//        }
//        gl4.glUseProgram(0);
//
//        programs[Program.vertexFormat.ordinal()] = new ProgramBase(gl4, shaderFilepath,
//                "VSvertexFormat.glsl", "FS.glsl");
//        gl4.glUseProgram(programs[Program.vertexFormat.ordinal()].getProgramId());
//        {
//            gl4.glUniformMatrix4fv(((ProgramBase) programs[Program.vertexFormat.ordinal()]).getModelToClipUL(),
//                    1, false, modelToClip);
//        }
//        gl4.glUseProgram(0);
//
//        programs[Program.ubo.ordinal()] = new ProgramUbo(gl4, shaderFilepath, "VSubo.glsl", "FS.glsl");
//
//        programs[Program.uniform.ordinal()] = new ProgramUniform(gl4, shaderFilepath, "VSuniform.glsl", "FS.glsl");
//        gl4.glUseProgram(programs[Program.uniform.ordinal()].getProgramId());
//        {
//            gl4.glUniformMatrix4fv(((ProgramUniform) programs[Program.uniform.ordinal()]).getModelToClipUL(),
//                    1, false, modelToClip);
//        }
//        gl4.glUseProgram(0);
    }
    
    private void initTextures() {
        
        ByteBuffer byteBuffer = BufferUtils.createByteBuffer(Texture.size.ordinal() * Integer.BYTES);
        glGenTextures(Texture.size.ordinal(), byteBuffer);
        textures = byteBuffer.asIntBuffer();
        
        float[] color = new float[]{0f, .66f, .33f, 1f};
        FloatBuffer colorBuffer = BufferUtils.createFloatBuffer(windowSize.x * windowSize.y * 4);
        for (int p = 0; p < (windowSize.x * windowSize.y); p++) {
            colorBuffer.put(color);
        }
        colorBuffer.flip();
        
        for (int t = 0; t < 2; t++) {
            
            glBindTexture(GL_TEXTURE_RECTANGLE, textures.get(t));
            {
                glTexImage2D(GL_TEXTURE_RECTANGLE, 0, GL_RGBA, windowSize.x, windowSize.y,
                        0, GL_RGBA, GL_FLOAT, colorBuffer);
                
                glTexParameteri(GL_TEXTURE_RECTANGLE, GL12.GL_TEXTURE_BASE_LEVEL, 0);
                glTexParameteri(GL_TEXTURE_RECTANGLE, GL12.GL_TEXTURE_MAX_LEVEL, 0);
                
                glTexParameteri(GL_TEXTURE_RECTANGLE, GL_TEXTURE_WRAP_S, GL12.GL_CLAMP_TO_EDGE);
                glTexParameteri(GL_TEXTURE_RECTANGLE, GL_TEXTURE_WRAP_T, GL12.GL_CLAMP_TO_EDGE);
                glTexParameteri(GL_TEXTURE_RECTANGLE, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
                glTexParameteri(GL_TEXTURE_RECTANGLE, GL_TEXTURE_MAG_FILTER, GL_NEAREST);
            }
            glBindTexture(GL2GL3.GL_TEXTURE_RECTANGLE, 0);
        }
    }
    
    private void initTargets() {
        
        ByteBuffer byteBuffer = BufferUtils.createByteBuffer(Program.size.ordinal() * Integer.BYTES);
        GL30.glGenFramebuffers(Program.size.ordinal(), byteBuffer);
        fbos = byteBuffer.asIntBuffer();
        
        for (int framebuffer = 0; framebuffer < Program.size.ordinal(); framebuffer++) {
            
            glBindTexture(GL2GL3.GL_TEXTURE_RECTANGLE, textures.get(Texture.color.ordinal()));
            
            glTexImage2D(GL2GL3.GL_TEXTURE_RECTANGLE, 0, GL_RGBA, windowSize.x, windowSize.y,
                    0, GL_RGBA, GL_FLOAT, (ByteBuffer) null);
            
            glTexParameteri(GL2GL3.GL_TEXTURE_RECTANGLE, GL2ES3.GL_TEXTURE_BASE_LEVEL, 0);
            glTexParameteri(GL2GL3.GL_TEXTURE_RECTANGLE, GL2GL3.GL_TEXTURE_MAX_LEVEL, 0);
            
            glBindTexture(GL2GL3.GL_TEXTURE_RECTANGLE, textures.get(Texture.depth.ordinal()));
            
            glTexImage2D(GL2GL3.GL_TEXTURE_RECTANGLE, 0, GL_DEPTH_COMPONENT, windowSize.x,
                    windowSize.y, 0, GL_DEPTH_COMPONENT, GL_FLOAT, (ByteBuffer) null);
            
            glTexParameteri(GL2GL3.GL_TEXTURE_RECTANGLE, GL2ES3.GL_TEXTURE_BASE_LEVEL, 0);
            glTexParameteri(GL2GL3.GL_TEXTURE_RECTANGLE, GL2ES3.GL_TEXTURE_MAX_LEVEL, 0);
            
            GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, fbos.get(framebuffer));
            
            GL30.glFramebufferTexture2D(GL30.GL_FRAMEBUFFER, GL30.GL_DEPTH_ATTACHMENT,
                    GL2GL3.GL_TEXTURE_RECTANGLE, textures.get(Texture.depth.ordinal()), 0);
            GL30.glFramebufferTexture2D(GL30.GL_FRAMEBUFFER, GL30.GL_COLOR_ATTACHMENT0,
                    GL2GL3.GL_TEXTURE_RECTANGLE, textures.get(Texture.color.ordinal()), 0);
        }
    }
    
    private void initVbo() {
        
        float[] vertexAttributes = new float[]{0f, 0f, 0f};
        
        objects.put(Objects.vbo.ordinal(), GL15.glGenBuffers());
        
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, objects.get(Objects.vbo.ordinal()));
        {
            FloatBuffer buffer = GLBuffers.newDirectFloatBuffer(vertexAttributes);
            
            GL15.glBufferData(GL15.GL_ARRAY_BUFFER, buffer, GL15.GL_STATIC_DRAW);
        }
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);
    }
    
    private void initVao() {
        
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, objects.get(Objects.vbo.ordinal()));
        
        objects.put(Objects.vao.ordinal(), GL30.glGenVertexArrays());
        GL30.glBindVertexArray(objects.get(Objects.vao.ordinal()));
        {
            GL20.glEnableVertexAttribArray(0);
            {
                GL20.glVertexAttribPointer(0, 3, GL_FLOAT, false, 0, 0);
            }
        }
        GL30.glBindVertexArray(0);
    }
    
    private void loop() {
        // This line is critical for LWJGL's interoperation with GLFW's
        // OpenGL context, or any context that is managed externally.
        // LWJGL detects the context that is current in the current thread,
        // creates the ContextCapabilities instance and makes the OpenGL
        // bindings available for use.
//        GLContext.createFromCurrent();

        // Set the clear color
//        glClearColor(1.0f, 0.0f, 0.0f, 0.0f);
        stateChange = StateChange.RenderTarget;
        repeat = 10_000;
        
        fpsSout = 10;
        
        long now = getTime();
        startTime = now;
        lastTime = now;

        // Run the rendering loop until the user has attempted to close
        // the window or has pressed the ESCAPE key.
        while (glfwWindowShouldClose(window) == GL_FALSE) {
            
            for (int i = 0; i < repeat; i++) {
                GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, fbos.get(i % 2));
                glDrawArrays(GL_POINTS, 0, 1);
                GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, 0);
            }
            updateFPS();
            glfwSwapBuffers(window); // swap the color buffers
            // Poll for window events. The key callback above will only be
            // invoked during this call.
            glfwPollEvents();
        }
    }
    
    public void updateFPS() {
        if (totalFrames % fpsSout == 0 && totalFrames != 0) {
            
            long now = getTime();
            long deltaTime = now - lastTime;
            float totalTime = now - startTime;
            float deltaFps = (float) deltaFrames / ((float) deltaTime / 1_000);
            String deltaFpsS = String.valueOf(deltaFps);
            float deltaFrameTime = (float) deltaTime / (float) deltaFrames;
            float totalFps = (float) totalFrames / (totalTime / 1_000);
            String totalFpsS = String.valueOf(totalFps);
            float totalFrameTime = totalTime / (float) totalFrames;
            String totalFrameTimeS = String.valueOf(totalFrameTime);
            System.out.println(((int) totalTime / 1_000) + " s: " + fpsSout + " f / " + deltaTime + " ms, "
                    + deltaFpsS.substring(0, deltaFpsS.indexOf(".") + 2) + " fps, " + deltaFrameTime
                    + " ms/f; total: " + totalFrames + " f, " + totalFpsS.substring(0, totalFpsS.indexOf(".") + 2)
                    + " fps, " + totalFrameTimeS.substring(0, totalFrameTimeS.indexOf(".") + 2) + " ms/f");
            lastTime = now;
            deltaFrames = 0;
        }
        deltaFrames++;
        totalFrames++;
    }
    
    public long getTime() {
        return System.nanoTime() / 1_000_000;
    }
    
    public static void main(String[] args) {
        new Lwjgl().run();
    }
    
}
