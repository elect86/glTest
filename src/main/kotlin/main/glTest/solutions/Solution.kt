package main.glTest.solutions

import glm_.func.rad
import glm_.glm
import glm_.mat4x4.Mat4
import glm_.vec2.Vec2i
import main.glTest.framework.OpenGlApi

abstract class Solution {

    open fun shutdown() {}

    /** The name of this solution. */
    abstract val name: String

    /** The name of the problem this solution addresses. */
    abstract val problemName: String

    /** Whether this solution could conceivably run on this Graphics API.
     *  If the support is conditional on an extension, the function should return
     *  true and then test for specific support in the Init function (returning false if unsupported). */
    abstract infix fun supportsApi(api: OpenGlApi): Boolean

    var proj = Mat4()

    var size = Vec2i()
        set(value) {
        field = value
        proj = glm.perspective(45f.rad, size.aspect, 0.1f, 10000f)
    }
}

class NullSolution : Solution() {

    fun init() = true

    override val name get() = "NullSolution"
    override val problemName get() = "NullProblem"
    override fun supportsApi(api: OpenGlApi) = true
}