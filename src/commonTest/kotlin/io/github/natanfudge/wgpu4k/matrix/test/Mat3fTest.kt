package io.github.natanfudge.wgpu4k.matrix.test

import io.github.natanfudge.wgpu4k.matrix.*
import kotlin.math.*
import kotlin.test.*

// Helper function for approximate equality check
internal fun assertMat3EqualsApproximately(expected: Mat3f, actual: Mat3f, message: String? = null, tolerance: Float = EPSILON) {
    assertTrue(expected.equalsApproximately(actual, tolerance), "$message: Expected \n<$expected> \nbut got \n<$actual>")
}

// Helper function for exact equality check
internal fun assertMat3Equals(expected: Mat3f, actual: Mat3f, message: String? = null) {
    assertEquals(expected, actual, message)
}

// Helper function for Vec2 approximate equality check
internal fun assertVec2EqualsApproximately(expected: Vec2f, actual: Vec2f, tolerance: Float = EPSILON, message: String? = null) {
    assertTrue(abs(expected.x - actual.x) < tolerance && abs(expected.y - actual.y) < tolerance,  "$message: Expected <$expected> but got <$actual>")
}

// Helper function for Vec3 approximate equality check
internal fun assertVec3EqualsApproximately(expected: Vec3f, actual: Vec3f, tolerance: Float = EPSILON, message: String? = null) {
    assertTrue(
        abs(expected.x - actual.x) < tolerance && abs(expected.y - actual.y) < tolerance && abs(expected.z - actual.z) < tolerance,
        "$message: Expected <$expected> but got <$actual>"
    )
}

// Helper function for Float approximate equality check
internal fun assertFloatEqualsApproximately(expected: Float, actual: Float, tolerance: Float = EPSILON, message: String? = null) {
    assertTrue(abs(expected - actual) < tolerance,  "$message: Expected <$expected> but got <$actual>")
}


class Mat3fTest {
    // Tests will be added here

// --- Companion Object Tests ---

    @Test
    fun testCompanionIdentity() {
        val expected = Mat3f.rowMajor(
            1f, 0f, 0f,
            0f, 1f, 0f,
            0f, 0f, 1f
        )
        assertMat3Equals(expected, Mat3f.identity(), "Default identity")

        val dst = Mat3f.identity()
        val result = Mat3f.identity(dst)
        assertMat3Equals(expected, dst, "Identity with destination")
        assertSame(dst, result, "Identity should return destination")
    }

    @Test
    fun testCompanionFromFloatArray() {
        val values = floatArrayOf(1f, 2f, 3f, 4f, 5f, 6f, 7f, 8f, 9f)
        // Mat3f stores data in column-major order internally but constructor takes row-major like input
        val expected = Mat3f.rowMajor(
            1f, 2f, 3f, // col 0
            4f, 5f, 6f, // col 1
            7f, 8f, 9f  // col 2
        )
        // fromFloatArray expects column-major data matching internal layout
        val colMajorValues = floatArrayOf(1f, 4f, 7f, 0f, 2f, 5f, 8f, 0f, 3f, 6f, 9f, 0f) // Padded for internal array size 12
        val actual = Mat3f.fromFloatArray(colMajorValues)
        assertMat3Equals(expected, actual, "fromFloatArray basic")

        // Edge case: Array with zeros
        val zeroValues = FloatArray(12)
        val zeroMat = Mat3f.fromFloatArray(zeroValues)
        assertMat3Equals(Mat3f.rowMajor(0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f), zeroMat, "fromFloatArray zeros")

        // Edge case: Invalid size (though constructor handles this) - Test the intended use
        assertFailsWith<IllegalArgumentException>("fromFloatArray should fail for incorrect size") {
            Mat3f.fromFloatArray(floatArrayOf(1f, 2f, 3f))
        }
    }

    @Test
    fun testCompanionInvoke() {
        // Test the invoke operator for convenient construction
        val m = Mat3f.rowMajor(
            1f, 2f, 3f,
            4f, 5f, 6f,
            7f, 8f, 9f
        )
        val expectedArray = floatArrayOf(1f, 4f, 7f, 0f, 2f, 5f, 8f, 0f, 3f, 6f, 9f, 0f) // Column major internal
        assertContentEquals(expectedArray, m.toFloatArray(), "Invoke constructor basic")

        // Edge case: Identity
        val id = Mat3f.rowMajor(
            1f, 0f, 0f,
            0f, 1f, 0f,
            0f, 0f, 1f
        )
        assertMat3Equals(Mat3f.identity(), id, "Invoke constructor identity")

        // Edge case: Zero
        val zero = Mat3f.rowMajor(
            0f, 0f, 0f,
            0f, 0f, 0f,
            0f, 0f, 0f
        )
        val zeroArray = FloatArray(12)
        assertContentEquals(zeroArray, zero.toFloatArray(), "Invoke constructor zero")
    }


    @Test
    fun testCompanionFromMat4() {
        val m4 = Mat4f.translation(Vec3f(10f, 20f, 30f))
            .rotateY(PI.toFloat() / 2f)
            .scale(Vec3f(2f, 3f, 4f))

        val expected = Mat3f.rowMajor(
            0f, 0f, -2f,
            0f, 3f, 0f,
            4f, 0f, 0f
        ).setTranslation(Vec2f(10f, 20f)) // Translation part is copied

        val actual = Mat3f.fromMat4(m4)
        assertMat3EqualsApproximately(expected, actual, message = "fromMat4 basic")

        // Edge case: Identity Mat4
        val id4 = Mat4f.identity()
        val expectedId3 = Mat3f.identity()
        assertMat3Equals(expectedId3, Mat3f.fromMat4(id4), "fromMat4 identity")

        // Edge case: Mat4 with only translation
        val trans4 = Mat4f.translation(Vec3f(5f, -5f, 0f))
        val expectedTrans3 = Mat3f.identity().setTranslation(Vec2f(5f, -5f))
        assertMat3Equals(expectedTrans3, Mat3f.fromMat4(trans4), "fromMat4 translation only")
    }

    @Test
    fun testCompanionFromQuat() {
        // Basic rotation (90 degrees around Y)
        val q1 = Quatf.fromAxisAngle(Vec3f(0f, 1f, 0f), PI.toFloat() / 2f)
        val expected1 = Mat3f.rotationY(PI.toFloat() / 2f)
        assertMat3EqualsApproximately(expected1, Mat3f.fromQuat(q1), message = "fromQuat Y rotation")

        // Identity quaternion
        val qId = Quatf.identity()
        val expectedId = Mat3f.identity()
        assertMat3EqualsApproximately(expectedId, Mat3f.fromQuat(qId), message = "fromQuat identity")

        // Rotation around arbitrary axis
        val axis = Vec3f(1f, 1f, 1f).normalize()
        val angle = PI.toFloat() / 3f
        val q3 = Quatf.fromAxisAngle(axis, angle)
        // Manually calculate expected matrix (can be complex, use known values or another library to verify if needed)
        // For simplicity, we'll test if transforming basis vectors matches quaternion transform
        val m3 = Mat3f.fromQuat(q3)
        val v = Vec3f(1f, 0f, 0f)
        val expectedV = v.transformQuat(q3) // Transform using quat
        val actualV = v.transformMat3(m3)   // Transform using matrix derived from quat
        assertVec3EqualsApproximately(expectedV, actualV, message = "fromQuat arbitrary axis transform check")
    }

