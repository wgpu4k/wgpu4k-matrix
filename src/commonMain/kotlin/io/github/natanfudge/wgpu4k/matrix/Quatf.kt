@file:Suppress("NOTHING_TO_INLINE")

package io.github.natanfudge.wgpu4k.matrix

import io.github.natanfudge.wgpu4k.matrix.Quatf.Companion.create
import kotlin.math.*

typealias RotationOrder = String

/**
 * Represents a quaternion for 3D rotations.
 *
 * @property x The x component (imaginary part i).
 * @property y The y component (imaginary part j).
 * @property z The z component (imaginary part k).
 * @property w The w component (real part).
 */
data class Quatf(
    var x: Float,
    var y: Float,
    var z: Float,
    var w: Float,
) {
    constructor() : this(0f, 0f, 0f, 1f)

    companion object {
        // 4 * 4 bytes
        const val SIZE_BYTES = 16u

        // Static temporary variables to avoid allocation in methods like rotationTo
        // Note: Be cautious with static mutable state in concurrent environments if applicable.
        private val tempVec3 = Vec3f()
        private val xUnitVec3 = Vec3f(1f, 0f, 0f)
        private val yUnitVec3 = Vec3f(0f, 1f, 0f)
        private val tempQuat1 = Quatf()
        private val tempQuat2 = Quatf()


        /**
         * Creates a Quat with initial values [x], [y], [z], [w].
         * Defaults to the identity quaternion (0, 0, 0, 1).
         */
        fun create(x: Float = 0.0f, y: Float = 0.0f, z: Float = 0.0f, w: Float = 1.0f): Quatf {
            return Quatf(x, y, z, w)
        }

        /**
         * Creates a Quat with initial values [x], [y], [z], [w] (alias for [create]).
         * Defaults to the identity quaternion (0, 0, 0, 1).
         */
        fun fromValues(x: Float = 0.0f, y: Float = 0.0f, z: Float = 0.0f, w: Float = 1.0f): Quatf {
            return Quatf(x, y, z, w)
        }

        /**
         * Creates an identity quaternion (0, 0, 0, 1).
         */
        fun identity(dst: Quatf = Quatf()): Quatf {
            dst.x = 0.0f
            dst.y = 0.0f
            dst.z = 0.0f
            dst.w = 1.0f
            return dst
        }

        /**
         * Creates a quaternion representing a rotation of [angleInRadians] around the normalized [axis].
         **/
        fun fromAxisAngle(axis: Vec3f, angleInRadians: Float, dst: Quatf = Quatf()): Quatf {
            val halfAngle = angleInRadians * 0.5f
            val s = sin(halfAngle)
            dst.x = s * axis.x // Use property access
            dst.y = s * axis.y // Use property access
            dst.z = s * axis.z // Use property access
            dst.w = cos(halfAngle)
            return dst
        }

        /**
         * Creates a quaternion from the given rotation matrix [m] (Mat3 or Mat4).
         * The created quaternion is not normalized.
         */
        fun fromMat(m: Any, dst: Quatf = Quatf()): Quatf {
            // Check if it's Mat3 or Mat4 based on expected property/method or size if it's an array-like structure
            // This requires knowing the Mat3/Mat4 implementation. Assuming indexer `[]` access for now.
            // Expect Float from matrix indexers
            val getElement: (Int) -> Float = when (m) {
                is Mat3f -> { index -> m[index] }
                is Mat4f -> { index -> m[index] }
                else -> throw IllegalArgumentException("Input matrix must be Mat3 or Mat4, but is $m")
            }

            // Indices for a 3x3 or the upper-left 3x3 of a 4x4 matrix in column-major order
            // m[0]=m00, m[1]=m10, m[2]=m20
            // m[4]=m01, m[5]=m11, m[6]=m21
            // m[8]=m02, m[9]=m12, m[10]=m22

            val m00 = getElement(0)
            val m10 = getElement(1) // unused directly in trace calculation below but used in elements
            val m20 = getElement(2) // unused directly
            val m01 = getElement(4)
            val m11 = getElement(5)
            val m21 = getElement(6)
            val m02 = getElement(8)
            val m12 = getElement(9)
            val m22 = getElement(10)

            val trace = m00 + m11 + m22

            if (trace > 0.0f) {
                val root = sqrt(trace + 1.0f)
                dst.w = 0.5f * root
                val invRoot = 0.5f / root
                dst.x = (m21 - m12) * invRoot
                dst.y = (m02 - m20) * invRoot
                dst.z = (m10 - m01) * invRoot
            } else {
                // Find the major diagonal element with the largest value
                var i = 0
                if (m11 > m00) i = 1
                if (m22 > getElement(i * 4 + i)) i = 2 // Check against m[0], m[5], or m[10]

                val j = (i + 1) % 3
                val k = (i + 2) % 3

                // Indices based on i, j, k mapping to 0, 1, 2
                val ii = i * 4 + i // Index of m[ii]
                val jj = j * 4 + j // Index of m[jj]
                val kk = k * 4 + k // Index of m[kk]
                val ij = i * 4 + j // Index of m[ij]
                val ji = j * 4 + i // Index of m[ji]
                val ik = i * 4 + k // Index of m[ik]
                val ki = k * 4 + i // Index of m[ki]
                val jk = j * 4 + k // Index of m[jk]
                val kj = k * 4 + j // Index of m[kj]


                val root = sqrt(getElement(ii) - getElement(jj) - getElement(kk) + 1.0f)
                val quatComp = floatArrayOf(0.0f, 0.0f, 0.0f) // Temporary array for x, y, z
                quatComp[i] = 0.5f * root
                val invRoot = 0.5f / root
                dst.w = (getElement(jk) - getElement(kj)) * invRoot
                quatComp[j] = (getElement(ji) + getElement(ij)) * invRoot
                quatComp[k] = (getElement(ki) + getElement(ik)) * invRoot

                dst.x = quatComp[0]
                dst.y = quatComp[1]
                dst.z = quatComp[2]
            }

            return dst
        }

        /**
         * Constructs a quaternion from Euler angles with a specified application order.
         *
         * This method interprets the three input angles as intrinsic rotations about the
         * local axes of the object (not world axes).  Given angles (x, y, z), and an
         * order string such as "xyz" or "zyx", the rotations are applied in the
         * sequence of letters from right to left: the quaternion multiplication is
         * computed as `q = q_order[2] ⊗ q_order[1] ⊗ q_order[0]`
         *
         * For example, with order = "zyx":
         *  1. Rotate by xAngle about the X-axis (q<sub>x</sub>)
         *  2. Rotate by yAngle about the Y-axis (q<sub>y</sub>)
         *  3. Rotate by zAngle about the Z-axis (q<sub>z</sub>)
         *
         * Then the final quaternion is:
         *  q = q<sub>z</sub> ⊗ q<sub>y</sub> ⊗ q<sub>x</sub>
         *
         * Note:
         *  - Angles are in radians.
         *  - All sine/cosine computations use half-angles internally.
         *  - The returned quaternion represents the combined rotation.
         *
         * @param xAngleInRadians Rotation about the X-axis, in radians.
         * @param yAngleInRadians Rotation about the Y-axis, in radians.
         * @param zAngleInRadians Rotation about the Z-axis, in radians.
         * @param order         Three-character string specifying axis application order.
         *                      Supported values: "xyz", "xzy", "yxz", "yzx", "zxy", "zyx".
         * @param dst           Destination quaternion to hold the result (optional).
         * @return              A quaternion representing the composite rotation.
         */
        fun fromEuler(
            xAngleInRadians: Float,
            yAngleInRadians: Float,
            zAngleInRadians: Float,
            order: RotationOrder,
            dst: Quatf = Quatf(),
        ): Quatf {

            val xHalfAngle = xAngleInRadians * 0.5f
            val yHalfAngle = yAngleInRadians * 0.5f
            val zHalfAngle = zAngleInRadians * 0.5f

            val sx = sin(xHalfAngle)
            val cx = cos(xHalfAngle)
            val sy = sin(yHalfAngle)
            val cy = cos(yHalfAngle)
            val sz = sin(zHalfAngle)
            val cz = cos(zHalfAngle)

            when (order.lowercase()) {
                "xyz" -> {
                    dst.x = sx * cy * cz + cx * sy * sz
                    dst.y = cx * sy * cz - sx * cy * sz
                    dst.z = cx * cy * sz + sx * sy * cz
                    dst.w = cx * cy * cz - sx * sy * sz
                }

                "xzy" -> {
                    dst.x = sx * cy * cz - cx * sy * sz
                    dst.y = cx * sy * cz - sx * cy * sz
                    dst.z = cx * cy * sz + sx * sy * cz
                    dst.w = cx * cy * cz + sx * sy * sz
                }

                "yxz" -> {
                    dst.x = sx * cy * cz + cx * sy * sz
                    dst.y = cx * sy * cz - sx * cy * sz
                    dst.z = cx * cy * sz - sx * sy * cz
                    dst.w = cx * cy * cz + sx * sy * sz
                }

                "yzx" -> {
                    dst.x = sx * cy * cz + cx * sy * sz
                    dst.y = cx * sy * cz + sx * cy * sz
                    dst.z = cx * cy * sz - sx * sy * cz
                    dst.w = cx * cy * cz - sx * sy * sz
                }

                "zxy" -> {
                    dst.x = sx * cy * cz - cx * sy * sz
                    dst.y = cx * sy * cz + sx * cy * sz
                    dst.z = cx * cy * sz + sx * sy * cz
                    dst.w = cx * cy * cz - sx * sy * sz
                }

                "zyx" -> {
                    dst.x = sx * cy * cz - cx * sy * sz
                    dst.y = cx * sy * cz + sx * cy * sz
                    dst.z = cx * cy * sz - sx * sy * cz
                    dst.w = cx * cy * cz + sx * sy * sz
                }

                else -> throw Error("Unknown rotation order: $order")
            }
            return dst
        }


        /**
         * Computes a quaternion representing the shortest rotation from unit vector [aUnit] to unit vector [bUnit].
         * This method is NOT thread safe as it uses static temporary variables.
         */
        fun rotationToUnsafe(aUnit: Vec3f, bUnit: Vec3f, dst: Quatf = Quatf()): Quatf {
            val dot = aUnit.dot(bUnit) // Vec3.dot returns Float

            if (dot < -0.999999f) {
                // Vectors are opposite, need an arbitrary axis orthogonal to aUnit
                // Cross product order matters: axis = a x defaultAxis
                xUnitVec3.cross(aUnit, tempVec3) // tempVec3 = xUnitVec3 x aUnit
                if (tempVec3.lenSq < 0.000001f) { // Use instance method lenSq()
                    yUnitVec3.cross(aUnit, tempVec3) // tempVec3 = yUnitVec3 x aUnit
                }
                tempVec3.normalize(tempVec3) // Use instance method
                fromAxisAngle(tempVec3, FloatPi, dst) // PI needs to be Float
                return dst
            } else if (dot > 0.999999f) {
                // Vectors are same direction
                dst.x = 0.0f
                dst.y = 0.0f
                dst.z = 0.0f
                dst.w = 1.0f
                return dst
            } else {
                // General case
                aUnit.cross(bUnit, tempVec3) // Use instance method tempVec3 = aUnit x bUnit
                dst.x = tempVec3.x // Already Float
                dst.y = tempVec3.y // Already Float
                dst.z = tempVec3.z // Already Float
                dst.w = 1.0f + dot
                return dst.normalize(dst) // Normalize the result
            }
        }


        /**
         * Performs a spherical linear interpolation with two control points (Squad) using keyframes [a], [b], [c], [d]
         * and interpolation coefficient [t].
         * q(t) = Slerp(Slerp(a, d, t), Slerp(b, c, t), 2t(1-t))
         * This method is NOT thread safe as it uses static temporary variables.
         */
        fun sqlerpUnsafe(
            a: Quatf,
            b: Quatf,
            c: Quatf,
            d: Quatf,
            t: Float,
            dst: Quatf = Quatf(),
        ): Quatf {
            // Use instance slerp method
            a.slerp(d, t, tempQuat1)
            b.slerp(c, t, tempQuat2)
            tempQuat1.slerp(tempQuat2, 2.0f * t * (1.0f - t), dst)
            return dst
        }
        // No static operators in Quatf
    }

    inline operator fun plus(other: Quatf) = add(other)
    inline operator fun minus(other: Quatf) = subtract(other) // Assuming subtract exists or is added
    inline operator fun times(scalar: Float) = mulScalar(scalar)
    inline operator fun times(other: Quatf) = multiply(other)
    inline operator fun div(scalar: Float) = divScalar(scalar)
    inline operator fun div(quat: Quatf) = this * quat.inverse()
    inline operator fun unaryMinus() = negate()

    /**
     * Computes the length (magnitude) of `this` quaternion.
     */
    val length: Float
        get() = sqrt(this.x * this.x + this.y * this.y + this.z * this.z + this.w * this.w)

    /**
     * Computes the length (magnitude) of `this` quaternion (alias for [length]).
     */
    val len: Float
        get() = length

    /**
     * Computes the square of the length of `this` quaternion.
     * Faster than [length] if only comparing magnitudes.
     */
    val lengthSq: Float
        get() = this.x * this.x + this.y * this.y + this.z * this.z + this.w * this.w

    /**
     * Computes the square of the length of `this` quaternion (alias for [lengthSq]).
     */
    val lenSq: Float
        get() = lengthSq


    /**
     * Sets this quaternion to the identity quaternion
     */
    fun setIdentity() {
        identity(this)
    }

    /**
     * Computes the inverse of `this` quaternion.
     * For unit quaternions, [conjugate] is equivalent and faster.
     */
    fun inverse(dst: Quatf = Quatf()): Quatf {
        val x = this.x
        val y = this.y
        val z = this.z
        val w = this.w
        val dot = x * x + y * y + z * z + w * w
        val invDot = if (dot != 0.0f) 1.0f / dot else 0.0f // Avoid division by zero

        dst.x = -x * invDot
        dst.y = -y * invDot
        dst.z = -z * invDot
        dst.w = w * invDot

        return dst
    }

    /**
     * Computes the conjugate of `this` quaternion.
     * If the quaternion is normalized, conjugate is the same as [inverse].
     */
    fun conjugate(dst: Quatf = Quatf()): Quatf {
        dst.x = -this.x
        dst.y = -this.y
        dst.z = -this.z
        dst.w = this.w
        return dst
    }

    /**
     * Copies the values from `this` quaternion.
     */
    fun copy(dst: Quatf = Quatf()): Quatf {
        dst.x = this.x
        dst.y = this.y
        dst.z = this.z
        dst.w = this.w
        return dst
    }

    /**
     * Copies `this` quaternion (alias for [copy]).
     */
    fun clone(dst: Quatf = Quatf()): Quatf = copy(dst)

    /**
     * Negates `this` quaternion (negates all components).
     */
    fun negate(dst: Quatf = Quatf()): Quatf {
        dst.x = -this.x
        dst.y = -this.y
        dst.z = -this.z
        dst.w = -this.w
        return dst
    }

    /**
     * Normalizes `this` quaternion (divides its components by its length).
     * Returns identity if length is near zero.
     */
    fun normalize(dst: Quatf = Quatf()): Quatf {
        val v0 = this.x
        val v1 = this.y
        val v2 = this.z
        val v3 = this.w

        // Calculate the magnitude (length) of the quaternion
        val len = sqrt(v0 * v0 + v1 * v1 + v2 * v2 + v3 * v3)

        // Define a small tolerance for the length check
        val epsilon = 0.00001f

        // Check if the length is large enough to avoid division by zero/near-zero
        if (len > epsilon) {
            // Normalize the quaternion components
            val invLen = 1.0f / len // Calculate inverse length once for efficiency
            dst.x = v0 * invLen
            dst.y = v1 * invLen
            dst.z = v2 * invLen
            dst.w = v3 * invLen
        } else {
            // If the length is too small, return the identity quaternion
            dst.x = 0.0f
            dst.y = 0.0f
            dst.z = 0.0f
            dst.w = 1.0f // Identity quaternion has w = 1
        }

        return dst

    }

    /**
     * Gets the rotation axis and angle for `this` quaternion.
     * @return A Pair containing the angle (in radians) and the axis (Vec3).
     */
    fun toAxisAngle(dstAxis: Vec3f? = null): Pair<Float, Vec3f> {
        val axis: Vec3f = dstAxis ?: Vec3f() // Explicitly type axis
        // Clamp w to avoid potential NaN from acos due to floating point errors
        val clampedW = max(-1.0f, min(1.0f, this.w))
        val angle = acos(clampedW) * 2.0f
        val s = sin(angle * 0.5f)
        if (abs(s) > EPSILON) { // Check absolute value of s
            val invS = 1.0f / s
            axis.x = this.x * invS
            axis.y = this.y * invS
            axis.z = this.z * invS
        } else {
            // If s is close to zero, angle is close to 0 or PI*2, axis is arbitrary but should be unit length
            axis.x = 1.0f
            axis.y = 0.0f
            axis.z = 0.0f
        }
        return Pair(angle, axis)
    }

    /**
     * Computes the angle in radians between `this` quaternion and [other].
     */
    fun angle(other: Quatf): Float {
        val d = this.dot(other)
        // Clamp dot product to avoid NaNs from acos due to floating point inaccuracies
        val clampedDot = max(-1.0f, min(1.0f, d))
        // Use the formula angle = acos(2 * dot^2 - 1) which is derived from |a · b| = cos(theta)
        // but handles the double cover (q and -q represent the same rotation)
        return acos(2.0f * clampedDot * clampedDot - 1.0f)
    }

    /**
     * Multiplies `this` quaternion by [other] (`this` * [other]).
     */
    fun multiply(other: Quatf, dst: Quatf = Quatf()): Quatf {
        val ax = this.x
        val ay = this.y
        val az = this.z
        val aw = this.w
        val bx = other.x
        val by = other.y
        val bz = other.z
        val bw = other.w

        dst.x = ax * bw + aw * bx + ay * bz - az * by
        dst.y = ay * bw + aw * by + az * bx - ax * bz
        dst.z = az * bw + aw * bz + ax * by - ay * bx
        dst.w = aw * bw - ax * bx - ay * by - az * bz

        return dst
    }

    /**
     * Multiplies `this` quaternion by [other] (`this` * [other]) (alias for [multiply]).
     */
    fun mul(other: Quatf, dst: Quatf = Quatf()): Quatf = multiply(other, dst)

    /**
     * Rotates `this` quaternion around the X axis by [angleInRadians].
     */
    fun rotateX(angleInRadians: Float, dst: Quatf = Quatf()): Quatf {
        val halfAngle = angleInRadians * 0.5f
        val qx = this.x
        val qy = this.y
        val qz = this.z
        val qw = this.w
        val bx = sin(halfAngle)
        val bw = cos(halfAngle)

        dst.x = qx * bw + qw * bx
        dst.y = qy * bw + qz * bx
        dst.z = qz * bw - qy * bx
        dst.w = qw * bw - qx * bx

        return dst
    }

    /**
     * Rotates `this` quaternion around the Y axis by [angleInRadians].
     */
    fun rotateY(angleInRadians: Float, dst: Quatf = Quatf()): Quatf {
        val halfAngle = angleInRadians * 0.5f
        val qx = this.x
        val qy = this.y
        val qz = this.z
        val qw = this.w
        val by = sin(halfAngle)
        val bw = cos(halfAngle)

        dst.x = qx * bw - qz * by
        dst.y = qy * bw + qw * by
        dst.z = qz * bw + qx * by
        dst.w = qw * bw - qy * by

        return dst
    }

    /**
     * Rotates `this` quaternion around the Z axis by [angleInRadians].
     */
    fun rotateZ(angleInRadians: Float, dst: Quatf = Quatf()): Quatf {
        val halfAngle = angleInRadians * 0.5f
        val qx = this.x
        val qy = this.y
        val qz = this.z
        val qw = this.w
        val bz = sin(halfAngle)
        val bw = cos(halfAngle)

        dst.x = qx * bw + qy * bz
        dst.y = qy * bw - qx * bz
        dst.z = qz * bw + qw * bz
        dst.w = qw * bw - qz * bz

        return dst
    }

    /**
     * Adds [other] to `this` quaternion.
     */
    fun add(other: Quatf, dst: Quatf = Quatf()): Quatf {
        dst.x = this.x + other.x
        dst.y = this.y + other.y
        dst.z = this.z + other.z
        dst.w = this.w + other.w
        return dst
    }

    /**
     * Subtracts [other] from `this` quaternion (`this` - [other]).
     */
    fun subtract(other: Quatf, dst: Quatf = Quatf()): Quatf {
        dst.x = this.x - other.x
        dst.y = this.y - other.y
        dst.z = this.z - other.z
        dst.w = this.w - other.w
        return dst
    }

    /**
     * Subtracts [other] from `this` quaternion (`this` - [other]) (alias for [subtract]).
     */
    fun sub(other: Quatf, dst: Quatf = Quatf()): Quatf = subtract(other, dst)

    /**
     * Multiplies `this` quaternion by the scalar [k].
     */
    fun mulScalar(k: Float, dst: Quatf = Quatf()): Quatf {
        dst.x = this.x * k
        dst.y = this.y * k
        dst.z = this.z * k
        dst.w = this.w * k
        return dst
    }

    /**
     * Multiplies `this` quaternion by the scalar [k] (alias for [mulScalar]).
     */
    fun scale(k: Float, dst: Quatf = Quatf()): Quatf = mulScalar(k, dst)

    /**
     * Divides `this` quaternion by the scalar [k].
     */
    fun divScalar(k: Float, dst: Quatf = Quatf()): Quatf {
        val invK = 1.0f / k // Calculate inverse once
        dst.x = this.x * invK
        dst.y = this.y * invK
        dst.z = this.z * invK
        dst.w = this.w * invK
        return dst
    }

    fun rotate(v: Vec3f, dst: Vec3f = Vec3f()): Vec3f = v.transformQuat(this, dst)

    /**
     * Computes the dot product of `this` quaternion and [other].
     */
    fun dot(other: Quatf): Float {
        return this.x * other.x + this.y * other.y + this.z * other.z + this.w * other.w
    }


    /**
     * Checks if `this` quaternion is exactly equal to [other].
     * Use with caution for floating-point numbers; prefer [equalsApproximately].
     */
    fun equals(other: Quatf): Boolean {
        return this.x == other.x && this.y == other.y && this.z == other.z && this.w == other.w
    }

    /**
     * Spherically linearly interpolates between `this` quaternion and [other] by [t].
     * Handles shortest path interpolation.
     */
    fun slerp(other: Quatf, t: Float, dst: Quatf = Quatf()): Quatf {
        val ax = this.x
        val ay = this.y
        val az = this.z
        val aw = this.w
        var bx = other.x
        var by = other.y
        var bz = other.z
        var bw = other.w

        var cosOmega = ax * bx + ay * by + az * bz + aw * bw

//        val doubleResult = ax.toDouble() * bx.toDouble()  + ay.toDouble()  * by.toDouble()  + az.toDouble()  * bz.toDouble()  + aw.toDouble()  * bw.toDouble()

        // Adjust signs if necessary to take the shortest path
        if (cosOmega < 0.0f) {
            cosOmega = -cosOmega
            bx = -bx
            by = -by
            bz = -bz
            bw = -bw
        }

        var scale0: Float
        var scale1: Float

        if (1.0f - cosOmega > EPSILON) {
            // Standard case (slerp)
            val omega = acos(cosOmega)
            val sinOmega = sin(omega)
            scale0 = sin((1.0f - t) * omega) / sinOmega
            scale1 = sin(t * omega) / sinOmega
        } else {
            // Quaternions are very close - use linear interpolation (lerp)
            scale0 = 1.0f - t
            scale1 = t
        }

        dst.x = scale0 * ax + scale1 * bx
        dst.y = scale0 * ay + scale1 * by
        dst.z = scale0 * az + scale1 * bz
        dst.w = scale0 * aw + scale1 * bw

        return dst
    }

    /**
     * Performs linear interpolation between `this` quaternion and [other] by [t].
     * Note: For rotations, [slerp] is usually preferred.
     */
    fun lerp(other: Quatf, t: Float, dst: Quatf = Quatf()): Quatf {
        dst.x = this.x + t * (other.x - this.x)
        dst.y = this.y + t * (other.y - this.y)
        dst.z = this.z + t * (other.z - this.z)
        dst.w = this.w + t * (other.w - this.w)
        return dst
    }

    /**
     * Checks if `this` quaternion is approximately equal to [other] within the given [epsilon].
     */
    fun equalsApproximately(other: Quatf, epsilon: Float = EPSILON): Boolean {
        return abs(this.x - other.x) < epsilon &&
                abs(this.y - other.y) < epsilon &&
                abs(this.z - other.z) < epsilon &&
                abs(this.w - other.w) < epsilon
    }

    /**
     * Sets the components of `this` to [x], [y], [z], [w].
     * @return `this`
     */
    fun set(x: Float, y: Float, z: Float, w: Float): Quatf {
        this.x = x
        this.y = y
        this.z = z
        this.w = w
        return this
    }

    override fun toString(): String = "(${x.ns},${y.ns},${z.ns},${w.ns})"

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Quatf) return false

        if (x != other.x) return false
        if (y != other.y) return false
        if (z != other.z) return false
        if (w != other.w) return false

        return true
    }

    override fun hashCode(): Int {
        var result = x.hashCode()
        result = 31 * result + y.hashCode()
        result = 31 * result + z.hashCode()
        result = 31 * result + w.hashCode()
        return result
    }
}