package io.github.natanfudge.wgpu4k.matrix.test

import io.github.natanfudge.wgpu4k.matrix.*
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.tan
import kotlin.test.*

// Helper function for Mat4f approximate equality check
internal fun assertMat4EqualsApproximately(expected: Mat4f, actual: Mat4f, message: String? = null, tolerance: Float = EPSILON) {
    assertTrue(expected.equalsApproximately(actual, tolerance), "$message: Expected \n<${expected}> \nbut got \n<${actual}>")
}

// Helper function for Mat4f exact equality check
internal fun assertMat4Equals(expected: Mat4f, actual: Mat4f, message: String? = null) {
    assertEquals(expected, actual, message)
}



class Mat4fTest {
    // --- Companion Object Tests ---

    @Test
    fun testCompanionIdentity() {
        val expected = Mat4f.rowMajor(
            1f, 0f, 0f, 0f,
            0f, 1f, 0f, 0f,
            0f, 0f, 1f, 0f,
            0f, 0f, 0f, 1f
        )
        assertMat4Equals(expected, Mat4f.identity(), "Default identity")

        val dst = Mat4f.identity()
        val result = Mat4f.identity(dst)
        assertMat4Equals(expected, dst, "Identity with destination")
        assertSame(dst, result, "Identity should return destination")
    }

    @Test
    fun testCompanionFromFloatArray() {
        // Mat4f constructor and fromFloatArray expect column-major data.
        // Mat4f.rowMajor is used for easier definition of expected values.
        val colMajorValues = floatArrayOf(
            1f, 2f, 3f, 4f,       // Col 0
            5f, 6f, 7f, 8f,       // Col 1
            9f, 10f, 11f, 12f,    // Col 2
            13f, 14f, 15f, 16f    // Col 3
        )
        val expected = Mat4f( // Constructor is column-major
            1f, 2f, 3f, 4f,
            5f, 6f, 7f, 8f,
            9f, 10f, 11f, 12f,
            13f, 14f, 15f, 16f
        )
        val actual = Mat4f.fromFloatArray(colMajorValues)
        assertMat4Equals(expected, actual, "fromFloatArray basic")


        // Edge case: Array with zeros
        val zeroValues = FloatArray(16)
        val zeroMat = Mat4f.fromFloatArray(zeroValues)
        assertMat4Equals(Mat4f.rowMajor(0f,0f,0f,0f, 0f,0f,0f,0f, 0f,0f,0f,0f, 0f,0f,0f,0f), zeroMat, "fromFloatArray zeros")

        // Edge case: Invalid size
        assertFailsWith<IllegalArgumentException>("fromFloatArray should fail for too small array") {
            Mat4f.fromFloatArray(FloatArray(15))
        }
    }

    @Test
    fun testCompanionInvokeAndRowMajorConstructor() {
        // Test the invoke operator (column-major)
        val mInvoke = Mat4f(
            1f, 2f, 3f, 4f,    // col 0
            5f, 6f, 7f, 8f,    // col 1
            9f, 10f, 11f, 12f, // col 2
            13f, 14f, 15f, 16f // col 3
        )
        // Test rowMajor constructor
        val mRowMajor = Mat4f.rowMajor(
            1f, 5f, 9f, 13f,   // row 0
            2f, 6f, 10f, 14f,  // row 1
            3f, 7f, 11f, 15f,  // row 2
            4f, 8f, 12f, 16f   // row 3
        )
        assertMat4Equals(mInvoke, mRowMajor, "Invoke (column-major) vs rowMajor constructor")

        val expectedArray = floatArrayOf(
            1f, 2f, 3f, 4f,
            5f, 6f, 7f, 8f,
            9f, 10f, 11f, 12f,
            13f, 14f, 15f, 16f
        )
        assertContentEquals(expectedArray, mInvoke.toFloatArray(), "Invoke constructor basic toFloatArray")
        assertContentEquals(expectedArray, mRowMajor.toFloatArray(), "rowMajor constructor basic toFloatArray")


        // Edge case: Identity
        val idInvoke = Mat4f(
            1f, 0f, 0f, 0f,
            0f, 1f, 0f, 0f,
            0f, 0f, 1f, 0f,
            0f, 0f, 0f, 1f
        )
        val idRowMajor = Mat4f.rowMajor(
            1f, 0f, 0f, 0f,
            0f, 1f, 0f, 0f,
            0f, 0f, 1f, 0f,
            0f, 0f, 0f, 1f
        )
        assertMat4Equals(Mat4f.identity(), idInvoke, "Invoke constructor identity")
        assertMat4Equals(Mat4f.identity(), idRowMajor, "rowMajor constructor identity")

        // Edge case: Zero
        val zeroInvoke = Mat4f(0f,0f,0f,0f, 0f,0f,0f,0f, 0f,0f,0f,0f, 0f,0f,0f,0f)
        val zeroRowMajor = Mat4f.rowMajor(0f,0f,0f,0f, 0f,0f,0f,0f, 0f,0f,0f,0f, 0f,0f,0f,0f)
        val zeroArray = FloatArray(16)
        assertContentEquals(zeroArray, zeroInvoke.toFloatArray(), "Invoke constructor zero")
        assertContentEquals(zeroArray, zeroRowMajor.toFloatArray(), "rowMajor constructor zero")
    }

    @Test
    fun testCompanionFromQuat() {
        // Basic rotation (90 degrees around Y)
        val q1 = Quatf.fromAxisAngle(Vec3f(0f, 1f, 0f), PI.toFloat() / 2f)
        val expected1 = Mat4f.rotationY(PI.toFloat() / 2f)
        assertMat4EqualsApproximately(expected1, Mat4f.fromQuat(q1), message = "fromQuat Y rotation")

        // Identity quaternion
        val qId = Quatf.identity()
        val expectedId = Mat4f.identity()
        assertMat4EqualsApproximately(expectedId, Mat4f.fromQuat(qId), message = "fromQuat identity")
    }

    @Test
    fun testCompanionTranslation() {
        val v = Vec3f(5f, -10f, 15f)
        val expected = Mat4f.rowMajor(
            1f, 0f, 0f, 5f,
            0f, 1f, 0f, -10f,
            0f, 0f, 1f, 15f,
            0f, 0f, 0f, 1f
        )
        assertMat4Equals(expected, Mat4f.translation(v), "Translation basic")
        val dst = Mat4f.identity()
        Mat4f.translation(v, dst)
        assertMat4Equals(expected, dst, "Translation basic with dst")


        // Edge case: Zero vector
        val zeroV = Vec3f(0f, 0f, 0f)
        assertMat4Equals(Mat4f.identity(), Mat4f.translation(zeroV), "Translation zero vector")

        // Edge case: Large values
        val largeV = Vec3f(1e6f, -1e6f, 1e5f)
        val expectedLarge = Mat4f.rowMajor(
            1f, 0f, 0f, 1e6f,
            0f, 1f, 0f, -1e6f,
            0f, 0f, 1f, 1e5f,
            0f, 0f, 0f, 1f
        )
        assertMat4Equals(expectedLarge, Mat4f.translation(largeV), "Translation large values")
    }

    @Test
    fun testCompanionRotationX() {
        val angle = PI.toFloat() / 6f // 30 degrees
        val c = cos(angle)
        val s = sin(angle)
        val expected = Mat4f.rowMajor(
            1f, 0f, 0f, 0f,
            0f, c, -s, 0f,
            0f, s, c, 0f,
            0f, 0f, 0f, 1f
        )
        assertMat4EqualsApproximately(expected, Mat4f.rotationX(angle), "RotationX basic (30 deg)")
        val dst = Mat4f.identity()
        Mat4f.rotationX(angle, dst)
        assertMat4EqualsApproximately(expected, dst, "RotationX basic (30 deg) with dst")


        // Edge case: Zero angle
        assertMat4EqualsApproximately(Mat4f.identity(), Mat4f.rotationX(0f), "RotationX zero angle")

        // Edge case: 180 degrees (PI)
        val mPi = Mat4f.rotationX(PI.toFloat())
        val expectedPi = Mat4f.rowMajor(
            1f, 0f, 0f, 0f,
            0f, -1f, 0f, 0f,
            0f, 0f, -1f, 0f,
            0f, 0f, 0f, 1f
        )
        assertMat4EqualsApproximately(expectedPi, mPi, tolerance = 1e-6f, message = "RotationX 180 deg")
    }

    @Test
    fun testCompanionRotationY() {
        val angle = -PI.toFloat() / 2f // -90 degrees
        val c = cos(angle)
        val s = sin(angle)
        val expected = Mat4f.rowMajor(
            c, 0f, s, 0f,
            0f, 1f, 0f, 0f,
            -s, 0f, c, 0f,
            0f, 0f, 0f, 1f
        )
        assertMat4EqualsApproximately(expected, Mat4f.rotationY(angle), tolerance = 1e-6f, message = "RotationY basic (-90 deg)")
        val dst = Mat4f.identity()
        Mat4f.rotationY(angle, dst)
        assertMat4EqualsApproximately(expected, dst, tolerance = 1e-6f, message = "RotationY basic (-90 deg) with dst")


        // Edge case: Zero angle
        assertMat4EqualsApproximately(Mat4f.identity(), Mat4f.rotationY(0f), "RotationY zero angle")

        // Edge case: 360 degrees (2 PI)
        assertMat4EqualsApproximately(Mat4f.identity(), Mat4f.rotationY(2f * PI.toFloat()), tolerance = 1e-6f, message = "RotationY full circle")
    }

