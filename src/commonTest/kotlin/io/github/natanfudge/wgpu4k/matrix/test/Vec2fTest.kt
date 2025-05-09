package io.github.natanfudge.wgpu4k.matrix.test

import io.github.natanfudge.wgpu4k.matrix.EPSILON
import io.github.natanfudge.wgpu4k.matrix.Vec2f
import io.github.natanfudge.wgpu4k.matrix.Vec3f
import io.github.natanfudge.wgpu4k.matrix.test.js.assertVec3Equals
import kotlin.math.abs
import kotlin.math.acos
import kotlin.test.*

class Vec2fOperatorTest {

    private fun assertVec2fEqualsApproximately(expected: Vec2f, actual: Vec2f, message: String? = null) {
        val deltaX = abs(expected.x - actual.x)
        val deltaY = abs(expected.y - actual.y)
        assertTrue(
            deltaX < EPSILON && deltaY < EPSILON,
            (message ?: "") + "\nExpected: $expected\nActual:   $actual\nDelta:    Vec2f(x=$deltaX, y=$deltaY)"
        )
    }

    @Test
    fun testPlusOperator() {
        val v1 = Vec2f(1f, 2f)
        val v2 = Vec2f(3f, 4f)
        val expected1 = Vec2f(4f, 6f)
        assertVec2fEqualsApproximately(expected1, v1 + v2, "Positive + Positive")

        val v3 = Vec2f(-1f, -2f)
        val v4 = Vec2f(-3f, -4f)
        val expected2 = Vec2f(-4f, -6f)
        assertVec2fEqualsApproximately(expected2, v3 + v4, "Negative + Negative")

        val v5 = Vec2f(0f, 0f)
        val v6 = Vec2f(5f, -5f)
        val expected3 = Vec2f(5f, -5f)
        assertVec2fEqualsApproximately(expected3, v5 + v6, "Zero + Vector")
        assertVec2fEqualsApproximately(expected3, v6 + v5, "Vector + Zero")
    }

    @Test
    fun testMinusOperator() {
        val v1 = Vec2f(5f, 7f)
        val v2 = Vec2f(2f, 3f)
        val expected1 = Vec2f(3f, 4f)
        assertVec2fEqualsApproximately(expected1, v1 - v2, "Positive - Positive")

        val v3 = Vec2f(-5f, -7f)
        val v4 = Vec2f(-2f, -3f)
        val expected2 = Vec2f(-3f, -4f)
        assertVec2fEqualsApproximately(expected2, v3 - v4, "Negative - Negative")

        val v5 = Vec2f(0f, 0f)
        val v6 = Vec2f(5f, -5f)
        val expected3 = Vec2f(-5f, 5f)
        assertVec2fEqualsApproximately(expected3, v5 - v6, "Zero - Vector")
        val expected4 = Vec2f(5f, -5f)
        assertVec2fEqualsApproximately(expected4, v6 - v5, "Vector - Zero")
    }

    @Test
    fun testTimesScalarOperator() {
        val v1 = Vec2f(2f, 3f)
        val s1 = 4f
        val expected1 = Vec2f(8f, 12f)
        assertVec2fEqualsApproximately(expected1, v1 * s1, "Vector * Positive Scalar")

        val v2 = Vec2f(2f, -3f)
        val s2 = -2f
        val expected2 = Vec2f(-4f, 6f)
        assertVec2fEqualsApproximately(expected2, v2 * s2, "Vector * Negative Scalar")

        val v3 = Vec2f(5f, 10f)
        val s3 = 0f
        val expected3 = Vec2f(0f, 0f)
        assertVec2fEqualsApproximately(expected3, v3 * s3, "Vector * Zero Scalar")
    }

