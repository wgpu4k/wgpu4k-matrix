@file:Suppress("NOTHING_TO_INLINE")

package io.github.natanfudge.wgpu4k.matrix

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
 * The provided JS code uses indices 0-2, 4-6, 8-10, which corresponds to the
 * first 3 rows and first 3 columns of a 4x4 matrix.
 *
 * Do not depend on the values in the padding cells, as the behavior of those may change in the future.
 */
/*@JvmInline value*/ class Mat3f private constructor(val arrays: FloatArray) {
    inline val m00 get() = this[0]
    inline val m01 get() = this[4]
    inline val m02 get() = this[8]
    inline val m10 get() = this[1]
    inline val m11 get() = this[5]
    inline val m12 get() = this[9]
    inline val m20 get() = this[2]
    inline val m21 get() = this[6]
    inline val m22 get() = this[10]

    init {
        if (arrays.size != 12) {
            throw IllegalArgumentException("Mat3 requires a 12-element FloatArray for storage.")
        }
    }

    inline operator fun plus(other: Mat3f) = add(this, other)
    inline operator fun minus(other: Mat3f) = diff(this, other)
    inline operator fun times(scalar: Float) = multiplyScalar(scalar)
    inline operator fun times(matrix: Mat3f) = multiply(matrix)
    inline operator fun div(scalar: Float) = div(scalar, Mat3f())
    inline operator fun unaryMinus() = negate()


    inline operator fun get(index: Int): Float {
        return this.arrays[index]
    }

    inline operator fun set(index: Int, value: Float) {
        this.arrays[index] = value
    }


    /**
     * Creates a new Mat3 with the given values ([v0] to [v8]) in column-major order.
     */
    constructor(
        v0: Float = 0f, v1: Float = 0f, v2: Float = 0f,
        v3: Float = 0f, v4: Float = 0f, v5: Float = 0f,
        v6: Float = 0f, v7: Float = 0f, v8: Float = 0f,
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
        // The JS code explicitly sets these to 0, aligning with a 4x4 layout where the 4th column is not used for 3x3
        this[3] = 0f
        this[7] = 0f
        this[11] = 0f
    })


    override fun toString(): String {
        return """
            [$m00,$m01,$m02]
            [$m10,$m11,$m12]
            [$m20,$m21,$m22]
        """.trimIndent()
    }

    companion object {

        /**
         * Creates a Mat3 from the given [values].
         * You should generally not use this constructor as it assumes indices 3, 7 and 11 are all 0s for padding reasons.
         */
        operator fun invoke(vararg values: Float) = Mat3f(floatArrayOf(*values))

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
                arrays[0] = 1f; arrays[1] = 0f; arrays[2] = 0f; arrays[3] = 0f
                arrays[4] = 0f; arrays[5] = 1f; arrays[6] = 0f; arrays[7] = 0f
                arrays[8] = 0f; arrays[9] = 0f; arrays[10] = 1f; arrays[11] = 0f
            }
        }

        /**
         * Creates a Mat3 from the upper left 3x3 part of [m4].
         */
        fun fromMat4(m4: Mat4f, dst: Mat3f = Mat3f()): Mat3f {
            return dst.apply {
                arrays[0] = m4[0]; arrays[1] = m4[1]; arrays[2] = m4[2]; arrays[3] = 0f
                arrays[4] = m4[4]; arrays[5] = m4[5]; arrays[6] = m4[6]; arrays[7] = 0f
                arrays[8] = m4[8]; arrays[9] = m4[9]; arrays[10] = m4[10]; arrays[11] = 0f
            }
        }

        /**
         * Creates a Mat3 rotation matrix from [q].
         */
        fun fromQuat(q: Quatf, dst: Mat3f = Mat3f()): Mat3f { // Assuming QuatArg is FloatArray
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
                arrays[0] = 1f - yy - zz; arrays[1] = yx + wz; arrays[2] = zx - wy; arrays[3] = 0f;
                arrays[4] = yx - wz; arrays[5] = 1f - xx - zz; arrays[6] = zy + wx; arrays[7] = 0f;
                arrays[8] = zx + wy; arrays[9] = zy - wx; arrays[10] = 1f - xx - yy; arrays[11] = 0f;
            }
        }

        /**
         * Creates a 3-by-3 matrix which translates by the given vector [v].
         */
        fun translation(v: Vec2f, dst: Mat3f = Mat3f()): Mat3f {
            return dst.apply {
                arrays[0] = 1f; arrays[1] = 0f; arrays[2] = 0f; arrays[3] = 0f
                arrays[4] = 0f; arrays[5] = 1f; arrays[6] = 0f; arrays[7] = 0f
                arrays[8] = v.x; arrays[9] = v.y; arrays[10] = 1f; arrays[11] = 0f
            }
        }

        /**
         * Creates a 3-by-3 matrix which rotates by the given [angleInRadians].
         */
        fun rotation(angleInRadians: Float, dst: Mat3f = Mat3f()): Mat3f {
            val c = cos(angleInRadians);
            val s = sin(angleInRadians);

            return dst.apply {
                arrays[0] = c; arrays[1] = s; arrays[2] = 0f; arrays[3] = 0f
                arrays[4] = -s; arrays[5] = c; arrays[6] = 0f; arrays[7] = 0f
                arrays[8] = 0f; arrays[9] = 0f; arrays[10] = 1f; arrays[11] = 0f
            }
        }

        /**
         * Creates a 3-by-3 matrix which rotates around the x-axis by the given [angleInRadians].
         */
        fun rotationX(angleInRadians: Float, dst: Mat3f = Mat3f()): Mat3f {
            val c = cos(angleInRadians);
            val s = sin(angleInRadians);

            return dst.apply {
                arrays[0] = 1f; arrays[1] = 0f; arrays[2] = 0f; arrays[3] = 0f
                arrays[4] = 0f; arrays[5] = c; arrays[6] = s; arrays[7] = 0f
                arrays[8] = 0f; arrays[9] = -s; arrays[10] = c; arrays[11] = 0f
            }
        }

        /**
         * Creates a 3-by-3 matrix which rotates around the y-axis by the given [angleInRadians].
         */
        fun rotationY(angleInRadians: Float, dst: Mat3f = Mat3f()): Mat3f {
            val c = cos(angleInRadians);
            val s = sin(angleInRadians);

            return dst.apply {
                arrays[0] = c; arrays[1] = 0f; arrays[2] = -s; arrays[3] = 0f
                arrays[4] = 0f; arrays[5] = 1f; arrays[6] = 0f; arrays[7] = 0f
                arrays[8] = s; arrays[9] = 0f; arrays[10] = c; arrays[11] = 0f
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
                arrays[0] = v.x; arrays[1] = 0f; arrays[2] = 0f; arrays[3] = 0f
                arrays[4] = 0f; arrays[5] = v.y; arrays[6] = 0f; arrays[7] = 0f
                arrays[8] = 0f; arrays[9] = 0f; arrays[10] = 1f; arrays[11] = 0f
            }
        }

        /**
         * Creates a 3-by-3 matrix which scales in each dimension by the components of [v].
         */
        fun scaling3D(v: Vec3f, dst: Mat3f = Mat3f()): Mat3f {
            return dst.apply {
                arrays[0] = v[0]; arrays[1] = 0f; arrays[2] = 0f; arrays[3] = 0f
                arrays[4] = 0f; arrays[5] = v[1]; arrays[6] = 0f; arrays[7] = 0f
                arrays[8] = 0f; arrays[9] = 0f; arrays[10] = v[2]; arrays[11] = 0f
            }
        }

        /**
         * Creates a 3-by-3 matrix which scales uniformly in the X and Y dimensions by [s].
         */
        fun uniformScaling(s: Float, dst: Mat3f = Mat3f()): Mat3f {
            return dst.apply {
                arrays[0] = s; arrays[1] = 0f; arrays[2] = 0f; arrays[3] = 0f
                arrays[4] = 0f; arrays[5] = s; arrays[6] = 0f; arrays[7] = 0f
                arrays[8] = 0f; arrays[9] = 0f; arrays[10] = 1f; arrays[11] = 0f
            }
        }

        /**
         * Creates a 3-by-3 matrix which scales uniformly in each dimension by [s].
         */
        fun uniformScaling3D(s: Float, dst: Mat3f = Mat3f()): Mat3f {
            return dst.apply {
                arrays[0] = s; arrays[1] = 0f; arrays[2] = 0f; arrays[3] = 0f
                arrays[4] = 0f; arrays[5] = s; arrays[6] = 0f; arrays[7] = 0f
                arrays[8] = 0f; arrays[9] = 0f; arrays[10] = s; arrays[11] = 0f
            }
        }
    }

    /**
     * Gets a copy of the internal FloatArray representation of the matrix.
     * Modifying the returned array will not affect the original matrix.
     */
    fun toFloatArray(): FloatArray = arrays.copyOf() // Return a copy for safety

    /**
     * Sets the values of `this` from [v0] to [v8].
     */
    fun set(
        v0: Float, v1: Float, v2: Float,
        v3: Float, v4: Float, v5: Float,
        v6: Float, v7: Float, v8: Float,
    ): Mat3f = this.apply {
        arrays[0] = v0; arrays[1] = v1; arrays[2] = v2; arrays[3] = 0f;
        arrays[4] = v3; arrays[5] = v4; arrays[6] = v5; arrays[7] = 0f;
        arrays[8] = v6; arrays[9] = v7; arrays[10] = v8; arrays[11] = 0f;
    }

    /**
     * Negates `this` matrix.
     */
    fun negate(dst: Mat3f = Mat3f()): Mat3f {
        return dst.apply {
            arrays[0] = -this@Mat3f.arrays[0]; arrays[1] = -this@Mat3f.arrays[1]; arrays[2] = -this@Mat3f.arrays[2]; arrays[3] = 0f
            arrays[4] = -this@Mat3f.arrays[4]; arrays[5] = -this@Mat3f.arrays[5]; arrays[6] = -this@Mat3f.arrays[6]; arrays[7] = 0f
            arrays[8] = -this@Mat3f.arrays[8]; arrays[9] = -this@Mat3f.arrays[9]; arrays[10] = -this@Mat3f.arrays[10]; arrays[11] = 0f
        }
    }

    /**
     * Multiplies `this` matrix by the scalar [s].
     */
    fun multiplyScalar(s: Float, dst: Mat3f = Mat3f()): Mat3f {
        dst.arrays[0] = arrays[0] * s; dst.arrays[1] = arrays[1] * s; dst.arrays[2] = arrays[2] * s;arrays[3] = 0f
        dst.arrays[4] = arrays[4] * s; dst.arrays[5] = arrays[5] * s; dst.arrays[6] = arrays[6] * s;arrays[7] = 0f
        dst.arrays[8] = arrays[8] * s; dst.arrays[9] = arrays[9] * s; dst.arrays[10] = arrays[10] * s;arrays[11] = 0f

        return dst
    }

    /**
     * Divides `this` matrix by the scalar [s].
     */
    fun div(s: Float, dst: Mat3f = Mat3f()): Mat3f {
        dst.arrays[0] = arrays[0] / s; dst.arrays[1] = arrays[1] / s; dst.arrays[2] = arrays[2] / s; arrays[3] = 0f
        dst.arrays[4] = arrays[4] / s; dst.arrays[5] = arrays[5] / s; dst.arrays[6] = arrays[6] / s;arrays[7] = 0f
        dst.arrays[8] = arrays[8] / s; dst.arrays[9] = arrays[9] / s; dst.arrays[10] = arrays[10] / s;arrays[11] = 0f

        return dst
    }

    /**
     * Adds [other] to `this` matrix.
     */
    fun add(other: Mat3f, dst: Mat3f = Mat3f()): Mat3f {
        dst.arrays[0] = arrays[0] + other.arrays[0]; dst.arrays[1] = arrays[1] + other.arrays[1]; dst.arrays[2] = arrays[2] + other.arrays[2]; arrays[3] = 0f
        dst.arrays[4] = arrays[4] + other.arrays[4]; dst.arrays[5] = arrays[5] + other.arrays[5]; dst.arrays[6] = arrays[6] + other.arrays[6]; arrays[7] = 0f
        dst.arrays[8] = arrays[8] + other.arrays[8]; dst.arrays[9] = arrays[9] + other.arrays[9]; dst.arrays[10] = arrays[10] + other.arrays[10]; arrays[11] =
            0f

        return dst
    }

    inline fun plus(other: Mat3f, dst: Mat3f = Mat3f()) = add(other, dst)

    /**
     * Calculates the difference between `this` and [other].
     */
    fun diff(other: Mat3f, dst: Mat3f = Mat3f()): Mat3f {
        dst.arrays[0] = arrays[0] - other.arrays[0]; dst.arrays[1] = arrays[1] - other.arrays[1]; dst.arrays[2] = arrays[2] - other.arrays[2];arrays[3] = 0f
        dst.arrays[4] = arrays[4] - other.arrays[4]; dst.arrays[5] = arrays[5] - other.arrays[5]; dst.arrays[6] = arrays[6] - other.arrays[6];arrays[7] = 0f
        dst.arrays[8] = arrays[8] - other.arrays[8]; dst.arrays[9] = arrays[9] - other.arrays[9]; dst.arrays[10] = arrays[10] - other.arrays[10]; arrays[11] =
            0f

        return dst
    }

    inline fun minus(other: Mat3f, dst: Mat3f = Mat3f()) = diff(other, dst)

    /**
     * Copies `this` matrix.
     */
    fun copy(dst: Mat3f = Mat3f()): Mat3f {
        this.arrays.copyInto(dst.arrays)
        return dst
    }

    /**
     * Copies `this` matrix (alias for [copy]).
     */
    inline fun clone(dst: Mat3f = Mat3f()): Mat3f = copy(dst)

    /**
     * Checks if `this` matrix is approximately equal to [other].
     */
    fun equalsApproximately(other: Mat3f): Boolean {
        return abs(arrays[0] - other.arrays[0]) < EPSILON &&
                abs(arrays[1] - other.arrays[1]) < EPSILON &&
                abs(arrays[2] - other.arrays[2]) < EPSILON &&
                abs(arrays[4] - other.arrays[4]) < EPSILON &&
                abs(arrays[5] - other.arrays[5]) < EPSILON &&
                abs(arrays[6] - other.arrays[6]) < EPSILON &&
                abs(arrays[8] - other.arrays[8]) < EPSILON &&
                abs(arrays[9] - other.arrays[9]) < EPSILON &&
                abs(arrays[10] - other.arrays[10]) < EPSILON
    }

    /**
     * Checks if `this` matrix is exactly equal to [other].
     */
    override fun equals(other: Any?): Boolean {
        return other is Mat3f &&
                arrays[0] == other.arrays[0] &&
                arrays[1] == other.arrays[1] &&
                arrays[2] == other.arrays[2] &&
                arrays[4] == other.arrays[4] &&
                arrays[5] == other.arrays[5] &&
                arrays[6] == other.arrays[6] &&
                arrays[8] == other.arrays[8] &&
                arrays[9] == other.arrays[9] &&
                arrays[10] == other.arrays[10]
    }

    /**
     * Computes the hash code for `this` matrix.
     */
    override fun hashCode(): Int {
        var result = arrays.contentHashCode()
        // We only consider the relevant 9 elements for equality/hash code
        result = 31 * result + arrays[0].hashCode()
        result = 31 * result + arrays[1].hashCode()
        result = 31 * result + arrays[2].hashCode()
        result = 31 * result + arrays[4].hashCode()
        result = 31 * result + arrays[5].hashCode()
        result = 31 * result + arrays[6].hashCode()
        result = 31 * result + arrays[8].hashCode()
        result = 31 * result + arrays[9].hashCode()
        result = 31 * result + arrays[10].hashCode()
        return result
    }


    /**
     * Creates a 3-by-3 identity matrix.
     */
    fun identity(dst: Mat3f = Mat3f()): Mat3f {

        dst.arrays[0] = 1f; dst.arrays[1] = 0f; dst.arrays[2] = 0f;
        dst.arrays[4] = 0f; dst.arrays[5] = 1f; dst.arrays[6] = 0f;
        dst.arrays[8] = 0f; dst.arrays[9] = 0f; dst.arrays[10] = 1f;

        return dst
    }

    /**
     * Computes the transpose of `this` matrix.
     */
    fun transpose(dst: Mat3f = Mat3f()): Mat3f {
        if (dst === this) {
            // Perform in-place transpose
            var t: Float

            // 0 1 2
            // 4 5 6
            // 8 9 10

            t = arrays[1]; arrays[1] = arrays[4]; arrays[4] = t;
            t = arrays[2]; arrays[2] = arrays[8]; arrays[8] = t;
            t = arrays[6]; arrays[6] = arrays[9]; arrays[9] = t;

            return dst
        }

        val m00 = arrays[0 * 4 + 0]
        val m01 = arrays[0 * 4 + 1]
        val m02 = arrays[0 * 4 + 2]
        val m10 = arrays[1 * 4 + 0]
        val m11 = arrays[1 * 4 + 1]
        val m12 = arrays[1 * 4 + 2]
        val m20 = arrays[2 * 4 + 0]
        val m21 = arrays[2 * 4 + 1]
        val m22 = arrays[2 * 4 + 2]

        return dst.apply {
            arrays[0] = m00; arrays[1] = m10; arrays[2] = m20; arrays[3] = 0f
            arrays[4] = m01; arrays[5] = m11; arrays[6] = m21; arrays[7] = 0f
            arrays[8] = m02; arrays[9] = m12; arrays[10] = m22; arrays[11] = 0f
        }
    }

    /**
     * Computes the inverse of `this` matrix.
     * Returns identity if the matrix is not invertible.
     */
    fun inverse(dst: Mat3f = Mat3f()): Mat3f {

        val m00 = arrays[0 * 4 + 0]
        val m01 = arrays[0 * 4 + 1]
        val m02 = arrays[0 * 4 + 2]
        val m10 = arrays[1 * 4 + 0]
        val m11 = arrays[1 * 4 + 1]
        val m12 = arrays[1 * 4 + 2]
        val m20 = arrays[2 * 4 + 0]
        val m21 = arrays[2 * 4 + 1]
        val m22 = arrays[2 * 4 + 2]

        val b01 = m22 * m11 - m12 * m21
        val b11 = -m22 * m10 + m12 * m20
        val b21 = m21 * m10 - m11 * m20

        val det = m00 * b01 + m01 * b11 + m02 * b21
        if (det == 0f) {
            // Matrix is not invertible
            return dst.identity() // Or throw an exception, or return a special value
        }
        val invDet = 1 / det

        return dst.apply {
            arrays[0] = b01 * invDet; arrays[3] = 0f
            arrays[1] = (-m22 * m01 + m02 * m21) * invDet; arrays[7] = 0f
            arrays[2] = (m12 * m01 - m02 * m11) * invDet; arrays[11] = 0f
            arrays[4] = b11 * invDet
            arrays[5] = (m22 * m00 - m02 * m20) * invDet
            arrays[6] = (-m12 * m00 + m02 * m10) * invDet
            arrays[8] = b21 * invDet
            arrays[9] = (-m21 * m00 + m01 * m20) * invDet
            arrays[10] = (m11 * m00 - m01 * m10) * invDet
        }
    }

    /**
     * Computes the determinant of `this` matrix.
     */
    fun determinant(): Float {
        val m00 = arrays[0 * 4 + 0]
        val m01 = arrays[0 * 4 + 1]
        val m02 = arrays[0 * 4 + 2]
        val m10 = arrays[1 * 4 + 0]
        val m11 = arrays[1 * 4 + 1]
        val m12 = arrays[1 * 4 + 2]
        val m20 = arrays[2 * 4 + 0]
        val m21 = arrays[2 * 4 + 1]
        val m22 = arrays[2 * 4 + 2]

        return m00 * (m11 * m22 - m21 * m12) -
                m10 * (m01 * m22 - m21 * m02) +
                m20 * (m01 * m12 - m11 * m02)
    }

    /**
     * Computes the inverse of `this` matrix (alias for [inverse]).
     * Returns identity if the matrix is not invertible.
     */
    fun invert(dst: Mat3f = Mat3f()): Mat3f = inverse(dst)

    /**
     * Multiplies `this` matrix by [other] (`this` * [other]).
     */
    fun multiply(other: Mat3f, dst: Mat3f = Mat3f()): Mat3f {

        val a00 = arrays[0]
        val a01 = arrays[1]
        val a02 = arrays[2]
        val a10 = arrays[4 + 0]
        val a11 = arrays[4 + 1]
        val a12 = arrays[4 + 2]
        val a20 = arrays[8 + 0]
        val a21 = arrays[8 + 1]
        val a22 = arrays[8 + 2]
        val b00 = other.arrays[0]
        val b01 = other.arrays[1]
        val b02 = other.arrays[2]
        val b10 = other.arrays[4 + 0]
        val b11 = other.arrays[4 + 1]
        val b12 = other.arrays[4 + 2]
        val b20 = other.arrays[8 + 0]
        val b21 = other.arrays[8 + 1]
        val b22 = other.arrays[8 + 2]

        return dst.apply {
            arrays[0] = a00 * b00 + a10 * b01 + a20 * b02; arrays[3] = 0f
            arrays[1] = a01 * b00 + a11 * b01 + a21 * b02; arrays[7] = 0f
            arrays[2] = a02 * b00 + a12 * b01 + a22 * b02; arrays[11] = 0f
            arrays[4] = a00 * b10 + a10 * b11 + a20 * b12
            arrays[5] = a01 * b10 + a11 * b11 + a21 * b12
            arrays[6] = a02 * b10 + a12 * b11 + a22 * b12
            arrays[8] = a00 * b20 + a10 * b21 + a20 * b22
            arrays[9] = a01 * b20 + a11 * b21 + a21 * b22
            arrays[10] = a02 * b20 + a12 * b21 + a22 * b22
        }
    }

    /**
     * Multiplies `this` matrix by [other] (`this` * [other]) (alias for [multiply]).
     */
    inline fun mul(other: Mat3f, dst: Mat3f = Mat3f()): Mat3f = multiply(other, dst)

    /**
     * Creates a matrix copy of `this` with the translation component set to [v].
     */
    fun setTranslation(v: Vec2f, dst: Mat3f = identity()): Mat3f { // Use identity if dst is null

        if (this !== dst) {
            dst.arrays[0] = arrays[0];
            dst.arrays[1] = arrays[1];
            dst.arrays[2] = arrays[2];
            dst.arrays[4] = arrays[4];
            dst.arrays[5] = arrays[5];
            dst.arrays[6] = arrays[6];
        }
        dst.arrays[8] = v.x;
        dst.arrays[9] = v.y;
        dst.arrays[10] = 1f; // Ensure the bottom-right is 1 for translation
        return dst
    }

    /**
     * Gets the translation component of `this` matrix.
     */
    fun getTranslation(dst: Vec2f = Vec2f.create()): Vec2f {
        dst.x = arrays[8]
        dst.y = arrays[9]
        return dst
    }

    /**
     * Gets the specified [axis] (0=x, 1=y) of `this` matrix as a Vec2.
     */
    fun getAxis(axis: Int, dst: Vec2f = Vec2f.create()): Vec2f {
        val off = axis * 4
        dst.x = arrays[off + 0]
        dst.y = arrays[off + 1]
        return dst
    }

    /**
     * Creates a matrix copy of `this` with the specified [axis] (0=x, 1=y) set to [v].
     */
    fun setAxis(v: Vec2f, axis: Int, dst: Mat3f = Mat3f()): Mat3f {
        val newDst = if (dst === this) this else copy(dst)

        val off = axis * 4
        newDst.arrays[off + 0] = v.x
        newDst.arrays[off + 1] = v.y
        return newDst
    }

    /**
     * Gets the 2D scaling component of `this` matrix.
     */
    fun getScaling(dst: Vec2f = Vec2f.create()): Vec2f {

        val xx = arrays[0]
        val xy = arrays[1]
        val yx = arrays[4]
        val yy = arrays[5]

        dst.x = sqrt(xx * xx + xy * xy)
        dst.y = sqrt(yx * yx + yy * yy)

        return dst
    }


    /**
     * Gets the 3D scaling component of `this` matrix.
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
     * Translates `this` matrix by [v].
     */
    fun translate(v: Vec2f, dst: Mat3f = Mat3f()): Mat3f {

        val v0 = v.x
        val v1 = v.y

        val m00 = arrays[0]
        val m01 = arrays[1]
        val m02 = arrays[2]
        val m10 = arrays[1 * 4 + 0]
        val m11 = arrays[1 * 4 + 1]
        val m12 = arrays[1 * 4 + 2]
        val m20 = arrays[2 * 4 + 0]
        val m21 = arrays[2 * 4 + 1]
        val m22 = arrays[2 * 4 + 2]

        if (this !== dst) {
            dst.arrays[0] = m00
            dst.arrays[1] = m01
            dst.arrays[2] = m02
            dst.arrays[4] = m10
            dst.arrays[5] = m11
            dst.arrays[6] = m12
        }

        dst.arrays[8] = m00 * v0 + m10 * v1 + m20
        dst.arrays[9] = m01 * v0 + m11 * v1 + m21
        dst.arrays[10] = m02 * v0 + m12 * v1 + m22

        return dst
    }

    /**
     * Rotates `this` matrix by [angleInRadians] around the Z axis.
     */
    fun rotate(angleInRadians: Float, dst: Mat3f = Mat3f()): Mat3f {

        val m00 = arrays[0 * 4 + 0]
        val m01 = arrays[0 * 4 + 1]
        val m02 = arrays[0 * 4 + 2]
        val m10 = arrays[1 * 4 + 0]
        val m11 = arrays[1 * 4 + 1]
        val m12 = arrays[1 * 4 + 2]
        val c = cos(angleInRadians)
        val s = sin(angleInRadians)

        dst.arrays[0] = c * m00 + s * m10
        dst.arrays[1] = c * m01 + s * m11
        dst.arrays[2] = c * m02 + s * m12; dst.arrays[3] = 0f

        dst.arrays[4] = c * m10 - s * m00
        dst.arrays[5] = c * m11 - s * m01
        dst.arrays[6] = c * m12 - s * m02; dst.arrays[7] = 0f


        if (this !== dst) {
            dst.arrays[8] = arrays[8];
            dst.arrays[9] = arrays[9];
            dst.arrays[10] = arrays[10]; dst.arrays[11] = 0f
        }

        return dst
    }

    /**
     * Rotates `this` matrix by [angleInRadians] around the X axis.
     */
    fun rotateX(angleInRadians: Float, dst: Mat3f = Mat3f()): Mat3f {

        val m10 = arrays[4]
        val m11 = arrays[5]
        val m12 = arrays[6]
        val m20 = arrays[8]
        val m21 = arrays[9]
        val m22 = arrays[10]

        val c = cos(angleInRadians)
        val s = sin(angleInRadians)

        dst.arrays[4] = c * m10 + s * m20; dst.arrays[7] = 0f
        dst.arrays[5] = c * m11 + s * m21
        dst.arrays[6] = c * m12 + s * m22
        dst.arrays[8] = c * m20 - s * m10; dst.arrays[11] = 0f
        dst.arrays[9] = c * m21 - s * m11
        dst.arrays[10] = c * m22 - s * m12

        if (this !== dst) {
            dst.arrays[0] = arrays[0];
            dst.arrays[1] = arrays[1];
            dst.arrays[2] = arrays[2]; dst.arrays[3] = 0f
        }

        return dst
    }

    /**
     * Rotates `this` matrix by [angleInRadians] around the Y axis.
     */
    fun rotateY(angleInRadians: Float, dst: Mat3f = Mat3f()): Mat3f {

        val m00 = arrays[0 * 4 + 0]
        val m01 = arrays[0 * 4 + 1]
        val m02 = arrays[0 * 4 + 2]
        val m20 = arrays[2 * 4 + 0]
        val m21 = arrays[2 * 4 + 1]
        val m22 = arrays[2 * 4 + 2]
        val c = cos(angleInRadians)
        val s = sin(angleInRadians)

        dst.arrays[0] = c * m00 - s * m20; dst.arrays[3] = 0f
        dst.arrays[1] = c * m01 - s * m21
        dst.arrays[2] = c * m02 - s * m22
        dst.arrays[8] = c * m20 + s * m00; dst.arrays[11] = 0f
        dst.arrays[9] = c * m21 + s * m01
        dst.arrays[10] = c * m22 + s * m02

        if (this !== dst) {
            dst.arrays[4] = arrays[4];
            dst.arrays[5] = arrays[5];
            dst.arrays[6] = arrays[6]; dst.arrays[7] = 0f
        }

        return dst
    }

    /**
     * Rotates `this` matrix by [angleInRadians] around the Z axis (alias for [rotate]).
     */
    fun rotateZ(angleInRadians: Float, dst: Mat3f = Mat3f()): Mat3f = rotate(angleInRadians, dst)

    /**
     * Scales the X and Y dimensions of `this` matrix by the components of [v].
     */
    fun scale(v: Vec2f, dst: Mat3f = Mat3f()): Mat3f {

        val v0 = v.x
        val v1 = v.y

        dst.arrays[0] = v0 * arrays[0 * 4 + 0]; dst.arrays[3] = 0f
        dst.arrays[1] = v0 * arrays[0 * 4 + 1]
        dst.arrays[2] = v0 * arrays[0 * 4 + 2]

        dst.arrays[4] = v1 * arrays[1 * 4 + 0]; dst.arrays[7] = 0f
        dst.arrays[5] = v1 * arrays[1 * 4 + 1]
        dst.arrays[6] = v1 * arrays[1 * 4 + 2]

        if (this !== dst) {
            dst.arrays[8] = arrays[8];
            dst.arrays[9] = arrays[9];
            dst.arrays[10] = arrays[10]; dst.arrays[11] = 0f
        }

        return dst
    }

    /**
     * Scales each dimension of `this` matrix by the components of [v].
     */
    fun scale3D(v: Vec3f, dst: Mat3f = Mat3f()): Mat3f {

        val v0 = v[0]
        val v1 = v[1]
        val v2 = v[2]

        dst.arrays[0] = v0 * arrays[0 * 4 + 0]; dst.arrays[3] = 0f
        dst.arrays[1] = v0 * arrays[0 * 4 + 1]
        dst.arrays[2] = v0 * arrays[0 * 4 + 2]

        dst.arrays[4] = v1 * arrays[1 * 4 + 0]; dst.arrays[7] = 0f
        dst.arrays[5] = v1 * arrays[1 * 4 + 1]
        dst.arrays[6] = v1 * arrays[1 * 4 + 2]

        dst.arrays[8] = v2 * arrays[2 * 4 + 0]; dst.arrays[11] = 0f
        dst.arrays[9] = v2 * arrays[2 * 4 + 1]
        dst.arrays[10] = v2 * arrays[2 * 4 + 2]

        return dst
    }

    /**
     * Scales the X and Y dimensions of `this` matrix uniformly by [s].
     */
    fun uniformScale(s: Float, dst: Mat3f = Mat3f()): Mat3f {

        dst.arrays[0] = s * arrays[0 * 4 + 0]; dst.arrays[3] = 0f
        dst.arrays[1] = s * arrays[0 * 4 + 1]
        dst.arrays[2] = s * arrays[0 * 4 + 2]

        dst.arrays[4] = s * arrays[1 * 4 + 0]; dst.arrays[7] = 0f
        dst.arrays[5] = s * arrays[1 * 4 + 1]
        dst.arrays[6] = s * arrays[1 * 4 + 2]

        if (this !== dst) {
            dst.arrays[8] = arrays[8];
            dst.arrays[9] = arrays[9];
            dst.arrays[10] = arrays[10]; dst.arrays[11] = 0f
        }

        return dst
    }

    /**
     * Scales each dimension of `this` matrix uniformly by [s].
     */
    fun uniformScale3D(s: Float, dst: Mat3f = Mat3f()): Mat3f {

        dst.arrays[0] = s * arrays[0 * 4 + 0]; dst.arrays[3] = 0f
        dst.arrays[1] = s * arrays[0 * 4 + 1]
        dst.arrays[2] = s * arrays[0 * 4 + 2]

        dst.arrays[4] = s * arrays[1 * 4 + 0]; dst.arrays[7] = 0f
        dst.arrays[5] = s * arrays[1 * 4 + 1]
        dst.arrays[6] = s * arrays[1 * 4 + 2]

        dst.arrays[8] = s * arrays[2 * 4 + 0]; dst.arrays[11] = 0f
        dst.arrays[9] = s * arrays[2 * 4 + 1]
        dst.arrays[10] = s * arrays[2 * 4 + 2]

        return dst
    }
}