    @Test
    fun testCompanionRotationZ() {
        val angle = PI.toFloat() / 4f // 45 degrees
        val c = cos(angle)
        val s = sin(angle)
        val expected = Mat4f.rowMajor(
            c, -s, 0f, 0f,
            s, c, 0f, 0f,
            0f, 0f, 1f, 0f,
            0f, 0f, 0f, 1f
        )
        assertMat4EqualsApproximately(expected, Mat4f.rotationZ(angle), "RotationZ basic (45 deg)")
        val dst = Mat4f.identity()
        Mat4f.rotationZ(angle, dst)
        assertMat4EqualsApproximately(expected, dst, "RotationZ basic (45 deg) with dst")


        // Edge case: Zero angle
        assertMat4EqualsApproximately(Mat4f.identity(), Mat4f.rotationZ(0f), "RotationZ zero angle")

        // Edge case: Full rotation (360 degrees / 2 PI)
        assertMat4EqualsApproximately(
            Mat4f.identity(),
            Mat4f.rotationZ(2f * PI.toFloat()),
            tolerance = 1e-6f,
            message = "RotationZ full circle"
        )
    }

    @Test
    fun testCompanionScaling() {
        val v = Vec3f(2f, -3f, 4f)
        val expected = Mat4f.rowMajor(
            2f, 0f, 0f, 0f,
            0f, -3f, 0f, 0f,
            0f, 0f, 4f, 0f,
            0f, 0f, 0f, 1f
        )
        assertMat4Equals(expected, Mat4f.scaling(v), "Scaling basic")
        val dst = Mat4f.identity()
        Mat4f.scaling(v, dst)
        assertMat4Equals(expected, dst, "Scaling basic with dst")


        // Edge case: Zero vector (results in zero matrix for top-left 3x3, and 1 in w)
        val zeroV = Vec3f(0f, 0f, 0f)
        val expectedZero = Mat4f.rowMajor(
            0f, 0f, 0f, 0f,
            0f, 0f, 0f, 0f,
            0f, 0f, 0f, 0f,
            0f, 0f, 0f, 1f
        )
        assertMat4Equals(expectedZero, Mat4f.scaling(zeroV), "Scaling zero vector")

        // Edge case: Scaling by 1 (identity)
        val oneV = Vec3f(1f, 1f, 1f)
        assertMat4Equals(Mat4f.identity(), Mat4f.scaling(oneV), "Scaling by one")
    }

    @Test
    fun testCompanionUniformScaling() {
        val s = 5f
        val expected = Mat4f.rowMajor(
            5f, 0f, 0f, 0f,
            0f, 5f, 0f, 0f,
            0f, 0f, 5f, 0f,
            0f, 0f, 0f, 1f
        )
        assertMat4Equals(expected, Mat4f.uniformScaling(s), "UniformScaling basic")
        val dst = Mat4f.identity()
        Mat4f.uniformScaling(s, dst)
        assertMat4Equals(expected, dst, "UniformScaling basic with dst")


        // Edge case: Scale by 0
        val expectedZero = Mat4f.rowMajor(
            0f, 0f, 0f, 0f,
            0f, 0f, 0f, 0f,
            0f, 0f, 0f, 0f,
            0f, 0f, 0f, 1f
        )
        assertMat4Equals(expectedZero, Mat4f.uniformScaling(0f), "UniformScaling by zero")

        // Edge case: Scale by 1
        assertMat4Equals(Mat4f.identity(), Mat4f.uniformScaling(1f), "UniformScaling by one")
    }

    @Test
    fun testCompanionLookAt() {
        val eye = Vec3f(0f, 0f, 5f)
        val target = Vec3f(0f, 0f, 0f)
        val up = Vec3f(0f, 1f, 0f)

        // Expected matrix for eye at (0,0,5) looking at origin, up (0,1,0)
        // Right handed system: X-axis = (1,0,0), Y-axis = (0,1,0), Z-axis = (0,0,-1) (view direction)
        // Translation = -eye = (0,0,-5)
        val expected = Mat4f.rowMajor(
            1f, 0f, 0f, 0f,
            0f, 1f, 0f, 0f,
            0f, 0f, 1f, -5f, // z is negated by view, then translated by -eye.z
            0f, 0f, 0f, 1f
        ).transpose() // lookAt produces a view matrix, which is effectively transposed world coordinates
                       // Correction: My manual calculation was a bit off.
                       // The view matrix transforms world coordinates to view coordinates.
                       // If eye is at +5Z, world objects at Z=0 should appear at Z=-5 in view space.
                       // The Z-axis of the view space points *out* of the screen.
                       // Forward vector f = normalize(target - eye) = (0,0,-1)
                       // Side vector s = normalize(cross(f, up)) = (1,0,0)
                       // Up' vector u = cross(s, f) = (0,1,0)
                       // Matrix is:
                       // [ sx  sy  sz  -dot(s,eye) ]
                       // [ ux  uy  uz  -dot(u,eye) ]
                       // [ -fx -fy -fz -dot(-f,eye)]
                       // [ 0   0   0   1           ]
        val expectedLookAt = Mat4f.rowMajor(
            1f, 0f, 0f, -eye.x, // s.x, s.y, s.z, -dot(s, eye)
            0f, 1f, 0f, -eye.y, // u.x, u.y, u.z, -dot(u, eye)
            0f, 0f, 1f, -eye.z, // -f.x, -f.y, -f.z, -dot(-f, eye) -> f is (0,0,-1) so -f is (0,0,1)
            0f, 0f, 0f, 1f
        )
        // After re-deriving, the translation part is -(s.eye, u.eye, -f.eye)
        // For eye(0,0,5), target(0,0,0), up(0,1,0):
        // f = (0,0,-1)
        // s = (1,0,0)
        // u = (0,1,0)
        // expected:
        // [ 1  0  0  0 ]
        // [ 0  1  0  0 ]
        // [ 0  0  1 -5 ]
        // [ 0  0  0  1 ]

        var actual = Mat4f.lookAt(eye, target, up)
        assertMat4EqualsApproximately(expectedLookAt, actual, message = "lookAt basic")

        val dst = Mat4f.identity()
        Mat4f.lookAt(eye, target, up, dst)
        assertMat4EqualsApproximately(expectedLookAt, dst, message = "lookAt basic with dst")

        // Test case 2: eye at (5,5,5), target (0,0,0), up (0,1,0)
        val eye2 = Vec3f(5f, 5f, 5f)
        val target2 = Vec3f(0f, 0f, 0f)
        val up2 = Vec3f(0f, 1f, 0f)
        // f = normalize(-5,-5,-5) approx (-0.577, -0.577, -0.577)
        // s = normalize(cross(f, up)) = normalize((-0.577, 0, 0.577)) approx (-0.707, 0, 0.707)
        // u = cross(s,f) approx (0.408, -0.816, 0.408) - this seems off, should be normalized
        // Let's use a known result or verify with a reliable source for complex lookAt
        // Using JOML's output for verification:
        // eye(5,5,5), center(0,0,0), up(0,1,0)
        // JOML result (column major):
        //  0.70710677, -0.4082483,  0.57735026, 0.0
        //  0.0,         0.8164966,  0.57735026, 0.0
        // -0.70710677, -0.4082483,  0.57735026, 0.0
        //  0.0,         0.0,        -8.660254,  1.0
        // Transposed for row-major:
        val expectedLookAt2 = Mat4f.rowMajor(
            0.70710677f, 0.0f, -0.70710677f, 0.0f,
            -0.4082483f, 0.8164966f, -0.4082483f, 0.0f,
            0.57735026f, 0.57735026f, 0.57735026f, -8.660254f,
            0.0f, 0.0f, 0.0f, 1.0f
        )
        actual = Mat4f.lookAt(eye2, target2, up2)
        assertMat4EqualsApproximately(expectedLookAt2, actual, message = "lookAt complex", tolerance = 1e-5f) // Increased tolerance

    }

    @Test
    fun testCompanionOrthographic() {
        val left = -1f; val right = 1f; val bottom = -1f; val top = 1f; val near = 1f; val far = 100f
        val tx = (right + left) / (left - right)
        val ty = (top + bottom) / (bottom - top)
        val tz = near / (near - far)

        val expected = Mat4f.rowMajor(
            2f / (right - left), 0f, 0f, tx,
            0f, 2f / (top - bottom), 0f, ty,
            0f, 0f, 1f / (near - far), tz,
            0f, 0f, 0f, 1f
        )
        var actual = Mat4f.orthographic(left, right, bottom, top, near, far)
        assertMat4EqualsApproximately(expected, actual, message = "orthographic basic")

        val dst = Mat4f.identity()
        Mat4f.orthographic(left, right, bottom, top, near, far, dst)
        assertMat4EqualsApproximately(expected, dst, message = "orthographic basic with dst")

    }

    @Test
    fun testCompanionPerspective() {
        val fovy = PI.toFloat() / 2f // 90 degrees
        val aspect = 1.0f
        val near = 1f
        val far = 100f

        val f = tan(PI.toFloat() * 0.5f - 0.5f * fovy)
        val rangeInv = 1f / (near - far)

        val expected = Mat4f.rowMajor(
            f / aspect, 0f, 0f, 0f,
            0f, f, 0f, 0f,
            0f, 0f, far * rangeInv, far * near * rangeInv,
            0f, 0f, -1f, 0f
        )
        var actual = Mat4f.perspective(fovy, aspect, near, far)
        assertMat4EqualsApproximately(expected, actual, message = "perspective basic", tolerance = 1e-5f)

        val dst = Mat4f.identity()
        Mat4f.perspective(fovy, aspect, near, far, dst)
        assertMat4EqualsApproximately(expected, dst, message = "perspective basic with dst", tolerance = 1e-5f)

        // Test with different aspect ratio
        val aspect2 = 16f/9f
        val expected2 = Mat4f.rowMajor(
            f / aspect2, 0f, 0f, 0f,
            0f, f, 0f, 0f,
            0f, 0f, far * rangeInv, far * near * rangeInv,
            0f, 0f, -1f, 0f
        )
        val actual2 = Mat4f.perspective(fovy, aspect2, near, far)
        assertMat4EqualsApproximately(expected2, actual2, message = "perspective different aspect", tolerance = 1e-5f)

        // Test with infinite far plane
        val expectedInf = Mat4f.rowMajor(
            f / aspect, 0f, 0f, 0f,
            0f, f, 0f, 0f,
            0f, 0f, -1f, -near,
            0f, 0f, -1f, 0f
        )
        val actualInf = Mat4f.perspective(fovy, aspect, near, Float.POSITIVE_INFINITY)
        assertMat4EqualsApproximately(expectedInf, actualInf, message = "perspective with infinite far plane", tolerance = 1e-5f)
    }

