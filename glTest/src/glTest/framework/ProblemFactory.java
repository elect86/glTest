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
import glTest.problems.UntexturedObjectsProblem;
import glTest.solutions.null_.NullSolution;
import glTest.solutions.dynamicStreaming.DynamicStreamingGLBufferSubData;
import glTest.solutions.dynamicStreaming.DynamicStreamingGLMapPersistent;
import glTest.solutions.dynamicStreaming.DynamicStreamingGLMapUnsynchronized;
import glTest.solutions.untexturedObjects.drawLoop.UntexturedObjectsGLDrawLoop;
import glTest.solutions.untexturedObjects.multiDraw.UntexturedObjectsGLMultiDraw;
import glTest.solutions.untexturedObjects.multiDrawBuffer.UntexturedObjectsGLMultiDrawBuffer;
import glTest.solutions.untexturedObjects.uniforms.UntexturedObjectsGLUniform;

/**
 *
 * @author GBarbieri
 */
public class ProblemFactory {

    private ArrayList<Problem> problems = new ArrayList<>();
    private HashMap<String, Solution[]> solutions = new HashMap<>();

    public ProblemFactory() {

        Problem newProbl;

        /**
         * Null.
         */
        newProbl = new NullProblem();

        problems.add(newProbl);
        solutions.put(newProbl.getName(),
                new Solution[]{
                    new NullSolution()});

        /**
         * DynamicStreaming.
         */
        newProbl = new DynamicStreamingProblem();

        problems.add(newProbl);
        solutions.put(newProbl.getName(),
                new Solution[]{
                    new DynamicStreamingGLBufferSubData(),
                    new DynamicStreamingGLMapUnsynchronized(),
                    new DynamicStreamingGLMapPersistent()
                });
        /**
         * UntexturedObjects.
         */
        newProbl = new UntexturedObjectsProblem();

        problems.add(newProbl);
        solutions.put(newProbl.getName(),
                new Solution[]{
                    new UntexturedObjectsGLUniform(),
                    new UntexturedObjectsGLDrawLoop(),
//                    Bug
//                    new UntexturedObjectsGLMultiDraw(true),
                    new UntexturedObjectsGLMultiDrawBuffer(false)});
    }

    public ArrayList<Problem> getProblems() {
        return problems;
    }

    public Solution[] getSolutions(Problem problem) {

        assert (problem != null);

        return solutions.get(problem.getName());
    }
}
