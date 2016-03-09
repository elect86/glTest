/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package glTest.solutions;

import com.jogamp.opengl.GL4;
import glTest.framework.ApplicationState;
import glm.mat._4.Mat4;

/**
 *
 * @author GBarbieri
 */
public abstract class Solution {

    protected Mat4 proj;
    protected int width = ApplicationState.RESOLUTION.x;
    protected int height = ApplicationState.RESOLUTION.y;
    
    public abstract boolean init(GL4 gl4);
    
    public abstract boolean shutdown(GL4 gl4);

    // The name of this solution.
    public abstract String getName();

    // The name of the problem this solution addresses.
    public abstract String getProblemName();

    // Whether this solution could conceivably run on this Graphics API. 
    // If the support is conditional on an extension, the function should return
    // true and then test for specific support in the Init function (returning false if unsupported).
    public abstract boolean supportsApi(int glApi);
}