    // --- Instance Method Tests ---

    @Test
    fun testSet() {
        val m = Mat4f.identity()
        val result = m.set( // Column-major arguments
            1f, 2f, 3f, 4f,
            5f, 6f, 7f, 8f,
            9f, 10f, 11f, 12f,
            13f, 14f, 15f, 16f
        )
        val expected = Mat4f( // Constructor is also column-major
            1f, 2f, 3f, 4f,
            5f, 6f, 7f, 8f,
            9f, 10f, 11f, 12f,
            13f, 14f, 15f, 16f
        )
        assertMat4Equals(expected, m, "Set basic")
        assertSame(m, result, "Set should return self")

        // Set to identity
        m.set(
            1f, 0f, 0f, 0f,
            0f, 1f, 0f, 0f,
            0f, 0f, 1f, 0f,
            0f, 0f, 0f, 1f
        )
        assertMat4Equals(Mat4f.identity(), m, "Set to identity")

        // Set from another Mat4f
        val source = Mat4f.translation(Vec3f(1f,2f,3f))
        m.set(source)
        assertMat4Equals(source, m, "Set from another Mat4f")
        assertNotSame(source, m, "Set from another Mat4f should copy values, not reference")

        // Set from FloatArray (column-major)
        val floatArray = floatArrayOf(
            16f,15f,14f,13f,
            12f,11f,10f,9f,
            8f,7f,6f,5f,
            4f,3f,2f,1f)
        val expectedFromArray = Mat4f(
            16f,15f,14f,13f,
            12f,11f,10f,9f,
            8f,7f,6f,5f,
            4f,3f,2f,1f
        )
        m.set(floatArray)
        assertMat4Equals(expectedFromArray, m, "Set from FloatArray")
    }

    @Test
    fun testNegate() {
        val m = Mat4f.rowMajor(
            1f, -2f, 3f, -4f,
            -5f, 6f, -7f, 8f,
            9f, -10f, 11f, -12f,
            -13f, 14f, -15f, 16f
        )
        val expected = Mat4f.rowMajor(
            -1f, 2f, -3f, 4f,
            5f, -6f, 7f, -8f,
            -9f, 10f, -11f, 12f,
            13f, -14f, 15f, -16f
        )
        val negatedM = m.negate() // Test without destination
        assertMat4Equals(expected, negatedM, "Negate without destination")
        assertNotSame(m, negatedM, "Negate without destination should create new instance")


        val dst = Mat4f.identity()
        val result = m.negate(dst) // Test with destination
        assertMat4Equals(expected, dst, "Negate with destination")
        assertSame(dst, result, "Negate with destination should return destination")

        // Edge case: Negate zero matrix
        val zero = Mat4f.rowMajor(0f,0f,0f,0f, 0f,0f,0f,0f, 0f,0f,0f,0f, 0f,0f,0f,0f)
        val zeroNegated = zero.negate()
        assertMat4Equals(zero, zeroNegated, "Negate zero matrix")
    }

    @Test
    fun testMultiplyScalar() {
        val m = Mat4f.rowMajor(
            1f, 2f, 3f, 4f,
            5f, 6f, 7f, 8f,
            9f, 10f, 11f, 12f,
            13f, 14f, 15f, 16f
        )
        val s = 2f
        val expected = Mat4f.rowMajor(
            2f, 4f, 6f, 8f,
            10f, 12f, 14f, 16f,
            18f, 20f, 22f, 24f,
            26f, 28f, 30f, 32f
        )

        val scaledM = m.multiplyScalar(s) // Test without destination
        assertMat4Equals(expected, scaledM, "MultiplyScalar without destination")
        assertNotSame(m, scaledM, "MultiplyScalar without destination should create new instance")

        val dst = Mat4f.identity()
        val result = m.multiplyScalar(s, dst) // Test with destination
        assertMat4Equals(expected, dst, "MultiplyScalar with destination")
        assertSame(dst, result, "MultiplyScalar with destination should return destination")

        // Edge case: Multiply by zero
        val zeroScaled = m.multiplyScalar(0f)
        val expectedZero = Mat4f.rowMajor(0f,0f,0f,0f, 0f,0f,0f,0f, 0f,0f,0f,0f, 0f,0f,0f,0f)
        assertMat4Equals(expectedZero, zeroScaled, "MultiplyScalar by zero")

        // Edge case: Multiply by one
        val oneScaled = m.multiplyScalar(1f)
        assertMat4Equals(m, oneScaled, "MultiplyScalar by one")
        assertNotSame(m, oneScaled, "MultiplyScalar by one should still create new instance if no dst")
    }

    @Test
    fun testDiv() { // Assumes div is scalar division
        val m = Mat4f.rowMajor(
            2f, 4f, 6f, 8f,
            10f, 12f, 14f, 16f,
            18f, 20f, 22f, 24f,
            26f, 28f, 30f, 32f
        )
        val s = 2f
        val expected = Mat4f.rowMajor(
            1f, 2f, 3f, 4f,
            5f, 6f, 7f, 8f,
            9f, 10f, 11f, 12f,
            13f, 14f, 15f, 16f
        )

        val divM = m.div(s) // Test without destination
        assertMat4Equals(expected, divM, "Div without destination")
        assertNotSame(m, divM, "Div without destination should create new instance")

        val dst = Mat4f.identity()
        val result = m.div(s, dst) // Test with destination
        assertMat4Equals(expected, dst, "Div with destination")
        assertSame(dst, result, "Div with destination should return destination")

        // Edge case: Divide by one
        val oneDiv = m.div(1f)
        assertMat4Equals(m, oneDiv, "Div by one")
        assertNotSame(m, oneDiv, "Div by one should still create new instance if no dst")

        // Edge case: Divide by zero
        val divByZero = m.div(0f)
        assertTrue(divByZero.m00.isInfinite(), "Dividing by zero should result in Inf/NaN")
        // Check a few more elements
        assertTrue(divByZero.m11.isInfinite(), "Dividing by zero should result in Inf/NaN")
        assertTrue(divByZero.m33.isInfinite(), "Dividing by zero should result in Inf/NaN")
        val zeroMat = Mat4f.rowMajor(0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f)
        val zeroDivByZero = zeroMat.div(0f)
        assertTrue(zeroDivByZero.m00.isNaN(), "0/0 should be NaN")
    }

    @Test
    fun testAdd() { // Also tests plus
        val m1 = Mat4f.rowMajor(
            1f, 2f, 3f, 4f, 5f, 6f, 7f, 8f,
            9f, 10f, 11f, 12f, 13f, 14f, 15f, 16f
        )
        val m2 = Mat4f.rowMajor(
            16f, 15f, 14f, 13f, 12f, 11f, 10f, 9f,
            8f, 7f, 6f, 5f, 4f, 3f, 2f, 1f
        )
        val expected = Mat4f.rowMajor(
            17f, 17f, 17f, 17f, 17f, 17f, 17f, 17f,
            17f, 17f, 17f, 17f, 17f, 17f, 17f, 17f
        )

        val addedM = m1.add(m2) // Test add without destination
        assertMat4Equals(expected, addedM, "Add without destination")
        assertNotSame(m1, addedM, "Add without destination should create new instance")

        val dst = Mat4f.identity()
        val result = m1.add(m2, dst) // Test add with destination
        assertMat4Equals(expected, dst, "Add with destination")
        assertSame(dst, result, "Add with destination should return destination")

        // Test plus alias
        val plusM = m1.plus(m2)
        assertMat4Equals(expected, plusM, "Plus alias without destination")
        assertNotSame(m1, plusM, "Plus alias without destination should create new instance")

        // Edge case: Add identity
        val id = Mat4f.identity()
        val mPlusId = m1.add(id)
        val expectedMPlusId = Mat4f.rowMajor(
            2f, 2f, 3f, 4f, 5f, 7f, 7f, 8f,
            9f, 10f, 12f, 12f, 13f, 14f, 15f, 17f
        )
        assertMat4Equals(expectedMPlusId, mPlusId, "Add identity")
    }

    @Test
    fun testSubtract() { // Also tests minus
        val m1 = Mat4f.rowMajor(
            17f, 17f, 17f, 17f, 17f, 17f, 17f, 17f,
            17f, 17f, 17f, 17f, 17f, 17f, 17f, 17f
        )
        val m2 = Mat4f.rowMajor(
            16f, 15f, 14f, 13f, 12f, 11f, 10f, 9f,
            8f, 7f, 6f, 5f, 4f, 3f, 2f, 1f
        )
        val expected = Mat4f.rowMajor(
            1f, 2f, 3f, 4f, 5f, 6f, 7f, 8f,
            9f, 10f, 11f, 12f, 13f, 14f, 15f, 16f
        )

        val subtractedM = m1.subtract(m2) // Test subtract without destination
        assertMat4Equals(expected, subtractedM, "Subtract without destination")
        assertNotSame(m1, subtractedM, "Subtract without destination should create new instance")

        val dst = Mat4f.identity()
        val result = m1.subtract(m2, dst) // Test subtract with destination
        assertMat4Equals(expected, dst, "Subtract with destination")
        assertSame(dst, result, "Subtract with destination should return destination")

        // Test minus alias
        val minusM = m1.minus(m2)
        assertMat4Equals(expected, minusM, "Minus alias without destination")
        assertNotSame(m1, minusM, "Minus alias without destination should create new instance")

        // Edge case: Subtract identity
        val id = Mat4f.identity()
        val mMinusId = expected.subtract(id) // Using 'expected' from above which is 1..16
        val expectedMMinusId = Mat4f.rowMajor(
            0f, 2f, 3f, 4f, 5f, 5f, 7f, 8f,
            9f, 10f, 10f, 12f, 13f, 14f, 15f, 15f
        )
        assertMat4Equals(expectedMMinusId, mMinusId, "Subtract identity")
    }