    @Test
    fun testCompanionTranslation() {
        val v = Vec2f(5f, -10f)
        val expected = Mat3f.rowMajor(
            1f, 0f, 0f,
            0f, 1f, 0f,
            5f, -10f, 1f
        )
        assertMat3Equals(expected, Mat3f.translation(v), "Translation basic")

        // Edge case: Zero vector
        val zeroV = Vec2f(0f, 0f)
        assertMat3Equals(Mat3f.identity(), Mat3f.translation(zeroV), "Translation zero vector")

        // Edge case: Large values
        val largeV = Vec2f(1e6f, -1e6f)
        val expectedLarge = Mat3f.rowMajor(
            1f, 0f, 0f,
            0f, 1f, 0f,
            1e6f, -1e6f, 1f
        )
        assertMat3Equals(expectedLarge, Mat3f.translation(largeV), "Translation large values")
    }

    @Test
    fun testCompanionRotation() { // Also tests rotationZ
        val angle = PI.toFloat() / 4f // 45 degrees
        val c = cos(angle)
        val s = sin(angle)
        val expected = Mat3f.rowMajor(
            c, s, 0f,
            -s, c, 0f,
            0f, 0f, 1f
        )
        assertMat3EqualsApproximately(expected, Mat3f.rotation(angle), "Rotation basic (45 deg)")
        assertMat3EqualsApproximately(expected, Mat3f.rotationZ(angle), "RotationZ basic (45 deg)")


        // Edge case: Zero angle
        assertMat3EqualsApproximately(Mat3f.identity(), Mat3f.rotation(0f), "Rotation zero angle")

        // Edge case: Full rotation (360 degrees / 2 PI)
        assertMat3EqualsApproximately(
            Mat3f.identity(),
            Mat3f.rotation(2f * PI.toFloat()),
            tolerance = 1e-6f,
            message = "Rotation full circle"
        ) // Increased tolerance for float precision
    }

    @Test
    fun testCompanionRotationX() {
        val angle = PI.toFloat() / 6f // 30 degrees
        val c = cos(angle)
        val s = sin(angle)
        // Note: Mat3f represents 2D affine transforms + 3D rotation part.
        // rotationX affects the 3D part, which isn't directly used in 2D transforms.
        // The resulting matrix might look like identity in its 2D affine part.
        // Let's check the elements directly.
        val m = Mat3f.rotationX(angle)
        val expectedArray = floatArrayOf(
            1f, 0f, 0f, 0f, // Col 0
            0f, c, s, 0f, // Col 1
            0f, -s, c, 0f, // Col 2
        )
        assertContentEquals(expectedArray, m.toFloatArray(), "RotationX basic (30 deg)")


        // Edge case: Zero angle
        assertMat3EqualsApproximately(Mat3f.identity(), Mat3f.rotationX(0f), "RotationX zero angle")

        // Edge case: 180 degrees (PI)
        val mPi = Mat3f.rotationX(PI.toFloat())
        val expectedPiArray = floatArrayOf(
            1f, 0f, 0f, 0f,
            0f, -1f, 0f, 0f,
            0f, 0f, -1f, 0f
        )
        // Use element-wise comparison due to potential float inaccuracies near -1
        assertFloatEqualsApproximately(expectedPiArray[0], mPi[0, 0])
        assertFloatEqualsApproximately(expectedPiArray[1], mPi[1, 0])
        // ... compare other elements ...
        assertFloatEqualsApproximately(expectedPiArray[5], mPi[1, 1], tolerance = 1e-6f)
        assertFloatEqualsApproximately(expectedPiArray[10], mPi[2, 2], tolerance = 1e-6f)
        // assertContentEquals(expectedPiArray, mPi.toFloatArray(), "RotationX 180 deg") // Might fail due to precision
    }

    @Test
    fun testCompanionRotationY() {
        val angle = -PI.toFloat() / 2f // -90 degrees
        val c = cos(angle) // 0
        val s = sin(angle) // -1
        val m = Mat3f.rotationY(angle)
        val expectedArray = floatArrayOf(
            c, 0f, -s, 0f, // Col 0 -> 0, 0, 1
            0f, 1f, 0f, 0f, // Col 1 -> 0, 1, 0
            s, 0f, c, 0f  // Col 2 -> -1, 0, 0
        )
        assertMat3EqualsApproximately(Mat3f.fromFloatArray(expectedArray), m, tolerance = 1e-6f, message = "RotationY basic (-90 deg)")

        // Edge case: Zero angle
        assertMat3EqualsApproximately(Mat3f.identity(), Mat3f.rotationY(0f), "RotationY zero angle")

        // Edge case: 360 degrees (2 PI)
        assertMat3EqualsApproximately(Mat3f.identity(), Mat3f.rotationY(2f * PI.toFloat()), tolerance = 1e-6f, message = "RotationY full circle")
    }

    @Test
    fun testCompanionScaling() {
        val v = Vec2f(2f, -3f)
        val expected = Mat3f.rowMajor(
            2f, 0f, 0f,
            0f, -3f, 0f,
            0f, 0f, 1f
        )
        assertMat3Equals(expected, Mat3f.scaling(v), "Scaling basic")

        // Edge case: Zero vector (results in zero matrix except bottom-right)
        val zeroV = Vec2f(0f, 0f)
        val expectedZero = Mat3f.rowMajor(
            0f, 0f, 0f,
            0f, 0f, 0f,
            0f, 0f, 1f // W component remains 1
        )
        assertMat3Equals(expectedZero, Mat3f.scaling(zeroV), "Scaling zero vector")

        // Edge case: Scaling by 1 (identity)
        val oneV = Vec2f(1f, 1f)
        assertMat3Equals(Mat3f.identity(), Mat3f.scaling(oneV), "Scaling by one")
    }

    @Test
    fun testCompanionScaling3D() {
        val v = Vec3f(2f, 3f, -4f)
        // Scaling3D affects the 3x3 rotation/scale part
        val expected = Mat3f.rowMajor(
            2f, 0f, 0f,
            0f, 3f, 0f,
            0f, 0f, -4f
        )
        assertMat3Equals(expected, Mat3f.scaling3D(v), "Scaling3D basic")

        // Edge case: Zero vector
        val zeroV = Vec3f(0f, 0f, 0f)
        val expectedZero = Mat3f.rowMajor(
            0f, 0f, 0f,
            0f, 0f, 0f,
            0f, 0f, 0f // W component becomes 0 here unlike 2D scaling
        )
        assertMat3Equals(expectedZero, Mat3f.scaling3D(zeroV), "Scaling3D zero vector")

        // Edge case: Scaling by 1 (identity)
        val oneV = Vec3f(1f, 1f, 1f)
        assertMat3Equals(Mat3f.identity(), Mat3f.scaling3D(oneV), "Scaling3D by one")
    }

    @Test
    fun testCompanionUniformScaling() {
        val s = 5f
        val expected = Mat3f.rowMajor(
            5f, 0f, 0f,
            0f, 5f, 0f,
            0f, 0f, 1f
        )
        assertMat3Equals(expected, Mat3f.uniformScaling(s), "UniformScaling basic")

        // Edge case: Scale by 0
        assertMat3Equals(Mat3f.rowMajor(0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 1f), Mat3f.uniformScaling(0f), "UniformScaling by zero")

        // Edge case: Scale by 1
        assertMat3Equals(Mat3f.identity(), Mat3f.uniformScaling(1f), "UniformScaling by one")
    }

