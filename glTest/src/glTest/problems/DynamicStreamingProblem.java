/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package glTest.problems;

import static com.jogamp.opengl.GL.GL_COLOR_BUFFER_BIT;
import static com.jogamp.opengl.GL.GL_DEPTH_BUFFER_BIT;
import com.jogamp.opengl.GL4;
import com.jogamp.opengl.util.GLBuffers;
import glm.vec._2.Vec2;
import glm.vec._4.Vec4;
import java.nio.ByteBuffer;

/**
 *
 * @author GBarbieri
 */
public class DynamicStreamingProblem extends Problem {

    private ByteBuffer vertexData;
    private int iteration;
    public static int vertsPerParticle = 6;
    private int particleCountX = 500;
    private int particleCountY = 320;
    private int particleCount = (particleCountX * particleCountY);
    private int vertexCount = particleCount * vertsPerParticle;
    private int particleBufferSize = Vec2.SIZE * vertexCount;

    @Override
    public boolean init(GL4 gl4) {

        vertexData = GLBuffers.newDirectByteBuffer(particleCount * vertsPerParticle * Vec2.SIZE);

        return true;
    }

    @Override
    public void render(GL4 gl4) {

        // TODO: Update should be moved into its own thread, but for now let's just do it here.
        update();

        if (activeSolution != null) {
//            activeSolution.r
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

                vertexData.putFloat(address * 2, x + offsetX + 0).putFloat(y + offsetY + 0);
                vertexData.putFloat(address * 2, x + offsetX + w).putFloat(y + offsetY + 0);
                vertexData.putFloat(address * 2, x + offsetX + 0).putFloat(y + offsetY + h);
                vertexData.putFloat(address * 2, x + offsetX + w).putFloat(y + offsetY + 0);
                vertexData.putFloat(address * 2, x + offsetX + 0).putFloat(y + offsetY + h);
                vertexData.putFloat(address * 2, x + offsetX + w).putFloat(y + offsetY + h);

                address += vertsPerParticle;
            }
        }

        iteration++;
    }

    @Override
    public String getName() {
        return "DynamicStreaming";
    }

    public void clear(GL4 gl4, Vec4 clearColor, float clearDepth) {

        gl4.glClearColor(0.3f, 0.0f, 0.3f, 1.0f);
        gl4.glClearDepth(1.0f);
        gl4.glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
    }
}
