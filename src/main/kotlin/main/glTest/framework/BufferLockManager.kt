package main.glTest.framework

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

class BufferLock(val range: BufferRange, var syncObj: GLsync) {

    fun wait(cpuUpdates: Boolean) {
        if (cpuUpdates) {
            var waitFlags = 0
            var waitDuration = 0L
            while (true)
            /*
                - GL_ALREADY_SIGNALED indicates that sync was signaled at the time that glClientWaitSync was called.
                - GL_TIMEOUT_EXPIRED indicates that at least timeout nanoseconds passed and sync did not become signaled.
                - GL_CONDITION_SATISFIED indicates that sync was signaled before the timeout expired.
                - GL_WAIT_FAILED indicates that an error occurred. Additionally, an OpenGL error will be generated.
             */
                when (glClientWaitSync(syncObj, waitFlags, waitDuration)) {
                    GL_ALREADY_SIGNALED, GL_CONDITION_SATISFIED -> return
                    GL_WAIT_FAILED -> {
                        System.err.println("Not sure what to do here. Probably raise an exception or something.")
                        return
                    }
                    else -> {
                        println("BufferLock::wait, GL_TIMEOUT_EXPIRED")
                        // After the first time, need to start flushing, and wait for a looong time.
                        waitFlags = GL_SYNC_FLUSH_COMMANDS_BIT
                        waitDuration = oneSecondInNanoSeconds
                    }
                }
        } else
            glWaitSync(syncObj, 0, GL_TIMEOUT_IGNORED)
    }

    fun cleanup() = glDeleteSync(syncObj)
}

typealias GLsync = Long

const val oneSecondInNanoSeconds = 1_000_000_000L


class BufferLockManager(
        /** Whether it's the CPU (true) that updates, or the GPU (false) */
        val cpuUpdates: Boolean) {

    val bufferLocks = ArrayList<BufferLock>()

    fun destroy() {
        bufferLocks.forEach { it.cleanup() }
        bufferLocks.clear()
    }

    inline fun waitForAndLockRange(lockBeginBytes: Long, lockLength: Long, block: () -> Unit) {
        waitForLockedRange(lockBeginBytes, lockLength)
        block()
        lockRange(lockBeginBytes, lockLength)
    }

    fun waitForLockedRange(lockBeginBytes: Long, lockLength: Long) {
        val testRange = BufferRange(lockBeginBytes, lockLength)
        val iterator = bufferLocks.iterator()
        while (iterator.hasNext()) {
            val range = iterator.next()
            if (testRange overlaps range.range) {
                range.wait(cpuUpdates)
                range.cleanup()
                iterator.remove()
            }
        }
    }

    fun lockRange(lockBeginBytes: Long, lockLength: Long) {
        val newRange = BufferRange(lockBeginBytes, lockLength)
        val syncName = glFenceSync(GL_SYNC_GPU_COMMANDS_COMPLETE, 0)
        bufferLocks += BufferLock(newRange, syncName)
    }
}

