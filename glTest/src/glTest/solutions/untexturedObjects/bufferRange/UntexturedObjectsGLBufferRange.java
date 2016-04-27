/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package glTest.solutions.untexturedObjects.bufferRange;

import static com.jogamp.opengl.GL.GL_BLEND;
import static com.jogamp.opengl.GL.GL_CULL_FACE;
import static com.jogamp.opengl.GL.GL_DEPTH_TEST;
import static com.jogamp.opengl.GL.GL_FLOAT;
import static com.jogamp.opengl.GL.GL_FRONT;
import static com.jogamp.opengl.GL.GL_SCISSOR_TEST;
import static com.jogamp.opengl.GL2ES3.*;
import com.jogamp.opengl.GL4;
import com.jogamp.opengl.util.GLBuffers;
import glTest.framework.ApplicationState;
import common.BufferUtils;
import glTest.framework.GLApi;
import glTest.framework.GLUtilities;
import glTest.solutions.untexturedObjects.UntexturedObjectsSolution;
import glm.glm;
import glm.mat._4.Mat4;
import glm.vec._3.Vec3;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;

/**
 *
 * @author GBarbieri
 */
public class UntexturedObjectsGLBufferRange extends UntexturedObjectsSolution {

    private static final String SHADER_SRC = "buffer-range";
    protected static final String SHADERS_ROOT = "glTest/solutions/untexturedObjects/bufferRange/shaders/";

    private class Buffer {

        public static final int VERTEX = 0;
        public static final int INDEX = 1;
        public static final int UNIFORM = 2;
        public static final int MAX = 3;
    }

    public UntexturedObjectsGLBufferRange() {
        updateFps = 11;
    }

    private IntBuffer vertexArrayName = GLBuffers.newDirectIntBuffer(1),
            bufferName = GLBuffers.newDirectIntBuffer(Buffer.MAX);
    private ByteBuffer storage;
    private int matrixStride, maxBatchSize;

    @Override
    public boolean init(GL4 gl4, ByteBuffer vertices, ByteBuffer indices, int objectCount) {

        if (!super.init(gl4, vertices, indices, objectCount)) {
            return false;
        }

        // Program
        programName = GLUtilities.createProgram(gl4, SHADERS_ROOT, SHADER_SRC);

        if (programName == 0) {
            System.err.println("Unable to initialize solution " + getName() + ", shader compilation/linking failed.");
            return false;
        }

        gl4.glGenVertexArrays(1, vertexArrayName);
        gl4.glBindVertexArray(vertexArrayName.get(0));

        gl4.glGenBuffers(Buffer.MAX, bufferName);

        gl4.glBindBuffer(GL_ARRAY_BUFFER, bufferName.get(Buffer.VERTEX));
        gl4.glBufferData(GL_ARRAY_BUFFER, vertices.capacity(), vertices, GL_STATIC_DRAW);

        gl4.glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, bufferName.get(Buffer.INDEX));
        gl4.glBufferData(GL_ELEMENT_ARRAY_BUFFER, indices.capacity(), indices, GL_STATIC_DRAW);

        IntBuffer uniformBufferOffsetAlignment = GLBuffers.newDirectIntBuffer(1);
        IntBuffer maxUniformBlockSize = GLBuffers.newDirectIntBuffer(1);
        gl4.glGetIntegerv(GL_UNIFORM_BUFFER_OFFSET_ALIGNMENT, uniformBufferOffsetAlignment);
        gl4.glGetIntegerv(GL_MAX_UNIFORM_BLOCK_SIZE, maxUniformBlockSize);

        matrixStride = Math.max(Mat4.SIZE, uniformBufferOffsetAlignment.get(0));
        maxBatchSize = maxUniformBlockSize.get(0) / matrixStride;

        final int maxSupportedBatchSize = Math.min(64 * 64 * 64, objectCount);

        maxBatchSize = Math.min(maxBatchSize, maxSupportedBatchSize);

        storage = GLBuffers.newDirectByteBuffer(matrixStride * maxBatchSize);

