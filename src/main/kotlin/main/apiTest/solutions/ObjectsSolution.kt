package main.apiTest.solutions

import glm_.*
import glm_.mat4x4.Mat4
import glm_.vec3.Vec3
import glm_.vec3.operators.times
import gln.GLbitfield
import gln.glf.glf
import gln.glf.semantic
import gln.program.GlslProgram
import gln.uniform.glUniform
import gln.vertexArray.glBindVertexArray
import gln.vertexArray.glDisableVertexAttribArray
import gln.vertexArray.glEnableVertexAttribArray
import gln.vertexArray.glVertexAttribPointer
import kool.*
import main.*
import main.apiTest.Mat4Buffer
import main.apiTest.framework.*
import main.apiTest.problems.VertexBufferA
import org.lwjgl.opengl.GL11.glDisableClientState
import org.lwjgl.opengl.GL11.glEnableClientState
import org.lwjgl.opengl.GL20C.glUseProgram
import org.lwjgl.opengl.GL33C.glVertexAttribDivisor
import org.lwjgl.opengl.GL40C.GL_DRAW_INDIRECT_BUFFER
import org.lwjgl.opengl.GL42C.glDrawElementsInstancedBaseInstance
import org.lwjgl.opengl.GL43C.GL_SHADER_STORAGE_BUFFER
import org.lwjgl.opengl.GL43C.glMultiDrawElementsIndirect
import org.lwjgl.opengl.GL44C.*
import org.lwjgl.opengl.NVBindlessMultiDrawIndirect.nglMultiDrawElementsIndirectBindlessNV
import org.lwjgl.opengl.NVShaderBufferLoad.*
import org.lwjgl.opengl.NVVertexBufferUnifiedMemory.*
import org.lwjgl.system.MemoryUtil.NULL
import org.lwjgl.system.MemoryUtil.memCopy
import java.nio.*

abstract class ObjectsSolution : Solution() {

    var objectCount = 0
    var indexCount = 0

    val ib = IntBuffer(1)
    val vb = IntBuffer(1)

    val vao = IntBuffer(1)

    var program = 0

    open fun init(vertices: VertexBufferA, indices: ShortBuffer, objectCount: Int): Boolean {

        this.objectCount = objectCount
        indexCount = indices.rem

        return true
    }

    abstract fun render(transforms: Mat4Buffer)

    abstract override val name: String

    override val problemName get() = "UntexturedObjects"

    override fun supportsApi(api: OpenGlApi) = api.isOpenGL()

    fun bindProgramAndSetViewProj() {

        val dir = Vec3(-0.5f, -1f, 1f)
        val at = Vec3()
        val up = Vec3(0, 0, 1)
        dir.normalizeAssign()
        val eye = at - 250 * dir
        val view = glm.lookAt(eye, at, up)
        val viewProj = proj * view

        glUseProgram(program)
        glUniform(semantic.uniform.TRANSFORM0, viewProj)
    }

    fun setCommonGlState() {

        // Rasterizer State
        glEnable(GL_CULL_FACE)
        glCullFace(GL_FRONT)
        glDisable(GL_SCISSOR_TEST)

        // Blend State
        glDisable(GL_BLEND)
        glColorMask(true)

        // Depth Stencil State
        glEnable(GL_DEPTH_TEST)
        glDepthMask(false)
    }

    override fun shutdown() {
        if (glIsBuffer(vb)) glDeleteBuffers(vb)
        if (glIsBuffer(ib)) glDeleteBuffers(ib)
        if (glIsVertexArray(vao)) glDeleteBuffers(vao)
        glDeleteProgram(program)
    }

    val fragment = "shader"
}

class ObjectsGLUniform : ObjectsSolution() {

    override fun init(vertices: VertexBufferA, indices: ShortBuffer, objectCount: Int): Boolean {

        if (!super.init(vertices, indices, objectCount))
            return false

        // Program
        program = GlslProgram.fromRoot("shaders/objects", "uniform", fragment).name

        if (program == 0) {
            System.err.println("Unable to initialize solution '$name', shader compilation/linking failed.")
            return false
        }

        glGenBuffers(vb, ib)

        glBindBuffer(GL_ARRAY_BUFFER, vb)
        glBufferData(GL_ARRAY_BUFFER, vertices.data, GL_STATIC_DRAW)

        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, ib)
        glBufferData(GL_ELEMENT_ARRAY_BUFFER, indices, GL_STATIC_DRAW)

        glGenVertexArrays(vao)
        glBindVertexArray(vao)

        return glGetError() == GL_NO_ERROR
    }

    override fun render(transforms: Mat4Buffer) {

        // Program
        bindProgramAndSetViewProj()

        // Input Layout
        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, ib)
        glBindBuffer(GL_ARRAY_BUFFER, vb)
        glVertexAttribPointer(glf.pos3_col3)
        glEnableVertexAttribArray(glf.pos3_col3)

        setCommonGlState()

        var adr = transforms.adr
        for (i in 0 until transforms.size) { // avoid Mat4 allocation
            nglUniformMatrix4fv(semantic.uniform.TRANSFORM1, 1, false, adr)
            glDrawElements(GL_TRIANGLES, indexCount, GL_UNSIGNED_SHORT, 0)
            adr += Mat4.size
        }
    }

    override fun shutdown() {
        glDisableVertexAttribArray(glf.pos3_col3)
        super.shutdown()
    }

    override val name get() = "GLUniform"
}

class ObjectsGLDrawLoop : ObjectsSolution() {

    val drawId = IntBuffer(1)
    val transformBuffer = IntBuffer(1)

