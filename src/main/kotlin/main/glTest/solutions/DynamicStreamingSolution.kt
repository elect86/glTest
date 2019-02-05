package main.glTest.solutions

import gli_.memCopy
import glm_.L
import glm_.i
import glm_.vec2.Vec2
import glm_.vec4.Vec4
import gln.glViewport
import gln.glf.glf
import gln.glf.semantic
import gln.program.GlslProgram
import gln.vertexArray.glBindVertexArray
import gln.vertexArray.glDisableVertexAttribArray
import gln.vertexArray.glEnableVertexAttribArray
import gln.vertexArray.glVertexAttribPointer
import kool.*
import main.*
import main.glTest.framework.BufferLockManager
import main.glTest.framework.OpenGlApi
import main.glTest.framework.tripleBuffer
import main.glTest.problems.Vec2Buffer
import main.glTest.problems.vertsPerParticle
import org.lwjgl.opengl.GL
import org.lwjgl.opengl.GL20C.glUseProgram
import org.lwjgl.opengl.GL31C.GL_UNIFORM_BUFFER
import org.lwjgl.opengl.GL44C.*
import org.lwjgl.system.MemoryUtil.NULL
import java.nio.ByteBuffer

abstract class DynamicStreamingSolution : Solution() {

    val constants = FloatBuffer(Vec4.length)

    val uniformBuffer = IntBuffer(1)
    val vertexBuffer = IntBuffer(1)

    var program = 0

    val vao = IntBuffer(1)

    var startDestOffset = 0L
    var particleBufferSize = 0L

    open fun init(maxVertexCount: Int): Boolean {

        glGenBuffers(uniformBuffer, vertexBuffer)

        // Program
        program = GlslProgram.fromRoot("shaders", "dynamic-streaming").name

        if (program == 0) {
            System.err.println("Unable to initialize solution '$name', shader compilation/linking failed.")
            return false
        }
        return true
    }

    fun setCommonGlState() {

        // Program
        glUseProgram(program)

        // Uniforms
        constants[0] = 2f / size.x
        constants[1] = -2f / size.y

        glBindBuffer(GL_UNIFORM_BUFFER, uniformBuffer)
        glBufferData(GL_UNIFORM_BUFFER, constants, GL_DYNAMIC_DRAW)
        glBindBufferBase(GL_UNIFORM_BUFFER, semantic.uniform.CONSTANT, uniformBuffer)

        // Input Layout
        glBindBuffer(GL_ARRAY_BUFFER, vertexBuffer)
        glVertexAttribPointer(glf.pos2)
        glEnableVertexAttribArray(glf.pos2)

        // Rasterizer State
        glDisable(GL_CULL_FACE)
        glCullFace(GL_FRONT)
        glDisable(GL_SCISSOR_TEST)
        glViewport(size)

        // Blend State
        glDisable(GL_BLEND)
        glColorMask(true)

        // Depth Stencil State
        glDisable(GL_DEPTH_TEST)
        glDepthMask(false)
    }

    abstract fun render(vertices: Vec2Buffer)

    override fun shutdown() {

        glDisableVertexAttribArray(glf.pos2)
        glBindVertexArray()
        glDeleteVertexArrays(vao)

        if (glIsBuffer(vertexBuffer)) glDeleteBuffers(vertexBuffer)
        if (glIsBuffer(uniformBuffer)) glDeleteBuffers(uniformBuffer)

        glDeleteProgram(program)
    }

    abstract override val name: String

    override val problemName get() = "DynamicStreaming"

    override fun supportsApi(api: OpenGlApi) = api.isOpenGL()
}

class DynamicStreamingGLBufferSubData : DynamicStreamingSolution() {

    override fun init(maxVertexCount: Int): Boolean {

        if (!super.init(maxVertexCount)) return false

        // Dynamic vertex buffer
        particleBufferSize = tripleBuffer * Vec2.size * maxVertexCount.L

        glBindBuffer(GL_ARRAY_BUFFER, vertexBuffer)
        glBufferData(GL_ARRAY_BUFFER, particleBufferSize, GL_DYNAMIC_DRAW)

        glGenVertexArrays(vao)
        glBindVertexArray(vao)

        return glGetError() == GL_NO_ERROR
    }

    override fun render(vertices: Vec2Buffer) {

        setCommonGlState()

        val particleCount = vertices.cap / vertsPerParticle
        val particleSizeBytes = vertsPerParticle * Vec2.size
        val startIndex = startDestOffset / Vec2.size

        for (i in 0 until particleCount) {

            val vertexOffset = i * vertsPerParticle
            val srcOffset = vertexOffset
            val dstOffset = startDestOffset + i * particleSizeBytes

            nglBufferSubData(GL_ARRAY_BUFFER, dstOffset.L, particleSizeBytes.L, vertices.adr(srcOffset))

            glDrawArrays(GL_TRIANGLES, startIndex.i + vertexOffset, vertsPerParticle)
        }

        startDestOffset = (startDestOffset + particleCount * particleSizeBytes) % particleBufferSize

        if (startDestOffset == 0L)
            glBufferData(GL_ARRAY_BUFFER, particleBufferSize, GL_DYNAMIC_DRAW)
    }

