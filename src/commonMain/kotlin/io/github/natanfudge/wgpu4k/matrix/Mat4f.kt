@file:Suppress("NOTHING_TO_INLINE")

package io.github.natanfudge.wgpu4k.matrix

import kotlin.math.*

/**
 * Represents a 4x4 matrix stored in a 16-element FloatArray.
 * The elements are stored in column-major order.
 *
 * The layout is:
 * 0  4  8   12
 * 1  5  9   13
 * 2  6  10  14
 * 3  7  11  15
 */
class Mat4f private constructor(val array: FloatArray) {

    /**
     * Creates a new Mat4 with the given values ([v0] to [v15]) in column-major order.
     */
    constructor(
        v0: Float, v1: Float, v2: Float, v3: Float,
        v4: Float, v5: Float, v6: Float, v7: Float,
        v8: Float, v9: Float, v10: Float, v11: Float,
        v12: Float, v13: Float, v14: Float, v15: Float,
    ) : this(FloatArray(16).apply {
        this[0] = v0; this[1] = v1; this[2] = v2; this[3] = v3
        this[4] = v4; this[5] = v5; this[6] = v6; this[7] = v7
        this[8] = v8; this[9] = v9; this[10] = v10; this[11] = v11
        this[12] = v12; this[13] = v13; this[14] = v14; this[15] = v15
    })

    constructor() : this(FloatArray(16))

