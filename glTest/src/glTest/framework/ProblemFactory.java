/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package glTest.framework;

import glTest.problems.NullProblem;
import glTest.problems.Problem;
import glTest.solutions.Solution;
import com.jogamp.opengl.GL4;
import java.util.ArrayList;
import java.util.HashMap;
import glTest.problems.DynamicStreamingProblem;
import glTest.solutions.NullSolution;
import glTest.solutions.dynamicStreaming.DynamicStreamingGLBufferSubData;
import glTest.solutions.dynamicStreaming.DynamicStreamingGLMapPersistent;
import glTest.solutions.dynamicStreaming.DynamicStreamingGLMapUnsynchronized;

/**
 *
 * @author GBarbieri
 */
public class ProblemFactory {

    private ArrayList<Problem> problems = new ArrayList<>();
    private HashMap<String, Solution[]> solutions = new HashMap<>();

    public ProblemFactory(GL4 gl4, boolean skipInit) {

        Problem newProbl;

        /**
         * Null.
         */
        newProbl = new NullProblem();

        if (skipInit || newProbl.init(gl4)) {
            if (!skipInit) {
                newProbl.shutdown(gl4);
            }
            problems.add(newProbl);
            solutions.put(newProbl.getName(), new Solution[]{
                new NullSolution()});
        } else {
            newProbl.shutdown(gl4);
            System.err.println("Unable to create the Null Problem--exiting.");
        }

        /**
         * DynamicStreaming.
         */
        newProbl = new DynamicStreamingProblem();

        if (skipInit || newProbl.init(gl4)) {
            if (!skipInit) {
                newProbl.shutdown(gl4);
            }
            problems.add(newProbl);
            solutions.put(newProbl.getName(), new Solution[]{
                new DynamicStreamingGLBufferSubData(),
                new DynamicStreamingGLMapUnsynchronized(),
                new DynamicStreamingGLMapPersistent()
            });
        } else {
            newProbl.shutdown(gl4);
        }
    }

    public ArrayList<Problem> getProblems() {
        return problems;
    }

    public Solution[] getSolutions(Problem problem) {

        assert (problem != null);
        
        return solutions.get(problem.getName());
    }
}
