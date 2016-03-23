/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package glTest.framework;

import static com.jogamp.opengl.GL4ES3.GL_ALREADY_SIGNALED;
import static com.jogamp.opengl.GL4ES3.GL_CONDITION_SATISFIED;
import static com.jogamp.opengl.GL4ES3.GL_SYNC_FLUSH_COMMANDS_BIT;
import static com.jogamp.opengl.GL4ES3.GL_SYNC_GPU_COMMANDS_COMPLETE;
import static com.jogamp.opengl.GL4ES3.GL_WAIT_FAILED;
import com.jogamp.opengl.GL4;

/**
 *
 * @author elect
 */
public class RingBuffer {

    private int sectors;
    private int size;
    private int sectorSize;
    private int readId;
    private int writeId;
    private long[] fence;
    private final long oneSecondInNanoSeconds = 1_000_000_000;
    private int stalls;

    public RingBuffer(int sectors, int sectorSize) {
        this(sectors, sectorSize, 0, 1);
    }

    public RingBuffer(int sectors, int sectorSize, int bindId, int writeId) {
        this.sectors = sectors;
        this.size = sectors * sectorSize;
        this.sectorSize = sectorSize;
        this.readId = sectors > 1 ? bindId : 0;
        this.writeId = sectors > 1 ? writeId : 0;
        fence = new long[sectors];
    }

    public void wait(GL4 gl4) {
        if (fence[writeId] > 0) {
            int waitFlags = 0;
            long waitDuration = 0;
            while (true) {
                int waitRet = gl4.glClientWaitSync(fence[writeId], waitFlags, waitDuration);
                if (waitRet == GL_ALREADY_SIGNALED || waitRet == GL_CONDITION_SATISFIED) {
                    return;
                }
                if (waitRet == GL_WAIT_FAILED) {
                    System.err.println("Not sure what to do here. Probably fart an exception or suicide.");
                    return;
                }
                /**
                 * After the first time, need to start flushing, and wait for a
                 * looong time.
                 */
                stalls++;
                System.out.println("new stall, total " + stalls);
                waitFlags = GL_SYNC_FLUSH_COMMANDS_BIT;
                waitDuration = oneSecondInNanoSeconds;
            }
        }
    }

    public void lockAndUpdate(GL4 gl4) {
        lock(gl4);
        update();
    }

    public void lock(GL4 gl4) {
        /**
         * glDeleteSync will silently ignore a sync value of zero, but there is
         * no need to query OpenGL if not needed.
         */
        if (fence[readId] > 0) {
            gl4.glDeleteSync(fence[readId]);
        }
        fence[readId] = gl4.glFenceSync(GL_SYNC_GPU_COMMANDS_COMPLETE, 0);
    }

    public void update() {
        readId = (readId + 1) % sectors;
        writeId = (writeId + 1) % sectors;
    }

    public int getSectors() {
        return sectors;
    }

    public int getSize() {
        return size;
    }

    public int getSectorSize() {
        return sectorSize;
    }

    public int getReadId() {
        return readId;
    }

    public int getWriteId() {
        return writeId;
    }
    
    public int getReadOffset() {
        return readId * sectorSize;
    }

    public int getWriteOffset() {
        return writeId * sectorSize;
    }

    public int getStalls() {
        return stalls;
    }
}