    companion object {
        // 16 * 4 bytes
        const val SIZE_BYTES = 64u

        fun rowMajor(
            a00: Float, a01: Float, a02: Float, a03: Float,
            a10: Float, a11: Float, a12: Float, a13: Float,
            a20: Float, a21: Float, a22: Float, a23: Float,
            a30: Float, a31: Float, a32: Float, a33: Float,
        ) = Mat4f(
            a00, a10, a20, a30,
            a01, a11, a21, a31,
            a02, a12, a22, a32,
            a03, a13, a23, a33
        )

        /**
         * Creates a Mat4 from a 16-element FloatArray [values].
         * Assumes the array is already in the correct internal format.
         */
        fun fromFloatArray(values: FloatArray): Mat4f {
            if (values.size != 16) {
                throw IllegalArgumentException("Mat4.fromFloatArray requires a 16-element FloatArray.")
            }
            return Mat4f(values.copyOf()) // Create a copy to ensure internal state is not modified externally
        }

        /**
         * Creates a new identity Mat4.
         */
        fun identity(dst: Mat4f = Mat4f()): Mat4f {
            return dst.apply {
                array[0] = 1f; array[1] = 0f; array[2] = 0f; array[3] = 0f
                array[4] = 0f; array[5] = 1f; array[6] = 0f; array[7] = 0f
                array[8] = 0f; array[9] = 0f; array[10] = 1f; array[11] = 0f
                array[12] = 0f; array[13] = 0f; array[14] = 0f; array[15] = 1f
            }
        }

        /**
         * Creates a Mat4 from [m3].
         */
        fun fromMat3(m3: Mat3f, dst: Mat4f = Mat4f()): Mat4f {
            return dst.apply {
                array[0] = m3[0]; array[1] = m3[1]; array[2] = m3[2]; array[3] = 0f
                array[4] = m3[4]; array[5] = m3[5]; array[6] = m3[6]; array[7] = 0f
                array[8] = m3[8]; array[9] = m3[9]; array[10] = m3[10]; array[11] = 0f
                array[12] = 0f; array[13] = 0f; array[14] = 0f; array[15] = 1f
            }
        }

        /**
         * Creates a Mat4 rotation matrix from [q].
         */
        fun fromQuat(q: Quatf, dst: Mat4f = Mat4f()): Mat4f {
            val x = q.x;
            val y = q.y;
            val z = q.z;
            val w = q.w
            val x2 = x + x;
            val y2 = y + y;
            val z2 = z + z

            val xx = x * x2
            val yx = y * x2
            val yy = y * y2
            val zx = z * x2
            val zy = z * y2
            val zz = z * z2
            val wx = w * x2
            val wy = w * y2
            val wz = w * z2

            return dst.apply {
                array[0] = 1f - yy - zz; array[1] = yx + wz; array[2] = zx - wy; array[3] = 0f
                array[4] = yx - wz; array[5] = 1f - xx - zz; array[6] = zy + wx; array[7] = 0f
                array[8] = zx + wy; array[9] = zy - wx; array[10] = 1f - xx - yy; array[11] = 0f
                array[12] = 0f; array[13] = 0f; array[14] = 0f; array[15] = 1f
            }
        }

        /**
         * Creates a 4-by-4 matrix which translates by the given vector [v].
         */
        fun translation(v: Vec3f, dst: Mat4f = Mat4f()): Mat4f {
            return translation(v.x, v.y, v.z, dst)
        }

        /**
         * Creates a 4-by-4 matrix which translates by [x], [y], [z]
         */
        fun translation(x: Float, y: Float, z: Float, dst: Mat4f = Mat4f()): Mat4f {
            return dst.apply {
                array[0] = 1f; array[1] = 0f; array[2] = 0f; array[3] = 0f
                array[4] = 0f; array[5] = 1f; array[6] = 0f; array[7] = 0f
                array[8] = 0f; array[9] = 0f; array[10] = 1f; array[11] = 0f
                array[12] = x; array[13] = y; array[14] = z; array[15] = 1f
            }
        }

        /**
         * Creates a 4-by-4 matrix which rotates around the x-axis by the given [angleInRadians].
         */
        fun rotationX(angleInRadians: Float, dst: Mat4f = Mat4f()): Mat4f {
            val c = cos(angleInRadians)
            val s = sin(angleInRadians)

            return dst.apply {
                array[0] = 1f; array[1] = 0f; array[2] = 0f; array[3] = 0f
                array[4] = 0f; array[5] = c; array[6] = s; array[7] = 0f
                array[8] = 0f; array[9] = -s; array[10] = c; array[11] = 0f
                array[12] = 0f; array[13] = 0f; array[14] = 0f; array[15] = 1f
            }
        }

        /**
         * Creates a 4-by-4 matrix which rotates around the y-axis by the given [angleInRadians].
         */
        fun rotationY(angleInRadians: Float, dst: Mat4f = Mat4f()): Mat4f {
            val c = cos(angleInRadians)
            val s = sin(angleInRadians)

            return dst.apply {
                array[0] = c; array[1] = 0f; array[2] = -s; array[3] = 0f
                array[4] = 0f; array[5] = 1f; array[6] = 0f; array[7] = 0f
                array[8] = s; array[9] = 0f; array[10] = c; array[11] = 0f
                array[12] = 0f; array[13] = 0f; array[14] = 0f; array[15] = 1f
            }
        }

        /**
         * Creates a 4-by-4 matrix which rotates around the z-axis by the given [angleInRadians].
         */
        fun rotationZ(angleInRadians: Float, dst: Mat4f = Mat4f()): Mat4f {
            val c = cos(angleInRadians)
            val s = sin(angleInRadians)

            return dst.apply {
                array[0] = c; array[1] = s; array[2] = 0f; array[3] = 0f
                array[4] = -s; array[5] = c; array[6] = 0f; array[7] = 0f
                array[8] = 0f; array[9] = 0f; array[10] = 1f; array[11] = 0f
                array[12] = 0f; array[13] = 0f; array[14] = 0f; array[15] = 1f
            }
        }

        /**
         * Creates a 4-by-4 matrix which rotates around the given [axis] by the given [angleInRadians].
         */
        fun axisRotation(axis: Vec3f, angleInRadians: Float, dst: Mat4f = Mat4f()): Mat4f = rotation(axis.x, axis.y, axis.z, angleInRadians, dst)

        /**
         * Creates a 4-by-4 matrix which rotates around the given [axis] by the given [angleInRadians].
         */
        inline fun rotation(axis: Vec3f, angleInRadians: Float, dst: Mat4f = Mat4f()): Mat4f = axisRotation(axis, angleInRadians, dst)


        /**
         * Creates a 4-by-4 matrix which rotates around the given [x], [y], [z] axis by the given [angleInRadians].
         */
        fun rotation(x: Float, y: Float, z: Float, angleInRadians: Float, dst: Mat4f = Mat4f()): Mat4f {
            var x = x
            var y = y
            var z = z

            val n = sqrt(x * x + y * y + z * z)
            if (n < EPSILON) {
                return identity(dst)
            }

            x /= n
            y /= n
            z /= n

            val xx = x * x
            val yy = y * y
            val zz = z * z
            val c = cos(angleInRadians)
            val s = sin(angleInRadians)
            val oneMinusCosine = 1f - c

            dst.array[0] = xx + (1f - xx) * c
            dst.array[1] = x * y * oneMinusCosine + z * s
            dst.array[2] = x * z * oneMinusCosine - y * s
            dst.array[3] = 0f

            dst.array[4] = x * y * oneMinusCosine - z * s
            dst.array[5] = yy + (1f - yy) * c
            dst.array[6] = y * z * oneMinusCosine + x * s
            dst.array[7] = 0f

            dst.array[8] = x * z * oneMinusCosine + y * s
            dst.array[9] = y * z * oneMinusCosine - x * s
            dst.array[10] = zz + (1f - zz) * c
            dst.array[11] = 0f

            dst.array[12] = 0f
            dst.array[13] = 0f
            dst.array[14] = 0f
            dst.array[15] = 1f

            return dst

        }

        /**
         * Creates a 4-by-4 matrix which scales in each dimension by the components of [v].
         */
        fun scaling(v: Vec3f, dst: Mat4f = Mat4f()): Mat4f {
            return scaling(v.x, v.y, v.z, dst)
        }

        /**
         * Creates a 4-by-4 matrix which scales in each dimension by [x] ,[y], [z]
         */
        fun scaling(x: Float, y: Float, z: Float, dst: Mat4f = Mat4f()): Mat4f {
            return dst.apply {
                array[0] = x; array[1] = 0f; array[2] = 0f; array[3] = 0f
                array[4] = 0f; array[5] = y; array[6] = 0f; array[7] = 0f
                array[8] = 0f; array[9] = 0f; array[10] = z; array[11] = 0f
                array[12] = 0f; array[13] = 0f; array[14] = 0f; array[15] = 1f
            }
        }

        /**
         * Creates a 4-by-4 matrix which scales uniformly in each dimension by [s].
         */
        fun uniformScaling(s: Float, dst: Mat4f = Mat4f()): Mat4f {
            return dst.apply {
                array[0] = s; array[1] = 0f; array[2] = 0f; array[3] = 0f
                array[4] = 0f; array[5] = s; array[6] = 0f; array[7] = 0f
                array[8] = 0f; array[9] = 0f; array[10] = s; array[11] = 0f
                array[12] = 0f; array[13] = 0f; array[14] = 0f; array[15] = 1f
            }
        }

        /**
         * Computes a 4-by-4 perspective transformation matrix given the angular height
         * of the frustum, the aspect ratio, and the near and far clipping planes.  The
         * arguments define a frustum extending in the negative z direction.  The given
         * angle is the vertical angle of the frustum, and the horizontal angle is
         * determined to produce the given aspect ratio.  The arguments near and far are
         * the distances to the near and far clipping planes.  Note that near and far
         * are not z coordinates, but rather they are distances along the negative
         * z-axis.  The matrix generated sends the viewing frustum to the unit box.
         * We assume a unit box extending from -1 to 1 in the x and y dimensions and
         * from 0 to 1 in the z dimension.
         *
         * Note: If you pass `Float.POSITIVE_INFINITY` for zFar then it will produce a projection matrix
         * returns -Infinity for Z when transforming coordinates with Z <= 0 and +Infinity for Z
         * otherwise.
         *
         * @param fieldOfViewYInRadians The camera angle from top to bottom (in radians).
         * @param aspect The aspect ratio width / height.
         * @param zNear The depth (negative z coordinate) of the near clipping plane.
         * @param zFar The depth (negative z coordinate) of the far clipping plane.
         * @param dst Matrix to hold result. If not passed a new one is created.
         * @return The perspective matrix.
         */
        fun perspective(fieldOfViewYInRadians: Float, aspect: Float, zNear: Float, zFar: Float, dst: Mat4f = Mat4f()): Mat4f {
            val f = tan(PI.toFloat() * 0.5f - 0.5f * fieldOfViewYInRadians)

            dst.array[0] = f / aspect
            dst.array[1] = 0f
            dst.array[2] = 0f
            dst.array[3] = 0f

            dst.array[4] = 0f
            dst.array[5] = f
            dst.array[6] = 0f
            dst.array[7] = 0f

            dst.array[8] = 0f
            dst.array[9] = 0f
            dst.array[11] = -1f

            dst.array[12] = 0f
            dst.array[13] = 0f
            dst.array[15] = 0f

            if (zFar.isFinite()) {
                val rangeInv = 1f / (zNear - zFar)
                dst.array[10] = zFar * rangeInv
                dst.array[14] = zFar * zNear * rangeInv
            } else {
                dst.array[10] = -1f
                dst.array[14] = -zNear
            }

            return dst
        }

        /**
         * Computes a 4-by-4 reverse-z perspective transformation matrix given the angular height
         * of the frustum, the aspect ratio, and the near and far clipping planes.  The
         * arguments define a frustum extending in the negative z direction.  The given
         * angle is the vertical angle of the frustum, and the horizontal angle is
         * determined to produce the given aspect ratio.  The arguments near and far are
         * the distances to the near and far clipping planes.  Note that near and far
         * are not z coordinates, but rather they are distances along the negative
         * z-axis.  The matrix generated sends the viewing frustum to the unit box.
         * We assume a unit box extending from -1 to 1 in the x and y dimensions and
         * from 1 (at -zNear) to 0 (at -zFar) in the z dimension.
         *
         * @param fieldOfViewYInRadians The camera angle from top to bottom (in radians).
         * @param aspect The aspect ratio width / height.
         * @param zNear The depth (negative z coordinate) of the near clipping plane.
         * @param zFar The depth (negative z coordinate) of the far clipping plane. (default = Infinity)
         * @param dst Matrix to hold result. If not passed a new one is created.
         * @return The perspective matrix.
         */
        fun perspectiveReverseZ(fieldOfViewYInRadians: Float, aspect: Float, zNear: Float, zFar: Float = Float.POSITIVE_INFINITY, dst: Mat4f = Mat4f()): Mat4f {
            val f = 1f / tan(fieldOfViewYInRadians * 0.5f)

            dst.array[0] = f / aspect
            dst.array[1] = 0f
            dst.array[2] = 0f
            dst.array[3] = 0f

            dst.array[4] = 0f
            dst.array[5] = f
            dst.array[6] = 0f
            dst.array[7] = 0f

            dst.array[8] = 0f
            dst.array[9] = 0f
            dst.array[11] = -1f

            dst.array[12] = 0f
            dst.array[13] = 0f
            dst.array[15] = 0f

            if (zFar == Float.POSITIVE_INFINITY) {
                dst.array[10] = 0f
                dst.array[14] = zNear
            } else {
                val rangeInv = 1f / (zFar - zNear)
                dst.array[10] = zNear * rangeInv
                dst.array[14] = zFar * zNear * rangeInv
            }

            return dst
        }

        /**
         * Creates a 4-by-4 orthographic projection matrix defined by [left], [right], [bottom], [top], [near], and [far] clipping planes.
         * If [left] = [right], the result is undefined.
         */
        fun orthographic(left: Float, right: Float, bottom: Float, top: Float, near: Float, far: Float, dst: Mat4f = Mat4f()) =
            ortho(left, right, bottom, top, near, far, dst)

        /**
         * Computes a 4-by-4 orthogonal transformation matrix that transforms from
         * the given the left, right, bottom, and top dimensions to -1 +1 in x, and y
         * and 0 to +1 in z.
         * @param left Left side of the near clipping plane viewport.
         * @param right Right side of the near clipping plane viewport.
         * @param bottom Bottom of the near clipping plane viewport.
         * @param top Top of the near clipping plane viewport.
         * @param near The depth (negative z coordinate) of the near clipping plane.
         * @param far The depth (negative z coordinate) of the far clipping plane.
         * @param dst Output matrix. If not passed a new one is created.
         * @return The orthographic projection matrix.
         */
        fun ortho(left: Float, right: Float, bottom: Float, top: Float, near: Float, far: Float, dst: Mat4f = Mat4f()): Mat4f {
            return dst.apply {
                array[0] = 2f / (right - left)
                array[1] = 0f
                array[2] = 0f
                array[3] = 0f

                array[4] = 0f
                array[5] = 2f / (top - bottom)
                array[6] = 0f
                array[7] = 0f

                array[8] = 0f
                array[9] = 0f
                array[10] = 1f / (near - far)
                array[11] = 0f

                array[12] = (right + left) / (left - right)
                array[13] = (top + bottom) / (bottom - top)
                array[14] = near / (near - far)
                array[15] = 1f
            }
        }

        /**
         * Computes a 4-by-4 perspective transformation matrix given the left, right,
         * top, bottom, near and far clipping planes. The arguments define a frustum
         * extending in the negative z direction. The arguments near and far are the
         * distances to the near and far clipping planes. Note that near and far are not
         * z coordinates, but rather they are distances along the negative z-axis. The
         * matrix generated sends the viewing frustum to the unit box. We assume a unit
         * box extending from -1 to 1 in the x and y dimensions and from 0 to 1 in the z
         * dimension.
         * @param left The x coordinate of the left plane of the box.
         * @param right The x coordinate of the right plane of the box.
         * @param bottom The y coordinate of the bottom plane of the box.
         * @param top The y coordinate of the right plane of the box.
         * @param near The negative z coordinate of the near plane of the box.
         * @param far The negative z coordinate of the far plane of the box.
         * @param dst Output matrix. If not passed a new one is created.
         * @return The perspective projection matrix.
         */
        fun frustum(left: Float, right: Float, bottom: Float, top: Float, near: Float, far: Float, dst: Mat4f = Mat4f()): Mat4f {
            val dx = right - left
            val dy = top - bottom
            val dz = near - far

            return dst.apply {
                array[0] = 2 * near / dx
                array[1] = 0f
                array[2] = 0f
                array[3] = 0f

                array[4] = 0f
                array[5] = 2 * near / dy
                array[6] = 0f
                array[7] = 0f

                array[8] = (left + right) / dx
                array[9] = (top + bottom) / dy
                array[10] = far / dz
                array[11] = -1f

                array[12] = 0f
                array[13] = 0f
                array[14] = near * far / dz
                array[15] = 0f
            }
        }

        /**
         * Computes a 4-by-4 reverse-z perspective transformation matrix given the left, right,
         * top, bottom, near and far clipping planes. The arguments define a frustum
         * extending in the negative z direction. The arguments near and far are the
         * distances to the near and far clipping planes. Note that near and far are not
         * z coordinates, but rather they are distances along the negative z-axis. The
         * matrix generated sends the viewing frustum to the unit box. We assume a unit
         * box extending from -1 to 1 in the x and y dimensions and from 1 (-near) to 0 (-far) in the z
         * dimension.
         * @param left The x coordinate of the left plane of the box.
         * @param right The x coordinate of the right plane of the box.
         * @param bottom The y coordinate of the bottom plane of the box.
         * @param top The y coordinate of the right plane of the box.
         * @param near The negative z coordinate of the near plane of the box.
         * @param far The negative z coordinate of the far plane of the box.
         * @param dst Output matrix. If not passed a new one is created.
         * @return The perspective projection matrix.
         */
        fun frustumReverseZ(
            left: Float,
            right: Float,
            bottom: Float,
            top: Float,
            near: Float,
            far: Float = Float.POSITIVE_INFINITY,
            dst: Mat4f = Mat4f(),
        ): Mat4f {
            val dx = right - left
            val dy = top - bottom

            return dst.apply {
                array[0] = 2 * near / dx
                array[1] = 0f
                array[2] = 0f
                array[3] = 0f

                array[4] = 0f
                array[5] = 2 * near / dy
                array[6] = 0f
                array[7] = 0f

                array[8] = (left + right) / dx
                array[9] = (top + bottom) / dy
                array[11] = -1f

                array[12] = 0f
                array[13] = 0f
                array[15] = 0f

                if (far == Float.POSITIVE_INFINITY) {
                    array[10] = 0f
                    array[14] = near
                } else {
                    val rangeInv = 1f / (far - near)
                    array[10] = near * rangeInv
                    array[14] = far * near * rangeInv
                }
            }
        }

        /**
         * Computes a 4-by-4 view transformation.
         *
         * This is a view matrix which transforms all other objects
         * to be in the space of the view defined by the parameters.
         *
         * @param eye The position of the object.
         * @param target The position meant to be aimed at.
         * @param up A vector pointing up.
         * @param dst Matrix to hold result. If not passed a new one is created.
         * @return The look-at matrix.
         */
        fun lookAt(eye: Vec3f, target: Vec3f, up: Vec3f, dst: Mat4f = Mat4f()): Mat4f {
            val eyex = eye.x
            val eyey = eye.y
            val eyez = eye.z
            val upx = up.x
            val upy = up.y
            val upz = up.z
            val targetx = target.x
            val targety = target.y
            val targetz = target.z

            val z0 = eyex - targetx
            val z1 = eyey - targety
            val z2 = eyez - targetz

            // Normalize z
            var len = z0 * z0 + z1 * z1 + z2 * z2
            var nz0 = z0
            var nz1 = z1
            var nz2 = z2
            if (len > 0) {
                len = 1 / sqrt(len)
                nz0 *= len
                nz1 *= len
                nz2 *= len
            }

            // Cross product of up and z to get x
            var x0 = upy * nz2 - upz * nz1
            var x1 = upz * nz0 - upx * nz2
            var x2 = upx * nz1 - upy * nz0

            // Normalize x
            len = x0 * x0 + x1 * x1 + x2 * x2
            if (len > 0) {
                len = 1 / sqrt(len)
                x0 *= len
                x1 *= len
                x2 *= len
            }

            // Cross product of z and x to get y
            val y0 = nz1 * x2 - nz2 * x1
            val y1 = nz2 * x0 - nz0 * x2
            val y2 = nz0 * x1 - nz1 * x0

            return dst.apply {
                array[0] = x0; array[1] = y0; array[2] = nz0; array[3] = 0f
                array[4] = x1; array[5] = y1; array[6] = nz1; array[7] = 0f
                array[8] = x2; array[9] = y2; array[10] = nz2; array[11] = 0f
                array[12] = -(x0 * eyex + x1 * eyey + x2 * eyez)
                array[13] = -(y0 * eyex + y1 * eyey + y2 * eyez)
                array[14] = -(nz0 * eyex + nz1 * eyey + nz2 * eyez)
                array[15] = 1f
            }
        }

        // Temporary vectors for aim and cameraAim functions
        private val xAxis = Vec3f()
        private val yAxis = Vec3f()
        private val zAxis = Vec3f()

        /**
         * Computes a 4-by-4 aim transformation.
         *
         * This is a matrix which positions an object aiming down positive Z.
         * toward the target.
         *
         * Note: this is **NOT** the inverse of lookAt as lookAt looks at negative Z.
         *
         * @param position The position of the object.
         * @param target The position meant to be aimed at.
         * @param up A vector pointing up.
         * @param dst Matrix to hold result. If not passed a new one is created.
         * @return The aim matrix.
         */
        fun aim(position: Vec3f, target: Vec3f, up: Vec3f, dst: Mat4f = Mat4f()): Mat4f {
            // Normalize the z-axis (position to target)
            target.subtract(position, zAxis).normalize(zAxis)

            // Compute the x-axis as the cross product of up and z-axis, then normalize
            up.cross(zAxis, xAxis).normalize(xAxis)

            // Compute the y-axis as the cross product of z-axis and x-axis, then normalize
            zAxis.cross(xAxis, yAxis).normalize(yAxis)

            // Set the matrix values
            dst.array[0] = xAxis.x
            dst.array[1] = xAxis.y
            dst.array[2] = xAxis.z
            dst.array[3] = 0f

            dst.array[4] = yAxis.x
            dst.array[5] = yAxis.y
            dst.array[6] = yAxis.z
            dst.array[7] = 0f

            dst.array[8] = zAxis.x
            dst.array[9] = zAxis.y
            dst.array[10] = zAxis.z
            dst.array[11] = 0f

            dst.array[12] = position.x
            dst.array[13] = position.y
            dst.array[14] = position.z
            dst.array[15] = 1f

            return dst
        }

        /**
         * Computes a 4-by-4 camera aim transformation.
         *
         * This is a matrix which positions an object aiming down negative Z.
         * toward the target.
         *
         * Note: this is the inverse of `lookAt`
         *
         * @param eye The position of the object.
         * @param target The position meant to be aimed at.
         * @param up A vector pointing up.
         * @param dst Matrix to hold result. If not passed a new one is created.
         * @return The aim matrix.
         */
        fun cameraAim(eye: Vec3f, target: Vec3f, up: Vec3f, dst: Mat4f = Mat4f()): Mat4f {
            eye.subtract(target, zAxis).normalize(zAxis)
            up.cross(zAxis, xAxis).normalize(xAxis)
            zAxis.cross(xAxis, yAxis).normalize(yAxis)

            return dst.apply {
                array[0] = xAxis.x; array[1] = xAxis.y; array[2] = xAxis.z; array[3] = 0f
                array[4] = yAxis.x; array[5] = yAxis.y; array[6] = yAxis.z; array[7] = 0f
                array[8] = zAxis.x; array[9] = zAxis.y; array[10] = zAxis.z; array[11] = 0f
                array[12] = eye.x; array[13] = eye.y; array[14] = eye.z; array[15] = 1f
            }
        }

        /**
         * Creates a Mat4 from the given [values].
         * You should generally not use this constructor as it assumes the array is already in the correct format.
         */
        operator fun invoke(vararg values: Float) = Mat4f(floatArrayOf(*values))
    }

