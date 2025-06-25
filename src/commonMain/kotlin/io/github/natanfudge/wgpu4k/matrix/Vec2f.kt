@file:Suppress("NOTHING_TO_INLINE")

package io.github.natanfudge.wgpu4k.matrix

import kotlinx.serialization.Serializable
import kotlin.math.*
import kotlin.random.Random

/**
 * Represents a mutable 2D Vector with instance methods,
 * including optional 'dst' parameter support.
 */
@Serializable
class Vec2f(var x: Float, var y: Float) {
    constructor() : this(0f, 0f)

    companion object {
        // 2 * 4 bytes
        const val SIZE_BYTES = 8u

        /** Creates a new Vec2 instance. */
        fun create(x: Float = 0f, y: Float = 0f): Vec2f = Vec2f(x, y)

        /** Creates a new Vec2 instance. (Alias for create) */
        fun fromValues(x: Float = 0f, y: Float = 0f): Vec2f = Vec2f(x, y)

        /** Creates a random unit vector scaled by [scale]. */
        fun random(scale: Float = 1f, dst: Vec2f = Vec2f()): Vec2f {
            val angle = Random.nextFloat() * 2f * FloatPi
            dst.x = cos(angle) * scale
            dst.y = sin(angle) * scale
            return dst
        }

        /**
         * Sets the components of [dst] to [x] and [y]
         */
        fun set(x: Float, y: Float, dst: Vec2f = Vec2f()): Vec2f {
            dst.x = x
            dst.y = y
            return dst
        }

        /**
         * Either creates a new zero vector, or sets [dst] to 0,0.
         * */
        fun zero(dst: Vec2f = Vec2f()): Vec2f {
            dst.x = 0f
            dst.y = 0f
            return dst
        }
        // No static operators in Vec2f
    }

    fun toArray() = floatArrayOf(x, y)

    inline operator fun plus(other: Vec2f) = add(other)
    inline operator fun minus(other: Vec2f) = subtract(other)
    inline operator fun times(scalar: Float) = mulScalar(scalar)
    inline operator fun div(scalar: Float) = divScalar(scalar)
    inline operator fun unaryMinus() = negate()

    /**
     * Allows accessing components using array syntax (e.g., vec[0]).
     */
    operator fun get(index: Int): Float {
        return when (index) {
            0 -> x
            1 -> y
            else -> throw IndexOutOfBoundsException("Index $index is out of bounds for Vec2")
        }
    }

    /**
     * Allows setting components using array syntax (e.g., vec[0] = 1.0f).
     */
    operator fun set(index: Int, value: Float) {
        when (index) {
            0 -> x = value
            1 -> y = value
            else -> throw IndexOutOfBoundsException("Index $index is out of bounds for Vec2")
        }
    }

    /**
     * Computes the length (magnitude) of `this` vector.
     */
    val length: Float
        get() = sqrt(this.lengthSq)

    /** Computes the length (magnitude) of `this` vector (alias for [length]). */
    val len: Float
        get() = length
    inline val norm: Float get() = length

    /**
     * Computes the square of the length of `this` vector. Faster than [length] if only comparing magnitudes.
     */
    val lengthSq: Float
        get() = x * x + y * y

    /** Computes the square of the length of `this` vector (alias for [lengthSq]). */
    val lenSq: Float
        get() = lengthSq

    /**
     * Sets this vector to the zero vec2
     */
    fun setZero() {
        zero(this)
    }

    fun absoluteValue(dst: Vec2f = Vec2f()): Vec2f {
        dst.x = abs(this.x)
        dst.y = abs(this.y)
        return dst
    }

    /**
     * Applies Math.ceil to each component of `this`.
     */
    fun ceil(dst: Vec2f = Vec2f()): Vec2f {
        dst.x = kotlin.math.ceil(this.x)
        dst.y = kotlin.math.ceil(this.y)
        return dst
    }

    /**
     * Applies Math.floor to each component of `this`.
     */
    fun floor(dst: Vec2f = Vec2f()): Vec2f {
        dst.x = kotlin.math.floor(this.x)
        dst.y = kotlin.math.floor(this.y)
        return dst
    }

