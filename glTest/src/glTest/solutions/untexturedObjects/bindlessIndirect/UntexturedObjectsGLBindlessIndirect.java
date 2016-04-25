/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package glTest.solutions.untexturedObjects.bindlessIndirect;

import com.jogamp.opengl.GL2;
import com.jogamp.opengl.GL4;
import static com.jogamp.opengl.GL4.*;
import com.jogamp.opengl.GLContext;
import com.jogamp.opengl.util.GLBuffers;
import glTest.framework.ApplicationState;
import glTest.framework.BufferUtils;
import glTest.framework.DrawElementsIndirectCommand;
import glTest.framework.GLApi;
import glTest.framework.GLUtilities;
import glTest.solutions.untexturedObjects.UntexturedObjectsSolution;
import glm.glm;
import glm.mat._4.Mat4;
import glm.vec._3.Vec3;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.nio.LongBuffer;

/**
 *
 * @author GBarbieri
 */
public class UntexturedObjectsGLBindlessIndirect extends UntexturedObjectsSolution {

    private static final String SHADER_SRC = "bindless-indirect";
    protected static final String SHADERS_ROOT = "glTest/solutions/untexturedObjects/bindlessIndirect/shaders/";

    private class Buffer {

        public static final int TRANSFORM0_ = 0;
        public static final int INDIRECT = 1;
        public static final int MAX = 2;
    }

    private class Pointer {

        public static final int TRANSFORM0_ = 0;
        public static final int INDIRECT = 1;
        public static final int MAX = 2;
    }

    private IntBuffer vertexArrayName = GLBuffers.newDirectIntBuffer(1), ibName, vbName,
            bufferName = GLBuffers.newDirectIntBuffer(Buffer.MAX);
    private ByteBuffer[] pointer = new ByteBuffer[Pointer.MAX];
    private LongBuffer ibAddresses, vbAddresses;
    private long[] ibSizes, vbSizes;
    private ByteBuffer commands;

    @Override
    public boolean init(GL4 gl4, ByteBuffer vertices, ByteBuffer indices, int objectCount) {

        if (!gl4.isExtensionAvailable("GL_ARB_buffer_storage")) {
            System.err.println("Unable to initialize solution " + getName() + ", ARB_buffer_storage unavailable.");
            return false;
        }

        if (!gl4.isExtensionAvailable("GL_NV_shader_buffer_load")) {
            System.err.println("Unable to initialize solution " + getName() + ", NV_shader_buffer_load unavailable.");
            return false;
        }

        if (!super.init(gl4, vertices, indices, objectCount)) {
            return false;
        }

        // Program
        programName = GLUtilities.createProgram(gl4, SHADERS_ROOT, SHADER_SRC);

        if (programName == 0) {
            System.err.println("Unable to initialize solution " + getName() + ", shader compilation/linking failed.");
            return false;
        }

        ibName = GLBuffers.newDirectIntBuffer(objectCount);
        ibAddresses = GLBuffers.newDirectLongBuffer(objectCount);
        ibSizes = new long[objectCount];

        vbName = GLBuffers.newDirectIntBuffer(objectCount);
        vbAddresses = GLBuffers.newDirectLongBuffer(objectCount);
        vbSizes = new long[objectCount];

        commands = GLBuffers.newDirectByteBuffer(objectCount * Command.SIZE);

        gl4.glGenBuffers(objectCount, ibName);
        gl4.glGenBuffers(objectCount, vbName);

        for (int u = 0; u < objectCount; u++) {

            gl4.glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, ibName.get(u));
            gl4.glBufferStorage(GL_ELEMENT_ARRAY_BUFFER, indices.capacity(), indices, 0);
            ibAddresses.position(u);
            gl4.glGetBufferParameterui64vNV(GL_ELEMENT_ARRAY_BUFFER, GL_BUFFER_GPU_ADDRESS_NV, ibAddresses);
            gl4.glMakeBufferResidentNV(GL_ELEMENT_ARRAY_BUFFER, GL_READ_ONLY);
            ibSizes[u] = indices.capacity();

            gl4.glBindBuffer(GL_ARRAY_BUFFER, vbName.get(u));
            gl4.glBufferStorage(GL_ARRAY_BUFFER, vertices.capacity(), vertices, 0);
            vbAddresses.position(u);
            gl4.glGetBufferParameterui64vNV(GL_ARRAY_BUFFER, GL_BUFFER_GPU_ADDRESS_NV, vbAddresses);
            gl4.glMakeBufferResidentNV(GL_ARRAY_BUFFER, GL_READ_ONLY);
            vbSizes[u] = vertices.capacity();
        }
        ibAddresses.position(0);
        vbAddresses.position(0);

