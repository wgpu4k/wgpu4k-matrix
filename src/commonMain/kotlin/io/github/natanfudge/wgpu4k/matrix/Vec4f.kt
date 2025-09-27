@file:Suppress("NOTHING_TO_INLINE")

package io.github.natanfudge.wgpu4k.matrix

import kotlinx.serialization.Serializable
import kotlin.math.*

/**
 * Represents a 4-dimensional vector.
 */
@Serializable
data class Vec4f(
    var x: Float,
    var y: Float,
    var z: Float,
    var w: Float,
) {
    constructor() : this(0f, 0f, 0f, 0f)

    companion object {
        // 4 * 4 bytes
        const val SIZE_BYTES = 16u
        const val ELEMENT_COUNT = 4u

        /**
         * Creates a vec4 with initial values [x], [y], [z], and [w].
         */
        fun create(x: Float = 0.0f, y: Float = 0.0f, z: Float = 0.0f, w: Float = 0.0f): Vec4f {
            return Vec4f(x, y, z, w)
        }

        /**
         * Creates a vec4 with initial values [x], [y], [z], and [w]. (same as create)
         */
        fun fromValues(x: Float = 0.0f, y: Float = 0.0f, z: Float = 0.0f, w: Float = 0.0f): Vec4f {
            return Vec4f(x, y, z, w)
        }

        /**
         * Sets the components of [dst] to [x], [y],  [z] and [w].
         */
        fun set(x: Float, y: Float, z: Float, w: Float, dst: Vec4f = Vec4f()): Vec4f {
            dst.x = x
            dst.y = y
            dst.z = z
            dst.w = w
            return dst
        }
    }

    fun toArray() = floatArrayOf(x, y, z, w)

    inline operator fun plus(other: Vec4f) = add(other)
    inline operator fun minus(other: Vec4f) = subtract(other)
    inline operator fun times(scalar: Float) = mulScalar(scalar)
    /**
     * Component-wise (Hadamard) product of `this` and [other].
     * Important note: NOT cross product, and NOT dot product!
     */
    operator fun times(other: Vec4f) = Vec4f(this.x * other.x, this.y * other.y, this.z * other.z, this.w * other.w)
    inline operator fun div(scalar: Float) = divScalar(scalar)
    inline operator fun unaryMinus() = negate()

    /**
     * Allows accessing components using array syntax (e.g., vec[0]).
     */
    operator fun get(index: Int): Float {
        return when (index) {
            0 -> x
            1 -> y
            2 -> z
            3 -> w
            else -> throw IndexOutOfBoundsException("Index $index is out of bounds for Vec4")
        }
    }

    /**
     * Allows setting components using array syntax (e.g., vec[0] = 1.0f).
     */
    operator fun set(index: Int, value: Float) {
        when (index) {
            0 -> x = value
            1 -> y = value
            2 -> z = value
            3 -> w = value
            else -> throw IndexOutOfBoundsException("Index $index is out of bounds for Vec4")
        }
    }

    /**
     * Computes the length of `this`.
     */
    val length: Float
        get() = sqrt(this.x * this.x + this.y * this.y + this.z * this.z + this.w * this.w)
    inline val norm: Float get() = length

    /**
     * Computes the length of `this`. (Alias for length)
     */
    val len: Float
        get() = length

    /**
     * Computes the square of the length of `this`.
     */
    val lengthSq: Float
        get() = this.x * this.x + this.y * this.y + this.z * this.z + this.w * this.w

    /**
     * Computes the square of the length of `this`. (Alias for lengthSq)
     */
    val lenSq: Float
        get() = lengthSq

    /**
     * Sets this vector to the zero vec4
     */
    fun setZero() {
        zero(this)
    }

    /**
     * Applies Math.ceil to each component of `this`.
     */
    fun ceil(dst: Vec4f = Vec4f()): Vec4f {
        dst.x = ceil(this.x)
        dst.y = ceil(this.y)
        dst.z = ceil(this.z)
        dst.w = ceil(this.w)
        return dst
    }

    /**
     * Applies Math.floor to each component of `this`.
     */
    fun floor(dst: Vec4f = Vec4f()): Vec4f {
        dst.x = floor(this.x)
        dst.y = floor(this.y)
        dst.z = floor(this.z)
        dst.w = floor(this.w)
        return dst
    }

    /**
     * Applies Math.round to each component of `this`.
     */
    fun round(dst: Vec4f = Vec4f()): Vec4f {
        dst.x = round(this.x)
        dst.y = round(this.y)
        dst.z = round(this.z)
        dst.w = round(this.w)
        return dst
    }

    /**
     * Computes the component-wise inverse (1/x) of `this`.
     */
    fun inverse(dst: Vec4f = Vec4f()): Vec4f {
        dst.x = 1.0f / this.x
        dst.y = 1.0f / this.y
        dst.z = 1.0f / this.z
        dst.w = 1.0f / this.w
        return dst
    }

    /**
     * Computes the component-wise inverse (1/x) of `this`. (Alias for inverse)
     */
    fun invert(dst: Vec4f = Vec4f()): Vec4f = inverse(dst)

    /**
     * Normalizes `this` (divides by its length).
     * Returns a zero vector if the length is close to zero.
     */
    fun normalize(dst: Vec4f = Vec4f()): Vec4f {
        val l = this.length
        if (l > EPSILON) {
            dst.x = this.x / l
            dst.y = this.y / l
            dst.z = this.z / l
            dst.w = this.w / l
        } else {
            dst.x = 0.0f
            dst.y = 0.0f
            dst.z = 0.0f
            dst.w = 0.0f
        }
        return dst
    }

    inline fun normalized(dst: Vec4f = Vec4f()): Vec4f = normalize(dst)

    /**
     * Negates `this`.
     */
    fun negate(dst: Vec4f = Vec4f()): Vec4f {
        dst.x = -this.x
        dst.y = -this.y
        dst.z = -this.z
        dst.w = -this.w
        return dst
    }

    /**
     * Copies the components of `this`.
     */
    fun copy(x: Float = this.x, y: Float = this.y, z: Float = this.z, w: Float = this.w, dst: Vec4f = Vec4f()): Vec4f {
        dst.x = x
        dst.y = y
        dst.z = z
        dst.w = w
        return dst
    }

    /**
     * Sets `dst` to `v[axis] = value` and returns it
     */
    fun copy(axis: Int, value: Float, dst: Vec4f = Vec4f()): Vec4f = when (axis) {
        0 -> copy(x = value, dst = dst)
        1 -> copy(y = value, dst = dst)
        2 -> copy(z = value, dst = dst)
        3 -> copy(w = value, dst = dst)
        else -> throw IndexOutOfBoundsException("Invalid axis index: $axis")
    }

    /**
     * Creates a copy of `this`. (Alias for copy)
     */
    fun clone(x: Float = this.x, y: Float = this.y, z: Float = this.z, w: Float = this.w, dst: Vec4f = Vec4f()): Vec4f = copy(x, y, z, w, dst)

    /**
     * Sets the components of `this` to zero.
     */
    fun zero(dst: Vec4f = Vec4f()): Vec4f {
        dst.x = 0.0f
        dst.y = 0.0f
        dst.z = 0.0f
        dst.w = 0.0f
        return dst
    }

    /**
     * Adds [other] to `this`.
     */
    fun add(other: Vec4f, dst: Vec4f = Vec4f()): Vec4f {
        dst.x = this.x + other.x
        dst.y = this.y + other.y
        dst.z = this.z + other.z
        dst.w = this.w + other.w
        return dst
    }

    /**
     * Subtracts [other] from `this`.
     */
    fun subtract(other: Vec4f, dst: Vec4f = Vec4f()): Vec4f {
        dst.x = this.x - other.x
        dst.y = this.y - other.y
        dst.z = this.z - other.z
        dst.w = this.w - other.w
        return dst
    }

    /**
     * Subtracts [other] from `this`. (Alias for subtract)
     */
    fun sub(other: Vec4f, dst: Vec4f = Vec4f()): Vec4f = subtract(other, dst)

    /**
     */
    fun max(other: Vec4f, dst: Vec4f = Vec4f()): Vec4f {
        dst.x = max(this.x, other.x)
        dst.y = max(this.y, other.y)
        dst.z = max(this.z, other.z)
        dst.w = max(this.w, other.w)
        return dst
    }

    /**
     * Computes the component-wise minimum of `this` and [other].
     */
    fun min(other: Vec4f, dst: Vec4f = Vec4f()): Vec4f {
        dst.x = min(this.x, other.x)
        dst.y = min(this.y, other.y)
        dst.z = min(this.z, other.z)
        dst.w = min(this.w, other.w)
        return dst
    }

    /**
     * Multiplies `this` by scalar [k].
     */
    fun mulScalar(k: Float, dst: Vec4f = Vec4f()): Vec4f {
        dst.x = this.x * k
        dst.y = this.y * k
        dst.z = this.z * k
        dst.w = this.w * k
        return dst
    }

    /**
     * Multiples this vector by [k]
     */
    inline fun mul(k: Float, dst: Vec4f = Vec4f()) = mulScalar(k, dst)

    /**
     * Multiplies `this` by scalar [k]. (Alias for mulScalar)
     */
    fun scale(k: Float, dst: Vec4f = Vec4f()): Vec4f = mulScalar(k, dst)

    /**
     * Divides `this` by scalar [k].
     */
    fun divScalar(k: Float, dst: Vec4f = Vec4f()): Vec4f {
        dst.x = this.x / k
        dst.y = this.y / k
        dst.z = this.z / k
        dst.w = this.w / k
        return dst
    }

    /**
     * Divides this vector by [k]
     */
    inline fun div(k: Float, dst: Vec4f = Vec4f()) = divScalar(k, dst)

    /**
     * Computes the dot product of `this` and [other].
     */
    infix fun dot(other: Vec4f): Float {
        return (this.x * other.x) + (this.y * other.y) + (this.z * other.z) + (this.w * other.w)
    }

    /**
     * Computes the distance between `this` and [other].
     */
    fun distance(other: Vec4f): Float {
        val dx = this.x - other.x
        val dy = this.y - other.y
        val dz = this.z - other.z
        val dw = this.w - other.w
        return sqrt(dx * dx + dy * dy + dz * dz + dw * dw)
    }

    /**
     * Computes the distance between `this` and [other]. (Alias for distance)
     */
    fun dist(other: Vec4f): Float = distance(other)

    /**
     * Computes the square of the distance between `this` and [other].
     */
    fun distanceSq(other: Vec4f): Float {
        val dx = this.x - other.x
        val dy = this.y - other.y
        val dz = this.z - other.z
        val dw = this.w - other.w
        return dx * dx + dy * dy + dz * dz + dw * dw
    }

    /**
     * Computes the square of the distance between `this` and [other]. (Alias for distanceSq)
     */
    fun distSq(other: Vec4f): Float = distanceSq(other)

    /**
     * Multiplies `this` by [other] component-wise.
     */
    fun multiply(other: Vec4f, dst: Vec4f = Vec4f()): Vec4f {
        dst.x = this.x * other.x
        dst.y = this.y * other.y
        dst.z = this.z * other.z
        dst.w = this.w * other.w
        return dst
    }

    /**
     * Multiplies `this` by [other] component-wise. (Alias for multiply)
     */
    fun mul(other: Vec4f, dst: Vec4f = Vec4f()): Vec4f = multiply(other, dst)

    /**
     * Divides `this` by [other] component-wise.
     */
    fun divide(other: Vec4f, dst: Vec4f = Vec4f()): Vec4f {
        dst.x = this.x / other.x
        dst.y = this.y / other.y
        dst.z = this.z / other.z
        dst.w = this.w / other.w
        return dst
    }

    /**
     * Divides `this` by [other] component-wise. (Alias for divide)
     */
    fun div(other: Vec4f, dst: Vec4f = Vec4f()): Vec4f = divide(other, dst)

    /**
     * Transforms `this` by the 4x4 matrix [m].
     * (m[0]=m00, m[1]=m10, m[2]=m20, m[3]=m30, m[4]=m01, m[5]=m11, ...)
     */
    fun transformMat4(m: Mat4f, dst: Vec4f = Vec4f()): Vec4f {
        val x = this.x
        val y = this.y
        val z = this.z
        val w = this.w

        dst.x = m[0] * x + m[4] * y + m[8] * z + m[12] * w
        dst.y = m[1] * x + m[5] * y + m[9] * z + m[13] * w
        dst.z = m[2] * x + m[6] * y + m[10] * z + m[14] * w
        dst.w = m[3] * x + m[7] * y + m[11] * z + m[15] * w

        return dst
    }

    /**
     * Sets the length of `this` to [length].
     */
    fun setLength(length: Float, dst: Vec4f = Vec4f()): Vec4f {
        this.normalize(dst)
        return dst.mulScalar(length, dst)
    }

    /**
     * Ensures `this` is not longer than [maxLen].
     * @return The vector, shortened to [maxLen] if its original length was greater, otherwise a copy of `this`.
     */
    fun truncate(maxLen: Float, dst: Vec4f = Vec4f()): Vec4f {
        if (this.length > maxLen) {
            return this.setLength(maxLen, dst)
        }
        return this.copy(dst = dst)
    }

    /**
     * Computes the midpoint between `this` and [other].
     */
    fun midpoint(other: Vec4f, dst: Vec4f = Vec4f()): Vec4f {
        return this.lerp(other, 0.5f, dst)
    }

    /**
     * Clamp each element of `this` between [min] and [max].
     */
    fun clamp(min: Float = 0.0f, max: Float = 1.0f, dst: Vec4f = Vec4f()): Vec4f {
        dst.x = max(min, min(max, this.x))
        dst.y = max(min, min(max, this.y))
        dst.z = max(min, min(max, this.z))
        dst.w = max(min, min(max, this.w))
        return dst
    }

    /**
     * Adds [other] scaled by [scale] to `this`.
     */
    fun addScaled(other: Vec4f, scale: Float, dst: Vec4f = Vec4f()): Vec4f {
        dst.x = this.x + other.x * scale
        dst.y = this.y + other.y * scale
        dst.z = this.z + other.z * scale
        dst.w = this.w + other.w * scale
        return dst
    }

    /**
     * Checks if `this` is approximately equal to [other] within [epsilon].
     */
    fun equalsApproximately(other: Vec4f, epsilon: Float = EPSILON): Boolean {
        return abs(this.x - other.x) < epsilon &&
                abs(this.y - other.y) < epsilon &&
                abs(this.z - other.z) < epsilon &&
                abs(this.w - other.w) < epsilon
    }

    /**
     * Performs linear interpolation between `this` and [other] using coefficient [t].
     * Calculates `this` + [t] * ([other] - `this`).
     */
    fun lerp(other: Vec4f, t: Float, dst: Vec4f = Vec4f()): Vec4f {
        dst.x = this.x + t * (other.x - this.x)
        dst.y = this.y + t * (other.y - this.y)
        dst.z = this.z + t * (other.z - this.z)
        dst.w = this.w + t * (other.w - this.w)
        return dst
    }

    /**
     * Performs linear interpolation between `this` and [other] using coefficient vector [t].
     * Calculates `this` + [t] * ([other] - `this`) component-wise.
     */
    fun lerpV(other: Vec4f, t: Vec4f, dst: Vec4f = Vec4f()): Vec4f {
        dst.x = this.x + t.x * (other.x - this.x)
        dst.y = this.y + t.y * (other.y - this.y)
        dst.z = this.z + t.z * (other.z - this.z)
        dst.w = this.w + t.w * (other.w - this.w)
        return dst
    }

    /**
     * Sets the components of `this` to [x], [y], [z], and [w].
     */
    fun set(x: Float, y: Float, z: Float, w: Float): Vec4f {
        this.x = x
        this.y = y
        this.z = z
        this.w = w
        return this
    }

    /**
     * Sets the values of `this` equal to the values of [other].
     */
    fun set(other: Vec4f): Vec4f {
        this.x = other.x
        this.y = other.y
        this.z = other.z
        this.w = other.w
        return this
    }

    /**
     * @param round if true, floating point values will look nicer by doing some rounding operations. The default is true.
     */
    fun toString(round: Boolean): String = if (round) "(${x.ns},${y.ns},${z.ns},${w.ns})" else "($x,$y,$z,$w)"
    override fun toString(): String = toString(round = true)
}
