/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package glTest.problems;

import com.jogamp.opengl.GL4;
import glTest.solutions.Solution;

/**
 *
 * @author GBarbieri
 */
public abstract class Problem {

    protected Solution activeSolution;

    public boolean init(GL4 gl4) {
        return false;
    }

    public void render(GL4 gl4) {

    }

    public void shutdown(GL4 gl4) {

    }

    public void clear(GL4 gl4) {

    }

    public String getName() {
        return "";
    }

    public boolean setSolution(GL4 gl4, Solution solution) {

        assert (solution == null || solution.getProblemName().equals(getName()));

        if (activeSolution != null) {
            System.out.println("Solution " + activeSolution.getName() + " - shutdown beginning.");
            activeSolution.shutdown(gl4);
            System.out.println("Solution " + activeSolution.getName() + " - shutdown complete.");
        }

        activeSolution = solution;

        // The parameters to be handed off by Init are specific to the problem being solved, so is called after this
        // by the derived class.
        return true;
    }

}
