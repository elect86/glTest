/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package glTest.solutions.untexturedObjects;

import static com.jogamp.opengl.GL.GL_ARRAY_BUFFER;
import static com.jogamp.opengl.GL.GL_BLEND;
import static com.jogamp.opengl.GL.GL_CULL_FACE;
import static com.jogamp.opengl.GL.GL_DEPTH_TEST;
import static com.jogamp.opengl.GL.GL_ELEMENT_ARRAY_BUFFER;
import static com.jogamp.opengl.GL.GL_FLOAT;
import static com.jogamp.opengl.GL.GL_FRONT;
import static com.jogamp.opengl.GL.GL_NO_ERROR;
import static com.jogamp.opengl.GL.GL_SCISSOR_TEST;
import static com.jogamp.opengl.GL.GL_STATIC_DRAW;
import static com.jogamp.opengl.GL.GL_TRIANGLES;
import static com.jogamp.opengl.GL.GL_UNSIGNED_SHORT;
import com.jogamp.opengl.GL4;
import com.jogamp.opengl.util.GLBuffers;
import glTest.framework.ApplicationState;
import glTest.framework.BufferUtils;
import glTest.framework.GLUtilities;
import glf.Vertex_v3fn3f;
import glm.glm;
import glm.mat._4.Mat4;
import glm.vec._3.Vec3;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;

/**
 *
 * @author GBarbieri
 */
public class UntexturedObjectsGLUniform extends UntexturedObjectsSolution {

    private static final String SHADER_SRC = "cubes_gl_uniform";
    protected static final String SHADERS_ROOT = "src/glTest/solutions/untexturedObjects/shaders/";

    private IntBuffer vbo = GLBuffers.newDirectIntBuffer(1), ibo = GLBuffers.newDirectIntBuffer(1),
            vao = GLBuffers.newDirectIntBuffer(1);

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

        gl4.glGenBuffers(1, vbo);
        gl4.glBindBuffer(GL_ARRAY_BUFFER, vbo.get(0));
        gl4.glBufferData(GL_ARRAY_BUFFER, vertices.capacity(), vertices, GL_STATIC_DRAW);

        gl4.glGenBuffers(1, ibo);
        gl4.glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, ibo.get(0));
        gl4.glBufferData(GL_ELEMENT_ARRAY_BUFFER, indices.capacity(), indices, GL_STATIC_DRAW);

        gl4.glGenVertexArrays(1, vao);
        gl4.glBindVertexArray(vao.get(0));

        ApplicationState.animator.setUpdateFPSFrames(15, System.out);
        
        return gl4.glGetError() == GL_NO_ERROR;
    }

    private Vec3 dir = new Vec3(), at = new Vec3(), up = new Vec3(), eye = new Vec3();
    private Mat4 view = new Mat4(), viewProj = new Mat4();
    private FloatBuffer matBuffer = GLBuffers.newDirectFloatBuffer(16);
    private float[] matFa = new float[16];

    @Override
    public void render(GL4 gl4, Mat4[] transforms) {

        // Program
        dir.set(-0.5f, -1, 1);
        at.set(0, 0, 0);
        up.set(0, 0, 1);
        dir.normalize();
        at.sub(dir.mul(250), eye);
        glm.lookAt(eye, at, up, view);

        proj.mul(view, viewProj);

        gl4.glUseProgram(program);
        matBuffer.put(viewProj.toFa(matFa)).rewind();
        gl4.glUniformMatrix4fv(Semantic.Uniform.TRANSFORM0, 1, false, matBuffer);

        // Input Layout
        gl4.glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, ibo.get(0));
        gl4.glBindBuffer(GL_ARRAY_BUFFER, vbo.get(0));
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

        for (Mat4 mat : transforms) {

            matBuffer.put(mat.toFa(matFa)).rewind();

            gl4.glUniformMatrix4fv(Semantic.Uniform.TRANSFORM1, 1, false, matBuffer);
            gl4.glDrawElements(GL_TRIANGLES, indexCount, GL_UNSIGNED_SHORT, 0);
        }
    }
    
    @Override
    public boolean shutdown(GL4 gl4) {

        gl4.glDisableVertexAttribArray(Semantic.Attr.POSITION);
        gl4.glDisableVertexAttribArray(Semantic.Attr.COLOR);
        
        gl4.glDeleteVertexArrays(1, vao);

        gl4.glDeleteBuffers(1, vbo);
        gl4.glDeleteBuffers(1, ibo);        
        gl4.glDeleteProgram(program);

        super.shutdown(gl4);
        
        BufferUtils.destroyDirectBuffer(vao);
        BufferUtils.destroyDirectBuffer(vbo);
        BufferUtils.destroyDirectBuffer(ibo);

        return true;
    }

    @Override
    public String getName() {
        return "GLUniform";
    }
}