    /**
     * Applies Math.round to each component of `this`.
     */
    fun round(dst: Vec2f = Vec2f()): Vec2f {
        dst.x = kotlin.math.round(this.x)
        dst.y = kotlin.math.round(this.y)
        return dst
    }

    /**
     * Computes the component-wise inverse (1/x) of `this` vector.
     */
    fun inverse(dst: Vec2f = Vec2f()): Vec2f {
        dst.x = 1f / this.x
        dst.y = 1f / this.y
        return dst
    }

    /** Computes the component-wise inverse (1/x) of `this` vector (alias for [inverse]). */
    fun invert(dst: Vec2f = Vec2f()): Vec2f = inverse(dst)


    /**
     * Normalizes `this` vector (scales it to unit length).
     */
    fun normalize(dst: Vec2f = Vec2f()): Vec2f {
        val l = this.length // Use instance length property
        if (l > EPSILON) {
            val invLen = 1f / l
            dst.x = this.x * invLen
            dst.y = this.y * invLen
        } else {
            dst.x = 0f
            dst.y = 0f
        }
        return dst
    }

    /**
     * Negates `this` vector (multiplies components by -1).
     */
    fun negate(dst: Vec2f = Vec2f()): Vec2f {
        dst.x = -this.x
        dst.y = -this.y
        return dst
    }


    /** Copies the components of `this` vector (alias for [copyTo]). */
    fun clone(x: Float = this.x, y: Float = this.y, dst: Vec2f = Vec2f()): Vec2f = copy(x, y, dst)


    /**
     * Copies the components of `this`.
     */
    fun copy(x: Float = this.x, y: Float = this.y, dst: Vec2f = Vec2f()): Vec2f {
        dst.x = x
        dst.y = y
        return dst
    }

    /**
     * Sets `dst` to `v[axis] = value` and returns it
     */
    fun copy(axis: Int, value: Float, dst: Vec2f = Vec2f()): Vec2f = when (axis) {
        0 -> copy(x = value, dst = dst)
        1 -> copy(y = value, dst = dst)
        else -> throw IndexOutOfBoundsException("Invalid axis index: $axis")
    }


    /**
     * Adds [other] to `this`.
     */
    fun add(other: Vec2f, dst: Vec2f = Vec2f()): Vec2f {
        dst.x = this.x + other.x
        dst.y = this.y + other.y
        return dst
    }

    /**
     * Computes the angle in radians between `this` vector and [other].
     * If `this` or [other] is the zero vector, will return PI/2
     */
    fun angle(other: Vec2f): Float {
        // length property now refers to 'this.length'
        val mag = this.length * other.length // Need to access length property on other too
        val dotProd = this.dot(other) // Use dot method
        val cosine = if (mag != 0f) dotProd / mag else 0f
        return acos(cosine.coerceIn(-1f, 1f))
    }

    /**
     * Subtracts [other] from `this` vector (`this` - [other]).
     */
    fun subtract(other: Vec2f, dst: Vec2f = Vec2f()): Vec2f {
        dst.x = this.x - other.x
        dst.y = this.y - other.y
        return dst
    }

    /** Subtracts [other] from `this` vector (`this` - [other]) (alias for [subtract]). */
    fun sub(other: Vec2f, dst: Vec2f = Vec2f()): Vec2f = subtract(other, dst)


    /**
     * Computes the component-wise maximum of `this` vector and [other].
     */
    fun max(other: Vec2f, dst: Vec2f = Vec2f()): Vec2f {
        dst.x = kotlin.math.max(this.x, other.x)
        dst.y = kotlin.math.max(this.y, other.y)
        return dst
    }

    /**
     * Computes the component-wise minimum of `this` vector and [other].
     */
    fun min(other: Vec2f, dst: Vec2f = Vec2f()): Vec2f {
        dst.x = kotlin.math.min(this.x, other.x)
        dst.y = kotlin.math.min(this.y, other.y)
        return dst
    }

