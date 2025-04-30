package io.github.natanfudge.wgpu4k.matrix

import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.acos
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt
import kotlin.random.Random

/**
 * Represents a mutable 2D Vector with instance methods mimicking the JS API structure,
 * including optional 'dst' parameter support.
 */
data class Vec2(var x: Float = 0f, var y: Float = 0f) {

    // --- Instance Methods (Operating on `this`, supporting 'dst') ---

    /**
     * Sets the components of `this` to [x] and [y].
     */
    fun set(x: Float, y: Float): Vec2 {
        this.x = x
        this.y = y
        return this
    }

    /**
     * Applies Math.ceil to each component of `this`.
     */
    fun ceil(dst: Vec2? = null): Vec2 {
        val target = dst ?: Vec2()
        target.x = kotlin.math.ceil(this.x)
        target.y = kotlin.math.ceil(this.y)
        return target
    }

    /**
     * Applies Math.floor to each component of `this`.
     */
    fun floor(dst: Vec2? = null): Vec2 {
        val target = dst ?: Vec2()
        target.x = kotlin.math.floor(this.x)
        target.y = kotlin.math.floor(this.y)
        return target
    }

    /**
     * Applies Math.round to each component of `this`.
     */
    fun round(dst: Vec2? = null): Vec2 {
        val target = dst ?: Vec2()
        target.x = kotlin.math.round(this.x)
        target.y = kotlin.math.round(this.y)
        return target
    }

    /**
     * Clamps each component of `this` between [min] and [max].
     */
    fun clamp(min: Float = 0f, max: Float = 1f, dst: Vec2? = null): Vec2 {
        val target = dst ?: Vec2()
        target.x = this.x.coerceIn(min, max)
        target.y = this.y.coerceIn(min, max)
        return target
    }

    /**
     * Adds [other] to `this`.
     */
    fun add(other: Vec2, dst: Vec2? = null): Vec2 {
        val target = dst ?: Vec2()
        target.x = this.x + other.x
        target.y = this.y + other.y
        return target
    }

    /**
     * Adds [other] scaled by [scale] to `this`.
     */
    fun addScaled(other: Vec2, scale: Float, dst: Vec2? = null): Vec2 {
        val target = dst ?: Vec2()
        target.x = this.x + other.x * scale
        target.y = this.y + other.y * scale
        return target
    }

    /**
     * Returns the angle in radians between this vector and another vector ('other').
     * @param other The other vector.
     * @return The angle in radians.
     */
    fun angle(other: Vec2): Float {
        // length() method now refers to 'this.length()'
        val mag = this.length() * other.length() // Need to call length method on other too
        val dotProd = this.dot(other) // Use dot method
        val cosine = if (mag != 0f) dotProd / mag else 0f
        return acos(cosine.coerceIn(-1f, 1f))
    }

    /**
     * Subtracts another vector ('other') from this vector. (Result = this - other)
     * @param other The vector to subtract.
     * @param dst Vector to hold result. If null, a new one is created.
     * @return The Vec2 instance (`dst` or a new one) holding the difference.
     */
    fun subtract(other: Vec2, dst: Vec2? = null): Vec2 {
        val target = dst ?: Vec2()
        target.x = this.x - other.x
        target.y = this.y - other.y
        return target
    }

    /** Subtracts another vector ('other') from this vector. (Alias for subtract) */
    fun sub(other: Vec2, dst: Vec2? = null): Vec2 = subtract(other, dst)

    /**
     * Checks if this vector is approximately equal to another vector ('other').
     * @param other The vector to compare against.
     * @return true if vectors are approximately equal.
     */
    fun equalsApproximately(other: Vec2): Boolean {
        return abs(this.x - other.x) < EPSILON &&
                abs(this.y - other.y) < EPSILON
    }

    // Note: Default data class `equals` provides exact component-wise equality.
    // The explicit `equals(a, b)` from the previous version is removed as it conflicts
    // and the data class provides the standard behavior for `this == other`.

    /**
     * Performs linear interpolation between this vector and another ('other').
     * Result = this + t * (other - this).
     * @param other The target vector.
     * @param t Interpolation coefficient.
     * @param dst Vector to hold result. If null, a new one is created.
     * @return The Vec2 instance (`dst` or a new one) holding the interpolated result.
     */
    fun lerp(other: Vec2, t: Float, dst: Vec2? = null): Vec2 {
        val target = dst ?: Vec2()
        target.x = this.x + t * (other.x - this.x)
        target.y = this.y + t * (other.y - this.y)
        return target
    }

