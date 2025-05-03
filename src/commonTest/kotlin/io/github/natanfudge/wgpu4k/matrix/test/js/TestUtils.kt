package io.github.natanfudge.wgpu4k.matrix.test.js

import io.github.natanfudge.wgpu4k.matrix.*
import kotlin.math.abs
import kotlin.test.assertEquals
import kotlin.test.assertTrue

// Removed all helper functions and constants.
// Assuming they are provided by dependencies or other implicit sources. 

// --- Global Test Constants ---
//const val TEST_EPSILON_D = 1e-6 // Tolerance for Double comparisons
//const val EPSILON = 1e-6f // Tolerance for Float comparisons

// --- Assertion Helpers ---

//// Basic Double comparison
//fun assertEqualsApproximately(expected: Float, actual: Float, tolerance: Float = EPSILON, message: String? = null) {
//    val delta = abs(expected - actual)
//    assertTrue(delta < tolerance, message ?: "Doubles not approximately equal. Expected $expected, got $actual, delta $delta > tolerance $tolerance")
//}

// Basic Float comparison
fun assertEqualsApproximately(expected: Float, actual: Float, tolerance: Float = EPSILON, message: String? = null) {
    val delta = abs(expected - actual)
    assertTrue(delta < tolerance, message ?: "Floats not approximately equal. Expected $expected, got $actual, delta $delta > tolerance $tolerance")
}

// Vec3 comparison (assuming Vec3 uses Float)
fun assertVec3Equals(expected: Vec3f, actual: Vec3f, message: String? = null) {
    val msg = message ?: "Expected Vec3 $expected, got $actual"
    assertEquals(expected.x, actual.x, "$msg (x mismatch)")
    assertEquals(expected.y, actual.y, "$msg (y mismatch)")
    assertEquals(expected.z, actual.z, "$msg (z mismatch)")
}

fun assertVec3EqualsApproximately(expected: Vec3f, actual: Vec3f, tolerance: Float = EPSILON, message: String? = null) {
    val msgPrefix = message ?: "Expected Vec3 approx $expected, got $actual."
    assertTrue(abs(expected.x - actual.x) < tolerance, "$msgPrefix x diff: ${abs(expected.x - actual.x)}")
    val ydiff = expected.y - actual.y
    assertTrue(abs(expected.y - actual.y) < tolerance, "$msgPrefix y diff: ${abs(expected.y - actual.y)}")
    assertTrue(abs(expected.z - actual.z) < tolerance, "$msgPrefix z diff: ${abs(expected.z - actual.z)}")
}

// Vec3 vs FloatArray comparison
fun assertVec3EqualsApproximately(expected: FloatArray, actual: Vec3f, tolerance: Float = EPSILON, message: String? = null) {
    assertEquals(3, expected.size, "Expected FloatArray must have size 3")
    val vecExpected = Vec3f(expected[0], expected[1], expected[2])
    val msgPrefix = message ?: "Expected approx ${vecExpected}, got $actual."
    assertTrue(abs(expected[0] - actual.x) < tolerance, "$msgPrefix x diff: ${abs(expected[0] - actual.x)}")
    assertTrue(abs(expected[1] - actual.y) < tolerance, "$msgPrefix y diff: ${abs(expected[1] - actual.y)}")
    assertTrue(abs(expected[2] - actual.z) < tolerance, "$msgPrefix z diff: ${abs(expected[2] - actual.z)}")
}


// Quat comparison (using Double)
fun assertQuatEquals(expected: Quatf, actual: Quatf, message: String? = null) {
    val msg = message ?: "Expected Quat $expected, got $actual"
    assertEquals(expected.x, actual.x, "$msg (x mismatch)")
    assertEquals(expected.y, actual.y, "$msg (y mismatch)")
    assertEquals(expected.z, actual.z, "$msg (z mismatch)")
    assertEquals(expected.w, actual.w, "$msg (w mismatch)")
}

fun assertQuatEqualsApproximately(expected: Quatf, actual: Quatf, tolerance: Float = EPSILON, message: String? = null) {
    val normalInequality = getQuatInequality(expected, actual, tolerance, message)
    val negInequality = getQuatInequality(expected, actual.neg(), tolerance, message)
    // q === -q for all quats
    if (normalInequality != null && negInequality != null) throw AssertionError(normalInequality)
}

private fun Quatf.neg() = Quatf(-x, -y, -z, -w)

private fun getQuatInequality(expected: Quatf, actual: Quatf, tolerance: Float = EPSILON, message: String? = null): String? {
    val msgPrefix = message ?: "Expected Quat approx $expected, got $actual."
    if (abs(expected.x - actual.x) >= tolerance) return "$msgPrefix x diff: ${abs(expected.x - actual.x)}"
    if (abs(expected.y - actual.y) >= tolerance) return "$msgPrefix y diff: ${abs(expected.y - actual.y)}"
    if (abs(expected.z - actual.z) >= tolerance) return "$msgPrefix z diff: ${abs(expected.z - actual.w)}"
    if (abs(expected.w - actual.w) >= tolerance) return "$msgPrefix w diff: ${abs(expected.w - actual.w)}"

    return null
}


