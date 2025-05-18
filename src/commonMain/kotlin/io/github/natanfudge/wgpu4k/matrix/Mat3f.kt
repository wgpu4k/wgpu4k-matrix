@file:Suppress("NOTHING_TO_INLINE")

package io.github.natanfudge.wgpu4k.matrix

import io.github.natanfudge.wgpu4k.matrix.Mat3f.Companion.rowMajor
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt


/**
 * Represents a 3x3 matrix stored in a 12-element FloatArray,
 * using a layout similar to a 4x4 matrix for potential compatibility or alignment.
 * The elements are stored in column-major order, but only the first 3 columns
 * and first 3 rows are used for 3x3 operations. The 4th column is ignored
 * or used for padding/homogenization as seen in the create function.
 *
 * The layout is:
 * 0  4  8   (12)
 * 1  5  9   (13)
 * 2  6  10  (14)
 * 3  7  11  (15)
 *
 * Where () are unused for Mat3 but might be present in the 12-element array.
 *
 * Do not depend on the values in the padding cells, as the behavior of those may change in the future.
 */
/*@JvmInline value*/ class Mat3f private constructor(val array: FloatArray) {

    constructor() : this(FloatArray(12))

    /**
     * Creates a new Mat3 with the given values ([v0] to [v8]) in **column-major** order.
     * See [rowMajor] for constructing a matrix in row-major order (WebGPU uses column-major so we will convert it)
     */
    constructor(
        v0: Float, v1: Float, v2: Float,
        v3: Float, v4: Float, v5: Float,
        v6: Float, v7: Float, v8: Float,
    ) : this(FloatArray(12).apply {
        this[0] = v0
        this[1] = v1
        this[2] = v2
        this[4] = v3
        this[5] = v4
        this[6] = v5
        this[8] = v6
        this[9] = v7
        this[10] = v8
        this[3] = 0f
        this[7] = 0f
        this[11] = 0f
    })

    companion object {
        /**
         * Creates a Mat3 from the given [values].
         * You should generally not use this constructor as it assumes indices 3, 7, and 11 are all 0s for padding reasons.
         */
        operator fun invoke(vararg values: Float) = Mat3f(floatArrayOf(*values))

        // 12 * 4 bytes
        const val SIZE_BYTES = 48u

        /**
         * Constructs a [Mat3f] in row-major order.
         */
        fun rowMajor(
            v0: Float, v1: Float, v2: Float,
            v3: Float, v4: Float, v5: Float,
            v6: Float, v7: Float, v8: Float,
        ) = Mat3f(
            v0, v3, v6,
            v1, v4, v7,
            v2, v5, v8
        )


        /**
         * Creates a Mat3 from a 12-element FloatArray [values].
         * Assumes the array is already in the correct internal format.
         */
        fun fromFloatArray(values: FloatArray): Mat3f {
            if (values.size != 12) {
                throw IllegalArgumentException("Mat3.fromFloatArray requires a 12-element FloatArray.")
            }
            return Mat3f(values.copyOf()) // Create a copy to ensure internal state is not modified externally
        }

        /**
         * Creates a new identity Mat3.
         */
        fun identity(dst: Mat3f = Mat3f()): Mat3f {
            return dst.apply {
                array[0] = 1f; array[1] = 0f; array[2] = 0f; array[3] = 0f
                array[4] = 0f; array[5] = 1f; array[6] = 0f; array[7] = 0f
                array[8] = 0f; array[9] = 0f; array[10] = 1f; array[11] = 0f
            }
        }

        /**
         * Creates a Mat3 from the upper left 3x3 part of [m4].
         */
        fun fromMat4(m4: Mat4f, dst: Mat3f = Mat3f()): Mat3f {
            return dst.apply {
                array[0] = m4[0]; array[1] = m4[1]; array[2] = m4[2]; array[3] = 0f
                array[4] = m4[4]; array[5] = m4[5]; array[6] = m4[6]; array[7] = 0f
                array[8] = m4[8]; array[9] = m4[9]; array[10] = m4[10]; array[11] = 0f
            }
        }

        /**
         * Creates a Mat3 rotation matrix from [q].
         */
        fun fromQuat(q: Quatf, dst: Mat3f = Mat3f()): Mat3f {
            val x = q.x;
            val y = q.y;
            val z = q.z;
            val w = q.w;
            val x2 = x + x;
            val y2 = y + y;
            val z2 = z + z;

            val xx = x * x2;
            val yx = y * x2;
            val yy = y * y2;
            val zx = z * x2;
            val zy = z * y2;
            val zz = z * z2;
            val wx = w * x2;
            val wy = w * y2;
            val wz = w * z2;

            return dst.apply {
                array[0] = 1f - yy - zz; array[1] = yx + wz; array[2] = zx - wy; array[3] = 0f;
                array[4] = yx - wz; array[5] = 1f - xx - zz; array[6] = zy + wx; array[7] = 0f;
                array[8] = zx + wy; array[9] = zy - wx; array[10] = 1f - xx - yy; array[11] = 0f;
            }
        }

        /**
         * Creates a 3-by-3 matrix which translates by the given vector [v].
         */
        fun translation(v: Vec2f, dst: Mat3f = Mat3f()): Mat3f {
            return dst.apply {
                array[0] = 1f; array[1] = 0f; array[2] = 0f; array[3] = 0f
                array[4] = 0f; array[5] = 1f; array[6] = 0f; array[7] = 0f
                array[8] = v.x; array[9] = v.y; array[10] = 1f; array[11] = 0f
            }
        }

        /**
         * Creates a 3-by-3 matrix which rotates by the given [angleInRadians].
         */
        fun rotation(angleInRadians: Float, dst: Mat3f = Mat3f()): Mat3f {
            val c = cos(angleInRadians);
            val s = sin(angleInRadians);

            return dst.apply {
                array[0] = c; array[1] = s; array[2] = 0f; array[3] = 0f
                array[4] = -s; array[5] = c; array[6] = 0f; array[7] = 0f
                array[8] = 0f; array[9] = 0f; array[10] = 1f; array[11] = 0f
            }
        }

        /**
         * Creates a 3-by-3 matrix which rotates around the x-axis by the given [angleInRadians].
         */
        fun rotationX(angleInRadians: Float, dst: Mat3f = Mat3f()): Mat3f {
            val c = cos(angleInRadians);
            val s = sin(angleInRadians);

            return dst.apply {
                array[0] = 1f; array[1] = 0f; array[2] = 0f; array[3] = 0f
                array[4] = 0f; array[5] = c; array[6] = s; array[7] = 0f
                array[8] = 0f; array[9] = -s; array[10] = c; array[11] = 0f
            }
        }

        /**
         * Creates a 3-by-3 matrix which rotates around the y-axis by the given [angleInRadians].
         */
        fun rotationY(angleInRadians: Float, dst: Mat3f = Mat3f()): Mat3f {
            val c = cos(angleInRadians);
            val s = sin(angleInRadians);

            return dst.apply {
                array[0] = c; array[1] = 0f; array[2] = -s; array[3] = 0f
                array[4] = 0f; array[5] = 1f; array[6] = 0f; array[7] = 0f
                array[8] = s; array[9] = 0f; array[10] = c; array[11] = 0f
            }
        }

        /**
         * Creates a 3-by-3 matrix which rotates around the z-axis by the given [angleInRadians].
         */
        fun rotationZ(angleInRadians: Float, dst: Mat3f = Mat3f()): Mat3f = rotation(angleInRadians, dst)


        /**
         * Creates a 3-by-3 matrix which scales in the X and Y dimensions by the components of [v].
         */
        fun scaling(v: Vec2f, dst: Mat3f = Mat3f()): Mat3f {
            return dst.apply {
                array[0] = v.x; array[1] = 0f; array[2] = 0f; array[3] = 0f
                array[4] = 0f; array[5] = v.y; array[6] = 0f; array[7] = 0f
                array[8] = 0f; array[9] = 0f; array[10] = 1f; array[11] = 0f
            }
        }

        /**
         * Creates a 3-by-3 matrix which scales in each dimension by the components of [v].
         */
        fun scaling3D(v: Vec3f, dst: Mat3f = Mat3f()): Mat3f {
            return dst.apply {
                array[0] = v[0]; array[1] = 0f; array[2] = 0f; array[3] = 0f
                array[4] = 0f; array[5] = v[1]; array[6] = 0f; array[7] = 0f
                array[8] = 0f; array[9] = 0f; array[10] = v[2]; array[11] = 0f
            }
        }

        /**
         * Creates a 3-by-3 matrix which scales uniformly in the X and Y dimensions by [s].
         */
        fun uniformScaling(s: Float, dst: Mat3f = Mat3f()): Mat3f {
            return dst.apply {
                array[0] = s; array[1] = 0f; array[2] = 0f; array[3] = 0f
                array[4] = 0f; array[5] = s; array[6] = 0f; array[7] = 0f
                array[8] = 0f; array[9] = 0f; array[10] = 1f; array[11] = 0f
            }
        }

        /**
         * Creates a 3-by-3 matrix which scales uniformly in each dimension by [s].
         */
        fun uniformScaling3D(s: Float, dst: Mat3f = Mat3f()): Mat3f {
            return dst.apply {
                array[0] = s; array[1] = 0f; array[2] = 0f; array[3] = 0f
                array[4] = 0f; array[5] = s; array[6] = 0f; array[7] = 0f
                array[8] = 0f; array[9] = 0f; array[10] = s; array[11] = 0f
            }
        }

    }

    inline operator fun plus(other: Mat3f) = add(other)
    inline operator fun minus(other: Mat3f) = diff(other)
    inline operator fun times(scalar: Float) = multiplyScalar(scalar)
    inline operator fun times(matrix: Mat3f) = multiply(matrix)
    inline operator fun times(vector: Vec3f) = multiplyVector(vector)
    inline operator fun div(scalar: Float) = div(scalar, Mat3f())
    inline operator fun unaryMinus() = negate()


    inline operator fun get(index: Int): Float {
        return this.array[index]
    }


    inline operator fun get(row: Int, col: Int): Float {
        return this.array[col * 4 + row] // Column-major order
    }

    inline operator fun set(row: Int, col: Int, value: Float) {
        this.array[col * 4 + row] = value // Column-major order
    }

    inline operator fun set(index: Int, value: Float) {
        this.array[index] = value
    }

    inline var m00
        get() = this[0];
        set(value) {
            this[0] = value
        }
    inline var m01
        get() = this[4];
        set(value) {
            this[4] = value
        }
    inline var m02
        get() = this[8];
        set(value) {
            this[8] = value
        }
    inline var m10
        get() = this[1];
        set(value) {
            this[1] = value
        }
    inline var m11
        get() = this[5];
        set(value) {
            this[5] = value
        }
    inline var m12
        get() = this[9];
        set(value) {
            this[9] = value
        }
    inline var m20
        get() = this[2];
        set(value) {
            this[2] = value
        }
    inline var m21
        get() = this[6];
        set(value) {
            this[6] = value
        }
    inline var m22
        get() = this[10];
        set(value) {
            this[10] = value
        }

    init {
        if (array.size != 12) {
            throw IllegalArgumentException("Mat3 requires a 12-element FloatArray for storage.")
        }
    }

    /**
     * Sets this matrix to the identity matrix
     */
    fun setIdentity() {
        identity(this)
    }

    /**
     * Gets a copy of the internal FloatArray representation of the matrix.
     * Modifying the returned array will not affect the original matrix.
     */
    fun toFloatArray(): FloatArray = array.copyOf() // Return a copy for safety

    /**
     * Computes the determinant of `this`.
     */
    fun determinant(): Float {
        val m00 = array[0]
        val m01 = array[1]
        val m02 = array[2]
        val m10 = array[4]
        val m11 = array[5]
        val m12 = array[6]
        val m20 = array[8]
        val m21 = array[9]
        val m22 = array[10]

        return m00 * (m11 * m22 - m21 * m12) -
                m10 * (m01 * m22 - m21 * m02) +
                m20 * (m01 * m12 - m11 * m02)
    }

    /**
     * Negates `this`.
     */
    fun negate(dst: Mat3f = Mat3f()): Mat3f {
        return dst.apply {
            array[0] = -this@Mat3f.array[0]; array[1] = -this@Mat3f.array[1]; array[2] = -this@Mat3f.array[2]; array[3] = 0f
            array[4] = -this@Mat3f.array[4]; array[5] = -this@Mat3f.array[5]; array[6] = -this@Mat3f.array[6]; array[7] = 0f
            array[8] = -this@Mat3f.array[8]; array[9] = -this@Mat3f.array[9]; array[10] = -this@Mat3f.array[10]; array[11] = 0f
        }
    }

    /**
     * Multiplies `this` by the scalar [s].
     */
    fun multiplyScalar(s: Float, dst: Mat3f = Mat3f()): Mat3f {
        dst.array[0] = array[0] * s; dst.array[1] = array[1] * s; dst.array[2] = array[2] * s;dst.array[3] = 0f
        dst.array[4] = array[4] * s; dst.array[5] = array[5] * s; dst.array[6] = array[6] * s;dst.array[7] = 0f
        dst.array[8] = array[8] * s; dst.array[9] = array[9] * s; dst.array[10] = array[10] * s;dst.array[11] = 0f

        return dst
    }

    /**
     * Divides `this` by the scalar [s].
     */
    fun div(s: Float, dst: Mat3f = Mat3f()): Mat3f {
        dst.array[0] = array[0] / s; dst.array[1] = array[1] / s; dst.array[2] = array[2] / s; dst.array[3] = 0f
        dst.array[4] = array[4] / s; dst.array[5] = array[5] / s; dst.array[6] = array[6] / s;dst.array[7] = 0f
        dst.array[8] = array[8] / s; dst.array[9] = array[9] / s; dst.array[10] = array[10] / s;dst.array[11] = 0f

        return dst
    }

    /**
     * Adds [other] to `this`.
     */
    fun add(other: Mat3f, dst: Mat3f = Mat3f()): Mat3f {
        dst.array[0] = array[0] + other.array[0]; dst.array[1] = array[1] + other.array[1]; dst.array[2] = array[2] + other.array[2]; dst.array[3] = 0f
        dst.array[4] = array[4] + other.array[4]; dst.array[5] = array[5] + other.array[5]; dst.array[6] = array[6] + other.array[6]; dst.array[7] = 0f
        dst.array[8] = array[8] + other.array[8]; dst.array[9] = array[9] + other.array[9]; dst.array[10] = array[10] + other.array[10]; dst.array[11] =
            0f

        return dst
    }

    inline fun plus(other: Mat3f, dst: Mat3f = Mat3f()) = add(other, dst)

    /**
     * Calculates the difference between `this` and [other].
     */
    fun diff(other: Mat3f, dst: Mat3f = Mat3f()): Mat3f {
        dst.array[0] = array[0] - other.array[0]; dst.array[1] = array[1] - other.array[1]; dst.array[2] = array[2] - other.array[2];dst.array[3] = 0f
        dst.array[4] = array[4] - other.array[4]; dst.array[5] = array[5] - other.array[5]; dst.array[6] = array[6] - other.array[6];dst.array[7] = 0f
        dst.array[8] = array[8] - other.array[8]; dst.array[9] = array[9] - other.array[9]; dst.array[10] = array[10] - other.array[10]; dst.array[11] =
            0f

        return dst
    }

    inline fun minus(other: Mat3f, dst: Mat3f = Mat3f()) = diff(other, dst)

    /**
     * Copies `this`.
     */
    fun copy(dst: Mat3f = Mat3f()): Mat3f {
        this.array.copyInto(dst.array)
        return dst
    }

    /**
     * Copies `this` (alias for [copy]).
     */
    inline fun clone(dst: Mat3f = Mat3f()): Mat3f = copy(dst)


    /**
     * Computes the transpose of `this`.
     */
    fun transpose(dst: Mat3f = Mat3f()): Mat3f {
        if (dst === this) {
            // Perform in-place transpose
            var t: Float

            // 0 1 2
            // 4 5 6
            // 8 9 10

            t = array[1]; array[1] = array[4]; array[4] = t;
            t = array[2]; array[2] = array[8]; array[8] = t;
            t = array[6]; array[6] = array[9]; array[9] = t;

            return dst
        }

        val m00 = array[0]
        val m01 = array[1]
        val m02 = array[2]
        val m10 = array[4]
        val m11 = array[5]
        val m12 = array[6]
        val m20 = array[8]
        val m21 = array[9]
        val m22 = array[10]

        return dst.apply {
            array[0] = m00; array[1] = m10; array[2] = m20; array[3] = 0f
            array[4] = m01; array[5] = m11; array[6] = m21; array[7] = 0f
            array[8] = m02; array[9] = m12; array[10] = m22; array[11] = 0f
        }
    }

    /**
     * Computes the inverse of `this`.
     * Returns identity if the matrix is not invertible.
     */
    fun inverse(dst: Mat3f = Mat3f()): Mat3f {
        val m00 = array[0]
        val m01 = array[1]
        val m02 = array[2]
        val m10 = array[4]
        val m11 = array[5]
        val m12 = array[6]
        val m20 = array[8]
        val m21 = array[9]
        val m22 = array[10]

        val b01 = m22 * m11 - m12 * m21
        val b11 = -m22 * m10 + m12 * m20
        val b21 = m21 * m10 - m11 * m20

        val det = m00 * b01 + m01 * b11 + m02 * b21
        if (det == 0f) {
            // Matrix is not invertible
            return identity()
        }
        val invDet = 1 / det

        return dst.apply {
            array[0] = b01 * invDet; array[3] = 0f
            array[1] = (-m22 * m01 + m02 * m21) * invDet; array[7] = 0f
            array[2] = (m12 * m01 - m02 * m11) * invDet; array[11] = 0f
            array[4] = b11 * invDet
            array[5] = (m22 * m00 - m02 * m20) * invDet
            array[6] = (-m12 * m00 + m02 * m10) * invDet
            array[8] = b21 * invDet
            array[9] = (-m21 * m00 + m01 * m20) * invDet
            array[10] = (m11 * m00 - m01 * m10) * invDet
        }
    }

    /**
     * Computes the inverse of `this` (alias for [inverse]).
     * Returns identity if the matrix is not invertible.
     */
    fun invert(dst: Mat3f = Mat3f()): Mat3f = inverse(dst)

    /**
     * Multiplies `this` by [other] (`this` * [other]).
     */
    fun multiply(other: Mat3f, dst: Mat3f = Mat3f()): Mat3f {
        val a00 = array[0]
        val a01 = array[1]
        val a02 = array[2]
        val a10 = array[4]
        val a11 = array[5]
        val a12 = array[6]
        val a20 = array[8]
        val a21 = array[9]
        val a22 = array[10]
        val b00 = other.array[0]
        val b01 = other.array[1]
        val b02 = other.array[2]
        val b10 = other.array[4]
        val b11 = other.array[5]
        val b12 = other.array[6]
        val b20 = other.array[8]
        val b21 = other.array[9]
        val b22 = other.array[10]

        return dst.apply {
            array[0] = a00 * b00 + a10 * b01 + a20 * b02; array[3] = 0f
            array[1] = a01 * b00 + a11 * b01 + a21 * b02; array[7] = 0f
            array[2] = a02 * b00 + a12 * b01 + a22 * b02; array[11] = 0f
            array[4] = a00 * b10 + a10 * b11 + a20 * b12
            array[5] = a01 * b10 + a11 * b11 + a21 * b12
            array[6] = a02 * b10 + a12 * b11 + a22 * b12
            array[8] = a00 * b20 + a10 * b21 + a20 * b22
            array[9] = a01 * b20 + a11 * b21 + a21 * b22
            array[10] = a02 * b20 + a12 * b21 + a22 * b22
        }
    }

    /**
     * Multiplies `this` by [other] (`this` * [other]) (alias for [multiply]).
     */
    inline fun mul(other: Mat3f, dst: Mat3f = Mat3f()): Mat3f = multiply(other, dst)

    /**
     * Multiplies this matrix by the vector [v].
     * @return The resulting vector.
     */
    fun multiplyVector(v: Vec3f, dst: Vec3f = Vec3f.create()): Vec3f {
        val x = v.x
        val y = v.y
        val z = v.z

        dst.x = this[0] * x + this[4] * y + this[8] * z
        dst.y = this[1] * x + this[5] * y + this[9] * z
        dst.z = this[2] * x + this[6] * y + this[10] * z

        return dst
    }

    /**
     * Gets the translation component of `this`.
     */
    fun getTranslation(dst: Vec2f = Vec2f.create()): Vec2f {
        dst.x = array[8]
        dst.y = array[9]
        return dst
    }

    /**
     * Calculates the absolute value of how much this matrix scales vectors in the X and Y axes.
     */
    fun getScaling(dst: Vec2f = Vec2f.create()): Vec2f {
        val xx = array[0]
        val xy = array[1]
        val yx = array[4]
        val yy = array[5]

        dst.x = sqrt(xx * xx + xy * xy)
        dst.y = sqrt(yx * yx + yy * yy)

        return dst
    }


    /**
     * Gets the absolute value of how much this matrix scales 3D vectors in the X,Y,Z dimensions.
     */
    fun get3DScaling(dst: Vec3f = Vec3f.create()): Vec3f {
        val xx = this[0]
        val xy = this[1]
        val xz = this[2]
        val yx = this[4]
        val yy = this[5]
        val yz = this[6]
        val zx = this[8]
        val zy = this[9]
        val zz = this[10]

        dst[0] = sqrt(xx * xx + xy * xy + xz * xz)
        dst[1] = sqrt(yx * yx + yy * yy + yz * yz)
        dst[2] = sqrt(zx * zx + zy * zy + zz * zz)

        return dst
    }

    /**
     * Post-multiplies this 3x3 matrix by a 2D translation matrix created from [v] and writes the result into [dst].
     * `dst = this * translation(v)`
     *
     * Order of operations on a transformed vector:
     * - Column vectors (`dst * vec`): The translation defined by [v] is applied to `vec` **before** the transformation represented by the original matrix `this`.
     * - Row vectors (`vec * dst`): The translation defined by [v] is applied to `vec` **after** the transformation represented by the original matrix `this`.
     */
    fun translate(v: Vec2f, dst: Mat3f = Mat3f()): Mat3f {
        val v0 = v.x
        val v1 = v.y

        val m00 = array[0]
        val m01 = array[1]
        val m02 = array[2]
        val m10 = array[4]
        val m11 = array[5]
        val m12 = array[6]
        val m20 = array[8]
        val m21 = array[9]
        val m22 = array[10]

        if (this !== dst) {
            dst.array[0] = m00
            dst.array[1] = m01
            dst.array[2] = m02
            dst.array[4] = m10
            dst.array[5] = m11
            dst.array[6] = m12
        }

        dst.array[8] = m00 * v0 + m10 * v1 + m20
        dst.array[9] = m01 * v0 + m11 * v1 + m21
        dst.array[10] = m02 * v0 + m12 * v1 + m22

        return dst
    }

    /**
     * Post-multiplies this 3x3 matrix by a 2D rotation matrix (around the Z-axis) created from [angleInRadians] and writes the result into [dst].
     * `dst = this * rotationZ(angleInRadians)`
     *
     * Order of operations on a transformed vector:
     * - Column vectors (`dst * vec`): The rotation defined by [angleInRadians] is applied to `vec` **before** the transformation represented by the original matrix `this`.
     * - Row vectors (`vec * dst`): The rotation defined by [angleInRadians] is applied to `vec` **after** the transformation represented by the original matrix `this`.
     * This is equivalent to `rotateZ`. For 2D transformations, this rotates points in the XY plane.
     */
    fun rotate(angleInRadians: Float, dst: Mat3f = Mat3f()): Mat3f {
        val m00 = array[0]
        val m01 = array[1]
        val m02 = array[2]
        val m10 = array[4]
        val m11 = array[5]
        val m12 = array[6]
        val c = cos(angleInRadians)
        val s = sin(angleInRadians)

        dst.array[0] = c * m00 + s * m10
        dst.array[1] = c * m01 + s * m11
        dst.array[2] = c * m02 + s * m12; dst.array[3] = 0f

        dst.array[4] = c * m10 - s * m00
        dst.array[5] = c * m11 - s * m01
        dst.array[6] = c * m12 - s * m02; dst.array[7] = 0f


        if (this !== dst) {
            dst.array[8] = array[8];
            dst.array[9] = array[9];
            dst.array[10] = array[10]; dst.array[11] = 0f
        }

        return dst
    }

    /**
     * Post-multiplies this 3x3 matrix by a 3D rotation matrix around the X-axis created from [angleInRadians] and writes the result into [dst].
     * `dst = this * rotationX(angleInRadians)`
     *
     * If you multiply a [Vec3f] with the resulting matrix (`dst * vec`), the rotation around the X-axis applies *after* the original matrix's (`this`) transform.
     */
    fun rotateX(angleInRadians: Float, dst: Mat3f = Mat3f()): Mat3f {
        val m10 = array[4]
        val m11 = array[5]
        val m12 = array[6]
        val m20 = array[8]
        val m21 = array[9]
        val m22 = array[10]

        val c = cos(angleInRadians)
        val s = sin(angleInRadians)

        dst.array[4] = c * m10 + s * m20; dst.array[7] = 0f
        dst.array[5] = c * m11 + s * m21
        dst.array[6] = c * m12 + s * m22
        dst.array[8] = c * m20 - s * m10; dst.array[11] = 0f
        dst.array[9] = c * m21 - s * m11
        dst.array[10] = c * m22 - s * m12

        if (this !== dst) {
            dst.array[0] = array[0];
            dst.array[1] = array[1];
            dst.array[2] = array[2]; dst.array[3] = 0f
        }

        return dst
    }

    /**
     * Post-multiplies this 3x3 matrix by a 3D rotation matrix around the Y-axis created from [angleInRadians] and writes the result into [dst].
     * `dst = this * rotationY(angleInRadians)`
     *
     * If you multiply a [Vec3f] with the resulting matrix (`dst * vec`), the rotation around the Y-axis applies *after* the original matrix's (`this`) transform.
     */
    fun rotateY(angleInRadians: Float, dst: Mat3f = Mat3f()): Mat3f {
        val m00 = array[0]
        val m01 = array[1]
        val m02 = array[2]
        val m20 = array[8]
        val m21 = array[9]
        val m22 = array[10]
        val c = cos(angleInRadians)
        val s = sin(angleInRadians)

        dst.array[0] = c * m00 - s * m20; dst.array[3] = 0f
        dst.array[1] = c * m01 - s * m21
        dst.array[2] = c * m02 - s * m22
        dst.array[8] = c * m20 + s * m00; dst.array[11] = 0f
        dst.array[9] = c * m21 + s * m01
        dst.array[10] = c * m22 + s * m02

        if (this !== dst) {
            dst.array[4] = array[4];
            dst.array[5] = array[5];
            dst.array[6] = array[6]; dst.array[7] = 0f
        }

        return dst
    }

    /**
     * Post-multiplies this 3x3 matrix by a 2D rotation matrix (around the Z-axis) created from [angleInRadians] and writes the result into [dst].
     * `dst = this * rotationZ(angleInRadians)`
     *
     * If you multiply a [Vec2f] (or a [Vec3f] with z=0) with the resulting matrix (`dst * vec`), the rotation applies *after* the original matrix's (`this`) transform.
     * This is an alias for [rotate]. For 2D transformations, this rotates points in the XY plane.
     */
    fun rotateZ(angleInRadians: Float, dst: Mat3f = Mat3f()): Mat3f = rotate(angleInRadians, dst)

    /**
     * Post-multiplies this 3x3 matrix by a 2D scaling matrix created from the components of [v] (for X and Y axes) and writes the result into [dst].
     * The Z-axis scaling component is implicitly 1.
     * `dst = this * scaling(v.x, v.y, 1.0f)`
     *
     * If you multiply a [Vec2f] (or a [Vec3f] with z component unaffected by this specific scaling's Z part) with the resulting matrix (`dst * vec`),
     * the scaling on X and Y axes applies *after* the original matrix's (`this`) transform.
     */
    fun scale(v: Vec2f, dst: Mat3f = Mat3f()): Mat3f {
        val v0 = v.x
        val v1 = v.y

        dst.array[0] = v0 * array[0]
        dst.array[1] = v0 * array[1]
        dst.array[2] = v0 * array[2]
        dst.array[3] = 0f
        dst.array[4] = v1 * array[4]
        dst.array[5] = v1 * array[5]
        dst.array[6] = v1 * array[6]
        dst.array[7] = 0f

        if (this !== dst) {
            dst.array[8] = array[8];
            dst.array[9] = array[9];
            dst.array[10] = array[10]; dst.array[11] = 0f
        }

        return dst
    }

    /**
     * Post-multiplies this 3x3 matrix by a 3D scaling matrix created from the components of [v] (for X, Y, and Z axes) and writes the result into [dst].
     * `dst = this * scaling(v.x, v.y, v.z)`
     *
     * If you multiply a [Vec3f] with the resulting matrix (`dst * vec`), the scaling on X, Y, and Z axes applies *after* the original matrix's (`this`) transform.
     */
    fun scale3D(v: Vec3f, dst: Mat3f = Mat3f()): Mat3f {
        val v0 = v[0]
        val v1 = v[1]
        val v2 = v[2]

        dst.array[0] = v0 * array[0]; dst.array[3] = 0f
        dst.array[1] = v0 * array[1]
        dst.array[2] = v0 * array[2]

        dst.array[4] = v1 * array[4]; dst.array[7] = 0f
        dst.array[5] = v1 * array[5]
        dst.array[6] = v1 * array[6]

        dst.array[8] = v2 * array[8]; dst.array[11] = 0f
        dst.array[9] = v2 * array[9]
        dst.array[10] = v2 * array[10]

        return dst
    }

    /**
     * Post-multiplies this 3x3 matrix by a 2D uniform scaling matrix created from [s] (for X and Y axes) and writes the result into [dst].
     * The Z-axis scaling component is implicitly 1.
     * `dst = this * scaling(s, s, 1.0f)`
     *
     * If you multiply a [Vec2f] (or a [Vec3f] with z component unaffected by this specific scaling's Z part) with the resulting matrix (`dst * vec`),
     * the uniform scaling on X and Y axes applies *after* the original matrix's (`this`) transform.
     */
    fun uniformScale(s: Float, dst: Mat3f = Mat3f()): Mat3f {
        dst.array[0] = s * array[0]; dst.array[3] = 0f
        dst.array[1] = s * array[1]
        dst.array[2] = s * array[2]

        dst.array[4] = s * array[4]; dst.array[7] = 0f
        dst.array[5] = s * array[5]
        dst.array[6] = s * array[6]

        if (this !== dst) {
            dst.array[8] = array[8];
            dst.array[9] = array[9];
            dst.array[10] = array[10]; dst.array[11] = 0f
        }

        return dst
    }

    /**
     * Post-multiplies this 3x3 matrix by a 3D uniform scaling matrix created from [s] (for X, Y, and Z axes) and writes the result into [dst].
     * `dst = this * uniformScaling3D(s)`
     *
     * Order of operations on a transformed vector:
     * - Column vectors (`dst * vec`): The uniform scaling defined by [s] is applied to `vec` **before** the transformation represented by the original matrix `this`.
     * - Row vectors (`vec * dst`): The uniform scaling defined by [s] is applied to `vec` **after** the transformation represented by the original matrix `this`.
     */
    fun uniformScale3D(s: Float, dst: Mat3f = Mat3f()): Mat3f {
        dst.array[0] = s * array[0]; dst.array[3] = 0f
        dst.array[1] = s * array[1]
        dst.array[2] = s * array[2]

        dst.array[4] = s * array[4]; dst.array[7] = 0f
        dst.array[5] = s * array[5]
        dst.array[6] = s * array[6]

        dst.array[8] = s * array[8]; dst.array[11] = 0f
        dst.array[9] = s * array[9]
        dst.array[10] = s * array[10]

        return dst
    }

    /**
     * Pre-multiplies this 3x3 matrix by a 2D translation matrix created from [v] and writes the result into [dst].
     * `dst = translation(v) * this`
     *
     * Order of operations on a transformed vector:
     * - Column vectors (`dst * vec`): The translation defined by [v] is applied to `vec` **after** the transformation represented by the original matrix `this`.
     * - Row vectors (`vec * dst`): The translation defined by [v] is applied to `vec` **before** the transformation represented by the original matrix `this`.
     */
    fun preTranslate(v: Vec2f, dst: Mat3f = Mat3f()): Mat3f {
        val m00 = array[0];
        val m01 = array[4];
        val m02 = array[8]
        val m10 = array[1];
        val m11 = array[5];
        val m12 = array[9]
        val m20 = array[2];
        val m21 = array[6];
        val m22 = array[10]

        val vx = v.x
        val vy = v.y

        dst.array[0] = m00 + vx * m20
        dst.array[1] = m10 + vy * m20
        dst.array[2] = m20

        dst.array[4] = m01 + vx * m21
        dst.array[5] = m11 + vy * m21
        dst.array[6] = m21

        dst.array[8] = m02 + vx * m22
        dst.array[9] = m12 + vy * m22
        dst.array[10] = m22

        dst.array[3] = 0f; dst.array[7] = 0f; dst.array[11] = 0f
        return dst
    }

    /**
     * Pre-multiplies this 3x3 matrix by a 2D rotation matrix (around the Z-axis) created from [angleInRadians] and writes the result into [dst].
     * `dst = rotationZ(angleInRadians) * this`
     *
     * Order of operations on a transformed vector:
     * - Column vectors (`dst * vec`): The rotation defined by [angleInRadians] is applied to `vec` **after** the transformation represented by the original matrix `this`.
     * - Row vectors (`vec * dst`): The rotation defined by [angleInRadians] is applied to `vec` **before** the transformation represented by the original matrix `this`.
     * This is equivalent to `preRotateZ`. For 2D transformations, this rotates points in the XY plane.
     */
    fun preRotate(angleInRadians: Float, dst: Mat3f = Mat3f()): Mat3f {
        val m00 = array[0];
        val m01 = array[4];
        val m02 = array[8]
        val m10 = array[1];
        val m11 = array[5];
        val m12 = array[9]
        val m20 = array[2];
        val m21 = array[6];
        val m22 = array[10]

        val s = sin(angleInRadians)
        val c = cos(angleInRadians)

        dst.array[0] = c * m00 - s * m10
        dst.array[1] = s * m00 + c * m10
        dst.array[2] = m20

        dst.array[4] = c * m01 - s * m11
        dst.array[5] = s * m01 + c * m11
        dst.array[6] = m21

        dst.array[8] = c * m02 - s * m12
        dst.array[9] = s * m02 + c * m12
        dst.array[10] = m22

        dst.array[3] = 0f; dst.array[7] = 0f; dst.array[11] = 0f
        return dst
    }

    /**
     * Pre-multiplies this 3x3 matrix by a 3D rotation matrix around the X-axis created from [angleInRadians] and writes the result into [dst].
     * `dst = rotationX(angleInRadians) * this`
     *
     * Order of operations on a transformed vector:
     * - Column vectors (`dst * vec`): The X-axis rotation defined by [angleInRadians] is applied to `vec` **after** the transformation represented by the original matrix `this`.
     * - Row vectors (`vec * dst`): The X-axis rotation defined by [angleInRadians] is applied to `vec` **before** the transformation represented by the original matrix `this`.
     */
    fun preRotateX(angleInRadians: Float, dst: Mat3f = Mat3f()): Mat3f {
        val m00 = array[0];
        val m01 = array[4];
        val m02 = array[8]
        val m10 = array[1];
        val m11 = array[5];
        val m12 = array[9]
        val m20 = array[2];
        val m21 = array[6];
        val m22 = array[10]

        val s = sin(angleInRadians)
        val c = cos(angleInRadians)

        dst.array[0] = m00
        dst.array[1] = c * m10 - s * m20
        dst.array[2] = s * m10 + c * m20

        dst.array[4] = m01
        dst.array[5] = c * m11 - s * m21
        dst.array[6] = s * m11 + c * m21

        dst.array[8] = m02
        dst.array[9] = c * m12 - s * m22
        dst.array[10] = s * m12 + c * m22

        dst.array[3] = 0f; dst.array[7] = 0f; dst.array[11] = 0f
        return dst
    }

    /**
     * Pre-multiplies this 3x3 matrix by a 3D rotation matrix around the Y-axis created from [angleInRadians] and writes the result into [dst].
     * `dst = rotationY(angleInRadians) * this`
     *
     * Order of operations on a transformed vector:
     * - Column vectors (`dst * vec`): The Y-axis rotation defined by [angleInRadians] is applied to `vec` **after** the transformation represented by the original matrix `this`.
     * - Row vectors (`vec * dst`): The Y-axis rotation defined by [angleInRadians] is applied to `vec` **before** the transformation represented by the original matrix `this`.
     */
    fun preRotateY(angleInRadians: Float, dst: Mat3f = Mat3f()): Mat3f {
        val m00 = array[0];
        val m01 = array[4];
        val m02 = array[8]
        val m10 = array[1];
        val m11 = array[5];
        val m12 = array[9]
        val m20 = array[2];
        val m21 = array[6];
        val m22 = array[10]

        val s = sin(angleInRadians)
        val c = cos(angleInRadians)

        dst.array[0] = c * m00 + s * m20
        dst.array[1] = m10
        dst.array[2] = -s * m00 + c * m20

        dst.array[4] = c * m01 + s * m21
        dst.array[5] = m11
        dst.array[6] = -s * m01 + c * m21

        dst.array[8] = c * m02 + s * m22
        dst.array[9] = m12
        dst.array[10] = -s * m02 + c * m22

        dst.array[3] = 0f; dst.array[7] = 0f; dst.array[11] = 0f
        return dst
    }

    /**
     * Pre-multiplies this 3x3 matrix by a 2D rotation matrix (around the Z-axis) created from [angleInRadians] and writes the result into [dst].
     * `dst = rotationZ(angleInRadians) * this`
     *
     * Order of operations on a transformed vector:
     * - Column vectors (`dst * vec`): The Z-axis rotation defined by [angleInRadians] is applied to `vec` **after** the transformation represented by the original matrix `this`.
     * - Row vectors (`vec * dst`): The Z-axis rotation defined by [angleInRadians] is applied to `vec` **before** the transformation represented by the original matrix `this`.
     * This is an alias for [preRotate].
     */
    inline fun preRotateZ(angleInRadians: Float, dst: Mat3f = Mat3f()): Mat3f = preRotate(angleInRadians, dst)


    /**
     * Pre-multiplies this 3x3 matrix by a 2D scaling matrix created from [v] (for X and Y axes) and writes the result into [dst].
     * `dst = scaling(v) * this`
     *
     * Order of operations on a transformed vector:
     * - Column vectors (`dst * vec`): The scaling defined by [v] is applied to `vec` **after** the transformation represented by the original matrix `this`.
     * - Row vectors (`vec * dst`): The scaling defined by [v] is applied to `vec` **before** the transformation represented by the original matrix `this`.
     */
    fun preScale(v: Vec2f, dst: Mat3f = Mat3f()): Mat3f {
        val m00 = array[0];
        val m01 = array[4];
        val m02 = array[8]
        val m10 = array[1];
        val m11 = array[5];
        val m12 = array[9]
        val m20 = array[2];
        val m21 = array[6];
        val m22 = array[10]

        val sx = v.x
        val sy = v.y

        dst.array[0] = sx * m00
        dst.array[1] = sy * m10
        dst.array[2] = m20

        dst.array[4] = sx * m01
        dst.array[5] = sy * m11
        dst.array[6] = m21

        dst.array[8] = sx * m02
        dst.array[9] = sy * m12
        dst.array[10] = m22

        dst.array[3] = 0f; dst.array[7] = 0f; dst.array[11] = 0f
        return dst
    }

    /**
     * Pre-multiplies this 3x3 matrix by a 3D scaling matrix created from [v] (for X, Y, and Z axes) and writes the result into [dst].
     * `dst = scaling3D(v) * this`
     *
     * Order of operations on a transformed vector:
     * - Column vectors (`dst * vec`): The scaling defined by [v] is applied to `vec` **after** the transformation represented by the original matrix `this`.
     * - Row vectors (`vec * dst`): The scaling defined by [v] is applied to `vec` **before** the transformation represented by the original matrix `this`.
     */
    fun preScale3D(v: Vec3f, dst: Mat3f = Mat3f()): Mat3f {
        val m00 = array[0];
        val m01 = array[4];
        val m02 = array[8]
        val m10 = array[1];
        val m11 = array[5];
        val m12 = array[9]
        val m20 = array[2];
        val m21 = array[6];
        val m22 = array[10]

        val sx = v.x
        val sy = v.y
        val sz = v.z

        dst.array[0] = sx * m00
        dst.array[1] = sy * m10
        dst.array[2] = sz * m20

        dst.array[4] = sx * m01
        dst.array[5] = sy * m11
        dst.array[6] = sz * m21

        dst.array[8] = sx * m02
        dst.array[9] = sy * m12
        dst.array[10] = sz * m22

        dst.array[3] = 0f; dst.array[7] = 0f; dst.array[11] = 0f
        return dst
    }

    /**
     * Pre-multiplies this 3x3 matrix by a 2D uniform scaling matrix created from [s] (for X and Y axes) and writes the result into [dst].
     * `dst = uniformScaling(s) * this`
     *
     * Order of operations on a transformed vector:
     * - Column vectors (`dst * vec`): The uniform scaling defined by [s] is applied to `vec` **after** the transformation represented by the original matrix `this`.
     * - Row vectors (`vec * dst`): The uniform scaling defined by [s] is applied to `vec` **before** the transformation represented by the original matrix `this`.
     */
    fun preUniformScale(s: Float, dst: Mat3f = Mat3f()): Mat3f {
        val m00 = array[0];
        val m01 = array[4];
        val m02 = array[8]
        val m10 = array[1];
        val m11 = array[5];
        val m12 = array[9]
        val m20 = array[2];
        val m21 = array[6];
        val m22 = array[10]

        dst.array[0] = s * m00
        dst.array[1] = s * m10
        dst.array[2] = m20 // Z component of the third column is not scaled by 2D uniform scale

        dst.array[4] = s * m01
        dst.array[5] = s * m11
        dst.array[6] = m21 // Z component of the third column is not scaled by 2D uniform scale

        dst.array[8] = s * m02
        dst.array[9] = s * m12
        dst.array[10] = m22 // Z component of the third column is not scaled by 2D uniform scale

        dst.array[3] = 0f; dst.array[7] = 0f; dst.array[11] = 0f
        return dst
    }

    /**
     * Pre-multiplies this 3x3 matrix by a 3D uniform scaling matrix created from [s] (for X, Y, and Z axes) and writes the result into [dst].
     * `dst = uniformScaling3D(s) * this`
     *
     * Order of operations on a transformed vector:
     * - Column vectors (`dst * vec`): The uniform scaling defined by [s] is applied to `vec` **after** the transformation represented by the original matrix `this`.
     * - Row vectors (`vec * dst`): The uniform scaling defined by [s] is applied to `vec` **before** the transformation represented by the original matrix `this`.
     */
    fun preUniformScale3D(s: Float, dst: Mat3f = Mat3f()): Mat3f {
        val m00 = array[0];
        val m01 = array[4];
        val m02 = array[8]
        val m10 = array[1];
        val m11 = array[5];
        val m12 = array[9]
        val m20 = array[2];
        val m21 = array[6];
        val m22 = array[10]

        dst.array[0] = s * m00
        dst.array[1] = s * m10
        dst.array[2] = s * m20

        dst.array[4] = s * m01
        dst.array[5] = s * m11
        dst.array[6] = s * m21

        dst.array[8] = s * m02
        dst.array[9] = s * m12
        dst.array[10] = s * m22

        dst.array[3] = 0f; dst.array[7] = 0f; dst.array[11] = 0f
        return dst
    }


    /**
     * Checks if `this` is approximately equal to [other].
     */
    fun equalsApproximately(other: Mat3f, tolerance: Float = EPSILON): Boolean {
        return abs(array[0] - other.array[0]) < tolerance &&
                abs(array[1] - other.array[1]) < tolerance &&
                abs(array[2] - other.array[2]) < tolerance &&
                abs(array[4] - other.array[4]) < tolerance &&
                abs(array[5] - other.array[5]) < tolerance &&
                abs(array[6] - other.array[6]) < tolerance &&
                abs(array[8] - other.array[8]) < tolerance &&
                abs(array[9] - other.array[9]) < tolerance &&
                abs(array[10] - other.array[10]) < tolerance
    }

    /**
     * Sets the translation component of `dst` to [v].
     * Better named "withTranslation" but that's the JS name.
     */
    fun setTranslation(v: Vec2f, dst: Mat3f = Mat3f()): Mat3f {
        if (this !== dst) {
            dst.array[0] = array[0];
            dst.array[1] = array[1];
            dst.array[2] = array[2];
            dst.array[4] = array[4];
            dst.array[5] = array[5];
            dst.array[6] = array[6];
        }
        dst.array[8] = v.x;
        dst.array[9] = v.y;
        dst.array[10] = 1f; // Ensure the bottom-right is 1 for translation
        return dst
    }

    /**
     * Gets the specified [axis] (0=x, 1=y) of `this` as a Vec2.
     */
    fun getAxis(axis: Int, dst: Vec2f = Vec2f.create()): Vec2f {
        if (axis != 0 && axis != 1) throw IllegalArgumentException("Mat3f only has axis 0 and 1")
        val off = axis * 4
        dst.x = array[off + 0]
        dst.y = array[off + 1]
        return dst
    }

    /**
     * Creates a matrix copy of `this` with the specified [axis] (0=x, 1=y) set to [v].
     */
    fun setAxis(v: Vec2f, axis: Int, dst: Mat3f = Mat3f()): Mat3f {
        if (axis != 0 && axis != 1) throw IllegalArgumentException("Mat3f only has axis 0 and 1")
        val newDst = if (dst === this) this else copy(dst)

        val off = axis * 4
        newDst.array[off + 0] = v.x
        newDst.array[off + 1] = v.y
        return newDst
    }

    /**
     * Sets the values of `this` from [v0] to [v8], in column-major order.
     */
    fun set(
        v0: Float, v1: Float, v2: Float,
        v3: Float, v4: Float, v5: Float,
        v6: Float, v7: Float, v8: Float,
    ): Mat3f = this.apply {
        array[0] = v0; array[1] = v1; array[2] = v2; array[3] = 0f;
        array[4] = v3; array[5] = v4; array[6] = v5; array[7] = 0f;
        array[8] = v6; array[9] = v7; array[10] = v8; array[11] = 0f;
    }

    override fun toString(): String {
        return """
            [${m00.ns},${m01.ns},${m02.ns}]
            [${m10.ns},${m11.ns},${m12.ns}]
            [${m20.ns},${m21.ns},${m22.ns}]
        """.trimIndent()
    }

    /**
     * Checks if `this` is exactly equal to [other].
     */
    override fun equals(other: Any?): Boolean {
        return other is Mat3f &&
                array[0] == other.array[0] &&
                array[1] == other.array[1] &&
                array[2] == other.array[2] &&
                array[4] == other.array[4] &&
                array[5] == other.array[5] &&
                array[6] == other.array[6] &&
                array[8] == other.array[8] &&
                array[9] == other.array[9] &&
                array[10] == other.array[10]
    }

    /**
     * Computes the hash code for `this`.
     */
    override fun hashCode(): Int {
        var result = array.contentHashCode()
        // We only consider the relevant 9 elements for equality/hash code
        result = 31 * result + array[0].hashCode()
        result = 31 * result + array[1].hashCode()
        result = 31 * result + array[2].hashCode()
        result = 31 * result + array[4].hashCode()
        result = 31 * result + array[5].hashCode()
        result = 31 * result + array[6].hashCode()
        result = 31 * result + array[8].hashCode()
        result = 31 * result + array[9].hashCode()
        result = 31 * result + array[10].hashCode()
        return result
    }
}
