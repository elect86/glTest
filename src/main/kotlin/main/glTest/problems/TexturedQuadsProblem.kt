package main.glTest.problems

import gli_.Texture
import gli_.gli
import glm_.glm
import glm_.mat4x4.Mat4
import glm_.vec2.Vec2
import glm_.vec3.Vec3
import glm_.vec4.Vec4
import kool.FloatBuffer
import kool.free
import kool.shortBufferOf
import main.glTest.Mat4Buffer
import main.glTest.solutions.Solution
import java.nio.FloatBuffer
import java.nio.ShortBuffer


val objectsBX = 100
val objectsBY = 100
val objectBCount = objectsBX * objectsBY


class VertexB(val pos: Vec3, val tex: Vec2) {
    constructor(posX: Float, posY: Float, posZ: Float, texX: Float, texY: Float) :
            this(Vec3(posX, posY, posZ), Vec2(texX, texY))

    fun to(buffer: FloatBuffer, index: Int) {
        pos.to(buffer, index)
        tex.to(buffer, index + Vec3.length)
    }

    companion object {
        val length get() = Vec3.length + Vec2.length
    }
}

fun VertexBufferB(vertices: Collection<VertexB>) = VertexBufferB(vertices.size) { vertices.elementAt(it) }
fun VertexBufferB(vararg vertices: VertexB) = VertexBufferB(vertices.size) { vertices.elementAt(it) }

fun VertexBufferB(size: Int, block: (Int) -> VertexB): VertexBufferB {
    val buffer = VertexBufferB(FloatBuffer(VertexB.length * size))
    for (i in 0 until size)
        block(i).to(buffer.data, i * VertexB.length)
    return buffer
}

inline class VertexBufferB(val data: FloatBuffer) {

    fun free() = data.free()

//    val size get() = data.cap / (Vec3.size * 2)
}

val textureNames = arrayOf("Mandelbrot.dds", "image.dds")

class TexturedQuadsProblem : Problem() {

    var transforms: Mat4Buffer? = null
    var vertices: VertexBufferB? = null
    lateinit var indices: ShortBuffer
    val textures = ArrayList<Texture>()

    var iteration = 0

    override fun destroy() {
        textures.forEach { it.dispose() }
        textures.clear()
    }

    override fun getClearValues(outCol: Vec4): Float {
        outCol.put(0f, 0f, 0.1f, 1f)
        return 1f
    }

    override fun init(): Boolean {

        genUnitQuad()
        transforms = Mat4Buffer(objectBCount)

        return loadTextures()
    }

    override fun render() {
        update()

        (activeSolution as? TexturedQuadsSolution).render(transforms)

    }

    override fun shutdown() {
        super.shutdown()

        textures.forEach { it.dispose() }
        textures.clear()

        indices.free()
        vertices!!.free()
        transforms!!.free()
    }

    override val name get() = "TexturedQuadsProblem"

    override fun setSolution(solution: Solution?): Boolean {

        if (!super.setSolution(solution)) return false

        activeSolution?.let { sol ->
            println("Solution ${sol.name} - Initializing.")
            return (sol as TexturedQuadsSolution).init(vertices, indices, textures).also { ret ->
                println("Solution ${sol.name} - Initialize complete (Success: $ret).")
            }
        }

        return true
    }

    fun update() {
        val angle = iteration * 0.01f

        var mIdx = 0
        for (x in 0 until objectsBX) {
            for (y in 0 until objectsBY) {
                transforms!!.matrixRotationZ(mIdx, angle)
                transforms!!.data
                        .put(mIdx * Mat4.length + 12, 2f * x - objectsBX)
                        .put(mIdx * Mat4.length + 13, 2f * y - objectsBY)
                        .put(mIdx * Mat4.length + 14, 0f)
                mIdx++
            }
        }

        ++iteration

        if (angle > 2 * glm.PIf)
            iteration = 0
    }

    fun genUnitQuad() {

        // Buffers
        vertices = VertexBufferB(
                VertexB(-0.5f, -0.5f, +0f, 0f, 0f),
                VertexB(+0.5f, -0.5f, +0f, 0f, 1f),
                VertexB(+0.5f, +0.5f, +0f, 1f, 0f),
                VertexB(-0.5f, +0.5f, +0f, 1f, 1f))

        indices = shortBufferOf(0, 1, 2, 0, 2, 3)
    }

    fun loadTextures(): Boolean {

        for (texName in textureNames)
            try {
                textures += gli.load("textures/$texName")
            } catch (err: Error) {
                System.err.println("Failed to initialize $name, couldn't load texture '$texName' from disk.")
                return false
            }

        return true
    }
}