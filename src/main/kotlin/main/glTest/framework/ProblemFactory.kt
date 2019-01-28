/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package main.glTest.framework

import main.glTest.problems.DynamicStreamingProblem
import main.glTest.problems.NullProblem
import main.glTest.problems.Problem
import main.glTest.problems.UntexturedObjectsProblem
import main.glTest.solutions.*
import kotlin.collections.set

/**
 *
 * @author GBarbieri
 */
class ProblemFactory(skipInit: Boolean) {

    val problems = ArrayList<Problem>()
    private val solutions = mutableMapOf<String, ArrayList<Solution>>()

    init {
        // Null
        var newProb: Problem = NullProblem()
        if (skipInit || newProb.init()) {
            if (!skipInit)
                newProb.shutdown()
            problems += newProb
            solutions[problems.last().name]!!.add(NullSolution())
        } else {
            newProb.shutdown()
            throw Error("Unable to create the Null Problem--exiting.")
        }

        // DynamicStreaming
        newProb = DynamicStreamingProblem()
        if (skipInit || newProb.init()) {
            if (!skipInit)
                newProb.shutdown()
            problems += newProb
            solutions[problems.last().name]!!.apply {
                add(DynamicStreamingGLBufferSubData())
                add(DynamicStreamingGLMapUnsynchronized())
                add(DynamicStreamingGLMapPersistent())
            }
        } else
            newProb.shutdown()

        // UntexturedObjects
        newProb = UntexturedObjectsProblem()
        if (skipInit || newProb.init()) {
            if (!skipInit)
                newProb.shutdown()

            problems += newProb
            solutions[problems.last().name]!!.apply {
                add(UntexturedObjectsGLUniform ())
                add(UntexturedObjectsGLDrawLoop())
                add(UntexturedObjectsGLMultiDraw(true))
                add(UntexturedObjectsGLMultiDraw(false))
                add(UntexturedObjectsGLMultiDrawBuffer(true))
                add(UntexturedObjectsGLMultiDrawBuffer(false))
                add(UntexturedObjectsGLBindless())
                add(UntexturedObjectsGLBindlessIndirect())
                add(UntexturedObjectsGLBufferRange())
                add(UntexturedObjectsGLBufferStorage(true))
                add(UntexturedObjectsGLBufferStorage(false))
                add(UntexturedObjectsGLDynamicBuffer())
                add(UntexturedObjectsGLMapUnsynchronized())
                add(UntexturedObjectsGLMapPersistent())
                add(UntexturedObjectsGLTexCoord())
            }
        }  else
            newProb.shutdown()

        // Textured Quads
        newProb = new TexturedQuadsProblem ()
        if (_skipInit || newProb->Init()) {
            if (!_skipInit) { newProb ->
                Shutdown()
            }
            mProblems.push_back(newProb)
            mSolutions[mProblems.back()->GetName()].push_back(new TexturedQuadsGLBindless())
            mSolutions[mProblems.back()->GetName()].push_back(new TexturedQuadsGLBindlessMultiDraw())
            mSolutions[mProblems.back()->GetName()].push_back(new TexturedQuadsGLNaive())
            mSolutions[mProblems.back()->GetName()].push_back(new TexturedQuadsGLNaiveUniform())
            mSolutions[mProblems.back()->GetName()].push_back(new TexturedQuadsGLNoTex())
            mSolutions[mProblems.back()->GetName()].push_back(new TexturedQuadsGLNoTexUniform())
            mSolutions[mProblems.back()->GetName()].push_back(new TexturedQuadsGLSparseBindlessTextureArray())
            mSolutions[mProblems.back()->GetName()].push_back(new TexturedQuadsGLSparseBindlessTextureArrayMultiDraw(true))
            mSolutions[mProblems.back()->GetName()].push_back(new TexturedQuadsGLSparseBindlessTextureArrayMultiDraw(false))
            mSolutions[mProblems.back()->GetName()].push_back(new TexturedQuadsGLTextureArray())
            mSolutions[mProblems.back()->GetName()].push_back(new TexturedQuadsGLTextureArrayUniform())
            mSolutions[mProblems.back()->GetName()].push_back(new TexturedQuadsGLTextureArrayMultiDraw(true))
            mSolutions[mProblems.back()->GetName()].push_back(new TexturedQuadsGLTextureArrayMultiDraw(false))
            mSolutions[mProblems.back()->GetName()].push_back(new TexturedQuadsGLTextureArrayMultiDrawBuffer(true))
            mSolutions[mProblems.back()->GetName()].push_back(new TexturedQuadsGLTextureArrayMultiDrawBuffer(false))
            #if WITH_D3D11
            mSolutions[mProblems.back()->GetName()].push_back(new TexturedQuadsD3D11Naive())
            #endif

        } else { newProb ->
            Shutdown()
            SafeDelete(newProb)
        }

        var newProbl: Problem

        /**
         * Null.
         */
        newProbl = NullProblem()

        problems.add(newProbl)
        solutions[newProbl.getName()] = arrayOf<Solution>(NullSolution())

        /**
         * DynamicStreaming.
         */
        newProbl = DynamicStreamingProblem()

        problems.add(newProbl)
        solutions[newProbl.getName()] = arrayOf<Solution>(DynamicStreamingGLBufferSubData(),
                //                    Not working the wanted way
                //                    new DynamicStreamingGLBufferSubData3Buffers(),
                //                    new DynamicStreamingGLMapUnsynchronized3Buffers(),
                //                    new DynamicStreamingGLMapPersistent3Buffers(),
                DynamicStreamingGLMapUnsynchronized(), DynamicStreamingGLMapPersistent())
        /**
         * UntexturedObjects.
         */
        newProbl = UntexturedObjectsProblem()

        problems.add(newProbl)
        solutions[newProbl.getName()] = arrayOf<Solution>(UntexturedObjectsGLUniform(), UntexturedObjectsGLDrawLoop(),
                //                    Bug
                //                    new UntexturedObjectsGLMultiDraw(true),
                UntexturedObjectsGLMultiDrawBuffer(true), UntexturedObjectsGLMultiDrawBuffer(false), UntexturedObjectsGLBindless(), UntexturedObjectsGLBindlessIndirect(), // warning, buffers seen as non-resident
                UntexturedObjectsGLBufferRange(), UntexturedObjectsGLBufferStorage(true), UntexturedObjectsGLBufferStorage(false), UntexturedObjectsGLDynamicBuffer(), UntexturedObjectsGLMapUnsynchronized(), UntexturedObjectsGLMapPersistent(), UntexturedObjectsGLTexCoord())
    }

    fun getSolutions(problem: Problem?): Array<Solution> {

        assert(problem != null)

        return solutions[problem!!.getName()]
    }
}