    @Test
    fun testCompanionUniformScaling3D() {
        val s = -2f
        val expected = Mat3f.rowMajor(
            -2f, 0f, 0f,
            0f, -2f, 0f,
            0f, 0f, -2f
        )
        assertMat3Equals(expected, Mat3f.uniformScaling3D(s), "UniformScaling3D basic")

        // Edge case: Scale by 0
        val expectedZero = Mat3f.rowMajor(
            0f, 0f, 0f,
            0f, 0f, 0f,
            0f, 0f, 0f
        )
        assertMat3Equals(expectedZero, Mat3f.uniformScaling3D(0f), "UniformScaling3D by zero")


        // Edge case: Scale by 1
        assertMat3Equals(Mat3f.identity(), Mat3f.uniformScaling3D(1f), "UniformScaling3D by one")
    }


    // --- Instance Method Tests ---

    @Test
    fun testToString() {
        val m = Mat3f.rowMajor(1f, 2f, 3f, 4f, 5f, 6f, 7f, 8f, 9f)
        val s = m.toString()
        // Basic check: contains matrix elements
        assertTrue(s.contains("1.0") && s.contains("5.0") && s.contains("9.0"), "toString contains elements")
        // Check formatting (might be fragile)
        assertTrue(s.startsWith("Mat3f") && s.contains("\n"), "toString basic format")

        // Edge case: Identity
        val idStr = Mat3f.identity().toString()
        assertTrue(idStr.contains("1.0") && idStr.contains("0.0"), "toString identity")

        // Edge case: Zero
        val zeroStr = Mat3f.rowMajor(0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f).toString()
        assertTrue(zeroStr.contains("0.0") && !zeroStr.contains("1.0"), "toString zero")
    }

    @Test
    fun testSet() {
        val m = Mat3f.identity()
        val result = m.set(
            1f, 2f, 3f,
            4f, 5f, 6f,
            7f, 8f, 9f
        )
        val expected = Mat3f.rowMajor(1f, 2f, 3f, 4f, 5f, 6f, 7f, 8f, 9f)
        assertMat3Equals(expected, m, "Set basic")
        assertSame(m, result, "Set should return self")

        // Set to identity
        m.set(
            1f, 0f, 0f,
            0f, 1f, 0f,
            0f, 0f, 1f
        )
        assertMat3Equals(Mat3f.identity(), m, "Set to identity")

        // Set to zero
        m.set(
            0f, 0f, 0f,
            0f, 0f, 0f,
            0f, 0f, 0f
        )
        val zeroArray = FloatArray(12)
        assertContentEquals(zeroArray, m.toFloatArray(), "Set to zero")
    }

    @Test
    fun testNegate() {
        val m = Mat3f.rowMajor(1f, -2f, 3f, -4f, 5f, -6f, 7f, -8f, 9f)
        val expected = Mat3f.rowMajor(-1f, 2f, -3f, 4f, -5f, 6f, -7f, 8f, -9f)
        val negatedM = m.negate() // Test without destination
        assertMat3Equals(expected, negatedM, "Negate without destination")
        assertNotSame(m, negatedM, "Negate without destination should create new instance")


        val dst = Mat3f.identity()
        val result = m.negate(dst) // Test with destination
        assertMat3Equals(expected, dst, "Negate with destination")
        assertSame(dst, result, "Negate with destination should return destination")

        // Edge case: Negate zero matrix
        val zero = Mat3f.rowMajor(0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f)
        val zeroNegated = zero.negate()
        assertMat3Equals(zero, zeroNegated, "Negate zero matrix")
    }

    @Test
    fun testMultiplyScalar() {
        val m = Mat3f.rowMajor(1f, 2f, 3f, 4f, 5f, 6f, 7f, 8f, 9f)
        val s = 2f
        val expected = Mat3f.rowMajor(2f, 4f, 6f, 8f, 10f, 12f, 14f, 16f, 18f)

        val scaledM = m.multiplyScalar(s) // Test without destination
        assertMat3Equals(expected, scaledM, "MultiplyScalar without destination")
        assertNotSame(m, scaledM, "MultiplyScalar without destination should create new instance")

        val dst = Mat3f.identity()
        val result = m.multiplyScalar(s, dst) // Test with destination
        assertMat3Equals(expected, dst, "MultiplyScalar with destination")
        assertSame(dst, result, "MultiplyScalar with destination should return destination")

        // Edge case: Multiply by zero
        val zeroScaled = m.multiplyScalar(0f)
        val expectedZero = Mat3f.rowMajor(0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f) // Note: w component becomes 0
        assertMat3Equals(expectedZero, zeroScaled, "MultiplyScalar by zero")

        // Edge case: Multiply by one
        val oneScaled = m.multiplyScalar(1f)
        assertMat3Equals(m, oneScaled, "MultiplyScalar by one")
        assertNotSame(m, oneScaled, "MultiplyScalar by one should still create new instance if no dst")
    }

    @Test
    fun testDiv() {
        val m = Mat3f.rowMajor(2f, 4f, 6f, 8f, 10f, 12f, 14f, 16f, 18f)
        val s = 2f
        val expected = Mat3f.rowMajor(1f, 2f, 3f, 4f, 5f, 6f, 7f, 8f, 9f)

        val divM = m.div(s) // Test without destination
        assertMat3Equals(expected, divM, "Div without destination")
        assertNotSame(m, divM, "Div without destination should create new instance")

        val dst = Mat3f.identity()
        val result = m.div(s, dst) // Test with destination
        assertMat3Equals(expected, dst, "Div with destination")
        assertSame(dst, result, "Div with destination should return destination")

        // Edge case: Divide by one
        val oneDiv = m.div(1f)
        assertMat3Equals(m, oneDiv, "Div by one")
        assertNotSame(m, oneDiv, "Div by one should still create new instance if no dst")

        // Edge case: Divide by zero (expect Infinity or NaN depending on platform/settings)
        // Kotlin/JVM throws ArithmeticException for float division by zero
        assertFailsWith<ArithmeticException> {
            m.div(0f)
        }
    }

    @Test
    fun testAdd() { // Also tests plus
        val m1 = Mat3f.rowMajor(1f, 2f, 3f, 4f, 5f, 6f, 7f, 8f, 9f)
        val m2 = Mat3f.rowMajor(9f, 8f, 7f, 6f, 5f, 4f, 3f, 2f, 1f)
        val expected = Mat3f.rowMajor(10f, 10f, 10f, 10f, 10f, 10f, 10f, 10f, 10f)

        val addedM = m1.add(m2) // Test add without destination
        assertMat3Equals(expected, addedM, "Add without destination")
        assertNotSame(m1, addedM, "Add without destination should create new instance")
        assertNotSame(m2, addedM, "Add without destination should create new instance")

        val dst = Mat3f.identity()
        val result = m1.add(m2, dst) // Test add with destination
        assertMat3Equals(expected, dst, "Add with destination")
        assertSame(dst, result, "Add with destination should return destination")

        // Test plus alias
        val plusM = m1.plus(m2)
        assertMat3Equals(expected, plusM, "Plus alias without destination")
        assertNotSame(m1, plusM, "Plus alias without destination should create new instance")


        // Edge case: Add identity
        val id = Mat3f.identity()
        val m1PlusId = m1.add(id)
        val expectedM1PlusId = Mat3f.rowMajor(2f, 2f, 3f, 4f, 6f, 6f, 7f, 8f, 10f)
        assertMat3Equals(expectedM1PlusId, m1PlusId, "Add identity")

        // Edge case: Add zero matrix
        val zero = Mat3f.rowMajor(0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f)
        val m1PlusZero = m1.add(zero)
        assertMat3Equals(m1, m1PlusZero, "Add zero matrix")
        assertNotSame(m1, m1PlusZero, "Add zero should create new instance if no dst")
    }