    inline operator fun plus(other: Mat4f) = add(other)
    inline operator fun minus(other: Mat4f) = diff(other)
    inline operator fun times(scalar: Float) = multiplyScalar(scalar)
    inline operator fun times(matrix: Mat4f) = multiply(matrix)
    inline operator fun times(vector: Vec4f) = multiplyVector(vector)
    inline operator fun div(scalar: Float) = div(scalar, Mat4f())
    inline operator fun unaryMinus() = negate()
    inline operator fun get(index: Int): Float {
        return this.array[index]
    }

    inline operator fun set(index: Int, value: Float) {
        this.array[index] = value
    }

    inline operator fun get(row: Int, col: Int): Float {
        return this.array[col * 4 + row] // Column-major order
    }

    inline operator fun set(row: Int, col: Int, value: Float) {
        this.array[col * 4 + row] = value // Column-major order
    }

    inline var m00
        get() = this[0];
        set(value) {
            array[0] = value
        }
    inline var m01
        get() = this[4];
        set(value) {
            array[4] = value
        }
    inline var m02
        get() = this[8];
        set(value) {
            array[8] = value
        }
    inline var m03
        get() = this[12];
        set(value) {
            array[12] = value
        }
    inline var m10
        get() = this[1];
        set(value) {
            array[1] = value
        }
    inline var m11
        get() = this[5];
        set(value) {
            array[5] = value
        }
    inline var m12
        get() = this[9];
        set(value) {
            array[9] = value
        }
    inline var m13
        get() = this[13];
        set(value) {
            array[13] = value
        }
    inline var m20
        get() = this[2];
        set(value) {
            array[2] = value
        }
    inline var m21
        get() = this[6];
        set(value) {
            array[6] = value
        }
    inline var m22
        get() = this[10];
        set(value) {
            array[10] = value
        }
    inline var m23
        get() = this[14];
        set(value) {
            array[14] = value
        }
    inline var m30
        get() = this[3];
        set(value) {
            array[3] = value
        }
    inline var m31
        get() = this[7];
        set(value) {
            array[7] = value
        }
    inline var m32
        get() = this[11];
        set(value) {
            array[11] = value
        }
    inline var m33
        get() = this[15];
        set(value) {
            array[15] = value
        }

    init {
        if (array.size != 16) {
            throw IllegalArgumentException("Mat4 requires a 16-element FloatArray for storage.")
        }
    }

    /**
     * Sets this matrix to the identity matrix
     */
    fun setIdentity() {
        identity(this)
    }

    /**
     * Computes the determinant of `this`.
     */
    fun determinant(): Float {
        val m00 = array[0]
        val m01 = array[1]
        val m02 = array[2]
        val m03 = array[3]
        val m10 = array[4]
        val m11 = array[5]
        val m12 = array[6]
        val m13 = array[7]
        val m20 = array[8]
        val m21 = array[9]
        val m22 = array[10]
        val m23 = array[11]
        val m30 = array[12]
        val m31 = array[13]
        val m32 = array[14]
        val m33 = array[15]

        val tmp0 = m22 * m33
        val tmp1 = m32 * m23
        val tmp2 = m12 * m33
        val tmp3 = m32 * m13
        val tmp4 = m12 * m23
        val tmp5 = m22 * m13
        val tmp6 = m02 * m33
        val tmp7 = m32 * m03
        val tmp8 = m02 * m23
        val tmp9 = m22 * m03
        val tmp10 = m02 * m13
        val tmp11 = m12 * m03

        val t0 = (tmp0 * m11 + tmp3 * m21 + tmp4 * m31) -
                (tmp1 * m11 + tmp2 * m21 + tmp5 * m31)
        val t1 = (tmp1 * m01 + tmp6 * m21 + tmp9 * m31) -
                (tmp0 * m01 + tmp7 * m21 + tmp8 * m31)
        val t2 = (tmp2 * m01 + tmp7 * m11 + tmp10 * m31) -
                (tmp3 * m01 + tmp6 * m11 + tmp11 * m31)
        val t3 = (tmp5 * m01 + tmp8 * m11 + tmp11 * m21) -
                (tmp4 * m01 + tmp9 * m11 + tmp10 * m21)

        return m00 * t0 + m10 * t1 + m20 * t2 + m30 * t3
    }

    /**
     * Gets a copy of the internal FloatArray representation of the matrix.
     * Modifying the returned array will not affect the original matrix.
     */
    fun toFloatArray(dst: FloatArray = FloatArray(16)): FloatArray = array.copyInto(dst) // Return a copy for safety

    /**
     * Sets the values of `this` equal to the values of [mat].
     */
    fun set(mat: Mat4f) {
        mat.array.copyInto(this.array)
    }

