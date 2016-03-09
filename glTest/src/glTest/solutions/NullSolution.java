/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package glTest.solutions;

import com.jogamp.opengl.GL4;

/**
 *
 * @author GBarbieri
 */
public class NullSolution extends Solution {

    @Override
    public boolean init(GL4 gl4) {
        return true;
    }

    public void render(GL4 gl4) {
    }

    @Override
    public boolean shutdown(GL4 gl4) {
        return true;
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
