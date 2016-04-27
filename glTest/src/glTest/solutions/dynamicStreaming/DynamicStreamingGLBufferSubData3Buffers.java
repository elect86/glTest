/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package glTest.solutions.dynamicStreaming;

import static com.jogamp.opengl.GL2ES3.*;
import com.jogamp.opengl.GL4;
import com.jogamp.opengl.util.GLBuffers;
import glTest.framework.ApplicationState;
import common.BufferUtils;
import glTest.framework.GLApi;
import glTest.framework.GLUtilities;
import glm.vec._2.Vec2;
import java.nio.ByteBuffer;
import static glTest.problems.DynamicStreamingProblem.vertsPerParticle;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;

/**
 *
 * @author elect
 */
public class DynamicStreamingGLBufferSubData3Buffers extends DynamicStreamingSolution {

    protected class Buffer_ {

        public static final int UNIFORM = 0;
        public static final int VERTEX0 = 1;
        public static final int VERTEX1 = 2;
        public static final int MAX = 3;
    }
    private IntBuffer bufferName_ = GLBuffers.newDirectIntBuffer(Buffer_.MAX);
    private FloatBuffer floatBuffer;
    private int writeId = 0, readId = 1;

    @Override
    public boolean init(GL4 gl4) {

        super.init(gl4);

        // Gen Buffers
        gl4.glGenBuffers(Buffer_.MAX, bufferName_);

        // Program
        program = GLUtilities.createProgram(gl4, SHADERS_ROOT, SHADER_SRC);

        if (program == 0) {
            System.err.println("Unable to initialize solution " + getName() + ", shader compilation/linking failed.");
            return false;
        }

        // Dynamic vertex buffer        
        for (int i = Buffer_.VERTEX0; i < Buffer_.MAX; i++) {
            gl4.glBindBuffer(GL_ARRAY_BUFFER, bufferName_.get(i));
            gl4.glBufferData(GL_ARRAY_BUFFER, Vec2.SIZE * vertexCount, null, GL_DYNAMIC_DRAW);
        }

        gl4.glGenVertexArrays(1, vertexArrayName);
        gl4.glBindVertexArray(vertexArrayName.get(0));

        floatBuffer = GLBuffers.newDirectFloatBuffer(vertsPerParticle * Vec2.SIZE / Float.BYTES);

        ApplicationState.animator.setUpdateFPSFrames(1, System.out);

        return GLApi.getError(gl4) == GL_NO_ERROR;
    }

    @Override
    public void render(GL4 gl4, ByteBuffer vertices) {
        System.out.println("writeId: " + writeId);
        System.out.println("readId: " + readId);
        // Program
        gl4.glUseProgram(program);

        // Uniforms
        constants.putFloat(Float.BYTES * 0, +2.0f / width);
        constants.putFloat(Float.BYTES * 1, -2.0f / height);

        gl4.glBindBuffer(GL_UNIFORM_BUFFER, bufferName_.get(Buffer_.UNIFORM));
        gl4.glBufferData(GL_UNIFORM_BUFFER, constants.capacity(), constants, GL_DYNAMIC_DRAW);
        gl4.glBindBufferBase(GL_UNIFORM_BUFFER, Semantic.Uniform.CB0, bufferName_.get(Buffer_.UNIFORM));

        // Input Layout
        gl4.glBindBuffer(GL_ARRAY_BUFFER, bufferName_.get(Buffer_.VERTEX0 + readId));
        gl4.glVertexAttribPointer(Semantic.Attr.POSITION, 2, GL_FLOAT, false, Vec2.SIZE, 0);
        gl4.glEnableVertexAttribArray(Semantic.Attr.POSITION);

        // Rasterizer State
        gl4.glDisable(GL_CULL_FACE);
        gl4.glCullFace(GL_FRONT);
        gl4.glDisable(GL_SCISSOR_TEST);
        gl4.glViewport(0, 0, width, height);

        // Blend State
        gl4.glDisable(GL_BLEND);
        gl4.glColorMask(true, true, true, true);

        // Depth Stencil State
        gl4.glDisable(GL_DEPTH_TEST);
        gl4.glDepthMask(false);

        int particleCount = vertexCount / vertsPerParticle;
        int particleSizeBytes = vertsPerParticle * Vec2.SIZE;

        gl4.glBindBuffer(GL_ARRAY_BUFFER, bufferName_.get(Buffer_.VERTEX0 + writeId));

        for (int i = 0; i < particleCount; ++i) {

            int vertexOffset = i * vertsPerParticle;
            int srcOffset = vertexOffset * Vec2.SIZE;

            gl4.glBufferSubData(GL_ARRAY_BUFFER, i * particleSizeBytes, particleSizeBytes, vertices.position(srcOffset));

            gl4.glDrawArrays(GL_TRIANGLES, vertexOffset, vertsPerParticle);
        }
        bufferName_.position(Buffer_.VERTEX0 + readId);
        gl4.glDeleteBuffers(1, bufferName_);
        gl4.glGenBuffers(1, bufferName_);
        // Dynamic vertex buffer        
        gl4.glBindBuffer(GL_ARRAY_BUFFER, bufferName_.get(Buffer_.VERTEX0 + readId));
        gl4.glBufferData(GL_ARRAY_BUFFER, Vec2.SIZE * vertexCount, null, GL_DYNAMIC_DRAW);

        writeId = (++writeId) % 2;
        readId = (++readId) % 2;
    }