    /**
     * Sets the values of `this` equal to the values of [arr], in column-major order.
     */
    fun set(arr: FloatArray) {
        arr.copyInto(this.array)
    }

    /**
     * Negates `this`.
     */
    fun negate(dst: Mat4f = Mat4f()): Mat4f {
        return dst.apply {
            array[0] = -this@Mat4f.array[0]; array[1] = -this@Mat4f.array[1]; array[2] = -this@Mat4f.array[2]; array[3] = -this@Mat4f.array[3]
            array[4] = -this@Mat4f.array[4]; array[5] = -this@Mat4f.array[5]; array[6] = -this@Mat4f.array[6]; array[7] = -this@Mat4f.array[7]
            array[8] = -this@Mat4f.array[8]; array[9] = -this@Mat4f.array[9]; array[10] = -this@Mat4f.array[10]; array[11] = -this@Mat4f.array[11]
            array[12] = -this@Mat4f.array[12]; array[13] = -this@Mat4f.array[13]; array[14] = -this@Mat4f.array[14]; array[15] = -this@Mat4f.array[15]
        }
    }

    /**
     * Multiplies `this` by the scalar [s].
     */
    fun multiplyScalar(s: Float, dst: Mat4f = Mat4f()): Mat4f {
        return dst.apply {
            array[0] = this@Mat4f.array[0] * s; array[1] = this@Mat4f.array[1] * s; array[2] = this@Mat4f.array[2] * s; array[3] = this@Mat4f.array[3] * s
            array[4] = this@Mat4f.array[4] * s; array[5] = this@Mat4f.array[5] * s; array[6] = this@Mat4f.array[6] * s; array[7] = this@Mat4f.array[7] * s
            array[8] = this@Mat4f.array[8] * s; array[9] = this@Mat4f.array[9] * s; array[10] = this@Mat4f.array[10] * s; array[11] = this@Mat4f.array[11] * s
            array[12] = this@Mat4f.array[12] * s; array[13] = this@Mat4f.array[13] * s; array[14] = this@Mat4f.array[14] * s; array[15] =
            this@Mat4f.array[15] * s
        }
    }

    /**
     * Divides `this` matrix by the scalar [s].
     */
    fun div(s: Float, dst: Mat4f = Mat4f()): Mat4f {
        return dst.apply {
            array[0] = this@Mat4f.array[0] / s; array[1] = this@Mat4f.array[1] / s; array[2] = this@Mat4f.array[2] / s; array[3] = this@Mat4f.array[3] / s
            array[4] = this@Mat4f.array[4] / s; array[5] = this@Mat4f.array[5] / s; array[6] = this@Mat4f.array[6] / s; array[7] = this@Mat4f.array[7] / s
            array[8] = this@Mat4f.array[8] / s; array[9] = this@Mat4f.array[9] / s; array[10] = this@Mat4f.array[10] / s; array[11] = this@Mat4f.array[11] / s
            array[12] = this@Mat4f.array[12] / s; array[13] = this@Mat4f.array[13] / s; array[14] = this@Mat4f.array[14] / s; array[15] =
            this@Mat4f.array[15] / s
        }
    }

    /**
     * Adds [other] to `this`.
     */
    fun add(other: Mat4f, dst: Mat4f = Mat4f()): Mat4f {
        return dst.apply {
            array[0] = this@Mat4f.array[0] + other.array[0]; array[1] = this@Mat4f.array[1] + other.array[1]
            array[2] = this@Mat4f.array[2] + other.array[2]; array[3] = this@Mat4f.array[3] + other.array[3]
            array[4] = this@Mat4f.array[4] + other.array[4]; array[5] = this@Mat4f.array[5] + other.array[5]
            array[6] = this@Mat4f.array[6] + other.array[6]; array[7] = this@Mat4f.array[7] + other.array[7]
            array[8] = this@Mat4f.array[8] + other.array[8]; array[9] = this@Mat4f.array[9] + other.array[9]
            array[10] = this@Mat4f.array[10] + other.array[10]; array[11] = this@Mat4f.array[11] + other.array[11]
            array[12] = this@Mat4f.array[12] + other.array[12]; array[13] = this@Mat4f.array[13] + other.array[13]
            array[14] = this@Mat4f.array[14] + other.array[14]; array[15] = this@Mat4f.array[15] + other.array[15]
        }
    }

    fun subtract(other: Mat4f, dst: Mat4f = Mat4f()) = diff(other, dst)

    /**
     * Calculates the difference between `this` and [other].
     */
    fun diff(other: Mat4f, dst: Mat4f = Mat4f()): Mat4f {
        return dst.apply {
            array[0] = this@Mat4f.array[0] - other.array[0]; array[1] = this@Mat4f.array[1] - other.array[1]
            array[2] = this@Mat4f.array[2] - other.array[2]; array[3] = this@Mat4f.array[3] - other.array[3]
            array[4] = this@Mat4f.array[4] - other.array[4]; array[5] = this@Mat4f.array[5] - other.array[5]
            array[6] = this@Mat4f.array[6] - other.array[6]; array[7] = this@Mat4f.array[7] - other.array[7]
            array[8] = this@Mat4f.array[8] - other.array[8]; array[9] = this@Mat4f.array[9] - other.array[9]
            array[10] = this@Mat4f.array[10] - other.array[10]; array[11] = this@Mat4f.array[11] - other.array[11]
            array[12] = this@Mat4f.array[12] - other.array[12]; array[13] = this@Mat4f.array[13] - other.array[13]
            array[14] = this@Mat4f.array[14] - other.array[14]; array[15] = this@Mat4f.array[15] - other.array[15]
        }
    }

    /**
     * Copies `this`.
     */
    fun copy(dst: Mat4f = Mat4f()): Mat4f {
        this.array.copyInto(dst.array)
        return dst
    }

    /**
     * Copies `this` (alias for [copy]).
     */
    fun clone(dst: Mat4f = Mat4f()): Mat4f = copy(dst)

    /**
     * Multiplies `this` by [other] (`this` * [other]).
     */
    fun multiply(other: Mat4f, dst: Mat4f = Mat4f()): Mat4f {
        val a00 = array[0]
        val a01 = array[1]
        val a02 = array[2]
        val a03 = array[3]
        val a10 = array[4]
        val a11 = array[5]
        val a12 = array[6]
        val a13 = array[7]
        val a20 = array[8]
        val a21 = array[9]
        val a22 = array[10]
        val a23 = array[11]
        val a30 = array[12]
        val a31 = array[13]
        val a32 = array[14]
        val a33 = array[15]

        val b00 = other.array[0]
        val b01 = other.array[1]
        val b02 = other.array[2]
        val b03 = other.array[3]
        val b10 = other.array[4]
        val b11 = other.array[5]
        val b12 = other.array[6]
        val b13 = other.array[7]
        val b20 = other.array[8]
        val b21 = other.array[9]
        val b22 = other.array[10]
        val b23 = other.array[11]
        val b30 = other.array[12]
        val b31 = other.array[13]
        val b32 = other.array[14]
        val b33 = other.array[15]

        return dst.apply {
            array[0] = a00 * b00 + a10 * b01 + a20 * b02 + a30 * b03
            array[1] = a01 * b00 + a11 * b01 + a21 * b02 + a31 * b03
            array[2] = a02 * b00 + a12 * b01 + a22 * b02 + a32 * b03
            array[3] = a03 * b00 + a13 * b01 + a23 * b02 + a33 * b03
            array[4] = a00 * b10 + a10 * b11 + a20 * b12 + a30 * b13
            array[5] = a01 * b10 + a11 * b11 + a21 * b12 + a31 * b13
            array[6] = a02 * b10 + a12 * b11 + a22 * b12 + a32 * b13
            array[7] = a03 * b10 + a13 * b11 + a23 * b12 + a33 * b13
            array[8] = a00 * b20 + a10 * b21 + a20 * b22 + a30 * b23
            array[9] = a01 * b20 + a11 * b21 + a21 * b22 + a31 * b23
            array[10] = a02 * b20 + a12 * b21 + a22 * b22 + a32 * b23
            array[11] = a03 * b20 + a13 * b21 + a23 * b22 + a33 * b23
            array[12] = a00 * b30 + a10 * b31 + a20 * b32 + a30 * b33
            array[13] = a01 * b30 + a11 * b31 + a21 * b32 + a31 * b33
            array[14] = a02 * b30 + a12 * b31 + a22 * b32 + a32 * b33
            array[15] = a03 * b30 + a13 * b31 + a23 * b32 + a33 * b33
        }
    }

    /**
     * Multiplies `this` by [other] (`this` * [other]) (alias for [multiply]).
     */
    fun mul(other: Mat4f, dst: Mat4f = Mat4f()): Mat4f = multiply(other, dst)

    /**
     * Multiplies this matrix by the vector [v].
     * @return The resulting vector.
     */
    fun multiplyVector(v: Vec4f, dst: Vec4f = Vec4f.create()): Vec4f {
        val x = v.x
        val y = v.y
        val z = v.z
        val w = v.w

        dst.x = this[0] * x + this[4] * y + this[8] * z + this[12] * w
        dst.y = this[1] * x + this[5] * y + this[9] * z + this[13] * w
        dst.z = this[2] * x + this[6] * y + this[10] * z + this[14] * w
        dst.w = this[3] * x + this[7] * y + this[11] * z + this[15] * w

        return dst
    }

    /**
     * Computes the transpose of `this`.
     */
    fun transpose(dst: Mat4f = Mat4f()): Mat4f {
        if (dst === this) {
            // Perform in-place transpose
            var t: Float

            t = array[1]; array[1] = array[4]; array[4] = t
            t = array[2]; array[2] = array[8]; array[8] = t
            t = array[3]; array[3] = array[12]; array[12] = t
            t = array[6]; array[6] = array[9]; array[9] = t
            t = array[7]; array[7] = array[13]; array[13] = t
            t = array[11]; array[11] = array[14]; array[14] = t

            return dst
        }

        val m00 = array[0]
        val m01 = array[1]
        val m02 = array[2]
        val m03 = array[3]
        val m10 = array[4]
        val m11 = array[5]
        val m12 = array[6]
        val m13 = array[7]
        val m20 = array[8]
        val m21 = array[9]
        val m22 = array[10]
        val m23 = array[11]
        val m30 = array[12]
        val m31 = array[13]
        val m32 = array[14]
        val m33 = array[15]

        return dst.apply {
            array[0] = m00; array[1] = m10; array[2] = m20; array[3] = m30
            array[4] = m01; array[5] = m11; array[6] = m21; array[7] = m31
            array[8] = m02; array[9] = m12; array[10] = m22; array[11] = m32
            array[12] = m03; array[13] = m13; array[14] = m23; array[15] = m33
        }
    }