    /**
     * Multiplies `this` vector by the scalar [k].
     */
    fun mulScalar(k: Float, dst: Vec2f = Vec2f()): Vec2f {
        dst.x = this.x * k
        dst.y = this.y * k
        return dst
    }

    /** Multiplies `this` vector by the scalar [k] (alias for [mulScalar]). */
    fun scale(k: Float, dst: Vec2f = Vec2f()): Vec2f = mulScalar(k, dst)

    /**
     * Divides `this` vector by the scalar [k].
     */
    fun divScalar(k: Float, dst: Vec2f = Vec2f()): Vec2f {
        dst.x = this.x / k
        dst.y = this.y / k
        return dst
    }

    /**
     * Computes the 2D cross product (returns the Z component) of `this` vector and [other].
     *
     */
    fun cross(other: Vec2f, dst: Vec3f = Vec3f()): Vec3f {
        val z = this.x * other.y - this.y * other.x
        dst[0] = 0f
        dst[1] = 0f
        dst[2] = z
        return dst
    }

    /**
     * Computes the dot product of `this` vector and [other].
     */
    fun dot(other: Vec2f): Float {
        return this.x * other.x + this.y * other.y
    }


    /**
     * Computes the distance between `this` point and [other].
     */
    fun distance(other: Vec2f): Float {
        return sqrt(this.distanceSq(other)) // Call distanceSq instance method
    }

    /** Computes the distance between `this` point and [other] (alias for [distance]). */
    fun dist(other: Vec2f): Float = distance(other)

    /**
     * Computes the square of the distance between `this` point and [other].
     */
    fun distanceSq(other: Vec2f): Float {
        val dx = this.x - other.x
        val dy = this.y - other.y
        return dx * dx + dy * dy
    }

    /** Computes the square of the distance between `this` point and [other] (alias for [distanceSq]). */
    fun distSq(other: Vec2f): Float = distanceSq(other)

    /**
     * Multiplies `this` vector by [other] component-wise.
     */
    fun multiply(other: Vec2f, dst: Vec2f = Vec2f()): Vec2f {
        dst.x = this.x * other.x
        dst.y = this.y * other.y
        return dst
    }

    /** Multiplies `this` vector by [other] component-wise (alias for [multiply]). */
    fun mul(other: Vec2f, dst: Vec2f = Vec2f()): Vec2f = multiply(other, dst)

    /**
     * Divides `this` vector by [other] component-wise.
     */
    fun divide(other: Vec2f, dst: Vec2f = Vec2f()): Vec2f {
        dst.x = this.x / other.x
        dst.y = this.y / other.y
        return dst
    }

    /** Divides `this` vector by [other] component-wise (alias for [divide]). */
    fun div(other: Vec2f, dst: Vec2f = Vec2f()): Vec2f = divide(other, dst)

    /**
     * Transforms `this` vector (point, w=1) by the 4x4 matrix [m].
     */
    fun transformMat4(m: Mat4f, dst: Vec2f = Vec2f()): Vec2f {
        // Store original x,y in case target === this
        val originalX = this.x
        val originalY = this.y
        dst.x = originalX * m[0] + originalY * m[4] + m[12]
        dst.y = originalX * m[1] + originalY * m[5] + m[13]
        return dst
    }

    /**
     * Transforms `this` vector (point, w=1) by the 3x3 matrix [m].
     */
    fun transformMat3(m: Mat3f, dst: Vec2f = Vec2f()): Vec2f {
        dst.x = m[0] * x + m[4] * y + m[8];
        dst.y = m[1] * x + m[5] * y + m[9];

        return dst
    }

    /**
     * Sets the length of `this` vector to [len].
     */
    fun setLength(len: Float, dst: Vec2f = Vec2f()): Vec2f {
        // Normalize 'this' vector's components into 'target'
        this.normalize(dst) // Use normalize method, outputting to target
        // Scale 'target' in place to the desired length
        dst.x *= len
        dst.y *= len
        return dst
    }