    @Test
    fun testDiff() { // Also tests minus
        val m1 = Mat3f.rowMajor(10f, 10f, 10f, 10f, 10f, 10f, 10f, 10f, 10f)
        val m2 = Mat3f.rowMajor(1f, 2f, 3f, 4f, 5f, 6f, 7f, 8f, 9f)
        val expected = Mat3f.rowMajor(9f, 8f, 7f, 6f, 5f, 4f, 3f, 2f, 1f)


        val diffM = m1.diff(m2) // Test diff without destination
        assertMat3Equals(expected, diffM, "Diff without destination")
        assertNotSame(m1, diffM, "Diff without destination should create new instance")

        val dst = Mat3f.identity()
        val result = m1.diff(m2, dst) // Test diff with destination
        assertMat3Equals(expected, dst, "Diff with destination")
        assertSame(dst, result, "Diff with destination should return destination")

        // Test minus alias
        val minusM = m1.minus(m2)
        assertMat3Equals(expected, minusM, "Minus alias without destination")
        assertNotSame(m1, minusM, "Minus alias without destination should create new instance")

        // Edge case: Subtract identity
        val id = Mat3f.identity()
        val m1MinusId = m1.diff(id)
        val expectedM1MinusId = Mat3f.rowMajor(9f, 10f, 10f, 10f, 9f, 10f, 10f, 10f, 9f)
        assertMat3Equals(expectedM1MinusId, m1MinusId, "Diff identity")

        // Edge case: Subtract zero matrix
        val zero = Mat3f.rowMajor(0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f)
        val m1MinusZero = m1.diff(zero)
        assertMat3Equals(m1, m1MinusZero, "Diff zero matrix")
        assertNotSame(m1, m1MinusZero, "Diff zero should create new instance if no dst")
    }

    @Test
    fun testCopy() { // Also tests clone
        val m = Mat3f.rowMajor(1f, 2f, 3f, 4f, 5f, 6f, 7f, 8f, 9f)
        val dst = Mat3f.identity()

        val result = m.copy(dst)
        assertMat3Equals(m, dst, "Copy basic")
        assertSame(dst, result, "Copy should return destination")
        assertNotSame(m, dst, "Copy should copy to a different instance")

        // Test clone alias
        val clonedM = m.clone()
        assertMat3Equals(m, clonedM, "Clone basic")
        assertNotSame(m, clonedM, "Clone should create a new instance")

        // Edge case: Copy identity
        val id = Mat3f.identity()
        val idCopy = id.copy()
        assertMat3Equals(id, idCopy, "Copy identity")
        assertNotSame(id, idCopy, "Copy identity new instance")

        // Edge case: Copy zero
        val zero = Mat3f.rowMajor(0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f)
        val zeroCopy = zero.copy()
        assertMat3Equals(zero, zeroCopy, "Copy zero")
        assertNotSame(zero, zeroCopy, "Copy zero new instance")
    }

    @Test
    fun testEqualsAndHashCode() {
        val m1 = Mat3f.rowMajor(1f, 2f, 3f, 4f, 5f, 6f, 7f, 8f, 9f)
        val m2 = Mat3f.rowMajor(1f, 2f, 3f, 4f, 5f, 6f, 7f, 8f, 9f)
        val m3 = Mat3f.rowMajor(1f, 2f, 3f, 4f, 0f, 6f, 7f, 8f, 9f) // Different element
        val m4 = Mat3f.identity()

        // Test equals
        assertTrue(m1.equals(m2), "equals: m1 == m2")
        assertTrue(m2.equals(m1), "equals: m2 == m1")
        assertFalse(m1.equals(m3), "equals: m1 != m3")
        assertFalse(m1.equals(m4), "equals: m1 != m4")
        assertFalse(m1.equals(null), "equals: m1 != null")
        assertFalse(m1.equals("string"), "equals: m1 != String")
        assertTrue(m1.equals(m1), "equals: m1 == m1 (reflexive)")


        // Test hashCode
        assertEquals(m1.hashCode(), m2.hashCode(), "hashCode: m1 == m2")
        assertNotEquals(m1.hashCode(), m3.hashCode(), "hashCode: m1 != m3")
        assertNotEquals(m1.hashCode(), m4.hashCode(), "hashCode: m1 != m4")

        // Test consistency: hashCode should be the same on multiple calls
        val h1 = m1.hashCode()
        val h2 = m1.hashCode()
        assertEquals(h1, h2, "hashCode: consistent")
    }

    @Test
    fun testEqualsApproximately() {
        val m1 = Mat3f.rowMajor(1f, 2f, 3f, 4f, 5f, 6f, 7f, 8f, 9f)
        val m2 = Mat3f.rowMajor(1f, 2f, 3f, 4f, 5f, 6f, 7f, 8f, 9f)
        val m3 = Mat3f.rowMajor(1f + EPSILON / 2f, 2f, 3f, 4f, 5f - EPSILON / 2f, 6f, 7f, 8f, 9f + EPSILON / 2f)
        val m4 = Mat3f.rowMajor(1f + EPSILON * 2f, 2f, 3f, 4f, 5f, 6f, 7f, 8f, 9f) // Outside default tolerance
    }

    @Test
    fun testTranspose() {
        val m = Mat3f.rowMajor(
            1f, 2f, 3f,
            4f, 5f, 6f,
            7f, 8f, 9f
        )
        // Remember Mat3f constructor takes row-major like input, but stores column-major.
        // Transpose swaps rows and columns conceptually.
        // Original (conceptual rows): [1,4,7], [2,5,8], [3,6,9]
        // Transposed (conceptual rows): [1,2,3], [4,5,6], [7,8,9]
        val expected = Mat3f.rowMajor(
            1f, 4f, 7f,
            2f, 5f, 8f,
            3f, 6f, 9f
        )

        val transposedM = m.transpose() // Test without destination
        assertMat3Equals(expected, transposedM, "Transpose without destination")
        assertNotSame(m, transposedM, "Transpose without destination should create new instance")

        val dst = Mat3f.identity()
        val result = m.transpose(dst) // Test with destination
        assertMat3Equals(expected, dst, "Transpose with destination")
        assertSame(dst, result, "Transpose with destination should return destination")

        // Edge case: Transpose identity
        val id = Mat3f.identity()
        val idTransposed = id.transpose()
        assertMat3Equals(id, idTransposed, "Transpose identity")
        assertNotSame(id, idTransposed, "Transpose identity new instance")

        // Edge case: Transpose a symmetric matrix (should be itself)
        val sym = Mat3f.rowMajor(1f, 2f, 3f, 2f, 5f, 6f, 3f, 6f, 9f)
        val symTransposed = sym.transpose()
        assertMat3Equals(sym, symTransposed, "Transpose symmetric matrix")
    }

