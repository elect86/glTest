package main.apiTest

import glm_.func.cos
import glm_.func.sin
import glm_.mat4x4.Mat4
import kool.*
import java.nio.ByteBuffer
import java.nio.FloatBuffer

fun Mat4Buffer(size: Int) = Mat4Buffer(FloatBuffer(size * Mat4.length))

fun Mat4Buffer(data: ByteBuffer) = Mat4Buffer(data.asFloatBuffer())

inline class Mat4Buffer(val data: FloatBuffer) : Iterable<Mat4> {

    val indices get() = 0 until size

//    operator fun get(index: Int): Mat4 = Mat4(data, index * Mat4.length) TODO
    operator fun get(index: Int): Mat4 = Mat4 { i -> data[index * Mat4.length + i] }

    override fun iterator(): Iterator<Mat4> = Mat4BufferIterator()

    inner class Mat4BufferIterator : Iterator<Mat4> {

        private var pos = data.pos / Mat4.length

//        override fun next() = Mat4(data, pos).also { pos += Mat4.length } TODO
        override fun next() = Mat4 { _ -> data[pos++] }
        override fun hasNext() = pos < data.cap
    }

    val adr get() = data.adr

    fun adr(index: Int) = adr + index * Mat4.size

    val size get() = data.cap / Mat4.length

    fun matrixRotationZ(i: Int, angle: Float) {
        val s = angle.sin
        val c = angle.cos

        var ofs = i * Mat4.length
        data
                .put(ofs++, c).put(ofs++, -s).put(ofs++, 0f).put(ofs++, 0f)
                .put(ofs++, s).put(ofs++, +c).put(ofs++, 0f).put(ofs++, 0f)
                .put(ofs++, 0f).put(ofs++, 0f).put(ofs++, 1f).put(ofs++, 0f)
                .put(ofs++, 0f).put(ofs++, 0f).put(ofs++, 0f).put(ofs, 1f)
//            m.put(
//                    c, -s, 0f, 0f,
//                    s, +c, 0f, 0f,
//                    0f, 0f, 1f, 0f,
//                    0f, 0f, 0f, 1f)
    }
}