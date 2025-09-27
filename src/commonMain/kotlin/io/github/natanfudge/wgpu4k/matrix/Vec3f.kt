@file:Suppress("NOTHING_TO_INLINE")

package io.github.natanfudge.wgpu4k.matrix

import io.github.natanfudge.wgpu4k.matrix.Vec3f.Companion.ACTUAL_SIZE_BYTES
import io.github.natanfudge.wgpu4k.matrix.Vec3f.Companion.ALIGN_BYTES
import io.github.natanfudge.wgpu4k.matrix.Vec3f.Companion.create
import kotlinx.serialization.Serializable
import kotlin.math.*
import kotlin.random.Random


/**
 * Represents a 3-component vector using individual x, y, z fields.
 */
@Serializable
data class Vec3f(
    var x: Float,
    var y: Float,
    var z: Float,
) {
    constructor() : this(0f, 0f, 0f)

    /**
     * Converts this into an array, including the padding value to reach 16 bytes, matching [ALIGN_BYTES].
     */
    fun toAlignedArray() = floatArrayOf(x, y, z, 0f)

    /**
     * Converts this into an array, not including the padding value, only reaching 12 bytes, matching [ACTUAL_SIZE_BYTES].
     */
    fun toCompactArray() = floatArrayOf(x, y, z)

    companion object {
        /**
         * Just 3 * 4, suitable for aligning vec3f in vertex attributes.
         *  for uniform and storage buffers you usually want [ALIGN_BYTES]
         */
        const val ACTUAL_SIZE_BYTES = 12u

        /**
         * WebGPU vec3f actually takes 16 bytes in uniform and storage buffers because of alignment.
         * For vertex attributes you usually want [ACTUAL_SIZE_BYTES]
         */
        const val ALIGN_BYTES = 16u
        const val ALIGN_ELEMENT_COUNT = 4u
        const val ELEMENT_COUNT = 3u

        /**
         * Creates a vec3 with initial values [x], [y], and [z].
         */
        fun create(x: Float = 0f, y: Float = 0f, z: Float = 0f): Vec3f {
            return Vec3f(x, y, z)
        }

        /**
         * Creates a vec3 with initial values [x], [y], and [z] (alias for [create]).
         */
        fun fromValues(x: Float = 0f, y: Float = 0f, z: Float = 0f): Vec3f {
            return create(x, y, z)
        }

        /**
         * Sets the components of [dst] to [x], [y], and [z].
         */
        fun set(x: Float, y: Float, z: Float, dst: Vec3f = Vec3f()): Vec3f {
            dst.x = x
            dst.y = y
            dst.z = z
            return dst
        }

        /**
         * Creates a random vector within a sphere of radius [scale].
         */
        fun random(scale: Float = 1f, dst: Vec3f = Vec3f()): Vec3f {
            val angle = Random.nextFloat() * 2f * FloatPi
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
        fun zero(dst: Vec3f = Vec3f()): Vec3f {
            dst.x = 0f
            dst.y = 0f
            dst.z = 0f
            return dst
        }

        /**
         * Gets the translation component of the 4x4 matrix [m].
         */
        fun getTranslation(m: Mat4f, dst: Vec3f = Vec3f()): Vec3f {
            dst.x = m[12]
            dst.y = m[13]
            dst.z = m[14]
            return dst
        }

        /**
         * Gets the specified [axis] (0=x, 1=y, 2=z) of the 4x4 matrix [m].
         */
        fun getAxis(m: Mat4f, axis: Int, dst: Vec3f = Vec3f()): Vec3f {
            val off = axis * 4
            dst.x = m[off + 0]
            dst.y = m[off + 1]
            dst.z = m[off + 2]
            return dst
        }

        /**
         * Gets the scaling component of the 4x4 matrix [m].
         */
        fun getScaling(m: Mat4f, dst: Vec3f = Vec3f()): Vec3f {
            val xColX = m[0];
            val xColY = m[1];
            val xColZ = m[2]
            val yColX = m[4];
            val yColY = m[5];
            val yColZ = m[6]
            val zColX = m[8];
            val zColY = m[9];
            val zColZ = m[10]

            dst.x = sqrt(xColX * xColX + xColY * xColY + xColZ * xColZ)
            dst.y = sqrt(yColX * yColX + yColY * yColY + yColZ * yColZ)
            dst.z = sqrt(zColX * zColX + zColY * zColY + zColZ * zColZ)
            return dst
        }

        /**
         * Converts a 4D homogeneous vector to a 3D Cartesian vector by performing perspective division.
         *
         * This is typically used after applying a projection matrix, where the resulting `Vec4f`
         * needs to be converted back into 3D space by dividing x, y, and z by w.
         *
         * */
        fun fromHomogenous(vec4f: Vec4f, dst: Vec3f = Vec3f()): Vec3f {
            dst.x = vec4f.x / vec4f.w
            dst.y = vec4f.y / vec4f.w
            dst.z = vec4f.z / vec4f.w
            return dst
        }


        // No static operators in Vec3f
    }

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

    inline operator fun plus(other: Vec3f) = add(other)
    inline fun plusX(increase: Float, dst: Vec3f = Vec3f()): Vec3f {
        dst.x = this.x + increase
        dst.y = this.y
        dst.z = this.z
        return dst
    }

    inline fun plusY(increase: Float, dst: Vec3f = Vec3f()): Vec3f {
        dst.x = this.x
        dst.y = this.y + increase
        dst.z = this.z
        return dst
    }

    inline fun plusZ(increase: Float, dst: Vec3f = Vec3f()): Vec3f {
        dst.x = this.x
        dst.y = this.y
        dst.z = this.z + increase
        return dst
    }

    inline operator fun minus(other: Vec3f) = subtract(other)
    inline operator fun times(scalar: Float) = mulScalar(scalar)

    /**
     * Component-wise (Hadamard) product of `this` and [other].
     * Important note: NOT cross product, and NOT dot product!
     */
    operator fun times(other: Vec3f) = Vec3f(this.x * other.x, this.y * other.y, this.z * other.z)
    inline operator fun div(scalar: Float) = divScalar(scalar)
    inline operator fun unaryMinus() = negate()

    /**
     * Computes the length (magnitude) of `this` vector.
     */
    val length: Float
        get() = sqrt(this.x * this.x + this.y * this.y + this.z * this.z)

    inline val norm: Float get() = length

    /**
     * Computes the length (magnitude) of `this` vector (alias for [length]).
     */
    val len: Float
        get() = length

    /**
     * Computes the square of the length of `this` vector. Faster than [length] if only comparing magnitudes.
     */
    val lengthSq: Float
        get() = this.x * this.x + this.y * this.y + this.z * this.z

    inline fun lengthSquared() = lengthSq

    /**
     * Computes the square of the length of `this` vector (alias for [lengthSq]).
     */
    val lenSq: Float
        get() = lengthSq

    val isZero get() = this.x == 0f && this.y == 0f && this.z == 0f

    /**
     * Sets this vector to the zero vec3
     */
    fun setZero() {
        zero(this)
    }

    fun absoluteValue(dst: Vec3f = Vec3f()): Vec3f {
        dst.x = abs(this.x)
        dst.y = abs(this.y)
        dst.z = abs(this.z)
        return dst
    }


    /**
     * Computes the ceiling of each component of `this`.
     */
    fun ceil(dst: Vec3f = Vec3f()): Vec3f {
        dst.x = ceil(this.x)
        dst.y = ceil(this.y)
        dst.z = ceil(this.z)
        return dst
    }

    /**
     * Computes the floor of each component of `this`.
     */
    fun floor(dst: Vec3f = Vec3f()): Vec3f {
        dst.x = floor(this.x)
        dst.y = floor(this.y)
        dst.z = floor(this.z)
        return dst
    }

    /**
     * Computes the rounded value of each component of `this`.
     */
    fun round(dst: Vec3f = Vec3f()): Vec3f {
        dst.x = round(this.x)
        dst.y = round(this.y)
        dst.z = round(this.z)
        return dst
    }

    /**
     * Computes the component-wise inverse (1/x) of `this` vector.
     */
    fun inverse(dst: Vec3f = Vec3f()): Vec3f {
        dst.x = 1f / this.x
        dst.y = 1f / this.y
        dst.z = 1f / this.z
        return dst
    }

    /**
     * Computes the component-wise inverse (1/x) of `this` vector (alias for [inverse]).
     */
    fun invert(dst: Vec3f = Vec3f()): Vec3f {
        return inverse(dst)
    }


    /**
     * Normalizes `this` vector (scales it to unit length).
     */
    fun normalize(dst: Vec3f = Vec3f()): Vec3f {
        val l = this.length
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

    inline fun normalized(dst: Vec3f = Vec3f()): Vec3f = normalize(dst)


    /**
     * Negates `this` vector (multiplies components by -1).
     */
    fun negate(dst: Vec3f = Vec3f()): Vec3f {
        dst.x = -this.x
        dst.y = -this.y
        dst.z = -this.z
        return dst
    }

    /**
     * Copies the components of `this`.
     */
    fun copy(x: Float = this.x, y: Float = this.y, z: Float = this.z, dst: Vec3f = Vec3f()): Vec3f {
        dst.x = x
        dst.y = y
        dst.z = z
        return dst
    }

    /**
     * Sets `dst` to `v[axis] = value` and returns it
     */
    fun copy(axis: Int, value: Float, dst: Vec3f = Vec3f()): Vec3f = when (axis) {
        0 -> copy(x = value, dst = dst)
        1 -> copy(y = value, dst = dst)
        2 -> copy(z = value, dst = dst)
        else -> throw IndexOutOfBoundsException("Invalid axis index: $axis")
    }

    /**
     * Copies the components of `this` (alias for [copy]).
     */
    inline fun clone(x: Float = this.x, y: Float = this.y, z: Float = this.z, dst: Vec3f = Vec3f()): Vec3f = copy(x, y, z, dst)

    /**
     * Sets the components of `this` vector to 0.
     * @return `this`
     */
    fun zero(): Vec3f {
        this.x = 0f
        this.y = 0f
        this.z = 0f
        return this
    }

    /**
     * Adds [b] to `this`.
     */
    fun add(b: Vec3f, dst: Vec3f = Vec3f()): Vec3f {
        dst.x = this.x + b.x
        dst.y = this.y + b.y
        dst.z = this.z + b.z
        return dst
    }


    /**
     * Computes the angle in radians between `this` and [b].
     */
    fun angle(b: Vec3f): Float {
        val mag1 = this.length // Use instance length property
        val mag2 = b.length
        val mag = mag1 * mag2
        val cosine = if (mag != 0f) this.dot(b) / mag else 0f // Use instance dot method
        // Clamp cosine to avoid floating point errors leading to NaN in acos
        val clampedCosine = max(-1f, min(1f, cosine))
        return acos(clampedCosine)
    }

    /**
     * Subtracts [b] from `this`.
     */
    fun subtract(b: Vec3f, dst: Vec3f = Vec3f()): Vec3f {
        dst.x = this.x - b.x
        dst.y = this.y - b.y
        dst.z = this.z - b.z
        return dst
    }

    /**
     * Subtracts [b] from `this` (alias for [subtract]).
     */
    fun sub(b: Vec3f, dst: Vec3f = Vec3f()): Vec3f {
        return subtract(b, dst)
    }

    /**
     * Computes the component-wise maximum of `this` and [b].
     */
    fun max(b: Vec3f, dst: Vec3f = Vec3f()): Vec3f {
        dst.x = max(this.x, b.x)
        dst.y = max(this.y, b.y)
        dst.z = max(this.z, b.z)
        return dst
    }

    /**
     * Computes the component-wise minimum of `this` and [b].
     */
    fun min(b: Vec3f, dst: Vec3f = Vec3f()): Vec3f {
        dst.x = min(this.x, b.x)
        dst.y = min(this.y, b.y)
        dst.z = min(this.z, b.z)
        return dst
    }

    /**
     * Multiplies `this` by scalar [k].
     */
    fun mulScalar(k: Float, dst: Vec3f = Vec3f()): Vec3f {
        dst.x = this.x * k
        dst.y = this.y * k
        dst.z = this.z * k
        return dst
    }

    /**
     * Multiplies `this` by scalar [k] (alias for [mulScalar]).
     */
    fun scale(k: Float, dst: Vec3f = Vec3f()): Vec3f {
        return mulScalar(k, dst)
    }

    /**
     * Divides `this` by scalar [k].
     */
    fun divScalar(k: Float, dst: Vec3f = Vec3f()): Vec3f {
        dst.x = this.x / k
        dst.y = this.y / k
        dst.z = this.z / k
        return dst
    }

    /**
     * Computes the cross product of `this` and [b].
     */
    fun cross(b: Vec3f, dst: Vec3f = Vec3f()): Vec3f {
        val ax = this.x;
        val ay = this.y;
        val az = this.z
        val bx = b.x;
        val by = b.y;
        val bz = b.z

        dst.x = ay * bz - az * by
        dst.y = az * bx - ax * bz
        dst.z = ax * by - ay * bx

        return dst
    }

    inline infix fun cross(b: Vec3f) = cross(b, Vec3f())

    /**
     * Computes the dot product of `this` and [b].
     */
    infix fun dot(b: Vec3f): Float {
        return (this.x * b.x) + (this.y * b.y) + (this.z * b.z)
    }


    /**
     * Computes the distance between `this` point and [b].
     */
    fun distance(b: Vec3f): Float {
        val dx = this.x - b.x
        val dy = this.y - b.y
        val dz = this.z - b.z
        return sqrt(dx * dx + dy * dy + dz * dz)
    }

    /**
     * Computes the distance between `this` point and [b] (alias for [distance]).
     */
    fun dist(b: Vec3f): Float {
        return distance(b)
    }

    /**
     * Computes the square of the distance between `this` point and [b].
     */
    fun distanceSq(b: Vec3f): Float {
        val dx = this.x - b.x
        val dy = this.y - b.y
        val dz = this.z - b.z
        return dx * dx + dy * dy + dz * dz
    }

    /**
     * Computes the square of the distance between `this` point and [b] (alias for [distanceSq]).
     */
    fun distSq(b: Vec3f): Float {
        return distanceSq(b)
    }

    /**
     * Multiplies `this` by [b] component-wise.
     */
    fun multiply(b: Vec3f, dst: Vec3f = Vec3f()): Vec3f {
        dst.x = this.x * b.x
        dst.y = this.y * b.y
        dst.z = this.z * b.z
        return dst
    }

    /**
     * Multiplies `this` by [b] component-wise (alias for [multiply]).
     */
    fun mul(b: Vec3f, dst: Vec3f = Vec3f()): Vec3f {
        return multiply(b, dst)
    }

    /**
     * Divides `this` by [b] component-wise.
     */
    fun divide(b: Vec3f, dst: Vec3f = Vec3f()): Vec3f {
        dst.x = this.x / b.x
        dst.y = this.y / b.y
        dst.z = this.z / b.z
        return dst
    }

    /**
     * Divides `this` by [b] component-wise (alias for [divide]).
     */
    fun div(b: Vec3f, dst: Vec3f = Vec3f()): Vec3f {
        return divide(b, dst)
    }

    /**
     * Transforms `this` vector (point, w=1) by the 4x4 matrix [m].
     */
    fun transformMat4(m: Mat4f, dst: Vec3f = Vec3f()): Vec3f {
        val x = this.x;
        val y = this.y;
        val z = this.z
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
    fun transformMat4Upper3x3(m: Mat4f, dst: Vec3f = Vec3f()): Vec3f {
        val vx = this.x;
        val vy = this.y;
        val vz = this.z

        dst.x = vx * m[0] + vy * m[4] + vz * m[8]
        dst.y = vx * m[1] + vy * m[5] + vz * m[9]
        dst.z = vx * m[2] + vy * m[6] + vz * m[10]

        return dst
    }

    /**
     * Transforms `this` vector (point, w=1) by the 3x3 matrix [m].
     */
    fun transformMat3(m: Mat3f, dst: Vec3f = Vec3f()): Vec3f {
        // Using standard math (Mat3 * Vec3, Col Major Mat3) as the JS source had inconsistent indices.
        val x = this.x;
        val y = this.y;
        val z = this.z
        dst.x = (m[0] * x) + (m[4] * y) + (m[8] * z)
        dst.y = m[1] * x + m[5] * y + m[9] * z
        dst.z = m[2] * x + m[6] * y + m[10] * z
        return dst
    }

    /**
     * Transforms `this` vector by the quaternion [q].
     */
    fun transformQuat(q: Quatf, dst: Vec3f = Vec3f()): Vec3f {
        val qx = q.x;
        val qy = q.y;
        val qz = q.z;
        val w2 = q.w * 2
        val x = this.x;
        val y = this.y;
        val z = this.z

        val uvX = qy * z - qz * y;
        val uvY = qz * x - qx * z;
        val uvZ = qx * y - qy * x;

        dst.x = x + uvX * w2 + (qy * uvZ - qz * uvY) * 2;
        dst.y = y + uvY * w2 + (qz * uvX - qx * uvZ) * 2;
        dst.z = z + uvZ * w2 + (qx * uvY - qy * uvX) * 2;

        return dst
    }

    /**
     * Sets the length of `this` vector to [len].
     */
    fun setLength(len: Float, dst: Vec3f = Vec3f()): Vec3f {
        this.normalize(dst) // Normalizes into dst
        return dst.mulScalar(len, dst) // Scales dst in place
    }

    /**
     * Truncates `this` vector if its length exceeds [maxLen].
     */
    fun truncate(maxLen: Float, dst: Vec3f = Vec3f()): Vec3f {
        val currentLength = this.length
        if (currentLength > maxLen) {
            return this.setLength(maxLen, dst)
        }
        return this.copy(dst = dst)
    }

    /**
     * Calculates the midpoint between `this` vector and [b].
     */
    fun midpoint(b: Vec3f, dst: Vec3f = Vec3f()): Vec3f {
        return this.lerp(b, 0.5f, dst)
    }

    /**
     * Clamps each component of `this` between [min] and [max].
     */
    fun clamp(min: Float = 0f, max: Float = 1f, dst: Vec3f = Vec3f()): Vec3f {
        dst.x = min(max, max(min, this.x))
        dst.y = min(max, max(min, this.y))
        dst.z = min(max, max(min, this.z))
        return dst
    }

    /**
     * Adds [b] scaled by [scale] to `this`.
     */
    fun addScaled(b: Vec3f, scale: Float, dst: Vec3f = Vec3f()): Vec3f {
        dst.x = this.x + b.x * scale
        dst.y = this.y + b.y * scale
        dst.z = this.z + b.z * scale
        return dst
    }

    /**
     * Checks if `this` and [b] are approximately equal.
     */
    fun equalsApproximately(b: Vec3f, tolerance: Float = EPSILON): Boolean {
        return abs(this.x - b.x) < tolerance &&
                abs(this.y - b.y) < tolerance &&
                abs(this.z - b.z) < tolerance
    }

    /**
     * Linearly interpolates between `this` and [b] using coefficient [t].
     */
    fun lerp(b: Vec3f, t: Float, dst: Vec3f = Vec3f()): Vec3f {
        dst.x = this.x + t * (b.x - this.x)
        dst.y = this.y + t * (b.y - this.y)
        dst.z = this.z + t * (b.z - this.z)
        return dst
    }

    /**
     * Performs component-wise linear interpolation between `this` and [b] using coefficient vector [t].
     */
    fun lerpV(b: Vec3f, t: Vec3f, dst: Vec3f = Vec3f()): Vec3f {
        dst.x = this.x + t.x * (b.x - this.x)
        dst.y = this.y + t.y * (b.y - this.y)
        dst.z = this.z + t.z * (b.z - this.z)
        return dst
    }

    /**
     * Rotates `this` vector around the X axis relative to the point [b] by [rad] radians.
     */
    fun rotateX(b: Vec3f, rad: Float, dst: Vec3f = Vec3f()): Vec3f {
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
    fun rotateY(b: Vec3f, rad: Float, dst: Vec3f = Vec3f()): Vec3f {
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
    fun rotateZ(b: Vec3f, rad: Float, dst: Vec3f = Vec3f()): Vec3f {
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
     * Sets the components of `this` to [x], [y], and [z].
     * @return `this`
     */
    fun set(x: Float, y: Float, z: Float): Vec3f {
        this.x = x
        this.y = y
        this.z = z
        return this
    }

    /**
     * Sets the values of `this` equal to the values of [other].
     */
    fun set(other: Vec3f): Vec3f {
        this.x = other.x
        this.y = other.y
        this.z = other.z
        return this
    }

    /**
     * Converts this into a `Vec4f` with `w = 1`
     */
    fun toVec4f() = Vec4f(x, y, z, 1f)

    /**
     * @param round if true, floating point values will look nicer by doing some rounding operations. The default is true.
     */
    fun toString(round: Boolean): String = if (round) "(${x.ns},${y.ns},${z.ns})" else "($x,$y,$z)"
    override fun toString(): String = toString(round = true)
}