    @Test
    fun testTranspose() {
        val m = Mat4f.rowMajor(
            1f, 2f, 3f, 4f,
            5f, 6f, 7f, 8f,
            9f, 10f, 11f, 12f,
            13f, 14f, 15f, 16f
        )
        val expected = Mat4f.rowMajor( // Which is column-major of original
            1f, 5f, 9f, 13f,
            2f, 6f, 10f, 14f,
            3f, 7f, 11f, 15f,
            4f, 8f, 12f, 16f
        )

        val transposedM = m.transpose() // Test without destination
        assertMat4Equals(expected, transposedM, "Transpose without destination")
        assertNotSame(m, transposedM, "Transpose without destination should create new instance")

        val dst = Mat4f.identity()
        val result = m.transpose(dst) // Test with destination
        assertMat4Equals(expected, dst, "Transpose with destination")
        assertSame(dst, result, "Transpose with destination should return destination")

        // Test transpose of an identity matrix (should be identity)
        val id = Mat4f.identity()
        val transposedId = id.transpose()
        assertMat4Equals(id, transposedId, "Transpose of identity")
        // Transposing an identity matrix into itself should work
        id.transpose(id)
        assertMat4Equals(Mat4f.identity(), id, "Transpose identity into self")


        // Test transpose twice gives original
        val twiceTransposed = m.transpose().transpose()
        assertMat4Equals(m, twiceTransposed, "Transpose twice gives original")
    }

    @Test
    fun testDeterminant() {
        // Case 1: Identity matrix
        val id = Mat4f.identity()
        assertFloatEqualsApproximately(1f, id.determinant(), message = "Determinant of identity")

        // Case 2: Simple scaling matrix
        val scale = Mat4f.scaling(Vec3f(2f, 3f, 4f)) // det = 2*3*4*1 = 24
        assertFloatEqualsApproximately(24f, scale.determinant(), message = "Determinant of scaling matrix")

        // Case 3: Matrix with a zero row/column (det should be 0)
        val zeroCol = Mat4f.rowMajor(
            0f, 2f, 3f, 4f,
            0f, 6f, 7f, 8f,
            0f, 10f, 11f, 12f,
            0f, 14f, 15f, 16f
        )
        assertFloatEqualsApproximately(0f, zeroCol.determinant(), message = "Determinant of matrix with zero column")

        val zeroRow = Mat4f.rowMajor(
            0f, 0f, 0f, 0f,
            5f, 6f, 7f, 8f,
            9f, 10f, 11f, 12f,
            13f, 14f, 15f, 16f
        )
        assertFloatEqualsApproximately(0f, zeroRow.determinant(), message = "Determinant of matrix with zero row")


        // Case 4: A known matrix and its determinant
        // From https://www.mathsisfun.com/algebra/matrix-determinant.html (example for 3x3, extend to 4x4)
        // For simplicity, use a matrix that's easy to calculate or verify
        // Example: translation matrix, determinant should be 1
        val trans = Mat4f.translation(Vec3f(10f, 20f, 30f))
        assertFloatEqualsApproximately(1f, trans.determinant(), message = "Determinant of translation matrix")

        // Example: rotation matrix, determinant should be 1 (or -1 for reflection)
        val rot = Mat4f.rotationY(PI.toFloat() / 3f)
        assertFloatEqualsApproximately(1f, rot.determinant(), message = "Determinant of rotation matrix")

        // A more complex matrix (e.g. from a solved example)
        // Let M =
        // [ 1, 0, 2, -1 ]
        // [ 3, 0, 0, 5  ]
        // [ 2, 1, 4, -3 ]
        // [ 1, 0, 5, 0  ]
        // Det = -1 * Det of [ [1,2,-1], [3,0,5], [1,5,0] ] (cofactor expansion along 2nd col)
        // Det3x3 = 1(0-25) - 2(0-5) + (-1)(15-0) = -25 + 10 - 15 = -30
        // So Det4x4 = -1 * (-30) = 30
        val mComplex = Mat4f.rowMajor(
            1f, 0f, 2f, -1f,
            3f, 0f, 0f, 5f,
            2f, 1f, 4f, -3f,
            1f, 0f, 5f, 0f
        )
        assertFloatEqualsApproximately(30f, mComplex.determinant(), message = "Determinant of a complex matrix", tolerance = 1e-5f)
    }

    @Test
    fun testInvert() {
        // Case 1: Identity matrix
        val id = Mat4f.identity()
        val invId = id.invert()
        assertMat4EqualsApproximately(id, invId, message = "Inverse of identity")
        assertNotSame(id, invId, "Invert without dst should be new instance")
        val dstId = Mat4f.identity()
        id.invert(dstId)
        assertMat4EqualsApproximately(id, dstId, message = "Inverse of identity with dst")


        // Case 2: Simple scaling matrix
        val scale = Mat4f.scaling(Vec3f(2f, 3f, 4f))
        val expectedInvScale = Mat4f.scaling(Vec3f(1f/2f, 1f/3f, 1f/4f))
        val invScale = scale.invert()
        assertMat4EqualsApproximately(expectedInvScale, invScale, message = "Inverse of scaling matrix")

        // Case 3: Translation matrix
        val trans = Mat4f.translation(Vec3f(1f, 2f, 3f))
        val expectedInvTrans = Mat4f.translation(Vec3f(-1f, -2f, -3f))
        val invTrans = trans.invert()
        assertMat4EqualsApproximately(expectedInvTrans, invTrans, message = "Inverse of translation matrix")

        // Case 4: Rotation matrix (inverse is transpose)
        val rot = Mat4f.rotationZ(PI.toFloat() / 4f)
        val expectedInvRot = rot.transpose() // For pure rotation, inv(R) = transpose(R)
        val invRot = rot.invert()
        assertMat4EqualsApproximately(expectedInvRot, invRot, message = "Inverse of rotation matrix")

        // Case 5: A general invertible matrix
        // M = [ 1, 0, 0, 1 ]  (Translate X by 1)
        //     [ 0, 2, 0, 0 ]  (Scale Y by 2)
        //     [ 0, 0, 1, 0 ]
        //     [ 0, 0, 0, 1 ]
        // Inv(M) = [ 1, 0, 0, -1 ]
        //          [ 0, 0.5,0, 0 ]
        //          [ 0, 0, 1, 0 ]
        //          [ 0, 0, 0, 1 ]
        val mGeneral = Mat4f.identity().translate(Vec3f(1f,0f,0f)).scale(Vec3f(1f,2f,1f))
        // Order matters: scale then translate
        val mGen = Mat4f.scaling(Vec3f(1f,2f,1f)).translate(Vec3f(1f,0f,0f))
        // This is actually: Scale(1,2,1) then Translate(1,0,0)
        // S = [1,0,0,0; 0,2,0,0; 0,0,1,0; 0,0,0,1]
        // T = [1,0,0,1; 0,1,0,0; 0,0,1,0; 0,0,0,1]
        // M = T * S = [1,0,0,1; 0,2,0,0; 0,0,1,0; 0,0,0,1]
        // Inv(M) = Inv(S) * Inv(T)
        // InvS = [1,0,0,0; 0,0.5,0,0; 0,0,1,0; 0,0,0,1]
        // InvT = [1,0,0,-1; 0,1,0,0; 0,0,1,0; 0,0,0,1]
        // InvM = [1,0,0,-1; 0,0.5,0,0; 0,0,1,0; 0,0,0,1]
        val expectedInvGeneral = Mat4f.rowMajor(
            1f, 0f, 0f, -1f,
            0f, 0.5f, 0f, 0f,
            0f, 0f, 1f, 0f,
            0f, 0f, 0f, 1f
        )
        val actualInvGeneral = mGen.invert()
        assertMat4EqualsApproximately(expectedInvGeneral, actualInvGeneral, message = "Inverse of general matrix (scale then translate)")


        // Test M * M^-1 = I
        val original = Mat4f.rowMajor(
            1f, 2f, 0f, 1f,
            0f, 1f, 3f, 0f,
            -1f,0f, 1f, 2f,
            0f, 0f, -1f,1f
        )
        val originalCopy = original.clone()
        val inverted = original.invert()
        assertNotNull(inverted, "Invertible matrix should return non-null inverse")
        val product = originalCopy.multiply(inverted!!) // Use originalCopy as multiply modifies self
        assertMat4EqualsApproximately(Mat4f.identity(), product, message = "M * M^-1 = I", tolerance = 1e-5f)

        val productDst = Mat4f.identity()
        originalCopy.multiply(inverted, productDst)
        assertMat4EqualsApproximately(Mat4f.identity(), productDst, message = "M * M^-1 = I (with dst)", tolerance = 1e-5f)
    }

