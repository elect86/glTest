/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package solutions;

import com.jogamp.opengl.GL3;

/**
 *
 * @author GBarbieri
 */
public class NullSolution extends Solution {

    public boolean init(GL3 gl3) {
        return true;
    }

    public void render(GL3 gl3) {
    }

    public void shutdown(GL3 gl3) {
    }

    @Override
    public String getName() {
        return "NullSolution";
    }

    @Override
    public String getProblemName() {
        return "NullProblem";
    }

    @Override
    public boolean supportsApi(int glApi) {
        return true;
    }

}
