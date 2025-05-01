package io.github.natanfudge.wgpu4k.matrix.test

import io.github.natanfudge.wgpu4k.matrix.EPSILON
import io.github.natanfudge.wgpu4k.matrix.Mat3
import io.github.natanfudge.wgpu4k.matrix.Mat4
import io.github.natanfudge.wgpu4k.matrix.Quat
import io.github.natanfudge.wgpu4k.matrix.Vec2
import io.github.natanfudge.wgpu4k.matrix.Vec3
import kotlin.test.*
import kotlin.math.*

// Assuming Mat3, Mat3Utils, Vec2, Vec3, Vec2Arg, Vec3Arg, Mat4Arg are defined as in the previous response.
// If not, copy them here.



// Helper assertion functions
fun assertMat3EqualApproximately(actual: Mat3, expected: Mat3, message: String? = null) {
    if (!actual.equalsApproximately(expected)) {
        val errorMessage = "$message: Expected Mat3 <${expected.toFloatArray().joinToString()}> but was <${actual.toFloatArray().joinToString()}> (approximately)"
        fail(errorMessage)
    }
}

fun assertMat3Equal(actual: Mat3, expected: Mat3, message: String? = null) {
    if (actual != expected) { // Uses the overridden equals operator
        val errorMessage = message ?: "Expected Mat3 <${expected.toFloatArray().joinToString()}> but was <${actual.toFloatArray().joinToString()}> (exactly)"
        fail(errorMessage)
    }
}

fun assertVec2EqualApproximately(actual: Vec2, expected: Vec2, message: String? = null) {
        if (abs(actual.x - expected.x) >= EPSILON) {
            val errorMessage = message ?: "Vec2s are not approximately equal at x. Expected ${expected.x} but was ${expected.x}"
            fail(errorMessage)
        }
        if (abs(actual.y - expected.y) >= EPSILON) {
            val errorMessage = message ?: "Vec2s are not approximately equal at y. Expected ${expected.y} but was ${expected.y}"
            fail(errorMessage)
        }

}

fun assertFloatArrayEqualApproximately(actual: FloatArray, expected: FloatArray, message: String? = null) {
    if (actual.size != expected.size) {
        fail("Array sizes do not match. Expected ${expected.size} but was ${actual.size}")
    }
    for (i in actual.indices) {
        if (abs(actual[i] - expected[i]) >= EPSILON) {
            val errorMessage = message ?: "Arrays are not approximately equal at index $i. Expected ${expected[i]} but was ${actual[i]}"
            fail(errorMessage)
        }
    }
}

@Suppress("DuplicatedCode") // Helper test functions will have similar structure
class Mat3Test {

    // The base matrix 'm' from the JavaScript test
    private val m = Mat3.fromFloatArray(floatArrayOf(
        0f,  1f,  2f,  0f,
        4f,  5f,  6f,  0f,
        8f,  9f, 10f,  0f
    ))

    // Helper function to test Mat3 functions that return a Mat3
    private fun testMat3(
        func: (dst: Mat3) -> Mat3,
        expected: Mat3,
        message: String? = null
    ) {
        // Test with destination
        val dest = Mat3() // Create a new destination matrix
        val resultWithDest = func(dest)
        assertStrictEquals(resultWithDest, dest, "$message - with dest: returned object is not the destination")
        assertMat3EqualApproximately(resultWithDest, expected, "$message - with dest")
    }




