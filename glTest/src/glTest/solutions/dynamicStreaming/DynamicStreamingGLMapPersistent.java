/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package glTest.solutions.dynamicStreaming;

import static com.jogamp.opengl.GL.GL_ARRAY_BUFFER;
import static com.jogamp.opengl.GL.GL_BLEND;
import static com.jogamp.opengl.GL.GL_CULL_FACE;
import static com.jogamp.opengl.GL.GL_DEPTH_TEST;
import static com.jogamp.opengl.GL.GL_DYNAMIC_DRAW;
import static com.jogamp.opengl.GL.GL_FLOAT;
import static com.jogamp.opengl.GL.GL_FRONT;
import static com.jogamp.opengl.GL.GL_MAP_INVALIDATE_RANGE_BIT;
import static com.jogamp.opengl.GL.GL_MAP_UNSYNCHRONIZED_BIT;
import static com.jogamp.opengl.GL.GL_MAP_WRITE_BIT;
import static com.jogamp.opengl.GL.GL_NO_ERROR;
import static com.jogamp.opengl.GL.GL_SCISSOR_TEST;
import static com.jogamp.opengl.GL.GL_TRIANGLES;
import static com.jogamp.opengl.GL2ES3.GL_UNIFORM_BUFFER;
import com.jogamp.opengl.GL3;
import com.jogamp.opengl.GL4;
import static com.jogamp.opengl.GL4.GL_MAP_COHERENT_BIT;
import static com.jogamp.opengl.GL4.GL_MAP_PERSISTENT_BIT;
import glTest.framework.BufferUtils;
import glTest.framework.GLApi;
import glTest.framework.GLUtilities;
import glTest.framework.RingBuffer;
import glm.vec._2.Vec2;
import java.nio.ByteBuffer;
import static glTest.problems.DynamicStreamingProblem.vertsPerParticle;
import glTest.solutions.DynamicStreamingSolution;

/**
 *
 * @author elect
 */
public class DynamicStreamingGLMapPersistent extends DynamicStreamingSolution {

    private RingBuffer ringBuffer;
    private ByteBuffer vertexDataPointer;

    @Override
    public boolean init(GL3 gl3, int maxVertexCount) {

        if (!gl3.isExtensionAvailable("GL_ARB_buffer_storage")) {
            System.err.println("Unable to initialize solution '" + getName()
                    + "', glBufferStorage(), i.e. v, unavailable");
        }

        // Uniform Buffer
        gl3.glGenBuffers(1, uniformBuffer);

        // Program
        String[] uniformNames = new String[]{"CB0"};
        uniformLocation = new int[1];
        program = GLUtilities.createProgram(gl3, "streaming_vb_gl_vs.glsl", "streaming_vb_gl_fs.glsl",
                uniformNames, uniformLocation);

        if (program == 0) {
            System.err.println("Unable to initialize solution " + getName()
                    + ", shader compilation/linking failed.");
            return false;
        }

        // Dynamic vertex buffer
        gl3.glGenBuffers(1, vertexBuffer);
        gl3.glBindBuffer(GL_ARRAY_BUFFER, vertexBuffer.get(0));

        ringBuffer = new RingBuffer(GLApi.tripleBuffer, Vec2.SIZE * maxVertexCount);
//        particleBufferSize = GLApi.tripleBuffer * Vec2.SIZE * maxVertexCount;
        particleBufferSize = ringBuffer.size;

        int flags = GL_MAP_WRITE_BIT | GL_MAP_PERSISTENT_BIT | GL_MAP_COHERENT_BIT;
        ((GL4) gl3).glBufferStorage(GL_ARRAY_BUFFER, particleBufferSize, null, flags);
        vertexDataPointer = gl3.glMapBufferRange(GL_ARRAY_BUFFER, 0, particleBufferSize, flags);

        gl3.glGenVertexArrays(1, vao);
        gl3.glBindVertexArray(vao.get(0));

        return gl3.glGetError() == GL_NO_ERROR;
    }

    @Override
    public void render(GL3 gl3, ByteBuffer vertices) {

        // Program
        gl3.glUseProgram(program);

        // Uniforms
        constants.putFloat(Float.BYTES * 0, +2.0f / width);
        constants.putFloat(Float.BYTES * 1, -2.0f / height);

        gl3.glBindBuffer(GL_UNIFORM_BUFFER, uniformBuffer.get(0));
        gl3.glBufferData(GL_UNIFORM_BUFFER, constants.capacity(), constants, GL_DYNAMIC_DRAW);
        gl3.glBindBufferBase(GL_UNIFORM_BUFFER, 0, uniformBuffer.get(0));

        // Input Layout
        gl3.glBindBuffer(GL_ARRAY_BUFFER, vertexBuffer.get(0));
        gl3.glVertexAttribPointer(0, 2, GL_FLOAT, false, Vec2.SIZE, 0);
        gl3.glEnableVertexAttribArray(0);

        // Rasterizer State
        gl3.glDisable(GL_CULL_FACE);
        gl3.glCullFace(GL_FRONT);
        gl3.glDisable(GL_SCISSOR_TEST);
        gl3.glViewport(0, 0, width, height);

        // Blend State
        gl3.glDisable(GL_BLEND);
        gl3.glColorMask(true, true, true, true);

        // Depth Stencil State
        gl3.glDisable(GL_DEPTH_TEST);
        gl3.glDepthMask(false);

        int particleCount = (vertices.capacity() / Vec2.SIZE) / vertsPerParticle;
        int particleSizeBytes = vertsPerParticle * Vec2.SIZE;
        int startIndex = startDestOffset / Vec2.SIZE;

        ringBuffer.wait(gl3);

        for (int i = 0; i < particleCount; ++i) {

            int vertexOffset = i * vertsPerParticle;
            int dstOffset = startDestOffset + (i * particleSizeBytes);

            for (int j = 0; j < particleSizeBytes; j++) {
                vertexDataPointer.put(dstOffset+ j, vertices.get(vertexOffset + j));
            }

            gl3.glDrawArrays(GL_TRIANGLES, startIndex + vertexOffset, vertsPerParticle);
        }

        ringBuffer.lockAndUpdate(gl3);

        startDestOffset = (startDestOffset + (particleCount * particleSizeBytes)) % particleBufferSize;
    }

    @Override
    public void shutdown(GL3 gl3) {
    
        gl3.glDisableVertexAttribArray(0);
        gl3.glDeleteVertexArrays(1, vao);
        
        gl3.glBindBuffer(GL_ARRAY_BUFFER, vertexBuffer.get(0));
        gl3.glUnmapBuffer(GL_ARRAY_BUFFER);
        gl3.glDeleteBuffers(1, vertexBuffer);
        
        gl3.glDeleteBuffers(1, uniformBuffer);
        gl3.glDeleteProgram(program);
        
        BufferUtils.destroyDirectBuffer(vao);
        BufferUtils.destroyDirectBuffer(vertexBuffer);
        BufferUtils.destroyDirectBuffer(uniformBuffer);
    }

    @Override
    public String getName() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public boolean supportsApi(int glApi) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

}
