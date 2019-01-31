/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package main.apiTest.problems

import glm_.vec4.Vec4
import main.apiTest.solutions.NullSolution
import main.apiTest.solutions.Solution

/**
 *
 * @author GBarbieri
 */
class NullProblem : Problem() {

    override val name get() = "NullProblem"

    override fun init() = true  // Nothing to initialize

    override fun render() {} // Nothing, because it's the NULL solution.

    override fun getClearValues(outCol: Vec4): Float {
        outCol.put(0.2f, 0f, 0f, 1f)
        return 1f // outDepth
    }

    override fun setSolution(solution: Solution?): Boolean {
        if (!super.setSolution(solution))
            return false

        activeSolution?.let { sol ->
            println("Solution ${sol.name} - Initializing.")
            return (sol as NullSolution).init().also { ret ->
                println("Solution ${sol.name} - Initialize complete (Success: $ret).")
            }
        }

        return true
    }
}
