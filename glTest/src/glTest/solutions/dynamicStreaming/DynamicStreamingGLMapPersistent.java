/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package glTest.solutions.dynamicStreaming;

import com.jogamp.opengl.GL4;
import static com.jogamp.opengl.GL4.*;
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
public class DynamicStreamingGLMapPersistent extends DynamicStreamingSolution {

    private RingBuffer particleRingBuffer;
    private ByteBuffer vertexDataPtr;

    public DynamicStreamingGLMapPersistent() {
        updateFps = 58;
    }    
    
    @Override
    public boolean init(GL4 gl4) {

        super.init(gl4);

        if (!gl4.isExtensionAvailable("GL_ARB_buffer_storage")) {
            System.err.println("Unable to initialize solution '" + getName() + "', glBufferStorage() unavailable");
        }

        // Gen Buffers
        gl4.glGenBuffers(Buffer.MAX, bufferName);

        // Program
        program = GLUtilities.createProgram(gl4, SHADERS_ROOT, SHADER_SRC);

        if (program == 0) {
            System.err.println("Unable to initialize solution " + getName()
                    + ", shader compilation/linking failed.");
            return false;
        }

        // Dynamic vertex buffer
        gl4.glBindBuffer(GL_ARRAY_BUFFER, bufferName.get(Buffer.VERTEX));

        particleRingBuffer = new RingBuffer(GLApi.tripleBuffer, Vec2.SIZE * vertexCount);

        int flags = GL_MAP_WRITE_BIT | GL_MAP_PERSISTENT_BIT | GL_MAP_COHERENT_BIT;
        gl4.glBufferStorage(GL_ARRAY_BUFFER, particleRingBuffer.getSize(), null, flags);
        vertexDataPtr = gl4.glMapBufferRange(GL_ARRAY_BUFFER, 0, particleRingBuffer.getSize(), flags);

        gl4.glGenVertexArrays(1, vertexArrayName);
        gl4.glBindVertexArray(vertexArrayName.get(0));

        return GLApi.getError(gl4) == GL_NO_ERROR;
    }

    @Override
    public void render(GL4 gl4, ByteBuffer vertices) {

        // Program
        gl4.glUseProgram(program);

        // Uniforms
        constants.putFloat(Float.BYTES * 0, +2.0f / width);
        constants.putFloat(Float.BYTES * 1, -2.0f / height);

        gl4.glBindBuffer(GL_UNIFORM_BUFFER, bufferName.get(Buffer.UNIFORM));
        gl4.glBufferData(GL_UNIFORM_BUFFER, constants.capacity(), constants, GL_DYNAMIC_DRAW);
        gl4.glBindBufferBase(GL_UNIFORM_BUFFER, 0, bufferName.get(Buffer.UNIFORM));

        // Input Layout
        gl4.glBindBuffer(GL_ARRAY_BUFFER, bufferName.get(Buffer.VERTEX));
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

        /**
         * Need to wait for this area to become available. If we've sized things properly,
         * it will always be available right away.
         */
        particleRingBuffer.wait(gl4);

        for (int i = 0; i < particleCount; ++i) {

            int vertexOffset = i * vertsPerParticle;
            int dstOffset = startDestOffset + (i * particleSizeBytes);
            /**
             * 20% faster, 60 vs 47 fps.
             */
            for (int j = 0; j < particleSizeBytes; j++) {
                vertexDataPtr.put(dstOffset + j, vertices.get(vertexOffset * Vec2.SIZE + j));
            }
//            vertices.position(vertexOffset * Vec2.SIZE);
//            vertices.limit(vertices.position() + particleSizeBytes);
//            vertexDataPtr.position(dstOffset);
//            vertexDataPtr.put(vertices);

            gl4.glDrawArrays(GL_TRIANGLES, startIndex + vertexOffset, vertsPerParticle);
        }

        // Lock this area for the future.
        particleRingBuffer.lockAndUpdate(gl4);

        startDestOffset = (startDestOffset + (particleCount * particleSizeBytes)) % particleRingBuffer.getSize();
    }

    @Override
    public void render(GL4 gl4, float[][] vertices) {

        // Program
        gl4.glUseProgram(program);

        // Uniforms
        constants.putFloat(Float.BYTES * 0, +2.0f / width);
        constants.putFloat(Float.BYTES * 1, -2.0f / height);

        gl4.glBindBuffer(GL_UNIFORM_BUFFER, bufferName.get(Buffer.UNIFORM));
        gl4.glBufferData(GL_UNIFORM_BUFFER, constants.capacity(), constants, GL_DYNAMIC_DRAW);
        gl4.glBindBufferBase(GL_UNIFORM_BUFFER, 0, bufferName.get(Buffer.UNIFORM));

        // Input Layout
        gl4.glBindBuffer(GL_ARRAY_BUFFER, bufferName.get(Buffer.VERTEX));
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

        /**
         * Need to wait for this area to become available. If we've sized things properly,
         * it will always be available right away.
         */
        particleRingBuffer.wait(gl4);

        for (int i = 0; i < particleCount; ++i) {

            int vertexOffset = i * vertsPerParticle;
            int dstOffset = startDestOffset + (i * particleSizeBytes);
            
            vertexDataPtr.position(dstOffset);
            vertexDataPtr.asFloatBuffer().put(vertices[i]);

            gl4.glDrawArrays(GL_TRIANGLES, startIndex + vertexOffset, vertsPerParticle);
        }

        // Lock this area for the future.
        particleRingBuffer.lockAndUpdate(gl4);

        startDestOffset = (startDestOffset + (particleCount * particleSizeBytes)) % particleRingBuffer.getSize();
    }

    @Override
    public boolean shutdown(GL4 gl4) {

        gl4.glDisableVertexAttribArray(Semantic.Attr.POSITION);
        gl4.glDeleteVertexArrays(1, vertexArrayName);

        gl4.glBindBuffer(GL_ARRAY_BUFFER, bufferName.get(Buffer.VERTEX));
        gl4.glUnmapBuffer(GL_ARRAY_BUFFER);

        gl4.glDeleteBuffers(Buffer.MAX, bufferName);

        gl4.glDeleteProgram(program);

        super.shutdown(gl4);

        return true;
    }

    @Override
    public String getName() {
        return "GLMapPersistent";
    }
}
