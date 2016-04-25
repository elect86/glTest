/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package glTest.solutions.untexturedObjects.bufferStorage;

import static com.jogamp.opengl.GL.GL_ARRAY_BUFFER;
import static com.jogamp.opengl.GL.GL_BLEND;
import static com.jogamp.opengl.GL.GL_CULL_FACE;
import static com.jogamp.opengl.GL.GL_DEPTH_TEST;
import static com.jogamp.opengl.GL.GL_ELEMENT_ARRAY_BUFFER;
import static com.jogamp.opengl.GL.GL_FLOAT;
import static com.jogamp.opengl.GL.GL_FRONT;
import static com.jogamp.opengl.GL.GL_MAP_WRITE_BIT;
import static com.jogamp.opengl.GL.GL_NO_ERROR;
import static com.jogamp.opengl.GL.GL_SCISSOR_TEST;
import static com.jogamp.opengl.GL.GL_STATIC_DRAW;
import static com.jogamp.opengl.GL.GL_TRIANGLES;
import static com.jogamp.opengl.GL.GL_UNSIGNED_INT;
import static com.jogamp.opengl.GL.GL_UNSIGNED_SHORT;
import static com.jogamp.opengl.GL3ES3.GL_DRAW_INDIRECT_BUFFER;
import static com.jogamp.opengl.GL3ES3.GL_SHADER_STORAGE_BUFFER;
import com.jogamp.opengl.GL4;
import static com.jogamp.opengl.GL4.GL_CLIENT_MAPPED_BUFFER_BARRIER_BIT;
import static com.jogamp.opengl.GL4.GL_DYNAMIC_STORAGE_BIT;
import static com.jogamp.opengl.GL4.GL_MAP_PERSISTENT_BIT;
import com.jogamp.opengl.util.GLBuffers;
import glTest.framework.ApplicationState;
import glTest.framework.BufferUtils;
import glTest.framework.DrawElementsIndirectCommand;
import glTest.framework.GLApi;
import glTest.framework.GLUtilities;
import glTest.framework.RingBuffer;
import glTest.solutions.untexturedObjects.UntexturedObjectsSolution;
import glf.Vertex_v3fn3f;
import glm.glm;
import glm.mat._4.Mat4;
import glm.vec._3.Vec3;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;

/**
 *
 * @author GBarbieri
 */
public class UntexturedObjectsGLBufferStorage extends UntexturedObjectsSolution {

    private static final String SHADER_SRC = "buffer-storage";
    protected static final String SHADERS_ROOT = "glTest/solutions/untexturedObjects/bufferStorage/shaders/";

    private class Buffer {

        public static final int VERTEX = 0;
        public static final int ELEMENT = 1;
        public static final int DRAW_ID = 2;
        public static final int TRASFORM = 3;
        public static final int INDIRECT_COMMAND = 4;
        public static final int MAX = 5;
    }

    private IntBuffer vertexArrayName = GLBuffers.newDirectIntBuffer(1),
            bufferName = GLBuffers.newDirectIntBuffer(Buffer.MAX);
    private boolean useShaderDrawParameters;
    private ByteBuffer transformPtr, commandBuffer;
    private RingBuffer transformRingBuffer;

    public UntexturedObjectsGLBufferStorage(boolean useShaderDrawParameters) {
        this.useShaderDrawParameters = useShaderDrawParameters;
    }

