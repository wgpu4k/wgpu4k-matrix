@file:Suppress("NOTHING_TO_INLINE")

package io.github.natanfudge.wgpu4k.matrix

import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt
import kotlin.math.tan
import kotlin.math.abs

/**
 * Represents a 4x4 matrix stored in a 16-element FloatArray.
 * The elements are stored in column-major order.
 *
 * The layout is:
 * 0  4  8   12
 * 1  5  9   13
 * 2  6  10  14
 * 3  7  11  15
 */
class Mat4 private constructor(val array: FloatArray) {

    inline val m00 get() = this[0]
    inline val m01 get() = this[4]
    inline val m02 get() = this[8]
    inline val m03 get() = this[12]
    inline val m10 get() = this[1]
    inline val m11 get() = this[5]
    inline val m12 get() = this[9]
    inline val m13 get() = this[13]
    inline val m20 get() = this[2]
    inline val m21 get() = this[6]
    inline val m22 get() = this[10]
    inline val m23 get() = this[14]
    inline val m30 get() = this[3]
    inline val m31 get() = this[7]
    inline val m32 get() = this[11]
    inline val m33 get() = this[15]

    init {
        if (array.size != 16) {
            throw IllegalArgumentException("Mat4 requires a 16-element FloatArray for storage.")
        }
    }

    inline operator fun get(index: Int): Float {
        return this.array[index]
    }

    inline operator fun set(index: Int, value: Float) {
         this.array[index] = value
    }

    /**
     * Creates a new Mat4 with the given values ([v0] to [v15]) in column-major order.
     */
    constructor(
        v0: Float = 0f, v1: Float = 0f, v2: Float = 0f, v3: Float = 0f,
        v4: Float = 0f, v5: Float = 0f, v6: Float = 0f, v7: Float = 0f,
        v8: Float = 0f, v9: Float = 0f, v10: Float = 0f, v11: Float = 0f,
        v12: Float = 0f, v13: Float = 0f, v14: Float = 0f, v15: Float = 0f
    ) : this(FloatArray(16).apply {
        this[0] = v0; this[1] = v1; this[2] = v2; this[3] = v3
        this[4] = v4; this[5] = v5; this[6] = v6; this[7] = v7
        this[8] = v8; this[9] = v9; this[10] = v10; this[11] = v11
        this[12] = v12; this[13] = v13; this[14] = v14; this[15] = v15
    })

    override fun toString(): String {
        return """
            [$m00,$m01,$m02,$m03]
            [$m10,$m11,$m12,$m13]
            [$m20,$m21,$m22,$m23]
            [$m30,$m31,$m32,$m33]
        """.trimIndent()
    }