    @Test
    fun testCreate() {
        val tests = listOf(
            Mat3.fromFloatArray(floatArrayOf(0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f)) to emptyList<Float>(),
            Mat3.fromFloatArray(floatArrayOf(1f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f)) to listOf(1f),
            Mat3.fromFloatArray(floatArrayOf(1f, 2f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f)) to listOf(1f, 2f),
            Mat3.fromFloatArray(floatArrayOf(1f, 2f, 3f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f)) to listOf(1f, 2f, 3f),
            Mat3.fromFloatArray(floatArrayOf(1f, 2f, 3f, 0f, 4f, 0f, 0f, 0f, 0f, 0f, 0f, 0f)) to listOf(1f, 2f, 3f, 4f),
            Mat3.fromFloatArray(floatArrayOf(1f, 2f, 3f, 0f, 4f, 5f, 0f, 0f, 0f, 0f, 0f, 0f)) to listOf(1f, 2f, 3f, 4f, 5f),
            Mat3.fromFloatArray(floatArrayOf(1f, 2f, 3f, 0f, 4f, 5f, 6f, 0f, 0f, 0f, 0f, 0f)) to listOf(1f, 2f, 3f, 4f, 5f, 6f),
            Mat3.fromFloatArray(floatArrayOf(1f, 2f, 3f, 0f, 4f, 5f, 6f, 0f, 7f, 0f, 0f, 0f)) to listOf(1f, 2f, 3f, 4f, 5f, 6f, 7f),
            Mat3.fromFloatArray(floatArrayOf(1f, 2f, 3f, 0f, 4f, 5f, 6f, 0f, 7f, 8f, 0f, 0f)) to listOf(1f, 2f, 3f, 4f, 5f, 6f, 7f, 8f),
            Mat3.fromFloatArray(floatArrayOf(1f, 2f, 3f, 0f, 4f, 5f, 6f, 0f, 7f, 8f, 9f, 0f)) to listOf(1f, 2f, 3f, 4f, 5f, 6f, 7f, 8f, 9f)
        )
        for ((expected, args) in tests) {
            val actual = when (args.size) {
                0 -> Mat3()
                1 -> Mat3(v0 = args[0])
                2 -> Mat3(v0 = args[0], v1 = args[1])
                3 -> Mat3(v0 = args[0], v1 = args[1], v2 = args[2])
                4 -> Mat3(v0 = args[0], v1 = args[1], v2 = args[2], v3 = args[3])
                5 -> Mat3(v0 = args[0], v1 = args[1], v2 = args[2], v3 = args[3], v4 = args[4])
                6 -> Mat3(v0 = args[0], v1 = args[1], v2 = args[2], v3 = args[3], v4 = args[4], v5 = args[5])
                7 -> Mat3(v0 = args[0], v1 = args[1], v2 = args[2], v3 = args[3], v4 = args[4], v5 = args[5], v6 = args[6])
                8 -> Mat3(v0 = args[0], v1 = args[1], v2 = args[2], v3 = args[3], v4 = args[4], v5 = args[5], v6 = args[6], v7 = args[7])
                9 -> Mat3(v0 = args[0], v1 = args[1], v2 = args[2], v3 = args[3], v4 = args[4], v5 = args[5], v6 = args[6], v7 = args[7], v8 = args[8])
                else -> throw IllegalArgumentException("Too many arguments for Mat3 create test")
            }
            assertMat3EqualApproximately(actual, expected)
        }
    }

    @Test
    fun testNegate() {
        val expected = Mat3.fromFloatArray(floatArrayOf(
            -0f,  -1f,  -2f,  0f,
            -4f,  -5f,  -6f,  0f,
            -8f,  -9f, -10f,  0f
        ))
        testMat3({ dst -> m.negate(dst) }, expected)
    }

    @Test
    fun testAdd() {
        val expected = Mat3.fromFloatArray(floatArrayOf(
            0f,  2f,  4f,  0f,
            8f, 10f, 12f,  0f,
            16f, 18f, 20f,  0f
        ))
        testMat3({ dst -> m.add(m, dst) }, expected)
    }

    @Test
    fun testMultiplyScalar() {
        val expected = Mat3.fromFloatArray(floatArrayOf(
            0f,  2f,  4f,  0f,
            8f, 10f, 12f,  0f,
            16f, 18f, 20f,  0f
        ))
        testMat3({ dst -> m.multiplyScalar(2f, dst) }, expected)
    }

    @Test
    fun testCopy() {
        val expected = m.clone() // Expected is a copy of m
        testMat3({ dst ->
            val result = m.copy(dst)
            assertNotSame(result, m, "Result should not be the same object as the source")
            result
        }, expected)
    }