    @Test
    fun testMultiplyMat4f() { // Also tests times (operator)
        val m1 = Mat4f.rowMajor( // Scale by 2 then translate by (1,0,0)
            2f, 0f, 0f, 1f,
            0f, 2f, 0f, 0f,
            0f, 0f, 2f, 0f,
            0f, 0f, 0f, 1f
        )
        // This is actually: M1 = T(1,0,0) * S(2,2,2)
        // So m1 = [2,0,0,1; 0,2,0,0; 0,0,2,0; 0,0,0,1] (if constructor was row-major for values)
        // But constructor is column-major.
        // Let's redefine m1 and m2 clearly.
        // m1 = Scale (2,2,2)
        val scaleM = Mat4f.uniformScaling(2f)
        // m2 = Translate (1,2,3)
        val transM = Mat4f.translation(Vec3f(1f, 2f, 3f))

        // Expected result of transM * scaleM (post-multiply scaleM to transM)
        // T = [1,0,0,1; 0,1,0,2; 0,0,1,3; 0,0,0,1]
        // S = [2,0,0,0; 0,2,0,0; 0,0,2,0; 0,0,0,1]
        // T*S = [2,0,0,1; 0,2,0,2; 0,0,2,3; 0,0,0,1]
        val expectedTS = Mat4f.rowMajor(
            2f, 0f, 0f, 1f,
            0f, 2f, 0f, 2f,
            0f, 0f, 2f, 3f,
            0f, 0f, 0f, 1f
        )
        val actualTS = transM.clone().multiply(scaleM) // transM is 'this', scaleM is 'other'
        assertMat4EqualsApproximately(expectedTS, actualTS, message = "Multiply T*S (transM.multiply(scaleM))")

        // Expected result of scaleM * transM
        // S*T = [2,0,0,2; 0,2,0,4; 0,0,2,6; 0,0,0,1]
        val expectedST = Mat4f.rowMajor(
            2f, 0f, 0f, 2f,
            0f, 2f, 0f, 4f,
            0f, 0f, 2f, 6f,
            0f, 0f, 0f, 1f
        )
        val actualST = scaleM.clone().multiply(transM)
        assertMat4EqualsApproximately(expectedST, actualST, message = "Multiply S*T (scaleM.multiply(transM))")

        // Test with destination
        val dst = Mat4f.identity()
        scaleM.clone().multiply(transM, dst)
        assertMat4EqualsApproximately(expectedST, dst, message = "Multiply S*T with destination")
        assertSame(dst, scaleM.clone().multiply(transM, dst), "Multiply with dst should return dst")


        // Test 'times' operator (should be equivalent to multiply)
        // Note: a * b in Kotlin is a.times(b)
        // If Mat4f.multiply modifies 'this', then 'a * b' should be like 'a.clone().multiply(b)'
        // The current implementation of multiply(other, dst) does: dst.set(this).multiplyRight(other)
        // And multiply(other) does: this.multiplyRight(other)
        // So 'this' is modified.
        // The 'times' operator should probably create a new matrix.
        // Let's assume 'times' creates a new matrix: val result = this.clone().multiply(other, new Mat4f())
        // Or if 'times' is just an alias for 'multiply(other)' then it modifies 'this'.
        // The current Mat4f.kt shows 'operator fun times(other: Mat4f): Mat4f = this.multiply(other, Mat4f())'
        // This means it creates a new matrix and does not modify 'this'.
        val timesResult = scaleM * transM // scaleM.times(transM)
        assertMat4EqualsApproximately(expectedST, timesResult, "Operator times S*T")
        assertNotSame(scaleM, timesResult, "Operator times should create new instance")
        assertNotSame(transM, timesResult, "Operator times should create new instance")
        // Verify original scaleM is not modified by 'times'
        assertMat4Equals(Mat4f.uniformScaling(2f), scaleM, "Original matrix unchanged after 'times' operator")


        // Multiply by identity
        val m = Mat4f.rotationX(PI.toFloat() / 3f)
        val mCopy = m.clone()
        val mTimesId = m.clone().multiply(Mat4f.identity())
        assertMat4EqualsApproximately(mCopy, mTimesId, "Multiply by identity (right)")

        val idTimesM = Mat4f.identity().multiply(m.clone())
        assertMat4EqualsApproximately(mCopy, idTimesM, "Multiply by identity (left)")
    }


    @Test
    fun testInstanceTranslate() {
        val m = Mat4f.identity()
        val tVec = Vec3f(10f, -5f, 3f)
        val expected = Mat4f.translation(tVec) // Companion method creates a new translation matrix

        val result = m.translate(tVec, m) // Modifies m
        assertMat4EqualsApproximately(expected, m, "Instance translate basic")
        assertSame(m, result, "Instance translate should return self")

        // Chain translations
        val m2 = Mat4f.identity()
        m2.translate(Vec3f(1f, 1f, 1f), m2)
        m2.translate(Vec3f(2f, 3f, 4f), m2) // This translation is post-multiplied
        // Expected: T(1,1,1) * T(2,3,4) = T(1+2, 1+3, 1+4) = T(3,4,5)
        val expectedChained = Mat4f.translation(Vec3f(3f, 4f, 5f))
        assertMat4EqualsApproximately(expectedChained, m2, "Instance translate chained")

        // Translate with destination
        val m3 = Mat4f.scaling(Vec3f(2f,2f,2f)) // Start with a non-identity matrix
        val m3Original = m3.clone()
        val dst = Mat4f.identity()
        val resultDst = m3.translate(tVec, dst) // dst = m3 * T(tVec)
        val expectedDst = m3Original.multiply(Mat4f.translation(tVec))
        assertMat4EqualsApproximately(expectedDst, dst, "Instance translate with destination")
        assertSame(dst, resultDst, "Instance translate with destination should return dst")
        assertMat4EqualsApproximately(m3Original, m3, "Instance translate with destination should not modify self")
    }

    @Test
    fun testInstanceRotateXYZ() {
        val angle = PI.toFloat() / 4f // 45 degrees

        // RotateX
        val mX = Mat4f.identity()
        val expectedX = Mat4f.rotationX(angle)
        val resultX = mX.rotateX(angle, mX)
        assertMat4EqualsApproximately(expectedX, mX, "Instance rotateX")
        assertSame(mX, resultX)

        // RotateY
        val mY = Mat4f.identity()
        val expectedY = Mat4f.rotationY(angle)
        val resultY = mY.rotateY(angle, mY)
        assertMat4EqualsApproximately(expectedY, mY, "Instance rotateY")
        assertSame(mY, resultY)

        // RotateZ
        val mZ = Mat4f.identity()
        val expectedZ = Mat4f.rotationZ(angle)
        val resultZ = mZ.rotateZ(angle, mZ)
        assertMat4EqualsApproximately(expectedZ, mZ, "Instance rotateZ")
        assertSame(mZ, resultZ)

        // Chained rotations (e.g., R_y * R_x applied to identity)
        val mChain = Mat4f.identity()
        mChain.rotateX(angle, mChain) // mChain is now Rx
        mChain.rotateY(angle, mChain) // mChain is now Rx * Ry (post-multiply Ry)
        val expectedChain = Mat4f.rotationX(angle).multiply(Mat4f.rotationY(angle))
        assertMat4EqualsApproximately(expectedChain, mChain, "Instance rotate chained X then Y", tolerance = 1e-5f)

        // Rotate with destination
        val mBase = Mat4f.translation(Vec3f(1f,0f,0f))
        val mBaseOriginal = mBase.clone()
        val dst = Mat4f.identity()
        val resultDst = mBase.rotateZ(angle, dst) // dst = mBase * Rz
        val expectedDst = mBaseOriginal.multiply(Mat4f.rotationZ(angle))
        assertMat4EqualsApproximately(expectedDst, dst, "Instance rotateZ with destination")
        assertSame(dst, resultDst)
        assertMat4EqualsApproximately(mBaseOriginal, mBase, "Instance rotateZ with destination should not modify self")
    }

    @Test
    fun testInstanceRotateAxisAngle() {
        val axis = Vec3f(1f, 1f, 0f).normalize()
        val angle = PI.toFloat() / 3f // 60 degrees

        val m = Mat4f.identity()
        // Expected: Create rotation matrix from axis-angle and multiply identity by it
        val rotationMat = Mat4f.fromQuat(Quatf.fromAxisAngle(axis, angle)) // Companion.fromQuat is a good reference
        val expected = Mat4f.identity().multiply(rotationMat)

        val result = m.rotate(axis, angle, m) // Modifies m
        assertMat4EqualsApproximately(expected, m, "Instance rotate axis-angle", tolerance = 1e-5f)
        assertSame(m, result)

        // Rotate with destination
        val mBase = Mat4f.translation(Vec3f(1f,2f,3f))
        val mBaseOriginal = mBase.clone()
        val dst = Mat4f.identity()
        val resultDst = mBase.rotate(axis, angle, dst) // dst = mBase * R(axis,angle)
        val expectedDst = mBaseOriginal.multiply(rotationMat)
        assertMat4EqualsApproximately(expectedDst, dst, "Instance rotate axis-angle with destination", tolerance = 1e-5f)
        assertSame(dst, resultDst)
        assertMat4EqualsApproximately(mBaseOriginal, mBase, "Instance rotate axis-angle with destination should not modify self")
    }