    companion object {
        /**
         * Creates a Mat4 from the given [values].
         * You should generally not use this constructor as it assumes the array is already in the correct format.
         */
        operator fun invoke(vararg values: Float) = Mat4(floatArrayOf(*values))

        /**
         * Creates a Mat4 from a 16-element FloatArray [values].
         * Assumes the array is already in the correct internal format.
         */
        fun fromFloatArray(values: FloatArray): Mat4 {
            if (values.size != 16) {
                throw IllegalArgumentException("Mat4.fromFloatArray requires a 16-element FloatArray.")
            }
            return Mat4(values.copyOf()) // Create a copy to ensure internal state is not modified externally
        }

        /**
         * Creates a new identity Mat4.
         */
        fun identity(dst: Mat4? = null): Mat4 {
            val newDst = dst ?: Mat4()
            return newDst.apply {
                array[0] = 1f; array[1] = 0f; array[2] = 0f; array[3] = 0f
                array[4] = 0f; array[5] = 1f; array[6] = 0f; array[7] = 0f
                array[8] = 0f; array[9] = 0f; array[10] = 1f; array[11] = 0f
                array[12] = 0f; array[13] = 0f; array[14] = 0f; array[15] = 1f
            }
        }

        /**
         * Creates a Mat4 from [m3].
         */
        fun fromMat3(m3: Mat3, dst: Mat4? = null): Mat4 {
            val newDst = dst ?: Mat4()
            return newDst.apply {
                array[0] = m3[0]; array[1] = m3[1]; array[2] = m3[2]; array[3] = 0f
                array[4] = m3[4]; array[5] = m3[5]; array[6] = m3[6]; array[7] = 0f
                array[8] = m3[8]; array[9] = m3[9]; array[10] = m3[10]; array[11] = 0f
                array[12] = 0f; array[13] = 0f; array[14] = 0f; array[15] = 1f
            }
        }

        /**
         * Creates a Mat4 rotation matrix from [q].
         */
        fun fromQuat(q: Quat, dst: Mat4? = null): Mat4 {
            val newDst = dst ?: Mat4()

            val x = q.x; val y = q.y; val z = q.z; val w = q.w
            val x2 = x + x; val y2 = y + y; val z2 = z + z

            val xx = x * x2
            val yx = y * x2
            val yy = y * y2
            val zx = z * x2
            val zy = z * y2
            val zz = z * z2
            val wx = w * x2
            val wy = w * y2
            val wz = w * z2

            return newDst.apply {
                array[0] = 1f - yy.toFloat() - zz.toFloat(); array[1] = yx.toFloat() + wz.toFloat(); array[2] = zx.toFloat() - wy.toFloat(); array[3] = 0f
                array[4] = yx.toFloat() - wz.toFloat(); array[5] = 1f - xx.toFloat() - zz.toFloat(); array[6] = zy.toFloat() + wx.toFloat(); array[7] = 0f
                array[8] = zx.toFloat() + wy.toFloat(); array[9] = zy.toFloat() - wx.toFloat(); array[10] = 1f - xx.toFloat() - yy.toFloat(); array[11] = 0f
                array[12] = 0f; array[13] = 0f; array[14] = 0f; array[15] = 1f
            }
        }

        /**
         * Creates a 4-by-4 matrix which translates by the given vector [v].
         */
        fun translation(v: Vec3, dst: Mat4? = null): Mat4 {
            val newDst = dst ?: Mat4()

            return newDst.apply {
                array[0] = 1f; array[1] = 0f; array[2] = 0f; array[3] = 0f
                array[4] = 0f; array[5] = 1f; array[6] = 0f; array[7] = 0f
                array[8] = 0f; array[9] = 0f; array[10] = 1f; array[11] = 0f
                array[12] = v.x; array[13] = v.y; array[14] = v.z; array[15] = 1f
            }
        }

        /**
         * Creates a 4-by-4 matrix which rotates around the x-axis by the given [angleInRadians].
         */
        fun rotationX(angleInRadians: Float, dst: Mat4? = null): Mat4 {
            val newDst = dst ?: Mat4()

            val c = cos(angleInRadians)
            val s = sin(angleInRadians)

            return newDst.apply {
                array[0] = 1f; array[1] = 0f; array[2] = 0f; array[3] = 0f
                array[4] = 0f; array[5] = c; array[6] = s; array[7] = 0f
                array[8] = 0f; array[9] = -s; array[10] = c; array[11] = 0f
                array[12] = 0f; array[13] = 0f; array[14] = 0f; array[15] = 1f
            }
        }

        /**
         * Creates a 4-by-4 matrix which rotates around the y-axis by the given [angleInRadians].
         */
        fun rotationY(angleInRadians: Float, dst: Mat4? = null): Mat4 {
            val newDst = dst ?: Mat4()

            val c = cos(angleInRadians)
            val s = sin(angleInRadians)

            return newDst.apply {
                array[0] = c; array[1] = 0f; array[2] = -s; array[3] = 0f
                array[4] = 0f; array[5] = 1f; array[6] = 0f; array[7] = 0f
                array[8] = s; array[9] = 0f; array[10] = c; array[11] = 0f
                array[12] = 0f; array[13] = 0f; array[14] = 0f; array[15] = 1f
            }
        }

        /**
         * Creates a 4-by-4 matrix which rotates around the z-axis by the given [angleInRadians].
         */
        fun rotationZ(angleInRadians: Float, dst: Mat4? = null): Mat4 {
            val newDst = dst ?: Mat4()

            val c = cos(angleInRadians)
            val s = sin(angleInRadians)

            return newDst.apply {
                array[0] = c; array[1] = s; array[2] = 0f; array[3] = 0f
                array[4] = -s; array[5] = c; array[6] = 0f; array[7] = 0f
                array[8] = 0f; array[9] = 0f; array[10] = 1f; array[11] = 0f
                array[12] = 0f; array[13] = 0f; array[14] = 0f; array[15] = 1f
            }
        }

        /**
         * Creates a 4-by-4 matrix which rotates around the given [axis] by the given [angleInRadians].
         */
        fun axisRotation(axis: Vec3, angleInRadians: Float, dst: Mat4? = null): Mat4 {
            val newDst = dst ?: Mat4()

            let {
                var x = axis.x
                var y = axis.y
                var z = axis.z

                val n = sqrt(x * x + y * y + z * z)
                if (n < EPSILON) {
                    return identity(newDst)
                }

                x /= n
                y /= n
                z /= n

                val xx = x * x
                val yy = y * y
                val zz = z * z
                val c = cos(angleInRadians)
                val s = sin(angleInRadians)
                val oneMinusCosine = 1f - c

                newDst.array[0] = xx + (1f - xx) * c
                newDst.array[1] = x * y * oneMinusCosine + z * s
                newDst.array[2] = x * z * oneMinusCosine - y * s
                newDst.array[3] = 0f

                newDst.array[4] = x * y * oneMinusCosine - z * s
                newDst.array[5] = yy + (1f - yy) * c
                newDst.array[6] = y * z * oneMinusCosine + x * s
                newDst.array[7] = 0f

                newDst.array[8] = x * z * oneMinusCosine + y * s
                newDst.array[9] = y * z * oneMinusCosine - x * s
                newDst.array[10] = zz + (1f - zz) * c
                newDst.array[11] = 0f

                newDst.array[12] = 0f
                newDst.array[13] = 0f
                newDst.array[14] = 0f
                newDst.array[15] = 1f

                return newDst
            }
        }

        /**
         * Creates a 4-by-4 matrix which scales in each dimension by the components of [v].
         */
        fun scaling(v: Vec3, dst: Mat4? = null): Mat4 {
            val newDst = dst ?: Mat4()

            return newDst.apply {
                array[0] = v.x; array[1] = 0f; array[2] = 0f; array[3] = 0f
                array[4] = 0f; array[5] = v.y; array[6] = 0f; array[7] = 0f
                array[8] = 0f; array[9] = 0f; array[10] = v.z; array[11] = 0f
                array[12] = 0f; array[13] = 0f; array[14] = 0f; array[15] = 1f
            }
        }

        /**
         * Creates a 4-by-4 matrix which scales uniformly in each dimension by [s].
         */
        fun uniformScaling(s: Float, dst: Mat4? = null): Mat4 {
            val newDst = dst ?: Mat4()

            return newDst.apply {
                array[0] = s; array[1] = 0f; array[2] = 0f; array[3] = 0f
                array[4] = 0f; array[5] = s; array[6] = 0f; array[7] = 0f
                array[8] = 0f; array[9] = 0f; array[10] = s; array[11] = 0f
                array[12] = 0f; array[13] = 0f; array[14] = 0f; array[15] = 1f
            }
        }

        /**
         * Creates a 4-by-4 perspective projection matrix.
         * @param fieldOfViewYInRadians The field of view in the y direction (in radians).
         * @param aspect The aspect ratio (width / height).
         * @param zNear The distance to the near clipping plane.
         * @param zFar The distance to the far clipping plane.
         */
        fun perspective(fieldOfViewYInRadians: Float, aspect: Float, zNear: Float, zFar: Float, dst: Mat4? = null): Mat4 {
            val newDst = dst ?: Mat4()

            val f = 1.0f / tan(fieldOfViewYInRadians / 2)
            val rangeInv = 1.0f / (zNear - zFar)

            return newDst.apply {
                array[0] = f / aspect; array[1] = 0f; array[2] = 0f; array[3] = 0f
                array[4] = 0f; array[5] = f; array[6] = 0f; array[7] = 0f
                array[8] = 0f; array[9] = 0f; array[10] = (zNear + zFar) * rangeInv; array[11] = -1f
                array[12] = 0f; array[13] = 0f; array[14] = zNear * zFar * rangeInv * 2; array[15] = 0f
            }
        }

        /**
         * Creates a 4-by-4 orthographic projection matrix defined by [left], [right], [bottom], [top], [near], and [far] clipping planes.
         */
        fun ortho(left: Float, right: Float, bottom: Float, top: Float, near: Float, far: Float, dst: Mat4? = null): Mat4 {
            val newDst = dst ?: Mat4()

            val width = right - left
            val height = top - bottom
            val depth = far - near

            return newDst.apply {
                array[0] = 2 / width; array[1] = 0f; array[2] = 0f; array[3] = 0f
                array[4] = 0f; array[5] = 2 / height; array[6] = 0f; array[7] = 0f
                array[8] = 0f; array[9] = 0f; array[10] = -2 / depth; array[11] = 0f
                array[12] = -(left + right) / width; array[13] = -(top + bottom) / height; array[14] = -(far + near) / depth; array[15] = 1f
            }
        }

        /**
         * Creates a 4-by-4 frustum matrix defined by [left], [right], [bottom], [top], [near], and [far] clipping planes.
         */
        fun frustum(left: Float, right: Float, bottom: Float, top: Float, near: Float, far: Float, dst: Mat4? = null): Mat4 {
            val newDst = dst ?: Mat4()

            val dx = right - left
            val dy = top - bottom
            val dz = far - near

            return newDst.apply {
                array[0] = 2 * near / dx; array[1] = 0f; array[2] = 0f; array[3] = 0f
                array[4] = 0f; array[5] = 2 * near / dy; array[6] = 0f; array[7] = 0f
                array[8] = (left + right) / dx; array[9] = (top + bottom) / dy; array[10] = -(far + near) / dz; array[11] = -1f
                array[12] = 0f; array[13] = 0f; array[14] = -2 * near * far / dz; array[15] = 0f
            }
        }

        /**
         * Creates a 4-by-4 look-at matrix.
         * @param eye The position of the eye.
         * @param target The position to look at.
         * @param up The up vector.
         */
        fun lookAt(eye: Vec3, target: Vec3, up: Vec3, dst: Mat4? = null): Mat4 {
            val newDst = dst ?: Mat4()

            val eyex = eye.x
            val eyey = eye.y
            val eyez = eye.z
            val upx = up.x
            val upy = up.y
            val upz = up.z
            val targetx = target.x
            val targety = target.y
            val targetz = target.z

            val z0 = eyex - targetx
            val z1 = eyey - targety
            val z2 = eyez - targetz

            // Normalize z
            var len = z0 * z0 + z1 * z1 + z2 * z2
            var nz0 = z0
            var nz1 = z1
            var nz2 = z2
            if (len > 0) {
                len = 1 / sqrt(len)
                nz0 *= len
                nz1 *= len
                nz2 *= len
            }

            // Cross product of up and z to get x
            var x0 = upy * nz2 - upz * nz1
            var x1 = upz * nz0 - upx * nz2
            var x2 = upx * nz1 - upy * nz0

            // Normalize x
            len = x0 * x0 + x1 * x1 + x2 * x2
            if (len > 0) {
                len = 1 / sqrt(len)
                x0 *= len
                x1 *= len
                x2 *= len
            }

            // Cross product of z and x to get y
            val y0 = nz1 * x2 - nz2 * x1
            val y1 = nz2 * x0 - nz0 * x2
            val y2 = nz0 * x1 - nz1 * x0

            return newDst.apply {
                array[0] = x0; array[1] = y0; array[2] = nz0; array[3] = 0f
                array[4] = x1; array[5] = y1; array[6] = nz1; array[7] = 0f
                array[8] = x2; array[9] = y2; array[10] = nz2; array[11] = 0f
                array[12] = -(x0 * eyex + x1 * eyey + x2 * eyez)
                array[13] = -(y0 * eyex + y1 * eyey + y2 * eyez)
                array[14] = -(nz0 * eyex + nz1 * eyey + nz2 * eyez)
                array[15] = 1f
            }
        }
    }

