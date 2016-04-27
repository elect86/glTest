/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package glTest.solutions.untexturedObjects.mapPersistent;

import static com.jogamp.opengl.GL.GL_ARRAY_BUFFER;
import static com.jogamp.opengl.GL.GL_BLEND;
import static com.jogamp.opengl.GL.GL_CULL_FACE;
import static com.jogamp.opengl.GL.GL_DEPTH_TEST;
import static com.jogamp.opengl.GL.GL_ELEMENT_ARRAY_BUFFER;
import static com.jogamp.opengl.GL.GL_FLOAT;
import static com.jogamp.opengl.GL.GL_FRONT;
import static com.jogamp.opengl.GL.GL_MAP_INVALIDATE_RANGE_BIT;
import static com.jogamp.opengl.GL.GL_MAP_UNSYNCHRONIZED_BIT;
import static com.jogamp.opengl.GL.GL_MAP_WRITE_BIT;
import static com.jogamp.opengl.GL.GL_NO_ERROR;
import static com.jogamp.opengl.GL.GL_SCISSOR_TEST;
import static com.jogamp.opengl.GL.GL_STATIC_DRAW;
import static com.jogamp.opengl.GL.GL_TRIANGLES;
import static com.jogamp.opengl.GL.GL_UNSIGNED_INT;
import static com.jogamp.opengl.GL.GL_UNSIGNED_SHORT;
import static com.jogamp.opengl.GL3ES3.GL_SHADER_STORAGE_BUFFER;
import com.jogamp.opengl.GL4;
import static com.jogamp.opengl.GL4.GL_MAP_COHERENT_BIT;
import static com.jogamp.opengl.GL4.GL_MAP_PERSISTENT_BIT;
import com.jogamp.opengl.util.GLBuffers;
import glTest.framework.ApplicationState;
import common.BufferUtils;
import glTest.framework.GLApi;
import glTest.framework.GLUtilities;
import glTest.framework.RingBuffer;
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
public class UntexturedObjectsGLMapPersistent extends UntexturedObjectsSolution {

    private static final String SHADER_SRC = "map-persistent";
    protected static final String SHADERS_ROOT = "glTest/solutions/untexturedObjects/mapPersistent/shaders/";

    private class Buffer {

        public static final int VERTEX = 0;
        public static final int ELEMENT = 1;
        public static final int TRASFORM = 2;
        public static final int DRAW_ID = 3;
        public static final int MAX = 4;
    }

    private IntBuffer vertexArrayName = GLBuffers.newDirectIntBuffer(1),
            bufferName = GLBuffers.newDirectIntBuffer(Buffer.MAX);
    private RingBuffer ringBuffer;
    private ByteBuffer transformPtr;

    public UntexturedObjectsGLMapPersistent() {
        updateFps = 40;
    }

    @Override
    public boolean init(GL4 gl4, ByteBuffer vertices, ByteBuffer indices, int objectCount) {

        if (!super.init(gl4, vertices, indices, objectCount)) {
            return false;
        }

        if (!gl4.isExtensionAvailable("GL_ARB_buffer_storage")) {
            System.err.println("Unable to initialize solution " + getName() + ", ARB_buffer_storage unavailable.");
            return false;
        }

        // Program
        programName = GLUtilities.createProgram(gl4, SHADERS_ROOT, SHADER_SRC);

        if (programName == 0) {
            System.err.println("Unable to initialize solution " + getName() + ", shader compilation/linking failed.");
            return false;
        }

        // Buffers
        gl4.glGenVertexArrays(1, vertexArrayName);
        gl4.glBindVertexArray(vertexArrayName.get(0));

        gl4.glGenBuffers(Buffer.MAX, bufferName);

        gl4.glBindBuffer(GL_ARRAY_BUFFER, bufferName.get(Buffer.VERTEX));
        gl4.glBufferData(GL_ARRAY_BUFFER, vertices.capacity(), vertices, GL_STATIC_DRAW);

        gl4.glVertexAttribPointer(Semantic.Attr.POSITION, 3, GL_FLOAT, false, Vec3.SIZE * 2, 0);
        gl4.glVertexAttribPointer(Semantic.Attr.COLOR, 3, GL_FLOAT, false, Vec3.SIZE * 2, Vec3.SIZE);
        gl4.glEnableVertexAttribArray(Semantic.Attr.POSITION);
        gl4.glEnableVertexAttribArray(Semantic.Attr.COLOR);

        {
            IntBuffer drawIds = GLBuffers.newDirectIntBuffer(objectCount);
            for (int i = 0; i < objectCount; i++) {
                drawIds.put(i, i);
            }

            gl4.glBindBuffer(GL_ARRAY_BUFFER, bufferName.get(Buffer.DRAW_ID));
            gl4.glBufferData(GL_ARRAY_BUFFER, drawIds.capacity() * Integer.BYTES, drawIds, GL_STATIC_DRAW);

            gl4.glVertexAttribIPointer(Semantic.Attr.DRAW_ID, 1, GL_UNSIGNED_INT, Integer.BYTES, 0);
            gl4.glVertexAttribDivisor(Semantic.Attr.DRAW_ID, 1);
            gl4.glEnableVertexAttribArray(Semantic.Attr.DRAW_ID);

            BufferUtils.destroyDirectBuffer(drawIds);
        }

        gl4.glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, bufferName.get(Buffer.ELEMENT));
        gl4.glBufferData(GL_ELEMENT_ARRAY_BUFFER, indices.capacity(), indices, GL_STATIC_DRAW);

        gl4.glBindBufferBase(GL_SHADER_STORAGE_BUFFER, Semantic.Storage.TRANSFORM0_, bufferName.get(Buffer.TRASFORM));

        ringBuffer = new RingBuffer(GLApi.tripleBuffer, Mat4.SIZE * objectCount);
        int flags = GL_MAP_WRITE_BIT | GL_MAP_PERSISTENT_BIT | GL_MAP_COHERENT_BIT;
        gl4.glBufferStorage(GL_SHADER_STORAGE_BUFFER, ringBuffer.getSize(), null, flags);
        transformPtr = gl4.glMapBufferRange(GL_SHADER_STORAGE_BUFFER, 0, ringBuffer.getSize(), flags);

        return GLApi.getError(gl4) == GL_NO_ERROR;
    }

    @Override
    public void render(GL4 gl4, ByteBuffer transforms) {

        int count = transforms.capacity() / Mat4.SIZE;
        assert (count <= objectCount);

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

        ringBuffer.wait(gl4);

        for (int i = 0; i < count; i++) {

            int offset = ringBuffer.getSectorOffset() + i * Mat4.SIZE;
            
            transforms.position(i * Mat4.SIZE);
            transforms.limit(transforms.position() + Mat4.SIZE);
            transformPtr.position(offset);
            transformPtr.put(transforms);

            gl4.glDrawElementsInstancedBaseInstance(GL_TRIANGLES, indexCount, GL_UNSIGNED_SHORT, 0, 1, i);
        }

        ringBuffer.lockAndUpdate(gl4);
    }

    @Override
    public boolean shutdown(GL4 gl4) {

        gl4.glDisableVertexAttribArray(Semantic.Attr.POSITION);
        gl4.glDisableVertexAttribArray(Semantic.Attr.COLOR);
        gl4.glDisableVertexAttribArray(Semantic.Attr.DRAW_ID);

        gl4.glDeleteBuffers(Buffer.MAX, bufferName);
        gl4.glDeleteVertexArrays(1, vertexArrayName);
        gl4.glDeleteProgram(programName);

        return true;
    }

    @Override
    public String getName() {
        return "GLMapPersistent";
    }

}
