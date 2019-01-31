//package main.apiTest.problems
//
//import glm_.vec2.Vec2
//import glm_.vec3.Vec3
//import glm_.vec4.Vec4
//import kool.FloatBuffer
//import main.apiTest.Mat4Buffer
//import java.nio.FloatBuffer
//import java.nio.ShortBuffer
//
//
//class VertexB(val pos: Vec3, val tex: Vec2) {
//    constructor(posX: Float, posY: Float, posZ: Float, texX: Float, texY: Float) :
//            this(Vec3(posX, posY, posZ), Vec2(texX, texY))
//
//    fun to(buffer: FloatBuffer, index: Int) {
//        pos.to(buffer, index)
//        tex.to(buffer, index + Vec3.length)
//    }
//
//    companion object {
//        val length get() = Vec3.length + Vec2.length
//    }
//}
//
//fun VertexBufferB(vertices: Collection<VertexB>) = VertexBufferB(vertices.size) { vertices.elementAt(it) }
//
//fun VertexBufferB(size: Int, block: (Int) -> VertexB): VertexBufferB {
//    val buffer = VertexBufferB(FloatBuffer(VertexB.length * size))
//    for (i in 0 until size)
//        block(i).to(buffer.data, i * VertexB.length)
//    return buffer
//}
//
//inline class VertexBufferB(val data: FloatBuffer) {
//
////    val size get() = data.cap / (Vec3.size * 2)
//}
//
//class TexturedQuadsProblem : Problem(){
//
//    var transforms: Mat4Buffer? = null
//    var vertices: VertexBufferB? = null
//    lateinit var indices: ShortBuffer
//    std::vector<TextureDetails*> mTextures
//
//    TexturedQuadsProblem::~TexturedQuadsProblem()
//    {
//        for (auto it = mTextures.begin(); it != mTextures.end(); ++it) {
//        SafeDelete(*it)
//    }
//
//        mTextures.clear()
//    }
//
//    override fun getClearValues(outCol: Vec4): Float {
//        outCol.put(0f, 0f, 0.1f, 1f)
//        return 1f
//    }
//
//    override fun init(): Boolean {
//
//        genUnitQuad()
//        transforms.resize(kObjectCount)
//
//        return loadTextures()
//    }
//    virtual bool Init() override
//    virtual void Render() override
//    virtual void Shutdown() override
//    inline virtual std::string GetName() override { return "TexturedQuadsProblem"; }
//
//    virtual bool SetSolution(Solution* _solution) override
//
//    struct Vertex
//            {
//                Vec3 pos
//                Vec2 tex
//            }
//
//    typedef uint16_t Index
//
//
//    protected:
//    void Update()
//
//    unsigned int mIteration
//
//    fun genUnitQuad() {
//
//        // Buffers
//        const Vertex vertices[] =
//                {
//                    { -0.5, -0.5f,  0.0f, 0.0f, 0.0f, },
//                    {  0.5, -0.5f,  0.0f, 0.0f, 1.0f, },
//                    {  0.5,  0.5f,  0.0f, 1.0f, 0.0f, },
//                    { -0.5,  0.5f,  0.0f, 1.0f, 1.0f  },
//                };
//
//        for (int i = 0; i < ArraySize(vertices); ++i) {
//            VertexA vert = vertices[i];
//            mVertices.push_back(vert);
//        }
//
//        const uint16_t indices[] =
//                {
//                    0, 1, 2, 0, 2, 3,
//                };
//
//        for (int i = 0; i < ArraySize(indices); ++i) {
//            mIndices.push_back(indices[i]);
//        }
//    }
//    bool loadTextures()
//}