    /**
     * Gets a copy of the internal FloatArray representation of the matrix.
     * Modifying the returned array will not affect the original matrix.
     */
    fun toFloatArray(): FloatArray = array.copyOf() // Return a copy for safety

    /**
     * Sets the values of `this` from [v0] to [v15].
     */
    fun set(
        v0: Float, v1: Float, v2: Float, v3: Float,
        v4: Float, v5: Float, v6: Float, v7: Float,
        v8: Float, v9: Float, v10: Float, v11: Float,
        v12: Float, v13: Float, v14: Float, v15: Float
    ): Mat4 = this.apply {
        array[0] = v0; array[1] = v1; array[2] = v2; array[3] = v3
        array[4] = v4; array[5] = v5; array[6] = v6; array[7] = v7
        array[8] = v8; array[9] = v9; array[10] = v10; array[11] = v11
        array[12] = v12; array[13] = v13; array[14] = v14; array[15] = v15
    }

    /**
     * Negates `this`.
     */
    fun negate(dst: Mat4? = null): Mat4 {
        val newDst = dst ?: Mat4()

        return newDst.apply {
            array[0] = -this@Mat4.array[0]; array[1] = -this@Mat4.array[1]; array[2] = -this@Mat4.array[2]; array[3] = -this@Mat4.array[3]
            array[4] = -this@Mat4.array[4]; array[5] = -this@Mat4.array[5]; array[6] = -this@Mat4.array[6]; array[7] = -this@Mat4.array[7]
            array[8] = -this@Mat4.array[8]; array[9] = -this@Mat4.array[9]; array[10] = -this@Mat4.array[10]; array[11] = -this@Mat4.array[11]
            array[12] = -this@Mat4.array[12]; array[13] = -this@Mat4.array[13]; array[14] = -this@Mat4.array[14]; array[15] = -this@Mat4.array[15]
        }
    }

