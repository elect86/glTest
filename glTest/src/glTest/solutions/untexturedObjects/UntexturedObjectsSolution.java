/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package glTest.solutions.untexturedObjects;

import com.jogamp.opengl.GL4;
import com.jogamp.opengl.util.GLBuffers;
import glTest.framework.BufferUtils;
import glTest.solutions.Solution;
import glm.mat._4.Mat4;
import glm.vec._4.Vec4;
import java.nio.ByteBuffer;

/**
 *
 * @author GBarbieri
 */
public abstract class UntexturedObjectsSolution extends Solution {

    protected int objectCount;
    protected int indexCount;
    protected int program;

    public boolean init(GL4 gl4, ByteBuffer vertices, ByteBuffer indices, int objectCount) {

        this.objectCount = objectCount;
        this.indexCount = indices.capacity() / Short.BYTES;
        
        return true;
    }

    public abstract void render(GL4 gl4, Mat4[] transforms);

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
