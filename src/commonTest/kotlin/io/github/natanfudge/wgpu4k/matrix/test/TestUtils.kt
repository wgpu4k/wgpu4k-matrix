package io.github.natanfudge.wgpu4k.matrix.test

import io.github.natanfudge.wgpu4k.matrix.Quat
import io.github.natanfudge.wgpu4k.matrix.Vec3
import io.github.natanfudge.wgpu4k.matrix.Vec4
import kotlin.math.abs
import kotlin.test.assertEquals
import kotlin.test.assertTrue

// Removed all helper functions and constants.
// Assuming they are provided by dependencies or other implicit sources. 

// --- Global Test Constants ---
const val TEST_EPSILON_D = 1e-6 // Tolerance for Double comparisons
const val TEST_EPSILON_F = 1e-5f // Tolerance for Float comparisons

// --- Assertion Helpers ---

// Basic Double comparison
fun assertEqualsApproximately(expected: Double, actual: Double, tolerance: Double = TEST_EPSILON_D, message: String? = null) {
    val delta = abs(expected - actual)
    assertTrue(delta < tolerance, message ?: "Doubles not approximately equal. Expected $expected, got $actual, delta $delta > tolerance $tolerance")
}

// Basic Float comparison
fun assertEqualsApproximately(expected: Float, actual: Float, tolerance: Float = TEST_EPSILON_F, message: String? = null) {
    val delta = abs(expected - actual)
    assertTrue(delta < tolerance, message ?: "Floats not approximately equal. Expected $expected, got $actual, delta $delta > tolerance $tolerance")
}

// Vec3 comparison (assuming Vec3 uses Float)
fun assertVec3Equals(expected: Vec3, actual: Vec3, message: String? = null) {
    val msg = message ?: "Expected Vec3 $expected, got $actual"
    assertEquals(expected.x, actual.x, "$msg (x mismatch)")
    assertEquals(expected.y, actual.y, "$msg (y mismatch)")
    assertEquals(expected.z, actual.z, "$msg (z mismatch)")
}

fun assertVec3EqualsApproximately(expected: Vec3, actual: Vec3, tolerance: Float = TEST_EPSILON_F, message: String? = null) {
    val msgPrefix = message ?: "Expected Vec3 approx $expected, got $actual."
    assertTrue(abs(expected.x - actual.x) < tolerance, "$msgPrefix x diff: ${abs(expected.x - actual.x)}")
    assertTrue(abs(expected.y - actual.y) < tolerance, "$msgPrefix y diff: ${abs(expected.y - actual.y)}")
    assertTrue(abs(expected.z - actual.z) < tolerance, "$msgPrefix z diff: ${abs(expected.z - actual.z)}")
}

// Vec3 vs FloatArray comparison
fun assertVec3EqualsApproximately(expected: FloatArray, actual: Vec3, tolerance: Float = TEST_EPSILON_F, message: String? = null) {
    assertEquals(3, expected.size, "Expected FloatArray must have size 3")
    val vecExpected = Vec3(expected[0], expected[1], expected[2])
    val msgPrefix = message ?: "Expected approx ${vecExpected}, got $actual."
    assertTrue(abs(expected[0] - actual.x) < tolerance, "$msgPrefix x diff: ${abs(expected[0] - actual.x)}")
    assertTrue(abs(expected[1] - actual.y) < tolerance, "$msgPrefix y diff: ${abs(expected[1] - actual.y)}")
    assertTrue(abs(expected[2] - actual.z) < tolerance, "$msgPrefix z diff: ${abs(expected[2] - actual.z)}")
}


// Quat comparison (using Double)
fun assertQuatEquals(expected: Quat, actual: Quat, message: String? = null) {
    val msg = message ?: "Expected Quat $expected, got $actual"
    assertEquals(expected.x, actual.x, "$msg (x mismatch)")
    assertEquals(expected.y, actual.y, "$msg (y mismatch)")
    assertEquals(expected.z, actual.z, "$msg (z mismatch)")
    assertEquals(expected.w, actual.w, "$msg (w mismatch)")
}