        gl4.glGenVertexArrays(1, vertexArrayName);
        gl4.glBindVertexArray(vertexArrayName.get(0));

        BufferUtils.destroyDirectBuffer(uniformBufferOffsetAlignment);
        BufferUtils.destroyDirectBuffer(maxUniformBlockSize);

        return GLApi.getError(gl4) == GL_NO_ERROR;
    }

    @Override
    public void render(GL4 gl4, ByteBuffer transforms) {

        int xformCount = transforms.capacity() / Mat4.SIZE;
        assert (xformCount <= objectCount);

        // Program
        {
            dir.set(-0.5f, -1, 1);
            at.set(0, 0, 0);
            up.set(0, 0, 1);
            dir.normalize();
            at.sub(dir.mul(250), eye);
            glm.lookAt(eye, at, up, view);

            proj.mul(view, viewProj);
        }
        gl4.glUseProgram(programName);
        gl4.glUniformMatrix4fv(Semantic.Uniform.TRANSFORM0, 1, false, viewProj.toDfb(matBuffer));

        // Input Layout
        gl4.glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, bufferName.get(Buffer.INDEX));
        gl4.glBindBuffer(GL_ARRAY_BUFFER, bufferName.get(Buffer.VERTEX));
        gl4.glVertexAttribPointer(Semantic.Attr.POSITION, 3, GL_FLOAT, false, Vec3.SIZE * 2, 0);
        gl4.glVertexAttribPointer(Semantic.Attr.COLOR, 3, GL_FLOAT, false, Vec3.SIZE * 2, Vec3.SIZE);
        gl4.glEnableVertexAttribArray(Semantic.Attr.POSITION);
        gl4.glEnableVertexAttribArray(Semantic.Attr.COLOR);

        // Rasterizer State
        gl4.glEnable(GL_CULL_FACE);
        gl4.glCullFace(GL_FRONT);
        gl4.glDisable(GL_SCISSOR_TEST);

        // Blend State
        gl4.glDisable(GL_BLEND);
        gl4.glColorMask(true, true, true, true);

        // Depth Stencil State
        gl4.glEnable(GL_DEPTH_TEST);
        gl4.glDepthMask(true);

        gl4.glBindBuffer(GL_UNIFORM_BUFFER, bufferName.get(Buffer.UNIFORM));

        for (int batchStart = 0; batchStart < xformCount; batchStart += maxBatchSize) {

            int batchCount = Math.min(xformCount - batchStart, maxBatchSize);

            for (int batch = 0; batch < batchCount; batch++) {

                for (int byte_ = 0; byte_ < Mat4.SIZE; byte_++) {
                    storage.put(matrixStride * batch + byte_, transforms.get((batchStart + batch) * Mat4.SIZE + byte_));
                }
            }

            gl4.glBufferData(GL_UNIFORM_BUFFER, storage.capacity(), storage, GL_DYNAMIC_DRAW);

            for (int batch = 0; batch < batchCount; ++batch) {

                gl4.glBindBufferRange(GL_UNIFORM_BUFFER, Semantic.Uniform.TRANSFORM1, bufferName.get(Buffer.UNIFORM),
                        matrixStride * batch, Mat4.SIZE);

                gl4.glDrawElements(GL_TRIANGLES, indexCount, GL_UNSIGNED_SHORT, 0);
            }
        }
    }

    @Override
    public boolean shutdown(GL4 gl4) {

        gl4.glDisableVertexAttribArray(Semantic.Attr.POSITION);
        gl4.glDisableVertexAttribArray(Semantic.Attr.COLOR);

        gl4.glDeleteBuffers(Buffer.MAX, bufferName);

        gl4.glDeleteVertexArrays(1, vertexArrayName);

        gl4.glDeleteProgram(programName);

        BufferUtils.destroyDirectBuffer(bufferName);
        BufferUtils.destroyDirectBuffer(vertexArrayName);
        BufferUtils.destroyDirectBuffer(storage);

        return true;
    }

    @Override
    public String getName() {
        return "GLBufferRange";
    }

}