    override fun init(vertices: VertexBufferA, indices: ShortBuffer, objectCount: Int): Boolean {

        if (!super.init(vertices, indices, objectCount))
            return false

        // Program
        program = GlslProgram.fromRoot("shaders/objects", "draw-loop", fragment).name

        if (program == 0) {
            System.err.println("Unable to initialize solution '$name', shader compilation/linking failed.")
            return false
        }

        glGenVertexArrays(vao)
        glBindVertexArray(vao)

        glGenBuffers(vb, drawId, ib, transformBuffer)

        // Buffers
        glBindBuffer(GL_ARRAY_BUFFER, vb)
        glBufferData(GL_ARRAY_BUFFER, vertices.data, GL_STATIC_DRAW)
        glVertexAttribPointer(glf.pos3_col3)
        glEnableVertexAttribArray(glf.pos3_col3)

        IntBuffer(objectCount) { it }.use { drawIds ->
            glBindBuffer(GL_ARRAY_BUFFER, drawId)
            glBufferData(GL_ARRAY_BUFFER, drawIds, GL_STATIC_DRAW)
        }
        glVertexAttribIPointer(semantic.attr.DRAW_ID, 1, GL_UNSIGNED_INT, Int.BYTES, 0)
        glVertexAttribDivisor(semantic.attr.DRAW_ID, 1)
        glEnableVertexAttribArray(semantic.attr.DRAW_ID)

        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, ib)
        glBufferData(GL_ELEMENT_ARRAY_BUFFER, indices, GL_STATIC_DRAW)

        glBindBufferBase(GL_SHADER_STORAGE_BUFFER, semantic.storage.CONSTANT, transformBuffer)

        return glGetError() == GL_NO_ERROR
    }

    override fun render(transforms: Mat4Buffer) {

        val count = transforms.size
        assert(count <= objectCount)

        // Program
        bindProgramAndSetViewProj()

        setCommonGlState()

        glBindBuffer(GL_SHADER_STORAGE_BUFFER, transformBuffer)
        glBufferData(GL_SHADER_STORAGE_BUFFER, transforms.data, GL_DYNAMIC_DRAW)

        for (u in 0 until count)
            glDrawElementsInstancedBaseInstance(GL_TRIANGLES, indexCount, GL_UNSIGNED_SHORT, NULL, 1, u)
    }

    override fun shutdown() {

        glDisableVertexAttribArray(glf.pos3_col3)
        glDisableVertexAttribArray(semantic.attr.DRAW_ID)

        glDeleteBuffers(drawId, transformBuffer)

        super.shutdown()
    }

    override val name get() = "GLDrawLoop"
}

class ObjectsGLMultiDraw(
        useShaderDrawParameters: Boolean) : ObjectsSolution() {

    val drawId = if (useShaderDrawParameters) null else IntBuffer(1)

    val transformBuffer = IntBuffer(1)

    var commands: DrawElementsIndirectCommandBuffer? = null

    override fun init(vertices: VertexBufferA, indices: ShortBuffer, objectCount: Int): Boolean {

        if (!super.init(vertices, indices, objectCount))
            return false

        if (drawId == null && !caps.GL_ARB_shader_draw_parameters) {
            System.err.println("Unable to initialize solution, ARB_shader_draw_parameters is required but not available.")
            return false
        }

        // Program
        val vertex = "multi-draw-${when (drawId) {
            null -> "SDP"
            else -> "NoSDP"
        }}"
        program = GlslProgram.fromRoot("shaders/objects", vertex, fragment).name

        if (program == 0) {
            System.err.println("Unable to initialize solution '$name', shader compilation/linking failed.")
            return false
        }

        glGenVertexArrays(vao)
        glBindVertexArray(vao)

        glGenBuffers(vb, ib, transformBuffer)

        // Buffers
        glBindBuffer(GL_ARRAY_BUFFER, vb)
        glBufferData(GL_ARRAY_BUFFER, vertices.data, GL_STATIC_DRAW)
        glVertexAttribPointer(glf.pos3_col3)
        glEnableVertexAttribArray(glf.pos3_col3)

        // If we aren't using shader draw parameters, use the workaround instead.
        drawId?.let {
            glGenBuffers(drawId)
            glBindBuffer(GL_ARRAY_BUFFER, drawId)
            IntBuffer(objectCount) { it }.use { drawIds ->
                glBufferData(GL_ARRAY_BUFFER, drawIds, GL_STATIC_DRAW)
            }
            glVertexAttribIPointer(semantic.attr.DRAW_ID, 1, GL_UNSIGNED_INT, Int.BYTES, 0)
            glVertexAttribDivisor(semantic.attr.DRAW_ID, 1)
            glEnableVertexAttribArray(semantic.attr.DRAW_ID)
        }

        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, ib)
        glBufferData(GL_ELEMENT_ARRAY_BUFFER, indices, GL_STATIC_DRAW)

        glBindBufferBase(GL_SHADER_STORAGE_BUFFER, semantic.storage.CONSTANT, transformBuffer)

        // Set the command buffer size.
        commands = DrawElementsIndirectCommandBuffer(objectCount)

        return glGetError() == GL_NO_ERROR
    }

    override fun render(transforms: Mat4Buffer) {

        val count = transforms.size
        assert(count <= commands!!.size)

        // Program
        bindProgramAndSetViewProj()

        setCommonGlState()

        for (u in 0 until count)
            commands!![u].apply {
                this.count = indexCount
                instanceCount = 1
                firstIndex = 0
                baseVertex = 0
                baseInstance = if (drawId == null) 0 else u
            }

        // reset pos
        commands!!.data.pos = 0

        glBindBuffer(GL_SHADER_STORAGE_BUFFER, transformBuffer)
        glBufferData(GL_SHADER_STORAGE_BUFFER, transforms.data, GL_DYNAMIC_DRAW)

        glMultiDrawElementsIndirect(GL_TRIANGLES, GL_UNSIGNED_SHORT, commands!!.data, count, 0)
    }

    override fun shutdown() {

        drawId?.let {
            glDisableVertexAttribArray(semantic.attr.DRAW_ID)
            glDeleteBuffers(it)
        }
        glDisableVertexAttribArray(glf.pos3_col3)

        glDeleteBuffers(transformBuffer)

        super.shutdown()
    }

    override val name: String
        get() = "GLMultiDraw-" + when (drawId) {
            null -> "SDP"
            else -> "NoSDP"
        }
}

