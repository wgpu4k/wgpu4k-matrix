package io.github.natanfudge.wgpu4k.matrix

import kotlin.math.*

typealias RotationOrder = String // Use String for simplicity, matching TS values like "xyz"

/**
 * Represents a quaternion for 3D rotations.
 *
 * @property x The x component (imaginary part i).
 * @property y The y component (imaginary part j).
 * @property z The z component (imaginary part k).
 * @property w The w component (real part).
 */
data class Quat(
    var x: Double = 0.0,
    var y: Double = 0.0,
    var z: Double = 0.0,
    var w: Double = 1.0, // Default to identity quaternion
) {

    companion object {
        // Access to Vec3 companion object methods if needed, or instantiate Vec3 directly
        // private val vec3Companion = Vec3 // If Vec3 has companion object methods we need

        // Constants
        const val EPSILON = 1e-6 // Epsilon for quaternion comparisons, potentially different from Vec


        /**
         * Creates a Quat with initial values [x], [y], [z], [w].
         * Defaults to the identity quaternion (0, 0, 0, 1).
         */
        fun create(x: Double = 0.0, y: Double = 0.0, z: Double = 0.0, w: Double = 1.0): Quat {
            return Quat(x, y, z, w)
        }

        /**
         * Creates a Quat with initial values [x], [y], [z], [w] (alias for [create]).
         * Defaults to the identity quaternion (0, 0, 0, 1).
         */
        fun fromValues(x: Double = 0.0, y: Double = 0.0, z: Double = 0.0, w: Double = 1.0): Quat {
            return Quat(x, y, z, w)
        }

        /**
         * Creates an identity quaternion (0, 0, 0, 1).
         */
        fun identity(dst: Quat = Quat()): Quat {
            dst.x = 0.0
            dst.y = 0.0
            dst.z = 0.0
            dst.w = 1.0
            return dst
        }

        /**
         * Creates a quaternion representing a rotation of [angleInRadians] around the normalized [axis].
         **/
        fun fromAxisAngle(axis: Vec3, angleInRadians: Double, dst: Quat = Quat()): Quat {
            val halfAngle = angleInRadians * 0.5
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
        fun fromMat(m: Any, dst: Quat = Quat()): Quat {

            // Check if it's Mat3 or Mat4 based on expected property/method or size if it's an array-like structure
            // This requires knowing the Mat3/Mat4 implementation. Assuming indexer `[]` access for now.
            // Expect Float from matrix indexers, cast to Double for Quat calculations
            val getElement: (Int) -> Double = when (m) {
                is Mat3 -> { index -> m[index].toDouble() } // Cast Mat3 element to Double
                is Mat4 -> { index -> m[index].toDouble() } // Cast Mat4 element to Double
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

            if (trace > 0.0) {
                val root = sqrt(trace + 1.0) // 2w
                dst.w = 0.5 * root
                val invRoot = 0.5 / root // 1/(4w)
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


                val root = sqrt(getElement(ii) - getElement(jj) - getElement(kk) + 1.0)
                val quatComp = doubleArrayOf(0.0, 0.0, 0.0) // Temporary array for x, y, z
                quatComp[i] = 0.5 * root
                val invRoot = 0.5 / root
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
         * Creates a quaternion from the given Euler angles ([xAngleInRadians], [yAngleInRadians], [zAngleInRadians])
         * applied in the specified [order] (e.g., "xyz", "zyx").
         */
        fun fromEuler(
            xAngleInRadians: Double,
            yAngleInRadians: Double,
            zAngleInRadians: Double,
            order: RotationOrder,
            dst: Quat = Quat(),
        ): Quat {

            val xHalfAngle = xAngleInRadians * 0.5
            val yHalfAngle = yAngleInRadians * 0.5
            val zHalfAngle = zAngleInRadians * 0.5

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
                    dst.y = cx * sy * cz - sx * cy * sz // Error in TS? Should be cx * sy * cz + sx * cy * sz? Sticking to TS impl.
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

        // Static temporary variables to avoid allocation in methods like rotationTo
        // Note: Be cautious with static mutable state in concurrent environments if applicable.
        private val tempVec3 = Vec3()
        private val xUnitVec3 = Vec3(1f, 0f, 0f)
        private val yUnitVec3 = Vec3(0f, 1f, 0f)

        /**
         * Computes a quaternion representing the shortest rotation from unit vector [aUnit] to unit vector [bUnit].
         * This method is NOT thread safe as it uses static temporary variables.
         */
        fun rotationToUnsafe(aUnit: Vec3, bUnit: Vec3, dst: Quat = Quat()): Quat {
            val dot = aUnit.dot(bUnit).toDouble() // Use instance method, ensure Double

            if (dot < -0.999999) {
                // Vectors are opposite, need an arbitrary axis orthogonal to aUnit
                // Cross product order matters: axis = a x defaultAxis
                xUnitVec3.cross(aUnit, tempVec3) // tempVec3 = xUnitVec3 x aUnit
                if (tempVec3.lenSq() < 0.000001f) { // Use instance method lenSq()
                    yUnitVec3.cross(aUnit, tempVec3) // tempVec3 = yUnitVec3 x aUnit
                }
                tempVec3.normalize(tempVec3) // Use instance method
                fromAxisAngle(tempVec3, PI, dst) // PI is Double
                return dst
            } else if (dot > 0.999999) {
                // Vectors are same direction
                dst.x = 0.0
                dst.y = 0.0
                dst.z = 0.0
                dst.w = 1.0
                return dst
            } else {
                // General case
                aUnit.cross(bUnit, tempVec3) // Use instance method tempVec3 = aUnit x bUnit
                dst.x = tempVec3.x.toDouble() // Cast result to Double
                dst.y = tempVec3.y.toDouble() // Cast result to Double
                dst.z = tempVec3.z.toDouble() // Cast result to Double
                dst.w = 1.0 + dot
                return dst.normalize(dst) // Normalize the result
            }
        }

        private val tempQuat1 = Quat()
        private val tempQuat2 = Quat()

        /**
         * Performs a spherical linear interpolation with two control points (Squad) using keyframes [a], [b], [c], [d]
         * and interpolation coefficient [t].
         * q(t) = Slerp(Slerp(a, d, t), Slerp(b, c, t), 2t(1-t))
         * This method is NOT thread safe as it uses static temporary variables.
         */
        fun sqlerpUnsafe(
            a: Quat,
            b: Quat,
            c: Quat,
            d: Quat,
            t: Double,
            dst: Quat = Quat(),
        ): Quat {
            // Use instance slerp method
            a.slerp(d, t, tempQuat1)
            b.slerp(c, t, tempQuat2)
            tempQuat1.slerp(tempQuat2, 2.0 * t * (1.0 - t), dst)
            return dst
        }
    }

    /**
     * Sets the components of `this` to [x], [y], [z], [w].
     * @return `this`
     */
    fun set(x: Double, y: Double, z: Double, w: Double): Quat {
        this.x = x
        this.y = y
        this.z = z
        this.w = w
        return this
    }

    /**
     * Gets the rotation axis and angle for `this` quaternion.
     * @return A Pair containing the angle (in radians) and the axis (Vec3).
     */
    fun toAxisAngle(dstAxis: Vec3? = null): Pair<Double, Vec3> {
        val axis: Vec3 = dstAxis ?: Vec3() // Explicitly type axis
        // Clamp w to avoid potential NaN from acos due to floating point errors
        val clampedW = max(-1.0, min(1.0, this.w))
        val angle = acos(clampedW) * 2.0
        val s = sin(angle * 0.5)
        if (abs(s) > EPSILON) { // Check absolute value of s
            val invS = 1.0 / s // Calculate inverse once
            axis.x = (this.x * invS).toFloat() // Cast to Float for Vec3
            axis.y = (this.y * invS).toFloat() // Cast to Float for Vec3
            axis.z = (this.z * invS).toFloat() // Cast to Float for Vec3
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
    fun angle(other: Quat): Double {
        val d = this.dot(other)
        // Clamp dot product to avoid NaNs from acos due to floating point inaccuracies
        val clampedDot = max(-1.0, min(1.0, d))
        // Use the formula angle = acos(2 * dot^2 - 1) which is derived from |a · b| = cos(theta)
        // but handles the double cover (q and -q represent the same rotation)
        return acos(2.0 * clampedDot * clampedDot - 1.0)
    }

    /**
     * Multiplies `this` quaternion by [other] (`this` * [other]).
     */
    fun multiply(other: Quat, dst: Quat = Quat()): Quat {
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
    fun mul(other: Quat, dst: Quat = Quat()): Quat = multiply(other, dst)

    /**
     * Rotates `this` quaternion around the X axis by [angleInRadians].
     */
    fun rotateX(angleInRadians: Double, dst: Quat = Quat()): Quat {
        val halfAngle = angleInRadians * 0.5
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
    fun rotateY(angleInRadians: Double, dst: Quat = Quat()): Quat {
        val halfAngle = angleInRadians * 0.5
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
    fun rotateZ(angleInRadians: Double, dst: Quat = Quat()): Quat {
        val halfAngle = angleInRadians * 0.5
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
     * Spherically linearly interpolates between `this` quaternion and [other] by [t].
     * Handles shortest path interpolation.
     */
     fun slerp(other: Quat, t: Double, dst: Quat = Quat()): Quat {
         val ax = this.x
         val ay = this.y
         val az = this.z
         val aw = this.w
         var bx = other.x
         var by = other.y
         var bz = other.z
         var bw = other.w

         var cosOmega = ax * bx + ay * by + az * bz + aw * bw

         // Adjust signs if necessary to take the shortest path
         if (cosOmega < 0.0) {
             cosOmega = -cosOmega
             bx = -bx
             by = -by
             bz = -bz
             bw = -bw
         }

         var scale0: Double
         var scale1: Double

         if (1.0 - cosOmega > EPSILON) {
             // Standard case (slerp)
             val omega = acos(cosOmega)
             val sinOmega = sin(omega)
             scale0 = sin((1.0 - t) * omega) / sinOmega
             scale1 = sin(t * omega) / sinOmega
         } else {
             // Quaternions are very close - use linear interpolation (lerp)
             scale0 = 1.0 - t
             scale1 = t
         }

         dst.x = scale0 * ax + scale1 * bx
         dst.y = scale0 * ay + scale1 * by
         dst.z = scale0 * az + scale1 * bz
         dst.w = scale0 * aw + scale1 * bw

         return dst
    }

    /**
     * Computes the inverse of `this` quaternion.
     * For unit quaternions, [conjugate] is equivalent and faster.
     */
     fun inverse(dst: Quat = Quat()): Quat {
         val x = this.x
         val y = this.y
         val z = this.z
         val w = this.w
         val dot = x * x + y * y + z * z + w * w
         val invDot = if (dot != 0.0) 1.0 / dot else 0.0 // Avoid division by zero

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
     fun conjugate(dst: Quat = Quat()): Quat {
         dst.x = -this.x
         dst.y = -this.y
         dst.z = -this.z
         dst.w = this.w
         return dst
    }

    /**
     * Copies the values from `this` quaternion.
     */
    fun copy(dst: Quat = Quat()): Quat {
        dst.x = this.x
        dst.y = this.y
        dst.z = this.z
        dst.w = this.w
        return dst
    }

    /**
     * Copies `this` quaternion (alias for [copy]).
     */
    fun clone(dst: Quat = Quat()): Quat = copy(dst)

    /**
     * Adds [other] to `this` quaternion.
     */
    fun add(other: Quat, dst: Quat = Quat()): Quat {
        dst.x = this.x + other.x
        dst.y = this.y + other.y
        dst.z = this.z + other.z
        dst.w = this.w + other.w
        return dst
    }

    /**
     * Subtracts [other] from `this` quaternion (`this` - [other]).
     */
    fun subtract(other: Quat, dst: Quat = Quat()): Quat {
        dst.x = this.x - other.x
        dst.y = this.y - other.y
        dst.z = this.z - other.z
        dst.w = this.w - other.w
        return dst
    }

    /**
     * Subtracts [other] from `this` quaternion (`this` - [other]) (alias for [subtract]).
     */
    fun sub(other: Quat, dst: Quat = Quat()): Quat = subtract(other, dst)

    /**
     * Multiplies `this` quaternion by the scalar [k].
     */
    fun mulScalar(k: Double, dst: Quat = Quat()): Quat {
        dst.x = this.x * k
        dst.y = this.y * k
        dst.z = this.z * k
        dst.w = this.w * k
        return dst
    }

    /**
     * Multiplies `this` quaternion by the scalar [k] (alias for [mulScalar]).
     */
    fun scale(k: Double, dst: Quat = Quat()): Quat = mulScalar(k, dst)

    /**
     * Divides `this` quaternion by the scalar [k].
     */
    fun divScalar(k: Double, dst: Quat = Quat()): Quat {
        val invK = 1.0 / k // Calculate inverse once
        dst.x = this.x * invK
        dst.y = this.y * invK
        dst.z = this.z * invK
        dst.w = this.w * invK
        return dst
    }

    /**
     * Computes the dot product of `this` quaternion and [other].
     */
    fun dot(other: Quat): Double {
        return this.x * other.x + this.y * other.y + this.z * other.z + this.w * other.w
    }

    /**
     * Performs linear interpolation between `this` quaternion and [other] by [t].
     * Note: For rotations, [slerp] is usually preferred.
     */
    fun lerp(other: Quat, t: Double, dst: Quat = Quat()): Quat {
        dst.x = this.x + t * (other.x - this.x)
        dst.y = this.y + t * (other.y - this.y)
        dst.z = this.z + t * (other.z - this.z)
        dst.w = this.w + t * (other.w - this.w)
        return dst
    }

    /**
     * Computes the length (magnitude) of `this` quaternion.
     */
    val length: Double
        get() = sqrt(this.x * this.x + this.y * this.y + this.z * this.z + this.w * this.w)

    /**
     * Computes the length (magnitude) of `this` quaternion (alias for [length]).
     */
    val len: Double
        get() = length

    /**
     * Computes the square of the length of `this` quaternion.
     * Faster than [length] if only comparing magnitudes.
     */
    val lengthSq: Double
        get() = this.x * this.x + this.y * this.y + this.z * this.z + this.w * this.w

    /**
     * Computes the square of the length of `this` quaternion (alias for [lengthSq]).
     */
    val lenSq: Double
        get() = lengthSq

    /**
     * Normalizes `this` quaternion (divides its components by its length).
     * Returns identity if length is near zero.
     */
    fun normalize(dst: Quat = Quat()): Quat {

// Extract components from the input array 'v'
        val v0 = this.x
        val v1 = this.y
        val v2 = this.z
        val v3 = this.w

// Calculate the magnitude (length) of the quaternion
// Ensure components are treated as floating-point numbers (e.g., Double) for sqrt
        val len = sqrt(v0 * v0 + v1 * v1 + v2 * v2 + v3 * v3)

// Define a small tolerance for the length check
        val epsilon = 0.00001

// Check if the length is large enough to avoid division by zero/near-zero
        if (len > epsilon) {
            // Normalize the quaternion components
            val invLen = 1.0 / len // Calculate inverse length once for efficiency
            dst.x = v0 * invLen
            dst.y = v1 * invLen
            dst.z = v2 * invLen
            dst.w = v3 * invLen
        } else {
            // If the length is too small, return the identity quaternion
            dst.x = 0.0
            dst.y = 0.0
            dst.z = 0.0
            dst.w = 1.0 // Identity quaternion has w = 1
        }

// Return the resulting normalized or identity quaternion
        return dst

    }

    /**
     * Checks if `this` quaternion is approximately equal to [other] within the given [epsilon].
     */
    fun equalsApproximately(other: Quat, epsilon: Double = EPSILON): Boolean {
        return abs(this.x - other.x) < epsilon &&
                abs(this.y - other.y) < epsilon &&
                abs(this.z - other.z) < epsilon &&
                abs(this.w - other.w) < epsilon
    }

    /**
     * Checks if `this` quaternion is exactly equal to [other].
     * Use with caution for floating-point numbers; prefer [equalsApproximately].
     */
    fun equals(other: Quat): Boolean {
        return this.x == other.x && this.y == other.y && this.z == other.z && this.w == other.w
    }
    // Note: Data class provides an equals method. This explicit one matches the TS API name.
}