    @Test
    fun testEqualsApproximately() {
        // Helper to generate a matrix with slightly different values
        fun genAlmostEqualMat(ignoreIndex: Int) = FloatArray(12) { ndx ->
            if (ndx == ignoreIndex || ndx == 3 || ndx == 7 || ndx == 11) ndx.toFloat() else ndx.toFloat() + EPSILON * 0.5f
        }

        // Helper to generate a matrix with significantly different values
        fun genNotAlmostEqualMat(diffIndex: Int) = FloatArray(12) { ndx ->
            if (ndx == diffIndex || ndx == 3 || ndx == 7 || ndx == 11) ndx.toFloat() else ndx.toFloat() + 1.0001f
        }

        // Indices relevant for Mat3 equality (0-2, 4-6, 8-10)
        val relevantIndices = listOf(0, 1, 2, 4, 5, 6, 8, 9, 10)

        for (i in relevantIndices.indices) {
            val idxToDiff = relevantIndices[i]
            assertTrue(
                Mat3.fromFloatArray(genAlmostEqualMat(-1)).equalsApproximately(
                    Mat3.fromFloatArray(genAlmostEqualMat(idxToDiff))
                ),
                "Should be approximately equal when differing by small amount at index $idxToDiff"
            )
            assertTrue(
                !Mat3.fromFloatArray(genNotAlmostEqualMat(-1)).equalsApproximately(
                    Mat3.fromFloatArray(genNotAlmostEqualMat(idxToDiff))
                ),
                 "Should not be approximately equal when differing by large amount at index $idxToDiff"
            )
        }
    }

    @Test
    fun testEquals() {
        // Helper to generate a matrix with significantly different values
        fun genNotEqualMat(diffIndex: Int) = FloatArray(12) { ndx ->
            if (ndx == diffIndex || ndx == 3 || ndx == 7 || ndx == 11) ndx.toFloat() else ndx.toFloat() + 1.0001f
        }

        // Indices relevant for Mat3 equality (0-2, 4-6, 8-10)
        val relevantIndices = listOf(0, 1, 2, 4, 5, 6, 8, 9, 10)

        for (i in relevantIndices.indices) {
            val idxToDiff = relevantIndices[i]
            assertTrue(
                Mat3.fromFloatArray(genNotEqualMat(idxToDiff)) == // Uses the overridden equals operator
                        Mat3.fromFloatArray(genNotEqualMat(idxToDiff)),
                 "Should be exactly equal when values are the same at index $idxToDiff"
            )
            assertTrue(
                Mat3.fromFloatArray(genNotEqualMat(-1)) != // Uses the overridden equals operator
                        Mat3.fromFloatArray(genNotEqualMat(idxToDiff)),
                "Should not be exactly equal when values are different at index $idxToDiff"
            )
        }
    }

    @Test
    fun testClone() {
        val expected = m.clone() // Expected is a clone of m
        testMat3({ dst ->
            val result = m.clone(dst)
            assertNotSame(result, m, "Result should not be the same object as the source")
            result
        }, expected)
    }

    @Test
    fun testSet() {
        val expected = Mat3.fromFloatArray(floatArrayOf(2f, 3f, 4f, 0f, 22f, 33f, 44f, 0f, 222f, 333f, 444f, 0f))
        testMat3({ dst ->
            val targetMat = dst ?: Mat3()
            targetMat.set(2f, 3f, 4f, 22f, 33f, 44f, 222f, 333f, 444f)
        }, expected)
    }

    @Test
    fun testIdentity() {
        val expected = Mat3.fromFloatArray(floatArrayOf(
            1f, 0f, 0f, 0f,
            0f, 1f, 0f, 0f,
            0f, 0f, 1f, 0f
        ))
        testMat3({ dst -> Mat3.identity(dst) }, expected)
    }

    @Test
    fun testTranspose() {
        val expected = Mat3.fromFloatArray(floatArrayOf(
            0f, 4f, 8f, 0f,
            1f, 5f, 9f, 0f,
            2f, 6f, 10f, 0f
        ))
        testMat3({ dst -> m.transpose(dst) }, expected)
    }

