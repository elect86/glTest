package main.glTest.framework

import glm_.L
import gln.GLbitfield
import kool.ByteBuffer
import kool.IntBuffer
import kool.free
import kool.set
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

        when (bufferStorage) {

            BufferStorage.SystemMemory -> bufferContents = ByteBuffer(atomsTotalSize)

            BufferStorage.PersistentlyMappedBuffer -> {
                // This code currently doesn't care about the alignment of the returned memory. This could potentially
                // cause a crash, but since implementations are likely to return us memory that is at lest aligned
                // on a 64-byte boundary we're okay with this for now.
                // A robust implementation would ensure that the memory returned had enough slop that it could deal
                // with it's own alignment issues, at least. That's more work than I want to do right this second.

                glGenBuffers(name)
                glBindBuffer(target, name)
                glBufferStorage(target, atomsTotalSize.L, createFlags)
                bufferContents = glMapBufferRange(target, 0, atomsTotalSize.L, mapFlags) ?: run {
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

    fun bindBufferBase(index: Int) = glBindBufferBase(target, index, name)

    fun bindBufferRange(index: Int, headIdx: Int, atomSize: Int, atomCount: Int) = glBindBufferRange(target, index, name[0], headIdx * atomSize.L, atomCount * atomSize.L)
}

class CircularBuffer {

    CircularBuffer(bool _cpuUpdates = true)
    : mBuffer(_cpuUpdates)
    {}

    bool Create(BufferStorage _storage, GLenum _target, GLuint _atomCount, GLbitfield _createFlags, GLbitfield _mapFlags)
    {
        mSizeAtoms = _atomCount
        mHead = 0

        return mBuffer.Create(_storage, _target, _atomCount, _createFlags, _mapFlags)
    }

    void Destroy()
    {
        mBuffer.Destroy()

        mSizeAtoms = 0
        mHead = 0
    }

    Atom* Reserve(GLsizeiptr _atomCount)
    {
        if (_atomCount > mSizeAtoms) {
            console::error("Requested an update of size %d for a buffer of size %d atoms.", _atomCount, mSizeAtoms)
        }

        GLsizeiptr lockStart = mHead

                if (lockStart + _atomCount > mSizeAtoms) {
                    // Need to wrap here.
                    lockStart = 0
                }

        mBuffer.WaitForLockedRange(lockStart, _atomCount)
        return &mBuffer.GetContents()[lockStart]
    }

    void OnUsageComplete(GLsizeiptr _atomCount)
    {
        mBuffer.LockRange(mHead, _atomCount)
        mHead = (mHead + _atomCount) % mSizeAtoms
    }

    void BindBuffer()
    { mBuffer.BindBuffer(); }
    void BindBufferBase(GLuint _index)
    { mBuffer.BindBufferBase(_index); }
    void BindBufferRange(GLuint _index, GLsizeiptr _atomCount)
    { mBuffer.BindBufferRange(_index, mHead, _atomCount); }

    GLsizeiptr GetHead()
    const { return mHead; }
    void* GetHeadOffset()
    const { return (void *)(mHead * sizeof(Atom)); }
    GLsizeiptr GetSize()
    const { return mSizeAtoms; }

    private :
    Buffer<Atom> mBuffer

    GLsizeiptr mHead
    // TODO: Maybe this should be in the Buffer class?
    GLsizeiptr mSizeAtoms
}