    /**
     * Performs linear interpolation using a vector coefficient 't'.
     * Result = this + t * (other - this).
     * @param other The target vector.
     * @param t Interpolation coefficients vector.
     * @param dst vector to hold result. If null, a new one is created.
     * @return The Vec2 instance (`dst` or a new one) holding the interpolated result.
     */
    fun lerpV(other: Vec2, t: Vec2, dst: Vec2? = null): Vec2 {
        val target = dst ?: Vec2()
        target.x = this.x + t.x * (other.x - this.x)
        target.y = this.y + t.y * (other.y - this.y)
        return target
    }

    /**
     * Returns a vector containing the component-wise max of this vector and 'other'.
     * @param other The other vector.
     * @param dst Vector to hold result. If null, a new one is created.
     * @return The Vec2 instance (`dst` or a new one) holding the max components.
     */
    fun max(other: Vec2, dst: Vec2? = null): Vec2 {
        val target = dst ?: Vec2()
        target.x = kotlin.math.max(this.x, other.x)
        target.y = kotlin.math.max(this.y, other.y)
        return target
    }

    /**
     * Returns a vector containing the component-wise min of this vector and 'other'.
     * @param other The other vector.
     * @param dst Vector to hold result. If null, a new one is created.
     * @return The Vec2 instance (`dst` or a new one) holding the min components.
     */
    fun min(other: Vec2, dst: Vec2? = null): Vec2 {
        val target = dst ?: Vec2()
        target.x = kotlin.math.min(this.x, other.x)
        target.y = kotlin.math.min(this.y, other.y)
        return target
    }

    /**
     * Multiplies this vector by a scalar 'k'.
     * @param k The scalar.
     * @param dst Vector to hold result. If null, a new one is created.
     * @return The Vec2 instance (`dst` or a new one) holding the scaled vector result.
     */
    fun mulScalar(k: Float, dst: Vec2? = null): Vec2 {
        val target = dst ?: Vec2()
        target.x = this.x * k
        target.y = this.y * k
        return target
    }

    /** Multiplies this vector by a scalar. (Alias for mulScalar) */
    fun scale(k: Float, dst: Vec2? = null): Vec2 = mulScalar(k, dst)

    /**
     * Divides this vector by a scalar 'k'.
     * @param k The scalar.
     * @param dst Vector to hold result. If null, a new one is created.
     * @return The Vec2 instance (`dst` or a new one) holding the scaled vector result.
     */
    fun divScalar(k: Float, dst: Vec2? = null): Vec2 {
        val target = dst ?: Vec2()
        target.x = this.x / k
        target.y = this.y / k
        return target
    }

    /**
     * Inverses this vector (component-wise 1/x).
     * @param dst Vector to hold result. If null, a new one is created.
     * @return The Vec2 instance (`dst` or a new one) holding the inverted vector result.
     */
    fun inverse(dst: Vec2? = null): Vec2 {
        val target = dst ?: Vec2()
        target.x = 1f / this.x
        target.y = 1f / this.y
        return target
    }

    /** Invert this vector. (Alias for inverse) */
    fun invert(dst: Vec2? = null): Vec2 = inverse(dst)

    /**
     * Computes the cross product of this vector and 'other', returning a 3D vector (FloatArray[3]).
     * @param other The other vector.
     * @param dst FloatArray (size 3) to hold result. If null, a new one is created.
     * @return The 3D vector result [0, 0, z].
     */
    fun cross(other: Vec2, dst: FloatArray? = null): FloatArray {
        val target = dst ?: FloatArray(3)
        val z = this.x * other.y - this.y * other.x
        target[0] = 0f
        target[1] = 0f
        target[2] = z
        return target
    }

    /**
     * Computes the dot product of this vector and 'other'.
     * @param other The other vector.
     * @return dot product.
     */
    fun dot(other: Vec2): Float {
        return this.x * other.x + this.y * other.y
    }

    /**
     * Computes the length (magnitude) of this vector.
     * @return length of this vector.
     */
    fun length(): Float {
        return sqrt(this.lengthSq()) // Call lengthSq instance method
    }

    /** Computes the length of this vector (Alias for length) */
    fun len(): Float = length()