    @Test
    fun testInstanceScale() {
        val m = Mat4f.identity()
        val sVec = Vec3f(2f, -1f, 0.5f)
        val expected = Mat4f.scaling(sVec) // Companion method

        val result = m.scale(sVec, m) // Modifies m
        assertMat4EqualsApproximately(expected, m, "Instance scale basic")
        assertSame(m, result)

        // Chain scales
        val m2 = Mat4f.identity()
        m2.scale(Vec3f(2f, 2f, 2f), m2)
        m2.scale(Vec3f(1f, 2f, 3f), m2) // Post-multiplies S(1,2,3)
        // Expected: S(2,2,2) * S(1,2,3) = S(2*1, 2*2, 2*3) = S(2,4,6)
        val expectedChained = Mat4f.scaling(Vec3f(2f, 4f, 6f))
        assertMat4EqualsApproximately(expectedChained, m2, "Instance scale chained")

        // Scale with destination
        val m3 = Mat4f.translation(Vec3f(1f,1f,1f))
        val m3Original = m3.clone()
        val dst = Mat4f.identity()
        val resultDst = m3.scale(sVec, dst) // dst = m3 * S(sVec)
        val expectedDst = m3Original.multiply(Mat4f.scaling(sVec))
        assertMat4EqualsApproximately(expectedDst, dst, "Instance scale with destination")
        assertSame(dst, resultDst)
        assertMat4EqualsApproximately(m3Original, m3, "Instance scale with destination should not modify self")
    }

    @Test
    fun testInstanceUniformScale() {
        val m = Mat4f.identity()
        val sVal = 3f
        val expected = Mat4f.uniformScaling(sVal) // Companion method

        val result = m.uniformScale(sVal, m) // Modifies m
        assertMat4EqualsApproximately(expected, m, "Instance uniformScale basic")
        assertSame(m, result)

        // Chain uniform scales
        val m2 = Mat4f.identity()
        m2.uniformScale(2f, m2)
        m2.uniformScale(3f, m2) // Post-multiplies S(3)
        // Expected: S(2) * S(3) = S(2*3) = S(6)
        val expectedChained = Mat4f.uniformScaling(6f)
        assertMat4EqualsApproximately(expectedChained, m2, "Instance uniformScale chained")

        // UniformScale with destination
        val m3 = Mat4f.rotationX(PI.toFloat()/2f)
        val m3Original = m3.clone()
        val dst = Mat4f.identity()
        val resultDst = m3.uniformScale(sVal, dst) // dst = m3 * S(sVal)
        val expectedDst = m3Original.multiply(Mat4f.uniformScaling(sVal))
        assertMat4EqualsApproximately(expectedDst, dst, "Instance uniformScale with destination")
        assertSame(dst, resultDst)
        assertMat4EqualsApproximately(m3Original, m3, "Instance uniformScale with destination should not modify self")
    }

    @Test
    fun testGetTranslation() {
        val transVec = Vec3f(10f, 20f, 30f)
        val m = Mat4f.translation(transVec)
            .rotateY(PI.toFloat() / 2f) // Add some rotation to make it non-trivial
            .scale(Vec3f(2f,2f,2f))   // Add some scaling

        val out = Vec3f()
        val result = m.getTranslation(out)
        assertVec3EqualsApproximately(transVec, out, EPSILON,"getTranslation basic")
        assertSame(out, result, "getTranslation should return the destination vector")

        // Test with a matrix that has no translation (should return zero vector)
        val rotM = Mat4f.rotationX(PI.toFloat() / 4f)
        val out2 = Vec3f(1f,1f,1f) // Non-zero initial value
        rotM.getTranslation(out2)
        assertVec3EqualsApproximately(Vec3f.zero(), out2, EPSILON,"getTranslation from rotation matrix")
    }

    @Test
    fun testGetScale() {
        val scaleVec = Vec3f(2f, 3f, 0.5f)
        val m = Mat4f.identity()
            .translate(Vec3f(1f,2f,3f))
            .rotateZ(PI.toFloat()/3f)
            .scale(scaleVec)


        val out = Vec3f()
        val result = m.getScaling(out)
        assertVec3EqualsApproximately(scaleVec, out, message="getScale basic", tolerance = 1e-5f) // Increased tolerance due to potential rotation influence
        assertSame(out, result, "getScale should return the destination vector")

        // Test with identity matrix (scale should be 1,1,1)
        val id = Mat4f.identity()
        val outId = Vec3f()
        id.getScaling(outId)
        assertVec3EqualsApproximately(Vec3f(1f,1f,1f), outId, message ="getScale from identity matrix")

        // Test with a matrix with negative scale
        // Note: getScale typically returns positive scales by taking the length of basis vectors.
        // If the implementation extracts scale in a way that preserves sign from diagonal elements
        // (assuming no shear and pure axis-aligned scaling after rotation), this might work.
        // However, general getScale from a transformed matrix is complex if shear is present or rotation is complex.
        // The current implementation seems to take the length of the basis vectors, so scale will be positive.
        val negScaleVec = Vec3f(-2f, 3f, -4f)
        val mNeg = Mat4f.scaling(negScaleVec)
        val outNeg = Vec3f()
        mNeg.getScaling(outNeg)
        assertVec3EqualsApproximately(Vec3f(2f, 3f, 4f), outNeg, message="getScale with negative scale factors (expects positive)")
    }


    @Test
    fun testEqualsApproximately() {
        val m1 = Mat4f.rowMajor(1f, 2f, 3f, 4f, 5f, 6f, 7f, 8f, 9f, 10f, 11f, 12f, 13f, 14f, 15f, 16f)
        val m2 = Mat4f.rowMajor(1f, 2f, 3f, 4f, 5f, 6f, 7f, 8f, 9f, 10f, 11f, 12f, 13f, 14f, 15f, 16f)
        val m3 = Mat4f.rowMajor(1.000001f, 2.000001f, 3f, 4f, 5f, 6f, 7f, 8f, 9f, 10f, 11f, 12f, 13f, 14f, 15f, 16f)
        val m4 = Mat4f.rowMajor(1.1f, 2f, 3f, 4f, 5f, 6f, 7f, 8f, 9f, 10f, 11f, 12f, 13f, 14f, 15f, 16f)

        assertTrue(m1.equalsApproximately(m2), "equalsApproximately self true")
        assertTrue(m1.equalsApproximately(m3, tolerance = 1e-5f), "equalsApproximately within tolerance true")
        assertFalse(m1.equalsApproximately(m3, tolerance = 1e-7f), "equalsApproximately outside tolerance false")
        assertFalse(m1.equalsApproximately(m4), "equalsApproximately different false")
        assertTrue(m1.equals(m2), "equals self true")
        assertFalse(m1.equals(m3), "equals different due to precision false")
    }

    @Test
    fun testClone() {
        val original = Mat4f.rowMajor(
            1f, 2f, 3f, 4f,
            5f, 6f, 7f, 8f,
            9f, 10f, 11f, 12f,
            13f, 14f, 15f, 16f
        )
        val cloned = original.clone()

        assertMat4Equals(original, cloned, "Cloned matrix should be equal to original")
        assertNotSame(original, cloned, "Cloned matrix should be a new instance")

        // Modify original and check clone is unaffected
        original.set(0,0, 99f)
        assertNotEquals(original.m00, cloned.m00, "Modifying original should not affect clone")
    }


    @Test
    fun testToFloatArray() {
        val m = Mat4f.rowMajor(
            1f, 2f, 3f, 4f,
            5f, 6f, 7f, 8f,
            9f, 10f, 11f, 12f,
            13f, 14f, 15f, 16f
        )
        // Expected is column-major
        val expectedArray = floatArrayOf(
            1f, 5f, 9f, 13f,  // Col 0
            2f, 6f, 10f, 14f, // Col 1
            3f, 7f, 11f, 15f, // Col 2
            4f, 8f, 12f, 16f  // Col 3
        )
        val actualArray = m.toFloatArray()
        assertContentEquals(expectedArray, actualArray, "toFloatArray basic")

        val dstArray = FloatArray(16)
        val resultDst = m.toFloatArray(dstArray)
        assertContentEquals(expectedArray, dstArray, "toFloatArray with destination array")
        assertSame(dstArray, resultDst, "toFloatArray with dst should return dst array")

    }

    @Test
    fun testGetSetElements() {
        val m = Mat4f.identity()
        m[0, 0] = 10f // col 0, row 0
        m[1, 0] = 20f // col 0, row 1
        m[0, 1] = 30f // col 1, row 0
        m[3, 3] = 40f // col 3, row 3

        assertEquals(10f, m[0, 0], "Get/Set m[0,0]")
        assertEquals(20f, m[1, 0], "Get/Set m[0,1]")
        assertEquals(30f, m[0, 1], "Get/Set m[1,0]")
        assertEquals(40f, m[3, 3], "Get/Set m[3,3]")
        assertEquals(1f, m[1,1], "Unchanged identity element m[1,1]") // From original identity

        // Check internal array values directly if possible, or via toFloatArray
        val expectedArray = floatArrayOf( // Column-major
            10f, 20f, 0f, 0f,  // Col 0
            30f, 1f, 0f, 0f,   // Col 1 (m[1,1] is 1f from identity)
            0f, 0f, 1f, 0f,    // Col 2
            0f, 0f, 0f, 40f    // Col 3
        )
        assertContentEquals(expectedArray, m.toFloatArray(), "Get/Set check via toFloatArray")

        // Test set individual mXY properties
        val mProps = Mat4f.identity()
        mProps.m00 = 1f; mProps.m01 = 2f; mProps.m02 = 3f; mProps.m03 = 4f;
        mProps.m10 = 5f; mProps.m11 = 6f; mProps.m12 = 7f; mProps.m13 = 8f;
        mProps.m20 = 9f; mProps.m21 = 10f; mProps.m22 = 11f; mProps.m23 = 12f;
        mProps.m30 = 13f; mProps.m31 = 14f; mProps.m32 = 15f; mProps.m33 = 16f;

        val expectedProps = Mat4f( // Constructor is column-major
            1f,2f,3f,4f, // m00, m01, m02, m03
            5f,6f,7f,8f, // m10, m11, m12, m13
            9f,10f,11f,12f, // m20, m21, m22, m23
            13f,14f,15f,16f // m30, m31, m32, m33
        )
        // The Mat4f constructor takes column-major arguments:
        // Mat4f(m00, m10, m20, m30, m01, m11, m21, m31, ...)
        // So the expectedProps should be defined column-wise based on mXY setters
        val expectedPropsColMajor = Mat4f(
            1f, 5f, 9f, 13f,    // Col 0: m00, m10, m20, m30
            2f, 6f, 10f, 14f,   // Col 1: m01, m11, m21, m31
            3f, 7f, 11f, 15f,   // Col 2: m02, m12, m22, m32
            4f, 8f, 12f, 16f    // Col 3: m03, m13, m23, m33
        )
        // Let's re-verify the mXY setters.
        // If m.m01 = value, it sets array[4] (start of 2nd column, 1st element).
        // If m.m10 = value, it sets array[1] (1st column, 2nd element).
        // So mProps is correct if its internal array matches expectedPropsColMajor's internal array.
        assertMat4Equals(expectedPropsColMajor, mProps, "Set mXY properties")

        // Test get mXY properties
        assertEquals(1f, mProps.m00)
        assertEquals(2f, mProps.m01) // This should be mProps.data[4]
        assertEquals(5f, mProps.m10) // This should be mProps.data[1]
        assertEquals(16f, mProps.m33)
    }
    // --- Pre-multiplication Tests ---