    private fun testMultiply(fn: (a: Mat3, b: Mat3, dst: Mat3) -> Mat3) {
        val m2 = Mat3.fromFloatArray(floatArrayOf(
            4f,  5f,  6f, 0f,
            1f,  2f,  3f, 0f,
            9f, 10f, 11f, 0f
        ))
        val expected = Mat3.fromFloatArray(floatArrayOf(
            m2.toFloatArray()[0 * 4 + 0] * m.toFloatArray()[0 * 4 + 0] + m2.toFloatArray()[0 * 4 + 1] * m.toFloatArray()[1 * 4 + 0] + m2.toFloatArray()[0 * 4 + 2] * m.toFloatArray()[2 * 4 + 0],
            m2.toFloatArray()[0 * 4 + 0] * m.toFloatArray()[0 * 4 + 1] + m2.toFloatArray()[0 * 4 + 1] * m.toFloatArray()[1 * 4 + 1] + m2.toFloatArray()[0 * 4 + 2] * m.toFloatArray()[2 * 4 + 1],
            m2.toFloatArray()[0 * 4 + 0] * m.toFloatArray()[0 * 4 + 2] + m2.toFloatArray()[0 * 4 + 1] * m.toFloatArray()[1 * 4 + 2] + m2.toFloatArray()[0 * 4 + 2] * m.toFloatArray()[2 * 4 + 2],
            0f, // col 3
            m2.toFloatArray()[1 * 4 + 0] * m.toFloatArray()[0 * 4 + 0] + m2.toFloatArray()[1 * 4 + 1] * m.toFloatArray()[1 * 4 + 0] + m2.toFloatArray()[1 * 4 + 2] * m.toFloatArray()[2 * 4 + 0],
            m2.toFloatArray()[1 * 4 + 0] * m.toFloatArray()[0 * 4 + 1] + m2.toFloatArray()[1 * 4 + 1] * m.toFloatArray()[1 * 4 + 1] + m2.toFloatArray()[1 * 4 + 2] * m.toFloatArray()[2 * 4 + 1],
            m2.toFloatArray()[1 * 4 + 0] * m.toFloatArray()[0 * 4 + 2] + m2.toFloatArray()[1 * 4 + 1] * m.toFloatArray()[1 * 4 + 2] + m2.toFloatArray()[1 * 4 + 2] * m.toFloatArray()[2 * 4 + 2],
            0f, // col 3
            m2.toFloatArray()[2 * 4 + 0] * m.toFloatArray()[0 * 4 + 0] + m2.toFloatArray()[2 * 4 + 1] * m.toFloatArray()[1 * 4 + 0] + m2.toFloatArray()[2 * 4 + 2] * m.toFloatArray()[2 * 4 + 0],
            m2.toFloatArray()[2 * 4 + 0] * m.toFloatArray()[0 * 4 + 1] + m2.toFloatArray()[2 * 4 + 1] * m.toFloatArray()[1 * 4 + 1] + m2.toFloatArray()[2 * 4 + 2] * m.toFloatArray()[2 * 4 + 1],
            m2.toFloatArray()[2 * 4 + 0] * m.toFloatArray()[0 * 4 + 2] + m2.toFloatArray()[2 * 4 + 1] * m.toFloatArray()[1 * 4 + 2] + m2.toFloatArray()[2 * 4 + 2] * m.toFloatArray()[2 * 4 + 2],
            0f // col 3
        ))
        testMat3({ dst -> fn(m, m2, dst) }, expected)
    }

    @Test
    fun testMultiply() {
        testMultiply({ a, b, dst -> a.multiply(b, dst) })
    }

    @Test
    fun testMul() {
        testMultiply({ a, b, dst -> a.mul(b, dst) })
    }

    private fun testInverse(fn: (m: Mat3, dst: Mat3) -> Mat3) {
        val tests = listOf(
            Mat3.fromFloatArray(floatArrayOf(
                2f, 1f, 3f, 0f,
                1f, 2f, 1f, 0f,
                3f, 1f, 2f, 0f
            )) to Mat3.fromFloatArray(floatArrayOf(
                -0.375f, -0.125f,  0.625f, 0f,
                -0.125f,  0.625f, -0.125f, 0f,
                0.625f, -0.125f, -0.375f, 0f
            )),
            Mat3.fromFloatArray(floatArrayOf(
                1f, 0f, 0f, 0f,
                0f, 1f, 0f, 0f,
                2f, 3f, 4f, 0f
            )) to Mat3.fromFloatArray(floatArrayOf(
                1f, 0f, 0f, 0f,
                0f, 1f, 0f, 0f,
                -0.5f, -0.75f, 0.25f, 0f
            )),
            Mat3.fromFloatArray(floatArrayOf(
                1f, 0f, 0f, 0f,
                0f, 1f, 0f, 0f,
                -0.5f, -0.75f, 0.25f, 0f
            )) to Mat3.fromFloatArray(floatArrayOf(
                1f, 0f, 0f, 0f,
                0f, 1f, 0f, 0f,
                2f, 3f, 4f, 0f
            ))
        )
        for ((inputM, expected) in tests) {
            testMat3({ dst -> fn(inputM, dst) }, expected)
        }
    }