    /**
     * Multiplies `this` by the scalar [s].
     */
    fun multiplyScalar(s: Float, dst: Mat4? = null): Mat4 {
        val newDst = dst ?: Mat4()

        return newDst.apply {
            array[0] = this@Mat4.array[0] * s; array[1] = this@Mat4.array[1] * s; array[2] = this@Mat4.array[2] * s; array[3] = this@Mat4.array[3] * s
            array[4] = this@Mat4.array[4] * s; array[5] = this@Mat4.array[5] * s; array[6] = this@Mat4.array[6] * s; array[7] = this@Mat4.array[7] * s
            array[8] = this@Mat4.array[8] * s; array[9] = this@Mat4.array[9] * s; array[10] = this@Mat4.array[10] * s; array[11] = this@Mat4.array[11] * s
            array[12] = this@Mat4.array[12] * s; array[13] = this@Mat4.array[13] * s; array[14] = this@Mat4.array[14] * s; array[15] = this@Mat4.array[15] * s
        }
    }

    /**
     * Adds [other] to `this`.
     */
    fun add(other: Mat4, dst: Mat4? = null): Mat4 {
        val newDst = dst ?: Mat4()

        return newDst.apply {
            array[0] = this@Mat4.array[0] + other.array[0]; array[1] = this@Mat4.array[1] + other.array[1]
            array[2] = this@Mat4.array[2] + other.array[2]; array[3] = this@Mat4.array[3] + other.array[3]
            array[4] = this@Mat4.array[4] + other.array[4]; array[5] = this@Mat4.array[5] + other.array[5]
            array[6] = this@Mat4.array[6] + other.array[6]; array[7] = this@Mat4.array[7] + other.array[7]
            array[8] = this@Mat4.array[8] + other.array[8]; array[9] = this@Mat4.array[9] + other.array[9]
            array[10] = this@Mat4.array[10] + other.array[10]; array[11] = this@Mat4.array[11] + other.array[11]
            array[12] = this@Mat4.array[12] + other.array[12]; array[13] = this@Mat4.array[13] + other.array[13]
            array[14] = this@Mat4.array[14] + other.array[14]; array[15] = this@Mat4.array[15] + other.array[15]
        }
    }

    /**
     * Copies `this`.
     */
    fun copy(dst: Mat4? = null): Mat4 {
        val newDst = dst ?: Mat4()
        this.array.copyInto(newDst.array)
        return newDst
    }

    /**
     * Copies `this` (alias for [copy]).
     */
    fun clone(dst: Mat4? = null): Mat4 = copy(dst)

