/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package glTest.solutions.untexturedObjects.multiDrawBuffer;

import static com.jogamp.opengl.GL3ES3.*;
import com.jogamp.opengl.GL4;
import com.jogamp.opengl.util.GLBuffers;
import glTest.framework.ApplicationState;
import common.BufferUtils;
import glTest.framework.DrawElementsIndirectCommand;
import glTest.framework.GLApi;
import glTest.framework.GLUtilities;
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
public class UntexturedObjectsGLMultiDrawBuffer extends UntexturedObjectsSolution {

    private static final String SHADER_SRC = "multi-draw-buffer";
    protected static final String SHADERS_ROOT = "glTest/solutions/untexturedObjects/multiDrawBuffer/shaders/";

    private class Buffer {

        public static final int VERTEX = 0;
        public static final int ELEMENT = 1;
        public static final int DRAW_ID = 2;
        public static final int TRASFORM = 3;
        public static final int COMMAND = 4;
        public static final int MAX = 5;
    }

    private IntBuffer vertexArrayName = GLBuffers.newDirectIntBuffer(1),
            bufferName = GLBuffers.newDirectIntBuffer(Buffer.MAX);
    private boolean useShaderDrawParameters;
    private ByteBuffer commands;

    public UntexturedObjectsGLMultiDrawBuffer(boolean useShaderDrawParameters) {
        this.useShaderDrawParameters = useShaderDrawParameters;
        updateFps = useShaderDrawParameters? 60 : 130;
    }

    @Override
    public boolean init(GL4 gl4, ByteBuffer vertices, ByteBuffer indices, int objectCount) {

        if (useShaderDrawParameters && !gl4.isExtensionAvailable("GL_ARB_shader_draw_parameters")) {
            System.err.println("Unable to initialize solution, ARB_shader_draw_parameters is required but not available.");
            return false;
        }

        if (!super.init(gl4, vertices, indices, objectCount)) {
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

        gl4.glBindBufferBase(GL_SHADER_STORAGE_BUFFER, Semantic.Storage.TRANSFORM, bufferName.get(Buffer.TRASFORM));

        gl4.glBindBuffer(GL_DRAW_INDIRECT_BUFFER, bufferName.get(Buffer.COMMAND));

        // Set the command buffer size.
        commands = GLBuffers.newDirectByteBuffer(DrawElementsIndirectCommand.SIZE * objectCount);

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

        for (int u = 0; u < count; u++) {

            int offset = u * DrawElementsIndirectCommand.SIZE;

            commands.putInt(offset + DrawElementsIndirectCommand.OFFSET_COUNT, indexCount);
            commands.putInt(offset + DrawElementsIndirectCommand.OFFSET_INSTANCE_COUNT, 1);
            commands.putInt(offset + DrawElementsIndirectCommand.OFFSET_FIRST_INDEX, 0);
            commands.putInt(offset + DrawElementsIndirectCommand.OFFSET_BASE_VERTEX, 0);
            commands.putInt(offset + DrawElementsIndirectCommand.OFFSET_BASE_INSTANCE, useShaderDrawParameters ? 0 : u);
        }

        gl4.glBindBuffer(GL_SHADER_STORAGE_BUFFER, bufferName.get(Buffer.TRASFORM));
        gl4.glBufferData(GL_SHADER_STORAGE_BUFFER, count * Mat4.SIZE, transforms, GL_DYNAMIC_DRAW);

        gl4.glBindBuffer(GL_DRAW_INDIRECT_BUFFER, bufferName.get(Buffer.COMMAND));
        gl4.glBufferData(GL_DRAW_INDIRECT_BUFFER, commands.capacity(), commands, GL_DYNAMIC_DRAW);

        gl4.glMultiDrawElementsIndirect(GL_TRIANGLES, GL_UNSIGNED_SHORT, null, count, 0);
    }

    @Override
    public boolean shutdown(GL4 gl4) {

        gl4.glDisableVertexAttribArray(Semantic.Attr.POSITION);
        gl4.glDisableVertexAttribArray(Semantic.Attr.COLOR);
        if (!useShaderDrawParameters) {
            gl4.glDisableVertexAttribArray(Semantic.Attr.DRAW_ID);
        }

        gl4.glDeleteBuffers(Buffer.MAX, bufferName);
        gl4.glDeleteVertexArrays(1, vertexArrayName);
        gl4.glDeleteProgram(programName);

        return true;
    }

    @Override
    public String getName() {
        return "GLMultiDrawBuffer" + (useShaderDrawParameters ? "-SDP" : "-NoSDP");
    }
}