    @Test
    fun testInverse() {
        testInverse({ m, dst -> m.inverse(dst) })
    }

    @Test
    fun testInvert() {
        testInverse({ m, dst -> m.invert(dst) })
    }

    @Test
    fun testDeterminant() {
        val tests = listOf(
            Mat3.fromFloatArray(floatArrayOf(
                2f, 1f, 3f, 0f,
                1f, 2f, 1f, 0f,
                3f, 1f, 2f, 0f
            )) to -8f,
            Mat3.fromFloatArray(floatArrayOf(
                2f, 0f, 0f, 0f,
                0f, 3f, 0f, 0f,
                0f, 0f, 4f, 0f
            )) to 24f // 2 * 3 * 4 = 24
        )
        for ((inputM, expectedDet) in tests) {
            assertEquals(inputM.determinant(), expectedDet, EPSILON)
        }
    }

    @Test
    fun testSetTranslation() {
        // Expected: <0.0, 1.0, 2.0, 0.0, 4.0, 5.0, 6.0, 0.0, 11.0, 22.0, 1.0, 0.0>
        // Actual:   <1.0, 0.0, 0.0, 0.0, 0.0, 1.0, 0.0, 0.0, 11.0, 22.0, 1.0, 0.0>
        val expected = Mat3.fromFloatArray(floatArrayOf(
            0f,  1f,  2f, 0f,
            4f,  5f,  6f, 0f,
            11f, 22f,  1f, 0f // Note: the TS test has 1 here, which seems incorrect for a pure translation setting on Mat3 layout
        ))
        testMat3({ dst -> m.setTranslation(Vec2(11f, 22f), dst) }, expected)
    }

    @Test
    fun testGetTranslation() {
        val expected = Vec2(8f, 9f)
        testVec2({ dst -> m.getTranslation(dst) }, expected)
    }

    @Test
    fun testGetAxis() {
        val tests = listOf(
            0 to Vec2(0f, 1f), // X axis
            1 to Vec2(4f, 5f)  // Y axis
        )
        for ((axis, expected) in tests) {
            testVec2({ dst -> m.getAxis(axis, dst) }, expected, "getAxis($axis)")
        }
    }

    @Test
    fun testSetAxis() {
        val tests = listOf(
            0 to Mat3.fromFloatArray(floatArrayOf(
                11f, 22f,  2f,  0f,
                4f,  5f,  6f,  0f,
                8f,  9f, 10f,  0f
            )),
            1 to Mat3.fromFloatArray(floatArrayOf(
                0f,  1f,  2f,  0f,
                11f, 22f,  6f,  0f,
                8f,  9f, 10f,  0f
            ))
        )
        val v = Vec2(11f, 22f)
        for ((axis, expected) in tests) {
            testMat3({ dst -> m.setAxis(v, axis, dst) }, expected, "setAxis($axis)")
        }
    }

    @Test
    fun testGetScaling() {
        val testM = Mat3.fromFloatArray(floatArrayOf(
            2f,  8f,  3f, 0f,
            5f,  6f,  7f, 0f,
            9f, 10f, 11f, 0f
        ))
        val expected = Vec2(
            sqrt(2f * 2f + 8f * 8f),
            sqrt(5f * 5f + 6f * 6f)
        )
        testVec2({ dst -> testM.getScaling(dst) }, expected)
    }

    @Test
    fun testGet3DScaling() {
        val testM = Mat3.fromFloatArray(floatArrayOf(
            1f,  2f,  3f, 4f,
            5f,  6f,  7f, 8f,
            9f, 10f, 11f, 12f
        ))
        val expected = Vec3(
            sqrt(1f * 1f + 2f * 2f + 3f * 3f),
            sqrt(5f * 5f + 6f * 6f + 7f * 7f),
            sqrt(9f * 9f + 10f * 10f + 11f * 11f)
        )
        testVec3({ dst -> testM.get3DScaling(dst) }, expected)
    }

