package main.apiTest.framework

import glm_.L
import glm_.i
import gln.GLbitfield
import kool.*
import main.glBindBuffer
import main.glBindBufferBase
import org.lwjgl.opengl.GL15C.*
import org.lwjgl.opengl.GL30C.glBindBufferRange
import org.lwjgl.opengl.GL30C.glMapBufferRange
import org.lwjgl.opengl.GL44C.glBufferStorage
import java.nio.ByteBuffer

enum class BufferStorage { SystemMemory, PersistentlyMappedBuffer }

class Buffer(cpuUpdates: Boolean) {

    val lockManager = BufferLockManager(cpuUpdates)
    var bufferContents: ByteBuffer? = null
    val name = IntBuffer(1)
    var target = 0

    var bufferStorage = BufferStorage.SystemMemory

    fun create(storage: BufferStorage, target: Int, atomsTotalSize: Int, createFlags: GLbitfield, mapFlags: GLbitfield): Boolean {

        if (bufferContents != null) destroy()

        bufferStorage = storage
        this.target = target

        bufferContents = when (bufferStorage) {

            BufferStorage.SystemMemory ->  ByteBuffer(atomsTotalSize)

            BufferStorage.PersistentlyMappedBuffer -> {
                // This code currently doesn't care about the alignment of the returned memory. This could potentially
                // cause a crash, but since implementations are likely to return us memory that is at lest aligned
                // on a 64-byte boundary we're okay with this for now.
                // A robust implementation would ensure that the memory returned had enough slop that it could deal
                // with it's own alignment issues, at least. That's more work than I want to do right this second.

                glGenBuffers(name)
                glBindBuffer(target, name)
                glBufferStorage(target, atomsTotalSize.L, createFlags)
                glMapBufferRange(target, 0, atomsTotalSize.L, mapFlags) ?: run {
                    System.err.println("glMapBufferRange failed, probable bug.")
                    return false
                }
            }
        }
        return true
    }

    // Called by dtor, must be non-virtual.
    fun destroy() = when (bufferStorage) {
        BufferStorage.SystemMemory -> bufferContents!!.free()

        BufferStorage.PersistentlyMappedBuffer -> {
            glBindBuffer(target, name)
            glUnmapBuffer(target)
            glDeleteBuffers(name)

            bufferContents = null
            name[0] = 0
        }
    }

    fun waitForLockedRange(lockBegin: Long, lockLength: Long) = lockManager.waitForLockedRange(lockBegin, lockLength)

    fun lockRange(lockBegin: Long, lockLength: Long) = lockManager.lockRange(lockBegin, lockLength)

    fun bindBuffer() = glBindBuffer(target, name)

    infix fun bindBufferBase(index: Int) = glBindBufferBase(target, index, name)

    fun bindBufferRange(index: Int, headIdx: Long, atomSize: Int, atomCount: Int) = glBindBufferRange(target, index, name[0], headIdx * atomSize, atomCount * atomSize.L)
}

class CircularBuffer(cpuUpdates: Boolean = true) {

    val buffer = Buffer(cpuUpdates)

    var head = 0L
    // TODO: Maybe this should be in the Buffer class?
    var atomCount = 0
    var atomSize = 0

    fun create(storage: BufferStorage, target: Int, atomSize: Int, atomCount: Int, createFlags: GLbitfield, mapFlags: GLbitfield): Boolean {
        this.atomSize = atomSize
        this.atomCount = atomCount
        head = 0

        return buffer.create(storage, target, atomSize * atomCount, createFlags, mapFlags)
    }

    fun destroy() {
        buffer.destroy()

        atomCount = 0
        head = 0
    }

    infix fun reserve(atomCount: Int): Adr {
        if (atomCount > this.atomCount)
            System.err.println("Requested an update of size $atomCount for a buffer of size ${this.atomCount} atoms.")

        var lockStart = head

        if (lockStart + atomCount > this.atomCount)
            lockStart = 0   // Need to wrap here.

        buffer.waitForLockedRange(lockStart, atomCount.L)
        return buffer.bufferContents!!.adr + lockStart * atomSize
    }

    infix fun onUsageComplete(atomCount: Int)    {
        buffer.lockRange(head, atomCount.L)
        head = (head + atomCount) % this.atomCount
    }

    fun bindBuffer() = buffer.bindBuffer()

    fun bindBufferBase(index: Int) = buffer bindBufferBase index

    fun bindBufferRange(index: Int, atomSize: Int, atomCount: Int) = buffer.bindBufferRange(index, head, atomSize, atomCount)

    val headOffset get() = head * atomSize
}