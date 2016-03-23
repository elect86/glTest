/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package glTest.problems;

import static com.jogamp.opengl.GL2ES3.GL_COLOR;
import static com.jogamp.opengl.GL2ES3.GL_DEPTH;
import com.jogamp.opengl.GL4;
import com.jogamp.opengl.util.GLBuffers;
import glTest.solutions.Solution;
import glTest.solutions.untexturedObjects.UntexturedObjectsSolution;
import glf.Vertex_v3fn3f;
import glm.glm;
import glm.mat._4.Mat4;
import java.nio.ByteBuffer;

/**
 *
 * @author GBarbieri
 */
public class UntexturedObjectsProblem extends Problem {

    private static final boolean DRAW_SINGLE_TRIANGLE = false;

    private int objectsX = 64;
    private int objectsY = 64;
    private int objectsZ = 64;
    private int objectCount = objectsX * objectsY * objectsZ;

    private int transformCount = objectCount;
    private int vertexCount = DRAW_SINGLE_TRIANGLE ? 3 : 8;
    private int indexCount = DRAW_SINGLE_TRIANGLE ? 3 : 36;

    private ByteBuffer vertices;
    private ByteBuffer indices;
    private Mat4[] transforms;

    private int iteration = 0;

    @Override
    public boolean init(GL4 gl4) {

        super.init(gl4);

        genUnitCube();

        transforms = new Mat4[transformCount];
        
        for (int i = 0; i < transforms.length; i++) {
            transforms[i] = new Mat4();
        }

//        vertexData = GLBuffers.newDirectByteBuffer(vertexCount * Vec2.SIZE);
//        vertexData = ByteBuffer.allocate(vertexCount * Vec2.SIZE).order(ByteOrder.nativeOrder());
        clearColor.put(new float[]{0.0f, 0.1f, 0.0f, 1.0f}).rewind();
        clearDepth.put(new float[]{1.0f}).rewind();

        return true;
    }

    private void genUnitCube() {

        vertices = GLBuffers.newDirectByteBuffer(Vertex_v3fn3f.SIZE * vertexCount);

        vertices.asFloatBuffer().put(DRAW_SINGLE_TRIANGLE
                ? new float[]{
                    -0.5f, +0.5f, -0.5f, 0.0f, 1.0f, 0.0f,
                    +0.5f, +0.5f, -0.5f, 1.0f, 1.0f, 0.0f,
                    +0.5f, +0.5f, +0.5f, 1.0f, 1.0f, 1.0f}
                : new float[]{
                    -0.5f, +0.5f, -0.5f, 0.0f, 1.0f, 0.0f,
                    +0.5f, +0.5f, -0.5f, 1.0f, 1.0f, 0.0f,
                    +0.5f, +0.5f, +0.5f, 1.0f, 1.0f, 1.0f,
                    -0.5f, +0.5f, +0.5f, 0.0f, 1.0f, 1.0f,
                    -0.5f, -0.5f, +0.5f, 0.0f, 0.0f, 1.0f,
                    +0.5f, -0.5f, +0.5f, 1.0f, 0.0f, 1.0f,
                    +0.5f, -0.5f, -0.5f, 1.0f, 0.0f, 0.0f,
                    -0.5f, -0.5f, -0.5f, 0.0f, 0.0f, 0.0f});

        assert (vertexCount == (vertices.capacity() / Vertex_v3fn3f.SIZE));

        indices = GLBuffers.newDirectByteBuffer(indexCount * Short.BYTES);

        indices.asShortBuffer().put(DRAW_SINGLE_TRIANGLE
                ? new short[]{
                    0, 1, 2
                }
                : new short[]{
                    0, 1, 2, 0, 2, 3,
                    4, 5, 6, 4, 6, 7,
                    3, 2, 5, 3, 5, 4,
                    2, 1, 6, 2, 6, 5,
                    1, 7, 6, 1, 0, 7,
                    0, 3, 4, 0, 4, 7});

        assert (indexCount == (indices.capacity() / Short.BYTES));
    }

    @Override
    public void render(GL4 gl4) {

        gl4.glClearBufferfv(GL_COLOR, 0, clearColor);
        gl4.glClearBufferfv(GL_DEPTH, 0, clearDepth);
        
        update();
        
        ((UntexturedObjectsSolution)solution).render(gl4, transforms);
    }

    private void update() {

        float angle = iteration * 0.01f;

        int m = 0;

        for (int x = 0; x < objectsX; x++) {

            for (int y = 0; y < objectsY; y++) {

                for (int z = 0; z < objectsZ; z++) {

                    transforms[m].rotationZ(angle);
                    transforms[m].m30 = 2.0f * x - objectsX;
                    transforms[m].m31 = 2.0f * y - objectsY;
                    transforms[m].m32 = 2.0f * z - objectsZ;

                    m++;
                }
            }
        }

        iteration++;

        if (angle > 2 * Math.PI) {

            iteration = 0;
        }
    }

    @Override
    public void setSolution(GL4 gl4, Solution solution) {
        this.solution = solution;
        ((UntexturedObjectsSolution) solution).init(gl4, vertices, indices, objectCount);
    }

    @Override
    public String getName() {
        return "UntexturedObjects";
    }
}
