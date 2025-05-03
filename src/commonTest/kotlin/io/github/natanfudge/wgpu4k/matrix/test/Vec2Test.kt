package io.github.natanfudge.wgpu4k.matrix.test

import io.github.natanfudge.wgpu4k.matrix.Vec2f
import io.github.natanfudge.wgpu4k.matrix.EPSILON
import kotlin.math.abs
import kotlin.test.*

class Vec2fOperatorTest {

    private fun assertVec2fEqualsApproximately(expected: Vec2f, actual: Vec2f, message: String? = null) {
        val deltaX = abs(expected.x - actual.x)
        val deltaY = abs(expected.y - actual.y)
        assertTrue(deltaX < EPSILON && deltaY < EPSILON,
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
        // val v4 = Vec2f(5f, 10f)
        // val s4 = 0f
        // assertFailsWith<ArithmeticException> { v4 / s4 } // Or expect Infinity depending on desired behavior
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
}