// Quat vs DoubleArray comparison
fun assertQuatEqualsApproximately(expected: FloatArray, actual: Quatf, tolerance: Float = EPSILON, message: String? = null) {
    assertEquals(4, expected.size, "Expected DoubleArray must have size 4")
    val quatExpected = Quatf(expected[0], expected[1], expected[2], expected[3])
    val msgPrefix = message ?: "Expected Quat approx ${quatExpected}, got $actual."
    assertTrue(abs(expected[0] - actual.x) < tolerance, "$msgPrefix x diff: ${abs(expected[0] - actual.x)}")
    assertTrue(abs(expected[1] - actual.y) < tolerance, "$msgPrefix y diff: ${abs(expected[1] - actual.y)}")
    assertTrue(abs(expected[2] - actual.z) < tolerance, "$msgPrefix z diff: ${abs(expected[2] - actual.z)}")
    assertTrue(abs(expected[3] - actual.w) < tolerance, "$msgPrefix w diff: ${abs(expected[3] - actual.w)}")
}

// General FloatArray comparison
fun assertArrayEqualsApproximately(expected: FloatArray, actual: FloatArray, tolerance: Float = EPSILON, message: String? = null) {
    assertEquals(expected.size, actual.size, message ?: "Array sizes differ.")
    for (i in expected.indices) {
        assertTrue(abs(expected[i] - actual[i]) < tolerance, "${message ?: ""} Index $i mismatch. Expected ${expected[i]}, got ${actual[i]}")
    }
}

// --- Vec4 Helpers (Additions from Vec4JsTests.kt) ---

// Vec4 comparison (assuming Vec4 uses Double)
fun assertVec4Equals(expected: Vec4f, actual: Vec4f, message: String? = null) {
    val msg = message ?: "Expected Vec4 $expected, got $actual"
    assertEquals(expected.x, actual.x, "$msg (x mismatch)")
    assertEquals(expected.y, actual.y, "$msg (y mismatch)")
    assertEquals(expected.z, actual.z, "$msg (z mismatch)")
    assertEquals(expected.w, actual.w, "$msg (w mismatch)")
}

fun assertVec4EqualsApproximately(expected: Vec4f, actual: Vec4f, tolerance: Float = EPSILON, message: String? = null) {
    val msgPrefix = message ?: "Expected Vec4 approx $expected, got $actual."
    assertTrue(abs(expected.x - actual.x) < tolerance, "$msgPrefix x diff: ${abs(expected.x - actual.x)}")
    assertTrue(abs(expected.y - actual.y) < tolerance, "$msgPrefix y diff: ${abs(expected.y - actual.y)}")
    assertTrue(abs(expected.z - actual.z) < tolerance, "$msgPrefix z diff: ${abs(expected.z - actual.z)}")
    assertTrue(abs(expected.w - actual.w) < tolerance, "$msgPrefix w diff: ${abs(expected.w - actual.w)}")
}

// Vec4 vs DoubleArray comparison
fun assertVec4EqualsApproximately(expected: FloatArray, actual: Vec4f, tolerance: Float = EPSILON, message: String? = null) {
    assertEquals(4, expected.size, "Expected DoubleArray must have size 4")
    val vecExpected = Vec4f(expected[0], expected[1], expected[2], expected[3])
    val msgPrefix = message ?: "Expected Vec4 approx $vecExpected, got $actual."
    assertTrue(abs(expected[0] - actual.x) < tolerance, "$msgPrefix x diff: ${abs(expected[0] - actual.x)}")
    assertTrue(abs(expected[1] - actual.y) < tolerance, "$msgPrefix y diff: ${abs(expected[1] - actual.y)}")
    assertTrue(abs(expected[2] - actual.z) < tolerance, "$msgPrefix z diff: ${abs(expected[2] - actual.z)}")
    assertTrue(abs(expected[3] - actual.w) < tolerance, "$msgPrefix w diff: ${abs(expected[3] - actual.w)}")
}

// Helper function to test Mat3 functions that return a Vec2Arg (FloatArray)
fun testVec2(
    func: (dst: Vec2f) -> Vec2f,
    expected: Vec2f,
    message: String? = null,
) {
    // Test with destination
    val dest = Vec2f.create() // Create a new destination vector
    val resultWithDest = func(dest)
    assertStrictEquals(resultWithDest, dest, "$message - with dest: returned object is not the destination")
    assertVec2EqualApproximately(resultWithDest, expected, "$message - with dest")
}

// Helper function to test Mat3 functions that return a Vec3Arg (FloatArray)
fun testVec3(
    func: (dst: Vec3f) -> Vec3f,
    expected: Vec3f,
    message: String? = null,
) {
    // Test with destination
    val dest = Vec3f.create() // Create a new destination vector
    val resultWithDest = func(dest)
    assertStrictEquals(resultWithDest, dest, "$message - with dest: returned object is not the destination")
    assertVec3EqualsApproximately(resultWithDest, expected, message = "$message - with dest")
}