package main.apiTest.framework

import glm_.vec2.Vec2i
import glm_.vec4.Vec4
import gln.glClearColor
import gln.glViewport
import gln.vertexArray.glBindVertexArray
import kool.IntBuffer
import org.lwjgl.opengl.GL
import org.lwjgl.opengl.GL11C
import org.lwjgl.opengl.GL20C.GL_SHADING_LANGUAGE_VERSION
import org.lwjgl.opengl.GL30C.*
import org.lwjgl.opengl.GL41C.glClearDepthf
import org.lwjgl.opengl.GL43C.*
import org.lwjgl.opengl.GLCapabilities
import org.lwjgl.opengl.GLUtil
import org.lwjgl.system.Callback
import org.lwjgl.system.MemoryUtil.NULL
import uno.glfw.GlfwWindow
import uno.glfw.VSync
import uno.glfw.glfw
import uno.glfw.windowHint.Profile
import java.nio.IntBuffer


const val doubleBuffer = 2
const val tripleBuffer = 3

enum class OpenGlApi {
    Generic, Core;

    /** Returns true if:
     *  - compat is false and _api is any variant of OpenGL (including compatibility)
     *  - compat is true and _api is OpenGL compatibility profile, false otherwise     */
    fun isOpenGL(compat: Boolean = false): Boolean = when (this) {
        Generic -> true
        else -> !compat
    }
}

lateinit var caps: GLCapabilities

abstract class OpenGLBase {

    var titleRoot = ""
    val size = Vec2i(1)
    lateinit var wnd: GlfwWindow
    var errorCallback: Callback? = null

    fun destroy() {}

    open fun init(title: String, pos: Vec2i, size: Vec2i): Boolean {

        glfw.windowHint {
            debug = DEBUG
            visible = false
        }

        titleRoot = title
        this.size put size

        try {
            wnd = GlfwWindow(size, title, position = pos)
        } catch (exc: RuntimeException) {
            println("Unable to create GlfwWindow, which is required for GL.")
            return false
        }

        wnd.keyCallback = keyCallback

        wnd.makeContextCurrent()

        checkExtensions()

        println("""
            GL created successfully! Info follows.
            Vendor: ${glGetString(GL_VENDOR)}
            Renderer: ${glGetString(GL_RENDERER)}
            Version: ${glGetString(GL_VERSION)}
            Shading Language Version: ${glGetString(GL_SHADING_LANGUAGE_VERSION)}""")

        glfw.swapInterval = VSync.OFF

        // Default GL State
        glCullFace(GL_FRONT)
        glEnable(GL_CULL_FACE)
        glDisable(GL_SCISSOR_TEST)
        glEnable(GL_DEPTH_TEST)
        glDepthMask(true)
        glDepthFunc(GL_LESS)
        glDisable(GL_BLEND)
        glColorMask(true, true, true, true)

        if (DEBUG) {
            glDebugMessageControl(GL_DONT_CARE, GL_DONT_CARE, GL_DONT_CARE, null as IntBuffer?, true)
            glDebugMessageControl(GL_DONT_CARE, GL_DONT_CARE, GL_DEBUG_SEVERITY_NOTIFICATION, null as IntBuffer?, false)
//            glDebugMessageControl(GL_DEBUG_SOURCE_API, GL_DEBUG_TYPE_OTHER, GL_DEBUG_SEVERITY_LOW, 0, false)
            errorCallback = GLUtil.setupDebugMessageCallback()
            glEnable(GL_DEBUG_OUTPUT)
        }

        return true
    }

    fun checkExtensions() {

        caps = GL.createCapabilities()
        // Build a list of strings of extensions we care about.
        val myExtensions = arrayOf(
                "ARB_base_instance", "ARB_bindless_texture", "ARB_buffer_storage", "ARB_debug_output",
                "ARB_internalformat_query", "ARB_map_buffer_range", "ARB_multi_draw_indirect",
                "ARB_shader_draw_parameters", "ARB_shader_storage_buffer_object", "ARB_sync",
                "ARB_sparse_texture", "ARB_vertex_array_object", "ARB_texture_storage", "ARB_timer_query",
                "ARB_uniform_buffer_object", "EXT_texture_array", "NV_shader_buffer_load",
                "NV_vertex_buffer_unified_memory", "NV_bindless_multi_draw_indirect")

        val extensions = Array(glGetInteger(GL_NUM_EXTENSIONS)) { glGetStringi(GL_EXTENSIONS, it)!! }

        myExtensions.forEach { ext ->
            if ("GL_$ext" !in extensions)
                System.err.println("$ext not available")
        }
    }

    open fun shutdown() {
        wnd.unmakeContextCurrent()
        wnd.destroy()
    }

    fun activate() {
        println(getLongName() + ", activate")
        wnd.makeContextCurrent()
        wnd.show()
    }

    fun deactivate() {
        wnd.show(false)
        wnd.unmakeContextCurrent()
    }

    fun clear(clearColor: Vec4, clearDepth: Float) {
        // TODO: This should go elsewhere.
        glViewport(size)

        glClearColor(clearColor)
        glClearDepthf(clearDepth)
        glClear(GL_COLOR_BUFFER_BIT or GL_DEPTH_BUFFER_BIT)

        glGetError()
    }

    fun swapBuffers() = wnd.swapBuffers()

    abstract val apiType: OpenGlApi

    fun onResize(newSize: Vec2i) = size.put(newSize)

    fun onProblemOrSolutionSet(problemName: String, solutionName: String) {
        wnd.title = "$titleRoot - $problemName - $solutionName"
    }

    fun moveWindow(newPos: Vec2i) {
        wnd.pos = newPos
    }

    abstract fun getShortName(): String
    abstract fun getLongName(): String
}

/** This implementation is just whatever we get from the platform by default, without asking for anything specific
 *  ie, we're not asking for Core or Compatibility. See the log output to determine what type was actually created. */
class OpenGLGeneric : OpenGLBase() {

    override val apiType get() = OpenGlApi.Generic

    override fun getShortName() = shortName
    override fun getLongName() = longName

    companion object {
        val shortName get() = "oglgeneric"
        val longName get() = "OpenGL (Generic)"
    }
}

/** For this implementation, we ask explicitly for a Core Context. */
class OpenGLCore : OpenGLBase() {

    val vertexArrayObject = IntBuffer(1)

    override fun init(title: String, pos: Vec2i, size: Vec2i): Boolean {

        // These must be set before we call the base class.
        glfw.windowHint {
            context.version = "3.2"
            profile = Profile.core
        }

        if (!super.init(title, pos, size))
            return false

        // Now that we have something valid, create our VAO and bind it. Ugh! So lame that this is required.
        glGenVertexArrays(vertexArrayObject)
        glBindVertexArray(vertexArrayObject)

        return true
    }

    override fun shutdown() {
        // Must cleanup before we call base class.
        glBindVertexArray()
        glDeleteVertexArrays(vertexArrayObject)

        super.shutdown()
    }

    override val apiType get() = OpenGlApi.Core

    override fun getShortName() = shortName
    override fun getLongName() = longName

    companion object {
        val shortName get() = "oglcore"
        val longName get() = "OpenGL (Core)"
    }
}