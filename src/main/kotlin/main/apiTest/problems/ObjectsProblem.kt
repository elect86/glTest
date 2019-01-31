package main.apiTest.problems

import glm_.func.cos
import glm_.func.sin
import glm_.glm
import glm_.mat4x4.Mat4
import glm_.vec3.Vec3
import glm_.vec4.Vec4
import kool.*
import main.apiTest.Mat4Buffer
import main.apiTest.solutions.Solution
import main.apiTest.solutions.ObjectsSolution
import java.nio.FloatBuffer
import java.nio.ShortBuffer


const val DRAW_SINGLE_TRIANGLE = false

const val objectsX = 64
const val objectsY = 64
const val objectsZ = 64
const val objectCount = objectsX * objectsY * objectsZ

const val transformCount = objectCount
val vertexCount = if (DRAW_SINGLE_TRIANGLE) 3 else 8
val indexCount = if (DRAW_SINGLE_TRIANGLE) 3 else 36

class VertexA(val pos: Vec3, val col: Vec3) {
    constructor(posX: Float, posY: Float, posZ: Float, colX: Float, colY: Float, colZ: Float) :
            this(Vec3(posX, posY, posZ), Vec3(colX, colY, colZ))

    fun to(buffer: FloatBuffer, index: Int) {
        pos.to(buffer, index)
        col.to(buffer, index + Vec3.length)
    }

    companion object {
        val length get() = Vec3.length * 2
    }
}

fun VertexBufferA(vertices: Collection<VertexA>) = VertexBufferA(vertices.size) { vertices.elementAt(it) }

fun VertexBufferA(size: Int, block: (Int) -> VertexA): VertexBufferA {
    val buffer = VertexBufferA(FloatBuffer(VertexA.length * size))
    for (i in 0 until size)
        block(i).to(buffer.data, i * VertexA.length)
    return buffer
}

inline class VertexBufferA(val data: FloatBuffer) {

//    val size get() = data.cap / (Vec3.size * 2)
}

class ObjectsProblem : Problem() {

    var transforms: Mat4Buffer? = null
    var vertices: VertexBufferA? = null
    lateinit var indices: ShortBuffer

    var iteration = 0

    override fun getClearValues(outCol: Vec4): Float {
        outCol.put(0f, 0.1f, 0f, 1f)
        return 1f
    }

    override fun init(): Boolean {
        genUnitCube()
        transforms = Mat4Buffer(transformCount)

        return true
    }

    override fun render() {
        update()

        activeSolution?.let {
            (it as ObjectsSolution).render(transforms!!)
        }
    }

    override fun shutdown() {
        super.shutdown()

        indices.clear()
        vertices!!.data.free()
        vertices = null
        transforms!!.data.free()
        transforms = null
    }

    override val name get() = "UntexturedObjects"

    override fun setSolution(solution: Solution?): Boolean {

        if (!super.setSolution(solution))
            return false

        activeSolution?.let { sol ->
            println("Solution ${sol.name} - Initializing.")
            return (sol as ObjectsSolution).init(vertices!!, indices, objectCount).also { ret ->
                println("Solution ${sol.name} - Initialize complete (Success: $ret).")
            }
        }

        return true
    }

    fun update() {

        fun rotationZ(m: Mat4, angle: Float) {
            val s = angle.sin
            val c = angle.cos

            m.put(
                    c, -s, 0f, 0f,
                    s, +c, 0f, 0f,
                    0f, 0f, 1f, 0f,
                    0f, 0f, 0f, 1f)
        }

        val angle = iteration * 0.01f

        var mIdx = 0
        for (x in 0 until objectsX) {
            for (y in 0 until objectsY) {
                for (z in 0 until objectsZ) {
                    val m = transforms!![mIdx++]
                    rotationZ(m, angle)
                    m[3, 0] = 2f * x - objectsX
                    m[3, 1] = 2f * y - objectsY
                    m[3, 2] = 2f * z - objectsZ
                }
            }
        }

        ++iteration

        if (angle > 2 * glm.PIf)
            iteration = 0
    }

    fun genUnitCube() {

        val vertices = arrayListOf(
                VertexA(-0.5f, +0.5f, -0.5f, 0f, 1f, 0f),
                VertexA(+0.5f, +0.5f, -0.5f, 1f, 1f, 0f),
                VertexA(+0.5f, +0.5f, +0.5f, 1f, 1f, 1f))
        if (!DRAW_SINGLE_TRIANGLE) {
            vertices += VertexA(-0.5f, +0.5f, +0.5f, 0f, 1f, 1f)
            vertices += VertexA(-0.5f, -0.5f, +0.5f, 0f, 0f, 1f)
            vertices += VertexA(+0.5f, -0.5f, +0.5f, 1f, 0f, 1f)
            vertices += VertexA(+0.5f, -0.5f, -0.5f, 1f, 0f, 0f)
            vertices += VertexA(-0.5f, -0.5f, -0.5f, 0f, 0f, 0f)
        }

        this.vertices = VertexBufferA(vertices)

        assert(vertexCount == vertices.size) { "VertexA count mismatch" }

        val indices = when {
            DRAW_SINGLE_TRIANGLE -> shortBufferOf(0, 1, 2)
            else -> shortBufferOf(
                    0, 1, 2, 0, 2, 3,
                    4, 5, 6, 4, 6, 7,
                    3, 2, 5, 3, 5, 4,
                    2, 1, 6, 2, 6, 5,
                    1, 7, 6, 1, 0, 7,
                    0, 3, 4, 0, 4, 7)
        }
    }
}