    /**
     * Computes the inverse of `this`.
     * If `this` has no inverse, the result is undefined.
     */
    fun inverse(dst: Mat4f = Mat4f()): Mat4f {
        val m00 = array[0]
        val m01 = array[1]
        val m02 = array[2]
        val m03 = array[3]
        val m10 = array[4]
        val m11 = array[5]
        val m12 = array[6]
        val m13 = array[7]
        val m20 = array[8]
        val m21 = array[9]
        val m22 = array[10]
        val m23 = array[11]
        val m30 = array[12]
        val m31 = array[13]
        val m32 = array[14]
        val m33 = array[15]

        val tmp0 = m22 * m33
        val tmp1 = m32 * m23
        val tmp2 = m12 * m33
        val tmp3 = m32 * m13
        val tmp4 = m12 * m23
        val tmp5 = m22 * m13
        val tmp6 = m02 * m33
        val tmp7 = m32 * m03
        val tmp8 = m02 * m23
        val tmp9 = m22 * m03
        val tmp10 = m02 * m13
        val tmp11 = m12 * m03
        val tmp12 = m20 * m31
        val tmp13 = m30 * m21
        val tmp14 = m10 * m31
        val tmp15 = m30 * m11
        val tmp16 = m10 * m21
        val tmp17 = m20 * m11
        val tmp18 = m00 * m31
        val tmp19 = m30 * m01
        val tmp20 = m00 * m21
        val tmp21 = m20 * m01
        val tmp22 = m00 * m11
        val tmp23 = m10 * m01

        val t0 = (tmp0 * m11 + tmp3 * m21 + tmp4 * m31) -
                (tmp1 * m11 + tmp2 * m21 + tmp5 * m31)
        val t1 = (tmp1 * m01 + tmp6 * m21 + tmp9 * m31) -
                (tmp0 * m01 + tmp7 * m21 + tmp8 * m31)
        val t2 = (tmp2 * m01 + tmp7 * m11 + tmp10 * m31) -
                (tmp3 * m01 + tmp6 * m11 + tmp11 * m31)
        val t3 = (tmp5 * m01 + tmp8 * m11 + tmp11 * m21) -
                (tmp4 * m01 + tmp9 * m11 + tmp10 * m21)

        val d = 1.0f / (m00 * t0 + m10 * t1 + m20 * t2 + m30 * t3)

        return dst.apply {
            array[0] = t0 * d
            array[1] = t1 * d
            array[2] = t2 * d
            array[3] = t3 * d
            array[4] = ((tmp1 * m10 + tmp2 * m20 + tmp5 * m30) -
                    (tmp0 * m10 + tmp3 * m20 + tmp4 * m30)) * d
            array[5] = ((tmp0 * m00 + tmp7 * m20 + tmp8 * m30) -
                    (tmp1 * m00 + tmp6 * m20 + tmp9 * m30)) * d
            array[6] = ((tmp3 * m00 + tmp6 * m10 + tmp11 * m30) -
                    (tmp2 * m00 + tmp7 * m10 + tmp10 * m30)) * d
            array[7] = ((tmp4 * m00 + tmp9 * m10 + tmp10 * m20) -
                    (tmp5 * m00 + tmp8 * m10 + tmp11 * m20)) * d
            array[8] = ((tmp12 * m13 + tmp15 * m23 + tmp16 * m33) -
                    (tmp13 * m13 + tmp14 * m23 + tmp17 * m33)) * d
            array[9] = ((tmp13 * m03 + tmp18 * m23 + tmp21 * m33) -
                    (tmp12 * m03 + tmp19 * m23 + tmp20 * m33)) * d
            array[10] = ((tmp14 * m03 + tmp19 * m13 + tmp22 * m33) -
                    (tmp15 * m03 + tmp18 * m13 + tmp23 * m33)) * d
            array[11] = ((tmp17 * m03 + tmp20 * m13 + tmp23 * m23) -
                    (tmp16 * m03 + tmp21 * m13 + tmp22 * m23)) * d
            array[12] = ((tmp14 * m22 + tmp17 * m32 + tmp13 * m12) -
                    (tmp16 * m32 + tmp12 * m12 + tmp15 * m22)) * d
            array[13] = ((tmp20 * m32 + tmp12 * m02 + tmp19 * m22) -
                    (tmp18 * m22 + tmp21 * m32 + tmp13 * m02)) * d
            array[14] = ((tmp18 * m12 + tmp23 * m32 + tmp15 * m02) -
                    (tmp22 * m32 + tmp14 * m02 + tmp19 * m12)) * d
            array[15] = ((tmp22 * m22 + tmp16 * m02 + tmp21 * m12) -
                    (tmp20 * m12 + tmp23 * m22 + tmp17 * m02)) * d
        }
    }

    /**
     * Computes the inverse of `this` (alias for [inverse]).
     */
    fun invert(dst: Mat4f = Mat4f()): Mat4f = inverse(dst)

    /**
     * Gets the translation component of `this`.
     */
    fun getTranslation(dst: Vec3f = Vec3f.create()): Vec3f {
        dst.x = array[12]
        dst.y = array[13]
        dst.z = array[14]
        return dst
    }

    /**
     * Gets the scaling components of this matrix from its axes and stores them in [dst].
     * This assumes the matrix has orthogonal axes (no shear).
     * If there is shear, the result might not be meaningful as pure scaling factors.
     */
    fun getScaling(dst: Vec3f = Vec3f.create()): Vec3f {
        val xx = array[0]
        val xy = array[1]
        val xz = array[2]
        val yx = array[4]
        val yy = array[5]
        val yz = array[6]
        val zx = array[8]
        val zy = array[9]
        val zz = array[10]

        dst.x = sqrt(xx * xx + xy * xy + xz * xz)
        dst.y = sqrt(yx * yx + yy * yy + yz * yz)
        dst.z = sqrt(zx * zx + zy * zy + zz * zz)

        return dst
    }

    /**
     * Post-multiplies this 4x4 matrix by a 3D translation matrix created from [v] and writes the result into [dst].
     * `dst = this * translation(v)`
     *
     * Order of operations on a transformed vector:
     * - Column vectors (`dst * vec`): The translation defined by [v] is applied to `vec` **before** the transformation represented by the original matrix `this`.
     * - Row vectors (`vec * dst`): The translation defined by [v] is applied to `vec` **after** the transformation represented by the original matrix `this`.
     */
    fun translate(v: Vec3f, dst: Mat4f = Mat4f()): Mat4f = translate(v.x, v.y, v.z, dst)

    /**
     * Post-multiplies this 4x4 matrix by a 3D translation matrix created from [x], [y], [z] and writes the result into [dst].
     * `dst = this * translation(v)`
     *
     * Order of operations on a transformed vector:
     * - Column vectors (`dst * vec`): The translation defined by [x], [y], [z] is applied to `vec` **before** the transformation represented by the original matrix `this`.
     * - Row vectors (`vec * dst`): The translation defined by [x], [y], [z] is applied to `vec` **after** the transformation represented by the original matrix `this`.
     */
    fun translate(x: Float, y: Float, z: Float, dst: Mat4f = Mat4f()): Mat4f {
        val v0 = x
        val v1 = y
        val v2 = z

        val m00 = array[0]
        val m01 = array[1]
        val m02 = array[2]
        val m03 = array[3]
        val m10 = array[4]
        val m11 = array[5]
        val m12 = array[6]
        val m13 = array[7]
        val m20 = array[8]
        val m21 = array[9]
        val m22 = array[10]
        val m23 = array[11]

        if (this !== dst) {
            dst.array[0] = m00
            dst.array[1] = m01
            dst.array[2] = m02
            dst.array[3] = m03
            dst.array[4] = m10
            dst.array[5] = m11
            dst.array[6] = m12
            dst.array[7] = m13
            dst.array[8] = m20
            dst.array[9] = m21
            dst.array[10] = m22
            dst.array[11] = m23
        }

        dst.array[12] = m00 * v0 + m10 * v1 + m20 * v2 + array[12]
        dst.array[13] = m01 * v0 + m11 * v1 + m21 * v2 + array[13]
        dst.array[14] = m02 * v0 + m12 * v1 + m22 * v2 + array[14]
        dst.array[15] = m03 * v0 + m13 * v1 + m23 * v2 + array[15]

        return dst
    }

    /**
     * Post-multiplies this 4x4 matrix by a 3D rotation matrix about the X-axis by [angleInRadians] and writes the result into [dst].
     * `dst = this * rotationX(angleInRadians)`
     *
     * Order of operations on a transformed vector:
     * - Column vectors (`dst * vec`): The X-axis rotation is applied to `vec` **before** the transformation represented by the original matrix `this`.
     * - Row vectors (`vec * dst`): The X-axis rotation is applied to `vec` **after** the transformation represented by the original matrix `this`.
     */
    fun rotateX(angleInRadians: Float, dst: Mat4f = Mat4f()): Mat4f {
        val m10 = array[4]
        val m11 = array[5]
        val m12 = array[6]
        val m13 = array[7]
        val m20 = array[8]
        val m21 = array[9]
        val m22 = array[10]
        val m23 = array[11]

        val c = cos(angleInRadians)
        val s = sin(angleInRadians)

        dst.array[4] = c * m10 + s * m20
        dst.array[5] = c * m11 + s * m21
        dst.array[6] = c * m12 + s * m22
        dst.array[7] = c * m13 + s * m23
        dst.array[8] = c * m20 - s * m10
        dst.array[9] = c * m21 - s * m11
        dst.array[10] = c * m22 - s * m12
        dst.array[11] = c * m23 - s * m13

        if (this !== dst) {
            dst.array[0] = array[0]
            dst.array[1] = array[1]
            dst.array[2] = array[2]
            dst.array[3] = array[3]
            dst.array[12] = array[12]
            dst.array[13] = array[13]
            dst.array[14] = array[14]
            dst.array[15] = array[15]
        }

        return dst
    }