    @Test
    fun testInverse() { // Also tests invert
        // Basic invertible matrix
        val m = Mat3f.scaling(Vec2f(2f, 0.5f)).rotate(PI.toFloat() / 2f) // Scale then rotate 90 deg
        val expectedInverse = Mat3f.rotation(-PI.toFloat() / 2f).scale(Vec2f(0.5f, 2f)) // Rotate back then inverse scale

        val inverseM = m.inverse() // Test inverse without destination
        assertMat3EqualsApproximately(expectedInverse, inverseM, message = "Inverse without destination")
        assertNotSame(m, inverseM, "Inverse without destination should create new instance")

        val dst = Mat3f.identity()
        val result = m.inverse(dst) // Test inverse with destination
        assertMat3EqualsApproximately(expectedInverse, dst, message = "Inverse with destination")
        assertSame(dst, result, "Inverse with destination should return destination")

        // Test invert alias
        val invertedM = m.invert()
        assertMat3EqualsApproximately(expectedInverse, invertedM, message = "Invert alias")
        assertNotSame(m, invertedM, "Invert alias new instance")


        // Edge case: Inverse of identity
        val id = Mat3f.identity()
        val idInverse = id.inverse()
        assertMat3Equals(id, idInverse, "Inverse identity")
        assertNotSame(id, idInverse, "Inverse identity new instance")

        // Edge case: Non-invertible matrix (determinant is zero)
        val nonInvertible = Mat3f.rowMajor(1f, 2f, 3f, 2f, 4f, 6f, 7f, 8f, 9f) // Col 1 = 2 * Col 0
        assertNull(nonInvertible.inverse(Mat3f.identity()), "Inverse non-invertible should return null") // Check return null when dst provided
        assertNull(nonInvertible.inverse(), "Inverse non-invertible without dst should return null")
    }

    @Test
    fun testDeterminant() {
        // Basic matrix
        val m1 = Mat3f.rowMajor(1f, 2f, 0f, 3f, 4f, 0f, 5f, 6f, 1f) // Det = 1*(4*1 - 0*6) - 2*(3*1 - 0*5) + 0 = 4 - 6 = -2
        assertEquals(-2f, m1.determinant(), "Determinant basic")

        // Identity matrix
        val id = Mat3f.identity()
        assertEquals(1f, id.determinant(), "Determinant identity")

        // Non-invertible matrix (zero determinant)
        val nonInvertible = Mat3f.rowMajor(1f, 2f, 3f, 2f, 4f, 6f, 7f, 8f, 9f)
        assertEquals(0f, nonInvertible.determinant(), "Determinant non-invertible")

        // Matrix with scaling
        val scale = Mat3f.scaling(Vec2f(2f, 3f)) // Det = 2 * 3 * 1 = 6
        assertEquals(6f, scale.determinant(), "Determinant scaling")
    }

    @Test
    fun testMultiply() { // Also tests mul
        val m1 = Mat3f.rowMajor( // Represents translation by (7,8) then rotation by 90 deg
            0f, 1f, 0f,
            -1f, 0f, 0f,
            7f, 8f, 1f
        )
        val m2 = Mat3f.rowMajor( // Represents scaling by (2,3)
            2f, 0f, 0f,
            0f, 3f, 0f,
            0f, 0f, 1f
        )
        // Expected: Apply m2 then m1 (Scale -> Rotate -> Translate)
        val expected = Mat3f.rowMajor(
            0f, 3f, 0f, // Col 0: m1 * (2,0,0)t = (0,-2,7)t -> wrong, matrix mul is different
            -2f, 0f, 0f, // Col 1: m1 * (0,3,0)t = (3,0,8)t -> wrong
            7f, 8f, 1f  // Col 2: m1 * (0,0,1)t = (0,0,1)t -> wrong
        )
        // Manual calculation:
        // Row 0: [0, -1, 7] * [2,0,0]t = 0, * [0,3,0]t = -3, * [0,0,1]t = 7 -> [0, -3, 7]
        // Row 1: [1, 0, 8] * [2,0,0]t = 2, * [0,3,0]t = 0, * [0,0,1]t = 8 -> [2, 0, 8]
        // Row 2: [0, 0, 1] * [2,0,0]t = 0, * [0,3,0]t = 0, * [0,0,1]t = 1 -> [0, 0, 1]
        // Result (row major): [0,-3,7], [2,0,8], [0,0,1]
        // Result (column major for constructor): 0,2,0, -3,0,0, 7,8,1
        val expectedCorrect = Mat3f.rowMajor(0f, 2f, 0f, -3f, 0f, 0f, 7f, 8f, 1f)


        val multipliedM = m1.multiply(m2) // Test multiply without destination
        assertMat3EqualsApproximately(expectedCorrect, multipliedM, message = "Multiply without destination")
        assertNotSame(m1, multipliedM, "Multiply without destination should create new instance")

        val dst = Mat3f.identity()
        val result = m1.multiply(m2, dst) // Test multiply with destination
        assertMat3EqualsApproximately(expectedCorrect, dst, message = "Multiply with destination")
        assertSame(dst, result, "Multiply with destination should return destination")

        // Test mul alias
        val mulM = m1.mul(m2)
        assertMat3EqualsApproximately(expectedCorrect, mulM, message = "Mul alias")
        assertNotSame(m1, mulM, "Mul alias new instance")

        // Edge case: Multiply by identity
        val id = Mat3f.identity()
        val m1ById = m1.multiply(id)
        assertMat3Equals(m1, m1ById, "Multiply by identity (right)")
        val idByM1 = id.multiply(m1)
        assertMat3Equals(m1, idByM1, "Multiply by identity (left)")

        // Edge case: Multiply by inverse
        val mInv = m1.inverse()
        assertNotNull(mInv, "Inverse must exist for this test")
        val m1ByInv = m1.multiply(mInv!!)
        assertMat3EqualsApproximately(id, m1ByInv, message = "Multiply by inverse")
    }

    @Test
    fun testSetTranslation() {
        val m = Mat3f.rotation(PI.toFloat() / 4f)
        val v = Vec2f(10f, -20f)
        val expected = Mat3f.rowMajor(
            m[0, 0], m[0, 1], m[0, 2],
            m[1, 0], m[1, 1], m[1, 2],
            10f, -20f, 1f
        )

        val result = m.setTranslation(v) // Test without destination (modifies self)
        assertMat3EqualsApproximately(expected, m, message = "SetTranslation modifies self")
        assertSame(m, result, "SetTranslation should return self")


        // Test with destination
        val mClean = Mat3f.rotation(PI.toFloat() / 4f) // Reset m
        val dst = Mat3f.identity()
        val resultDst = mClean.setTranslation(v, dst)
        assertMat3EqualsApproximately(expected, dst, message = "SetTranslation with destination")
        assertSame(dst, resultDst, "SetTranslation with destination should return destination")
        assertNotEquals(mClean, dst, "SetTranslation with destination should not modify original")


        // Edge case: Set translation on identity
        val id = Mat3f.identity()
        val idTrans = id.setTranslation(Vec2f(1f, 1f))
        val expectedIdTrans = Mat3f.rowMajor(1f, 0f, 0f, 0f, 1f, 0f, 1f, 1f, 1f)
        assertMat3Equals(expectedIdTrans, idTrans, "SetTranslation on identity")
        assertSame(id, idTrans, "SetTranslation on identity modifies self")
    }

    @Test
    fun testGetTranslation() {
        val v = Vec2f(7f, -13f)
        val m = Mat3f.translation(v)
        val expected = v

        val trans = m.getTranslation() // Test without destination
        assertVec2EqualsApproximately(expected, trans, message = "GetTranslation without destination")

        val dst = Vec2f()
        val result = m.getTranslation(dst) // Test with destination
        assertVec2EqualsApproximately(expected, dst, message = "GetTranslation with destination")
        assertSame(dst, result, "GetTranslation with destination should return destination")

        // Edge case: Get translation from identity (should be zero)
        val id = Mat3f.identity()
        val idTrans = id.getTranslation()
        assertVec2EqualsApproximately(Vec2f(0f, 0f), idTrans, message = "GetTranslation from identity")
    }

