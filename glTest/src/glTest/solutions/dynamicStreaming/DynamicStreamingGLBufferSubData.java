/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package glTest.solutions.dynamicStreaming;

import static com.jogamp.opengl.GL2ES3.*;
import com.jogamp.opengl.GL4;
import glTest.framework.ApplicationState;
import glTest.framework.GLApi;
import glTest.framework.GLUtilities;
import glTest.framework.RingBuffer;
import glm.vec._2.Vec2;
import java.nio.ByteBuffer;
import static glTest.problems.DynamicStreamingProblem.vertsPerParticle;

/**
 *
 * @author elect
 */
public class DynamicStreamingGLBufferSubData extends DynamicStreamingSolution {

    @Override
    public boolean init(GL4 gl4) {

        super.init(gl4);

        // Uniform Buffer
        gl4.glGenBuffers(1, uniformBuffer);

        // Program
        program = GLUtilities.createProgram(gl4, SHADERS_ROOT, SHADER_SRC);

        if (program == 0) {
            System.err.println("Unable to initialize solution " + getName() + ", shader compilation/linking failed.");
            return false;
        }

        // Dynamic vertex buffer
        startDestOffset = 0;
        particleRingBuffer = new RingBuffer(GLApi.tripleBuffer, Vec2.SIZE * vertexCount);

        gl4.glGenBuffers(1, vertexBuffer);
        gl4.glBindBuffer(GL_ARRAY_BUFFER, vertexBuffer.get(0));
        gl4.glBufferData(GL_ARRAY_BUFFER, particleRingBuffer.getSize(), null, GL_DYNAMIC_DRAW);

        gl4.glGenVertexArrays(1, vao);
        gl4.glBindVertexArray(vao.get(0));

        ApplicationState.animator.setUpdateFPSFrames(2, System.out);

        return gl4.glGetError() == GL_NO_ERROR;
    }

    @Override
    public void render(GL4 gl4, ByteBuffer vertices) {

        // Program
        gl4.glUseProgram(program);

        // Uniforms
        constants.putFloat(Float.BYTES * 0, +2.0f / width);
        constants.putFloat(Float.BYTES * 1, -2.0f / height);

        gl4.glBindBuffer(GL_UNIFORM_BUFFER, uniformBuffer.get(0));
        gl4.glBufferData(GL_UNIFORM_BUFFER, constants.capacity(), constants, GL_DYNAMIC_DRAW);
        gl4.glBindBufferBase(GL_UNIFORM_BUFFER, Semantic.Uniform.CB0, uniformBuffer.get(0));

        // Input Layout
        gl4.glBindBuffer(GL_ARRAY_BUFFER, vertexBuffer.get(0));
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
        int startIndex = startDestOffset / Vec2.SIZE;

        for (int i = 0; i < particleCount; ++i) {

            int vertexOffset = i * vertsPerParticle;
            int srcOffset = vertexOffset * Vec2.SIZE;
            int dstOffset = startDestOffset + (i * particleSizeBytes);

            gl4.glBufferSubData(GL_ARRAY_BUFFER, dstOffset, particleSizeBytes, vertices.position(srcOffset));

            gl4.glDrawArrays(GL_TRIANGLES, startIndex + vertexOffset, vertsPerParticle);
        }

        startDestOffset = (startDestOffset + (particleCount * particleSizeBytes)) % particleRingBuffer.getSize();

        if (startDestOffset == 0) {
            gl4.glBufferData(GL_ARRAY_BUFFER, particleRingBuffer.getSize(), null, GL_DYNAMIC_DRAW);
        }
    }

    @Override
    public boolean shutdown(GL4 gl4) {

        gl4.glDisableVertexAttribArray(Semantic.Attr.POSITION);
        gl4.glDeleteVertexArrays(1, vao);

        gl4.glDeleteBuffers(1, vertexBuffer);

        gl4.glDeleteBuffers(1, uniformBuffer);
        gl4.glDeleteProgram(program);

        super.shutdown(gl4);

        return true;
    }

    @Override
    public String getName() {
        return "GLBufferSubData";
    }
}