    @Test
    fun testDivScalarOperator() {
        val v1 = Vec2f(8f, 12f)
        val s1 = 4f
        val expected1 = Vec2f(2f, 3f)
        assertVec2fEqualsApproximately(expected1, v1 / s1, "Vector / Positive Scalar")

        val v2 = Vec2f(-4f, 6f)
        val s2 = -2f
        val expected2 = Vec2f(2f, -3f)
        assertVec2fEqualsApproximately(expected2, v2 / s2, "Vector / Negative Scalar")

        val v3 = Vec2f(0f, 0f)
        val s3 = 5f
        val expected3 = Vec2f(0f, 0f)
        assertVec2fEqualsApproximately(expected3, v3 / s3, "Zero Vector / Scalar")

        // Test division by zero - should ideally throw, but Kotlin Float division results in Infinity
        val v4 = Vec2f(5f, 10f)
        val s4 = 0f
        val res = v4 / s4
        assertEquals(res.x, Float.POSITIVE_INFINITY)
        assertEquals(res.y, Float.POSITIVE_INFINITY)
    }

    @Test
    fun testUnaryMinusOperator() {
        val v1 = Vec2f(1f, 2f)
        val expected1 = Vec2f(-1f, -2f)
        assertVec2fEqualsApproximately(expected1, -v1, "Unary minus on positive vector")

        val v2 = Vec2f(-3f, -4f)
        val expected2 = Vec2f(3f, 4f)
        assertVec2fEqualsApproximately(expected2, -v2, "Unary minus on negative vector")

        val v3 = Vec2f(0f, 0f)
        val expected3 = Vec2f(0f, 0f)
        assertVec2fEqualsApproximately(expected3, -v3, "Unary minus on zero vector")
    }

    @Test
    fun testSet() {
        val v = Vec2f()
        v.set(5f, -10f)
        assertVec2fEqualsApproximately(Vec2f(5f, -10f), v, "Set positive and negative")

        v.set(0f, 0f)
        assertVec2fEqualsApproximately(Vec2f(0f, 0f), v, "Set zero")

        v.set(1.23f, 4.56f)
        assertVec2fEqualsApproximately(Vec2f(1.23f, 4.56f), v, "Set fractional")
    }

    @Test
    fun testCeil() {
        val v1 = Vec2f(1.2f, 3.8f)
        assertVec2fEqualsApproximately(Vec2f(2f, 4f), v1.ceil(), "Ceil positive fractional")

        val v2 = Vec2f(-1.2f, -3.8f)
        assertVec2fEqualsApproximately(Vec2f(-1f, -3f), v2.ceil(), "Ceil negative fractional")

        val v3 = Vec2f(5f, -2f)
        assertVec2fEqualsApproximately(Vec2f(5f, -2f), v3.ceil(), "Ceil integers")
    }

    @Test
    fun testFloor() {
        val v1 = Vec2f(1.2f, 3.8f)
        assertVec2fEqualsApproximately(Vec2f(1f, 3f), v1.floor(), "Floor positive fractional")

        val v2 = Vec2f(-1.2f, -3.8f)
        assertVec2fEqualsApproximately(Vec2f(-2f, -4f), v2.floor(), "Floor negative fractional")

        val v3 = Vec2f(5f, -2f)
        assertVec2fEqualsApproximately(Vec2f(5f, -2f), v3.floor(), "Floor integers")
    }

    @Test
    fun testRound() {
        val v1 = Vec2f(1.2f, 3.8f)
        assertVec2fEqualsApproximately(Vec2f(1f, 4f), v1.round(), "Round positive fractional")

        val v2 = Vec2f(-1.2f, -3.8f)
        assertVec2fEqualsApproximately(Vec2f(-1f, -4f), v2.round(), "Round negative fractional")

        val v3 = Vec2f(5.5f, -2.5f) // .5 rounds towards positive infinity
        assertVec2fEqualsApproximately(Vec2f(6f, -2f), v3.round(), "Round .5 cases")
    }

    @Test
    fun testClamp() {
        val v1 = Vec2f(15f, -5f)
        assertVec2fEqualsApproximately(Vec2f(10f, 0f), v1.clamp(0f, 10f), "Clamp outside range")

        val v2 = Vec2f(5f, 5f)
        assertVec2fEqualsApproximately(Vec2f(5f, 5f), v2.clamp(0f, 10f), "Clamp inside range")

        val v3 = Vec2f(0f, 10f)
        assertVec2fEqualsApproximately(Vec2f(0f, 10f), v3.clamp(0f, 10f), "Clamp at boundaries")
    }