    @Override
    public void render(GL4 gl4, float[][] vertices) {

        // Program
        gl4.glUseProgram(program);

        // Uniforms
        constants.putFloat(Float.BYTES * 0, +2.0f / width);
        constants.putFloat(Float.BYTES * 1, -2.0f / height);

        gl4.glBindBuffer(GL_UNIFORM_BUFFER, bufferName_.get(Buffer_.UNIFORM));
        gl4.glBufferData(GL_UNIFORM_BUFFER, constants.capacity(), constants, GL_DYNAMIC_DRAW);
        gl4.glBindBufferBase(GL_UNIFORM_BUFFER, Semantic.Uniform.CB0, bufferName_.get(Buffer_.UNIFORM));

        // Input Layout
        gl4.glBindBuffer(GL_ARRAY_BUFFER, bufferName_.get(Buffer_.VERTEX0));
        gl4.glVertexAttribPointer(Semantic.Attr.POSITION, 2, GL_FLOAT, false, Vec2.SIZE, 0);
        gl4.glEnableVertexAttribArray(Semantic.Attr.POSITION);

        // Rasterizer State
        gl4.glDisable(GL_CULL_FACE);
        gl4.glCullFace(GL_FRONT);
        gl4.glDisable(GL_SCISSOR_TEST);
        gl4.glViewport(0, 0, width, height);

        // Blend State
        gl4.glDisable(GL_BLEND);
        gl4.glColorMask(true, true, true, true);

        // Depth Stencil State
        gl4.glDisable(GL_DEPTH_TEST);
        gl4.glDepthMask(false);

        int particleCount = vertexCount / vertsPerParticle;
        int particleSizeBytes = vertsPerParticle * Vec2.SIZE;
        int startVertex = startDestOffset / Vec2.SIZE;

        for (int i = 0; i < particleCount; ++i) {

            int vertexOffset = i * vertsPerParticle;
            int dstOffset = startDestOffset + (i * particleSizeBytes);

            for (int j = 0; j < (vertsPerParticle * 2); j++) {
                floatBuffer.put(j, vertices[i][j]);
            }
//            floatBuffer.put(vertices[i]).position(0);

            gl4.glBufferSubData(GL_ARRAY_BUFFER, dstOffset, particleSizeBytes, floatBuffer);

            gl4.glDrawArrays(GL_TRIANGLES, startVertex + vertexOffset, vertsPerParticle);
        }

        startDestOffset = (startDestOffset + (particleCount * particleSizeBytes)) % particleRingBuffer.getSize();

        if (startDestOffset == 0) {
            gl4.glBufferData(GL_ARRAY_BUFFER, particleRingBuffer.getSize(), null, GL_DYNAMIC_DRAW);
        }
    }

    @Override
    public boolean shutdown(GL4 gl4) {

        gl4.glDisableVertexAttribArray(Semantic.Attr.POSITION);
        gl4.glDeleteVertexArrays(1, vertexArrayName);

        gl4.glDeleteBuffers(Buffer_.MAX, bufferName_);

        gl4.glDeleteProgram(program);

        BufferUtils.destroyDirectBuffer(floatBuffer);

        super.shutdown(gl4);

        return true;
    }

    @Override
    public String getName() {
        return "GLBufferSubData 3 buffers";
    }
}