    /**
     * Computes the square of the length of this vector.
     * @return square of the length of this vector.
     */
    fun lengthSq(): Float {
        return x * x + y * y
    }

    /** Computes the square of the length of this vector (Alias for lengthSq) */
    fun lenSq(): Float = lengthSq()

    /**
     * Computes the distance between this point and 'other'.
     * @param other The other point.
     * @return distance between this and other.
     */
    fun distance(other: Vec2): Float {
        return sqrt(this.distanceSq(other)) // Call distanceSq instance method
    }

    /** Computes the distance between this point and 'other' (Alias for distance) */
    fun dist(other: Vec2): Float = distance(other)

    /**
     * Computes the square of the distance between this point and 'other'.
     * @param other The other point.
     * @return square of the distance between this and other.
     */
    fun distanceSq(other: Vec2): Float {
        val dx = this.x - other.x
        val dy = this.y - other.y
        return dx * dx + dy * dy
    }

    /** Computes the square of the distance between this point and 'other' (Alias for distanceSq) */
    fun distSq(other: Vec2): Float = distanceSq(other)

    /**
     * Normalizes this vector (scales to unit length).
     * @param dst vector to hold result. If null, a new one is created.
     * @return The Vec2 instance (`dst` or a new one) holding the normalized vector.
     */
    fun normalize(dst: Vec2? = null): Vec2 {
        val target = dst ?: Vec2()
        val l = this.length() // Use instance length method
        if (l > EPSILON) {
            val invLen = 1f / l
            target.x = this.x * invLen
            target.y = this.y * invLen
        } else {
            target.x = 0f
            target.y = 0f
        }
        return target
    }

    /**
     * Negates this vector.
     * @param dst vector to hold result. If null, a new one is created.
     * @return The Vec2 instance (`dst` or a new one) holding the negated vector.
     */
    fun negate(dst: Vec2? = null): Vec2 {
        val target = dst ?: Vec2()
        target.x = -this.x
        target.y = -this.y
        return target
    }

    /**
     * Copies this vector's components into 'dst' or a new Vec2. (Same as clone)
     * Note: data class provides `copy()` method for creating modified copies.
     * This method adheres to the `dst` parameter pattern.
     * @param dst Vector to hold result. If null, a new one is created.
     * @return The Vec2 instance (`dst` or a new one) holding a copy of this vector's components.
     */
    fun copyTo(dst: Vec2? = null): Vec2 { // Renamed from 'copy' to avoid clash
        val target = dst ?: Vec2()
        target.x = this.x
        target.y = this.y
        return target
    }

    /** Clones this vector into 'dst' or a new Vec2. (Alias for copyTo) */
    fun clone(dst: Vec2? = null): Vec2 = copyTo(dst)

    /**
     * Multiplies this vector by 'other' component-wise.
     * @param other The other vector.
     * @param dst Vector to hold result. If null, a new one is created.
     * @return The Vec2 instance (`dst` or a new one) holding the component-wise product.
     */
    fun multiply(other: Vec2, dst: Vec2? = null): Vec2 {
        val target = dst ?: Vec2()
        target.x = this.x * other.x
        target.y = this.y * other.y
        return target
    }

    /** Multiplies this vector by 'other' component-wise. (Alias for multiply) */
    fun mul(other: Vec2, dst: Vec2? = null): Vec2 = multiply(other, dst)

    /**
     * Divides this vector by 'other' component-wise.
     * @param other The other vector.
     * @param dst Vector to hold result. If null, a new one is created.
     * @return The Vec2 instance (`dst` or a new one) holding the component-wise quotient.
     */
    fun divide(other: Vec2, dst: Vec2? = null): Vec2 {
        val target = dst ?: Vec2()
        target.x = this.x / other.x
        target.y = this.y / other.y
        return target
    }

    /** Divides this vector by 'other' component-wise. (Alias for divide) */
    fun div(other: Vec2, dst: Vec2? = null): Vec2 = divide(other, dst)

    /**
     * Transforms this vector (point, w=1) by a 4x4 matrix 'm' (column-major).
     * @param m The matrix (Mat4Arg - FloatArray[16]).
     * @param dst Optional Vec2 to store result. If null, a new one is created.
     * @return The Vec2 instance (`dst` or a new one) holding the transformed vector.
     */
    fun transformMat4(m: Mat4, dst: Vec2? = null): Vec2 {
        val target = dst ?: Vec2()
        // Store original x,y in case target === this
        val originalX = this.x
        val originalY = this.y
        target.x = originalX * m[0] + originalY * m[4] + m[12]
        target.y = originalX * m[1] + originalY * m[5] + m[13]
        return target
    }

