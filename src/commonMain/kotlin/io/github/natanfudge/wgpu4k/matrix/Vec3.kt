package io.github.natanfudge.wgpu4k.matrix

import kotlin.math.*
import kotlin.random.Random // Needed for Vec3.random






/**
 * Represents a 3-component vector using individual x, y, z fields.
 * 'this' corresponds to the first vector argument (v or a) in the original JavaScript functions.
 * Operations generally return a new Vec3 or modify the 'dst' Vec3 if provided,
 * keeping the API identical to the JavaScript version.
 *
 * @property x The x component of the vector.
 * @property y The y component of the vector.
 * @property z The z component of the vector.
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

    // --- Instance Methods (where 'this' is the first parameter 'v' or 'a') ---

    /**
     * Applies Math.ceil to each element of vector 'this'.
     * @param dst vector to hold result. If not passed in a new one is created.
     * @return A vector that is the ceil of each element of 'this'.
     */
    fun ceil(dst: Vec3 = Vec3()): Vec3 {
        dst.x = ceil(this.x)
        dst.y = ceil(this.y)
        dst.z = ceil(this.z)
        return dst
    }

    /**
     * Applies Math.floor to each element of vector 'this'.
     * @param dst vector to hold result. If not passed in a new one is created.
     * @return A vector that is the floor of each element of 'this'.
     */
    fun floor(dst: Vec3 = Vec3()): Vec3 {
        dst.x = floor(this.x)
        dst.y = floor(this.y)
        dst.z = floor(this.z)
        return dst
    }

    /**
     * Applies Math.round to each element of vector 'this'.
     * @param dst vector to hold result. If not passed in a new one is created.
     * @return A vector that is the round of each element of 'this'.
     */
    fun round(dst: Vec3 = Vec3()): Vec3 {
        dst.x = round(this.x)
        dst.y = round(this.y)
        dst.z = round(this.z)
        return dst
    }

    /**
     * Clamp each element of vector 'this' between min and max.
     * @param min Min value, default 0
     * @param max Max value, default 1
     * @param dst vector to hold result. If not passed in a new one is created.
     * @return A vector that the clamped value of each element of 'this'.
     */
    fun clamp(min: Float = 0f, max: Float = 1f, dst: Vec3 = Vec3()): Vec3 {
        dst.x = min(max, max(min, this.x))
        dst.y = min(max, max(min, this.y))
        dst.z = min(max, max(min, this.z))
        return dst
    }

    /**
     * Adds vector 'b' to vector 'this'.
     * @param b Operand vector.
     * @param dst vector to hold result. If not passed in a new one is created.
     * @return A vector that is the sum of 'this' and b.
     */
    fun add(b: Vec3, dst: Vec3 = Vec3()): Vec3 {
        dst.x = this.x + b.x
        dst.y = this.y + b.y
        dst.z = this.z + b.z
        return dst
    }

    /**
     * Adds vector 'b' scaled by 'scale' to vector 'this'.
     * @param b Operand vector.
     * @param scale Amount to scale b
     * @param dst vector to hold result. If not passed in a new one is created.
     * @return A vector that is the sum of 'this' + b * scale.
     */
    fun addScaled(b: Vec3, scale: Float, dst: Vec3 = Vec3()): Vec3 {
        dst.x = this.x + b.x * scale
        dst.y = this.y + b.y * scale
        dst.z = this.z + b.z * scale
        return dst
    }

    /**
     * Returns the angle in radians between vector 'this' and vector 'b'.
     * @param b Operand vector.
     * @return The angle in radians between the 2 vectors.
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
     * Subtracts vector 'b' from vector 'this'.
     * @param b Operand vector.
     * @param dst vector to hold result. If not passed in a new one is created.
     * @return A vector that is the difference of 'this' and b.
     */
    fun subtract(b: Vec3, dst: Vec3 = Vec3()): Vec3 {
        dst.x = this.x - b.x
        dst.y = this.y - b.y
        dst.z = this.z - b.z
        return dst
    }

    /**
     * Subtracts vector 'b' from vector 'this'. (Alias for subtract)
     * @param b Operand vector.
     * @param dst vector to hold result. If not passed in a new one is created.
     * @return A vector that is the difference of 'this' and b.
     */
    fun sub(b: Vec3, dst: Vec3 = Vec3()): Vec3 {
        return subtract(b, dst)
    }

    /**
     * Check if vector 'this' and vector 'b' are approximately equal.
     * @param b Operand vector.
     * @return true if vectors are approximately equal.
     */
    fun equalsApproximately(b: Vec3): Boolean {
        return abs(this.x - b.x) < EPSILON &&
                abs(this.y - b.y) < EPSILON &&
                abs(this.z - b.z) < EPSILON
    }

    /**
     * Check if vector 'this' and vector 'b' are exactly equal.
     * Note: In Kotlin, this shadows the default `equals` from `Any`.
     * Consider renaming to `exactEquals` if standard equality is needed elsewhere.
     * Keeping it as `equals` to match the JS API name precisely.
     * @param b Operand vector.
     * @return true if vectors are exactly equal.
     */
    fun equals(b: Vec3): Boolean {
        return this.x == b.x && this.y == b.y && this.z == b.z
    }

    /**
     * Performs linear interpolation between vector 'this' and vector 'b'.
     * Given vectors 'this' (a) and 'b' and interpolation coefficient t, returns
     * a + t * (b - a).
     * @param b Operand vector.
     * @param t Interpolation coefficient.
     * @param dst vector to hold result. If not passed in a new one is created.
     * @return The linear interpolated result.
     */
    fun lerp(b: Vec3, t: Float, dst: Vec3 = Vec3()): Vec3 {
        dst.x = this.x + t * (b.x - this.x)
        dst.y = this.y + t * (b.y - this.y)
        dst.z = this.z + t * (b.z - this.z)
        return dst
    }

    /**
     * Performs linear interpolation between vector 'this' and vector 'b' using coefficient vector 't'.
     * Given vectors 'this' (a) and 'b' and interpolation coefficient vector t, returns
     * a + t * (b - a).
     * @param b Operand vector.
     * @param t Interpolation coefficients vector.
     * @param dst vector to hold result. If not passed in a new one is created.
     * @return the linear interpolated result.
     */
    fun lerpV(b: Vec3, t: Vec3, dst: Vec3 = Vec3()): Vec3 {
        dst.x = this.x + t.x * (b.x - this.x)
        dst.y = this.y + t.y * (b.y - this.y)
        dst.z = this.z + t.z * (b.z - this.z)
        return dst
    }

    /**
     * Return max values of vector 'this' and vector 'b'.
     * Given vectors 'this' (a) and 'b' returns
     * [max(a[0], b[0]), max(a[1], b[1]), max(a[2], b[2])].
     * @param b Operand vector.
     * @param dst vector to hold result. If not passed in a new one is created.
     * @return The max components vector.
     */
    fun max(b: Vec3, dst: Vec3 = Vec3()): Vec3 {
        dst.x = max(this.x, b.x)
        dst.y = max(this.y, b.y)
        dst.z = max(this.z, b.z)
        return dst
    }

    /**
     * Return min values of vector 'this' and vector 'b'.
     * Given vectors 'this' (a) and 'b' returns
     * [min(a[0], b[0]), min(a[1], b[1]), min(a[2], b[2])].
     * @param b Operand vector.
     * @param dst vector to hold result. If not passed in a new one is created.
     * @return The min components vector.
     */
    fun min(b: Vec3, dst: Vec3 = Vec3()): Vec3 {
        dst.x = min(this.x, b.x)
        dst.y = min(this.y, b.y)
        dst.z = min(this.z, b.z)
        return dst
    }

    /**
     * Multiplies vector 'this' by a scalar.
     * @param k The scalar.
     * @param dst vector to hold result. If not passed in a new one is created.
     * @return The scaled vector.
     */
    fun mulScalar(k: Float, dst: Vec3 = Vec3()): Vec3 {
        dst.x = this.x * k
        dst.y = this.y * k
        dst.z = this.z * k
        return dst
    }

    /**
     * Multiplies vector 'this' by a scalar. (Alias for mulScalar)
     * @param k The scalar.
     * @param dst vector to hold result. If not passed in a new one is created.
     * @return The scaled vector.
     */
    fun scale(k: Float, dst: Vec3 = Vec3()): Vec3 {
        return mulScalar(k, dst)
    }

    /**
     * Divides vector 'this' by a scalar.
     * @param k The scalar.
     * @param dst vector to hold result. If not passed in a new one is created.
     * @return The scaled vector.
     */
    fun divScalar(k: Float, dst: Vec3 = Vec3()): Vec3 {
        dst.x = this.x / k
        dst.y = this.y / k
        dst.z = this.z / k
        return dst
    }

    /**
     * Inverse vector 'this' (1/component).
     * @param dst vector to hold result. If not passed in a new one is created.
     * @return The inverted vector.
     */
    fun inverse(dst: Vec3 = Vec3()): Vec3 {
        dst.x = 1f / this.x
        dst.y = 1f / this.y
        dst.z = 1f / this.z
        return dst
    }

    /**
     * Inverse vector 'this' (1/component). (Alias for inverse)
     * @param dst vector to hold result. If not passed in a new one is created.
     * @return The inverted vector.
     */
    fun invert(dst: Vec3 = Vec3()): Vec3 {
        return inverse(dst)
    }

    /**
     * Computes the cross product of vector 'this' and vector 'b'.
     * @param b Operand vector.
     * @param dst vector to hold result. If not passed in a new one is created.
     * @return The vector of 'this' cross b.
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
     * Computes the dot product of vector 'this' and vector 'b'.
     * @param b Operand vector.
     * @return dot product.
     */
    fun dot(b: Vec3): Float {
        return (this.x * b.x) + (this.y * b.y) + (this.z * b.z)
    }

    /**
     * Computes the length of vector 'this'.
     * @return length of vector.
     */
    fun length(): Float {
        return sqrt(this.x * this.x + this.y * this.y + this.z * this.z)
    }

    /**
     * Computes the length of vector 'this'. (Alias for length)
     * @return length of vector.
     */
    fun len(): Float {
        return length()
    }

    /**
     * Computes the square of the length of vector 'this'.
     * @return square of the length of vector.
     */
    fun lengthSq(): Float {
        return this.x * this.x + this.y * this.y + this.z * this.z
    }

    /**
     * Computes the square of the length of vector 'this'. (Alias for lengthSq)
     * @return square of the length of vector.
     */
    fun lenSq(): Float {
        return lengthSq()
    }

    /**
     * Computes the distance between 'this' point and point 'b'.
     * @param b vector.
     * @return distance between 'this' and b.
     */
    fun distance(b: Vec3): Float {
        val dx = this.x - b.x
        val dy = this.y - b.y
        val dz = this.z - b.z
        return sqrt(dx * dx + dy * dy + dz * dz)
    }

    /**
     * Computes the distance between 'this' point and point 'b'. (Alias for distance)
     * @param b vector.
     * @return distance between 'this' and b.
     */
    fun dist(b: Vec3): Float {
        return distance(b)
    }

    /**
     * Computes the square of the distance between 'this' point and point 'b'.
     * @param b vector.
     * @return square of the distance between 'this' and b.
     */
    fun distanceSq(b: Vec3): Float {
        val dx = this.x - b.x
        val dy = this.y - b.y
        val dz = this.z - b.z
        return dx * dx + dy * dy + dz * dz
    }

    /**
     * Computes the square of the distance between 'this' point and point 'b'. (Alias for distanceSq)
     * @param b vector.
     * @return square of the distance between 'this' and b.
     */
    fun distSq(b: Vec3): Float {
        return distanceSq(b)
    }

    /**
     * Divides vector 'this' by its Euclidean length and returns the quotient.
     * @param dst vector to hold result. If not passed in a new one is created.
     * @return The normalized vector.
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
     * Negates vector 'this'.
     * @param dst vector to hold result. If not passed in a new one is created.
     * @return -this.
     */
    fun negate(dst: Vec3 = Vec3()): Vec3 {
        dst.x = -this.x
        dst.y = -this.y
        dst.z = -this.z
        return dst
    }

    /**
     * Copies vector 'this'. (Alias for clone)
     * Also see [Vec3.Companion.create] and [Vec3.Companion.set]
     * @param dst vector to hold result. If not passed in a new one is created.
     * @return A copy of 'this'.
     */
    fun copy(dst: Vec3 = Vec3()): Vec3 {
        dst.x = this.x
        dst.y = this.y
        dst.z = this.z
        return dst
    }

    /**
     * Clones vector 'this'. (Alias for copy)
     * Also see [Vec3.Companion.create] and [Vec3.Companion.set]
     * @param dst vector to hold result. If not passed in a new one is created.
     * @return A copy of 'this'.
     */
    fun clone(dst: Vec3 = Vec3()): Vec3 {
        return copy(dst)
    }

    /**
     * Multiplies vector 'this' by vector 'b' (component-wise).
     * @param b Operand vector.
     * @param dst vector to hold result. If not passed in a new one is created.
     * @return The vector of products of entries of 'this' and b.
     */
    fun multiply(b: Vec3, dst: Vec3 = Vec3()): Vec3 {
        dst.x = this.x * b.x
        dst.y = this.y * b.y
        dst.z = this.z * b.z
        return dst
    }

    /**
     * Multiplies vector 'this' by vector 'b' (component-wise). (Alias for multiply)
     * @param b Operand vector.
     * @param dst vector to hold result. If not passed in a new one is created.
     * @return The vector of products of entries of 'this' and b.
     */
    fun mul(b: Vec3, dst: Vec3 = Vec3()): Vec3 {
        return multiply(b, dst)
    }

    /**
     * Divides vector 'this' by vector 'b' (component-wise).
     * @param b Operand vector.
     * @param dst vector to hold result. If not passed in a new one is created.
     * @return The vector of quotients of entries of 'this' and b.
     */
    fun divide(b: Vec3, dst: Vec3 = Vec3()): Vec3 {
        dst.x = this.x / b.x
        dst.y = this.y / b.y
        dst.z = this.z / b.z
        return dst
    }

    /**
     * Divides vector 'this' by vector 'b' (component-wise). (Alias for divide)
     * @param b Operand vector.
     * @param dst vector to hold result. If not passed in a new one is created.
     * @return The vector of quotients of entries of 'this' and b.
     */
    fun div(b: Vec3, dst: Vec3 = Vec3()): Vec3 {
        return divide(b, dst)
    }

    /**
     * transform vec3 'this' by 4x4 matrix 'm'.
     * @param m The matrix (Mat4Arg assumed).
     * @param dst optional vec3 to store result. If not passed a new one is created.
     * @return the transformed vector.
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
     * Transform vec3 'this' by upper 3x3 matrix inside 4x4 matrix 'm'.
     * Treats 'this' as a direction.
     * @param m The matrix (Mat4Arg assumed).
     * @param dst optional vec3 to store result. If not passed a new one is created.
     * @return The transformed vector.
     */
    fun transformMat4Upper3x3(m: Mat4, dst: Vec3 = Vec3()): Vec3 {
        val vx = this.x; val vy = this.y; val vz = this.z

        dst.x = vx * m[0 * 4 + 0] + vy * m[1 * 4 + 0] + vz * m[2 * 4 + 0]
        dst.y = vx * m[0 * 4 + 1] + vy * m[1 * 4 + 1] + vz * m[2 * 4 + 1]
        dst.z = vx * m[0 * 4 + 2] + vy * m[1 * 4 + 2] + vz * m[2 * 4 + 2]

        return dst
    }

    /**
     * Transforms vec3 'this' by 3x3 matrix 'm'.
     * Assumes standard column-major Mat3 * Vec3 multiplication based on likely intent.
     * @param m The matrix (Mat3Arg assumed, length 9, column-major).
     * @param dst optional vec3 to store result. If not passed a new one is created.
     * @return the transformed vector.
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
     * Transforms vec3 'this' by Quaternion 'q'.
     * @param q the quaternion (QuatArg assumed) to transform by.
     * @param dst optional vec3 to store result. If not passed a new one is created.
     * @return the transformed vector.
     */
    fun transformQuat(q: Quat, dst: Vec3 = Vec3()): Vec3 {
        // Access quaternion components using properties and ensure they are Float
        val qx = q.x.toFloat(); val qy = q.y.toFloat(); val qz = q.z.toFloat(); val qw = q.w.toFloat()
        // Calculation based on transforming a vector by a quaternion: v' = q * v * conjugate(q)
        // Simplified calculation:
        val x = this.x; val y = this.y; val z = this.z

        // uv = q.xyz (vector part of quaternion)
        // uuv = cross(uv, cross(uv, v)) + dot(uv, v) * uv
        // v' = v + 2 * cross(uv, w*v + cross(uv, v))
        // Implementation based on common optimized formula:
        // t = 2 * cross(q.xyz, v)
        // v' = v + q.w * t + cross(q.xyz, t)

        val uvX = qy * z - qz * y
        val uvY = qz * x - qx * z
        val uvZ = qx * y - qy * x

        val uuvX = qy * uvZ - qz * uvY
        val uuvY = qz * uvX - qx * uvZ
        val uuvZ = qx * uvY - qy * uvX

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
     * Rotate a 3D vector 'this' around the x-axis, relative to origin 'b'.
     *
     * @param b The origin of the rotation.
     * @param rad The angle of rotation in radians.
     * @param dst The vector to set. If not passed a new one is created.
     * @return the rotated vector.
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
     * Rotate a 3D vector 'this' around the y-axis, relative to origin 'b'.
     *
     * @param b The origin of the rotation.
     * @param rad The angle of rotation in radians.
     * @param dst The vector to set. If not passed a new one is created.
     * @return the rotated vector.
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
     * Rotate a 3D vector 'this' around the z-axis, relative to origin 'b'.
     *
     * @param b The origin of the rotation.
     * @param rad The angle of rotation in radians.
     * @param dst The vector to set. If not passed a new one is created.
     * @return the rotated vector.
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
     * Treat vector 'this' as a direction and set its length.
     *
     * @param len The length of the resulting vector.
     * @param dst The vector to set. If not passed a new one is created.
     * @return The lengthened vector.
     */
    fun setLength(len: Float, dst: Vec3 = Vec3()): Vec3 {
        this.normalize(dst) // Normalizes into dst
        return dst.mulScalar(len, dst) // Scales dst in place
    }

    /**
     * Ensure vector 'this' is not longer than a max length.
     *
     * @param maxLen The longest length of the resulting vector.
     * @param dst The vector to set. If not passed a new one is created.
     * @return The vector, shortened to maxLen if it's too long, otherwise a copy of 'this'.
     */
    fun truncate(maxLen: Float, dst: Vec3 = Vec3()): Vec3 {
        val currentLength = this.length()
        if (currentLength > maxLen) {
            return this.setLength(maxLen, dst)
        }
        return this.copy(dst)
    }

    /**
     * Return the vector exactly between 'this' endpoint vector and endpoint 'b'.
     *
     * @param b Endpoint 2.
     * @param dst The vector to set. If not passed a new one is created.
     * @return The vector exactly residing between 'this' and b.
     */
    fun midpoint(b: Vec3, dst: Vec3 = Vec3()): Vec3 {
        return this.lerp(b, 0.5f, dst)
    }

    /**
     * Sets the components of this Vec3.
     * Different from the static `set` function, this modifies the current instance.
     * @param x first value
     * @param y second value
     * @param z third value
     * @return This Vec3 with its elements set.
     */
    fun set(x: Float, y: Float, z: Float): Vec3 {
        this.x = x
        this.y = y
        this.z = z
        return this
    }

    /**
     * Zero's this vector (sets components to 0).
     * @return This Vec3 zeroed.
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
         * Creates a vec3; may be called with x, y, z to set initial values.
         * @param x Initial x value. Defaults to 0.
         * @param y Initial y value. Defaults to 0.
         * @param z Initial z value. Defaults to 0.
         * @return the created vector
         */
        fun create(x: Float = 0f, y: Float = 0f, z: Float = 0f): Vec3 {
            return Vec3(x, y, z)
        }

        /**
         * Creates a vec3; may be called with x, y, z to set initial values. (same as create)
         * @param x Initial x value. Defaults to 0.
         * @param y Initial y value. Defaults to 0.
         * @param z Initial z value. Defaults to 0.
         * @return the created vector
         */
        fun fromValues(x: Float = 0f, y: Float = 0f, z: Float = 0f): Vec3 {
            return create(x, y, z)
        }

        /**
         * Sets the values of a Vec3.
         * Also see [Vec3.Companion.create] and [Vec3.copy] (instance method)
         *
         * @param x first value
         * @param y second value
         * @param z third value
         * @param dst - vector to hold result. If not passed in a new one is created.
         * @return A vector with its elements set.
         */
        fun set(x: Float, y: Float, z: Float, dst: Vec3 = Vec3()): Vec3 {
            dst.x = x
            dst.y = y
            dst.z = z
            return dst
        }

        /**
         * Creates a random vector with components within a sphere of radius 'scale'.
         * @param scale - Radius of the sphere (default 1). The length of the vector will be <= scale.
         * @param dst - vector to hold result. If not passed in a new one is created.
         * @return The random vector.
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
         * Zero's a vector.
         * @param dst - vector to hold result. If not passed in a new one is created.
         * @return The zeroed vector.
         */
        fun zero(dst: Vec3 = Vec3()): Vec3 {
            dst.x = 0f
            dst.y = 0f
            dst.z = 0f
            return dst
        }

        /**
         * Returns the translation component of a 4-by-4 matrix as a vector with 3
         * entries.
         * @param m The matrix (Mat4Arg assumed).
         * @param dst vector to hold result. If not passed a new one is created.
         * @return The translation component of m.
         */
        fun getTranslation(m: Mat4, dst: Vec3 = Vec3()): Vec3 {
            dst.x = m[12]
            dst.y = m[13]
            dst.z = m[14]
            return dst
        }

        /**
         * Returns an axis of a 4x4 matrix as a vector with 3 entries.
         * Assumes matrix is column-major.
         * @param m The matrix (Mat4Arg assumed).
         * @param axis The axis index: 0 = x-axis (col 0), 1 = y-axis (col 1), 2 = z-axis (col 2).
         * @param dst vector to hold result. If not passed a new one is created.
         * @return The axis component of m.
         */
        fun getAxis(m: Mat4, axis: Int, dst: Vec3 = Vec3()): Vec3 {
            val off = axis * 4
            dst.x = m[off + 0]
            dst.y = m[off + 1]
            dst.z = m[off + 2]
            return dst
        }

        /**
         * Returns the scaling component of the matrix (scale factors along axes).
         * Assumes matrix is column-major and has no shear.
         * @param m The Matrix (Mat4Arg assumed).
         * @param dst The vector to set. If not passed a new one is created.
         * @return Vector containing the scaling factors.
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