    /**
     * Post-multiplies this 4x4 matrix by a 3D rotation matrix about the Y-axis by [angleInRadians] and writes the result into [dst].
     * `dst = this * rotationY(angleInRadians)`
     *
     * Order of operations on a transformed vector:
     * - Column vectors (`dst * vec`): The Y-axis rotation is applied to `vec` **before** the transformation represented by the original matrix `this`.
     * - Row vectors (`vec * dst`): The Y-axis rotation is applied to `vec` **after** the transformation represented by the original matrix `this`.
     */
    fun rotateY(angleInRadians: Float, dst: Mat4f = Mat4f()): Mat4f {
        val m00 = array[0]
        val m01 = array[1]
        val m02 = array[2]
        val m03 = array[3]
        val m20 = array[8]
        val m21 = array[9]
        val m22 = array[10]
        val m23 = array[11]

        val c = cos(angleInRadians)
        val s = sin(angleInRadians)

        dst.array[0] = c * m00 - s * m20
        dst.array[1] = c * m01 - s * m21
        dst.array[2] = c * m02 - s * m22
        dst.array[3] = c * m03 - s * m23
        dst.array[8] = c * m20 + s * m00
        dst.array[9] = c * m21 + s * m01
        dst.array[10] = c * m22 + s * m02
        dst.array[11] = c * m23 + s * m03

        if (this !== dst) {
            dst.array[4] = array[4]
            dst.array[5] = array[5]
            dst.array[6] = array[6]
            dst.array[7] = array[7]
            dst.array[12] = array[12]
            dst.array[13] = array[13]
            dst.array[14] = array[14]
            dst.array[15] = array[15]
        }

        return dst
    }

    /**
     * Post-multiplies this 4x4 matrix by a 3D rotation matrix about the Z-axis by [angleInRadians] and writes the result into [dst].
     * `dst = this * rotationZ(angleInRadians)`
     *
     * Order of operations on a transformed vector:
     * - Column vectors (`dst * vec`): The Z-axis rotation is applied to `vec` **before** the transformation represented by the original matrix `this`.
     * - Row vectors (`vec * dst`): The Z-axis rotation is applied to `vec` **after** the transformation represented by the original matrix `this`.
     */
    fun rotateZ(angleInRadians: Float, dst: Mat4f = Mat4f()): Mat4f {
        val m00 = array[0]
        val m01 = array[1]
        val m02 = array[2]
        val m03 = array[3]
        val m10 = array[4]
        val m11 = array[5]
        val m12 = array[6]
        val m13 = array[7]

        val c = cos(angleInRadians)
        val s = sin(angleInRadians)

        dst.array[0] = c * m00 + s * m10
        dst.array[1] = c * m01 + s * m11
        dst.array[2] = c * m02 + s * m12
        dst.array[3] = c * m03 + s * m13
        dst.array[4] = c * m10 - s * m00
        dst.array[5] = c * m11 - s * m01
        dst.array[6] = c * m12 - s * m02
        dst.array[7] = c * m13 - s * m03

        if (this !== dst) {
            dst.array[8] = array[8]
            dst.array[9] = array[9]
            dst.array[10] = array[10]
            dst.array[11] = array[11]
            dst.array[12] = array[12]
            dst.array[13] = array[13]
            dst.array[14] = array[14]
            dst.array[15] = array[15]
        }

        return dst
    }

    /**
     * Post-multiplies this 4x4 matrix by a 3D scaling matrix created from [v] (for X, Y, and Z axes) and writes the result into [dst].
     * `dst = this * scaling(v)`
     *
     * Order of operations on a transformed vector:
     * - Column vectors (`dst * vec`): The scaling defined by [v] is applied to `vec` **before** the transformation represented by the original matrix `this`.
     * - Row vectors (`vec * dst`): The scaling defined by [v] is applied to `vec` **after** the transformation represented by the original matrix `this`.
     */
    fun scale(v: Vec3f, dst: Mat4f = Mat4f()): Mat4f = scale(v.x, v.y, v.z, dst)

    /**
     * Post-multiplies this 4x4 matrix by a 3D scaling matrix created from [v] (for X, Y, and Z axes) and writes the result into [dst].
     * `dst = this * scaling(v)`
     *
     * Order of operations on a transformed vector:
     * - Column vectors (`dst * vec`): The scaling defined by [v] is applied to `vec` **before** the transformation represented by the original matrix `this`.
     * - Row vectors (`vec * dst`): The scaling defined by [v] is applied to `vec` **after** the transformation represented by the original matrix `this`.
     */
    fun scale(x: Float, y: Float, z: Float, dst: Mat4f = Mat4f()): Mat4f {
        val v0 = x
        val v1 = y
        val v2 = z

        dst.array[0] = v0 * array[0]
        dst.array[1] = v0 * array[1]
        dst.array[2] = v0 * array[2]
        dst.array[3] = v0 * array[3]
        dst.array[4] = v1 * array[4]
        dst.array[5] = v1 * array[5]
        dst.array[6] = v1 * array[6]
        dst.array[7] = v1 * array[7]
        dst.array[8] = v2 * array[8]
        dst.array[9] = v2 * array[9]
        dst.array[10] = v2 * array[10]
        dst.array[11] = v2 * array[11]

        if (this !== dst) {
            dst.array[12] = array[12]
            dst.array[13] = array[13]
            dst.array[14] = array[14]
            dst.array[15] = array[15]
        }

        return dst
    }

    /**
     * Post-multiplies this 4x4 matrix by a 3D uniform scaling matrix created from [s] and writes the result into [dst].
     * `dst = this * uniformScaling(s)`
     *
     * Order of operations on a transformed vector:
     * - Column vectors (`dst * vec`): The uniform scaling by [s] is applied to `vec` **before** the transformation represented by the original matrix `this`.
     * - Row vectors (`vec * dst`): The uniform scaling by [s] is applied to `vec` **after** the transformation represented by the original matrix `this`.
     */
    fun uniformScale(s: Float, dst: Mat4f = Mat4f()): Mat4f {
        dst.array[0] = s * array[0]
        dst.array[1] = s * array[1]
        dst.array[2] = s * array[2]
        dst.array[3] = s * array[3]
        dst.array[4] = s * array[4]
        dst.array[5] = s * array[5]
        dst.array[6] = s * array[6]
        dst.array[7] = s * array[7]
        dst.array[8] = s * array[8]
        dst.array[9] = s * array[9]
        dst.array[10] = s * array[10]
        dst.array[11] = s * array[11]

        if (this !== dst) {
            dst.array[12] = array[12]
            dst.array[13] = array[13]
            dst.array[14] = array[14]
            dst.array[15] = array[15]
        }
        return dst
    }

    fun scale(s: Float, dst: Mat4f = Mat4f()): Mat4f = uniformScale(s, dst)

    /**
     * Pre-multiplies this 4x4 matrix by a 3D translation matrix created from [v] and writes the result into [dst].
     * `dst = translation(v) * this`
     *
     * Order of operations on a transformed vector:
     * - Column vectors (`dst * vec`): The translation defined by [v] is applied to `vec` **after** the transformation represented by the original matrix `this`.
     * - Row vectors (`vec * dst`): The translation defined by [v] is applied to `vec` **before** the transformation represented by the original matrix `this`.
     */
    fun preTranslate(v: Vec3f, dst: Mat4f = Mat4f()): Mat4f = preTranslate(v.x, v.y, v.z, dst)

    /**
     * Pre-multiplies this 4x4 matrix by a 3D translation matrix created from [v] and writes the result into [dst].
     * `dst = translation(v) * this`
     *
     * Order of operations on a transformed vector:
     * - Column vectors (`dst * vec`): The translation defined by [v] is applied to `vec` **after** the transformation represented by the original matrix `this`.
     * - Row vectors (`vec * dst`): The translation defined by [v] is applied to `vec` **before** the transformation represented by the original matrix `this`.
     */
    fun preTranslate(x: Float, y: Float, z: Float, dst: Mat4f = Mat4f()): Mat4f {
        val a0 = array[0];
        val a1 = array[1];
        val a2 = array[2];
        val a3 = array[3]
        val a4 = array[4];
        val a5 = array[5];
        val a6 = array[6];
        val a7 = array[7]
        val a8 = array[8];
        val a9 = array[9];
        val a10 = array[10];
        val a11 = array[11]
        val a12 = array[12];
        val a13 = array[13];
        val a14 = array[14];
        val a15 = array[15]

        dst.array[0] = a0 + x * a3
        dst.array[1] = a1 + y * a3
        dst.array[2] = a2 + z * a3
        dst.array[3] = a3

        dst.array[4] = a4 + x * a7
        dst.array[5] = a5 + y * a7
        dst.array[6] = a6 + z * a7
        dst.array[7] = a7

        dst.array[8] = a8 + x * a11
        dst.array[9] = a9 + y * a11
        dst.array[10] = a10 + z * a11
        dst.array[11] = a11

        dst.array[12] = a12 + x * a15
        dst.array[13] = a13 + y * a15
        dst.array[14] = a14 + z * a15
        dst.array[15] = a15
        return dst
    }

    /**
     * Pre-multiplies this 4x4 matrix by a 3D rotation matrix about the X-axis by [angleInRadians] and writes the result into [dst].
     * `dst = rotationX(angleInRadians) * this`
     *
     * Order of operations on a transformed vector:
     * - Column vectors (`dst * vec`): The X-axis rotation is applied to `vec` **after** the transformation represented by the original matrix `this`.
     * - Row vectors (`vec * dst`): The X-axis rotation is applied to `vec` **before** the transformation represented by the original matrix `this`.
     */
    fun preRotateX(angleInRadians: Float, dst: Mat4f = Mat4f()): Mat4f {
        val s = sin(angleInRadians)
        val c = cos(angleInRadians)

        val a1 = array[1];
        val a2 = array[2]
        val a5 = array[5];
        val a6 = array[6]
        val a9 = array[9];
        val a10 = array[10]
        val a13 = array[13];
        val a14 = array[14]

        if (dst !== this) {
            // Copy elements that are not modified or are read before write for their column
            dst.array[0] = array[0]; dst.array[3] = array[3]
            dst.array[4] = array[4]; dst.array[7] = array[7]
            dst.array[8] = array[8]; dst.array[11] = array[11]
            dst.array[12] = array[12]; dst.array[15] = array[15]
        }

        dst.array[1] = c * a1 - s * a2
        dst.array[2] = s * a1 + c * a2

        dst.array[5] = c * a5 - s * a6
        dst.array[6] = s * a5 + c * a6

        dst.array[9] = c * a9 - s * a10
        dst.array[10] = s * a9 + c * a10

        dst.array[13] = c * a13 - s * a14
        dst.array[14] = s * a13 + c * a14
        return dst
    }