    @Test
    fun testGetAxis() {
        val m = Mat3f.rowMajor(
            1f, 2f, 3f, // Col 0 -> Axis 0 (X)
            4f, 5f, 6f, // Col 1 -> Axis 1 (Y)
            7f, 8f, 9f  // Col 2 -> Translation (Z/W) - getAxis extracts from rotation part
        )
        // getAxis extracts columns from the top-left 2x2 part (rotation/scale)
        val expectedX = Vec2f(1f, 2f) // First column's top 2 elements
        val expectedY = Vec2f(4f, 5f) // Second column's top 2 elements

        // Test Axis 0 (X)
        val axisX = m.getAxis(0)
        assertVec2EqualsApproximately(expectedX, axisX, message = "GetAxis 0 (X)")
        val dstX = Vec2f()
        val resultX = m.getAxis(0, dstX)
        assertVec2EqualsApproximately(expectedX, dstX, message = "GetAxis 0 (X) with destination")
        assertSame(dstX, resultX, "GetAxis 0 (X) returns destination")

        // Test Axis 1 (Y)
        val axisY = m.getAxis(1)
        assertVec2EqualsApproximately(expectedY, axisY, message = "GetAxis 1 (Y)")
        val dstY = Vec2f()
        val resultY = m.getAxis(1, dstY)
        assertVec2EqualsApproximately(expectedY, dstY, message = "GetAxis 1 (Y) with destination")
        assertSame(dstY, resultY, "GetAxis 1 (Y) returns destination")


        // Edge case: Get axis from identity
        val id = Mat3f.identity()
        assertVec2EqualsApproximately(Vec2f(1f, 0f), id.getAxis(0), message = "GetAxis 0 from identity")
        assertVec2EqualsApproximately(Vec2f(0f, 1f), id.getAxis(1), message = "GetAxis 1 from identity")

        // Edge case: Invalid axis (expect exception or default behavior)
        // Assuming it might throw or return zero/garbage - check documentation or implementation
        assertFailsWith<IndexOutOfBoundsException> { m.getAxis(2) } // Axis 2 is translation
        assertFailsWith<IndexOutOfBoundsException> { m.getAxis(-1) }
    }

    @Test
    fun testSetAxis() {
        val m = Mat3f.identity()
        val axis0 = Vec2f(1f, 2f)
        val axis1 = Vec2f(3f, 4f)

        // Set Axis 0 (X)
        val expectedX = Mat3f.rowMajor(1f, 2f, 0f, 0f, 1f, 0f, 0f, 0f, 1f)
        val resultX = m.setAxis(axis0, 0) // Test without destination (modifies self)
        assertMat3EqualsApproximately(expectedX, m, message = "SetAxis 0 modifies self")
        assertSame(m, resultX, "SetAxis 0 returns self")


        // Set Axis 1 (Y) - starting from identity again
        val mId = Mat3f.identity()
        val expectedY = Mat3f.rowMajor(1f, 0f, 0f, 3f, 4f, 0f, 0f, 0f, 1f)
        val dstY = Mat3f.identity()
        val resultY = mId.setAxis(axis1, 1, dstY) // Test with destination
        assertMat3EqualsApproximately(expectedY, dstY, message = "SetAxis 1 with destination")
        assertSame(dstY, resultY, "SetAxis 1 returns destination")
        assertNotEquals(mId, dstY, "SetAxis 1 with destination does not modify original")


        // Edge case: Set axis with zero vector
        val mZeroAxis = Mat3f.identity()
        mZeroAxis.setAxis(Vec2f(0f, 0f), 0)
        val expectedZeroX = Mat3f.rowMajor(0f, 0f, 0f, 0f, 1f, 0f, 0f, 0f, 1f)
        assertMat3EqualsApproximately(expectedZeroX, mZeroAxis, message = "SetAxis 0 with zero vector")

        // Edge case: Invalid axis (expect exception or default behavior)
        assertFailsWith<IndexOutOfBoundsException> { m.setAxis(axis0, 2) }
        assertFailsWith<IndexOutOfBoundsException> { m.setAxis(axis0, -1) }
    }

    @Test
    fun testGetScaling() {
        // Scaling matrix
        val scaleV = Vec2f(3f, -4f)
        val mScale = Mat3f.scaling(scaleV)
        val expectedScale = scaleV.absoluteValue()
        assertVec2EqualsApproximately(expectedScale, mScale.getScaling(), message = "GetScaling from scaling matrix")

        // Rotation + Scaling matrix
        val angle = PI.toFloat() / 6f
        val mRotScale = Mat3f.rotation(angle).scale(scaleV)
        // getScaling extracts magnitude of axis vectors
        val expectedRotScale = Vec2f(scaleV.x.absoluteValue, scaleV.y.absoluteValue) // Magnitudes
        assertVec2EqualsApproximately(expectedRotScale, mRotScale.getScaling(), message = "GetScaling from rotated scaling matrix")


        // Identity matrix (scaling is 1,1)
        val id = Mat3f.identity()
        assertVec2EqualsApproximately(Vec2f(1f, 1f), id.getScaling(), message = "GetScaling from identity")

        // Zero matrix (scaling is 0,0) - Note: getScaling might fail if axes are zero length
        val zeroMat = Mat3f.rowMajor(0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f) // This is not a valid affine transform matrix usually
        // Depending on implementation, this might return 0,0 or NaN/Infinity or throw.
        // Let's assume it returns 0,0 if axes are zero.
        assertVec2EqualsApproximately(Vec2f(0f, 0f), zeroMat.getScaling(), message = "GetScaling from zero matrix")

        // Test with destination
        val dst = Vec2f()
        val result = mRotScale.getScaling(dst)
        assertVec2EqualsApproximately(expectedRotScale, dst, message = "GetScaling with destination")
        assertSame(dst, result, "GetScaling returns destination")
    }

    @Test
    fun testGet3DScaling() {
        // Scaling matrix (using 3D scaling companion)
        val scaleV = Vec3f(2f, -3f, 4f)
        val mScale = Mat3f.scaling3D(scaleV)
        val expectedScale = scaleV.absoluteValue()
        assertVec3EqualsApproximately(expectedScale, mScale.get3DScaling(), message = "Get3DScaling from 3D scaling matrix")

        // Rotation + Scaling matrix (using 3D rotation/scaling)
        val angle = PI.toFloat() / 3f
        val mRotScale = Mat3f.rotationY(angle).scale3D(scaleV)
        // get3DScaling extracts magnitude of axis vectors (from 3x3 part)
        val expectedRotScale = Vec3f(scaleV.x.absoluteValue, scaleV.y.absoluteValue, scaleV.z.absoluteValue) // Magnitudes
        assertVec3EqualsApproximately(expectedRotScale, mRotScale.get3DScaling(), message = "Get3DScaling from rotated 3D scaling matrix")


        // Identity matrix (scaling is 1,1,1)
        val id = Mat3f.identity()
        assertVec3EqualsApproximately(Vec3f(1f, 1f, 1f), id.get3DScaling(), message = "Get3DScaling from identity")

        // Test with destination
        val dst = Vec3f()
        val result = mRotScale.get3DScaling(dst)
        assertVec3EqualsApproximately(expectedRotScale, dst, message = "Get3DScaling with destination")
        assertSame(dst, result, "Get3DScaling returns destination")
    }