    @Test
    fun testAddScaled() {
        val v1 = Vec2f(1f, 2f)
        val v2 = Vec2f(3f, 4f)
        val scale1 = 2f
        assertVec2fEqualsApproximately(Vec2f(7f, 10f), v1.addScaled(v2, scale1), "AddScaled positive scale")

        val v3 = Vec2f(1f, 2f)
        val v4 = Vec2f(3f, 4f)
        val scale2 = -1f
        assertVec2fEqualsApproximately(Vec2f(-2f, -2f), v3.addScaled(v4, scale2), "AddScaled negative scale")

        val v5 = Vec2f(1f, 2f)
        val v6 = Vec2f(3f, 4f)
        val scale3 = 0f
        assertVec2fEqualsApproximately(Vec2f(1f, 2f), v5.addScaled(v6, scale3), "AddScaled zero scale")
    }

    @Test
    fun testAngle() {
        val v1 = Vec2f(1f, 0f)
        val v2 = Vec2f(0f, 1f)
        assertEquals(kotlin.math.PI.toFloat() / 2f, v1.angle(v2), EPSILON, "Angle 90 degrees")

        val v3 = Vec2f(1f, 0f)
        val v4 = Vec2f(-1f, 0f)
        assertEquals(kotlin.math.PI.toFloat(), v3.angle(v4), EPSILON, "Angle 180 degrees")

        val v5 = Vec2f(3f, 4f) // Length 5
        val v6 = Vec2f(3f, 4f)
        assertEquals(0f, v5.angle(v6), EPSILON, "Angle 0 degrees (same vector)")

        val v7 = Vec2f(0f, 0f)
        val v8 = Vec2f(1f, 1f)
        assertEquals(
            acos(0.0f),
            v7.angle(v8),
            EPSILON,
            "Angle with zero vector"
        ) // Dot product is 0, mag is 0 -> cosine 0 -> acos(0) = PI/2? No, mag is 0 -> cosine is 0. acos(0) = PI/2. Let's re-check implementation. Ah, returns 0 if mag is 0.
        assertEquals(acos(0.0f), v8.angle(v7), EPSILON, "Angle with zero vector (reversed)")
    }

    @Test
    fun testEqualsApproximately() {
        val v1 = Vec2f(1f, 2f)
        val v2 = Vec2f(1f + EPSILON / 2f, 2f - EPSILON / 2f)
        assertTrue(v1.equalsApproximately(v2), "EqualsApproximately within epsilon")

        val v3 = Vec2f(1f, 2f)
        val v4 = Vec2f(1f + EPSILON * 2f, 2f)
        assertFalse(v3.equalsApproximately(v4), "EqualsApproximately outside epsilon (x)")

        val v5 = Vec2f(1f, 2f)
        val v6 = Vec2f(1f, 2f - EPSILON * 2f)
        assertFalse(v5.equalsApproximately(v6), "EqualsApproximately outside epsilon (y)")
    }

    @Test
    fun testLerp() {
        val v1 = Vec2f(0f, 0f)
        val v2 = Vec2f(10f, 20f)
        assertVec2fEqualsApproximately(Vec2f(5f, 10f), v1.lerp(v2, 0.5f), "Lerp t=0.5")
        assertVec2fEqualsApproximately(Vec2f(0f, 0f), v1.lerp(v2, 0f), "Lerp t=0")
        assertVec2fEqualsApproximately(Vec2f(10f, 20f), v1.lerp(v2, 1f), "Lerp t=1")
    }

    @Test
    fun testLerpV() {
        val v1 = Vec2f(0f, 100f)
        val v2 = Vec2f(10f, 0f)
        val t = Vec2f(0.5f, 0.2f)
        assertVec2fEqualsApproximately(Vec2f(5f, 80f), v1.lerpV(v2, t), "LerpV different t values")

        val tZero = Vec2f(0f, 0f)
        assertVec2fEqualsApproximately(v1, v1.lerpV(v2, tZero), "LerpV t=0")

        val tOne = Vec2f(1f, 1f)
        assertVec2fEqualsApproximately(v2, v1.lerpV(v2, tOne), "LerpV t=1")
    }

