package main.glOverhead

import glm_.max
import glm_.vec2.Vec2i
import gln.glViewport
import org.lwjgl.opengl.ARBDebugOutput.GL_DEBUG_OUTPUT_SYNCHRONOUS_ARB
import org.lwjgl.opengl.ARBDebugOutput.glDebugMessageCallbackARB
import org.lwjgl.opengl.GL
import org.lwjgl.opengl.GL11C.glEnable
import org.lwjgl.opengl.GL11C.glViewport
import org.lwjgl.opengl.GLCapabilities
import org.lwjgl.opengl.GLDebugMessageARBCallbackI
import org.lwjgl.opengl.GLUtil
import org.lwjgl.system.MemoryUtil.NULL
import uno.glfw.GlfwWindow
import uno.glfw.glfw


//------------screen
var fullScreen = true
val screenSize = Vec2i(640, 480)

//------------demo settings
var openglDebugMode = true

//double last_test_time = 0.0;
//double last_gpu_test_time = 0.0;
//
//int frame_index = 0;
//int current_frame_index = 0;
//int next_frame_index = 0;
//
//int NUM_ITERATIONS = 1;
//
//const int MAX_INSTANCES = 2000;
//int CURRENT_NUM_INSTANCES = MAX_INSTANCES;
//const int MAX_RANDOM_COLORS = 1000;
//const int NUM_UNIFORM_CHANGES_PER_DIP = 10;
//int NUM_FBO_CHANGES = 200;
//const int NUM_DIFFERENT_FBOS = 4;
//const int NUM_SIMPLE_VERTEX_BUFFERS = 4;
//const int NUM_TEXTURES_IN_COMPLEX_MATERIAL = 6;
//const int TEX_ARRAY_SIZE = 10;
//
//const int PER_INSTANCE_DATA_VECTORS = 2; //number of vec4 (pos + color)
//const int INSTANCES_DATA_VECTORS = MAX_INSTANCES * PER_INSTANCE_DATA_VECTORS;
//const int INSTANCES_DATA_SIZE = INSTANCES_DATA_VECTORS * sizeof(vec4);
//
//const int UNIFORMS_INSTANCING_MAX_CONSTANTS_FOR_INSTANCING = 100; // 72;
//const int UNIFORMS_INSTANCING_OBJECTS_PER_DIP = UNIFORMS_INSTANCING_MAX_CONSTANTS_FOR_INSTANCING / PER_INSTANCE_DATA_VECTORS; //max 72 constants, 2 constants per object
//int UNIFORMS_INSTANCING_NUM_GROUPS = MAX_INSTANCES / UNIFORMS_INSTANCING_OBJECTS_PER_DIP;

lateinit var caps: GLCapabilities

fun main() {

    fullScreen = false

    glfw.init()
    glfw.windowHint { debug = openglDebugMode }

    val window = GlfwWindow(screenSize, "OpenGL API overhead")

    caps = GL.getCapabilities()

    if (openglDebugMode && caps.glGetDebugMessageLogARB != NULL) { // use debug
        GLUtil.setupDebugMessageCallback()
        glEnable(GL_DEBUG_OUTPUT_SYNCHRONOUS_ARB)
    }

    sizeOpenGLScreen()	// Setup the screen translations and viewport
}

fun sizeOpenGLScreen(){
    screenSize.y = screenSize.y max 1
    glViewport(screenSize)
}