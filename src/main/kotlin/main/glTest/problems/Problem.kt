package main.glTest.problems

import glm_.vec4.Vec4
import main.glTest.solutions.Solution
import org.lwjgl.opengl.GL11C.glGetError

/**
 *
 * @author GBarbieri
 */
abstract class Problem {

    open fun destroy() {}

    /** @return outDepth */
    abstract fun getClearValues(outCol: Vec4): Float

    abstract fun init(): Boolean
    abstract fun render()
    open fun shutdown() {
        setSolution(null)
    }

    abstract val name: String

    var activeSolution: Solution? = null

    open fun setSolution(solution: Solution?): Boolean {

        assert(solution == null || solution.problemName == name)

        activeSolution?.let {
            println("Solution ${it.name} - shutdown beginning.")
            it.shutdown()
            glGetError()
            println("Solution ${it.name} shutdown complete.")
        }

        activeSolution = solution

        // The parameters to be handed off by Init are specific to the problem being solved,
        // by the derived class.
        return true
    }
}
