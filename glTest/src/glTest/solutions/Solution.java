/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package glTest.solutions;

import com.jogamp.opengl.GL4;
import glTest.framework.ApplicationState;
import glm.glm;
import glm.mat._4.Mat4;

/**
 *
 * @author GBarbieri
 */
public abstract class Solution {

    protected int width = ApplicationState.RESOLUTION.x;
    protected int height = ApplicationState.RESOLUTION.y;
    protected Mat4 proj = glm.perspective_((float) Math.PI * 0.25f, (float) width / height, 0.1f, 10_000f);

    public boolean init(GL4 gl4) {
        return true;
    }

    public abstract boolean shutdown(GL4 gl4);

    // The name of this solution.
    public abstract String getName();

    // The name of the problem this solution addresses.
    public abstract String getProblemName();
}
