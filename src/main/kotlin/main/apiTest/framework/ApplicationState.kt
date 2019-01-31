/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package main.apiTest.framework

import glm_.max
import java.util.ArrayList
import glm_.vec2.Vec2i
import main.apiTest.solutions.Solution
import org.lwjgl.system.Platform
import uno.glfw.glfw
import java.lang.Error


const val inactiveProblem = -1
const val inactiveSolution = -1

typealias ms = Long

object Options {

    var benchmarkMode = false
    var benchmarkTime: ms = 5_000L
    val resolution = Vec2i(1024, 768)

    val defaultInitialProblem = "NullProblem"
    val defaultInitialSolution = "NullSolution"

    var initialProblem = defaultInitialProblem
    var initialSolution = defaultInitialSolution

    var initialApi = when (Platform.get()) {
        Platform.MACOSX -> OpenGLCore.shortName
        else -> OpenGLGeneric.shortName
    }
}

typealias BenchmarkResults = MutableMap<Triple<String, String, String>, Triple<Int, ms, Int>>

fun BenchmarkResults(): BenchmarkResults = mutableMapOf()

class ApplicationState {

    val resolution = Options.resolution

    var activeProblem = inactiveProblem
    var activeSolution = inactiveSolution

    val glApis = mutableMapOf<String, OpenGLBase>()

    // ~ createGfxApis
    init {
        val generic = OpenGLGeneric()
        if (generic.init("apitest - OpenGL (compat)", Vec2i(50), resolution))
            glApis[OpenGLGeneric.shortName] = generic

        val core = OpenGLCore()
        if (core.init("apitest - OpenGL (core)", Vec2i(50), resolution))
            glApis[OpenGLCore.shortName] = core
    }

    var activeApi = glApis[Options.initialApi] ?: throw Error("Failed to select api with name '${Options.initialApi}'")

    init {
        activeApi.activate()
    }

    val factory = ProblemFactory(false)

    val problems = factory.problems

    init {
        assert(problems.isNotEmpty())
    }

    lateinit var solutions: Array<Solution>

    var frameCount = 0
    var timerStart = 0L

    var benchmarkTime = Options.benchmarkTime
    var benchmarkMode = Options.benchmarkMode

    object benchmarkState {
        var glApisBenchmarked = 0L
        var problemsBenchmarked = 0L
        var solutionsBenchmarked = 0L
        var benchmarkTimeStart: ms = 0L
        var benchmarkSingle = false

        val benchmarkTimings = BenchmarkResults()
    }

    init {
        setInitialProblemAndSolution(Options.initialProblem, Options.initialSolution)

        // This logic currently means you cannot benchmark the NullProblem/NullSolution
        benchmarkState.benchmarkSingle = benchmarkMode
                && (Options.initialProblem != Options.defaultInitialProblem || Options.initialSolution != Options.defaultInitialSolution)

        resetTimer()

        if (benchmarkMode)
            benchmarkState.benchmarkTimeStart = System.currentTimeMillis()
    }

    fun destroy() {
        shutdownActiveProblem()
        destroyGlApis()
    }

    fun getActiveProblem() = getProblem(activeProblem)
    fun getActiveSolution() = getSolution(activeSolution)

    val problemCount get() = problems.size
    val solutionCount get() = solutions.size
    val activeApiCount get() = glApis.size

    fun nextProblem() = changeProblem(1)
    fun prevProblem() = changeProblem(-1)

    fun nextSolution() = changeSolution(1)
    fun prevSolution() = changeSolution(-1)

    fun nextAPI() {

        assert(glApis.isNotEmpty())

        // Don't do any of this if we don't have another API to move to.
        if (glApis.size == 1) return

        // Shutdown the problem for now, we'll bring it back up in a minute.  Don't actually clear the active problem here,
        // because we're going to try to set it back up shortly.
        activeProblem = shutdownActiveProblem()

        val values = glApis.values
        val idx = values.indexOf(activeApi)
        assert(idx != -1)
        // In case this is the last one, wrap.
        val nextIdx = (idx + 1) % values.size
        val it = values.elementAt(nextIdx)

        // Activate the new before deactivating the old to avoid the window disappearing problem.
        it.activate()
        activeApi.deactivate()

        activeApi = it

        // Try to select the same problem again.
        changeProblem(0)
    }