class ObjectsGLMultiDrawBuffer(
        useShaderDraw: Boolean) : ObjectsSolution() {

    val drawId = if (useShaderDraw) null else IntBuffer(1)
    val transformBuffer = IntBuffer(1)

    var commands: DrawElementsIndirectCommandBuffer? = null
    val cmdBuffer = IntBuffer(1)

    override fun init(vertices: VertexBufferA, indices: ShortBuffer, objectCount: Int): Boolean {

        if (!super.init(vertices, indices, objectCount))
            return false

        if (drawId == null && !caps.GL_ARB_shader_draw_parameters) {
            System.err.println("Unable to initialize solution, ARB_shader_draw_parameters is required but not available.")
            return false
        }

        // Program
        val vertex = "multi-draw-${when (drawId) {
            null -> "SDP"
            else -> "NoSDP"
        }}"
        program = GlslProgram.fromRoot("shaders/objects", vertex, fragment).name

        if (program == 0) {
            System.err.println("Unable to initialize solution '$name', shader compilation/linking failed.")
            return false
        }

        glGenVertexArrays(vao)
        glBindVertexArray(vao)

        // Buffers
        glGenBuffers(vb, ib, transformBuffer, cmdBuffer)

        glBindBuffer(GL_ARRAY_BUFFER, vb)
        glBufferData(GL_ARRAY_BUFFER, vertices.data, GL_STATIC_DRAW)
        glVertexAttribPointer(glf.pos3_col3)
        glEnableVertexAttribArray(glf.pos3_col3)

        // If we aren't using shader draw parameters, use the workaround instead.
        drawId?.let {
            glGenBuffers(drawId)
            glBindBuffer(GL_ARRAY_BUFFER, drawId)
            IntBuffer(objectCount) { it }.use { drawIds ->
                glBufferData(GL_ARRAY_BUFFER, drawIds, GL_STATIC_DRAW)
            }
            glVertexAttribIPointer(semantic.attr.DRAW_ID, 1, GL_UNSIGNED_INT, Int.BYTES, 0)
            glVertexAttribDivisor(semantic.attr.DRAW_ID, 1)
            glEnableVertexAttribArray(semantic.attr.DRAW_ID)
        }

        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, ib)
        glBufferData(GL_ELEMENT_ARRAY_BUFFER, indices, GL_STATIC_DRAW)

        glBindBufferBase(GL_SHADER_STORAGE_BUFFER, semantic.storage.CONSTANT, transformBuffer)

        glBindBuffer(GL_DRAW_INDIRECT_BUFFER, cmdBuffer)

        // Set the command buffer size.
        commands = DrawElementsIndirectCommandBuffer(objectCount)

        return glGetError() == GL_NO_ERROR
    }

    override fun render(transforms: Mat4Buffer) {

        val count = transforms.size
        assert(count <= commands!!.size)

        // Program
        bindProgramAndSetViewProj()

        setCommonGlState()

        for (u in 0 until count)
            commands!![u].apply {
                this.count = indexCount
                instanceCount = 1
                firstIndex = 0
                baseVertex = 0
                baseInstance = if (drawId == null) 0 else u
            }

        glBindBuffer(GL_SHADER_STORAGE_BUFFER, transformBuffer)
        glBufferData(GL_SHADER_STORAGE_BUFFER, transforms.data, GL_DYNAMIC_DRAW)

        glBindBuffer(GL_DRAW_INDIRECT_BUFFER, cmdBuffer)
        glBufferData(GL_DRAW_INDIRECT_BUFFER, commands!!.data, GL_DYNAMIC_DRAW)

        glMultiDrawElementsIndirect(GL_TRIANGLES, GL_UNSIGNED_SHORT, NULL, count, 0)
    }

    override fun shutdown() {

        drawId?.let {
            glDisableVertexAttribArray(semantic.attr.DRAW_ID)
            glDeleteBuffers(drawId)
        }
        glDisableVertexAttribArray(glf.pos3_col3)

        glDeleteBuffers(transformBuffer, cmdBuffer)
        commands!!.free()

        super.shutdown()
    }

    override val name: String
        get() = "GLMultiDrawBuffer-" + when (drawId) {
            null -> "SDP"
            else -> "NoSDP"
        }
}

class ObjectsGLBindless : ObjectsSolution() {

    lateinit var ibs: IntBuffer
    lateinit var ibAddrs: LongBuffer
    lateinit var ibSizes: LongBuffer
    lateinit var vbs: IntBuffer
    lateinit var vboAddrs: LongBuffer
    lateinit var vboSizes: LongBuffer

    override fun init(vertices: VertexBufferA, indices: ShortBuffer, objectCount: Int): Boolean {

        if (caps.glBufferStorage == NULL) {
            System.err.println("Unable to initialize solution '$name', glBufferStorage() unavailable.")
            return false
        }

        if (caps.glGetBufferParameterui64vNV == NULL || caps.glMakeBufferResidentNV == NULL) {
            System.err.println("Unable to initialize solution '$name', GL_NV_shader_buffer_load unavailable.")
            return false
        }

        if (!super.init(vertices, indices, objectCount))
            return false

        // Program
        program = GlslProgram.fromRoot("shaders/objects", "bindless", fragment).name

        if (program == 0) {
            System.err.println("Unable to initialize solution '$name', shader compilation/linking failed.")
            return false
        }


        ibs = IntBuffer(objectCount)
        ibAddrs = LongBuffer(objectCount)
        ibSizes = LongBuffer(objectCount) { indices.cap * Short.BYTES.L }

        vbs = IntBuffer(objectCount)
        vboAddrs = LongBuffer(objectCount)
        vboSizes = LongBuffer(objectCount) { vertices.data.cap * Float.BYTES.L }

        glGenBuffers(ibs, vbs)
        for (u in 0 until objectCount) {
            glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, ibs[u])
            glBufferStorage(GL_ELEMENT_ARRAY_BUFFER, indices, 0)
            ibAddrs[u] = glGetBufferParameterui64NV(GL_ELEMENT_ARRAY_BUFFER, GL_BUFFER_GPU_ADDRESS_NV)
            glMakeBufferResidentNV(GL_ELEMENT_ARRAY_BUFFER, GL_READ_ONLY)

            glBindBuffer(GL_ARRAY_BUFFER, vbs[u])
            glBufferStorage(GL_ARRAY_BUFFER, vertices.data, 0)
            vboAddrs[u] = glGetBufferParameterui64NV(GL_ARRAY_BUFFER, GL_BUFFER_GPU_ADDRESS_NV)
            glMakeBufferResidentNV(GL_ARRAY_BUFFER, GL_READ_ONLY)
        }

