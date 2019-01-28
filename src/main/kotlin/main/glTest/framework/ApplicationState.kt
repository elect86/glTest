/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package main.glTest.framework

import java.util.ArrayList
import glm_.vec2.Vec2i
import org.lwjgl.system.Platform
import uno.glfw.glfw
import java.lang.Error

fun main(){
    ApplicationState()
}

const val DEBUG = true

const val inactiveProblem = -1
const val inactiveSolution = -1

object Options {

    var benchmarkMode = false
    var benchmarkTime = 5f
    val resolution = Vec2i(1024, 768)

    val defaultInitialProblem = "NullProblem"
    val defaultInitialSolution = "NullSolution"

    var initialProblem = defaultInitialProblem
    var initialSolution = defaultInitialSolution

    var initialApi = when(Platform.get()) {
        Platform.MACOSX -> OpenGLCore.shortName
        else -> OpenGLGeneric.shortName
    }
}

/**
 *
 * @author GBarbieri
 */
class ApplicationState {

    init {
        glfw.init()
    }

    val resolution = Options.resolution

    val activeProblem = inactiveProblem
    val activeSolution = inactiveSolution

    val glApis = mutableMapOf<String, OpenGLBase>()

    // ~ createGfxApis
    init {
        val generic = OpenGLGeneric()
        if (generic.init("apitest - OpenGL (compat)", Vec2i(), resolution))
            glApis[OpenGLGeneric.shortName] = generic

        val core = OpenGLCore()
        if (core.init("apitest - OpenGL (core)", Vec2i(), resolution))
            glApis[OpenGLCore.shortName] = core
    }

    val activeGl = glApis[Options.initialApi] ?: throw Error("Failed to select api with name '${Options.initialApi}'")

    init {
        activeGl.activate()
    }

    val factory = ProblemFactory()
    private var problems: ArrayList<Problem>? = null
    private var solutions: Array<Solution>? = null
    private var problem: Problem? = null
    private var solution: Solution? = null
    private val vertexArrayName = GLBuffers.newDirectIntBuffer(1)
    private val queryName = GLBuffers.newDirectIntBuffer(1)
    private val gpuTime = GLBuffers.newDirectLongBuffer(1)
    private var offsetProblem = 0
    private var offsetSolution = 0
    private var frames = 0
    private var cpuStart: Long = 0
    private var cpuTotal: Long = 0
    private var gpuTotal: Long = 0
    private var updateCountersStart: Long = 0
    private val updateTick: Long = 1000

    fun init(drawable: GLAutoDrawable) {

        val gl4 = drawable.getGL().getGL4()
        System.out.println("" + GLContext.getCurrent().getGLVersion())
        System.out.println("Vendor: " + gl4.glGetString(GL_VENDOR))
        System.out.println("Renderer: " + gl4.glGetString(GL_RENDERER))
        System.out.println("Version: " + gl4.glGetString(GL_VERSION))
        System.out.println("Shading Language Version: " + gl4.glGetString(GL_SHADING_LANGUAGE_VERSION))

        gl4.setSwapInterval(0)

        // Default GL State
        gl4.glCullFace(GL_FRONT)
        gl4.glEnable(GL_CULL_FACE)
        gl4.glDisable(GL_SCISSOR_TEST)
        gl4.glEnable(GL_DEPTH_TEST)
        gl4.glDepthMask(true)
        gl4.glDepthFunc(GL_LESS)
        gl4.glDisable(GL_BLEND)
        gl4.glColorMask(true, true, true, true)

        if (DEBUG) {
            gl4.glDebugMessageControl(GL_DONT_CARE, GL_DONT_CARE, GL_DONT_CARE, 0, null, false)
            gl4.glDebugMessageControl(GL_DONT_CARE, GL_DONT_CARE, GL_DEBUG_SEVERITY_HIGH, 0, null, true)
            gl4.glDebugMessageControl(GL_DONT_CARE, GL_DONT_CARE, GL_DEBUG_SEVERITY_MEDIUM, 0, null, true)
        }

        gl4.glGetQueryiv(GL_TIME_ELAPSED, GL_QUERY_COUNTER_BITS, queryName)
        println("GL_QUERY_COUNTER_BITS: " + queryName.get(0))

        gl4.glGenQueries(1, queryName)

        // Now that we have something valid, create our VAO and bind it. Ugh! So lame that this is required.
        gl4.glGenVertexArrays(1, vertexArrayName)
        gl4.glBindVertexArray(vertexArrayName.get(0))

        factory = ProblemFactory()
        problems = factory!!.getProblems()
        assert(problems!!.size > 0)

        setInitialProblemAndSolution(gl4, "NullProblem", "NullSolution")

        updateCountersStart = System.currentTimeMillis()
    }

    private fun setInitialProblemAndSolution(gl4: GL4, probName: String, solnName: String) {

        for (i in problems!!.indices) {
            if (problems!![i].getName() == probName) {
                problem = problems!![i]
                break
            }
        }

        solutions = factory!!.getSolutions(problem)
        for (sol in solutions!!) {
            if (sol.getName() == solnName) {
                solution = sol
                break
            }
        }

        initProblem(gl4)

        initSolution(gl4, 0)

        onProblemOrSolutionSet()
    }