    /**
     * Multiplies `this` by [other] (`this` * [other]).
     */
    fun multiply(other: Mat4, dst: Mat4? = null): Mat4 {
        val newDst = dst ?: Mat4()

        val a00 = array[0]
        val a01 = array[1]
        val a02 = array[2]
        val a03 = array[3]
        val a10 = array[4]
        val a11 = array[5]
        val a12 = array[6]
        val a13 = array[7]
        val a20 = array[8]
        val a21 = array[9]
        val a22 = array[10]
        val a23 = array[11]
        val a30 = array[12]
        val a31 = array[13]
        val a32 = array[14]
        val a33 = array[15]

        val b00 = other.array[0]
        val b01 = other.array[1]
        val b02 = other.array[2]
        val b03 = other.array[3]
        val b10 = other.array[4]
        val b11 = other.array[5]
        val b12 = other.array[6]
        val b13 = other.array[7]
        val b20 = other.array[8]
        val b21 = other.array[9]
        val b22 = other.array[10]
        val b23 = other.array[11]
        val b30 = other.array[12]
        val b31 = other.array[13]
        val b32 = other.array[14]
        val b33 = other.array[15]

        return newDst.apply {
            array[0] = a00 * b00 + a10 * b01 + a20 * b02 + a30 * b03
            array[1] = a01 * b00 + a11 * b01 + a21 * b02 + a31 * b03
            array[2] = a02 * b00 + a12 * b01 + a22 * b02 + a32 * b03
            array[3] = a03 * b00 + a13 * b01 + a23 * b02 + a33 * b03
            array[4] = a00 * b10 + a10 * b11 + a20 * b12 + a30 * b13
            array[5] = a01 * b10 + a11 * b11 + a21 * b12 + a31 * b13
            array[6] = a02 * b10 + a12 * b11 + a22 * b12 + a32 * b13
            array[7] = a03 * b10 + a13 * b11 + a23 * b12 + a33 * b13
            array[8] = a00 * b20 + a10 * b21 + a20 * b22 + a30 * b23
            array[9] = a01 * b20 + a11 * b21 + a21 * b22 + a31 * b23
            array[10] = a02 * b20 + a12 * b21 + a22 * b22 + a32 * b23
            array[11] = a03 * b20 + a13 * b21 + a23 * b22 + a33 * b23
            array[12] = a00 * b30 + a10 * b31 + a20 * b32 + a30 * b33
            array[13] = a01 * b30 + a11 * b31 + a21 * b32 + a31 * b33
            array[14] = a02 * b30 + a12 * b31 + a22 * b32 + a32 * b33
            array[15] = a03 * b30 + a13 * b31 + a23 * b32 + a33 * b33
        }
    }

    /**
     * Multiplies `this` by [other] (`this` * [other]) (alias for [multiply]).
     */
    fun mul(other: Mat4, dst: Mat4? = null): Mat4 = multiply(other, dst)

    /**
     * Computes the transpose of `this`.
     */
    fun transpose(dst: Mat4? = null): Mat4 {
        val newDst = dst ?: Mat4()

        if (newDst === this) {
            // Perform in-place transpose
            var t: Float

            t = array[1]; array[1] = array[4]; array[4] = t
            t = array[2]; array[2] = array[8]; array[8] = t
            t = array[3]; array[3] = array[12]; array[12] = t
            t = array[6]; array[6] = array[9]; array[9] = t
            t = array[7]; array[7] = array[13]; array[13] = t
            t = array[11]; array[11] = array[14]; array[14] = t

            return newDst
        }

        val m00 = array[0]
        val m01 = array[1]
        val m02 = array[2]
        val m03 = array[3]
        val m10 = array[4]
        val m11 = array[5]
        val m12 = array[6]
        val m13 = array[7]
        val m20 = array[8]
        val m21 = array[9]
        val m22 = array[10]
        val m23 = array[11]
        val m30 = array[12]
        val m31 = array[13]
        val m32 = array[14]
        val m33 = array[15]

        return newDst.apply {
            array[0] = m00; array[1] = m10; array[2] = m20; array[3] = m30
            array[4] = m01; array[5] = m11; array[6] = m21; array[7] = m31
            array[8] = m02; array[9] = m12; array[10] = m22; array[11] = m32
            array[12] = m03; array[13] = m13; array[14] = m23; array[15] = m33
        }
    }