    /**
     * Transforms this vector (point, w=1) by a 3x3 matrix [m].
     */
    fun transformMat3(m: Mat3, dst: Vec2? = null): Vec2 {
        val newDst = (dst ?:  Vec2())

        newDst.x = m[0] * x + m[4] * y + m[8];
        newDst.y = m[1] * x + m[5] * y + m[9];

        return newDst
    }

    /**
     * Rotates this vector (point) around a given 'origin' by 'rad' radians.
     * @param origin The origin of the rotation.
     * @param rad The angle of rotation in radians.
     * @param dst Optional Vec2 to store result. If null, a new one is created.
     * @return The Vec2 instance (`dst` or a new one) holding the rotated vector.
     */
    fun rotate(origin: Vec2, rad: Float, dst: Vec2? = null): Vec2 {
        val target = dst ?: Vec2()
        // Translate point to the origin relative to 'origin'
        val p0 = this.x - origin.x
        val p1 = this.y - origin.y
        val sinC = sin(rad)
        val cosC = cos(rad)
        // Perform rotation and translate back
        target.x = p0 * cosC - p1 * sinC + origin.x
        target.y = p0 * sinC + p1 * cosC + origin.y
        return target
    }

    /**
     * Sets the length of this vector. Modifies the vector components in 'dst' or a new Vec2.
     * @param len The desired length.
     * @param dst Optional Vec2 to store result. If null, a new one is created.
     * @return The Vec2 instance (`dst` or a new one) with the specified length.
     */
    fun setLength(len: Float, dst: Vec2? = null): Vec2 {
        val target = dst ?: Vec2()
        // Normalize 'this' vector's components into 'target'
        this.normalize(target) // Use normalize method, outputting to target
        // Scale 'target' in place to the desired length
        target.x *= len
        target.y *= len
        return target
    }

    /**
     * Truncates this vector if its length exceeds 'maxLen'.
     * @param maxLen The maximum allowed length.
     * @param dst Optional Vec2 to store result. If null, a new one is created.
     * @return The Vec2 instance (`dst` or a new one) holding the truncated vector or a copy of the original.
     */
    fun truncate(maxLen: Float, dst: Vec2? = null): Vec2 {
        val target = dst ?: Vec2()
        val lSq = this.lengthSq() // Use instance lengthSq
        if (lSq > maxLen * maxLen) {
            // If too long, calculate the correctly scaled vector using setLength
            // We want the result in target, so pass target as dst to setLength
            this.setLength(maxLen, target)
        } else {
            // Otherwise, just copy this vector's components into target
            this.copyTo(target)
        }
        return target
    }

    /**
     * Calculates the midpoint between this vector and 'other'.
     * @param other The other endpoint.
     * @param dst Optional Vec2 to store result. If null, a new one is created.
     * @return The Vec2 instance (`dst` or a new one) holding the midpoint vector.
     */
    fun midpoint(other: Vec2, dst: Vec2? = null): Vec2 {
        val target = dst ?: Vec2()
        // Calculate lerp(this, other, 0.5f) and store in target
        this.lerp(other, 0.5f, target)
        return target
    }

    // --- Companion Object for Static Factories and Constants ---
    companion object {
        /** A small epsilon value for floating-point comparisons. */
        const val EPSILON: Float = io.github.natanfudge.wgpu4k.matrix.EPSILON

        /** Creates a new Vec2 instance. */
        fun create(x: Float = 0f, y: Float = 0f): Vec2 = Vec2(x, y)

        /** Creates a new Vec2 instance. (Alias for create) */
        fun fromValues(x: Float = 0f, y: Float = 0f): Vec2 = Vec2(x, y)

        /** Creates a random unit vector scaled by `scale`. */
        fun random(scale: Float = 1f, dst: Vec2? = null): Vec2 {
            val target = dst ?: Vec2()
            val angle = Random.nextFloat() * 2f * PI.toFloat()
            target.x = cos(angle) * scale
            target.y = sin(angle) * scale
            return target
        }

        /** Zero's a vector (sets components to 0). */
        fun zero(dst: Vec2? = null): Vec2 {
            val target = dst ?: Vec2()
            target.x = 0f
            target.y = 0f
            return target
        }
    }
}