    fun display(drawable: GLAutoDrawable) {

        val gl4 = drawable.getGL().getGL4()

        if (offsetProblem != 0) {
            changeProblem(gl4)
        }
        if (offsetSolution != 0) {
            changeSolution(gl4)
        }

        if (problem == null) {
            return
        }

        gl4.glBeginQuery(GL_TIME_ELAPSED, queryName.get(0))
        run {
            cpuStart = System.nanoTime()
            run {
                // This is the main entry point shared by all tests.
                problem!!.render(gl4)
                frames++
            }
            cpuTotal += System.nanoTime() - cpuStart
        }
        gl4.glEndQuery(GL_TIME_ELAPSED)
        gl4.glGetQueryObjectui64v(queryName.get(0), GL_QUERY_RESULT, gpuTime)
        gpuTotal += gpuTime.get(0)

        // Present the results.
        //        if (frames == problem.getSolution().updateFps) {
        if (System.currentTimeMillis() - updateCountersStart > updateTick) {
            //            System.out.println("cpuTotal: " + cpuTotal / 1_000_000 + ", gpuTotal: " + gpuTotal + ", frames: " + frames);
            val cpu = String.format("%,.3f", cpuTotal.toDouble() / 1000000.0 / frames.toDouble()) + " ms"
            val gpu = String.format("%,.3f", gpuTotal.toDouble() / 1000000.0 / frames.toDouble()) + " ms"
            val fps = String.format("%,.2f", 1000 / (gpuTotal.toDouble() / 1000000.0 / frames.toDouble()))
            //            String fps = String.format("%,.2f", frames / ((double) gpuTotal / 1_000_000));
            println("CPU time: $cpu, GPU time: $gpu, theor. FPS: $fps")
            resetCounter()
        }
    }

    private fun resetCounter() {
        frames = 0
        cpuTotal = 0
        gpuTotal = 0
        updateCountersStart = System.currentTimeMillis()
    }

    fun reshape(drawable: GLAutoDrawable, x: Int, y: Int, width: Int, height: Int) {

        val gl4 = drawable.getGL().getGL4()

        gl4.glViewport(0, 0, width, height)
    }

    fun dispose(drawable: GLAutoDrawable) {

        val gl4 = drawable.getGL().getGL4()

        // Must cleanup before we call base class.
        gl4.glBindVertexArray(0)
        gl4.glDeleteVertexArrays(1, vertexArrayName)

        System.exit(0)
    }

    fun keyPressed(e: KeyEvent) {
        when (e.getKeyCode()) {
            KeyEvent.VK_ESCAPE -> animator.stop()
            KeyEvent.VK_LEFT -> offsetProblem = -1
            KeyEvent.VK_RIGHT -> offsetProblem = 1
            KeyEvent.VK_UP -> offsetSolution = -1
            KeyEvent.VK_DOWN -> offsetSolution = 1
        }
    }

    fun keyReleased(e: KeyEvent) {

    }

    private fun changeProblem(gl4: GL4) {

        shutdownSolution(gl4)

        shutdownProblem(gl4)

        val problemCount = problems!!.size
        var problemId = problems!!.indexOf(problem)
        problemId = (problemId + problemCount + offsetProblem) % problemCount

        problem = problems!![problemId]

        initProblem(gl4)

        solutions = factory!!.getSolutions(problem)

        solution = solutions!![problem!!.getSolutionId()]

        initSolution(gl4, problem!!.getSolutionId())

        offsetProblem = 0

        onProblemOrSolutionSet()
    }

    private fun changeSolution(gl4: GL4) {

        shutdownSolution(gl4)

        val solutionCount = solutions!!.size
        if (solutionCount == 0) {
            return
        }

        var solutionId = problem!!.getSolutionId()
        solutionId = (solutionId + solutionCount + offsetSolution) % solutionCount

        solution = solutions!![solutionId]

        initSolution(gl4, solutionId)

        offsetSolution = 0

        onProblemOrSolutionSet()
    }

    private fun initSolution(gl4: GL4, solutionId: Int) {

        print("Solution " + solution!!.getName() + " init... ")
        println(if (solution!!.init(gl4)) "Ok" else "Fail")

        problem!!.setSolution(gl4, solution)
        problem!!.setSolutionId(solutionId)

        resetCounter()
    }

    private fun shutdownSolution(gl4: GL4) {

        print("Solution " + solution!!.getName() + " shutdown... ")
        println(if (solution!!.shutdown(gl4)) "Ok" else "Fail")

        problem!!.setSolution(gl4, null)
    }

    private fun initProblem(gl4: GL4) {

        print("Problem " + problem!!.getName() + " - init... ")
        println(if (problem!!.init(gl4)) "Ok" else "Fail")
    }

    private fun shutdownProblem(gl4: GL4) {

        print("Problem " + problem!!.getName() + " shutdown... ")
        println(if (problem!!.shutdown(gl4)) "Ok" else "Fail")
    }

    private fun onProblemOrSolutionSet() {

        System.gc()

        var newTitle = rootTitle + " - " + problem!!.getName()

        if (solution != null) {
            newTitle += " - " + solution!!.getName()
        }
        glWindow.setTitle(newTitle)

        System.gc()
    }

    companion object {

        val RESOLUTION = Vec2i(1024, 768)

        @Throws(InterruptedException::class)
        @JvmStatic
        fun main(args: Array<String>) {

            TimeHack6435126.enableHighResolutionTimer()

            val app = ApplicationState()
            app.setup()
        }

        var animator: Animator
    }
}
