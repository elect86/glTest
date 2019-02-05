package main

import glm_.BYTES
import glm_.mat4x4.Mat4
import gln.glf.semantic
import kool.*
import org.lwjgl.opengl.GL15C
import org.lwjgl.opengl.GL30C
import org.lwjgl.system.MemoryUtil.memByteBuffer
import org.lwjgl.system.MemoryUtil.memIntBuffer
import java.nio.ByteBuffer
import java.nio.FloatBuffer
import java.nio.IntBuffer
import java.nio.LongBuffer

val semantic.storage.CONSTANT get() = 1

inline fun glColorMask(mask: Boolean) = GL15C.glColorMask(mask, mask, mask, mask)

fun glIsBuffer(buffer: IntBuffer) = GL15C.glIsBuffer(buffer[0])
fun glIsVertexArray(vao: IntBuffer) = GL30C.glIsVertexArray(vao[0])

fun glBindBuffer(target: Int, buffer: IntBuffer) = GL15C.glBindBuffer(target, buffer[0])

fun glBindBufferBase(target: Int, index: Int, buffer: IntBuffer) = GL30C.glBindBufferBase(target, index, buffer[0])

fun glGenBuffers(
        buffers0: IntBuffer,
        buffers1: IntBuffer) {
    GL15C.glGenBuffers(buffers0)
    GL15C.glGenBuffers(buffers1)
}

fun glGenBuffers(
        buffers0: IntBuffer,
        buffers1: IntBuffer,
        buffers2: IntBuffer) {
    GL15C.glGenBuffers(buffers0)
    GL15C.glGenBuffers(buffers1)
    GL15C.glGenBuffers(buffers2)
}

fun glGenBuffers(
        buffers0: IntBuffer,
        buffers1: IntBuffer,
        buffers2: IntBuffer,
        buffers3: IntBuffer) {
    GL15C.glGenBuffers(buffers0)
    GL15C.glGenBuffers(buffers1)
    GL15C.glGenBuffers(buffers2)
    GL15C.glGenBuffers(buffers3)
}

fun glGenBuffers(
        buffers0: IntBuffer,
        buffers1: IntBuffer,
        buffers2: IntBuffer,
        buffers3: IntBuffer,
        buffers4: IntBuffer) {
    GL15C.glGenBuffers(buffers0)
    GL15C.glGenBuffers(buffers1)
    GL15C.glGenBuffers(buffers2)
    GL15C.glGenBuffers(buffers3)
    GL15C.glGenBuffers(buffers4)
}

fun glDeleteBuffers(
        buffers0: IntBuffer,
        buffers1: IntBuffer) {
    GL15C.glDeleteBuffers(buffers0)
    GL15C.glDeleteBuffers(buffers1)
}

fun glDeleteBuffers(
        buffers0: IntBuffer,
        buffers1: IntBuffer,
        buffers2: IntBuffer) {
    GL15C.glDeleteBuffers(buffers0)
    GL15C.glDeleteBuffers(buffers1)
    GL15C.glDeleteBuffers(buffers2)
}

fun glDeleteBuffers(
        buffers0: IntBuffer,
        buffers1: IntBuffer,
        buffers2: IntBuffer,
        buffers3: IntBuffer) {
    GL15C.glDeleteBuffers(buffers0)
    GL15C.glDeleteBuffers(buffers1)
    GL15C.glDeleteBuffers(buffers2)
    GL15C.glDeleteBuffers(buffers3)
}

fun glDeleteBuffers(
        buffers0: IntBuffer,
        buffers1: IntBuffer,
        buffers2: IntBuffer,
        buffers3: IntBuffer,
        buffers4: IntBuffer) {
    GL15C.glDeleteBuffers(buffers0)
    GL15C.glDeleteBuffers(buffers1)
    GL15C.glDeleteBuffers(buffers2)
    GL15C.glDeleteBuffers(buffers3)
    GL15C.glDeleteBuffers(buffers4)
}

fun DrawElementsIndirectCommand(adr: Adr) = DrawElementsIndirectCommand(memByteBuffer(adr, DrawElementsIndirectCommand.size))
fun DrawElementsIndirectCommand(data: ByteBuffer) = DrawElementsIndirectCommand(data.asIntBuffer())