    @Test
    fun testMax() {
        val v1 = Vec2f(1f, 5f)
        val v2 = Vec2f(3f, 2f)
        assertVec2fEqualsApproximately(Vec2f(3f, 5f), v1.max(v2), "Max different components")

        val v3 = Vec2f(-1f, -5f)
        val v4 = Vec2f(-3f, -2f)
        assertVec2fEqualsApproximately(Vec2f(-1f, -2f), v3.max(v4), "Max negative numbers")

        val v5 = Vec2f(5f, 5f)
        assertVec2fEqualsApproximately(Vec2f(5f, 5f), v5.max(v5), "Max same vector")
    }

    @Test
    fun testMin() {
        val v1 = Vec2f(1f, 5f)
        val v2 = Vec2f(3f, 2f)
        assertVec2fEqualsApproximately(Vec2f(1f, 2f), v1.min(v2), "Min different components")

        val v3 = Vec2f(-1f, -5f)
        val v4 = Vec2f(-3f, -2f)
        assertVec2fEqualsApproximately(Vec2f(-3f, -5f), v3.min(v4), "Min negative numbers")

        val v5 = Vec2f(5f, 5f)
        assertVec2fEqualsApproximately(Vec2f(5f, 5f), v5.min(v5), "Min same vector")
    }

    @Test
    fun testInverse() {
        val v1 = Vec2f(2f, 4f)
        assertVec2fEqualsApproximately(Vec2f(0.5f, 0.25f), v1.inverse(), "Inverse positive")

        val v2 = Vec2f(-1f, -5f)
        assertVec2fEqualsApproximately(Vec2f(-1f, -0.2f), v2.inverse(), "Inverse negative")

        // Inverse of zero results in Infinity
        val v3 = Vec2f(0f, 1f)
        val inv3 = v3.inverse()
        assertTrue(inv3.x.isInfinite() && inv3.x > 0, "Inverse zero x")
        assertEquals(1f, inv3.y, "Inverse zero y")
    }

    @Test
    fun testCross() {
        val v1 = Vec2f(1f, 0f)
        val v2 = Vec2f(0f, 1f)
        val cross1 = v1.cross(v2)
        assertVec3Equals(Vec3f(0f, 0f, 1f), cross1, "Cross i x j = k")

        val v3 = Vec2f(0f, 1f)
        val v4 = Vec2f(1f, 0f)
        val cross2 = v3.cross(v4)
        assertVec3Equals(Vec3f(0f, 0f, -1f), cross2, "Cross j x i = -k")

        val v5 = Vec2f(2f, 3f)
        val v6 = Vec2f(4f, 6f) // Parallel vector
        val cross3 = v5.cross(v6)
        assertVec3Equals(Vec3f(0f, 0f, 0f), cross3, "Cross parallel vectors")
    }

    @Test
    fun testDot() {
        val v1 = Vec2f(1f, 2f)
        val v2 = Vec2f(3f, 4f)
        assertEquals(1f * 3f + 2f * 4f, v1.dot(v2), "Dot product positive")

        val v3 = Vec2f(1f, 0f)
        val v4 = Vec2f(0f, 1f) // Orthogonal
        assertEquals(0f, v3.dot(v4), "Dot product orthogonal")

        val v5 = Vec2f(-1f, -2f)
        val v6 = Vec2f(3f, -4f)
        assertEquals((-1f * 3f) + (-2f * -4f), v5.dot(v6), "Dot product mixed signs")
    }

    @Test
    fun testLengthAndLengthSq() {
        val v1 = Vec2f(3f, 4f) // Pythagorean triple
        assertEquals(25f, v1.lengthSq, "LengthSq positive")
        assertEquals(5f, v1.length, "Length positive")

        val v2 = Vec2f(-5f, -12f) // Pythagorean triple
        assertEquals(169f, v2.lengthSq, "LengthSq negative")
        assertEquals(13f, v2.length, "Length negative")

        val v3 = Vec2f(0f, 0f)
        assertEquals(0f, v3.lengthSq, "LengthSq zero")
        assertEquals(0f, v3.length, "Length zero")
    }