    /**
     * Truncates `this` vector if its length exceeds [maxLen].
     */
    fun truncate(maxLen: Float, dst: Vec2f = Vec2f()): Vec2f {
        val lSq = this.lengthSq // Use instance lengthSq property
        if (lSq > maxLen * maxLen) {
            // If too long, calculate the correctly scaled vector using setLength
            // We want the result in target, so pass target as dst to setLength
            this.setLength(maxLen, dst)
        } else {
            // Otherwise, just copy this vector's components into target
            this.copy(dst = dst)
        }
        return dst
    }

    /**
     * Calculates the midpoint between `this` vector and [other].
     */
    fun midpoint(other: Vec2f, dst: Vec2f = Vec2f()): Vec2f {
        // Calculate lerp(this, other, 0.5f) and store in target
        this.lerp(other, 0.5f, dst)
        return dst
    }

    /**
     * Sets the components of `this` to [x] and [y].
     */
    fun set(x: Float, y: Float): Vec2f {
        this.x = x
        this.y = y
        return this
    }

    /**
     * Sets the values of `this` equal to the values of [other].
     */
    fun set(other: Vec2f): Vec2f {
        this.x = other.x
        this.y = other.y
        return this
    }

    /**
     * Clamps each component of `this` between [min] and [max].
     */
    fun clamp(min: Float = 0f, max: Float = 1f, dst: Vec2f = Vec2f()): Vec2f {
        dst.x = this.x.coerceIn(min, max)
        dst.y = this.y.coerceIn(min, max)
        return dst
    }

    /**
     * Adds [other] scaled by [scale] to `this`.
     */
    fun addScaled(other: Vec2f, scale: Float, dst: Vec2f = Vec2f()): Vec2f {
        dst.x = this.x + other.x * scale
        dst.y = this.y + other.y * scale
        return dst
    }

    /**
     * Checks if `this` vector is approximately equal to [other].
     */
    fun equalsApproximately(other: Vec2f, tolerance: Float = EPSILON): Boolean {
        return abs(this.x - other.x) < tolerance &&
                abs(this.y - other.y) < tolerance
    }

    /**
     * Linearly interpolates between `this` vector and [other] using coefficient [t].
     * Result = `this` + [t] * ([other] - `this`).
     */
    fun lerp(other: Vec2f, t: Float, dst: Vec2f = Vec2f()): Vec2f {
        dst.x = this.x + t * (other.x - this.x)
        dst.y = this.y + t * (other.y - this.y)
        return dst
    }

    /**
     * Performs component-wise linear interpolation between `this` vector and [other] using coefficient vector [t].
     * Result = `this` + [t] * ([other] - `this`).
     */
    fun lerpV(other: Vec2f, t: Vec2f, dst: Vec2f = Vec2f()): Vec2f {
        dst.x = this.x + t.x * (other.x - this.x)
        dst.y = this.y + t.y * (other.y - this.y)
        return dst
    }

    /**
     * Rotates `this` vector (point) around the [origin] by [rad] radians.
     */
    fun rotate(origin: Vec2f, rad: Float, dst: Vec2f = Vec2f()): Vec2f {
        // Translate point to the origin relative to 'origin'
        val p0 = this.x - origin.x
        val p1 = this.y - origin.y
        val sinC = sin(rad)
        val cosC = cos(rad)
        // Perform rotation and translate back
        dst.x = p0 * cosC - p1 * sinC + origin.x
        dst.y = p0 * sinC + p1 * cosC + origin.y
        return dst
    }


    /**
     * @param round if true, floating point values will look nicer by doing some rounding operations. The default is true.
     */
    fun toString(round: Boolean): String = if (round) "(${x.ns},${y.ns})" else "($x,$y)"
    override fun toString(): String = toString(round = true)

    override fun equals(other: Any?): Boolean {
        return other is Vec2f && other.x == x && other.y == y
    }

    override fun hashCode(): Int {
        var result = x.hashCode()
        result = 31 * result + y.hashCode()
        return result
    }
}
