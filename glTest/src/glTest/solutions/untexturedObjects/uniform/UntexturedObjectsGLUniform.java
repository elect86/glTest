/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package glTest.solutions.untexturedObjects.uniform;

import static com.jogamp.opengl.GL.*;
import com.jogamp.opengl.GL4;
import com.jogamp.opengl.util.GLBuffers;
import glTest.framework.ApplicationState;
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
public class UntexturedObjectsGLUniform extends UntexturedObjectsSolution {

    private static final String SHADER_SRC = "uniform";
    protected static final String SHADERS_ROOT = "glTest/solutions/untexturedObjects/uniform/shaders/";

    private class Buffer {

        public static final int VERTEX = 0;
        public static final int ELEMENT = 1;
        public static final int MAX = 2;
    }

    private IntBuffer bufferName = GLBuffers.newDirectIntBuffer(Buffer.MAX),
            vertexArrayName = GLBuffers.newDirectIntBuffer(1);

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

        gl4.glGenBuffers(Buffer.MAX, bufferName);

        gl4.glBindBuffer(GL_ARRAY_BUFFER, bufferName.get(Buffer.VERTEX));
        gl4.glBufferData(GL_ARRAY_BUFFER, vertices.capacity(), vertices, GL_STATIC_DRAW);

        gl4.glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, bufferName.get(Buffer.ELEMENT));
        gl4.glBufferData(GL_ELEMENT_ARRAY_BUFFER, indices.capacity(), indices, GL_STATIC_DRAW);

        gl4.glGenVertexArrays(1, vertexArrayName);
        gl4.glBindVertexArray(vertexArrayName.get(0));

        ApplicationState.animator.setUpdateFPSFrames(20, System.out);

        return GLApi.getError(gl4) == GL_NO_ERROR;
    }

    @Override
    public void render(GL4 gl4, ByteBuffer transforms) {

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
        gl4.glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, bufferName.get(Buffer.ELEMENT));
        gl4.glBindBuffer(GL_ARRAY_BUFFER, bufferName.get(Buffer.VERTEX));
        gl4.glVertexAttribPointer(Semantic.Attr.POSITION, 3, GL_FLOAT, false, Vertex_v3fn3f.SIZE, 0);
        gl4.glVertexAttribPointer(Semantic.Attr.COLOR, 3, GL_FLOAT, false, Vertex_v3fn3f.SIZE, Vec3.SIZE);

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

            for (int j = 0; j < Mat4.SIZE; j++) {
                
            }
            transforms.position(i * Mat4.SIZE);
            gl4.glUniformMatrix4fv(Semantic.Uniform.TRANSFORM1, 1, false, transforms.asFloatBuffer());
            gl4.glDrawElements(GL_TRIANGLES, indexCount, GL_UNSIGNED_SHORT, 0);
        }
    }

    @Override
    public boolean shutdown(GL4 gl4) {

        gl4.glDisableVertexAttribArray(Semantic.Attr.POSITION);
        gl4.glDisableVertexAttribArray(Semantic.Attr.COLOR);

        gl4.glDeleteVertexArrays(1, vertexArrayName);

        gl4.glDeleteBuffers(Buffer.MAX, bufferName);
        gl4.glDeleteProgram(programName);

        super.shutdown(gl4);

        return true;
    }

    @Override
    public String getName() {
        return "GLUniform";
    }
}