    @Override
    public boolean init(GL4 gl4, ByteBuffer vertices, ByteBuffer indices, int objectCount) {

        if (!gl4.isExtensionAvailable("GL_ARB_buffer_storage")) {
            System.err.println("Unable to initialize solution " + getName() + ", ARB_buffer_storage unavailable.");
            return false;
        }

        if (!super.init(gl4, vertices, indices, objectCount)) {
            return false;
        }

        if (useShaderDrawParameters && !gl4.isExtensionAvailable("GL_ARB_shader_draw_parameters")) {
            System.err.println("Unable to initialize solution, ARB_shader_draw_parameters is required but not available.");
            return false;
        }

        // Program
        programName = GLUtilities.createProgram(gl4, SHADERS_ROOT,
                SHADER_SRC + (useShaderDrawParameters ? "-SDP" : "-NoSDP"), SHADER_SRC);

        if (programName == 0) {
            System.err.println("Unable to initialize solution " + getName() + ", shader compilation/linking failed.");
            return false;
        }

        gl4.glGenVertexArrays(1, vertexArrayName);
        gl4.glBindVertexArray(vertexArrayName.get(0));

        // Buffers
        gl4.glGenBuffers(Buffer.MAX, bufferName);

        gl4.glBindBuffer(GL_ARRAY_BUFFER, bufferName.get(Buffer.VERTEX));
        gl4.glBufferData(GL_ARRAY_BUFFER, vertices.capacity(), vertices, GL_STATIC_DRAW);

        gl4.glVertexAttribPointer(Semantic.Attr.POSITION, 3, GL_FLOAT, false, Vertex_v3fn3f.SIZE, 0);
        gl4.glVertexAttribPointer(Semantic.Attr.COLOR, 3, GL_FLOAT, false, Vertex_v3fn3f.SIZE, Vec3.SIZE);
        gl4.glEnableVertexAttribArray(Semantic.Attr.POSITION);
        gl4.glEnableVertexAttribArray(Semantic.Attr.COLOR);

        // If we aren't using shader draw parameters, use the workaround instead.
        if (!useShaderDrawParameters) {

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

        commandBuffer = GLBuffers.newDirectByteBuffer(DrawElementsIndirectCommand.SIZE * objectCount);
        gl4.glBindBuffer(GL_DRAW_INDIRECT_BUFFER, bufferName.get(Buffer.INDIRECT_COMMAND));
        gl4.glBufferStorage(GL_DRAW_INDIRECT_BUFFER, commandBuffer.capacity(), null, GL_DYNAMIC_STORAGE_BIT);
        
        transformRingBuffer = new RingBuffer(GLApi.tripleBuffer, Mat4.SIZE * objectCount);
        int mapFlags = GL_MAP_WRITE_BIT | GL_MAP_PERSISTENT_BIT;
        int createFlags = mapFlags | GL_DYNAMIC_STORAGE_BIT;

        gl4.glBindBuffer(GL_SHADER_STORAGE_BUFFER, bufferName.get(Buffer.TRASFORM));
        gl4.glBufferStorage(GL_SHADER_STORAGE_BUFFER, transformRingBuffer.getSize(), null, createFlags);
        transformPtr = gl4.glMapBufferRange(GL_SHADER_STORAGE_BUFFER, 0, transformRingBuffer.getSize(), mapFlags);

        ApplicationState.animator.setUpdateFPSFrames(useShaderDrawParameters ? 70 : 200, System.out);

        return GLApi.getError(gl4) == GL_NO_ERROR;
    }

    @Override
    public void render(GL4 gl4, ByteBuffer transforms) {
        
        int xformCount = transforms.capacity() / Mat4.SIZE;
        assert (xformCount <= transformRingBuffer.getSize());

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
        
        for (int u = 0; u < objectCount; u++) {

            int offset = u * DrawElementsIndirectCommand.SIZE;
            commandBuffer.putInt(offset + DrawElementsIndirectCommand.OFFSET_COUNT, indexCount);
            commandBuffer.putInt(offset + DrawElementsIndirectCommand.OFFSET_INSTANCE_COUNT, 1);
            commandBuffer.putInt(offset + DrawElementsIndirectCommand.OFFSET_FIRST_INDEX, 0);
            commandBuffer.putInt(offset + DrawElementsIndirectCommand.OFFSET_BASE_VERTEX, 0);
            commandBuffer.putInt(offset + DrawElementsIndirectCommand.OFFSET_BASE_INSTANCE, useShaderDrawParameters ? 0 : u);
        }

        gl4.glBindBuffer(GL_DRAW_INDIRECT_BUFFER, bufferName.get(Buffer.INDIRECT_COMMAND));
        gl4.glBufferSubData(GL_DRAW_INDIRECT_BUFFER, 0, commandBuffer.capacity(), commandBuffer.rewind());

        transformRingBuffer.wait(gl4);

//        for (int i = 0; i < Mat4.SIZE * objectCount; i++) {
//            transformPtr.put(transformRingBuffer.getSectorOffset() + i, transforms.get(i));
//        }
        transformPtr.position(transformRingBuffer.getSectorOffset());
        transformPtr.put(transforms);
        transformPtr.position(0);
        transforms.position(0);
//        gl4.glBindBuffer(GL_SHADER_STORAGE_BUFFER, bufferName.get(Buffer.TRASFORM));
//        gl4.glBufferSubData(GL_SHADER_STORAGE_BUFFER, transformRingBuffer.getSectorOffset(), transforms.capacity(), 
//                transforms);

        gl4.glBindBufferRange(GL_SHADER_STORAGE_BUFFER, Semantic.Storage.TRANSFORM0_, bufferName.get(Buffer.TRASFORM),
                transformRingBuffer.getSectorOffset(), transformRingBuffer.getSectorSize());

        // We didn't use MAP_COHERENT here.
        gl4.glMemoryBarrier(GL_CLIENT_MAPPED_BUFFER_BARRIER_BIT);

//        ByteBuffer ptr = ByteBuffer.allocate(Long.BYTES).order(ByteOrder.LITTLE_ENDIAN);
//        ptr.asLongBuffer().put(0, 0);
//        ptr.put(0, commandRingBuffer.getSectorOffset());
        gl4.glMultiDrawElementsIndirect(GL_TRIANGLES, GL_UNSIGNED_SHORT, null, xformCount, 0);

        transformRingBuffer.lockAndUpdate(gl4);
//        commandRingBuffer.lockAndUpdate(gl4);
    }

    @Override
    public boolean shutdown(GL4 gl4) {

        gl4.glDisableVertexAttribArray(Semantic.Attr.POSITION);
        gl4.glDisableVertexAttribArray(Semantic.Attr.COLOR);
        if (!useShaderDrawParameters) {
            gl4.glDisableVertexAttribArray(Semantic.Attr.DRAW_ID);
        }

//        gl4.glBindBuffer(GL_DRAW_INDIRECT_BUFFER, bufferName.get(Buffer.INDIRECT_COMMAND));
//        gl4.glUnmapBuffer(GL_DRAW_INDIRECT_BUFFER);
        gl4.glBindBuffer(GL_SHADER_STORAGE_BUFFER, bufferName.get(Buffer.TRASFORM));
        gl4.glUnmapBuffer(GL_SHADER_STORAGE_BUFFER);

        BufferUtils.destroyDirectBuffer(commandBuffer);
        
        gl4.glDeleteBuffers(Buffer.MAX, bufferName);
        gl4.glDeleteVertexArrays(1, vertexArrayName);
        gl4.glDeleteProgram(programName);

        return true;
    }

    @Override
    public String getName() {
        return "GLBufferStorage" + (useShaderDrawParameters ? "-SDP" : "-NoSDP");
    }
}