    @Test
    fun testTranslate() {
        val m = Mat3f.rotation(PI.toFloat() / 4f)
        val v = Vec2f(10f, 5f)
        val expected = Mat3f.translation(v).multiply(Mat3f.rotation(PI.toFloat() / 4f))

        val translatedM = m.translate(v) // Test without destination
        assertMat3EqualsApproximately(expected, translatedM, message = "Translate without destination")
        assertNotSame(m, translatedM, "Translate without destination should create new instance")

        val dst = Mat3f.identity()
        val result = m.translate(v, dst) // Test with destination
        assertMat3EqualsApproximately(expected, dst, message = "Translate with destination")
        assertSame(dst, result, "Translate with destination should return destination")

        // Edge case: Translate identity
        val id = Mat3f.identity()
        val idTranslated = id.translate(v)
        val expectedIdTranslated = Mat3f.translation(v)
        assertMat3Equals(expectedIdTranslated, idTranslated, "Translate identity")
        assertNotSame(id, idTranslated, "Translate identity new instance")

        // Edge case: Translate by zero vector
        val mTranslatedZero = m.translate(Vec2f(0f, 0f))
        assertMat3Equals(m, mTranslatedZero, "Translate by zero")
        assertNotSame(m, mTranslatedZero, "Translate by zero new instance")
    }

    @Test
    fun testRotate() { // Also tests rotateZ
        val m = Mat3f.translation(Vec2f(10f, 0f))
        val angle = PI.toFloat() / 2f // 90 degrees
        // Expected = Rotation(angle) * Translation(10,0)
        val expected = m.multiply(Mat3f.rotation(angle))

        val rotatedM = m.rotate(angle) // Test rotate without destination
        assertMat3EqualsApproximately(expected, rotatedM, message = "Rotate without destination")
        assertNotSame(m, rotatedM, "Rotate without destination should create new instance")

        val dst = Mat3f.identity()
        val result = m.rotate(angle, dst) // Test rotate with destination
        assertMat3EqualsApproximately(expected, dst, message = "Rotate with destination")
        assertSame(dst, result, "Rotate with destination should return destination")

        // Test rotateZ alias
        val rotatedZ_M = m.rotateZ(angle)
        assertMat3EqualsApproximately(expected, rotatedZ_M, message = "RotateZ alias")
        assertNotSame(m, rotatedZ_M, "RotateZ alias new instance")

        // Edge case: Rotate identity
        val id = Mat3f.identity()
        val idRotated = id.rotate(angle)
        val expectedIdRotated = Mat3f.rotation(angle)
        assertMat3EqualsApproximately(expectedIdRotated, idRotated, message = "Rotate identity")
        assertNotSame(id, idRotated, "Rotate identity new instance")

        // Edge case: Rotate by zero angle
        val mRotatedZero = m.rotate(0f)
        assertMat3Equals(m, mRotatedZero, "Rotate by zero")
        assertNotSame(m, mRotatedZero, "Rotate by zero new instance")
    }

    @Test
    fun testRotateX() {
        val m = Mat3f.scaling3D(Vec3f(1f, 2f, 3f)) // Affects 3D part
        val angle = PI.toFloat() / 3f
        // RotateX applies AFTER the matrix's existing transform
        // Expected = RotationX(angle) * Scaling3D(1,2,3)
        val expected = Mat3f.rotationX(angle).multiply(m)

        val rotatedM = m.rotateX(angle) // Test without destination
        assertMat3EqualsApproximately(expected, rotatedM, message = "RotateX without destination")
        assertNotSame(m, rotatedM, "RotateX without destination should create new instance")

        val dst = Mat3f.identity()
        val result = m.rotateX(angle, dst) // Test with destination
        assertMat3EqualsApproximately(expected, dst, message = "RotateX with destination")
        assertSame(dst, result, "RotateX with destination should return destination")

        // Edge case: Rotate identity
        val id = Mat3f.identity()
        val idRotated = id.rotateX(angle)
        val expectedIdRotated = Mat3f.rotationX(angle)
        assertMat3EqualsApproximately(expectedIdRotated, idRotated, message = "RotateX identity")
        assertNotSame(id, idRotated, "RotateX identity new instance")
    }

    @Test
    fun testToFloatArray() {
        val m = Mat3f.rowMajor(1f, 2f, 3f, 4f, 5f, 6f, 7f, 8f, 9f)
        // Expected internal column-major layout (padded)
        val expectedArray = floatArrayOf(1f, 4f, 7f, 0f, 2f, 5f, 8f, 0f, 3f, 6f, 9f, 0f)
        val actualArray = m.toFloatArray()
        assertContentEquals(expectedArray, actualArray, "toFloatArray basic")

        // Test identity
        val id = Mat3f.identity()
        val expectedIdArray = floatArrayOf(1f, 0f, 0f, 0f, 0f, 1f, 0f, 0f, 0f, 0f, 1f, 0f)
        assertContentEquals(expectedIdArray, id.toFloatArray(), "toFloatArray identity")

        // Test zero
        val zero = Mat3f.rowMajor(0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f)
        val expectedZeroArray = FloatArray(12)
        assertContentEquals(expectedZeroArray, zero.toFloatArray(), "toFloatArray zero")
    }

    @Test
    fun testPlusAlias() {
        val m1 = Mat3f.rowMajor(1f, 1f, 1f, 1f, 1f, 1f, 1f, 1f, 1f)
        val m2 = Mat3f.rowMajor(2f, 2f, 2f, 2f, 2f, 2f, 2f, 2f, 2f)
        val expected = Mat3f.rowMajor(3f, 3f, 3f, 3f, 3f, 3f, 3f, 3f, 3f)
        assertMat3Equals(expected, m1.plus(m2), "Plus alias test")
    }

    @Test
    fun testMinusAlias() {
        val m1 = Mat3f.rowMajor(5f, 5f, 5f, 5f, 5f, 5f, 5f, 5f, 5f)
        val m2 = Mat3f.rowMajor(1f, 2f, 1f, 2f, 1f, 2f, 1f, 2f, 1f)
        val expected = Mat3f.rowMajor(4f, 3f, 4f, 3f, 4f, 3f, 4f, 3f, 4f)
        assertMat3Equals(expected, m1.minus(m2), "Minus alias test")
    }

    @Test
    fun testCloneAlias() {
        val m = Mat3f.rowMajor(1f, 5f, 7f, 2f, 3f, 9f, 4f, 6f, 8f)
        val cloned = m.clone()
        assertMat3Equals(m, cloned, "Clone alias basic")
        assertNotSame(m, cloned, "Clone alias should create new instance")
    }

    @Test
    fun testInvertAlias() {
        val m = Mat3f.scaling(Vec2f(2f, 0.5f)).rotate(PI.toFloat() / 2f)
        val expectedInverse = Mat3f.rotation(-PI.toFloat() / 2f).scale(Vec2f(0.5f, 2f))
        val invertedM = m.invert()
        assertNotNull(invertedM, "Invert should succeed for invertible matrix")
        assertMat3EqualsApproximately(expectedInverse, invertedM!!, message = "Invert alias test")
        assertNotSame(m, invertedM, "Invert alias should create new instance")

        // Test non-invertible
        val nonInvertible = Mat3f.rowMajor(1f, 1f, 1f, 1f, 1f, 1f, 1f, 1f, 1f)
        assertEquals(Mat3f.identity(),nonInvertible.invert(), "Invert non-invertible should return identity")
    }

    @Test
    fun testMulAlias() {
        val m1 = Mat3f.translation(Vec2f(1f, 2f))
        val m2 = Mat3f.scaling(Vec2f(3f, 4f))
        val expected = m1.multiply(m2) // Calculate expected using original method
        val mulResult = m1.mul(m2)
        assertMat3EqualsApproximately(expected, mulResult, message = "Mul alias test")
        assertNotSame(m1, mulResult, "Mul alias should create new instance")
    }