    fun update() {

        if (benchmarkMode) {
            ++frameCount
            val elapsed: ms = System.currentTimeMillis() - benchmarkState.benchmarkTimeStart

            if (elapsed >= benchmarkTime) {
                ++benchmarkState.solutionsBenchmarked

                val key = Triple(activeApi.getShortName(), getActiveProblem()!!.name, getActiveSolution()!!.name)
                benchmarkState.benchmarkTimings[key] = Triple(frameCount, elapsed, 0)

                if (benchmarkState.benchmarkSingle) {
                    shutdownActiveProblem()
                    return
                }

                if (benchmarkState.solutionsBenchmarked >= solutionCount) {

                    ++benchmarkState.problemsBenchmarked

                    if (benchmarkState.problemsBenchmarked >= problems.size) {

                        ++benchmarkState.glApisBenchmarked

                        if (benchmarkState.glApisBenchmarked >= glApis.size) {
                            shutdownActiveProblem()
                            return
                        } else {
                            benchmarkState.solutionsBenchmarked = 0
                            benchmarkState.problemsBenchmarked = 0

                            nextAPI()
                            benchmarkState.benchmarkTimeStart = System.currentTimeMillis()
                        }
                    } else {
                        benchmarkState.solutionsBenchmarked = 0

                        nextProblem()
                        benchmarkState.benchmarkTimeStart = System.currentTimeMillis()
                    }
                } else {
                    nextSolution()
                    benchmarkState.benchmarkTimeStart = System.currentTimeMillis()
                }
            }
        } else updateFPS()
    }

    fun isBenchmarkModeComplete() = benchmarkState.glApisBenchmarked >= glApis.size
            || (benchmarkState.benchmarkSingle && benchmarkState.solutionsBenchmarked >= 1)

    val benchmarkResults get() = benchmarkState.benchmarkTimings

//    fun broadcastToOtherWindows(SDL_Event* _event)
//    {
//        // TODO: Add any other messages as necessary to keep the windows in sync.
//        assert(_event->type == SDL_WINDOWEVENT
//        && (_event->window.event == SDL_WINDOWEVENT_MOVED));
//
//        for (auto it = mGfxApis.begin(); it != mGfxApis.end(); ++it) {
//        if (it->second == mActiveApi) {
//        continue;
//    }
//
//        it->second->MoveWindow(_event->window.data1, _event->window.data2);
//    }
//    }

    fun getProblem(index: Int) = problems.getOrNull(index)
    fun getSolution(index: Int) = solutions.getOrNull(index)

    fun shutdownActiveProblem(): Int {
        val index = activeProblem
        getActiveProblem()?.let { problem ->
            problem.setSolution(null)
            activeSolution = inactiveSolution

            problem.shutdown()
            activeProblem = inactiveProblem
        }
        return index
    }

    fun setInitialProblemAndSolution(probName: String, solnName: String) {

        // TODO: This should be cleaned up. It's error prone.
        assert(activeProblem == inactiveProblem)
        if (probName.isNotEmpty()) {
            activeProblem = problems.indexOfFirst { it.name == probName }

            if (activeProblem == inactiveProblem)
                System.err.println("Couldn't locate problem named '$probName'. Run with -h to see all valid problem names.")

            if (!getActiveProblem()!!.init())
                System.err.println("Failed to initialize problem '$probName', exiting.")

            // We set the problem, now try to find the solution.
            solutions = factory.getSolutions(getActiveProblem()!!, activeApi)
            if (solnName.isNotEmpty()) {
                for (i in solutions.indices) {
                    val sol = solutions[i]
                    if (sol.name == solnName) {
                        activeSolution = i
                        if (!getActiveProblem()!!.setSolution(getActiveSolution()))
                            System.err.println("Unable to initialize solution '$solnName', exiting.")

                        onSetProblemOrSolution()
                        break
                    }
                }
            } else {
                changeSolution(1)
                if (activeSolution == inactiveSolution)
                    System.err.println("Unable to initialize any solutions for '$probName', exiting.")
                onSetProblemOrSolution()
            }

            if (activeSolution == inactiveSolution)
                System.err.println("Couldn't locate solution named '$solnName' for problem '$probName'. Run with -h to see all valid problem and solution names.")

        } else if (solnName.isNotEmpty()) {

            val (prob, solution) = findProblemWithSolution(solnName)
            activeProblem = prob

            if (solution == inactiveSolution)
                System.err.println("Unable to find solution '$solnName'. Run with -h to see all valid solution names.")

            if (activeProblem == inactiveProblem)
                System.err.println("Couldn't locate problem that had solution '$solnName'")

            if (!getActiveProblem()!!.init())
                System.err.println("Failed to initialize problem '$probName', exiting.")

            solutions = factory.getSolutions(getActiveProblem()!!, activeApi)
            activeSolution = solution
            if (!getActiveProblem()!!.setSolution(getActiveSolution()))
                System.err.println("Unable to initialize solution '$solnName', exiting.")

            onSetProblemOrSolution()

            assert(activeProblem >= 0 && activeSolution >= 0)

        } else
            System.err.println("""
                You went through some effort to specify a blank initial problem and initial solution.
                Congratulations, that doesn't work.""")
    }