        gl4.glGenBuffers(Buffer.MAX, bufferName);

        gl4.glBindBufferBase(GL_SHADER_STORAGE_BUFFER, Semantic.Storage.TRANSFORM0_, bufferName.get(Buffer.TRANSFORM0_));
        gl4.glBufferStorage(GL_SHADER_STORAGE_BUFFER, objectCount * Mat4.SIZE, null, GL_MAP_WRITE_BIT
                | GL_MAP_PERSISTENT_BIT | GL_DYNAMIC_STORAGE_BIT);
        pointer[Pointer.TRANSFORM0_] = gl4.glMapBufferRange(GL_SHADER_STORAGE_BUFFER, 0, objectCount * Mat4.SIZE,
                GL_MAP_WRITE_BIT | GL_MAP_PERSISTENT_BIT);

        gl4.glBindBuffer(GL_DRAW_INDIRECT_BUFFER, bufferName.get(Buffer.INDIRECT));
        gl4.glBufferStorage(GL_DRAW_INDIRECT_BUFFER, objectCount * Command.SIZE, null, GL_MAP_WRITE_BIT
                | GL_MAP_PERSISTENT_BIT | GL_DYNAMIC_STORAGE_BIT);
        pointer[Pointer.INDIRECT] = gl4.glMapBufferRange(GL_DRAW_INDIRECT_BUFFER, 0, objectCount * Command.SIZE,
                GL_MAP_WRITE_BIT | GL_MAP_PERSISTENT_BIT);

        gl4.glGenVertexArrays(1, vertexArrayName);
        gl4.glBindVertexArray(vertexArrayName.get(0));

        // turn off because of the warnings
        GLContext.getCurrent().enableGLDebugMessage(false);
        
        ApplicationState.animator.setUpdateFPSFrames(25, System.out);

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

        // Input Layout
        gl4.glEnableClientState(GL_ELEMENT_ARRAY_UNIFIED_NV);
        gl4.glEnableClientState(GL_VERTEX_ATTRIB_ARRAY_UNIFIED_NV);

        gl4.glVertexAttribFormatNV(Semantic.Attr.POSITION, 3, GL_FLOAT, false, Vec3.SIZE * 2);
        gl4.glVertexAttribFormatNV(Semantic.Attr.COLOR, 3, GL_FLOAT, false, Vec3.SIZE * 2);
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

