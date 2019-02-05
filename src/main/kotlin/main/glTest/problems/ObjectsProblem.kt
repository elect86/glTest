package main.glTest.problems

import glm_.glm
import glm_.mat4x4.Mat4
import glm_.vec3.Vec3
import glm_.vec4.Vec4
import kool.*
import main.glTest.Mat4Buffer
import main.glTest.solutions.Solution
import main.glTest.solutions.ObjectsSolution
import java.nio.FloatBuffer
import java.nio.ShortBuffer


const val DRAW_SINGLE_TRIANGLE = false

const val objectsAX = 64
const val objectsAY = 64
const val objectsAZ = 64
const val objectACount = objectsAX * objectsAY * objectsAZ

const val transformACount = objectACount
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

    fun free() = data.free()
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
        transforms = Mat4Buffer(transformACount)

        return true
    }

    override fun render() {
        update()

        (activeSolution as? ObjectsSolution)?.render(transforms!!)
    }

    override fun shutdown() {
        super.shutdown()

        indices.clear()
        vertices!!.free()
        vertices = null
        transforms!!.free()
        transforms = null
    }

    override val name get() = "UntexturedObjects"

    override fun setSolution(solution: Solution?): Boolean {

        if (!super.setSolution(solution))
            return false

        activeSolution?.let { sol ->
            println("Solution ${sol.name} - Initializing.")
            return (sol as ObjectsSolution).init(vertices!!, indices).also { ret ->
                println("Solution ${sol.name} - Initialize complete (Success: $ret).")
            }
        }

        return true
    }

    fun update() {

        val angle = iteration * 0.01f

        var mIdx = 0
        for (x in 0 until objectsAX) {
            for (y in 0 until objectsAY) {
                for (z in 0 until objectsAZ) {
                    transforms!!.matrixRotationZ(mIdx, angle)
                    transforms!!.data
                            .put(mIdx * Mat4.length + 12, 2f * x - objectsAX)
                            .put(mIdx * Mat4.length + 13, 2f * y - objectsAY)
                            .put(mIdx * Mat4.length + 14, 2f * z - objectsAZ)
                    mIdx++
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

        indices = when {
            DRAW_SINGLE_TRIANGLE -> shortBufferOf(0, 1, 2)
            else -> shortBufferOf(
                    0, 1, 2, 0, 2, 3,
                    4, 5, 6, 4, 6, 7,
                    3, 2, 5, 3, 5, 4,
                    2, 1, 6, 2, 6, 5,
                    1, 7, 6, 1, 0, 7,
                    0, 3, 4, 0, 4, 7)
        }

        assert(indexCount == indices.cap) { "Index count mismatch" }
    }
}