    override val name get() = "GLBufferSubData"
}

class DynamicStreamingGLMapPersistent : DynamicStreamingSolution() {

    val bufferLockManager = BufferLockManager(true)

    var vertexData: ByteBuffer? = null

    override fun init(maxVertexCount: Int): Boolean {

        if (GL.getCapabilities().glBufferStorage == NULL) {
            System.err.println("Unable to initialize solution '$name', glBufferStorage() unavailable.")
            return false
        }

        if (!super.init(maxVertexCount)) return false

        // Dynamic vertex buffer
        glBindBuffer(GL_ARRAY_BUFFER, vertexBuffer)

        particleBufferSize = tripleBuffer * Vec2.size * maxVertexCount.L
        val flags = GL_MAP_WRITE_BIT or GL_MAP_PERSISTENT_BIT or GL_MAP_COHERENT_BIT
        glBufferStorage(GL_ARRAY_BUFFER, particleBufferSize, flags)
        vertexData = glMapBufferRange(GL_ARRAY_BUFFER, 0, particleBufferSize, flags)

        glGenVertexArrays(vao)
        glBindVertexArray(vao)

        return glGetError() == GL_NO_ERROR
    }

    override fun render(vertices: Vec2Buffer) {

        setCommonGlState()

        val particleCount = vertices.cap / vertsPerParticle
        val particleSizeBytes = vertsPerParticle * Vec2.size
        val startIndex = startDestOffset.i / Vec2.size

        // Need to wait for this area to become available.
        // If we've sized things properly, it will always be available right away.
        bufferLockManager.waitForAndLockRange(startDestOffset, vertices.capSize) {

            for (i in 0 until particleCount) {
                val vertexOffset = i * vertsPerParticle
                val srcOffset = vertexOffset
                val dstOffset = startDestOffset + i * particleSizeBytes

                val dst = vertexData!!.adr + dstOffset
                memCopy(vertices.adr(srcOffset), dst, particleSizeBytes)

                glDrawArrays(GL_TRIANGLES, startIndex + vertexOffset, vertsPerParticle)
            }
        } // the same area will be automatically locked at the end for the future.

        startDestOffset = (startDestOffset + particleCount * particleSizeBytes) % particleBufferSize
    }

    override fun shutdown() {

        bufferLockManager.destroy()

        glBindBuffer(GL_ARRAY_BUFFER, vertexBuffer)
        glUnmapBuffer(GL_ARRAY_BUFFER)

        super.shutdown()
    }

    override val name get() = "GLMapPersistent"
}

class DynamicStreamingGLMapUnsynchronized : DynamicStreamingSolution() {

    val bufferLockManager = BufferLockManager(true)

    override fun init(maxVertexCount: Int): Boolean {

        if (!super.init(maxVertexCount)) return false

        // Dynamic vertex buffer
        particleBufferSize = tripleBuffer * Vec2.size * maxVertexCount.L
        glGenBuffers(vertexBuffer)
        glBindBuffer(GL_ARRAY_BUFFER, vertexBuffer)
        glBufferData(GL_ARRAY_BUFFER, particleBufferSize, GL_DYNAMIC_DRAW)

        glGenVertexArrays(vao)
        glBindVertexArray(vao)

        return glGetError() == GL_NO_ERROR
    }

    override fun render(vertices: Vec2Buffer) {

        setCommonGlState()

        val particleCount = vertices.cap / vertsPerParticle
        val particleSizeBytes = vertsPerParticle * Vec2.size
        val startIndex = startDestOffset.i / Vec2.size
        val access = GL_MAP_WRITE_BIT or GL_MAP_INVALIDATE_RANGE_BIT or GL_MAP_UNSYNCHRONIZED_BIT

        bufferLockManager.waitForAndLockRange(startDestOffset, vertices.capSize) {

            for (i in 0 until particleCount) {
                val vertexOffset = i * vertsPerParticle
                val srcOffset = vertexOffset
                val dstOffset = startDestOffset + i * particleSizeBytes

                glMapBufferRange(GL_ARRAY_BUFFER, dstOffset, particleSizeBytes.L, access)?.let { dst ->

                    memCopy(vertices.adr(srcOffset), dst.adr, particleSizeBytes)
                    glUnmapBuffer(GL_ARRAY_BUFFER)

                    glDrawArrays(GL_TRIANGLES, startIndex + vertexOffset, vertsPerParticle)
                }
            }
        }

        startDestOffset = (startDestOffset + particleCount * particleSizeBytes) % particleBufferSize
    }

    override fun shutdown() {
        bufferLockManager.destroy()
        super.shutdown()
    }

    override val name get() = "GLMapUnsynchronized"
}
