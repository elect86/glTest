package callOverhead.gl3;

import callOverhead.Resource;
import callOverhead.Semantic;
import glm.mat._4.Mat4;
import glm.vec._2.Vec2;
import glm.vec._2.i.Vec2i;
import glm.vec._4.Vec4;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import org.lwjgl.*;
import org.lwjgl.glfw.*;
import org.lwjgl.opengl.*;

import static org.lwjgl.glfw.Callbacks.*;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL12.GL_CLAMP_TO_EDGE;
import static org.lwjgl.opengl.GL12.GL_TEXTURE_BASE_LEVEL;
import static org.lwjgl.opengl.GL12.GL_TEXTURE_MAX_LEVEL;
import static org.lwjgl.opengl.GL13.GL_TEXTURE0;
import static org.lwjgl.opengl.GL15.GL_ARRAY_BUFFER;
import static org.lwjgl.opengl.GL15.GL_DYNAMIC_DRAW;
import static org.lwjgl.opengl.GL15.GL_STATIC_DRAW;
import static org.lwjgl.opengl.GL20.GL_FRAGMENT_SHADER;
import static org.lwjgl.opengl.GL20.GL_VERTEX_SHADER;
import static org.lwjgl.opengl.GL30.GL_COLOR_ATTACHMENT0;
import static org.lwjgl.opengl.GL30.GL_DEPTH_ATTACHMENT;
import static org.lwjgl.opengl.GL30.GL_DEPTH_COMPONENT32F;
import static org.lwjgl.opengl.GL30.GL_FRAMEBUFFER;
import static org.lwjgl.opengl.GL31.GL_TEXTURE_RECTANGLE;
import static org.lwjgl.opengl.GL31.GL_UNIFORM_BUFFER;
import static org.lwjgl.system.MemoryUtil.*;

public class Lwjgl {

    public static void main(String[] args) {
        new Lwjgl().run();
    }

    // We need to strongly reference callback instances.
    private GLFWErrorCallback errorCallback;
    private GLFWKeyCallback keyCallback;

    // The window handle
    private long window;
    private Vec2i windowSize;

    private int[] programName = new int[Resource.Program.MAX];
    private IntBuffer framebufferName = BufferUtils.createIntBuffer(Resource.Framebuffer.MAX),
            bufferName = BufferUtils.createIntBuffer(Resource.Buffer.MAX),
            vertexArrayName = BufferUtils.createIntBuffer(1),
            textureName = BufferUtils.createIntBuffer(Resource.Texture.MAX);
    private int repeat, mode, frames, updateInterval_ms = 1_000, uniformLocation;
    private long cpuStart_ns, cpuTotal_ns, updateStart_ms;
    private boolean update = true;

    public void run() {
        System.out.println("Lwjgl " + Version.getVersion() + "!");

        try {
            init();
            loop();

            // Free the window callbacks and destroy the window
            glfwFreeCallbacks(window);
            glfwDestroyWindow(window);
        } finally {
            // Terminate GLFW and free the error callback
            glfwTerminate();
            glfwSetErrorCallback(null).free();
        }
    }

    private void init() {
        // Setup an error callback. The default implementation
        // will print the error message in System.err.
        GLFWErrorCallback.createPrint(System.err).set();

        // Initialize GLFW. Most GLFW functions will not work before doing this.
        if (!glfwInit()) {
            throw new IllegalStateException("Unable to initialize GLFW");
        }

        // Configure our window
        glfwDefaultWindowHints(); // optional, the current window hints are already the default
        glfwWindowHint(GLFW_VISIBLE, GL_FALSE); // the window will stay hidden after creation
        glfwWindowHint(GLFW_RESIZABLE, GL_TRUE); // the window will be resizable

        windowSize = new Vec2i(1024, 768);

        // Create the window
        window = glfwCreateWindow(windowSize.x, windowSize.y, "LWJGL - GL Call Overhead", NULL, NULL);
        if (window == NULL) {
            throw new RuntimeException("Failed to create the GLFW window");
        }

        // Setup a key callback. It will be called every time a key is pressed, repeated or released.
        glfwSetKeyCallback(window, (window_, key, scancode, action, mods) -> {
            if (key == GLFW_KEY_ESCAPE && action == GLFW_RELEASE) {
                glfwSetWindowShouldClose(window_, true); // We will detect this in our rendering loop
            }
        });

        // Get the resolution of the primary monitor
        GLFWVidMode vidmode = glfwGetVideoMode(glfwGetPrimaryMonitor());
        // Center our window
        glfwSetWindowPos(
                window,
                (vidmode.width() - windowSize.x) / 2,
                (vidmode.height() - windowSize.y) / 2
        );

        // Make the OpenGL context current
        glfwMakeContextCurrent(window);
        // Disable v-sync
        glfwSwapInterval(0);

        // Make the window visible
        glfwShowWindow(window);

        initPrograms();
        initTextures();
        initFramebuffers();
        initBuffers();
        initVertexArray();

        mode = Semantic.Mode.FRAMEBUFFER;

        Resource.printHelp();
        Resource.printStateChange(mode);
    }