    fun findProblemWithSolution(solnName: String): Pair<Int, Int> {
        for (probIdx in problems.indices) {
            val prob = problems[probIdx]
            val allSolutions = factory.getSolutions(prob, activeApi)
            for (solnIdx in allSolutions.indices)
                if (allSolutions[solnIdx].name == solnName)
                    return probIdx to solnIdx
        }
        return inactiveProblem to inactiveSolution
    }

    fun changeProblem(offset_: Int) {

        var offset = offset_
        val prevProblem = activeProblem
        if (offset != 0) shutdownActiveProblem()

        val problemCount = problems.size

        // If we are going backwards, we need to pretend we have a valid problem picked already,
        // or we won't pick the last problem on the way back through.
        val curProbIndex = if (offset >= 0) prevProblem else 0 max prevProblem
        var newProblem = (curProbIndex + problemCount + offset) % problemCount

        // If the offset was 0 (because we changed gfx apis) change it to 1 in case this problem is inapplicable to this
        // api and we need to look for the next one.
        if (offset == 0)
            offset = 1

        for (u in 0 until problemCount) {
            val newProb = getProblem(newProblem)!!
            if (newProb.init()) {
                activeProblem = newProblem
                solutions = factory.getSolutions(newProb, activeApi)

                changeSolution(1)
                if (activeSolution != inactiveSolution)
                    break

                // Otherwise we failed, continue along the way.
                newProb.shutdown()
                activeProblem = inactiveProblem

                if (benchmarkMode)
                    ++benchmarkState.problemsBenchmarked
            }

            newProblem = (newProblem + problemCount + offset) % problemCount
        }

        onSetProblemOrSolution()
    }

    fun changeSolution(offset: Int) {

        val activeProb = getActiveProblem()!!

        val solutionCount = solutions.size
        if (solutionCount == 0) return

        val prevSolution = activeSolution
        activeProb.setSolution(null)
        activeSolution = inactiveSolution

        // If we are going backwards, we need to pretend we have a valid problem picked already, or we won't pick the last
        // problem on the way back through.
        val curSolnIndex = if (offset >= 0) prevSolution else 0 max prevSolution

        var newSolution = (curSolnIndex + solutionCount + offset) % solutionCount
        for (u in 0 until solutionCount) {
            val newSoln = getSolution(newSolution)
            if (activeProb.setSolution(newSoln)) {
                activeSolution = newSolution
                break
            } else if (benchmarkMode) {
                val key = Triple(activeApi.getShortName(), getActiveProblem()!!.name, newSoln!!.name)
                benchmarkState.benchmarkTimings[key] = Triple(0, 0L, 0)
                ++benchmarkState.solutionsBenchmarked
            }

            newSolution = (newSolution + solutionCount + offset) % solutionCount
        }

        onSetProblemOrSolution()
    }

    fun onSetProblemOrSolution() {

        activeApi.onProblemOrSolutionSet(getActiveProblem()!!.name, getActiveSolution()?.name ?: "")

        getActiveSolution()?.size = activeApi.size

        resetTimer()
    }

    fun updateFPS()    {
        ++frameCount
        val now = System.currentTimeMillis()
        val dt = (now - timerStart) / 1_000.0
        if (dt >= 1.0) {
            println("FPS: ${frameCount / dt}")
            frameCount = 0
            timerStart = now
        }
    }

    fun resetTimer() {
        frameCount = 0
        timerStart = System.currentTimeMillis()
    }

    fun destroyGlApis()    {
        glApis.values.forEach { it.destroy() }
        glApis.clear()
    }
}