fun assertQuatEqualsApproximately(expected: Quat, actual: Quat, tolerance: Double = TEST_EPSILON_D, message: String? = null) {
    val msgPrefix = message ?: "Expected Quat approx $expected, got $actual."
    assertTrue(abs(expected.x - actual.x) < tolerance, "$msgPrefix x diff: ${abs(expected.x - actual.x)}")
    assertTrue(abs(expected.y - actual.y) < tolerance, "$msgPrefix y diff: ${abs(expected.y - actual.y)}")
    assertTrue(abs(expected.z - actual.z) < tolerance, "$msgPrefix z diff: ${abs(expected.z - actual.z)}")
    assertTrue(abs(expected.w - actual.w) < tolerance, "$msgPrefix w diff: ${abs(expected.w - actual.w)}")
}

// Quat vs DoubleArray comparison
fun assertQuatEqualsApproximately(expected: DoubleArray, actual: Quat, tolerance: Double = TEST_EPSILON_D, message: String? = null) {
    assertEquals(4, expected.size, "Expected DoubleArray must have size 4")
    val quatExpected = Quat(expected[0], expected[1], expected[2], expected[3])
    val msgPrefix = message ?: "Expected Quat approx ${quatExpected}, got $actual."
    assertTrue(abs(expected[0] - actual.x) < tolerance, "$msgPrefix x diff: ${abs(expected[0] - actual.x)}")
    assertTrue(abs(expected[1] - actual.y) < tolerance, "$msgPrefix y diff: ${abs(expected[1] - actual.y)}")
    assertTrue(abs(expected[2] - actual.z) < tolerance, "$msgPrefix z diff: ${abs(expected[2] - actual.z)}")
    assertTrue(abs(expected[3] - actual.w) < tolerance, "$msgPrefix w diff: ${abs(expected[3] - actual.w)}")
}

// General FloatArray comparison
fun assertArrayEqualsApproximately(expected: FloatArray, actual: FloatArray, tolerance: Float = TEST_EPSILON_F, message: String? = null) {
    assertEquals(expected.size, actual.size, message ?: "Array sizes differ.")
    for (i in expected.indices) {
        assertTrue(abs(expected[i] - actual[i]) < tolerance, "${message ?: ""} Index $i mismatch. Expected ${expected[i]}, got ${actual[i]}")
    }
}

// --- Vec4 Helpers (Additions from Vec4Test.kt) ---

// Vec4 comparison (assuming Vec4 uses Double)
fun assertVec4Equals(expected: Vec4, actual: Vec4, message: String? = null) {
    val msg = message ?: "Expected Vec4 $expected, got $actual"
    assertEquals(expected.x, actual.x, "$msg (x mismatch)")
    assertEquals(expected.y, actual.y, "$msg (y mismatch)")
    assertEquals(expected.z, actual.z, "$msg (z mismatch)")
    assertEquals(expected.w, actual.w, "$msg (w mismatch)")
}

fun assertVec4EqualsApproximately(expected: Vec4, actual: Vec4, tolerance: Double = TEST_EPSILON_D, message: String? = null) {
    val msgPrefix = message ?: "Expected Vec4 approx $expected, got $actual."
    assertTrue(abs(expected.x - actual.x) < tolerance, "$msgPrefix x diff: ${abs(expected.x - actual.x)}")
    assertTrue(abs(expected.y - actual.y) < tolerance, "$msgPrefix y diff: ${abs(expected.y - actual.y)}")
    assertTrue(abs(expected.z - actual.z) < tolerance, "$msgPrefix z diff: ${abs(expected.z - actual.z)}")
    assertTrue(abs(expected.w - actual.w) < tolerance, "$msgPrefix w diff: ${abs(expected.w - actual.w)}")
}

// Vec4 vs DoubleArray comparison
fun assertVec4EqualsApproximately(expected: DoubleArray, actual: Vec4, tolerance: Double = TEST_EPSILON_D, message: String? = null) {
    assertEquals(4, expected.size, "Expected DoubleArray must have size 4")
    val vecExpected = Vec4(expected[0], expected[1], expected[2], expected[3])
    val msgPrefix = message ?: "Expected Vec4 approx $vecExpected, got $actual."
    assertTrue(abs(expected[0] - actual.x) < tolerance, "$msgPrefix x diff: ${abs(expected[0] - actual.x)}")
    assertTrue(abs(expected[1] - actual.y) < tolerance, "$msgPrefix y diff: ${abs(expected[1] - actual.y)}")
    assertTrue(abs(expected[2] - actual.z) < tolerance, "$msgPrefix z diff: ${abs(expected[2] - actual.z)}")
    assertTrue(abs(expected[3] - actual.w) < tolerance, "$msgPrefix w diff: ${abs(expected[3] - actual.w)}")
} 