    private void initPrograms() {

        for (int i = Resource.Program.A; i < Resource.Program.MAX; i++) {

            int vs = loadShader(Resource.SHADERS_ROOT + Resource.VERT_SHADERS_SOURCE[i] + ".vert", GL_VERTEX_SHADER);
            int fs = loadShader(Resource.SHADERS_ROOT + Resource.FRAG_SHADERS_SOURCE[i] + ".frag", GL_FRAGMENT_SHADER);

            programName[i] = GL20.glCreateProgram();

            GL20.glAttachShader(programName[i], vs);
            GL20.glAttachShader(programName[i], fs);

            GL20.glLinkProgram(programName[i]);
            GL20.glValidateProgram(programName[i]);
        }

        GL20.glUseProgram(programName[Resource.Program.TEXTURE]);
        GL20.glUniform1i(
                GL20.glGetUniformLocation(programName[Resource.Program.TEXTURE], "texture0"),
                Semantic.Sampler.TEXTURE0);

        GL31.glUniformBlockBinding(programName[Resource.Program.UBO],
                GL31.glGetUniformBlockIndex(programName[Resource.Program.UBO], "Transform0"),
                Semantic.Uniform.TRANSFORM0);

        uniformLocation = GL20.glGetUniformLocation(programName[Resource.Program.UNIFORM], "z");
    }

    private int loadShader(String filename, int type) {
        StringBuilder shaderSource = new StringBuilder();
        int shaderID;

        try {
            try (BufferedReader reader = new BufferedReader(new FileReader(filename))) {
                String line = reader.readLine();
                while (line != null) {
                    shaderSource.append(line).append("\n");
                    line = reader.readLine();
                }
            }
        } catch (IOException e) {
            System.err.println("Could not read file.");
            System.exit(-1);
        }

        shaderID = GL20.glCreateShader(type);
        GL20.glShaderSource(shaderID, shaderSource);
        GL20.glCompileShader(shaderID);

        return shaderID;
    }

    private void initTextures() {

        GL11.glGenTextures(textureName);

        float[] texColor = new float[]{0f, .5f, 1f, 1f};
        FloatBuffer colorBuffer = BufferUtils.createFloatBuffer(windowSize.x * windowSize.y * 4);
        for (int i = 0; i < (windowSize.x * windowSize.y); i++) {
            colorBuffer.put(texColor);
        }
        colorBuffer.rewind();

        for (int i = Resource.Texture.A; i <= Resource.Texture.B; i++) {

            GL11.glBindTexture(GL_TEXTURE_RECTANGLE, textureName.get(i));

            GL11.glTexImage2D(GL_TEXTURE_RECTANGLE, 0, GL_RGBA8, windowSize.x, windowSize.y, 0, GL_RGBA, GL_FLOAT,
                    colorBuffer);

            GL11.glTexParameteri(GL_TEXTURE_RECTANGLE, GL_TEXTURE_BASE_LEVEL, 0);
            GL11.glTexParameteri(GL_TEXTURE_RECTANGLE, GL_TEXTURE_MAX_LEVEL, 0);

            GL11.glTexParameteri(GL_TEXTURE_RECTANGLE, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
            GL11.glTexParameteri(GL_TEXTURE_RECTANGLE, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);
            GL11.glTexParameteri(GL_TEXTURE_RECTANGLE, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
            GL11.glTexParameteri(GL_TEXTURE_RECTANGLE, GL_TEXTURE_MAG_FILTER, GL_NEAREST);
        }
        GL11.glBindTexture(GL_TEXTURE_2D, textureName.get(Resource.Texture.COLOR));
        GL11.glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA8, windowSize.x, windowSize.y, 0, GL_RGBA, GL_FLOAT, (float[]) null);

        GL11.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_BASE_LEVEL, 0);
        GL11.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAX_LEVEL, 0);