    /**
     * Pre-multiplies this 4x4 matrix by a 3D rotation matrix about the Y-axis by [angleInRadians] and writes the result into [dst].
     * `dst = rotationY(angleInRadians) * this`
     *
     * Order of operations on a transformed vector:
     * - Column vectors (`dst * vec`): The Y-axis rotation is applied to `vec` **after** the transformation represented by the original matrix `this`.
     * - Row vectors (`vec * dst`): The Y-axis rotation is applied to `vec` **before** the transformation represented by the original matrix `this`.
     */
    fun preRotateY(angleInRadians: Float, dst: Mat4f = Mat4f()): Mat4f {
        val s = sin(angleInRadians)
        val c = cos(angleInRadians)

        val a0 = array[0];
        val a2 = array[2]
        val a4 = array[4];
        val a6 = array[6]
        val a8 = array[8];
        val a10 = array[10]
        val a12 = array[12];
        val a14 = array[14]

        if (dst !== this) {
            dst.array[1] = array[1]; dst.array[3] = array[3]
            dst.array[5] = array[5]; dst.array[7] = array[7]
            dst.array[9] = array[9]; dst.array[11] = array[11]
            dst.array[13] = array[13]; dst.array[15] = array[15]
        }

        dst.array[0] = c * a0 + s * a2
        dst.array[2] = -s * a0 + c * a2

        dst.array[4] = c * a4 + s * a6
        dst.array[6] = -s * a4 + c * a6

        dst.array[8] = c * a8 + s * a10
        dst.array[10] = -s * a8 + c * a10

        dst.array[12] = c * a12 + s * a14
        dst.array[14] = -s * a12 + c * a14
        return dst
    }

    /**
     * Pre-multiplies this 4x4 matrix by a 3D rotation matrix about the Z-axis by [angleInRadians] and writes the result into [dst].
     * `dst = rotationZ(angleInRadians) * this`
     *
     * Order of operations on a transformed vector:
     * - Column vectors (`dst * vec`): The Z-axis rotation is applied to `vec` **after** the transformation represented by the original matrix `this`.
     * - Row vectors (`vec * dst`): The Z-axis rotation is applied to `vec` **before** the transformation represented by the original matrix `this`.
     */
    fun preRotateZ(angleInRadians: Float, dst: Mat4f = Mat4f()): Mat4f {
        val s = sin(angleInRadians)
        val c = cos(angleInRadians)

        val a0 = array[0];
        val a1 = array[1]
        val a4 = array[4];
        val a5 = array[5]
        val a8 = array[8];
        val a9 = array[9]
        val a12 = array[12];
        val a13 = array[13]

        if (dst !== this) {
            dst.array[2] = array[2]; dst.array[3] = array[3]
            dst.array[6] = array[6]; dst.array[7] = array[7]
            dst.array[10] = array[10]; dst.array[11] = array[11]
            dst.array[14] = array[14]; dst.array[15] = array[15]
        }

        dst.array[0] = c * a0 - s * a1
        dst.array[1] = s * a0 + c * a1

        dst.array[4] = c * a4 - s * a5
        dst.array[5] = s * a4 + c * a5

        dst.array[8] = c * a8 - s * a9
        dst.array[9] = s * a8 + c * a9

        dst.array[12] = c * a12 - s * a13
        dst.array[13] = s * a12 + c * a13
        return dst
    }

    /**
     * Pre-multiplies this 4x4 matrix by a 3D scaling matrix created from [v] (for X, Y, and Z axes) and writes the result into [dst].
     * `dst = scaling(v) * this`
     *
     * Order of operations on a transformed vector:
     * - Column vectors (`dst * vec`): The scaling defined by [v] is applied to `vec` **after** the transformation represented by the original matrix `this`.
     * - Row vectors (`vec * dst`): The scaling defined by [v] is applied to `vec` **before** the transformation represented by the original matrix `this`.
     */
    fun preScale(v: Vec3f, dst: Mat4f = Mat4f()): Mat4f = preScale(v.x, v.y, v.z, dst)

    /**
     * Pre-multiplies this 4x4 matrix by a 3D scaling matrix created from [v] (for X, Y, and Z axes) and writes the result into [dst].
     * `dst = scaling(v) * this`
     *
     * Order of operations on a transformed vector:
     * - Column vectors (`dst * vec`): The scaling defined by [v] is applied to `vec` **after** the transformation represented by the original matrix `this`.
     * - Row vectors (`vec * dst`): The scaling defined by [v] is applied to `vec` **before** the transformation represented by the original matrix `this`.
     */
    fun preScale(x: Float, y: Float, z: Float, dst: Mat4f = Mat4f()): Mat4f {
        val sx = x;
        val sy = y;
        val sz = z

        dst.array[0] = array[0] * sx; dst.array[1] = array[1] * sy; dst.array[2] = array[2] * sz; dst.array[3] = array[3]
        dst.array[4] = array[4] * sx; dst.array[5] = array[5] * sy; dst.array[6] = array[6] * sz; dst.array[7] = array[7]
        dst.array[8] = array[8] * sx; dst.array[9] = array[9] * sy; dst.array[10] = array[10] * sz; dst.array[11] = array[11]
        dst.array[12] = array[12] * sx; dst.array[13] = array[13] * sy; dst.array[14] = array[14] * sz; dst.array[15] = array[15]
        return dst
    }


    /**
     * Pre-multiplies this 4x4 matrix by a 3D uniform scaling matrix created from [s] and writes the result into [dst].
     * `dst = uniformScaling(s) * this`
     *
     * Order of operations on a transformed vector:
     * - Column vectors (`dst * vec`): The uniform scaling by [s] is applied to `vec` **after** the transformation represented by the original matrix `this`.
     * - Row vectors (`vec * dst`): The uniform scaling by [s] is applied to `vec` **before** the transformation represented by the original matrix `this`.
     */
    fun preUniformScale(s: Float, dst: Mat4f = Mat4f()): Mat4f {
        dst.array[0] = array[0] * s; dst.array[1] = array[1] * s; dst.array[2] = array[2] * s; dst.array[3] = array[3]
        dst.array[4] = array[4] * s; dst.array[5] = array[5] * s; dst.array[6] = array[6] * s; dst.array[7] = array[7]
        dst.array[8] = array[8] * s; dst.array[9] = array[9] * s; dst.array[10] = array[10] * s; dst.array[11] = array[11]
        dst.array[12] = array[12] * s; dst.array[13] = array[13] * s; dst.array[14] = array[14] * s; dst.array[15] = array[15]
        return dst
    }

    /**
     * Post-multiplies this 4x4 matrix by a 3D rotation matrix about the given [axis] by [angleInRadians] and writes the result into [dst].
     * `dst = this * axisRotation(axis, angleInRadians)`
     *
     * Order of operations on a transformed vector:
     * - Column vectors (`dst * vec`): The axis rotation is applied to `vec` **before** the transformation represented by the original matrix `this`.
     * - Row vectors (`vec * dst`): The axis rotation is applied to `vec` **after** the transformation represented by the original matrix `this`.
     */
    fun axisRotate(axis: Vec3f, angleInRadians: Float, dst: Mat4f = Mat4f()): Mat4f = rotate(axis.x, axis.y, axis.z, angleInRadians, dst)

    /**
     * Post-multiplies this 4x4 matrix by a 3D rotation matrix about the given [axis] by [angleInRadians] and writes the result into [dst].
     * `dst = this * axisRotation(axis, angleInRadians)`
     *
     * Order of operations on a transformed vector:
     * - Column vectors (`dst * vec`): The axis rotation is applied to `vec` **before** the transformation represented by the original matrix `this`.
     * - Row vectors (`vec * dst`): The axis rotation is applied to `vec` **after** the transformation represented by the original matrix `this`.
     */
    inline fun rotate(axis: Vec3f, angleInRadians: Float, dst: Mat4f = Mat4f()) = axisRotate(axis, angleInRadians, dst)

    /**
     * Post-multiplies this 4x4 matrix by a 3D rotation matrix about the given [x], [y] ,[z] axis by [angleInRadians] and writes the result into [dst].
     * `dst = this * axisRotation(axis, angleInRadians)`
     *
     * Order of operations on a transformed vector:
     * - Column vectors (`dst * vec`): The axis rotation is applied to `vec` **before** the transformation represented by the original matrix `this`.
     * - Row vectors (`vec * dst`): The axis rotation is applied to `vec` **after** the transformation represented by the original matrix `this`.
     */
    fun rotate(x: Float, y: Float, z: Float, angleInRadians: Float, dst: Mat4f = Mat4f()): Mat4f {
        var x = x
        var y = y
        var z = z
        val n = sqrt(x * x + y * y + z * z);
        x /= n;
        y /= n;
        z /= n;
        val xx = x * x;
        val yy = y * y;
        val zz = z * z;
        val c = cos(angleInRadians);
        val s = sin(angleInRadians);
        val oneMinusCosine = 1 - c;

        val r00 = xx + (1 - xx) * c;
        val r01 = x * y * oneMinusCosine + z * s;
        val r02 = x * z * oneMinusCosine - y * s;
        val r10 = x * y * oneMinusCosine - z * s;
        val r11 = yy + (1 - yy) * c;
        val r12 = y * z * oneMinusCosine + x * s;
        val r20 = x * z * oneMinusCosine + y * s;
        val r21 = y * z * oneMinusCosine - x * s;
        val r22 = zz + (1 - zz) * c;

        val m00 = this[0];
        val m01 = this[1];
        val m02 = this[2];
        val m03 = this[3];
        val m10 = this[4];
        val m11 = this[5];
        val m12 = this[6];
        val m13 = this[7];
        val m20 = this[8];
        val m21 = this[9];
        val m22 = this[10];
        val m23 = this[11];

        dst[0] = r00 * m00 + r01 * m10 + r02 * m20;
        dst[1] = r00 * m01 + r01 * m11 + r02 * m21;
        dst[2] = r00 * m02 + r01 * m12 + r02 * m22;
        dst[3] = r00 * m03 + r01 * m13 + r02 * m23;
        dst[4] = r10 * m00 + r11 * m10 + r12 * m20;
        dst[5] = r10 * m01 + r11 * m11 + r12 * m21;
        dst[6] = r10 * m02 + r11 * m12 + r12 * m22;
        dst[7] = r10 * m03 + r11 * m13 + r12 * m23;
        dst[8] = r20 * m00 + r21 * m10 + r22 * m20;
        dst[9] = r20 * m01 + r21 * m11 + r22 * m21;
        dst[10] = r20 * m02 + r21 * m12 + r22 * m22;
        dst[11] = r20 * m03 + r21 * m13 + r22 * m23;

        if (this !== dst) {
            dst[12] = this[12];
            dst[13] = this[13];
            dst[14] = this[14];
            dst[15] = this[15];
        }

        return dst;
    }