        glGenVertexArrays(vao)
        glBindVertexArray(vao)

        return glGetError() == GL_NO_ERROR
    }

    override fun render(transforms: Mat4Buffer) {

        // Program
        bindProgramAndSetViewProj()

        // Input Layout
        glEnableClientState(GL_ELEMENT_ARRAY_UNIFIED_NV)
        glEnableClientState(GL_VERTEX_ATTRIB_ARRAY_UNIFIED_NV)

        glVertexAttribFormatNV(semantic.attr.POSITION, Vec3.length, GL_FLOAT, false, Vec3.size * 2)
        glVertexAttribFormatNV(semantic.attr.COLOR, Vec3.length, GL_FLOAT, false, Vec3.size * 2)
        glEnableVertexAttribArray(glf.pos3_col3)

        setCommonGlState()

        for (u in transforms.indices) {
            glBufferAddressRangeNV(GL_ELEMENT_ARRAY_ADDRESS_NV, 0, ibAddrs[u], ibSizes[u])
            glBufferAddressRangeNV(GL_VERTEX_ATTRIB_ARRAY_ADDRESS_NV, semantic.attr.POSITION, vboAddrs[u], vboSizes[u])
            glBufferAddressRangeNV(GL_VERTEX_ATTRIB_ARRAY_ADDRESS_NV, semantic.attr.COLOR, vboAddrs[u] + Vec3.size, vboSizes[u] - Vec3.size)

            glUniform(semantic.uniform.TRANSFORM1, transforms[u])
            glDrawElements(GL_TRIANGLES, indexCount, GL_UNSIGNED_SHORT, NULL)
        }
    }

    override fun shutdown() {

        glDisableVertexAttribArray(glf.pos3_col3)

        if (::ibs.isInitialized) {
            glDeleteBuffers(ibs)
            ibs.free()
        }
        if (::ibAddrs.isInitialized) ibAddrs.free()
        if (::ibSizes.isInitialized) ibSizes.free()

        if (::vbs.isInitialized) {
            glDeleteBuffers(vbs)
            vbs.free()
        }
        if (::vboAddrs.isInitialized) vboAddrs.free()
        if (::vboSizes.isInitialized) vboSizes.free()

        super.shutdown()

        if (caps.glGetBufferParameterui64vNV != NULL && caps.glMakeBufferResidentNV != NULL) {
            glDisableClientState(GL_ELEMENT_ARRAY_UNIFIED_NV)
            glDisableClientState(GL_VERTEX_ATTRIB_ARRAY_UNIFIED_NV)
        }
    }

    override val name get() = "GLBindless"
}

class ObjectsGLBindlessIndirect : ObjectsSolution() {

    lateinit var ibs: IntBuffer
    lateinit var ibAddrs: LongBuffer
    lateinit var ibSizes: LongBuffer
    lateinit var vbs: IntBuffer
    lateinit var vboAddrs: LongBuffer
    lateinit var vboSizes: LongBuffer

    val queryCount = 4

    val queries = IntBuffer(queryCount)
    var currentQueryIssue = 0
    var currentQueryGet = 0

    val transformBuffer = IntBuffer(1)
    var transformPtr: ByteBuffer? = null

    var commands: CommandNvBuffer? = null
    val cmdBuffer = IntBuffer(1)
    var cmdPtr: ByteBuffer? = null

    override fun init(vertices: VertexBufferA, indices: ShortBuffer, objectCount: Int): Boolean {

        if (caps.glBufferStorage == NULL) {
            System.err.println("Unable to initialize solution '$name', glBufferStorage() unavailable.")
            return false
        }

        if (caps.glGetBufferParameterui64vNV == NULL || caps.glMakeBufferResidentNV == NULL) {
            System.err.println("Unable to initialize solution '$name', GL_NV_shader_buffer_load unavailable.")
            return false
        }

        if (!super.init(vertices, indices, objectCount))
            return false

        // Program
        program = GlslProgram.fromRoot("shaders/objects", "bindless-indirect", fragment).name

        if (program == 0) {
            System.err.println("Unable to initialize solution '$name', shader compilation/linking failed.")
            return false
        }

        ibs = IntBuffer(objectCount)
        ibAddrs = LongBuffer(objectCount)
        ibSizes = LongBuffer(objectCount) { indices.cap * Short.BYTES.L }

        vbs = IntBuffer(objectCount)
        vboAddrs = LongBuffer(objectCount)
        vboSizes = LongBuffer(objectCount) { vertices.data.cap * Float.BYTES.L }

        commands = CommandNvBuffer(objectCount)

        glGenBuffers(ibs, vbs, transformBuffer, cmdBuffer)

        for (u in 0 until objectCount) {
            glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, ibs[u])
            glBufferStorage(GL_ELEMENT_ARRAY_BUFFER, indices, 0)
            ibAddrs[u] = glGetBufferParameterui64NV(GL_ELEMENT_ARRAY_BUFFER, GL_BUFFER_GPU_ADDRESS_NV)
            glMakeBufferResidentNV(GL_ELEMENT_ARRAY_BUFFER, GL_READ_ONLY)

            glBindBuffer(GL_ARRAY_BUFFER, vbs[u])
            glBufferStorage(GL_ARRAY_BUFFER, vertices.data, 0)
            vboAddrs[u] = glGetBufferParameterui64NV(GL_ARRAY_BUFFER, GL_BUFFER_GPU_ADDRESS_NV)
            glMakeBufferResidentNV(GL_ARRAY_BUFFER, GL_READ_ONLY)
        }

        glBindBufferBase(GL_SHADER_STORAGE_BUFFER, 0, transformBuffer)
        glBufferStorage(GL_SHADER_STORAGE_BUFFER, objectCount * 64L, GL_MAP_WRITE_BIT or GL_MAP_PERSISTENT_BIT or GL_DYNAMIC_STORAGE_BIT)
        transformPtr = glMapBufferRange(GL_SHADER_STORAGE_BUFFER, 0, objectCount * 64L, GL_MAP_WRITE_BIT or GL_MAP_PERSISTENT_BIT)