        for (int i = 0; i < objectCount; i++) {

            commands.putInt(Command.SIZE * i + Command.OFFSET_DRAW + DrawElementsIndirectCommand.OFFSET_COUNT, indexCount);
            commands.putInt(Command.SIZE * i + Command.OFFSET_DRAW + DrawElementsIndirectCommand.OFFSET_INSTANCE_COUNT, 1);
            commands.putInt(Command.SIZE * i + Command.OFFSET_DRAW + DrawElementsIndirectCommand.OFFSET_FIRST_INDEX, 0);
            commands.putInt(Command.SIZE * i + Command.OFFSET_DRAW + DrawElementsIndirectCommand.OFFSET_BASE_VERTEX, 0);
            commands.putInt(Command.SIZE * i + Command.OFFSET_DRAW + DrawElementsIndirectCommand.OFFSET_BASE_INSTANCE, 0);

            commands.putInt(Command.SIZE * i + Command.OFFSET_RESERVED, 0);

            commands.putInt(Command.SIZE * i + Command.OFFSET_INDEX_BUFFER + BindlessPtrNV.OFFSET_INDEX, 0);
            commands.putInt(Command.SIZE * i + Command.OFFSET_INDEX_BUFFER + BindlessPtrNV.OFFSET_RESERVED, 0);
            commands.putLong(Command.SIZE * i + Command.OFFSET_INDEX_BUFFER + BindlessPtrNV.OFFSET_ADDRESS,
                    ibAddresses.get(i));
            commands.putLong(Command.SIZE * i + Command.OFFSET_INDEX_BUFFER + BindlessPtrNV.OFFSET_LENGTH, ibSizes[i]);

            commands.putInt(Command.SIZE * i + Command.OFFSET_VERTEX_BUFFER0 + BindlessPtrNV.OFFSET_INDEX,
                    Semantic.Attr.POSITION);
            commands.putInt(Command.SIZE * i + Command.OFFSET_VERTEX_BUFFER0 + BindlessPtrNV.OFFSET_RESERVED, 0);
            commands.putLong(Command.SIZE * i + Command.OFFSET_VERTEX_BUFFER0 + BindlessPtrNV.OFFSET_ADDRESS,
                    vbAddresses.get(i) + 0);
            commands.putLong(Command.SIZE * i + Command.OFFSET_VERTEX_BUFFER0 + BindlessPtrNV.OFFSET_LENGTH,
                    vbSizes[i] - 0);

            commands.putInt(Command.SIZE * i + Command.OFFSET_VERTEX_BUFFER1 + BindlessPtrNV.OFFSET_INDEX,
                    Semantic.Attr.COLOR);
            commands.putInt(Command.SIZE * i + Command.OFFSET_VERTEX_BUFFER1 + BindlessPtrNV.OFFSET_RESERVED, 0);
            commands.putLong(Command.SIZE * i + Command.OFFSET_VERTEX_BUFFER1 + BindlessPtrNV.OFFSET_ADDRESS,
                    vbAddresses.get(i) + Vec3.SIZE);
            commands.putLong(Command.SIZE * i + Command.OFFSET_VERTEX_BUFFER1 + BindlessPtrNV.OFFSET_LENGTH,
                    vbSizes[i] - Vec3.SIZE);
        }

        for (int i = 0; i < transforms.capacity(); i++) {
            pointer[Pointer.TRANSFORM0_].put(i, transforms.get(i));
        }

        for (int i = 0; i < commands.capacity(); i++) {
            pointer[Pointer.INDIRECT].put(i, commands.get(i));
        }

        gl4.glMemoryBarrier(GL_CLIENT_MAPPED_BUFFER_BARRIER_BIT);

        ((GL2) gl4).glMultiDrawElementsIndirectBindlessNV(GL_TRIANGLES, GL_UNSIGNED_SHORT, null, objectCount, 0, 2);
    }

    @Override
    public boolean shutdown(GL4 gl4) {

        gl4.glDisableVertexAttribArray(Semantic.Attr.POSITION);
        gl4.glDisableVertexAttribArray(Semantic.Attr.COLOR);

        if (pointer[Pointer.TRANSFORM0_] != null) {

            gl4.glBindBuffer(GL_SHADER_STORAGE_BUFFER, bufferName.get(Buffer.TRANSFORM0_));
            gl4.glUnmapBuffer(GL_SHADER_STORAGE_BUFFER);
        }

        if (pointer[Pointer.INDIRECT] != null) {

            gl4.glBindBuffer(GL_SHADER_STORAGE_BUFFER, bufferName.get(Buffer.INDIRECT));
            gl4.glUnmapBuffer(GL_SHADER_STORAGE_BUFFER);
        }

        gl4.glDeleteBuffers(Buffer.MAX, bufferName);
        
        gl4.glDeleteVertexArrays(1, vertexArrayName);

        gl4.glDeleteProgram(programName);

        gl4.glDisableClientState(GL_ELEMENT_ARRAY_UNIFIED_NV);
        gl4.glDisableClientState(GL_VERTEX_ATTRIB_ARRAY_UNIFIED_NV);

        BufferUtils.destroyDirectBuffer(ibName);
        BufferUtils.destroyDirectBuffer(ibAddresses);
        BufferUtils.destroyDirectBuffer(vbName);
        BufferUtils.destroyDirectBuffer(vbAddresses);
        BufferUtils.destroyDirectBuffer(commands);
        
        GLContext.getCurrent().enableGLDebugMessage(true);

        return true;
    }

    @Override
    public String getName() {
        return "GLBindlessIndirect";
    }
}
