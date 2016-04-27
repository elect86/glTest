/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package glTest.solutions.dynamicStreaming;

import com.jogamp.opengl.GL4;
import com.jogamp.opengl.util.GLBuffers;
import common.BufferUtils;
import glTest.framework.RingBuffer;
import glTest.problems.DynamicStreamingProblem;
import glTest.solutions.Solution;
import glm.vec._4.Vec4;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;

/**
 *
 * @author GBarbieri
 */
public abstract class DynamicStreamingSolution extends Solution {

    protected static final String SHADERS_ROOT = "glTest/solutions/dynamicStreaming/shaders/";

    protected class Buffer {

        public static final int UNIFORM = 0;
        public static final int VERTEX = 1;
        public static final int MAX = 2;
    }

    protected IntBuffer bufferName, vertexArrayName;
    protected int program, startDestOffset, vertexCount = DynamicStreamingProblem.vertexCount;
    protected ByteBuffer constants;
    protected final String SHADER_SRC = "streaming";
    protected RingBuffer particleRingBuffer;

    @Override
    public boolean init(GL4 gl4) {

        bufferName = GLBuffers.newDirectIntBuffer(Buffer.MAX);
        vertexArrayName = GLBuffers.newDirectIntBuffer(1);
        constants = GLBuffers.newDirectByteBuffer(Vec4.SIZE);

        return true;
    }

    public abstract void render(GL4 gl4, ByteBuffer vertices);

    public void render(GL4 gl4, float[][] vertices) {

    }

    @Override
    public boolean shutdown(GL4 gl4) {

        BufferUtils.destroyDirectBuffer(vertexArrayName);
        BufferUtils.destroyDirectBuffer(bufferName);
        BufferUtils.destroyDirectBuffer(constants);

        return true;
    }

    @Override
    public abstract String getName();

    @Override
    public String getProblemName() {
        return "DynamicStreaming";
    }
}