        glBindBuffer(GL_DRAW_INDIRECT_BUFFER, cmdBuffer)
        glBufferStorage(GL_DRAW_INDIRECT_BUFFER, objectCount * CommandNV.size.L, GL_MAP_WRITE_BIT or GL_MAP_PERSISTENT_BIT or GL_DYNAMIC_STORAGE_BIT)
        cmdPtr = glMapBufferRange(GL_DRAW_INDIRECT_BUFFER, 0, objectCount * CommandNV.size.L, GL_MAP_WRITE_BIT or GL_MAP_PERSISTENT_BIT)

        glGenQueries(queries)

        glGenVertexArrays(vao)
        glBindVertexArray(vao)

        return glGetError() == GL_NO_ERROR
    }

    override fun render(transforms: Mat4Buffer) {

        val xformCount = transforms.size
        val objCount = commands!!.size
        assert(xformCount == objCount)

        // Program
        bindProgramAndSetViewProj()

        // Input Layout
        glEnableClientState(GL_ELEMENT_ARRAY_UNIFIED_NV)
        glEnableClientState(GL_VERTEX_ATTRIB_ARRAY_UNIFIED_NV)

        glVertexAttribFormatNV(semantic.attr.POSITION, Vec3.length, GL_FLOAT, false, Vec3.size * 2)
        glVertexAttribFormatNV(semantic.attr.COLOR, Vec3.length, GL_FLOAT, false, Vec3.size * 2)
        glEnableVertexAttribArray(glf.pos3_col3)

        setCommonGlState()

        var i = 0
        commands!!.forEach { cmd ->

            cmd.apply {
                draw.apply {
                    count = indexCount
                    instanceCount = 1
                    firstIndex = 0
                    baseVertex = 0
                    baseInstance = 0
                }
                reserved = 0
                indexBuffer.apply {
                    index = 0
                    reserved = 0
                    address = ibAddrs[i]
                    length = ibSizes[i]
                }
                vertexBuffer0.apply {
                    index = semantic.attr.POSITION
                    reserved = 0
                    address = vboAddrs[i] + 0
                    length = vboSizes[i] - 0
                }
                vertexBuffer1.apply {
                    index = semantic.attr.COLOR
                    reserved = 0
                    address = vboAddrs[i] + Vec3.size
                    length = vboSizes[i] - Vec3.size
                }
            }
            ++i
        }

        memCopy(transforms.data, transformPtr!!.asFloatBuffer())

        memCopy(commands!!.data, cmdPtr!!)

        glMemoryBarrier(GL_CLIENT_MAPPED_BUFFER_BARRIER_BIT)

        // resolveQueries();

        // glBeginQuery(GL_TIME_ELAPSED, m_queries[m_currentQueryIssue]);
        nglMultiDrawElementsIndirectBindlessNV(GL_TRIANGLES, GL_UNSIGNED_SHORT, 0, objCount, Vec3.size * 2, 2) // TODO !native
        // glEndQuery(GL_TIME_ELAPSED);

        // m_currentQueryIssue = (m_currentQueryIssue + 1) % kQueryCount;
    }

    override fun shutdown() {

        glDisableVertexAttribArray(glf.pos3_col3)

        transformPtr?.let {
            glBindBuffer(GL_SHADER_STORAGE_BUFFER, transformBuffer)
            glUnmapBuffer(GL_SHADER_STORAGE_BUFFER)
        }

        cmdPtr?.let {
            glBindBuffer(GL_DRAW_INDIRECT_BUFFER, cmdBuffer)
            glUnmapBuffer(GL_DRAW_INDIRECT_BUFFER)
        }

        glDeleteQueries(queries)

        glDeleteBuffers(transformBuffer, cmdBuffer, ibs, vbs)

        super.shutdown()

        // TODO: These could also go in ::End.
        if (caps.glGetBufferParameterui64vNV != NULL && caps.glMakeBufferResidentNV != NULL) {
            glDisableClientState(GL_ELEMENT_ARRAY_UNIFIED_NV)
            glDisableClientState(GL_VERTEX_ATTRIB_ARRAY_UNIFIED_NV)
        }
    }

    override val name get() = "GLBindlessIndirect"

    fun resolveQueries() {

        // Only happens the first time, and we don't need to resolve to move forward.
        if (currentQueryGet == -1) {
            currentQueryGet = 0
            return
        }

        while (true) {
            val available = glGetQueryObjectui(queries[currentQueryGet], GL_QUERY_RESULT_AVAILABLE).bool
            if (!available && ((currentQueryIssue + 1) % queryCount) != currentQueryGet)
                break   // If we're not already overlapping, can just exit if the result is unavailable.

            val timeElapsed = glGetQueryObjecti64(queries[currentQueryGet], GL_QUERY_RESULT)

            println("Elapsed GPU: %.2f ms".format(timeElapsed / 1000000f))

            currentQueryGet = (currentQueryGet + 1) % queryCount
            if (currentQueryGet == currentQueryIssue)
                break
        }
    }
}

class ObjectsGLBufferRange : ObjectsSolution() {

    val uniformBuffer = IntBuffer(1)

    var matrixStride = 0
    var maxUniformBlockSize = 0
    var maxBatchSize = 0

    lateinit var storage: ByteBuffer

    override fun init(vertices: VertexBufferA, indices: ShortBuffer, objectCount: Int): Boolean {

        if (!super.init(vertices, indices, objectCount))
            return false

        // Program
        program = GlslProgram.fromRoot("shaders/objects", "buffer-range", fragment).name

        if (program == 0) {
            System.err.println("Unable to initialize solution '$name', shader compilation/linking failed.")
            return false
        }

        glGenVertexArrays(vao)
        glBindVertexArray(vao)

        glGenBuffers(vb, ib)

        glBindBuffer(GL_ARRAY_BUFFER, vb)
        glBufferData(GL_ARRAY_BUFFER, vertices.data, GL_STATIC_DRAW)

        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, ib)
        glBufferData(GL_ELEMENT_ARRAY_BUFFER, indices, GL_STATIC_DRAW)

        val uniformBufferOffsetAlignment = glGetInteger(GL_UNIFORM_BUFFER_OFFSET_ALIGNMENT)
        maxUniformBlockSize = glGetInteger(GL_MAX_UNIFORM_BLOCK_SIZE)
        matrixStride = ceil(Mat4.size, uniformBufferOffsetAlignment)
        maxBatchSize = maxUniformBlockSize / matrixStride

