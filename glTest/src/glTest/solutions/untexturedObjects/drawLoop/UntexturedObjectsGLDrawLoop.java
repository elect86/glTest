/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package glTest.solutions.untexturedObjects.drawLoop;

import static com.jogamp.opengl.GL.*;
import static com.jogamp.opengl.GL3ES3.GL_SHADER_STORAGE_BUFFER;
import com.jogamp.opengl.GL4;
import com.jogamp.opengl.util.GLBuffers;
import glTest.framework.ApplicationState;
import glTest.framework.GLUtilities;
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
public class UntexturedObjectsGLDrawLoop extends UntexturedObjectsSolution {

    private static final String SHADER_SRC = "cubes-gl-multi-draw";
    protected static final String SHADERS_ROOT = "src/glTest/solutions/untexturedObjects/drawLoop/shaders/";

    private class Buffer {

        public static final int VERTEX = 0;
        public static final int DRAW_ID = 1;
        public static final int ELEMENT = 2;
        public static final int TRASFORM = 3;
        public static final int MAX = 4;
    }

    private IntBuffer vertexArrayName = GLBuffers.newDirectIntBuffer(1),
            bufferName = GLBuffers.newDirectIntBuffer(Buffer.MAX);

    @Override
    public boolean init(GL4 gl4, ByteBuffer vertices, ByteBuffer indices, int objectCount) {

        if (!super.init(gl4, vertices, indices, objectCount)) {
            return false;
        }

        // Program
        program = GLUtilities.createProgram(gl4, SHADERS_ROOT, SHADER_SRC);

        if (program == 0) {
            System.err.println("Unable to initialize solution " + getName() + ", shader compilation/linking failed.");
            return false;
        }

        gl4.glGenVertexArrays(1, vertexArrayName);
        gl4.glBindVertexArray(vertexArrayName.get(0));

        // Buffers
        gl4.glGenBuffers(Buffer.MAX, bufferName);
        gl4.glBindBuffer(GL_ARRAY_BUFFER, bufferName.get(Buffer.VERTEX));
        gl4.glBufferData(GL_ARRAY_BUFFER, vertices.capacity(), vertices, GL_STATIC_DRAW);

        gl4.glVertexAttribPointer(Semantic.Attr.POSITION, 3, GL_FLOAT, false, vertices.capacity(), 0);
        gl4.glVertexAttribPointer(Semantic.Attr.COLOR, 3, GL_FLOAT, false, vertices.capacity(), Vec3.SIZE);
        gl4.glEnableVertexAttribArray(Semantic.Attr.POSITION);
        gl4.glEnableVertexAttribArray(Semantic.Attr.COLOR);

        IntBuffer drawIds = GLBuffers.newDirectIntBuffer(objectCount);
        for (int i = 0; i < objectCount; i++) {
            drawIds.put(i, i);
        }

        gl4.glBindBuffer(GL_ARRAY_BUFFER, bufferName.get(Buffer.DRAW_ID));
        gl4.glBufferData(GL_ARRAY_BUFFER, drawIds.capacity() * Integer.BYTES, drawIds, GL_STATIC_DRAW);
        
        gl4.glVertexAttribIPointer(Semantic.Attr.DRAW_ID, 1, GL_UNSIGNED_INT, Integer.BYTES, 0);
        gl4.glVertexAttribDivisor(Semantic.Attr.DRAW_ID, 1);
        gl4.glEnableVertexAttribArray(Semantic.Attr.DRAW_ID);

        gl4.glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, bufferName.get(Buffer.ELEMENT));
        gl4.glBufferData(GL_ELEMENT_ARRAY_BUFFER, indices.capacity(), indices, GL_STATIC_DRAW);

        gl4.glBindBufferBase(GL_SHADER_STORAGE_BUFFER, Semantic.Storage.TRANSFORM, bufferName.get(Buffer.TRASFORM));
        
//        ApplicationState.animator.setUpdateFPSFrames(15, System.out);

        return gl4.glGetError() == GL_NO_ERROR;
    }

    @Override
    public void render(GL4 gl4, Mat4[] transforms) {
    
        int count = transforms.length;
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

            gl4.glUseProgram(program);
            gl4.glUniformMatrix4fv(Semantic.Uniform.TRANSFORM0, 1, false, viewProj.toDfb(matBuffer));
        }

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

        gl4.glBindBuffer(GL_SHADER_STORAGE_BUFFER, bufferName.get(Buffer.TRASFORM));
        gl4.glBufferData(GL_SHADER_STORAGE_BUFFER, count * Mat4.SIZE, matBuffer, count);
        
        for (Mat4 mat : transforms) {

            gl4.glUniformMatrix4fv(Semantic.Uniform.TRANSFORM1, 1, false, mat.toDfb(matBuffer));
            gl4.glDrawElements(GL_TRIANGLES, indexCount, GL_UNSIGNED_SHORT, 0);
        }
    }

    @Override
    public String getName() {
        return "GLDrawLoop";
    }

}