        GL11.glBindTexture(GL_TEXTURE_2D, textureName.get(Resource.Texture.DEPTH));
        GL11.glTexImage2D(GL_TEXTURE_2D, 0, GL_DEPTH_COMPONENT32F, windowSize.x, windowSize.y, 0, GL_DEPTH_COMPONENT,
                GL_FLOAT, (float[]) null);

        GL11.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_BASE_LEVEL, 0);
        GL11.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAX_LEVEL, 0);
    }

    private void initFramebuffers() {

        GL30.glGenFramebuffers(framebufferName);

        for (int i = 0; i < Resource.Framebuffer.MAX; i++) {

            GL30.glBindFramebuffer(GL_FRAMEBUFFER, framebufferName.get(i));

            GL30.glFramebufferTexture2D(GL_FRAMEBUFFER, GL_DEPTH_ATTACHMENT, GL_TEXTURE_2D,
                    textureName.get(Resource.Texture.DEPTH), 0);
            GL30.glFramebufferTexture2D(GL_FRAMEBUFFER, GL_COLOR_ATTACHMENT0, GL_TEXTURE_2D,
                    textureName.get(Resource.Texture.COLOR), 0);
        }
    }

    private void initBuffers() {

        FloatBuffer vertexBuffer = BufferUtils.createFloatBuffer(2);
        vertexBuffer.put(0, 0).put(0, 0);
        ByteBuffer transformBuffer = BufferUtils.createByteBuffer(Mat4.SIZE);
        ByteBuffer vec4Buffer = BufferUtils.createByteBuffer(Vec4.SIZE);

        GL15.glGenBuffers(bufferName);

        GL15.glBindBuffer(GL_ARRAY_BUFFER, bufferName.get(Resource.Buffer.ARRAY));
        GL15.glBufferData(GL_ARRAY_BUFFER, vertexBuffer, GL_STATIC_DRAW);

        for (int i = Resource.Buffer.A; i <= Resource.Buffer.B; i++) {

            GL15.glBindBuffer(GL_UNIFORM_BUFFER, bufferName.get(Resource.Buffer.A + i));
            GL15.glBufferData(GL_UNIFORM_BUFFER, vec4Buffer, GL_DYNAMIC_DRAW);
        }

        new Mat4(1).toDbb(transformBuffer);
        GL15.glBindBuffer(GL_UNIFORM_BUFFER, bufferName.get(Resource.Buffer.TRANSFORM));
        GL15.glBufferData(GL_UNIFORM_BUFFER, transformBuffer, GL_DYNAMIC_DRAW);

        common.BufferUtils.destroyDirectBuffer(vertexBuffer);
        common.BufferUtils.destroyDirectBuffer(transformBuffer);
        common.BufferUtils.destroyDirectBuffer(vec4Buffer);
    }

    private void initVertexArray() {

        GL30.glGenVertexArrays(vertexArrayName);
        GL30.glBindVertexArray(vertexArrayName.get(0));

        GL15.glBindBuffer(GL_ARRAY_BUFFER, bufferName.get(Resource.Buffer.ARRAY));
        GL20.glEnableVertexAttribArray(Semantic.Attr.POSITION);
        GL20.glVertexAttribPointer(Semantic.Attr.POSITION, 2, GL_FLOAT, false, Vec2.SIZE, 0);
    }

    private void loop() {
        // This line is critical for LWJGL's interoperation with GLFW's
        // OpenGL context, or any context that is managed externally.
        // LWJGL detects the context that is current in the current thread,
        // creates the ContextCapabilities instance and makes the OpenGL
        // bindings available for use.
        GL.createCapabilities();

        // Run the rendering loop until the user has attempted to close
        // the window or has pressed the ESCAPE key.
        while (!glfwWindowShouldClose(window)) {

            if (update) {
                update = false;
                update();
            }

            cpuStart_ns = System.nanoTime();

            switch (mode) {

                case Semantic.Mode.FRAMEBUFFER:

                    for (int i = 0; i < repeat; i++) {
                        GL30.glBindFramebuffer(GL_FRAMEBUFFER, framebufferName.get(i % 2));
                        GL11.glDrawArrays(GL_POINTS, 0, 1);
//                    gl3.glBindFramebuffer(GL_FRAMEBUFFER, 0);
                    }
                    break;

                case Semantic.Mode.PROGRAM:

                    for (int i = 0; i < repeat; i++) {
                        GL20.glUseProgram(programName[Resource.Program.A + i % 2]);
                        GL11.glDrawArrays(GL_POINTS, 0, 1);
//                    gl3.glUseProgram(0);
                    }
                    break;
//
                case Semantic.Mode.TEXTURE:

                    for (int i = 0; i < repeat; i++) {
                        GL13.glActiveTexture(GL_TEXTURE0 + i % 2);
                        GL11.glBindTexture(GL_TEXTURE_RECTANGLE, textureName.get(Resource.Texture.A + i % 2));
                        GL11.glDrawArrays(GL_POINTS, 0, 1);
//                    gl3.glBindTexture(GL_TEXTURE_RECTANGLE, 0);
                    }
                    break;

                case Semantic.Mode.VERTEX_FORMAT:

                    for (int i = 0; i < repeat; i++) {
                        GL20.glEnableVertexAttribArray(Semantic.Attr.POSITION);
                        GL20.glVertexAttribPointer(Semantic.Attr.POSITION, 2, GL_FLOAT, false, Vec2.SIZE, 0);
                        GL11.glDrawArrays(GL_POINTS, 0, 1);
//                    gl3.glDisableVertexAttribArray(Semantic.Attr.POSITION);
                    }
                    break;

                case Semantic.Mode.UBO:

                    for (int i = 0; i < repeat; i++) {
                        GL15.glBindBuffer(GL_UNIFORM_BUFFER, bufferName.get(Resource.Buffer.TRANSFORM));
                        GL30.glBindBufferRange(GL_UNIFORM_BUFFER, Semantic.Uniform.TRANSFORM0,
                                bufferName.get(Resource.Buffer.TRANSFORM), 0, Mat4.SIZE);
                        GL11.glDrawArrays(GL_POINTS, 0, 1);
//                    gl3.glBindBuffer(GL_UNIFORM_BUFFER, 0);
                    }
                    break;

                case Semantic.Mode.VERTEX_BINDING:

                    for (int i = 0; i < repeat; i++) {
                        GL15.glBindBuffer(GL_ARRAY_BUFFER, bufferName.get(Resource.Buffer.ARRAY));
                        GL20.glVertexAttribPointer(Semantic.Attr.POSITION, 2, GL_FLOAT, false, Vec2.SIZE, 0);
                        GL11.glDrawArrays(GL_POINTS, 0, 1);
//                    gl3.glBindBuffer(GL_ARRAY_BUFFER, 0);
                    }
                    break;

                case Semantic.Mode.UNIFORM:

                    for (int i = 0; i < repeat; i++) {
                        GL20.glUniform1f(uniformLocation, 0);
                        GL11.glDrawArrays(GL_POINTS, 0, 1);
                    }
                    break;
            }

            cpuTotal_ns += System.nanoTime() - cpuStart_ns;

            frames++;

            if ((System.currentTimeMillis() - updateStart_ms) > updateInterval_ms) {

                int totalSwitches = repeat * frames * 1;

                String switchesPerS = String.format("%,.0f", totalSwitches / ((double) cpuTotal_ns / 1_000_000_000));
                System.out.println("switches per seconds: " + switchesPerS);

                resetCounters();
                updateStart_ms = System.currentTimeMillis();
            }

            glfwSwapBuffers(window); // swap the color buffers
            // Poll for window events. The key callback above will only be
            // invoked during this call.
            glfwPollEvents();
        }
    }

    private void update() {

        GL30.glBindVertexArray(vertexArrayName.get(0));
        GL20.glUseProgram(programName[Resource.Program.A]);
        GL30.glBindFramebuffer(GL_FRAMEBUFFER, 0);

        switch (mode) {

            case Semantic.Mode.FRAMEBUFFER:
                repeat = 1_000;
                break;

            case Semantic.Mode.PROGRAM:
                repeat = 10_000;
                break;

            case Semantic.Mode.TEXTURE:
                GL20.glUseProgram(programName[Resource.Program.TEXTURE]);
                repeat = 100_000;
                break;

            case Semantic.Mode.VERTEX_FORMAT:
                repeat = 1_000_000;
                break;

            case Semantic.Mode.UBO:
                GL20.glUseProgram(programName[Resource.Program.UBO]);
                repeat = 1_000_000;
                break;

            case Semantic.Mode.VERTEX_BINDING:
                repeat = 1_000_000;
                break;

            case Semantic.Mode.UNIFORM:
                GL20.glUseProgram(programName[Resource.Program.UNIFORM]);
                repeat = 1_000_000;
                break;
        }
        resetCounters();
    }

    private void resetCounters() {
        frames = 0;
        cpuTotal_ns = 0;
    }
}