    @Test
    fun testPreTranslate() {
        val m = Mat4f.rowMajor(
            1f, 2f, 3f, 4f,
            5f, 6f, 7f, 8f,
            9f, 10f, 11f, 12f,
            13f, 14f, 15f, 16f
        )
        val v = Vec3f(10f, -5f, 2f)
        val translationMatrix = Mat4f.translation(v)
        val expected = translationMatrix.multiply(m)

        // Test without dst
        var actual = m.preTranslate(v)
        assertMat4EqualsApproximately(expected, actual, "preTranslate basic")

        // Test with dst
        val dst = Mat4f()
        actual = m.preTranslate(v, dst)
        assertMat4EqualsApproximately(expected, dst, "preTranslate with dst")
        assertSame(dst, actual, "preTranslate with dst should return dst")

        // Test with dst == this
        val mSelf = m.copy()
        actual = mSelf.preTranslate(v, mSelf)
        assertMat4EqualsApproximately(expected, mSelf, "preTranslate with dst == this")
        assertSame(mSelf, actual, "preTranslate with dst == this should return self")

        // Test with identity matrix
        val id = Mat4f.identity()
        val expectedId = Mat4f.translation(v)
        actual = id.preTranslate(v)
        assertMat4EqualsApproximately(expectedId, actual, "preTranslate on identity")
    }

    @Test
    fun testPreRotateX() {
        val m = Mat4f.rowMajor(
            1f, 2f, 3f, 4f,
            5f, 6f, 7f, 8f,
            9f, 10f, 11f, 12f,
            0f, 0f, 0f, 1f // Make it a typical transform matrix
        )
        val angle = PI.toFloat() / 3f // 60 degrees
        val rotationMatrix = Mat4f.rotationX(angle)
        val expected = rotationMatrix.multiply(m)

        var actual = m.preRotateX(angle)
        assertMat4EqualsApproximately(expected, actual, "preRotateX basic")

        val dst = Mat4f()
        actual = m.preRotateX(angle, dst)
        assertMat4EqualsApproximately(expected, dst, "preRotateX with dst")
        assertSame(dst, actual, "preRotateX with dst should return dst")

        val mSelf = m.copy()
        actual = mSelf.preRotateX(angle, mSelf)
        assertMat4EqualsApproximately(expected, mSelf, "preRotateX with dst == this")
        assertSame(mSelf, actual, "preRotateX with dst == this should return self")
    }

    @Test
    fun testPreRotateY() {
        val m = Mat4f.rowMajor(
            1f, 2f, 3f, 4f,
            5f, 6f, 7f, 8f,
            9f, 10f, 11f, 12f,
            0f, 0f, 0f, 1f
        )
        val angle = PI.toFloat() / 4f // 45 degrees
        val rotationMatrix = Mat4f.rotationY(angle)
        val expected = rotationMatrix.multiply(m)

        var actual = m.preRotateY(angle)
        assertMat4EqualsApproximately(expected, actual, "preRotateY basic")

        val dst = Mat4f()
        actual = m.preRotateY(angle, dst)
        assertMat4EqualsApproximately(expected, dst, "preRotateY with dst")
        assertSame(dst, actual, "preRotateY with dst should return dst")

        val mSelf = m.copy()
        actual = mSelf.preRotateY(angle, mSelf)
        assertMat4EqualsApproximately(expected, mSelf, "preRotateY with dst == this")
        assertSame(mSelf, actual, "preRotateY with dst == this should return self")
    }

    @Test
    fun testPreRotateZ() {
        val m = Mat4f.rowMajor(
            1f, 2f, 3f, 4f,
            5f, 6f, 7f, 8f,
            9f, 10f, 11f, 12f,
            0f, 0f, 0f, 1f
        )
        val angle = PI.toFloat() / 6f // 30 degrees
        val rotationMatrix = Mat4f.rotationZ(angle)
        val expected = rotationMatrix.multiply(m)

        var actual = m.preRotateZ(angle)
        assertMat4EqualsApproximately(expected, actual, "preRotateZ basic")

        val dst = Mat4f()
        actual = m.preRotateZ(angle, dst)
        assertMat4EqualsApproximately(expected, dst, "preRotateZ with dst")
        assertSame(dst, actual, "preRotateZ with dst should return dst")

        val mSelf = m.copy()
        actual = mSelf.preRotateZ(angle, mSelf)
        assertMat4EqualsApproximately(expected, mSelf, "preRotateZ with dst == this")
        assertSame(mSelf, actual, "preRotateZ with dst == this should return self")
    }

    @Test
    fun testPreAxisRotate() {
        val m = Mat4f.rowMajor(
            1f, 2f, 3f, 4f,
            5f, 6f, 7f, 8f,
            9f, 10f, 11f, 12f,
            0f, 0f, 0f, 1f
        )
        val angle = PI.toFloat() / 5f
        val axis = Vec3f(1f, 2f, 3f).normalize()
        val rotationMatrix = Mat4f.axisRotation(axis, angle)
        val expected = rotationMatrix.multiply(m)

        var actual = m.preRotate(axis, angle)
        assertMat4EqualsApproximately(expected, actual, "preAxisRotate basic", tolerance = 1e-6f)

        val dst = Mat4f()
        actual = m.preRotate(axis, angle, dst)
        assertMat4EqualsApproximately(expected, dst, "preAxisRotate with dst", tolerance = 1e-6f)
        assertSame(dst, actual, "preAxisRotate with dst should return dst")

        val mSelf = m.copy()
        actual = mSelf.preRotate(axis, angle, mSelf)
        assertMat4EqualsApproximately(expected, mSelf, "preAxisRotate with dst == this", tolerance = 1e-6f)
        assertSame(mSelf, actual, "preAxisRotate with dst == this should return self")
    }


    @Test
    fun testPreScale() {
        val m = Mat4f.rowMajor(
            1f, 2f, 3f, 4f,
            5f, 6f, 7f, 8f,
            9f, 10f, 11f, 12f,
            13f, 14f, 15f, 16f
        )
        val v = Vec3f(2f, -0.5f, 3f)
        val scalingMatrix = Mat4f.scaling(v)
        val expected = scalingMatrix.multiply(m)

        var actual = m.preScale(v)
        assertMat4EqualsApproximately(expected, actual, "preScale basic")

        val dst = Mat4f()
        actual = m.preScale(v, dst)
        assertMat4EqualsApproximately(expected, dst, "preScale with dst")
        assertSame(dst, actual, "preScale with dst should return dst")

        val mSelf = m.copy()
        actual = mSelf.preScale(v, mSelf)
        assertMat4EqualsApproximately(expected, mSelf, "preScale with dst == this")
        assertSame(mSelf, actual, "preScale with dst == this should return self")
    }

    @Test
    fun testPreUniformScale() {
        val m = Mat4f.rowMajor(
            1f, 2f, 3f, 4f,
            5f, 6f, 7f, 8f,
            9f, 10f, 11f, 12f,
            13f, 14f, 15f, 16f
        )
        val s = 2.5f
        val scalingMatrix = Mat4f.uniformScaling(s)
        val expected = scalingMatrix.multiply(m)

        var actual = m.preUniformScale(s)
        assertMat4EqualsApproximately(expected, actual, "preUniformScale basic")

        val dst = Mat4f()
        actual = m.preUniformScale(s, dst)
        assertMat4EqualsApproximately(expected, dst, "preUniformScale with dst")
        assertSame(dst, actual, "preUniformScale with dst should return dst")

        val mSelf = m.copy()
        actual = mSelf.preUniformScale(s, mSelf)
        assertMat4EqualsApproximately(expected, mSelf, "preUniformScale with dst == this")
        assertSame(mSelf, actual, "preUniformScale with dst == this should return self")
    }