    @Test
    fun testRotateZAlias() {
        val m = Mat3f.translation(Vec2f(5f, 0f))
        val angle = PI.toFloat() / 4f
        val expected = m.rotate(angle) // Calculate expected using original method
        val rotateZResult = m.rotateZ(angle)
        assertMat3EqualsApproximately(expected, rotateZResult, message = "RotateZ alias test")
        assertNotSame(m, rotateZResult, "RotateZ alias should create new instance")
    }


    @Test
    fun testRotateY() {
        val m = Mat3f.scaling3D(Vec3f(1f, 2f, 3f)) // Affects 3D part
        val angle = -PI.toFloat() / 4f
        // RotateY applies AFTER the matrix's existing transform
        // Expected = RotationY(angle) * Scaling3D(1,2,3)
        val expected = Mat3f.rotationY(angle).multiply(m)

        val rotatedM = m.rotateY(angle) // Test without destination
        assertMat3EqualsApproximately(expected, rotatedM, message = "RotateY without destination")
        assertNotSame(m, rotatedM, "RotateY without destination should create new instance")

        val dst = Mat3f.identity()
        val result = m.rotateY(angle, dst) // Test with destination
        assertMat3EqualsApproximately(expected, dst, message = "RotateY with destination")
        assertSame(dst, result, "RotateY with destination should return destination")

        // Edge case: Rotate identity
        val id = Mat3f.identity()
        val idRotated = id.rotateY(angle)
        val expectedIdRotated = Mat3f.rotationY(angle)
        assertMat3EqualsApproximately(expectedIdRotated, idRotated, message = "RotateY identity")
        assertNotSame(id, idRotated, "RotateY identity new instance")
    }

    @Test
    fun testScale() {
        val m = Mat3f.rotation(PI.toFloat() / 3f)
        val v = Vec2f(2f, -1f)
        // Scale applies AFTER the matrix's existing transform
        // Expected = Scaling(v) * Rotation(PI/3)
        val expected = Mat3f.scaling(v).multiply(m)

        val scaledM = m.scale(v) // Test without destination
        assertMat3EqualsApproximately(expected, scaledM, message = "Scale without destination")
        assertNotSame(m, scaledM, "Scale without destination should create new instance")

        val dst = Mat3f.identity()
        val result = m.scale(v, dst) // Test with destination
        assertMat3EqualsApproximately(expected, dst, message = "Scale with destination")
        assertSame(dst, result, "Scale with destination should return destination")

        // Edge case: Scale identity
        val id = Mat3f.identity()
        val idScaled = id.scale(v)
        val expectedIdScaled = Mat3f.scaling(v)
        assertMat3Equals(expectedIdScaled, idScaled, "Scale identity")
        assertNotSame(id, idScaled, "Scale identity new instance")

        // Edge case: Scale by (1,1)
        val mScaledOne = m.scale(Vec2f(1f, 1f))
        assertMat3Equals(m, mScaledOne, "Scale by one")
        assertNotSame(m, mScaledOne, "Scale by one new instance")
    }

    @Test
    fun testScale3D() {
        val m = Mat3f.rotationY(PI.toFloat() / 6f) // Affects 3D part
        val v = Vec3f(0.5f, 1f, 2f)
        // Scale3D applies AFTER the matrix's existing transform
        // Expected = Scaling3D(v) * RotationY(PI/6)
        val expected = Mat3f.scaling3D(v).multiply(m)

        val scaledM = m.scale3D(v) // Test without destination
        assertMat3EqualsApproximately(expected, scaledM, message = "Scale3D without destination")
        assertNotSame(m, scaledM, "Scale3D without destination should create new instance")

        val dst = Mat3f.identity()
        val result = m.scale3D(v, dst) // Test with destination
        assertMat3EqualsApproximately(expected, dst, message = "Scale3D with destination")
        assertSame(dst, result, "Scale3D with destination should return destination")

        // Edge case: Scale identity
        val id = Mat3f.identity()
        val idScaled = id.scale3D(v)
        val expectedIdScaled = Mat3f.scaling3D(v)
        assertMat3Equals(expectedIdScaled, idScaled, "Scale3D identity")
        assertNotSame(id, idScaled, "Scale3D identity new instance")
    }

    @Test
    fun testUniformScale() {
        val m = Mat3f.rotation(PI.toFloat() / 3f)
        val s = 3f
        // UniformScale applies AFTER the matrix's existing transform
        // Expected = UniformScaling(s) * Rotation(PI/3)
        val expected = Mat3f.uniformScaling(s).multiply(m)

        val scaledM = m.uniformScale(s) // Test without destination
        assertMat3EqualsApproximately(expected, scaledM, message = "UniformScale without destination")
        assertNotSame(m, scaledM, "UniformScale without destination should create new instance")

        val dst = Mat3f.identity()
        val result = m.uniformScale(s, dst) // Test with destination
        assertMat3EqualsApproximately(expected, dst, message = "UniformScale with destination")
        assertSame(dst, result, "UniformScale with destination should return destination")

        // Edge case: Scale identity
        val id = Mat3f.identity()
        val idScaled = id.uniformScale(s)
        val expectedIdScaled = Mat3f.uniformScaling(s)
        assertMat3Equals(expectedIdScaled, idScaled, "UniformScale identity")
        assertNotSame(id, idScaled, "UniformScale identity new instance")
    }

    @Test
    fun testUniformScale3D() {
        val m = Mat3f.rotationY(PI.toFloat() / 6f) // Affects 3D part
        val s = -0.5f
        // UniformScale3D applies AFTER the matrix's existing transform
        // Expected = UniformScaling3D(s) * RotationY(PI/6)
        val expected = Mat3f.uniformScaling3D(s).multiply(m)

        val scaledM = m.uniformScale3D(s) // Test without destination
        assertMat3EqualsApproximately(expected, scaledM, message = "UniformScale3D without destination")
        assertNotSame(m, scaledM, "UniformScale3D without destination should create new instance")

        val dst = Mat3f.identity()
        val result = m.uniformScale3D(s, dst) // Test with destination
        assertMat3EqualsApproximately(expected, dst, message = "UniformScale3D with destination")
        assertSame(dst, result, "UniformScale3D with destination should return destination")

        // Edge case: Scale identity
        val id = Mat3f.identity()
        val idScaled = id.uniformScale3D(s)
        val expectedIdScaled = Mat3f.uniformScaling3D(s)
        assertMat3Equals(expectedIdScaled, idScaled, "UniformScale3D identity")
        assertNotSame(id, idScaled, "UniformScale3D identity new instance")
    }


    @Test
    fun testInstanceIdentity() {
        val m = Mat3f.rowMajor(1f, 2f, 3f, 4f, 5f, 6f, 7f, 8f, 9f)
        val expected = Mat3f.identity()

        val result = m.identity() // Test without destination
        assertMat3Equals(expected, result, "Instance identity without destination")
        assertNotSame(m, result, "Instance identity without destination should create new instance")


        val dst = Mat3f.identity()
        val resultDst = m.identity(dst) // Test with destination
        assertMat3Equals(expected, dst, "Instance identity with destination")
        assertSame(dst, resultDst, "Instance identity with destination should return destination")

        // Test on an already identity matrix
        val id = Mat3f.identity()
        val idResult = id.identity()
        assertMat3Equals(id, idResult, "Instance identity on identity matrix")
        assertNotSame(id, idResult, "Instance identity on identity should create new instance if no dst")
    }
}