    @Test
    fun testMakeTranslationMatrix() {
        val expected = Mat3.fromFloatArray(floatArrayOf(
            1f, 0f, 0f, 0f,
            0f, 1f, 0f, 0f,
            2f, 3f, 1f, 0f
        ))
        testMat3({ dst -> Mat3.translation(Vec2(2f, 3f), dst) }, expected)
    }

    @Test
    fun testTranslate() {
        val expected = Mat3.fromFloatArray(floatArrayOf(
            0f,  1f,  2f,  0f,
            4f,  5f,  6f,  0f,
            8f + 0f * 2f + 4f * 3f,
            9f + 1f * 2f + 5f * 3f,
            10f + 2f * 2f + 6f * 3f, 0f
        ))
        testMat3({ dst -> m.translate(Vec2(2f, 3f), dst) }, expected)
    }

    @Test
    fun testMakeRotationMatrix() {
        val angle = 1.23f
        val c = cos(angle)
        val s = sin(angle)
        val expected = Mat3.fromFloatArray(floatArrayOf(
            c, s, 0f, 0f,
            -s, c, 0f, 0f,
            0f, 0f, 1f, 0f
        ))
        testMat3({ dst -> Mat3.rotation(angle, dst) }, expected)
    }

    @Test
    fun testRotate() {
        val angle = 1.23f
        // Calculate expected using multiplication, similar to the JS test
        val rotationMat = Mat3.rotation(angle)
        val expected = m.multiply(rotationMat)

        testMat3({ dst -> m.rotate(angle, dst) }, expected)
    }

    @Test
    fun testMakeRotationXMatrix() {
        val angle = 1.23f
        val c = cos(angle)
        val s = sin(angle)
        val expected = Mat3.fromFloatArray(floatArrayOf(
            1f,  0f, 0f, 0f,
            0f,  c, s, 0f,
            0f, -s, c, 0f
        ))
        testMat3({ dst -> Mat3.rotationX(angle, dst) }, expected)
    }

    @Test
    fun testRotateX() {
        val angle = 1.23f
        val rotationMat = Mat3.rotationX(angle)
        val expected = m.multiply(rotationMat)

        testMat3({ dst -> m.rotateX(angle, dst) }, expected)
    }

    @Test
    fun testMakeRotationYMatrix() {
        val angle = 1.23f
        val c = cos(angle)
        val s = sin(angle)
        val expected = Mat3.fromFloatArray(floatArrayOf(
            c, 0f, -s, 0f,
            0f, 1f,  0f, 0f,
            s, 0f,  c, 0f
        ))
        testMat3({ dst -> Mat3.rotationY(angle, dst) }, expected)
    }

    @Test
    fun testRotateY() {
        val angle = 1.23f
        val rotationMat = Mat3.rotationY(angle)
        val expected = m.multiply(rotationMat)

        testMat3({ dst -> m.rotateY(angle, dst) }, expected)
    }

    @Test
    fun testMakeRotationZMatrix() {
        val angle = 1.23f
        val c = cos(angle)
        val s = sin(angle)
        val expected = Mat3.fromFloatArray(floatArrayOf(
            c, s, 0f, 0f,
            -s, c, 0f, 0f,
            0f, 0f, 1f, 0f
        ))
        testMat3({ dst -> Mat3.rotationZ(angle, dst) }, expected)
    }

    @Test
    fun testRotateZ() {
        val angle = 1.23f
        val rotationMat = Mat3.rotationZ(angle)
        val expected = m.multiply(rotationMat)

        testMat3({ dst -> m.rotateZ(angle, dst) }, expected)
    }

    @Test
    fun testMakeScalingMatrix() {
        val expected = Mat3.fromFloatArray(floatArrayOf(
            2f, 0f, 0f, 0f,
            0f, 3f, 0f, 0f,
            0f, 0f, 1f, 0f
        ))
        testMat3({ dst -> Mat3.scaling(Vec2(2f, 3f), dst) }, expected)
    }

    @Test
    fun testScale() {
        val expected = Mat3.fromFloatArray(floatArrayOf(
            0f * 2f,  1f * 2f,  2f * 2f,  0f,
            4f * 3f,  5f * 3f,  6f * 3f,  0f,
            8f,  9f, 10f,  0f
        ))
        testMat3({ dst -> m.scale(Vec2(2f, 3f), dst) }, expected)
    }