        val maxSupportedBatchSize = (64 * 64 * 64) min objectCount

        maxBatchSize = maxBatchSize min maxSupportedBatchSize

        glGenBuffers(uniformBuffer)
        storage = Buffer(matrixStride * maxBatchSize)

        glGenVertexArrays(vao)
        glBindVertexArray(vao)

        return glGetError() == GL_NO_ERROR
    }

    fun ceil(num: Int, div: Int): Int {
        assert(num > 0 && div > 0)
        return div * ((num + div - 1) / div)
    }

    override fun render(transforms: Mat4Buffer) {

        assert(transforms.size <= Int.MAX_VALUE)
        val xformCount = transforms.size

        // Program
        bindProgramAndSetViewProj()

        // Input Layout
        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, ib)
        glBindBuffer(GL_ARRAY_BUFFER, vb)

        glVertexAttribPointer(glf.pos3_col3)
        glEnableVertexAttribArray(glf.pos3_col3)

        setCommonGlState()

        glBindBuffer(GL_UNIFORM_BUFFER, uniformBuffer)

        for (batchStart in 0 until xformCount step maxBatchSize) {

            val batchCount = (xformCount - batchStart) min maxBatchSize

            for (i in 0 until batchCount)
                transforms[batchStart + i].to(storage, matrixStride * i)

            glBufferData(GL_UNIFORM_BUFFER, storage, GL_DYNAMIC_DRAW)

            for (i in 0 until batchCount) {

                glBindBufferRange(GL_UNIFORM_BUFFER, semantic.storage.CONSTANT, uniformBuffer[0], matrixStride * i.L, Mat4.size.L)

                glDrawElements(GL_TRIANGLES, indexCount, GL_UNSIGNED_SHORT, NULL)
            }
        }
    }

    override fun shutdown() {

        glDisableVertexAttribArray(glf.pos3_col3)

        glDeleteBuffers(uniformBuffer)

        super.shutdown()
    }

    override val name get() = "GLBufferRange"
}

class ObjectsGLBufferStorage(
        useShaderDrawParameters: Boolean) : ObjectsSolution() {

    val drawId = if (useShaderDrawParameters) null else IntBuffer(1)

    val transformBuffer = CircularBuffer(true)
    val commands = CircularBuffer(true)

    override fun init(vertices: VertexBufferA, indices: ShortBuffer, objectCount: Int): Boolean {

        if (caps.glBufferStorage == NULL) {
            System.err.println("Unable to initialize solution '$name', glBufferStorage() unavailable.")
            return false
        }

        if (!super.init(vertices, indices, objectCount))
            return false

        if (drawId == null && !caps.GL_ARB_shader_draw_parameters) {
            System.err.println("Unable to initialize solution, ARB_shader_draw_parameters is required but not available.")
            return false
        }

        // Program
        val vertex = "buffer-storage-${when (drawId) {
            null -> "SDP"
            else -> "NoSDP"
        }}"
        program = GlslProgram.fromRoot("shaders/objects", vertex, fragment).name

        if (program == 0) {
            System.err.println("Unable to initialize solution '$name', shader compilation/linking failed.")
            return false
        }

        glGenVertexArrays(vao)
        glBindVertexArray(vao)

        // Buffers
        glGenBuffers(vb, ib)

        glBindBuffer(GL_ARRAY_BUFFER, vb)
        glBufferData(GL_ARRAY_BUFFER, vertices.data, GL_STATIC_DRAW)
        glVertexAttribPointer(glf.pos3_col3)
        glEnableVertexAttribArray(glf.pos3_col3)

        // If we aren't using shader draw parameters, use the workaround instead.
        drawId?.let {
            val drawIds = IntBuffer(objectCount) { it }

            glGenBuffers(drawId)
            glBindBuffer(GL_ARRAY_BUFFER, drawId)
            glBufferData(GL_ARRAY_BUFFER, drawIds, GL_STATIC_DRAW)
            glVertexAttribIPointer(semantic.attr.DRAW_ID, 1, GL_UNSIGNED_INT, Int.BYTES, 0)
            glVertexAttribDivisor(semantic.attr.DRAW_ID, 1)
            glEnableVertexAttribArray(semantic.attr.DRAW_ID)
        }

        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, ib)
        glBufferData(GL_ELEMENT_ARRAY_BUFFER, indices, GL_STATIC_DRAW)

        val mapFlags: GLbitfield = GL_MAP_WRITE_BIT or GL_MAP_PERSISTENT_BIT
        val createFlags: GLbitfield = mapFlags or GL_DYNAMIC_STORAGE_BIT

        commands.create(BufferStorage.PersistentlyMappedBuffer, GL_DRAW_INDIRECT_BUFFER, DrawElementsIndirectCommand.size,
                tripleBuffer * objectCount, createFlags, mapFlags)
        transformBuffer.create(BufferStorage.PersistentlyMappedBuffer, GL_SHADER_STORAGE_BUFFER, Mat4.size,
                tripleBuffer * objectCount, createFlags, mapFlags)

        return glGetError() == GL_NO_ERROR
    }

    override fun render(transforms: Mat4Buffer) {

        val xformCount = transforms.size
        assert(xformCount <= transformBuffer.atomCount && xformCount <= commands.atomCount)

        // Program
        bindProgramAndSetViewProj()

        setCommonGlState()

        val dstCmds = commands reserve xformCount
        for (u in 0 until xformCount)
            DrawElementsIndirectCommand(dstCmds + u * DrawElementsIndirectCommand.size).apply {
                count = indexCount
                instanceCount = 1
                firstIndex = 0
                baseVertex = 0
                baseInstance = if (drawId == null) 0 else u
            }
        val dstTransforms = transformBuffer reserve xformCount
        memCopy(transforms.data.adr, dstTransforms, Mat4.size * xformCount.L)

        transformBuffer.bindBufferRange(semantic.storage.CONSTANT, Mat4.size, xformCount)

        // We didn't use MAP_COHERENT here.
        glMemoryBarrier(GL_CLIENT_MAPPED_BUFFER_BARRIER_BIT)

        glMultiDrawElementsIndirect(GL_TRIANGLES, GL_UNSIGNED_SHORT, commands.headOffset, xformCount, 0)

        commands onUsageComplete xformCount
        transformBuffer onUsageComplete xformCount
    }

    override fun shutdown() {

        drawId?.let {
            glDisableVertexAttribArray(semantic.attr.DRAW_ID)
            glDeleteBuffers(drawId)
        }
        glDisableVertexAttribArray(glf.pos3_col3)

        commands.destroy()
        transformBuffer.destroy()

        super.shutdown()
    }

    override val name: String
        get() = "GLBufferStorage-" + when (drawId) {
            null -> "SDP"
            else -> "NoSDP"
        }
}