    /**
     * Pre-multiplies this 4x4 matrix by a 3D rotation matrix about the given [axis] by [angleInRadians] and writes the result into [dst].
     * `dst = axisRotation(axis, angleInRadians) * this`
     *
     * Order of operations on a transformed vector:
     * - Column vectors (`dst * vec`): The axis rotation is applied to `vec` **after** the transformation represented by the original matrix `this`.
     * - Row vectors (`vec * dst`): The axis rotation is applied to `vec` **before** the transformation represented by the original matrix `this`.
     */
    fun preRotate(
        x: Float, y: Float, z: Float,
        angleInRadians: Float,
        dst: Mat4f = Mat4f(),
    ): Mat4f {
        // Normalize axis
        var ax = x
        var ay = y
        var az = z
        val n = sqrt(ax * ax + ay * ay + az * az)

        if (n < EPSILON) { // Axis is zero vector, rotation is identity
            if (this !== dst) {
                this.array.copyInto(dst.array) // dst = this
            }
            return dst
        }
        ax /= n
        ay /= n
        az /= n

        // Calculate rotation matrix components (column-major R)
        val s = sin(angleInRadians)
        val c = cos(angleInRadians)
        val oneMinusCosine = 1f - c

        val xx = ax * ax
        val yy = ay * ay
        val zz = az * az

        // These are the elements of the rotation matrix R, stored column-wise.
        // r00, r01, r02 are the first column of R.
        // r10, r11, r12 are the second column of R.
        // r20, r21, r22 are the third column of R.
        val r00 = xx + (1f - xx) * c
        val r01 = ax * ay * oneMinusCosine + az * s
        val r02 = ax * az * oneMinusCosine - ay * s

        val r10 = ax * ay * oneMinusCosine - az * s
        val r11 = yy + (1f - yy) * c
        val r12 = ay * az * oneMinusCosine + ax * s

        val r20 = ax * az * oneMinusCosine + ay * s
        val r21 = ay * az * oneMinusCosine - ax * s
        val r22 = zz + (1f - zz) * c

        // Load elements of `this` matrix (M) to handle `dst === this` case correctly.
        // M is column-major: a0,a1,a2,a3 is first column, etc.
        val a0 = array[0]; val a1 = array[1]; val a2 = array[2]; val a3 = array[3]
        val a4 = array[4]; val a5 = array[5]; val a6 = array[6]; val a7 = array[7]
        val a8 = array[8]; val a9 = array[9]; val a10 = array[10]; val a11 = array[11]
        val a12 = array[12]; val a13 = array[13]; val a14 = array[14]; val a15 = array[15]

        // Apply D = R * M
        // R is the rotation matrix. M is `this` matrix. D is `dst` matrix.

        // Column 0 of D
        dst.array[0] = r00 * a0 + r10 * a1 + r20 * a2
        dst.array[1] = r01 * a0 + r11 * a1 + r21 * a2
        dst.array[2] = r02 * a0 + r12 * a1 + r22 * a2
        // dst.array[3] = 0*a0 + 0*a1 + 0*a2 + 1*a3 = a3 (since R's 4th row is 0,0,0,1)
        if (dst !== this) { dst.array[3] = a3 } else { /* It's already a3 */ }


        // Column 1 of D
        dst.array[4] = r00 * a4 + r10 * a5 + r20 * a6
        dst.array[5] = r01 * a4 + r11 * a5 + r21 * a6
        dst.array[6] = r02 * a4 + r12 * a5 + r22 * a6
        if (dst !== this) { dst.array[7] = a7 } else { /* It's already a7 */ }

        // Column 2 of D
        dst.array[8] = r00 * a8 + r10 * a9 + r20 * a10
        dst.array[9] = r01 * a8 + r11 * a9 + r21 * a10
        dst.array[10] = r02 * a8 + r12 * a9 + r22 * a10
        if (dst !== this) { dst.array[11] = a11 } else { /* It's already a11 */ }

        // Column 3 of D
        dst.array[12] = r00 * a12 + r10 * a13 + r20 * a14
        dst.array[13] = r01 * a12 + r11 * a13 + r21 * a14
        dst.array[14] = r02 * a12 + r12 * a13 + r22 * a14
        if (dst !== this) { dst.array[15] = a15 } else { /* It's already a15 */ }

        return dst
    }

    /**
     * Pre-multiplies this 4x4 matrix by a 3D rotation matrix about the given [axis] by [angleInRadians] and writes the result into [dst].
     * `dst = axisRotation(axis, angleInRadians) * this`
     *
     * Order of operations on a transformed vector:
     * - Column vectors (`dst * vec`): The axis rotation is applied to `vec` **after** the transformation represented by the original matrix `this`.
     * - Row vectors (`vec * dst`): The axis rotation is applied to `vec` **before** the transformation represented by the original matrix `this`.
     */
    fun preRotate(axis: Vec3f, angleInRadians: Float, dst: Mat4f = Mat4f()): Mat4f = preRotate(axis.x, axis.y, axis.z, angleInRadians, dst)

    /**
     * Checks if `this` is approximately equal to [other].
     */
    fun equalsApproximately(other: Mat4f, tolerance: Float = EPSILON): Boolean {
        return abs(array[0] - other.array[0]) < tolerance &&
                abs(array[1] - other.array[1]) < tolerance &&
                abs(array[2] - other.array[2]) < tolerance &&
                abs(array[3] - other.array[3]) < tolerance &&
                abs(array[4] - other.array[4]) < tolerance &&
                abs(array[5] - other.array[5]) < tolerance &&
                abs(array[6] - other.array[6]) < tolerance &&
                abs(array[7] - other.array[7]) < tolerance &&
                abs(array[8] - other.array[8]) < tolerance &&
                abs(array[9] - other.array[9]) < tolerance &&
                abs(array[10] - other.array[10]) < tolerance &&
                abs(array[11] - other.array[11]) < tolerance &&
                abs(array[12] - other.array[12]) < tolerance &&
                abs(array[13] - other.array[13]) < tolerance &&
                abs(array[14] - other.array[14]) < tolerance &&
                abs(array[15] - other.array[15]) < tolerance
    }

    /**
     * Creates a matrix copy of `this` with the translation component set to [v].
     */
    fun setTranslation(v: Vec3f, dst: Mat4f = Mat4f()): Mat4f {
        val newDst = if (dst === this) this else copy(dst)

        newDst.array[12] = v.x
        newDst.array[13] = v.y
        newDst.array[14] = v.z

        return newDst
    }

    /**
     * Gets the specified [axis] (0=x, 1=y, 2=z) of `this` as a Vec3.
     */
    fun getAxis(axis: Int, dst: Vec3f = Vec3f.create()): Vec3f {
        val off = axis * 4
        dst.x = array[off + 0]
        dst.y = array[off + 1]
        dst.z = array[off + 2]
        return dst
    }

    /**
     * Creates a matrix copy of `this` with the specified [axis] (0=x, 1=y, 2=z) set to [v].
     */
    fun setAxis(v: Vec3f, axis: Int, dst: Mat4f = Mat4f()): Mat4f {
        val newDst = if (dst === this) this else copy(dst)

        val off = axis * 4
        newDst.array[off + 0] = v.x
        newDst.array[off + 1] = v.y
        newDst.array[off + 2] = v.z

        return newDst
    }

    /**
     * Sets the values of `this` from [v0] to [v15], in column-major order.
     */
    fun set(
        v0: Float, v1: Float, v2: Float, v3: Float,
        v4: Float, v5: Float, v6: Float, v7: Float,
        v8: Float, v9: Float, v10: Float, v11: Float,
        v12: Float, v13: Float, v14: Float, v15: Float,
    ): Mat4f = this.apply {
        array[0] = v0; array[1] = v1; array[2] = v2; array[3] = v3
        array[4] = v4; array[5] = v5; array[6] = v6; array[7] = v7
        array[8] = v8; array[9] = v9; array[10] = v10; array[11] = v11
        array[12] = v12; array[13] = v13; array[14] = v14; array[15] = v15
    }

    override fun toString(): String {
        return """
            [${m00.ns},${m01.ns},${m02.ns},${m03.ns}]
            [${m10.ns},${m11.ns},${m12.ns},${m13.ns}]
            [${m20.ns},${m21.ns},${m22.ns},${m23.ns}]
            [${m30.ns},${m31.ns},${m32.ns},${m33.ns}]
        """.trimIndent()
    }

    /**
     * Checks if `this` is exactly equal to [other].
     */
    override fun equals(other: Any?): Boolean {
        return other is Mat4f &&
                array[0] == other.array[0] &&
                array[1] == other.array[1] &&
                array[2] == other.array[2] &&
                array[3] == other.array[3] &&
                array[4] == other.array[4] &&
                array[5] == other.array[5] &&
                array[6] == other.array[6] &&
                array[7] == other.array[7] &&
                array[8] == other.array[8] &&
                array[9] == other.array[9] &&
                array[10] == other.array[10] &&
                array[11] == other.array[11] &&
                array[12] == other.array[12] &&
                array[13] == other.array[13] &&
                array[14] == other.array[14] &&
                array[15] == other.array[15]
    }

    override fun hashCode(): Int {
        return array.contentHashCode()
    }
}
