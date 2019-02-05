package main.glTest.solutions

import gli_.Texture
import gln.program.GlslProgram
import kool.cap
import main.glOverhead.caps
import main.glTest.Mat4Buffer
import main.glTest.problems.VertexBufferB
import org.lwjgl.system.MemoryUtil.NULL
import java.nio.ShortBuffer

abstract class TexturedQuadsSolution : Solution() {

    var indexCount = 0
    var textureCount = 0
    var program = 0

    open fun init(vertices: VertexBufferB, indices: ShortBuffer, textures: Array<Texture>): Boolean {

        indexCount = indices.cap
        textureCount = textures.size

        return true
    }

    abstract fun render(transforms: Mat4Buffer)

    override val problemName get() = "TexturedQuadsProblem"

    val fragment = "shader"
}

class TexturedQuadsGLBindless : TexturedQuadsSolution() {

    override fun init(vertices: VertexBufferB, indices: ShortBuffer, textures: Array<Texture>): Boolean {

        if (!super.init(vertices, indices, textures)) return false

        // Prerequisites
        if (caps.glGetTextureHandleARB == NULL) {
            System.err.println("Unable to initialize solution '$name', requires support for bindless textures (not present).")
            return false
        }

        // Program
        program = GlslProgram.fromRoot("shaders/textured", "bindless", fragment).name

        if (program == 0) {
            System.err.println("Unable to initialize solution '$name', shader compilation/linking failed.")
            return false
        }

        // Textures
        for (auto it = _textures.begin(); it != _textures.end(); ++it) {
            GLuint tex = NewTex2DFromDetails(*(*it))
            if (!tex) {
                console::warn("Unable to initialize solution '%s', texture creation failed.", GetName().c_str())
                return false
            }

            // Needs to be freed later.
            mTextures.push_back(tex)

            GLuint64 texHandle = glGetTextureHandleARB(tex)
            if (texHandle == 0) {
                console::warn("Unable to initialize solution '%s', couldn't get texture handle.", GetName().c_str())
            }
            mTexHandles.push_back(texHandle)
        }

        // Buffers
        glGenVertexArrays(1, &mVertexArray)
        glBindVertexArray(mVertexArray)

        mVertexBuffer = NewBufferFromVector(GL_ARRAY_BUFFER, _vertices, GL_STATIC_DRAW)
        mIndexBuffer = NewBufferFromVector(GL_ELEMENT_ARRAY_BUFFER, _indices, GL_STATIC_DRAW)

        glGenBuffers(1, &mTransformBuffer)
        glBindBufferBase(GL_SHADER_STORAGE_BUFFER, 0, mTransformBuffer)

        glGenVertexArrays(1, &mVAO)
        glBindVertexArray(mVAO)

        return glGetError() == GL_NO_ERROR
    }

    virtual bool Init(const std::vector<TexturedQuadsProblem::Vertex>& _vertices,
            const std::vector<TexturedQuadsProblem::Index>& _indices,
            const std::vector<TextureDetails*>& _textures,
    size_t _objectCount)

    virtual void Render(const std::vector<Matrix>& _transforms)
    virtual void Shutdown()

    virtual std::string GetName() const { return "GLBindless"; }
    virtual bool SupportsApi(EGfxApi _api) const override { return IsOpenGL(_api); }

    private:
    GLuint mIndexBuffer
    GLuint mVertexBuffer
    GLuint mVertexArray
    GLuint mProgram
    GLuint mTransformBuffer
    GLuint mVAO

    struct UniformLocations {
        GLuint ViewProjection
        GLuint DrawID
        GLuint gTex
        UniformLocations { memset(this, 0, sizeof(*this)); }
    } mUniformLocation

    std::vector<GLuint> mTextures
    std::vector<GLuint64> mTexHandles
}