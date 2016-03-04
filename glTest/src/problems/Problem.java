/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package problems;

import com.jogamp.opengl.GL3;
import framework.GLApi;
import solutions.Solution;

/**
 *
 * @author GBarbieri
 */
public abstract class Problem {

    protected Solution activeSolution;

    public boolean init(GL3 gl3) {
        return false;
    }

    public void render(GL3 gl3) {

    }

    public void shutdown(GL3 gl3) {

    }

    public void clear(GL3 gl3) {

    }

    public String getName() {
        return "";
    }

    public boolean setSolution(GL3 gl3, Solution solution) {

        assert (solution == null || solution.getProblemName().equals(getName()));

        if (activeSolution != null) {
            System.out.println("Solution " + activeSolution.getName() + " - shutdown beginning.");
            activeSolution.shutdown(gl3);
            System.out.println("Solution " + activeSolution.getName() + " - shutdown complete.");
        }

        activeSolution = solution;

        // The parameters to be handed off by Init are specific to the problem being solved, so is called after this
        // by the derived class.
        return true;
    }

}
