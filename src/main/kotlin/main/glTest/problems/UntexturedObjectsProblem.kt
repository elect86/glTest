package main.glTest.problems

import glm_.mat4x4.Mat4
import glm_.vec4.Vec4
import main.glTest.solutions.Solution

/**
 *
 * @author GBarbieri
 */
class UntexturedObjectsProblem : Problem(){

    val transforms = ArrayList<Mat4>()
    std::vector<Vertex> mVertices
    std::vector<Index> mIndices

    unsigned int mIteration

    override fun getClearValues(outCol: Vec4): Float {
        outCol.put(0f, 0.1f, 0f, 1f)
        return 1f
    }

    override fun init(): Boolean {
        genUnitCube()
        transforms.ensureCapacity(transformCount)

        return true
    }

    override fun render() {
        update()

        activeSolution?.let {
            (it as UntexturedObjectsSolution).render(transforms)
        }
    }

    override fun shutdown() {
        super.shutdown()

        indices.clear()
        vertices.clear()
        transforms.clear()
    }

    override val name        get() = "UntexturedObjects"

    override fun setSolution(solution: Solution?): Boolean {

        if (!super.setSolution(solution))
            return false

        activeSolution?.let {sol ->
            println("Solution ${sol.name} - Initializing.")
            return (sol as UntexturedObjectsSolution).init(vertices, indices, objectCount).also { ret ->
                println("Solution ${sol.name} - Initialize complete (Success: $ret).")
            }
        }

        return true;
    }

    struct Vertex
            {
                Vec3 pos
                Vec3 color
            }

    typedef uint16_t Index


    protected:
    void Update()

    void genUnitCube()
}