inline class DrawElementsIndirectCommand(val data: IntBuffer) {
    var count: Int
        get() = data.get(data.pos)
        set(value) {
            data.put(data.pos, value)
        }
    var instanceCount: Int
        get() = data.get(data.pos + 1)
        set(value) {
            data.put(data.pos + 1, value)
        }
    var firstIndex: Int
        get() = data.get(data.pos + 2)
        set(value) {
            data.put(data.pos + 2, value)
        }
    var baseVertex: Int
        get() = data.get(data.pos + 3)
        set(value) {
            data.put(data.pos + 3, value)
        }
    var baseInstance: Int
        get() = data.get(data.pos + 4)
        set(value) {
            data.put(data.pos + 4, value)
        }

    infix fun to(buffer: ByteBuffer) = to(buffer.asIntBuffer())

    infix fun to(buffer: IntBuffer) {
        val pos = buffer.pos
        buffer[pos] = count
        buffer[pos + 1] = instanceCount
        buffer[pos + 2] = firstIndex
        buffer[pos + 3] = baseVertex
        buffer[pos + 4] = baseInstance
    }

    companion object {
        val length = 5
        val size = 5 * Int.BYTES
    }
}

fun DrawElementsIndirectCommandBuffer(data: ByteBuffer) = DrawElementsIndirectCommandBuffer(data.asIntBuffer())

inline class DrawElementsIndirectCommandBuffer(val data: IntBuffer) {
    operator fun get(index: Int): DrawElementsIndirectCommand {
        val adr = data.adr + index * DrawElementsIndirectCommand.size
        val buf = memIntBuffer(adr, DrawElementsIndirectCommand.length)
        return DrawElementsIndirectCommand(buf)
    }

    fun free() = data.free()

    val size get() = data.cap / DrawElementsIndirectCommand.length
}

fun DrawElementsIndirectCommandBuffer(size: Int) =
        DrawElementsIndirectCommandBuffer(IntBuffer(size * DrawElementsIndirectCommand.length))

fun LongBuffer.adr(index: Int): Adr = adr + index * Long.BYTES

inline class BindlessPtrNV(val data: ByteBuffer) {
    var index
        get() = data.getInt(data.pos)
        set(value) {
            data.putInt(data.pos, value)
        }
    var reserved
        get() = data.getInt(data.pos + Int.BYTES)
        set(value) {
            data.putInt(data.pos + Int.BYTES, value)
        }
    var address
        get() = data.getLong(data.pos + Int.BYTES * 2)
        set(value) {
            data.putLong(data.pos + Int.BYTES * 2, value)
        }
    var length
        get() = data.getLong(data.pos + Int.BYTES * 2 + Long.BYTES)
        set(value) {
            data.putLong(data.pos + Int.BYTES * 2 + Long.BYTES, value)
        }

    infix fun to(buffer: ByteBuffer) {
        val pos = buffer.pos
        buffer.putInt(pos, index)
        buffer.putInt(pos + Int.BYTES, reserved)
        buffer.putLong(pos + Int.BYTES * 2, address)
        buffer.putLong(pos + Int.BYTES * 2 + Long.BYTES, length)
    }

    companion object {
        val size = Int.BYTES * 2 + Long.BYTES * 2
    }
}

inline class CommandNV(val data: ByteBuffer) {
    var draw: DrawElementsIndirectCommand
        get() = DrawElementsIndirectCommand(data)
        set(value) = value to data
    var reserved: Int
        get() = data.getInt(DrawElementsIndirectCommand.size)
        set(value) {
            data.putInt(DrawElementsIndirectCommand.size, value)
        }
    var indexBuffer: BindlessPtrNV
        get() = BindlessPtrNV(data)
        set(value) = value to data
    var vertexBuffer0: BindlessPtrNV
        get() = BindlessPtrNV(data)
        set(value) = value to data
    var vertexBuffer1: BindlessPtrNV
        get() = BindlessPtrNV(data)
        set(value) = value to data

    companion object {
        val size = DrawElementsIndirectCommand.size + Int.BYTES + BindlessPtrNV.size * 3
    }
}

inline class CommandNvBuffer(val data: ByteBuffer) {

    val size get() = data.cap / CommandNV.size

    inline fun forEach(action: (CommandNV) -> Unit) {
        for (i in 0 until size) {
            data.pos = i * CommandNV.size
            val cmd = CommandNV(data)
            action(cmd)
        }
    }
}

fun CommandNvBuffer(size: Int) = CommandNvBuffer(ByteBuffer(size * CommandNV.size))