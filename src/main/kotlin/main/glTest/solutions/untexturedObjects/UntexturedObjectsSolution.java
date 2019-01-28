/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package glTest.solutions.untexturedObjects;

import com.jogamp.opengl.GL4;
import com.jogamp.opengl.util.GLBuffers;
import main.glTest.solutions.Solution;
import glm.mat._4.Mat4;
import glm.vec._3.Vec3;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;

/**
 *
 * @author GBarbieri
 */
public abstract class UntexturedObjectsSolution extends Solution {

    protected int objectCount, indexCount, programName;
    protected Vec3 dir = new Vec3(), at = new Vec3(), up = new Vec3(), eye = new Vec3();
    protected Mat4 view = new Mat4(), viewProj = new Mat4();
    protected FloatBuffer matBuffer = GLBuffers.newDirectFloatBuffer(16);

    public boolean init(GL4 gl4, ByteBuffer vertices, ByteBuffer indices, int objectCount) {

        this.objectCount = objectCount;
        this.indexCount = indices.capacity() / Short.BYTES;
        
        return true;
    }

    public abstract void render(GL4 gl4, ByteBuffer transforms);

    @Override
    public boolean shutdown(GL4 gl4) {
        
        return true;
    }

    @Override
    public abstract String getName();

    @Override
    public String getProblemName() {
        return "UntexturedObjects";
    }
}