    /**
     * Computes the inverse of `this`.
     */
    fun inverse(dst: Mat4? = null): Mat4 {
        val newDst = dst ?: Mat4()

        val m00 = array[0]
        val m01 = array[1]
        val m02 = array[2]
        val m03 = array[3]
        val m10 = array[4]
        val m11 = array[5]
        val m12 = array[6]
        val m13 = array[7]
        val m20 = array[8]
        val m21 = array[9]
        val m22 = array[10]
        val m23 = array[11]
        val m30 = array[12]
        val m31 = array[13]
        val m32 = array[14]
        val m33 = array[15]

        val tmp0 = m22 * m33
        val tmp1 = m32 * m23
        val tmp2 = m12 * m33
        val tmp3 = m32 * m13
        val tmp4 = m12 * m23
        val tmp5 = m22 * m13
        val tmp6 = m02 * m33
        val tmp7 = m32 * m03
        val tmp8 = m02 * m23
        val tmp9 = m22 * m03
        val tmp10 = m02 * m13
        val tmp11 = m12 * m03
        val tmp12 = m20 * m31
        val tmp13 = m30 * m21
        val tmp14 = m10 * m31
        val tmp15 = m30 * m11
        val tmp16 = m10 * m21
        val tmp17 = m20 * m11
        val tmp18 = m00 * m31
        val tmp19 = m30 * m01
        val tmp20 = m00 * m21
        val tmp21 = m20 * m01
        val tmp22 = m00 * m11
        val tmp23 = m10 * m01

        val t0 = (tmp0 * m11 + tmp3 * m21 + tmp4 * m31) -
                (tmp1 * m11 + tmp2 * m21 + tmp5 * m31)
        val t1 = (tmp1 * m01 + tmp6 * m21 + tmp9 * m31) -
                (tmp0 * m01 + tmp7 * m21 + tmp8 * m31)
        val t2 = (tmp2 * m01 + tmp7 * m11 + tmp10 * m31) -
                (tmp3 * m01 + tmp6 * m11 + tmp11 * m31)
        val t3 = (tmp5 * m01 + tmp8 * m11 + tmp11 * m21) -
                (tmp4 * m01 + tmp9 * m11 + tmp10 * m21)

        val d = 1.0f / (m00 * t0 + m10 * t1 + m20 * t2 + m30 * t3)

        return newDst.apply {
            array[0] = t0 * d
            array[1] = t1 * d
            array[2] = t2 * d
            array[3] = t3 * d
            array[4] = ((tmp1 * m10 + tmp2 * m20 + tmp5 * m30) -
                    (tmp0 * m10 + tmp3 * m20 + tmp4 * m30)) * d
            array[5] = ((tmp0 * m00 + tmp7 * m20 + tmp8 * m30) -
                    (tmp1 * m00 + tmp6 * m20 + tmp9 * m30)) * d
            array[6] = ((tmp3 * m00 + tmp6 * m10 + tmp11 * m30) -
                    (tmp2 * m00 + tmp7 * m10 + tmp10 * m30)) * d
            array[7] = ((tmp4 * m00 + tmp9 * m10 + tmp10 * m20) -
                    (tmp5 * m00 + tmp8 * m10 + tmp11 * m20)) * d
            array[8] = ((tmp12 * m13 + tmp15 * m23 + tmp16 * m33) -
                    (tmp13 * m13 + tmp14 * m23 + tmp17 * m33)) * d
            array[9] = ((tmp13 * m03 + tmp18 * m23 + tmp21 * m33) -
                    (tmp12 * m03 + tmp19 * m23 + tmp20 * m33)) * d
            array[10] = ((tmp14 * m03 + tmp19 * m13 + tmp22 * m33) -
                    (tmp15 * m03 + tmp18 * m13 + tmp23 * m33)) * d
            array[11] = ((tmp17 * m03 + tmp20 * m13 + tmp23 * m23) -
                    (tmp16 * m03 + tmp21 * m13 + tmp22 * m23)) * d
            array[12] = ((tmp14 * m22 + tmp17 * m32 + tmp13 * m12) -
                    (tmp16 * m32 + tmp12 * m12 + tmp15 * m22)) * d
            array[13] = ((tmp20 * m32 + tmp12 * m02 + tmp19 * m22) -
                    (tmp18 * m22 + tmp21 * m32 + tmp13 * m02)) * d
            array[14] = ((tmp18 * m12 + tmp23 * m32 + tmp15 * m02) -
                    (tmp22 * m32 + tmp14 * m02 + tmp19 * m12)) * d
            array[15] = ((tmp22 * m22 + tmp16 * m02 + tmp21 * m12) -
                    (tmp20 * m12 + tmp23 * m22 + tmp17 * m02)) * d
        }
    }

    /**
     * Computes the inverse of `this` (alias for [inverse]).
     */
    fun invert(dst: Mat4? = null): Mat4 = inverse(dst)

    /**
     * Computes the determinant of `this`.
     */
    fun determinant(): Float {
        val m00 = array[0]
        val m01 = array[1]
        val m02 = array[2]
        val m03 = array[3]
        val m10 = array[4]
        val m11 = array[5]
        val m12 = array[6]
        val m13 = array[7]
        val m20 = array[8]
        val m21 = array[9]
        val m22 = array[10]
        val m23 = array[11]
        val m30 = array[12]
        val m31 = array[13]
        val m32 = array[14]
        val m33 = array[15]

        val tmp0 = m22 * m33
        val tmp1 = m32 * m23
        val tmp2 = m12 * m33
        val tmp3 = m32 * m13
        val tmp4 = m12 * m23
        val tmp5 = m22 * m13
        val tmp6 = m02 * m33
        val tmp7 = m32 * m03
        val tmp8 = m02 * m23
        val tmp9 = m22 * m03
        val tmp10 = m02 * m13
        val tmp11 = m12 * m03

        val t0 = (tmp0 * m11 + tmp3 * m21 + tmp4 * m31) -
                (tmp1 * m11 + tmp2 * m21 + tmp5 * m31)
        val t1 = (tmp1 * m01 + tmp6 * m21 + tmp9 * m31) -
                (tmp0 * m01 + tmp7 * m21 + tmp8 * m31)
        val t2 = (tmp2 * m01 + tmp7 * m11 + tmp10 * m31) -
                (tmp3 * m01 + tmp6 * m11 + tmp11 * m31)
        val t3 = (tmp5 * m01 + tmp8 * m11 + tmp11 * m21) -
                (tmp4 * m01 + tmp9 * m11 + tmp10 * m21)

        return m00 * t0 + m10 * t1 + m20 * t2 + m30 * t3
    }

    /**
     * Creates a matrix copy of `this` with the translation component set to [v].
     */
    fun setTranslation(v: Vec3, dst: Mat4? = null): Mat4 {
        val newDst = if (dst === this) this else copy(dst)

        newDst.array[12] = v.x
        newDst.array[13] = v.y
        newDst.array[14] = v.z

        return newDst
    }

    /**
     * Gets the translation component of `this`.
     */
    fun getTranslation(dst: Vec3? = null): Vec3 {
        val newDst = dst ?: Vec3.create()
        newDst.x = array[12]
        newDst.y = array[13]
        newDst.z = array[14]
        return newDst
    }

    /**
     * Gets the specified [axis] (0=x, 1=y, 2=z) of `this` as a Vec3.
     */
    fun getAxis(axis: Int, dst: Vec3? = null): Vec3 {
        val newDst = dst ?: Vec3.create()
        val off = axis * 4
        newDst.x = array[off + 0]
        newDst.y = array[off + 1]
        newDst.z = array[off + 2]
        return newDst
    }