    @Test
    fun testDistanceAndDistanceSq() {
        val v1 = Vec2f(1f, 2f)
        val v2 = Vec2f(4f, 6f) // Diff (3, 4)
        assertEquals(25f, v1.distanceSq(v2), "DistanceSq positive")
        assertEquals(5f, v1.distance(v2), "Distance positive")

        val v3 = Vec2f(-1f, -2f)
        val v4 = Vec2f(-6f, -14f) // Diff (-5, -12)
        assertEquals(169f, v3.distanceSq(v4), "DistanceSq negative")
        assertEquals(13f, v3.distance(v4), "Distance negative")

        val v5 = Vec2f(10f, 20f)
        assertEquals(0f, v5.distanceSq(v5), "DistanceSq same point")
        assertEquals(0f, v5.distance(v5), "Distance same point")
    }

    @Test
    fun testNormalize() {
        val v1 = Vec2f(3f, 4f)
        val norm1 = v1.normalize()
        assertVec2fEqualsApproximately(Vec2f(0.6f, 0.8f), norm1, "Normalize positive")
        assertEquals(1f, norm1.length, EPSILON, "Normalized length positive")

        val v2 = Vec2f(-5f, 0f)
        val norm2 = v2.normalize()
        assertVec2fEqualsApproximately(Vec2f(-1f, 0f), norm2, "Normalize negative axis")
        assertEquals(1f, norm2.length, EPSILON, "Normalized length negative axis")

        val v3 = Vec2f(0f, 0f)
        val norm3 = v3.normalize()
        assertVec2fEqualsApproximately(Vec2f(0f, 0f), norm3, "Normalize zero vector")
        assertEquals(0f, norm3.length, EPSILON, "Normalized length zero vector")
    }

    @Test
    fun testCopyTo() {
        val v1 = Vec2f(10f, -20f)
        val v2 = Vec2f()
        v1.copyTo(v2)
        assertVec2fEqualsApproximately(v1, v2, "CopyTo existing destination")
        assertNotSame(v1, v2, "CopyTo should not be the same instance")

        val v3 = Vec2f(5f, 6f)
        val v4 = v3.copyTo() // Create new destination
        assertVec2fEqualsApproximately(v3, v4, "CopyTo new destination")
        assertNotSame(v3, v4, "CopyTo new destination should not be the same instance")

        val v5 = Vec2f(0f, 0f)
        val v6 = v5.copyTo()
        assertVec2fEqualsApproximately(Vec2f(0f, 0f), v6, "CopyTo zero vector")
    }

    @Test
    fun testMultiply() { // Component-wise multiplication
        val v1 = Vec2f(2f, 3f)
        val v2 = Vec2f(4f, 5f)
        assertVec2fEqualsApproximately(Vec2f(8f, 15f), v1.multiply(v2), "Multiply positive")

        val v3 = Vec2f(-2f, 3f)
        val v4 = Vec2f(4f, -5f)
        assertVec2fEqualsApproximately(Vec2f(-8f, -15f), v3.multiply(v4), "Multiply mixed signs")

        val v5 = Vec2f(10f, 20f)
        val v6 = Vec2f(0f, 0f)
        assertVec2fEqualsApproximately(Vec2f(0f, 0f), v5.multiply(v6), "Multiply by zero")
        assertVec2fEqualsApproximately(Vec2f(0f, 0f), v6.multiply(v5), "Multiply zero by vector")
    }

    @Test
    fun testDivide() { // Component-wise division
        val v1 = Vec2f(8f, 15f)
        val v2 = Vec2f(4f, 5f)
        assertVec2fEqualsApproximately(Vec2f(2f, 3f), v1.divide(v2), "Divide positive")

        val v3 = Vec2f(-8f, -15f)
        val v4 = Vec2f(4f, -5f)
        assertVec2fEqualsApproximately(Vec2f(-2f, 3f), v3.divide(v4), "Divide mixed signs")

        val v5 = Vec2f(0f, 0f)
        val v6 = Vec2f(10f, 20f)
        assertVec2fEqualsApproximately(Vec2f(0f, 0f), v5.divide(v6), "Divide zero by vector")

        // Division by zero results in Infinity
        val v7 = Vec2f(10f, 20f)
        val v8 = Vec2f(0f, 5f)
        val divResult = v7.divide(v8)
        assertTrue(divResult.x.isInfinite() && divResult.x > 0, "Divide by zero x")
        assertEquals(4f, divResult.y, "Divide by zero y")
    }


