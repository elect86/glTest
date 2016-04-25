/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package glTest.framework;

import glTest.problems.NullProblem;
import glTest.problems.Problem;
import glTest.solutions.Solution;
import java.util.ArrayList;
import java.util.HashMap;
import glTest.problems.DynamicStreamingProblem;
import glTest.problems.UntexturedObjectsProblem;
import glTest.solutions.null_.NullSolution;
import glTest.solutions.dynamicStreaming.DynamicStreamingGLBufferSubData;
import glTest.solutions.dynamicStreaming.DynamicStreamingGLBufferSubData3Buffers;
import glTest.solutions.dynamicStreaming.DynamicStreamingGLMapPersistent;
import glTest.solutions.dynamicStreaming.DynamicStreamingGLMapPersistent3Buffers;
import glTest.solutions.dynamicStreaming.DynamicStreamingGLMapUnsynchronized;
import glTest.solutions.dynamicStreaming.DynamicStreamingGLMapUnsynchronized3Buffers;
import glTest.solutions.untexturedObjects.bindless.UntexturedObjectsGLBindless;
import glTest.solutions.untexturedObjects.bindlessIndirect.UntexturedObjectsGLBindlessIndirect;
import glTest.solutions.untexturedObjects.bufferRange.UntexturedObjectsGLBufferRange;
import glTest.solutions.untexturedObjects.bufferStorage.UntexturedObjectsGLBufferStorage;
import glTest.solutions.untexturedObjects.drawLoop.UntexturedObjectsGLDrawLoop;
import glTest.solutions.untexturedObjects.dynamicBuffer.UntexturedObjectsGLDynamicBuffer;
import glTest.solutions.untexturedObjects.mapPersistent.UntexturedObjectsGLMapPersistent;
import glTest.solutions.untexturedObjects.mapUnsynchronized.UntexturedObjectsGLMapUnsynchronized;
import glTest.solutions.untexturedObjects.multiDrawBuffer.UntexturedObjectsGLMultiDrawBuffer;
import glTest.solutions.untexturedObjects.texCoord.UntexturedObjectsGLTexCoord;
import glTest.solutions.untexturedObjects.uniform.UntexturedObjectsGLUniform;

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
                    //                    Not working the wanted way
                    //                    new DynamicStreamingGLBufferSubData3Buffers(),
                    //                    new DynamicStreamingGLMapUnsynchronized3Buffers(),
                    //                    new DynamicStreamingGLMapPersistent3Buffers(),
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
                    new UntexturedObjectsGLMultiDrawBuffer(true),
                    new UntexturedObjectsGLMultiDrawBuffer(false),
                    new UntexturedObjectsGLBindless(),
                    new UntexturedObjectsGLBindlessIndirect(), // warning, buffers seen as non-resident
                    new UntexturedObjectsGLBufferRange(),
                    new UntexturedObjectsGLBufferStorage(true),
                    new UntexturedObjectsGLBufferStorage(false),
                    new UntexturedObjectsGLDynamicBuffer(),
                    new UntexturedObjectsGLMapUnsynchronized(),
                    new UntexturedObjectsGLMapPersistent(),
                    new UntexturedObjectsGLTexCoord()
                });
    }

    public ArrayList<Problem> getProblems() {
        return problems;
    }

    public Solution[] getSolutions(Problem problem) {

        assert (problem != null);

        return solutions.get(problem.getName());
    }
}