class ObjectsGLDynamicBuffer : ObjectsSolution() {

    val ub = IntBuffer(1)

    override fun init(vertices: VertexBufferA, indices: ShortBuffer, objectCount: Int): Boolean {

        if (!super.init(vertices, indices, objectCount))
            return false

        // Program
        program = GlslProgram.fromRoot("/shaders/objects", "dynamic-buffer").name

        if (program == 0) {
            System.err.println("Unable to initialize solution '$name', shader compilation/linking failed.")
            return false
        }

        glGenVertexArrays(vao)
        glBindVertexArray(vao)

        val UB0 = glGetUniformBlockIndex(program, "UB0")
        glUniformBlockBinding(program, UB0, semantic.uniform.CONSTANT)

        glGenBuffers(vb, ib, ub)
        glBindBuffer(GL_ARRAY_BUFFER, vb)
        glBufferData(GL_ARRAY_BUFFER, vertices.data, GL_STATIC_DRAW)

        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, ib)
        glBufferData(GL_ELEMENT_ARRAY_BUFFER, indices, GL_STATIC_DRAW)

        return glGetError() == GL_NO_ERROR
    }

    override fun render(transforms: Mat4Buffer) {

        // Program
        bindProgramAndSetViewProj()

        glBindBufferBase(GL_UNIFORM_BUFFER, 0, ub)

        // Input Layout
        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, ib)
        glBindBuffer(GL_ARRAY_BUFFER, vb)
        glVertexAttribPointer(glf.pos3_col3)
        glEnableVertexAttribArray(glf.pos3_col3)

        setCommonGlState()

        glBindBuffer(GL_UNIFORM_BUFFER, ub)
        val xformCount = transforms.size

        for (i in 0 until xformCount) {
            nglBufferData(GL_UNIFORM_BUFFER, Mat4.size.L, transforms.adr(i), GL_DYNAMIC_DRAW)
            glDrawElements(GL_TRIANGLES, indexCount, GL_UNSIGNED_SHORT, NULL)
        }
    }

    override fun shutdown() {

        glDisableVertexAttribArray(glf.pos3_col3)

        glDeleteBuffers(ub)

        super.shutdown()
    }

    override val name get() = "GLDynamicBuffer"
}

class ObjectsGLMapUnsynchronized : ObjectsSolution() {

    val drawId = IntBuffer(1)

    val transformBuffer = IntBuffer(1)

    var startDestOffset = 0L
    var transformBufferSize = 0

    val bufferLockManager = BufferLockManager(true)

    override fun init(vertices: VertexBufferA, indices: ShortBuffer, objectCount: Int): Boolean {

        if (!super.init(vertices, indices, objectCount))
            return false

        // Program
        program = GlslProgram.fromRoot("/shaders/objects", "map").name

        if (program == 0) {
            System.err.println("Unable to initialize solution '$name', shader compilation/linking failed.")
            return false
        }

        // Buffers
        glGenVertexArrays(vao)
        glBindVertexArray(vao)

        glGenBuffers(vb, drawId, ib, transformBuffer)

        glBindBuffer(GL_ARRAY_BUFFER, vb)
        glBufferData(GL_ARRAY_BUFFER, vertices.data, GL_STATIC_DRAW)
        glVertexAttribPointer(glf.pos3_col3)
        glEnableVertexAttribArray(glf.pos3_col3)

        val drawIds = IntBuffer(objectCount) { it }

        glBindBuffer(GL_ARRAY_BUFFER, drawId)
        glBufferData(GL_ARRAY_BUFFER, drawIds, GL_STATIC_DRAW)
        glVertexAttribIPointer(semantic.attr.DRAW_ID, 1, GL_UNSIGNED_INT, Int.BYTES, 0)
        glVertexAttribDivisor(semantic.attr.DRAW_ID, 1)
        glEnableVertexAttribArray(semantic.attr.DRAW_ID)


        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, ib)
        glBufferData(GL_ELEMENT_ARRAY_BUFFER, indices, GL_STATIC_DRAW)

        glBindBufferBase(GL_SHADER_STORAGE_BUFFER, 0, transformBuffer)

        transformBufferSize = tripleBuffer * Mat4.size * objectCount
        glBufferData(GL_SHADER_STORAGE_BUFFER, transformBufferSize.L, GL_DYNAMIC_DRAW)

        return glGetError() == GL_NO_ERROR
    }

    override fun render(transforms: Mat4Buffer) {

        val count = transforms.size
        assert(count <= objectCount)

        // Program
        bindProgramAndSetViewProj()

        setCommonGlState()

        val rangeSize = count * Mat4.size.L
        bufferLockManager.waitForLockedRange(startDestOffset, rangeSize)

        for (i in 0 until count) {
            val offset = startDestOffset + i * Mat4.size
            val length = Mat4.size
            val access = GL_MAP_WRITE_BIT or GL_MAP_INVALIDATE_RANGE_BIT or GL_MAP_UNSYNCHRONIZED_BIT
            val dst = glMapBufferRange(GL_SHADER_STORAGE_BUFFER, offset, length.L, access) ?: continue
            memCopy(transforms.adr(i), dst.adr, Mat4.size.L)
            glUnmapBuffer(GL_SHADER_STORAGE_BUFFER)

            glDrawElementsInstancedBaseInstance(GL_TRIANGLES, indexCount, GL_UNSIGNED_SHORT, NULL, 1, i)
        }

        bufferLockManager.lockRange(startDestOffset, rangeSize)
        startDestOffset = (startDestOffset + rangeSize) % transformBufferSize
    }

    override fun shutdown() {

        glDisableVertexAttribArray(semantic.attr.DRAW_ID)
        glDisableVertexAttribArray(glf.pos3_col3)

        glDeleteBuffers(drawId, transformBuffer)

        super.shutdown()
    }

    override val name get() = "GLMapUnsynchronized"
}

