package io.github.natanfudge.wgpu4k.matrix

import kotlin.math.*

/**
 * Represents a 4-dimensional vector.
 *
 * @property x The x component.
 * @property y The y component.
 * @property z The z component.
 * @property w The w component.
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
         * Creates a vec4; may be called with x, y, z, w to set initial values.
         * @param x - Initial x value.
         * @param y - Initial y value.
         * @param z - Initial z value.
         * @param w - Initial w value.
         * @return the created vector
         */
        fun create(x: Double = 0.0, y: Double = 0.0, z: Double = 0.0, w: Double = 0.0): Vec4 {
            return Vec4(x, y, z, w)
        }

        /**
         * Creates a vec4; may be called with x, y, z, w to set initial values. (same as create)
         * @param x - Initial x value.
         * @param y - Initial y value.
         * @param z - Initial z value.
         * @param w - Initial w value.
         * @return the created vector
         */
        fun fromValues(x: Double = 0.0, y: Double = 0.0, z: Double = 0.0, w: Double = 0.0): Vec4 {
            return Vec4(x, y, z, w)
        }
    }

    /**
     * Sets the values of this Vec4.
     *
     * @param x first value
     * @param y second value
     * @param z third value
     * @param w fourth value
     * @return This vector with its elements set.
     */
    fun set(x: Double, y: Double, z: Double, w: Double): Vec4 {
        this.x = x
        this.y = y
        this.z = z
        this.w = w
        return this
    }

    /**
     * Applies Math.ceil to each element of vector.
     * @param dst - vector to hold result. If null, a new one is created.
     * @return A vector that is the ceil of each element of this vector.
     */
    fun ceil(dst: Vec4? = null): Vec4 {
        val target = dst ?: Vec4()
        target.x = ceil(this.x)
        target.y = ceil(this.y)
        target.z = ceil(this.z)
        target.w = ceil(this.w)
        return target
    }

    /**
     * Applies Math.floor to each element of vector.
     * @param dst - vector to hold result. If null, a new one is created.
     * @return A vector that is the floor of each element of this vector.
     */
    fun floor(dst: Vec4? = null): Vec4 {
        val target = dst ?: Vec4()
        target.x = floor(this.x)
        target.y = floor(this.y)
        target.z = floor(this.z)
        target.w = floor(this.w)
        return target
    }

    /**
     * Applies Math.round to each element of vector.
     * @param dst - vector to hold result. If null, a new one is created.
     * @return A vector that is the round of each element of this vector.
     */
    fun round(dst: Vec4? = null): Vec4 {
        val target = dst ?: Vec4()
        target.x = round(this.x)
        target.y = round(this.y)
        target.z = round(this.z)
        target.w = round(this.w)
        return target
    }

    /**
     * Clamp each element of vector between min and max.
     * @param min - Min value, default 0.0
     * @param max - Max value, default 1.0
     * @param dst - vector to hold result. If null, a new one is created.
     * @return A vector that the clamped value of each element of this vector.
     */
    fun clamp(min: Double = 0.0, max: Double = 1.0, dst: Vec4? = null): Vec4 {
        val target = dst ?: Vec4()
        target.x = max(min, min(max, this.x))
        target.y = max(min, min(max, this.y))
        target.z = max(min, min(max, this.z))
        target.w = max(min, min(max, this.w))
        return target
    }

    /**
     * Adds another vector to this vector.
     * @param other - Operand vector.
     * @param dst - vector to hold result. If null, a new one is created.
     * @return A vector that is the sum of this vector and the other vector.
     */
    fun add(other: Vec4, dst: Vec4? = null): Vec4 {
        val target = dst ?: Vec4()
        target.x = this.x + other.x
        target.y = this.y + other.y
        target.z = this.z + other.z
        target.w = this.w + other.w
        return target
    }

    /**
     * Adds another vector scaled by a scalar to this vector.
     * @param other - Operand vector.
     * @param scale - Amount to scale other vector.
     * @param dst - vector to hold result. If null, a new one is created.
     * @return A vector that is the sum of this vector + other * scale.
     */
    fun addScaled(other: Vec4, scale: Double, dst: Vec4? = null): Vec4 {
        val target = dst ?: Vec4()
        target.x = this.x + other.x * scale
        target.y = this.y + other.y * scale
        target.z = this.z + other.z * scale
        target.w = this.w + other.w * scale
        return target
    }

    /**
     * Subtracts another vector from this vector.
     * @param other - Operand vector.
     * @param dst - vector to hold result. If null, a new one is created.
     * @return A vector that is the difference of this vector and the other vector.
     */
    fun subtract(other: Vec4, dst: Vec4? = null): Vec4 {
        val target = dst ?: Vec4()
        target.x = this.x - other.x
        target.y = this.y - other.y
        target.z = this.z - other.z
        target.w = this.w - other.w
        return target
    }

    /**
     * Subtracts another vector from this vector. (Alias for subtract)
     * @param other - Operand vector.
     * @param dst - vector to hold result. If null, a new one is created.
     * @return A vector that is the difference of this vector and the other vector.
     */
    fun sub(other: Vec4, dst: Vec4? = null): Vec4 = subtract(other, dst)

    /**
     * Check if this vector is approximately equal to another vector.
     * @param other - Operand vector.
     * @param epsilon - Threshold for equality check.
     * @return true if vectors are approximately equal.
     */
    fun equalsApproximately(other: Vec4, epsilon: Double = EPSILON): Boolean {
        return abs(this.x - other.x) < epsilon &&
               abs(this.y - other.y) < epsilon &&
               abs(this.z - other.z) < epsilon &&
               abs(this.w - other.w) < epsilon
    }

    /**
     * Check if this vector is exactly equal to another vector.
     * Note: Prefer equalsApproximately for floating-point comparisons.
     * @param other - Operand vector.
     * @return true if vectors are exactly equal.
     */
    fun equals(other: Vec4): Boolean {
        return this.x == other.x && this.y == other.y && this.z == other.z && this.w == other.w
    }
    // Note: The data class provides an equals method, but this explicit one matches the TS API name.
    // The data class equals will be used for standard equality checks (e.g., in collections).

    /**
     * Performs linear interpolation between this vector and another vector.
     * Given vectors this (a) and other (b) and interpolation coefficient t, returns
     * a + t * (b - a).
     * @param other - Operand vector (b).
     * @param t - Interpolation coefficient.
     * @param dst - vector to hold result. If null, a new one is created.
     * @return The linear interpolated result.
     */
    fun lerp(other: Vec4, t: Double, dst: Vec4? = null): Vec4 {
        val target = dst ?: Vec4()
        target.x = this.x + t * (other.x - this.x)
        target.y = this.y + t * (other.y - this.y)
        target.z = this.z + t * (other.z - this.z)
        target.w = this.w + t * (other.w - this.w)
        return target
    }

    /**
     * Performs linear interpolation between this vector and another vector using a vector coefficient.
     * Given vectors this (a) and other (b) and interpolation coefficient vector t, returns
     * a + t * (b - a).
     * @param other - Operand vector (b).
     * @param t - Interpolation coefficients vector.
     * @param dst - vector to hold result. If null, a new one is created.
     * @return the linear interpolated result.
     */
    fun lerpV(other: Vec4, t: Vec4, dst: Vec4? = null): Vec4 {
        val target = dst ?: Vec4()
        target.x = this.x + t.x * (other.x - this.x)
        target.y = this.y + t.y * (other.y - this.y)
        target.z = this.z + t.z * (other.z - this.z)
        target.w = this.w + t.w * (other.w - this.w)
        return target
    }

    /**
     * Return max values of this vector and another vector.
     * @param other - Operand vector.
     * @param dst - vector to hold result. If null, a new one is created.
     * @return The max components vector.
     */
    fun max(other: Vec4, dst: Vec4? = null): Vec4 {
        val target = dst ?: Vec4()
        target.x = max(this.x, other.x)
        target.y = max(this.y, other.y)
        target.z = max(this.z, other.z)
        target.w = max(this.w, other.w)
        return target
    }

    /**
     * Return min values of this vector and another vector.
     * @param other - Operand vector.
     * @param dst - vector to hold result. If null, a new one is created.
     * @return The min components vector.
     */
    fun min(other: Vec4, dst: Vec4? = null): Vec4 {
        val target = dst ?: Vec4()
        target.x = min(this.x, other.x)
        target.y = min(this.y, other.y)
        target.z = min(this.z, other.z)
        target.w = min(this.w, other.w)
        return target
    }

    /**
     * Multiplies this vector by a scalar.
     * @param k - The scalar.
     * @param dst - vector to hold result. If null, a new one is created.
     * @return The scaled vector.
     */
    fun mulScalar(k: Double, dst: Vec4? = null): Vec4 {
        val target = dst ?: Vec4()
        target.x = this.x * k
        target.y = this.y * k
        target.z = this.z * k
        target.w = this.w * k
        return target
    }

    /**
     * Multiplies this vector by a scalar. (Alias for mulScalar)
     * @param k - The scalar.
     * @param dst - vector to hold result. If null, a new one is created.
     * @return The scaled vector.
     */
    fun scale(k: Double, dst: Vec4? = null): Vec4 = mulScalar(k, dst)

    /**
     * Divides this vector by a scalar.
     * @param k - The scalar.
     * @param dst - vector to hold result. If null, a new one is created.
     * @return The scaled vector.
     */
    fun divScalar(k: Double, dst: Vec4? = null): Vec4 {
        val target = dst ?: Vec4()
        target.x = this.x / k
        target.y = this.y / k
        target.z = this.z / k
        target.w = this.w / k
        return target
    }

    /**
     * Computes the component-wise inverse (1/x) of this vector.
     * @param dst - vector to hold result. If null, a new one is created.
     * @return The inverted vector.
     */
    fun inverse(dst: Vec4? = null): Vec4 {
        val target = dst ?: Vec4()
        target.x = 1.0 / this.x
        target.y = 1.0 / this.y
        target.z = 1.0 / this.z
        target.w = 1.0 / this.w
        return target
    }

    /**
     * Computes the component-wise inverse (1/x) of this vector. (Alias for inverse)
     * @param dst - vector to hold result. If null, a new one is created.
     * @return The inverted vector.
     */
    fun invert(dst: Vec4? = null): Vec4 = inverse(dst)

    /**
     * Computes the dot product of this vector and another vector.
     * @param other - Operand vector.
     * @return dot product.
     */
    fun dot(other: Vec4): Double {
        return (this.x * other.x) + (this.y * other.y) + (this.z * other.z) + (this.w * other.w)
    }

    /**
     * Computes the length of this vector.
     */
    val length: Double
        get() = sqrt(this.x * this.x + this.y * this.y + this.z * this.z + this.w * this.w)

    /**
     * Computes the length of this vector. (Alias for length)
     */
    val len: Double
        get() = length

    /**
     * Computes the square of the length of this vector.
     */
    val lengthSq: Double
        get() = this.x * this.x + this.y * this.y + this.z * this.z + this.w * this.w

    /**
     * Computes the square of the length of this vector. (Alias for lengthSq)
     */
    val lenSq: Double
        get() = lengthSq

    /**
     * Computes the distance between this vector and another vector.
     * @param other - Operand vector.
     * @return distance between this vector and the other vector.
     */
    fun distance(other: Vec4): Double {
        val dx = this.x - other.x
        val dy = this.y - other.y
        val dz = this.z - other.z
        val dw = this.w - other.w
        return sqrt(dx * dx + dy * dy + dz * dz + dw * dw)
    }

    /**
     * Computes the distance between this vector and another vector. (Alias for distance)
     * @param other - Operand vector.
     * @return distance between this vector and the other vector.
     */
    fun dist(other: Vec4): Double = distance(other)

    /**
     * Computes the square of the distance between this vector and another vector.
     * @param other - Operand vector.
     * @return square of the distance between this vector and the other vector.
     */
    fun distanceSq(other: Vec4): Double {
        val dx = this.x - other.x
        val dy = this.y - other.y
        val dz = this.z - other.z
        val dw = this.w - other.w
        return dx * dx + dy * dy + dz * dz + dw * dw
    }

    /**
     * Computes the square of the distance between this vector and another vector. (Alias for distanceSq)
     * @param other - Operand vector.
     * @return square of the distance between this vector and the other vector.
     */
    fun distSq(other: Vec4): Double = distanceSq(other)

    /**
     * Divides this vector by its Euclidean length.
     * @param dst - vector to hold result. If null, a new one is created.
     * @return The normalized vector. Returns a zero vector if the length is too small.
     */
    fun normalize(dst: Vec4? = null): Vec4 {
        val target = dst ?: Vec4()
        val l = this.length
        if (l > EPSILON) {
            target.x = this.x / l
            target.y = this.y / l
            target.z = this.z / l
            target.w = this.w / l
        } else {
            target.x = 0.0
            target.y = 0.0
            target.z = 0.0
            target.w = 0.0
        }
        return target
    }

    /**
     * Negates this vector.
     * @param dst - vector to hold result. If null, a new one is created.
     * @return The negated vector.
     */
    fun negate(dst: Vec4? = null): Vec4 {
        val target = dst ?: Vec4()
        target.x = -this.x
        target.y = -this.y
        target.z = -this.z
        target.w = -this.w
        return target
    }

    /**
     * Copies the values from this vector to another vector.
     * @param dst - vector to hold result. If null, a new one is created.
     * @return A copy of this vector.
     */
    fun copy(dst: Vec4? = null): Vec4 {
        val target = dst ?: Vec4()
        target.x = this.x
        target.y = this.y
        target.z = this.z
        target.w = this.w
        return target
    }
    // Note: The data class provides a copy() method which is more idiomatic for creating copies.
    // This method is provided for API compatibility and the optional dst parameter.

    /**
     * Clones this vector. (Alias for copy)
     * @param dst - vector to hold result. If null, a new one is created.
     * @return A copy of this vector.
     */
    fun clone(dst: Vec4? = null): Vec4 = copy(dst)

    /**
     * Multiplies this vector by another vector (component-wise).
     * @param other - Operand vector.
     * @param dst - vector to hold result. If null, a new one is created.
     * @return The vector of products of entries of this vector and the other vector.
     */
    fun multiply(other: Vec4, dst: Vec4? = null): Vec4 {
        val target = dst ?: Vec4()
        target.x = this.x * other.x
        target.y = this.y * other.y
        target.z = this.z * other.z
        target.w = this.w * other.w
        return target
    }

    /**
     * Multiplies this vector by another vector (component-wise). (Alias for multiply)
     * @param other - Operand vector.
     * @param dst - vector to hold result. If null, a new one is created.
     * @return The vector of products of entries of this vector and the other vector.
     */
    fun mul(other: Vec4, dst: Vec4? = null): Vec4 = multiply(other, dst)

    /**
     * Divides this vector by another vector (component-wise).
     * @param other - Operand vector.
     * @param dst - vector to hold result. If null, a new one is created.
     * @return The vector of quotients of entries of this vector and the other vector.
     */
    fun divide(other: Vec4, dst: Vec4? = null): Vec4 {
        val target = dst ?: Vec4()
        target.x = this.x / other.x
        target.y = this.y / other.y
        target.z = this.z / other.z
        target.w = this.w / other.w
        return target
    }

    /**
     * Divides this vector by another vector (component-wise). (Alias for divide)
     * @param other - Operand vector.
     * @param dst - vector to hold result. If null, a new one is created.
     * @return The vector of quotients of entries of this vector and the other vector.
     */
    fun div(other: Vec4, dst: Vec4? = null): Vec4 = divide(other, dst)

    /**
     * Sets the components of this vector to zero.
     * @param dst - vector to hold result. If null, modifies this vector.
     * @return The zeroed vector.
     */
    fun zero(dst: Vec4? = null): Vec4 {
        val target = dst ?: this
        target.x = 0.0
        target.y = 0.0
        target.z = 0.0
        target.w = 0.0
        return target
    }

    /**
     * Transforms this vec4 by a 4x4 matrix.
     * Note: Assumes Mat4 provides an indexer `get(index: Int)` that maps to column-major order like the TS version.
     * (m[0]=m00, m[1]=m10, m[2]=m20, m[3]=m30, m[4]=m01, m[5]=m11, ...)
     * @param m - The matrix.
     * @param dst - optional vec4 to store result. If null, a new one is created.
     * @return the transformed vector.
     */
    fun transformMat4(m: Mat4, dst: Vec4? = null): Vec4 {
        val target = dst ?: Vec4()
        val x = this.x
        val y = this.y
        val z = this.z
        val w = this.w

        target.x = m[0] * x + m[4] * y + m[8] * z + m[12] * w
        target.y = m[1] * x + m[5] * y + m[9] * z + m[13] * w
        target.z = m[2] * x + m[6] * y + m[10] * z + m[14] * w
        target.w = m[3] * x + m[7] * y + m[11] * z + m[15] * w

        return target
    }

    /**
     * Sets the length of this vector.
     *
     * @param length The length of the resulting vector.
     * @param dst - optional vec4 to store result. If null, a new one is created.
     * @return The lengthened vector.
     */
    fun setLength(length: Double, dst: Vec4? = null): Vec4 {
        val target = dst ?: Vec4()
        this.normalize(target)
        return target.mulScalar(length, target)
    }

    /**
     * Ensures this vector is not longer than a max length.
     *
     * @param maxLen The longest length of the resulting vector.
     * @param dst - optional vec4 to store result. If null, a new one is created.
     * @return The vector, shortened to maxLen if it's too long, otherwise a copy of the original.
     */
    fun truncate(maxLen: Double, dst: Vec4? = null): Vec4 {
        val target = dst ?: Vec4()
        if (this.length > maxLen) {
            return this.setLength(maxLen, target)
        }
        return this.copy(target)
    }

    /**
     * Returns the vector exactly between this vector and another endpoint vector.
     *
     * @param other Endpoint vector.
     * @param dst - optional vec4 to store result. If null, a new one is created.
     * @return The vector exactly residing between this vector and the other endpoint.
     */
    fun midpoint(other: Vec4, dst: Vec4? = null): Vec4 {
        val target = dst ?: Vec4()
        return this.lerp(other, 0.5, target)
    }
}