    /**
     * Creates a matrix copy of `this` with the specified [axis] (0=x, 1=y, 2=z) set to [v].
     */
    fun setAxis(v: Vec3, axis: Int, dst: Mat4? = null): Mat4 {
        val newDst = if (dst === this) this else copy(dst)

        val off = axis * 4
        newDst.array[off + 0] = v.x
        newDst.array[off + 1] = v.y
        newDst.array[off + 2] = v.z

        return newDst
    }

    /**
     * Gets the scaling component of `this`.
     */
    fun getScaling(dst: Vec3? = null): Vec3 {
        val newDst = dst ?: Vec3.create()

        val xx = array[0]
        val xy = array[1]
        val xz = array[2]
        val yx = array[4]
        val yy = array[5]
        val yz = array[6]
        val zx = array[8]
        val zy = array[9]
        val zz = array[10]

        newDst.x = sqrt(xx * xx + xy * xy + xz * xz)
        newDst.y = sqrt(yx * yx + yy * yy + yz * yz)
        newDst.z = sqrt(zx * zx + zy * zy + zz * zz)

        return newDst
    }

    /**
     * Translates `this` by [v].
     */
    fun translate(v: Vec3, dst: Mat4? = null): Mat4 {
        val newDst = dst ?: Mat4()

        val v0 = v.x
        val v1 = v.y
        val v2 = v.z

        val m00 = array[0]
        val m01 = array[1]
        val m02 = array[2]
        val m03 = array[3]
        val m10 = array[4]
        val m11 = array[5]
        val m12 = array[6]
        val m13 = array[7]
        val m20 = array[8]
        val m21 = array[9]
        val m22 = array[10]
        val m23 = array[11]

        if (this !== newDst) {
            newDst.array[0] = m00
            newDst.array[1] = m01
            newDst.array[2] = m02
            newDst.array[3] = m03
            newDst.array[4] = m10
            newDst.array[5] = m11
            newDst.array[6] = m12
            newDst.array[7] = m13
            newDst.array[8] = m20
            newDst.array[9] = m21
            newDst.array[10] = m22
            newDst.array[11] = m23
        }

        newDst.array[12] = m00 * v0 + m10 * v1 + m20 * v2 + array[12]
        newDst.array[13] = m01 * v0 + m11 * v1 + m21 * v2 + array[13]
        newDst.array[14] = m02 * v0 + m12 * v1 + m22 * v2 + array[14]
        newDst.array[15] = m03 * v0 + m13 * v1 + m23 * v2 + array[15]

        return newDst
    }

    /**
     * Rotates `this` by [angleInRadians] around the X axis.
     */
    fun rotateX(angleInRadians: Float, dst: Mat4? = null): Mat4 {
        val newDst = dst ?: Mat4()

        val m10 = array[4]
        val m11 = array[5]
        val m12 = array[6]
        val m13 = array[7]
        val m20 = array[8]
        val m21 = array[9]
        val m22 = array[10]
        val m23 = array[11]

        val c = cos(angleInRadians)
        val s = sin(angleInRadians)

        newDst.array[4] = c * m10 + s * m20
        newDst.array[5] = c * m11 + s * m21
        newDst.array[6] = c * m12 + s * m22
        newDst.array[7] = c * m13 + s * m23
        newDst.array[8] = c * m20 - s * m10
        newDst.array[9] = c * m21 - s * m11
        newDst.array[10] = c * m22 - s * m12
        newDst.array[11] = c * m23 - s * m13

        if (this !== newDst) {
            newDst.array[0] = array[0]
            newDst.array[1] = array[1]
            newDst.array[2] = array[2]
            newDst.array[3] = array[3]
            newDst.array[12] = array[12]
            newDst.array[13] = array[13]
            newDst.array[14] = array[14]
            newDst.array[15] = array[15]
        }

        return newDst
    }

    /**
     * Rotates `this` by [angleInRadians] around the Y axis.
     */
    fun rotateY(angleInRadians: Float, dst: Mat4? = null): Mat4 {
        val newDst = dst ?: Mat4()

        val m00 = array[0]
        val m01 = array[1]
        val m02 = array[2]
        val m03 = array[3]
        val m20 = array[8]
        val m21 = array[9]
        val m22 = array[10]
        val m23 = array[11]

        val c = cos(angleInRadians)
        val s = sin(angleInRadians)

        newDst.array[0] = c * m00 - s * m20
        newDst.array[1] = c * m01 - s * m21
        newDst.array[2] = c * m02 - s * m22
        newDst.array[3] = c * m03 - s * m23
        newDst.array[8] = c * m20 + s * m00
        newDst.array[9] = c * m21 + s * m01
        newDst.array[10] = c * m22 + s * m02
        newDst.array[11] = c * m23 + s * m03

        if (this !== newDst) {
            newDst.array[4] = array[4]
            newDst.array[5] = array[5]
            newDst.array[6] = array[6]
            newDst.array[7] = array[7]
            newDst.array[12] = array[12]
            newDst.array[13] = array[13]
            newDst.array[14] = array[14]
            newDst.array[15] = array[15]
        }

        return newDst
    }

