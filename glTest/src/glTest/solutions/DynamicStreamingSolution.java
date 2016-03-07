/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package glTest.solutions;

import com.jogamp.opengl.GL4;
import com.jogamp.opengl.util.GLBuffers;
import glm.vec._4.Vec4;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;

/**
 *
 * @author GBarbieri
 */
public abstract class DynamicStreamingSolution extends Solution {

    protected IntBuffer uniformBuffer = GLBuffers.newDirectIntBuffer(1);
    protected IntBuffer vertexBuffer = GLBuffers.newDirectIntBuffer(1);
    protected int program;
    protected IntBuffer vao = GLBuffers.newDirectIntBuffer(1);
    protected int[] uniformLocation;
    protected int startDestOffset;
    protected int particleBufferSize;
    protected ByteBuffer constants = GLBuffers.newDirectByteBuffer(Vec4.SIZE);
    
    public abstract boolean init(GL4 gl4, int maxVertexCount);

    public abstract void render(GL4 gl4, ByteBuffer vertices);

    @Override
    public abstract void shutdown(GL4 gl4);

    @Override
    public abstract String getName();

    @Override
    public String getProblemName() {
        return "DynamicStreaming";
    }

}
