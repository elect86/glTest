/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package main.apiTest.problems

import glm_.L
import glm_.vec2.Vec2
import glm_.vec2.Vec2i
import glm_.vec4.Vec4
import kool.*
import main.apiTest.solutions.DynamicStreamingSolution
import main.apiTest.solutions.Solution
import java.nio.FloatBuffer

val vertsPerParticle = 6

inline class Vec2Buffer(val data: FloatBuffer) {

    fun free() = data.free()

    val rem get() = data.rem / Vec2.size

    val remSize get() = data.rem.L

    fun adr(index: Int) = data.adr + index * Vec2.size

    fun set(index: Int, x: Float, y: Float) {
        data[index * Vec2.length] = x
        data[index * Vec2.length + 1] = y
    }
}

fun Vec2Buffer(size: Int) = Vec2Buffer(FloatBuffer(size * Vec2.length))

class DynamicStreamingProblem : Problem() {

    val particleCount = Vec2i(500, 320)
    val particleTotal = particleCount.x * particleCount.y
    val vertexCount = particleTotal * vertsPerParticle
    val particleBufferSize = Vec2.size * vertexCount

    var vertexData: Vec2Buffer? = null
    var iteration = 0

    override fun getClearValues(outCol: Vec4): Float {
        outCol.put(0.3f, 0f, 0.3f, 1f)
        return 1f
    }

    override fun init(): Boolean {
        vertexData = Vec2Buffer(particleTotal * vertsPerParticle)
        return true
    }

    override fun render() {
        // TODO: Update should be moved into its own thread, but for now let's just do it here.
        update()

        activeSolution?.let {
            (it as DynamicStreamingSolution).render(vertexData!!)
        }
    }

    override fun shutdown() {
        super.shutdown()
        vertexData!!.free()
        vertexData = null
    }

    override val name get() = "DynamicStreaming"

    override fun setSolution(solution: Solution?): Boolean {

        if (!super.setSolution(solution))
            return false

        activeSolution?.let { sol ->
            println("Solution ${sol.name} - Initializing.")
            return (sol as DynamicStreamingSolution).init(vertexCount).also { ret ->
                println("Solution ${sol.name} - Initialize complete (Success: $ret).")
            }
        }
        return true
    }

    fun update() {

        val spacing = 1f
        val w = 1f
        val h = 1f

        val marchPixelsX = 24
        val marchPixelsY = 128

        val offsetX = (iteration % marchPixelsX) * w
        val offsetY = ((iteration / marchPixelsX) % marchPixelsY) * h

        var address = 0
        for (yPos in 0 until particleCount.y) {

            val y = spacing + yPos * (spacing + h)

            for (xPos in 0 until particleCount.x) {

                val x = spacing + xPos * (spacing + w)

                vertexData!!.apply {
                    set(address + 0, x + offsetX + 0, y + offsetY + 0)
                    set(address + 1, x + offsetX + w, y + offsetY + 0)
                    set(address + 2, x + offsetX + 0, y + offsetY + h)
                    set(address + 3, x + offsetX + w, y + offsetY + 0)
                    set(address + 4, x + offsetX + 0, y + offsetY + h)
                    set(address + 5, x + offsetX + w, y + offsetY + h)

                    address += vertsPerParticle
                }
            }
        }

        ++iteration
    }
}