    @Test
    fun testMultiplyVector() {
        // Test with identity matrix
        val identity = Mat4f.identity()
        val v1 = Vec4f(1f, 2f, 3f, 1f)
        assertVec4EqualsApproximately(v1, identity.multiplyVector(v1), message = "Identity matrix should not change vector")

        // Test with translation matrix
        val translation = Mat4f.translation(Vec3f(10f, 20f, 30f))
        val v2 = Vec4f(1f, 2f, 3f, 1f)
        assertVec4EqualsApproximately(Vec4f(11f, 22f, 33f, 1f), translation.multiplyVector(v2), 
            message = "Translation should add to position components when w=1")

        // Test with scaling matrix
        val scale = Mat4f.scaling(Vec3f(2f, 3f, 4f))
        val v3 = Vec4f(1f, 1f, 1f, 1f)
        assertVec4EqualsApproximately(Vec4f(2f, 3f, 4f, 1f), scale.multiplyVector(v3), 
            message = "Scaling should multiply position components")

        // Test with rotation matrix
        val rotX = Mat4f.rotationX(PI.toFloat() / 2f) // 90 degrees around X
        val v4 = Vec4f(0f, 1f, 0f, 1f)
        assertVec4EqualsApproximately(Vec4f(0f, 0f, 1f, 1f), rotX.multiplyVector(v4), tolerance = 0.0001f,
            message = "Rotation around X by 90 degrees should transform (0,1,0,1) to (0,0,1,1)")

        // Test with custom matrix
        val custom = Mat4f(
            1f, 2f, 3f, 4f,
            5f, 6f, 7f, 8f,
            9f, 10f, 11f, 12f,
            13f, 14f, 15f, 16f
        )
        val v5 = Vec4f(2f, 3f, 4f, 1f)
        // Expected: (1*2 + 5*3 + 9*4 + 13*1, 2*2 + 6*3 + 10*4 + 14*1, 3*2 + 7*3 + 11*4 + 15*1, 4*2 + 8*3 + 12*4 + 16*1)
        // = (2 + 15 + 36 + 13, 4 + 18 + 40 + 14, 6 + 21 + 44 + 15, 8 + 24 + 48 + 16) = (66, 76, 86, 96)
        assertVec4EqualsApproximately(Vec4f(66f, 76f, 86f, 96f), custom.multiplyVector(v5), 
            message = "Custom matrix multiplication should work correctly")

        // Test with destination vector
        val dst = Vec4f()
        val result = rotX.multiplyVector(v4, dst)
        assertVec4EqualsApproximately(Vec4f(0f, 0f, 1f, 1f), dst, tolerance = 0.0001f, 
            message = "Multiplication with destination should store result in destination")
        assertSame(dst, result, "Multiplication with destination should return destination")

        // Test operator overloading
        val opResult = rotX * v4
        assertVec4EqualsApproximately(Vec4f(0f, 0f, 1f, 1f), opResult, tolerance = 0.0001f, 
            message = "Operator * should work the same as multiplyVector")

        // Test with w=0 (direction vector)
        val v6 = Vec4f(1f, 0f, 0f, 0f)
        assertVec4EqualsApproximately(Vec4f(0f, 1f, 0f, 0f), Mat4f.rotationZ(PI.toFloat() / 2f).multiplyVector(v6), 
            tolerance = 0.0001f, message = "Direction vectors (w=0) should not be affected by translation")
    }

    /**
     * The "known correct" way to build the matrix using existing functions.
     * The standard transformation order is Scale -> Rotate -> Translate.
     * This means the matrix multiplication order is T * R * S.
     */
    private fun Mat4f.translateRotateScaleCorrectly(translate: Vec3f, rotate: Quatf, scale: Vec4f): Mat4f {
        val scaleMatrix = Mat4f.scaling(scale.x, scale.y, scale.z)
        val rotationMatrix = Mat4f.fromQuat(rotate)
        val translationMatrix = Mat4f.translation(translate)

        // M = T * R * S
        return translationMatrix.multiply(rotationMatrix).multiply(scaleMatrix)
    }

    @Test
    fun `test with identity transformations`() {
        // Arrange
        val translate = Vec3f(0f, 0f, 0f)
        val rotate = Quatf(0f, 0f, 0f, 1f) // Identity quaternion
        val scale = Vec3f(1f, 1f, 1f)
        val expectedMatrix = Mat4f.identity()

        // Act
        val resultMatrix = Mat4f.translateRotateScale(translate, rotate, scale)

        // Assert
        assertTrue(
            expectedMatrix.equalsApproximately(resultMatrix),
            "With identity transformations, the result should be an identity matrix.\nExpected:\n$expectedMatrix\nGot:\n$resultMatrix"
        )
    }

    @Test
    fun `test with translation only`() {
        // Arrange
        val translate = Vec3f(10f, -5f, 20f)
        val rotate = Quatf(0f, 0f, 0f, 1f)
        val scale = Vec3f(1f, 1f, 1f)
        val expectedMatrix = Mat4f.translation(translate)

        // Act
        val resultMatrix = Mat4f.translateRotateScale(translate, rotate, scale)

        // Assert
        assertTrue(
            expectedMatrix.equalsApproximately(resultMatrix),
            "With only translation, the result should be a pure translation matrix.\nExpected:\n$expectedMatrix\nGot:\n$resultMatrix"
        )
    }

    @Test
    fun `test with scale only`() {
        // Arrange
        val translate = Vec3f(0f, 0f, 0f)
        val rotate = Quatf(0f, 0f, 0f, 1f)
        val scale = Vec3f(2f, 0.5f, 10f)
        val expectedMatrix = Mat4f.scaling(scale.x, scale.y, scale.z)

        // Act
        val resultMatrix = Mat4f.translateRotateScale(translate, rotate, scale)

        // Assert
        assertTrue(
            expectedMatrix.equalsApproximately(resultMatrix),
            "With only scaling, the result should be a pure scaling matrix.\nExpected:\n$expectedMatrix\nGot:\n$resultMatrix"
        )
    }

    /**
     * The "known correct" way to build the matrix using existing functions.
     * The standard transformation order is Scale -> Rotate -> Translate.
     * This means the matrix multiplication order is T * R * S.
     */
    private fun buildMatrixCorrectly(translate: Vec3f, rotate: Quatf, scale: Vec3f): Mat4f {
        val scaleMatrix = Mat4f.scaling(scale.x, scale.y, scale.z)
        val rotationMatrix = Mat4f.fromQuat(rotate)
        val translationMatrix = Mat4f.translation(translate)

        // M = T * R * S
        return translationMatrix.multiply(rotationMatrix).multiply(scaleMatrix)
    }

    @Test
    fun `test with rotation only`() {
        // Arrange
        val translate = Vec3f(0f, 0f, 0f)
        // 90 degrees rotation around Y axis
        val rotate = Quatf.fromAxisAngle(Vec3f(0f, 1f, 0f), degToRad(90.0f))
        val scale = Vec3f(1f, 1f, 1f)
        val expectedMatrix = Mat4f.fromQuat(rotate)

        // Act
        val resultMatrix = Mat4f.translateRotateScale(translate, rotate, scale)

        // Assert
        assertTrue(
            expectedMatrix.equalsApproximately(resultMatrix),
            "With only rotation, the result should be a pure rotation matrix.\nExpected:\n$expectedMatrix\nGot:\n$resultMatrix"
        )
    }

    @Test
    fun `test with complex combination of all transformations`() {
        // Arrange
        val translate = Vec3f(100f, -50f, 25f)
        val scale = Vec3f(2f, 0.5f, 1.5f)

        // 45 degrees rotation around a non-cardinal axis (1, 1, 1)
        val rotationAxis = Vec3f(1f, 1f, 1f).normalize()
        val rotationAngle = radToDeg(45.0f)
        val rotate = Quatf.fromAxisAngle(rotationAxis, rotationAngle)

        // Calculate expected result using the trusted, sequential method
        val expectedMatrix = buildMatrixCorrectly(translate, rotate, scale)

        // Act
        // Calculate result using the new, optimized function
        val resultMatrix = Mat4f.translateRotateScale(translate, rotate, scale)

        // Assert
        assertTrue(
            expectedMatrix.equalsApproximately(resultMatrix, 1e-5f),
            "The optimized function should produce the same result as the sequential multiplication.\nExpected:\n$expectedMatrix\nGot:\n$resultMatrix"
        )
    }

    @Test
    fun `test with another complex combination`() {
        // Arrange
        val translate = Vec3f(-1.2f, 0f, 8.8f)
        val scale = Vec3f(1f, 1f, 5f)

        // -30 degrees rotation around X axis
        val rotationAxis = Vec3f(1f, 0f, 0f)
        val rotationAngle = radToDeg(-30.0f)
        val rotate = Quatf.fromAxisAngle(rotationAxis, rotationAngle)

        val expectedMatrix = buildMatrixCorrectly(translate, rotate, scale)

        // Act
        val resultMatrix = Mat4f.translateRotateScale(translate, rotate, scale)

        // Assert
        assertTrue(
            expectedMatrix.equalsApproximately(resultMatrix, 1e-5f),
            "The optimized function should produce the same result for a different complex case.\nExpected:\n$expectedMatrix\nGot:\n$resultMatrix"
        )
    }
}

// Helper for Quatf comparison (add if not present elsewhere)
internal fun assertQuatEqualsApproximately(expected: Quatf, actual: Quatf, message: String? = null, tolerance: Float = EPSILON) {
    assertTrue(
        abs(expected.x - actual.x) < tolerance &&
        abs(expected.y - actual.y) < tolerance &&
        abs(expected.z - actual.z) < tolerance &&
        abs(expected.w - actual.w) < tolerance,
        "$message: Expected quaternion <$expected> but got <$actual>"
    )
    // Also check for q and -q equivalence if necessary, though normalize should handle one form.
    val actualNeg = Quatf(-actual.x, -actual.y, -actual.z, -actual.w)
    assertTrue(
        (abs(expected.x - actual.x) < tolerance && abs(expected.y - actual.y) < tolerance && abs(expected.z - actual.z) < tolerance && abs(expected.w - actual.w) < tolerance) ||
        (abs(expected.x - actualNeg.x) < tolerance && abs(expected.y - actualNeg.y) < tolerance && abs(expected.z - actualNeg.z) < tolerance && abs(expected.w - actualNeg.w) < tolerance),
        "$message: Expected quaternion <$expected> or its negative, but got <$actual>"
    )
}