    @Test
    fun testMake3DScalingMatrix() {
        val expected = Mat3.fromFloatArray(floatArrayOf(
            2f, 0f, 0f, 0f,
            0f, 3f, 0f, 0f,
            0f, 0f, 4f, 0f
        ))
        testMat3({ dst -> Mat3.scaling3D(Vec3(2f, 3f, 4f), dst) }, expected)
    }

    @Test
    fun testScale3D() {
        val expected = Mat3.fromFloatArray(floatArrayOf(
            0f * 2f,  1f * 2f,  2f * 2f,  0f,
            4f * 3f,  5f * 3f,  6f * 3f,  0f,
            8f * 4f,  9f * 4f, 10f * 4f,  0f
        ))
        testMat3({ dst -> m.scale3D(Vec3(2f, 3f, 4f), dst) }, expected)
    }

    @Test
    fun testMakeUniformScalingMatrix() {
        val expected = Mat3.fromFloatArray(floatArrayOf(
            2f, 0f, 0f, 0f,
            0f, 2f, 0f, 0f,
            0f, 0f, 1f, 0f
        ))
        testMat3({ dst -> Mat3.uniformScaling(2f, dst) }, expected)
    }

    @Test
    fun testUniformScale() {
        val s = 2f
        val expected = Mat3.fromFloatArray(floatArrayOf(
            0f * s,  1f * s,  2f * s,  0f,
            4f * s,  5f * s,  6f * s,  0f,
            8f,  9f, 10f,  0f
        ))
        testMat3({ dst -> m.uniformScale(s, dst) }, expected)
    }

    @Test
    fun testMakeUniformScaling3DMatrix() {
        val expected = Mat3.fromFloatArray(floatArrayOf(
            2f, 0f, 0f, 0f,
            0f, 2f, 0f, 0f,
            0f, 0f, 2f, 0f
        ))
        testMat3({ dst -> Mat3.uniformScaling3D(2f, dst) }, expected)
    }

    @Test
    fun testUniformScale3D() {
        val s = 2f
        val expected = Mat3.fromFloatArray(floatArrayOf(
            0f * s,  1f * s,  2f * s,  0f,
            4f * s,  5f * s,  6f * s,  0f,
            8f * s,  9f * s, 10f * s,  0f
        ))
        testMat3({ dst -> m.uniformScale3D(s, dst) }, expected)
    }

    @Test
    fun testFromMat4() {
        val m4 = Mat4(
            1f, 2f, 3f, 4f,
            5f, 6f, 7f, 8f,
            9f, 10f, 11f, 12f,
            13f, 14f, 15f, 16f
        )
        val expected = Mat3.fromFloatArray(floatArrayOf(
            1f, 2f, 3f, 0f,
            5f, 6f, 7f, 0f,
            9f, 10f, 11f, 0f
        ))
        testMat3({ dst -> Mat3.fromMat4(m4, dst) }, expected)
    }

    @Test
    fun testFromQuat() {
        val tests = listOf(
            Quat.fromEuler(PI, 0.0, 0.0, "xyz") to Mat3.fromMat4(Mat4.rotationX(PI.toFloat())),
            Quat.fromEuler(0.0, PI, 0.0, "xyz") to Mat3.fromMat4(Mat4.rotationY(PI.toFloat())),
            Quat.fromEuler(0.0, 0.0, PI, "xyz") to Mat3.fromMat4(Mat4.rotationZ(PI.toFloat())),
            Quat.fromEuler(PI / 2, 0.0, 0.0, "xyz") to Mat3.fromMat4(Mat4.rotationX(PI.toFloat() / 2f)),
            Quat.fromEuler(0.0, PI / 2f, 0.0, "xyz") to Mat3.fromMat4(Mat4.rotationY(PI.toFloat() / 2f)),
            Quat.fromEuler(0.0, 0.0, PI / 2f, "xyz") to Mat3.fromMat4(Mat4.rotationZ(PI.toFloat() / 2f))
        )
        for ((q, expected) in tests) {
            testMat3({ dst -> Mat3.fromQuat(q, dst) }, expected)
        }
    }
}