class ObjectsGLMapPersistent : ObjectsSolution() {

    val drawId = IntBuffer(1)

    val transformBuffer = IntBuffer(1)

    var startDestOffset = 0L
    var transformBufferSize = 0L

    val bufferLockManager = BufferLockManager(true)

    var transformDataPtr: ByteBuffer? = null

    override fun init(vertices: VertexBufferA, indices: ShortBuffer, objectCount: Int): Boolean {

        if (!super.init(vertices, indices, objectCount))
            return false

        if (caps.glBufferStorage == NULL) {
            System.err.println("Unable to initialize solution '$name', glBufferStorage() unavailable.")
            return false
        }

        // Program
        program = GlslProgram.fromRoot("/shaders/objects", "map").name

        if (program == 0) {
            System.err.println("Unable to initialize solution '$name', shader compilation/linking failed.")
            return false
        }

        // Buffers
        glGenVertexArrays(vao)
        glBindVertexArray(vao)

        glGenBuffers(vb, drawId, ib, transformBuffer)

        glBindBuffer(GL_ARRAY_BUFFER, vb)
        glBufferData(GL_ARRAY_BUFFER, vertices.data, GL_STATIC_DRAW)
        glVertexAttribPointer(glf.pos3_col3)
        glEnableVertexAttribArray(glf.pos3_col3)

        val drawIds = IntBuffer(objectCount) { it }

        glBindBuffer(GL_ARRAY_BUFFER, drawId)
        glBufferData(GL_ARRAY_BUFFER, drawIds, GL_STATIC_DRAW)
        glVertexAttribIPointer(semantic.attr.DRAW_ID, 1, GL_UNSIGNED_INT, Int.BYTES, 0)
        glVertexAttribDivisor(semantic.attr.DRAW_ID, 1)
        glEnableVertexAttribArray(semantic.attr.DRAW_ID)

        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, ib)
        glBufferData(GL_ELEMENT_ARRAY_BUFFER, indices, GL_STATIC_DRAW)

        glBindBufferBase(GL_SHADER_STORAGE_BUFFER, 0, transformBuffer)

        transformBufferSize = tripleBuffer * Mat4.size * objectCount.L
        val flags: GLbitfield = GL_MAP_WRITE_BIT or GL_MAP_PERSISTENT_BIT or GL_MAP_COHERENT_BIT
        glBufferStorage(GL_SHADER_STORAGE_BUFFER, transformBufferSize, flags)
        transformDataPtr = glMapBufferRange(GL_SHADER_STORAGE_BUFFER, 0, transformBufferSize, flags)

        return glGetError() == GL_NO_ERROR
    }

    override fun render(transforms: Mat4Buffer) {

        val count = transforms.size
        assert(count <= objectCount)

        // Program
        bindProgramAndSetViewProj()

        setCommonGlState()

        val rangeSize = count * Mat4.size.L
        bufferLockManager.waitForLockedRange(startDestOffset, rangeSize)

        for (i in 0 until count) {
            val offset = startDestOffset + i * Mat4.size
            val dst = transformDataPtr!!.adr + offset
            memCopy(transforms.adr(i), dst, Mat4.size.L)

            glDrawElementsInstancedBaseInstance(GL_TRIANGLES, indexCount, GL_UNSIGNED_SHORT, NULL, 1, i)
        }

        bufferLockManager.lockRange(startDestOffset, rangeSize)
        startDestOffset = (startDestOffset + rangeSize) % transformBufferSize
    }

    override fun shutdown() {

        glDisableVertexAttribArray(semantic.attr.DRAW_ID)
        glDisableVertexAttribArray(glf.pos3_col3)

        glBindBuffer(GL_SHADER_STORAGE_BUFFER, transformBuffer)
        glUnmapBuffer(GL_SHADER_STORAGE_BUFFER)

        glDeleteBuffers(drawId, transformBuffer)

        super.shutdown()
    }

    override val name get() = "GLMapPersistent"
}

class ObjectsGLTexCoord : ObjectsSolution() {

    override fun init(vertices: VertexBufferA, indices: ShortBuffer, objectCount: Int): Boolean {

        if (!super.init(vertices, indices, objectCount))
            return false

        // Program
        program = GlslProgram.fromRoot("/shaders/objects", "tex-coord").name

        if (program == 0) {
            System.err.println("Unable to initialize solution '$name', shader compilation/linking failed.")
            return false
        }

        // Buffers
        glGenVertexArrays(vao)
        glBindVertexArray(vao)

        glGenBuffers(vb, ib)

        glBindBuffer(GL_ARRAY_BUFFER, vb)
        glBufferData(GL_ARRAY_BUFFER, vertices.data, GL_STATIC_DRAW)

        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, ib)
        glBufferData(GL_ELEMENT_ARRAY_BUFFER, indices, GL_STATIC_DRAW)

        return glGetError() == GL_NO_ERROR
    }

    override fun render(transforms: Mat4Buffer) {

        // Program
        bindProgramAndSetViewProj()

        // Input Layout
        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, ib)
        glBindBuffer(GL_ARRAY_BUFFER, vb)
        glVertexAttribPointer(glf.pos3_col3)
        glEnableVertexAttribArray(glf.pos3_col3)

        setCommonGlState()

        for (m in transforms) {
            glVertexAttrib4f(2, m[0, 0], m[0, 1], m[0, 2], m[0, 3])
            glVertexAttrib4f(3, m[1, 0], m[1, 1], m[1, 2], m[1, 3])
            glVertexAttrib4f(4, m[2, 0], m[2, 1], m[2, 2], m[2, 3])
            glVertexAttrib4f(5, m[3, 0], m[3, 1], m[3, 2], m[3, 3])

            glDrawElements(GL_TRIANGLES, indexCount, GL_UNSIGNED_SHORT, NULL)
        }
    }

    override fun shutdown() {
        glDisableVertexAttribArray(glf.pos3_col3)
        super.shutdown()
    }

    override val name get() = "GLTexCoord"
}