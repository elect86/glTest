/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package framework;

import problems.NullProblem;
import problems.Problem;
import solutions.Solution;
import com.jogamp.opengl.GL3;
import java.util.ArrayList;
import java.util.HashMap;
import solutions.NullSolution;

/**
 *
 * @author GBarbieri
 */
public class ProblemFactory {

    private ArrayList<Problem> problems = new ArrayList<>();
    private HashMap<String, Solution> solutions = new HashMap<>();

    public ProblemFactory(GL3 gl3, boolean skipInit) {

        Problem newProbl;

        // Null
        newProbl = new NullProblem();

        if (skipInit || newProbl.init(gl3)) {
            if (!skipInit) {
                newProbl.shutdown(gl3);
            }
            problems.add(newProbl);
            solutions.put(newProbl.getName(), new NullSolution());
        }else{
            newProbl.shutdown(gl3);
            System.err.println("Unable to create the Null Problem--exiting.");
        }

        // DynamicStreaming
        newProbl = new 
    }

    public ArrayList<Problem> getProblems() {
        return problems;
    }
}
