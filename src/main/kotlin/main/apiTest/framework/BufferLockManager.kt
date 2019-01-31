package main.apiTest.framework

import org.lwjgl.opengl.GL32C.*

/**
 *
 * @author elect
 */
class BufferRange(
        var startOffset: Long,
        var length: Long = 0L) {

    infix fun overlaps(rhs: BufferRange): Boolean =
            startOffset < (rhs.startOffset + rhs.length) && rhs.startOffset < (startOffset + length)
}

class BufferLock(val range: BufferRange, var syncObj: GLsync)

inline class GLsync(val L: Long)

const val oneSecondInNanoSeconds = 1_000_000_000L


class BufferLockManager(
        /** Whether it's the CPU (true) that updates, or the GPU (false) */
        val cpuUpdates: Boolean) {

    var bufferLocks = ArrayList<BufferLock>()

    fun destroy() {
        bufferLocks.forEach(::cleanup)
        bufferLocks.clear()
    }

    fun waitForLockedRange(lockBeginBytes: Long, lockLength: Long) {
        val testRange = BufferRange(lockBeginBytes, lockLength)
        val swapLocks = ArrayList<BufferLock>()
        for (it in bufferLocks)
            if (testRange overlaps it.range) {
                wait(it.syncObj)
                cleanup(it)
        } else
            swapLocks += it

        bufferLocks = swapLocks
    }

    fun lockRange(lockBeginBytes: Long, lockLength: Long) {
        val newRange = BufferRange (lockBeginBytes, lockLength )
        val syncName = glFenceSync(GL_SYNC_GPU_COMMANDS_COMPLETE, 0)
        val newLock = BufferLock(newRange, GLsync(syncName))

        bufferLocks.add(newLock)
    }

    fun wait(syncObj: GLsync) {

        if  (cpuUpdates) {
            var waitFlags = 0
            var waitDuration = 0L
            while (true) {
                val waitRet = glClientWaitSync(syncObj.L, waitFlags, waitDuration)
                if (waitRet == GL_ALREADY_SIGNALED || waitRet == GL_CONDITION_SATISFIED)
                    return

                if (waitRet == GL_WAIT_FAILED) {
                    System.err.println("Not sure what to do here. Probably raise an exception or something.")
                    return
                }

                // After the first time, need to start flushing, and wait for a looong time.
                waitFlags = GL_SYNC_FLUSH_COMMANDS_BIT
                waitDuration = oneSecondInNanoSeconds
            }
        } else
            glWaitSync(syncObj.L, 0, GL_TIMEOUT_IGNORED)
    }

    fun cleanup(bufferLock: BufferLock) = glDeleteSync(bufferLock.syncObj.L)
}

