package io.github.natanfudge.wgpu4k.matrix

import kotlin.math.*
import kotlin.random.Random // Needed for Vec3.random






/**
 * Represents a 3-component vector using individual x, y, z fields.
 */
class Vec3(
    var x: Float = 0f,
    var y: Float = 0f,
    var z: Float = 0f
) {

    // Constructors are now handled by default parameters in the primary constructor.

    /**
     * Allows accessing components using array syntax (e.g., vec[0]).
     */
    operator fun get(index: Int): Float {
        return when (index) {
            0 -> x
            1 -> y
            2 -> z
            else -> throw IndexOutOfBoundsException("Index $index is out of bounds for Vec3")
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
            else -> throw IndexOutOfBoundsException("Index $index is out of bounds for Vec3")
        }
    }

    // --- Instance Methods (where `this` is the first parameter 'v' or 'a') ---

    /**
     * Computes the ceiling of each component of `this`.
     */
    fun ceil(dst: Vec3 = Vec3()): Vec3 {
        dst.x = ceil(this.x)
        dst.y = ceil(this.y)
        dst.z = ceil(this.z)
        return dst
    }

    /**
     * Computes the floor of each component of `this`.
     */
    fun floor(dst: Vec3 = Vec3()): Vec3 {
        dst.x = floor(this.x)
        dst.y = floor(this.y)
        dst.z = floor(this.z)
        return dst
    }

    /**
     * Computes the rounded value of each component of `this`.
     */
    fun round(dst: Vec3 = Vec3()): Vec3 {
        dst.x = round(this.x)
        dst.y = round(this.y)
        dst.z = round(this.z)
        return dst
    }

    /**
     * Clamps each component of `this` between [min] and [max].
     */
    fun clamp(min: Float = 0f, max: Float = 1f, dst: Vec3 = Vec3()): Vec3 {
        dst.x = min(max, max(min, this.x))
        dst.y = min(max, max(min, this.y))
        dst.z = min(max, max(min, this.z))
        return dst
    }

    /**
     * Adds [b] to `this`.
     */
    fun add(b: Vec3, dst: Vec3 = Vec3()): Vec3 {
        dst.x = this.x + b.x
        dst.y = this.y + b.y
        dst.z = this.z + b.z
        return dst
    }

    /**
     * Adds [b] scaled by [scale] to `this`.
     */
    fun addScaled(b: Vec3, scale: Float, dst: Vec3 = Vec3()): Vec3 {
        dst.x = this.x + b.x * scale
        dst.y = this.y + b.y * scale
        dst.z = this.z + b.z * scale
        return dst
    }

    /**
     * Computes the angle in radians between `this` and [b].
     */
    fun angle(b: Vec3): Float {
        val mag1 = this.length() // Use instance length method
        val mag2 = b.length()
        val mag = mag1 * mag2
        val cosine = if (mag != 0f) this.dot(b) / mag else 0f // Use instance dot method
        // Clamp cosine to avoid floating point errors leading to NaN in acos
        val clampedCosine = max(-1f, min(1f, cosine))
        return acos(clampedCosine)
    }

    /**
     * Subtracts [b] from `this`.
     */
    fun subtract(b: Vec3, dst: Vec3 = Vec3()): Vec3 {
        dst.x = this.x - b.x
        dst.y = this.y - b.y
        dst.z = this.z - b.z
        return dst
    }

    /**
     * Subtracts [b] from `this` (alias for [subtract]).
     */
    fun sub(b: Vec3, dst: Vec3 = Vec3()): Vec3 {
        return subtract(b, dst)
    }

    /**
     * Checks if `this` and [b] are approximately equal.
     */
    fun equalsApproximately(b: Vec3): Boolean {
        return abs(this.x - b.x) < EPSILON &&
                abs(this.y - b.y) < EPSILON &&
                abs(this.z - b.z) < EPSILON
    }



    /**
     * Linearly interpolates between `this` and [b] using coefficient [t].
     */
    fun lerp(b: Vec3, t: Float, dst: Vec3 = Vec3()): Vec3 {
        dst.x = this.x + t * (b.x - this.x)
        dst.y = this.y + t * (b.y - this.y)
        dst.z = this.z + t * (b.z - this.z)
        return dst
    }

    /**
     * Performs component-wise linear interpolation between `this` and [b] using coefficient vector [t].
     */
    fun lerpV(b: Vec3, t: Vec3, dst: Vec3 = Vec3()): Vec3 {
        dst.x = this.x + t.x * (b.x - this.x)
        dst.y = this.y + t.y * (b.y - this.y)
        dst.z = this.z + t.z * (b.z - this.z)
        return dst
    }

    /**
     * Computes the component-wise maximum of `this` and [b].
     */
    fun max(b: Vec3, dst: Vec3 = Vec3()): Vec3 {
        dst.x = max(this.x, b.x)
        dst.y = max(this.y, b.y)
        dst.z = max(this.z, b.z)
        return dst
    }

    /**
     * Computes the component-wise minimum of `this` and [b].
     */
    fun min(b: Vec3, dst: Vec3 = Vec3()): Vec3 {
        dst.x = min(this.x, b.x)
        dst.y = min(this.y, b.y)
        dst.z = min(this.z, b.z)
        return dst
    }

    /**
     * Multiplies `this` by scalar [k].
     */
    fun mulScalar(k: Float, dst: Vec3 = Vec3()): Vec3 {
        dst.x = this.x * k
        dst.y = this.y * k
        dst.z = this.z * k
        return dst
    }

    /**
     * Multiplies `this` by scalar [k] (alias for [mulScalar]).
     */
    fun scale(k: Float, dst: Vec3 = Vec3()): Vec3 {
        return mulScalar(k, dst)
    }

    /**
     * Divides `this` by scalar [k].
     */
    fun divScalar(k: Float, dst: Vec3 = Vec3()): Vec3 {
        dst.x = this.x / k
        dst.y = this.y / k
        dst.z = this.z / k
        return dst
    }

    /**
     * Computes the component-wise inverse (1/x) of `this` vector.
     */
    fun inverse(dst: Vec3 = Vec3()): Vec3 {
        dst.x = 1f / this.x
        dst.y = 1f / this.y
        dst.z = 1f / this.z
        return dst
    }

    /**
     * Computes the component-wise inverse (1/x) of `this` vector (alias for [inverse]).
     */
    fun invert(dst: Vec3 = Vec3()): Vec3 {
        return inverse(dst)
    }

    /**
     * Computes the cross product of `this` and [b].
     */
    fun cross(b: Vec3, dst: Vec3 = Vec3()): Vec3 {
        val ax = this.x; val ay = this.y; val az = this.z
        val bx = b.x; val by = b.y; val bz = b.z

        dst.x = ay * bz - az * by
        dst.y = az * bx - ax * bz
        dst.z = ax * by - ay * bx

        return dst
    }

    /**
     * Computes the dot product of `this` and [b].
     */
    fun dot(b: Vec3): Float {
        return (this.x * b.x) + (this.y * b.y) + (this.z * b.z)
    }

    /**
     * Computes the length (magnitude) of `this` vector.
     */
    fun length(): Float {
        return sqrt(this.x * this.x + this.y * this.y + this.z * this.z)
    }

    /**
     * Computes the length (magnitude) of `this` vector (alias for [length]).
     */
    fun len(): Float {
        return length()
    }

    /**
     * Computes the square of the length of `this` vector. Faster than [length] if only comparing magnitudes.
     */
    fun lengthSq(): Float {
        return this.x * this.x + this.y * this.y + this.z * this.z
    }

    /**
     * Computes the square of the length of `this` vector (alias for [lengthSq]).
     */
    fun lenSq(): Float {
        return lengthSq()
    }

    /**
     * Computes the distance between `this` point and [b].
     */
    fun distance(b: Vec3): Float {
        val dx = this.x - b.x
        val dy = this.y - b.y
        val dz = this.z - b.z
        return sqrt(dx * dx + dy * dy + dz * dz)
    }

    /**
     * Computes the distance between `this` point and [b] (alias for [distance]).
     */
    fun dist(b: Vec3): Float {
        return distance(b)
    }

    /**
     * Computes the square of the distance between `this` point and [b].
     */
    fun distanceSq(b: Vec3): Float {
        val dx = this.x - b.x
        val dy = this.y - b.y
        val dz = this.z - b.z
        return dx * dx + dy * dy + dz * dz
    }

    /**
     * Computes the square of the distance between `this` point and [b] (alias for [distanceSq]).
     */
    fun distSq(b: Vec3): Float {
        return distanceSq(b)
    }

    /**
     * Normalizes `this` vector (scales it to unit length).
     */
    fun normalize(dst: Vec3 = Vec3()): Vec3 {
        val l = this.length()
        if (l > EPSILON) {
            dst.x = this.x / l
            dst.y = this.y / l
            dst.z = this.z / l
        } else {
            dst.x = 0f
            dst.y = 0f
            dst.z = 0f
        }
        return dst
    }

    /**
     * Negates `this` vector (multiplies components by -1).
     */
    fun negate(dst: Vec3 = Vec3()): Vec3 {
        dst.x = -this.x
        dst.y = -this.y
        dst.z = -this.z
        return dst
    }

    /**
     * Copies the components of `this`.
     */
    fun copy(dst: Vec3 = Vec3()): Vec3 {
        dst.x = this.x
        dst.y = this.y
        dst.z = this.z
        return dst
    }

    /**
     * Copies the components of `this` (alias for [copy]).
     */
    fun clone(dst: Vec3 = Vec3()): Vec3 {
        return copy(dst)
    }

    /**
     * Multiplies `this` by [b] component-wise.
     */
    fun multiply(b: Vec3, dst: Vec3 = Vec3()): Vec3 {
        dst.x = this.x * b.x
        dst.y = this.y * b.y
        dst.z = this.z * b.z
        return dst
    }

    /**
     * Multiplies `this` by [b] component-wise (alias for [multiply]).
     */
    fun mul(b: Vec3, dst: Vec3 = Vec3()): Vec3 {
        return multiply(b, dst)
    }

    /**
     * Divides `this` by [b] component-wise.
     */
    fun divide(b: Vec3, dst: Vec3 = Vec3()): Vec3 {
        dst.x = this.x / b.x
        dst.y = this.y / b.y
        dst.z = this.z / b.z
        return dst
    }

    /**
     * Divides `this` by [b] component-wise (alias for [divide]).
     */
    fun div(b: Vec3, dst: Vec3 = Vec3()): Vec3 {
        return divide(b, dst)
    }

    /**
     * Transforms `this` vector (point, w=1) by the 4x4 matrix [m].
     */
    fun transformMat4(m: Mat4, dst: Vec3 = Vec3()): Vec3 {
        val x = this.x; val y = this.y; val z = this.z
        var w = (m[3] * x + m[7] * y + m[11] * z + m[15])
        if (w == 0f) {
            w = 1f
        }

        dst.x = (m[0] * x + m[4] * y + m[8] * z + m[12]) / w
        dst.y = (m[1] * x + m[5] * y + m[9] * z + m[13]) / w
        dst.z = (m[2] * x + m[6] * y + m[10] * z + m[14]) / w

        return dst
    }

    /**
     * Transforms `this` vector (direction, w=0) by the upper 3x3 part of the 4x4 matrix [m].
     */
    fun transformMat4Upper3x3(m: Mat4, dst: Vec3 = Vec3()): Vec3 {
        val vx = this.x; val vy = this.y; val vz = this.z

        dst.x = vx * m[0 * 4 + 0] + vy * m[1 * 4 + 0] + vz * m[2 * 4 + 0]
        dst.y = vx * m[0 * 4 + 1] + vy * m[1 * 4 + 1] + vz * m[2 * 4 + 1]
        dst.z = vx * m[0 * 4 + 2] + vy * m[1 * 4 + 2] + vz * m[2 * 4 + 2]

        return dst
    }

    /**
     * Transforms `this` vector (point, w=1) by the 3x3 matrix [m].
     */
    fun transformMat3(m: Mat3, dst: Vec3 = Vec3()): Vec3 {
        // Using standard math (Mat3 * Vec3, Col Major Mat3) as the JS source had inconsistent indices.
        val x = this.x; val y = this.y; val z = this.z
        dst.x = (m[0] * x) + (m[4] * y) + (m[8] * z)
        dst.y = m[1] * x + m[5] * y + m[9] * z
        dst.z = m[2] * x + m[6] * y + m[10] * z
        return dst
    }

    /**
     * Transforms `this` vector by the quaternion [q].
     */
    fun transformQuat(q: Quat, dst: Vec3 = Vec3()): Vec3 {
        // Access quaternion components using properties and ensure they are Float
        val qx = q.x.toFloat(); val qy = q.y.toFloat(); val qz = q.z.toFloat(); val qw = q.w.toFloat()
        // Calculation based on transforming a vector by a quaternion: v' = q * v * conjugate(q)
        // Simplified calculation:
        val x = this.x; val y = this.y; val z = this.z

        // Correct calculation: v' = v + 2.0 * cross(q.xyz, cross(q.xyz, v) + q.w * v)
        // Or using the optimized formula:
        val tx = 2f * (qy * z - qz * y)
        val ty = 2f * (qz * x - qx * z)
        val tz = 2f * (qx * y - qy * x)

        dst.x = x + qw * tx + (qy * tz - qz * ty)
        dst.y = y + qw * ty + (qz * tx - qx * tz)
        dst.z = z + qw * tz + (qx * ty - qy * tx)

        return dst
    }

    /**
     * Rotates `this` vector around the X axis relative to the point [b] by [rad] radians.
     */
    fun rotateX(b: Vec3, rad: Float, dst: Vec3 = Vec3()): Vec3 {
        val px = this.x - b.x
        val py = this.y - b.y
        val pz = this.z - b.z

        val cosRad = cos(rad)
        val sinRad = sin(rad)

        // Perform rotation
        val ry = py * cosRad - pz * sinRad
        val rz = py * sinRad + pz * cosRad

        // Translate back
        dst.x = px + b.x // rx is px
        dst.y = ry + b.y
        dst.z = rz + b.z

        return dst
    }

    /**
     * Rotates `this` vector around the Y axis relative to the point [b] by [rad] radians.
     */
    fun rotateY(b: Vec3, rad: Float, dst: Vec3 = Vec3()): Vec3 {
        val px = this.x - b.x
        val py = this.y - b.y
        val pz = this.z - b.z

        val cosRad = cos(rad)
        val sinRad = sin(rad)

        // Perform rotation
        val rx = pz * sinRad + px * cosRad
        val rz = pz * cosRad - px * sinRad

        // Translate back
        dst.x = rx + b.x
        dst.y = py + b.y // ry is py
        dst.z = rz + b.z

        return dst
    }

    /**
     * Rotates `this` vector around the Z axis relative to the point [b] by [rad] radians.
     */
    fun rotateZ(b: Vec3, rad: Float, dst: Vec3 = Vec3()): Vec3 {
        val px = this.x - b.x
        val py = this.y - b.y
        val pz = this.z - b.z

        val cosRad = cos(rad)
        val sinRad = sin(rad)

        // Perform rotation
        val rx = px * cosRad - py * sinRad
        val ry = px * sinRad + py * cosRad

        // Translate back
        dst.x = rx + b.x
        dst.y = ry + b.y
        dst.z = pz + b.z // rz is pz

        return dst
    }

    /**
     * Sets the length of `this` vector to [len].
     */
    fun setLength(len: Float, dst: Vec3 = Vec3()): Vec3 {
        this.normalize(dst) // Normalizes into dst
        return dst.mulScalar(len, dst) // Scales dst in place
    }

    /**
     * Truncates `this` vector if its length exceeds [maxLen].
     */
    fun truncate(maxLen: Float, dst: Vec3 = Vec3()): Vec3 {
        val currentLength = this.length()
        if (currentLength > maxLen) {
            return this.setLength(maxLen, dst)
        }
        return this.copy(dst)
    }

    /**
     * Calculates the midpoint between `this` vector and [b].
     */
    fun midpoint(b: Vec3, dst: Vec3 = Vec3()): Vec3 {
        return this.lerp(b, 0.5f, dst)
    }

    /**
     * Sets the components of `this` to [x], [y], and [z].
     * @return `this`
     */
    fun set(x: Float, y: Float, z: Float): Vec3 {
        this.x = x
        this.y = y
        this.z = z
        return this
    }

    /**
     * Sets the components of `this` vector to 0.
     * @return `this`
     */
    fun zero(): Vec3 {
        this.x = 0f
        this.y = 0f
        this.z = 0f
        return this
    }

    // --- Companion Object for static-like methods ---
    companion object {
        /**
         * Creates a vec3 with initial values [x], [y], and [z].
         */
        fun create(x: Float = 0f, y: Float = 0f, z: Float = 0f): Vec3 {
            return Vec3(x, y, z)
        }

        /**
         * Creates a vec3 with initial values [x], [y], and [z] (alias for [create]).
         */
        fun fromValues(x: Float = 0f, y: Float = 0f, z: Float = 0f): Vec3 {
            return create(x, y, z)
        }

        /**
         * Sets the components of [dst] to [x], [y], and [z].
         */
        fun set(x: Float, y: Float, z: Float, dst: Vec3 = Vec3()): Vec3 {
            dst.x = x
            dst.y = y
            dst.z = z
            return dst
        }

        /**
         * Creates a random vector within a sphere of radius [scale].
         */
        fun random(scale: Float = 1f, dst: Vec3 = Vec3()): Vec3 {
            val angle = Random.nextFloat() * 2f * PI.toFloat()
            val z = Random.nextFloat() * 2f - 1f
            val zScale = sqrt(1f - z * z) * scale
            dst.x = cos(angle) * zScale
            dst.y = sin(angle) * zScale
            dst.z = z * scale
            return dst
        }

        /**
         * Creates a zero vector (components are 0).
         */
        fun zero(dst: Vec3 = Vec3()): Vec3 {
            dst.x = 0f
            dst.y = 0f
            dst.z = 0f
            return dst
        }

        /**
         * Gets the translation component of the 4x4 matrix [m].
         */
        fun getTranslation(m: Mat4, dst: Vec3 = Vec3()): Vec3 {
            dst.x = m[12]
            dst.y = m[13]
            dst.z = m[14]
            return dst
        }

        /**
         * Gets the specified [axis] (0=x, 1=y, 2=z) of the 4x4 matrix [m].
         */
        fun getAxis(m: Mat4, axis: Int, dst: Vec3 = Vec3()): Vec3 {
            val off = axis * 4
            dst.x = m[off + 0]
            dst.y = m[off + 1]
            dst.z = m[off + 2]
            return dst
        }

        /**
         * Gets the scaling component of the 4x4 matrix [m].
         */
        fun getScaling(m: Mat4, dst: Vec3 = Vec3()): Vec3 {
            val xColX = m[0]; val xColY = m[1]; val xColZ = m[2]
            val yColX = m[4]; val yColY = m[5]; val yColZ = m[6]
            val zColX = m[8]; val zColY = m[9]; val zColZ = m[10]

            dst.x = sqrt(xColX * xColX + xColY * xColY + xColZ * xColZ)
            dst.y = sqrt(yColX * yColX + yColY * yColY + yColZ * yColZ)
            dst.z = sqrt(zColX * zColX + zColY * zColY + zColZ * zColZ)
            return dst
        }
    }

    // Override toString for better debugging/logging
    override fun toString(): String {
        return "Vec3(x=$x, y=$y, z=$z)"
    }

    // Override equals and hashCode for proper comparisons and use in collections
    // Note: This provides standard Kotlin equality, different from the JS 'equals'
    // and 'equalsApproximately' which are preserved as explicit methods.
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Vec3

        if (x != other.x) return false
        if (y != other.y) return false
        if (z != other.z) return false

        return true
    }

    override fun hashCode(): Int {
        var result = x.hashCode()
        result = 31 * result + y.hashCode()
        result = 31 * result + z.hashCode()
        return result
    }
}