    /**
     * Rotates `this` by [angleInRadians] around the Z axis.
     */
    fun rotateZ(angleInRadians: Float, dst: Mat4? = null): Mat4 {
        val newDst = dst ?: Mat4()

        val m00 = array[0]
        val m01 = array[1]
        val m02 = array[2]
        val m03 = array[3]
        val m10 = array[4]
        val m11 = array[5]
        val m12 = array[6]
        val m13 = array[7]

        val c = cos(angleInRadians)
        val s = sin(angleInRadians)

        newDst.array[0] = c * m00 + s * m10
        newDst.array[1] = c * m01 + s * m11
        newDst.array[2] = c * m02 + s * m12
        newDst.array[3] = c * m03 + s * m13
        newDst.array[4] = c * m10 - s * m00
        newDst.array[5] = c * m11 - s * m01
        newDst.array[6] = c * m12 - s * m02
        newDst.array[7] = c * m13 - s * m03

        if (this !== newDst) {
            newDst.array[8] = array[8]
            newDst.array[9] = array[9]
            newDst.array[10] = array[10]
            newDst.array[11] = array[11]
            newDst.array[12] = array[12]
            newDst.array[13] = array[13]
            newDst.array[14] = array[14]
            newDst.array[15] = array[15]
        }

        return newDst
    }

    /**
     * Rotates `this` around the given [axis] by [angleInRadians].
     */
    fun axisRotate(axis: Vec3, angleInRadians: Float, dst: Mat4? = null): Mat4 {
        val newDst = dst ?: Mat4()

        val x = axis.x
        val y = axis.y
        val z = axis.z

        val n = sqrt(x * x + y * y + z * z)
        if (n < EPSILON) {
            return copy(newDst)
        }

        val axisX = x / n
        val axisY = y / n
        val axisZ = z / n

        val m = Mat4.axisRotation(Vec3(axisX, axisY, axisZ), angleInRadians)

        return multiply(m, newDst)
    }

    fun rotate(axis: Vec3, angleInRadians: Float, dst: Mat4? = null) = axisRotate(axis, angleInRadians, dst)

    /**
     * Scales `this` by the components of [v].
     */
    fun scale(v: Vec3, dst: Mat4? = null): Mat4 {
        val newDst = dst ?: Mat4()

        val v0 = v.x
        val v1 = v.y
        val v2 = v.z

        newDst.array[0] = v0 * array[0]
        newDst.array[1] = v0 * array[1]
        newDst.array[2] = v0 * array[2]
        newDst.array[3] = v0 * array[3]
        newDst.array[4] = v1 * array[4]
        newDst.array[5] = v1 * array[5]
        newDst.array[6] = v1 * array[6]
        newDst.array[7] = v1 * array[7]
        newDst.array[8] = v2 * array[8]
        newDst.array[9] = v2 * array[9]
        newDst.array[10] = v2 * array[10]
        newDst.array[11] = v2 * array[11]

        if (this !== newDst) {
            newDst.array[12] = array[12]
            newDst.array[13] = array[13]
            newDst.array[14] = array[14]
            newDst.array[15] = array[15]
        }

        return newDst
    }

    /**
     * Scales `this` uniformly by [s].
     */
    fun uniformScale(s: Float, dst: Mat4? = null): Mat4 {
        val newDst = dst ?: Mat4()

        newDst.array[0] = s * array[0]
        newDst.array[1] = s * array[1]
        newDst.array[2] = s * array[2]
        newDst.array[3] = s * array[3]
        newDst.array[4] = s * array[4]
        newDst.array[5] = s * array[5]
        newDst.array[6] = s * array[6]
        newDst.array[7] = s * array[7]
        newDst.array[8] = s * array[8]
        newDst.array[9] = s * array[9]
        newDst.array[10] = s * array[10]
        newDst.array[11] = s * array[11]

        if (this !== newDst) {
            newDst.array[12] = array[12]
            newDst.array[13] = array[13]
            newDst.array[14] = array[14]
            newDst.array[15] = array[15]
        }

        return newDst
    }

    /**
     * Checks if `this` is approximately equal to [other].
     */
    fun equalsApproximately(other: Mat4): Boolean {
        return abs(array[0] - other.array[0]) < EPSILON &&
                abs(array[1] - other.array[1]) < EPSILON &&
                abs(array[2] - other.array[2]) < EPSILON &&
                abs(array[3] - other.array[3]) < EPSILON &&
                abs(array[4] - other.array[4]) < EPSILON &&
                abs(array[5] - other.array[5]) < EPSILON &&
                abs(array[6] - other.array[6]) < EPSILON &&
                abs(array[7] - other.array[7]) < EPSILON &&
                abs(array[8] - other.array[8]) < EPSILON &&
                abs(array[9] - other.array[9]) < EPSILON &&
                abs(array[10] - other.array[10]) < EPSILON &&
                abs(array[11] - other.array[11]) < EPSILON &&
                abs(array[12] - other.array[12]) < EPSILON &&
                abs(array[13] - other.array[13]) < EPSILON &&
                abs(array[14] - other.array[14]) < EPSILON &&
                abs(array[15] - other.array[15]) < EPSILON
    }

    /**
     * Checks if `this` is exactly equal to [other].
     */
    override fun equals(other: Any?): Boolean {
        return other is Mat4 &&
                array[0] == other.array[0] &&
                array[1] == other.array[1] &&
                array[2] == other.array[2] &&
                array[3] == other.array[3] &&
                array[4] == other.array[4] &&
                array[5] == other.array[5] &&
                array[6] == other.array[6] &&
                array[7] == other.array[7] &&
                array[8] == other.array[8] &&
                array[9] == other.array[9] &&
                array[10] == other.array[10] &&
                array[11] == other.array[11] &&
                array[12] == other.array[12] &&
                array[13] == other.array[13] &&
                array[14] == other.array[14] &&
                array[15] == other.array[15]
    }

    /**
     * Computes the hash code for `this`.
     */
    override fun hashCode(): Int {
        return array.contentHashCode()
    }
}
