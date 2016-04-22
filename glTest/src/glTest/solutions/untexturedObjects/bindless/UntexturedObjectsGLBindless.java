/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package glTest.solutions.untexturedObjects.bindless;

import static com.jogamp.opengl.GL2GL3.*;
import com.jogamp.opengl.GL4;
import com.jogamp.opengl.util.GLBuffers;
import glTest.framework.ApplicationState;
import glTest.framework.BufferUtils;
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
public class UntexturedObjectsGLBindless extends UntexturedObjectsSolution {

    private static final String SHADER_SRC = "bindless";
    protected static final String SHADERS_ROOT = "glTest/solutions/untexturedObjects/bindless/shaders/";

    private IntBuffer vertexArrayName = GLBuffers.newDirectIntBuffer(1), ibName, vbName;
    private LongBuffer ibAddresses, vbAddresses;
    private long[] ibSizes, vbSizes;

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

        gl4.glGenVertexArrays(1, vertexArrayName);
        gl4.glBindVertexArray(vertexArrayName.get(0));

        ApplicationState.animator.setUpdateFPSFrames(5, System.out);

        return gl4.glGetError() == GL_NO_ERROR;
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

        for (int u = 0; u < count; u++) {

            gl4.glBufferAddressRangeNV(GL_ELEMENT_ARRAY_ADDRESS_NV, 0, ibAddresses.get(u), ibSizes[u]);
            gl4.glBufferAddressRangeNV(GL_VERTEX_ATTRIB_ARRAY_ADDRESS_NV, Semantic.Attr.POSITION, vbAddresses.get(u) + 0,
                    vbSizes[u] - 0);
            gl4.glBufferAddressRangeNV(GL_VERTEX_ATTRIB_ARRAY_ADDRESS_NV, Semantic.Attr.COLOR,
                    vbAddresses.get(u) + Vec3.SIZE, vbSizes[u] - Vec3.SIZE);

            transforms.position(u * Mat4.SIZE);
            gl4.glUniformMatrix4fv(Semantic.Uniform.TRANSFORM1, 1, false, transforms.asFloatBuffer());
            gl4.glDrawElements(GL_TRIANGLES, indexCount, GL_UNSIGNED_SHORT, 0);
        }
        transforms.position(0);
    }

    @Override
    public boolean shutdown(GL4 gl4) {

        gl4.glDisableVertexAttribArray(Semantic.Attr.POSITION);
        gl4.glDisableVertexAttribArray(Semantic.Attr.COLOR);

        if (ibName.capacity() > 0) {
            gl4.glDeleteBuffers(objectCount, ibName);
            BufferUtils.destroyDirectBuffer(ibName);
            BufferUtils.destroyDirectBuffer(ibAddresses);
        }

        if (vbName.capacity() > 0) {
            gl4.glDeleteBuffers(objectCount, vbName);
            BufferUtils.destroyDirectBuffer(vbName);
            BufferUtils.destroyDirectBuffer(vbAddresses);
        }

        gl4.glDeleteVertexArrays(1, vertexArrayName);

        gl4.glDeleteProgram(programName);

        gl4.glDisableClientState(GL_ELEMENT_ARRAY_UNIFIED_NV);
        gl4.glDisableClientState(GL_VERTEX_ATTRIB_ARRAY_UNIFIED_NV);
        
        BufferUtils.destroyDirectBuffer(ibName);
        BufferUtils.destroyDirectBuffer(ibAddresses);
        BufferUtils.destroyDirectBuffer(vbName);
        BufferUtils.destroyDirectBuffer(vbAddresses);
        
        return true;
    }

    @Override
    public String getName() {
        return "GLBindless";
    }
}
