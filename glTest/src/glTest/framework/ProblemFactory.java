/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package glTest.framework;

import glTest.problems.NullProblem;
import glTest.problems.Problem;
import glTest.solutions.Solution;
import com.jogamp.opengl.GL3;
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
        } else {
            newProbl.shutdown(gl3);
            System.err.println("Unable to create the Null Problem--exiting.");
        }

        // DynamicStreaming
        newProbl = new DynamicStreamingProblem();

        if (skipInit || newProbl.init(gl3)) {
            if (!skipInit) {
                newProbl.shutdown(gl3);
            }
            problems.add(newProbl);
            solutions.put(newProbl.getName(), new DynamicStreamingGLBufferSubData());
            solutions.put(newProbl.getName(), new DynamicStreamingGLMapUnsynchronized());
            solutions.put(newProbl.getName(), new DynamicStreamingGLMapPersistent());
        } else {
            newProbl.shutdown(gl3);
        }
    }

    public ArrayList<Problem> getProblems() {
        return problems;
    }

    public HashMap<String, Solution> getSolutions() {
        return solutions;
    }
}