    @Test
    fun testSetLength() {
        val v1 = Vec2f(3f, 4f) // Length 5
        assertVec2fEqualsApproximately(Vec2f(6f, 8f), v1.setLength(10f), "SetLength longer")

        val v2 = Vec2f(5f, 12f) // Length 13
        assertVec2fEqualsApproximately(Vec2f(5f / 13f * 5f, 12f / 13f * 5f), v2.setLength(5f), "SetLength shorter")

        val v3 = Vec2f(0f, 0f)
        assertVec2fEqualsApproximately(Vec2f(0f, 0f), v3.setLength(10f), "SetLength zero vector")
    }

    @Test
    fun testTruncate() {
        val v1 = Vec2f(3f, 4f) // Length 5
        assertVec2fEqualsApproximately(Vec2f(3f, 4f), v1.truncate(10f), "Truncate within maxLen")

        val v2 = Vec2f(5f, 12f) // Length 13
        val truncated2 = v2.truncate(5f)
        assertVec2fEqualsApproximately(Vec2f(5f / 13f * 5f, 12f / 13f * 5f), truncated2, "Truncate outside maxLen")
        assertEquals(5f, truncated2.length, EPSILON, "Truncated length")


        val v3 = Vec2f(0f, 0f)
        assertVec2fEqualsApproximately(Vec2f(0f, 0f), v3.truncate(10f), "Truncate zero vector")
    }

    @Test
    fun testMidpoint() {
        val v1 = Vec2f(0f, 0f)
        val v2 = Vec2f(10f, 20f)
        assertVec2fEqualsApproximately(Vec2f(5f, 10f), v1.midpoint(v2), "Midpoint origin to positive")

        val v3 = Vec2f(-10f, 5f)
        val v4 = Vec2f(20f, -15f)
        assertVec2fEqualsApproximately(Vec2f(5f, -5f), v3.midpoint(v4), "Midpoint mixed signs")

        val v5 = Vec2f(5f, 5f)
        assertVec2fEqualsApproximately(Vec2f(5f, 5f), v5.midpoint(v5), "Midpoint same point")
    }

    // --- Companion Object Tests ---

    @Test
    fun testCreate() {
        val v1 = Vec2f.create()
        assertVec2fEqualsApproximately(Vec2f(0f, 0f), v1, "Create default")

        val v2 = Vec2f.create(1f, 2f)
        assertVec2fEqualsApproximately(Vec2f(1f, 2f), v2, "Create with values")
    }

    @Test
    fun testFromValues() { // Alias for create
        val v1 = Vec2f.fromValues()
        assertVec2fEqualsApproximately(Vec2f(0f, 0f), v1, "FromValues default")

        val v2 = Vec2f.fromValues(1f, 2f)
        assertVec2fEqualsApproximately(Vec2f(1f, 2f), v2, "FromValues with values")
    }

    @Test
    fun testRandom() {
        val scale = 5f
        val v1 = Vec2f.random(scale)
        assertEquals(scale, v1.length, EPSILON, "Random vector length")

        val v2 = Vec2f.random() // Default scale 1
        assertEquals(1f, v2.length, EPSILON, "Random vector default length")

        // Test dst parameter
        val dst = Vec2f()
        val v3 = Vec2f.random(10f, dst)
        assertSame(dst, v3, "Random uses dst instance")
        assertEquals(10f, dst.length, EPSILON, "Random dst length")
    }

    @Test
    fun testZero() {
        val v1 = Vec2f.zero()
        assertVec2fEqualsApproximately(Vec2f(0f, 0f), v1, "Zero default")

        // Test dst parameter
        val dst = Vec2f(1f, 1f)
        val v2 = Vec2f.zero(dst)
        assertSame(dst, v2, "Zero uses dst instance")
        assertVec2fEqualsApproximately(Vec2f(0f, 0f), dst, "Zero sets dst to zero")
    }
}


