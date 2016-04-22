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
import glTest.solutions.dynamicStreaming.DynamicStreamingSolution;
import glm.vec._2.Vec2;
import java.nio.ByteBuffer;

/**
 *
 * @author GBarbieri
 */
public class DynamicStreamingProblem extends Problem {

    private ByteBuffer vertexData;
    private float[][] vertexData_;
    private int iteration;
    public static int vertsPerParticle = 6;
    public static int particleCountX = 500;
    public static int particleCountY = 320;
    public static int particleCount = (particleCountX * particleCountY);
    public static int vertexCount = particleCount * vertsPerParticle;
    private int particleBufferSize = Vec2.SIZE * vertexCount;
    public static final boolean useBuffer = false;

    @Override
    public boolean init(GL4 gl4) {

        super.init(gl4);

        vertexData = GLBuffers.newDirectByteBuffer(vertexCount * Vec2.SIZE);
//        vertexData = ByteBuffer.allocate(vertexCount * Vec2.SIZE).order(ByteOrder.nativeOrder());
        vertexData_ = new float[particleCount][vertsPerParticle * 2];

        clearColor.put(new float[]{0.3f, 0.0f, 0.3f, 1.0f}).rewind();
        clearDepth.put(new float[]{1.0f}).rewind();

        return true;
    }

    @Override
    public void render(GL4 gl4) {

        gl4.glClearBufferfv(GL_COLOR, 0, clearColor);
        gl4.glClearBufferfv(GL_DEPTH, 0, clearDepth);

        // TODO: Update should be moved into its own thread, but for now let's just do it here.
        if (useBuffer) {
            update();
        } else {
            update_();
        }

        if (solution != null) {
            if (useBuffer) {
                ((DynamicStreamingSolution) solution).render(gl4, vertexData);
            } else {
                ((DynamicStreamingSolution) solution).render(gl4, vertexData_);
            }
        }
    }

    private void update() {

        float spacing = 1.0f;
        float w = 1.0f;
        float h = 1.0f;

        int marchPixelsX = 24;
        int marchPixelsY = 128;

        float offsetX = (iteration % marchPixelsX) * w;
        float offsetY = ((iteration / marchPixelsX) % marchPixelsY) * h;

        int address = 0;

        for (int yPos = 0; yPos < particleCountY; ++yPos) {

            float y = spacing + yPos * (spacing + h);

            for (int xPos = 0; xPos < particleCountX; ++xPos) {

                float x = spacing + xPos * (spacing + w);

                vertexData
                        .putFloat(address + 0 * Vec2.SIZE + 0 * Float.BYTES, x + offsetX + 0)
                        .putFloat(address + 0 * Vec2.SIZE + 1 * Float.BYTES, y + offsetY + 0)
                        .putFloat(address + 1 * Vec2.SIZE + 0 * Float.BYTES, x + offsetX + w)
                        .putFloat(address + 1 * Vec2.SIZE + 1 * Float.BYTES, y + offsetY + 0)
                        .putFloat(address + 2 * Vec2.SIZE + 0 * Float.BYTES, x + offsetX + 0)
                        .putFloat(address + 2 * Vec2.SIZE + 1 * Float.BYTES, y + offsetY + h)
                        .putFloat(address + 3 * Vec2.SIZE + 0 * Float.BYTES, x + offsetX + w)
                        .putFloat(address + 3 * Vec2.SIZE + 1 * Float.BYTES, y + offsetY + 0)
                        .putFloat(address + 4 * Vec2.SIZE + 0 * Float.BYTES, x + offsetX + 0)
                        .putFloat(address + 4 * Vec2.SIZE + 1 * Float.BYTES, y + offsetY + h)
                        .putFloat(address + 5 * Vec2.SIZE + 0 * Float.BYTES, x + offsetX + w)
                        .putFloat(address + 5 * Vec2.SIZE + 1 * Float.BYTES, y + offsetY + h);

                address += vertsPerParticle * Vec2.SIZE;
            }
        }

        iteration++;
    }

    private void update_() {

        float spacing = 1.0f;
        float w = 1.0f;
        float h = 1.0f;

        int marchPixelsX = 24;
        int marchPixelsY = 128;

        float offsetX = (iteration % marchPixelsX) * w;
        float offsetY = ((iteration / marchPixelsX) % marchPixelsY) * h;

        int particle = 0;

        for (int yPos = 0; yPos < particleCountY; ++yPos) {

            float y = spacing + yPos * (spacing + h);

            for (int xPos = 0; xPos < particleCountX; ++xPos) {

                float x = spacing + xPos * (spacing + w);

                vertexData_[particle][0] = x + offsetX + 0;
                vertexData_[particle][1] = y + offsetY + 0;
                vertexData_[particle][2] = x + offsetX + w;
                vertexData_[particle][3] = y + offsetY + 0;
                vertexData_[particle][4] = x + offsetX + 0;
                vertexData_[particle][5] = y + offsetY + h;
                vertexData_[particle][6] = x + offsetX + w;
                vertexData_[particle][7] = y + offsetY + 0;
                vertexData_[particle][8] = x + offsetX + 0;
                vertexData_[particle][9] = y + offsetY + h;
                vertexData_[particle][10] = x + offsetX + w;
                vertexData_[particle][11] = y + offsetY + h;

                particle++;
            }
        }

        iteration++;
    }

    @Override
    public String getName() {
        return "DynamicStreaming";
    }
}
