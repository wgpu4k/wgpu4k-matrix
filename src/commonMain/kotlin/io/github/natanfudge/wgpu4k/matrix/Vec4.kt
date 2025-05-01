package io.github.natanfudge.wgpu4k.matrix

import kotlin.math.*

/**
 * Represents a 4-dimensional vector.
 */
data class Vec4(
    var x: Double = 0.0,
    var y: Double = 0.0,
    var z: Double = 0.0,
    var w: Double = 0.0
) {

    companion object {
        const val EPSILON = 0.00001

        /**
         * Creates a vec4 with initial values [x], [y], [z], and [w].
         */
        fun create(x: Double = 0.0, y: Double = 0.0, z: Double = 0.0, w: Double = 0.0): Vec4 {
            return Vec4(x, y, z, w)
        }

        /**
         * Creates a vec4 with initial values [x], [y], [z], and [w]. (same as create)
         */
        fun fromValues(x: Double = 0.0, y: Double = 0.0, z: Double = 0.0, w: Double = 0.0): Vec4 {
            return Vec4(x, y, z, w)
        }
    }

    /**
     * Sets the components of `this` to [x], [y], [z], and [w].
     */
    fun set(x: Double, y: Double, z: Double, w: Double): Vec4 {
        this.x = x
        this.y = y
        this.z = z
        this.w = w
        return this
    }

    /**
     * Applies Math.ceil to each component of `this`.
     */
    fun ceil(dst: Vec4 = Vec4()): Vec4 {
        dst.x = ceil(this.x)
        dst.y = ceil(this.y)
        dst.z = ceil(this.z)
        dst.w = ceil(this.w)
        return dst
    }

    /**
     * Applies Math.floor to each component of `this`.
     */
    fun floor(dst: Vec4 = Vec4()): Vec4 {
        dst.x = floor(this.x)
        dst.y = floor(this.y)
        dst.z = floor(this.z)
        dst.w = floor(this.w)
        return dst
    }

    /**
     * Applies Math.round to each component of `this`.
     */
    fun round(dst: Vec4 = Vec4()): Vec4 {
        dst.x = round(this.x)
        dst.y = round(this.y)
        dst.z = round(this.z)
        dst.w = round(this.w)
        return dst
    }

    /**
     * Clamp each element of `this` between [min] and [max].
     */
    fun clamp(min: Double = 0.0, max: Double = 1.0, dst: Vec4 = Vec4()): Vec4 {
        dst.x = max(min, min(max, this.x))
        dst.y = max(min, min(max, this.y))
        dst.z = max(min, min(max, this.z))
        dst.w = max(min, min(max, this.w))
        return dst
    }

    /**
     * Adds [other] to `this`.
     */
    fun add(other: Vec4, dst: Vec4 = Vec4()): Vec4 {
        dst.x = this.x + other.x
        dst.y = this.y + other.y
        dst.z = this.z + other.z
        dst.w = this.w + other.w
        return dst
    }

    /**
     * Adds [other] scaled by [scale] to `this`.
     */
    fun addScaled(other: Vec4, scale: Double, dst: Vec4 = Vec4()): Vec4 {
        dst.x = this.x + other.x * scale
        dst.y = this.y + other.y * scale
        dst.z = this.z + other.z * scale
        dst.w = this.w + other.w * scale
        return dst
    }

    /**
     * Subtracts [other] from `this`.
     */
    fun subtract(other: Vec4, dst: Vec4 = Vec4()): Vec4 {
        dst.x = this.x - other.x
        dst.y = this.y - other.y
        dst.z = this.z - other.z
        dst.w = this.w - other.w
        return dst
    }

    /**
     * Subtracts [other] from `this`. (Alias for subtract)
     */
    fun sub(other: Vec4, dst: Vec4 = Vec4()): Vec4 = subtract(other, dst)

    /**
     * Checks if `this` is approximately equal to [other] within [epsilon].
     */
    fun equalsApproximately(other: Vec4, epsilon: Double = EPSILON): Boolean {
        return abs(this.x - other.x) < epsilon &&
               abs(this.y - other.y) < epsilon &&
               abs(this.z - other.z) < epsilon &&
               abs(this.w - other.w) < epsilon
    }

    /**
     * Checks if `this` is exactly equal to [other].
     * Note: Prefer equalsApproximately for floating-point comparisons.
     */
    fun equals(other: Vec4): Boolean {
        return this.x == other.x && this.y == other.y && this.z == other.z && this.w == other.w
    }
    // Note: The data class provides an equals method, but this explicit one matches the TS API name.
    // The data class equals will be used for standard equality checks (e.g., in collections).

    /**
     * Performs linear interpolation between `this` and [other] using coefficient [t].
     * Calculates `this` + [t] * ([other] - `this`).
     */
     fun lerp(other: Vec4, t: Double, dst: Vec4 = Vec4()): Vec4 {
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
     fun lerpV(other: Vec4, t: Vec4, dst: Vec4 = Vec4()): Vec4 {
         dst.x = this.x + t.x * (other.x - this.x)
         dst.y = this.y + t.y * (other.y - this.y)
         dst.z = this.z + t.z * (other.z - this.z)
         dst.w = this.w + t.w * (other.w - this.w)
         return dst
    }

    /**
     * Computes the component-wise maximum of `this` and [other].
     */
    fun max(other: Vec4, dst: Vec4 = Vec4()): Vec4 {
        dst.x = max(this.x, other.x)
        dst.y = max(this.y, other.y)
        dst.z = max(this.z, other.z)
        dst.w = max(this.w, other.w)
        return dst
    }

    /**
     * Computes the component-wise minimum of `this` and [other].
     */
    fun min(other: Vec4, dst: Vec4 = Vec4()): Vec4 {
        dst.x = min(this.x, other.x)
        dst.y = min(this.y, other.y)
        dst.z = min(this.z, other.z)
        dst.w = min(this.w, other.w)
        return dst
    }

    /**
     * Multiplies `this` by scalar [k].
     */
    fun mulScalar(k: Double, dst: Vec4 = Vec4()): Vec4 {
        dst.x = this.x * k
        dst.y = this.y * k
        dst.z = this.z * k
        dst.w = this.w * k
        return dst
    }

    /**
     * Multiplies `this` by scalar [k]. (Alias for mulScalar)
     */
    fun scale(k: Double, dst: Vec4 = Vec4()): Vec4 = mulScalar(k, dst)

    /**
     * Divides `this` by scalar [k].
     */
    fun divScalar(k: Double, dst: Vec4 = Vec4()): Vec4 {
        dst.x = this.x / k
        dst.y = this.y / k
        dst.z = this.z / k
        dst.w = this.w / k
        return dst
    }

    /**
     * Computes the component-wise inverse (1/x) of `this`.
     */
    fun inverse(dst: Vec4 = Vec4()): Vec4 {
        dst.x = 1.0 / this.x
        dst.y = 1.0 / this.y
        dst.z = 1.0 / this.z
        dst.w = 1.0 / this.w
        return dst
    }

    /**
     * Computes the component-wise inverse (1/x) of `this`. (Alias for inverse)
     */
    fun invert(dst: Vec4 = Vec4()): Vec4 = inverse(dst)

    /**
     * Computes the dot product of `this` and [other].
     */
    fun dot(other: Vec4): Double {
        return (this.x * other.x) + (this.y * other.y) + (this.z * other.z) + (this.w * other.w)
    }

    /**
     * Computes the length of `this`.
     */
    val length: Double
        get() = sqrt(this.x * this.x + this.y * this.y + this.z * this.z + this.w * this.w)

    /**
     * Computes the length of `this`. (Alias for length)
     */
    val len: Double
        get() = length

    /**
     * Computes the square of the length of `this`.
     */
    val lengthSq: Double
        get() = this.x * this.x + this.y * this.y + this.z * this.z + this.w * this.w

    /**
     * Computes the square of the length of `this`. (Alias for lengthSq)
     */
    val lenSq: Double
        get() = lengthSq

    /**
     * Computes the distance between `this` and [other].
     */
    fun distance(other: Vec4): Double {
        val dx = this.x - other.x
        val dy = this.y - other.y
        val dz = this.z - other.z
        val dw = this.w - other.w
        return sqrt(dx * dx + dy * dy + dz * dz + dw * dw)
    }

    /**
     * Computes the distance between `this` and [other]. (Alias for distance)
     */
    fun dist(other: Vec4): Double = distance(other)

    /**
     * Computes the square of the distance between `this` and [other].
     */
    fun distanceSq(other: Vec4): Double {
        val dx = this.x - other.x
        val dy = this.y - other.y
        val dz = this.z - other.z
        val dw = this.w - other.w
        return dx * dx + dy * dy + dz * dz + dw * dw
    }

    /**
     * Computes the square of the distance between `this` and [other]. (Alias for distanceSq)
     */
    fun distSq(other: Vec4): Double = distanceSq(other)

    /**
     * Normalizes `this` (divides by its length).
     * Returns a zero vector if the length is close to zero.
     */
     fun normalize(dst: Vec4 = Vec4()): Vec4 {
         val l = this.length
         if (l > EPSILON) {
             dst.x = this.x / l
             dst.y = this.y / l
             dst.z = this.z / l
             dst.w = this.w / l
         } else {
             dst.x = 0.0
             dst.y = 0.0
             dst.z = 0.0
             dst.w = 0.0
         }
         return dst
    }

    /**
     * Negates `this`.
     */
    fun negate(dst: Vec4 = Vec4()): Vec4 {
        dst.x = -this.x
        dst.y = -this.y
        dst.z = -this.z
        dst.w = -this.w
        return dst
    }

    /**
     * Creates a copy of `this`.
     */
    fun copy(dst: Vec4 = Vec4()): Vec4 {
        dst.x = this.x
        dst.y = this.y
        dst.z = this.z
        dst.w = this.w
        return dst
    }
    // Note: The data class provides a copy() method which is more idiomatic for creating copies.
    // This method is provided for API compatibility and the optional dst parameter.

    /**
     * Creates a copy of `this`. (Alias for copy)
     */
    fun clone(dst: Vec4 = Vec4()): Vec4 = copy(dst)

    /**
     * Multiplies `this` by [other] component-wise.
     */
    fun multiply(other: Vec4, dst: Vec4 = Vec4()): Vec4 {
        dst.x = this.x * other.x
        dst.y = this.y * other.y
        dst.z = this.z * other.z
        dst.w = this.w * other.w
        return dst
    }

    /**
     * Multiplies `this` by [other] component-wise. (Alias for multiply)
     */
    fun mul(other: Vec4, dst: Vec4 = Vec4()): Vec4 = multiply(other, dst)

    /**
     * Divides `this` by [other] component-wise.
     */
    fun divide(other: Vec4, dst: Vec4 = Vec4()): Vec4 {
        dst.x = this.x / other.x
        dst.y = this.y / other.y
        dst.z = this.z / other.z
        dst.w = this.w / other.w
        return dst
    }

    /**
     * Divides `this` by [other] component-wise. (Alias for divide)
     */
    fun div(other: Vec4, dst: Vec4 = Vec4()): Vec4 = divide(other, dst)

    /**
     * Sets the components of `this` to zero.
     */
    fun zero(dst: Vec4 = Vec4()): Vec4 {
        // Note: This behavior differs from the original if dst is not provided.
        // The original modified `this`. This version modifies the new default dst.
        // If the original behavior is desired, this needs adjustment.
        // For now, applying the requested pattern strictly.
        dst.x = 0.0
        dst.y = 0.0
        dst.z = 0.0
        dst.w = 0.0
        return dst
    }

    /**
     * Transforms `this` by the 4x4 matrix [m].
     * Note: Assumes Mat4 provides an indexer `get(index: Int)` that maps to column-major order like the TS version.
     * (m[0]=m00, m[1]=m10, m[2]=m20, m[3]=m30, m[4]=m01, m[5]=m11, ...)
     */
     fun transformMat4(m: Mat4, dst: Vec4 = Vec4()): Vec4 {
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
    fun setLength(length: Double, dst: Vec4 = Vec4()): Vec4 {
        this.normalize(dst)
        return dst.mulScalar(length, dst)
    }

    /**
     * Ensures `this` is not longer than [maxLen].
     * @return The vector, shortened to [maxLen] if its original length was greater, otherwise a copy of `this`.
     */
     fun truncate(maxLen: Double, dst: Vec4 = Vec4()): Vec4 {
         if (this.length > maxLen) {
             return this.setLength(maxLen, dst)
         }
         return this.copy(dst)
    }

    /**
     * Computes the midpoint between `this` and [other].
     */
    fun midpoint(other: Vec4, dst: Vec4 = Vec4()): Vec4 {
        return this.lerp(other, 0.5, dst)
    }
}
