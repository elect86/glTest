/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package main.glTest.framework

import main.glTest.problems.*
import main.glTest.solutions.*

/**
 *
 * @author GBarbieri
 */
class ProblemFactory(skipInit: Boolean) {

    val problems = ArrayList<Problem>()
    private val solutions = mutableMapOf<String, Array<Solution>>()

    init {
        // Null
        var newProb: Problem = NullProblem()
        if (skipInit || newProb.init()) {
            if (!skipInit)
                newProb.shutdown()
            problems += newProb
            solutions[newProb.name] = arrayOf<Solution>(NullSolution())
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
            solutions[newProb.name] = arrayOf<Solution>(
                    DynamicStreamingGLBufferSubData(),
                    DynamicStreamingGLMapUnsynchronized(),
                    DynamicStreamingGLMapPersistent())
        } else
            newProb.shutdown()

        // Objects
        newProb = ObjectsProblem()
        if (skipInit || newProb.init()) {
            if (!skipInit)
                newProb.shutdown()

            problems += newProb
            solutions[newProb.name] = arrayOf<Solution>(
                    ObjectsGLUniform(),
                    ObjectsGLDrawLoop(),
                    ObjectsGLMultiDraw(true),
                    ObjectsGLMultiDraw(false),
                    ObjectsGLMultiDrawBuffer(true),
                    ObjectsGLMultiDrawBuffer(false),
                    ObjectsGLBindless(),
//                    ObjectsGLBindlessIndirect(),
                    ObjectsGLBufferRange(),
                    ObjectsGLBufferStorage(true),
                    ObjectsGLBufferStorage(false),
                    ObjectsGLDynamicBuffer(),
                    ObjectsGLMapUnsynchronized(),
                    ObjectsGLMapPersistent(),
                    ObjectsGLTexCoord())
        } else
            newProb.shutdown()

        // Textured Quads
        newProb = TexturedQuadsProblem ()
        if (skipInit || newProb.init()) {
            if (!skipInit)
                newProb.shutdown()

            problems += newProb
            solutions[newProb.name] = arrayOf<Solution>(
                    TexturedQuadsGLBindless())
//            mSolutions[mProblems.back()->GetName()].push_back(new TexturedQuadsGLBindlessMultiDraw())
//            mSolutions[mProblems.back()->GetName()].push_back(new TexturedQuadsGLNaive())
//            mSolutions[mProblems.back()->GetName()].push_back(new TexturedQuadsGLNaiveUniform())
//            mSolutions[mProblems.back()->GetName()].push_back(new TexturedQuadsGLNoTex())
//            mSolutions[mProblems.back()->GetName()].push_back(new TexturedQuadsGLNoTexUniform())
//            mSolutions[mProblems.back()->GetName()].push_back(new TexturedQuadsGLSparseBindlessTextureArray())
//            mSolutions[mProblems.back()->GetName()].push_back(new TexturedQuadsGLSparseBindlessTextureArrayMultiDraw(true))
//            mSolutions[mProblems.back()->GetName()].push_back(new TexturedQuadsGLSparseBindlessTextureArrayMultiDraw(false))
//            mSolutions[mProblems.back()->GetName()].push_back(new TexturedQuadsGLTextureArray())
//            mSolutions[mProblems.back()->GetName()].push_back(new TexturedQuadsGLTextureArrayUniform())
//            mSolutions[mProblems.back()->GetName()].push_back(new TexturedQuadsGLTextureArrayMultiDraw(true))
//            mSolutions[mProblems.back()->GetName()].push_back(new TexturedQuadsGLTextureArrayMultiDraw(false))
//            mSolutions[mProblems.back()->GetName()].push_back(new TexturedQuadsGLTextureArrayMultiDrawBuffer(true))
//            mSolutions[mProblems.back()->GetName()].push_back(new TexturedQuadsGLTextureArrayMultiDrawBuffer(false))
//            #if WITH_D3D11
//            mSolutions[mProblems.back()->GetName()].push_back(new TexturedQuadsD3D11Naive())
//            #endif
        } else {
            newProb.shutdown()
            newProb.destroy()
        }
    }

    fun getSolutions(problem: Problem, activeApi: OpenGLBase? = null): Array<Solution> {

        val tmpProblems = solutions[problem.name]!!

        activeApi?.let {
            val apiType = activeApi.apiType
            return tmpProblems.filter { it supportsApi apiType }.toTypedArray()
        }

        return tmpProblems
    }
}
