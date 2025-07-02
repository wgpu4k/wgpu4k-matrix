package io.github.natanfudge.wgpu4k.matrix.test.js

import io.github.natanfudge.wgpu4k.matrix.EPSILON
import io.github.natanfudge.wgpu4k.matrix.FloatPi
import io.github.natanfudge.wgpu4k.matrix.Mat4f
import io.github.natanfudge.wgpu4k.matrix.Quatf
import io.github.natanfudge.wgpu4k.matrix.Vec3f
import kotlin.math.*
import kotlin.test.*

// Helper assertion functions
fun assertMat4EqualApproximately(actual: Mat4f, expected: Mat4f, message: String? = null) {
    if (!actual.equalsApproximately(expected)) {
        val errorMessage = "$message: Expected Mat4 <${expected.toFloatArray().joinToString()}> but was <${actual.toFloatArray().joinToString()}> (approximately)"
        fail(errorMessage)
    }
}

fun assertVec3EqualApproximately(actual: Vec3f, expected: Vec3f, epsilon: Float = EPSILON, message: String? = null) {
    if (!actual.equalsApproximately(expected, epsilon)) {
        val errorMessage = "$message: Expected Vec3 <${expected.x}, ${expected.y}, ${expected.z}> but was <${actual.x}, ${actual.y}, ${actual.z}> (approximately)"
        fail(errorMessage)
    }
}

fun assertMat4Equal(actual: Mat4f, expected: Mat4f, message: String? = null) {
    if (actual != expected) { // Uses the overridden equals operator
        val errorMessage = message ?: "Expected Mat4 <${expected.toFloatArray().joinToString()}> but was <${actual.toFloatArray().joinToString()}> (exactly)"
        fail(errorMessage)
    }
}

fun formatTestMessage(message: String?): String {
    return message ?: ""
}

fun assertStrictEquals(actual: Any?, expected: Any?, message: String? = null) {
    assertSame(expected, actual, message)
}

class Mat4Test {

    // The base matrix 'm' from the JavaScript test
    private val m = Mat4f.copyOf(floatArrayOf(
        0f,  1f,  2f,  3f,
        4f,  5f,  6f,  7f,
        8f,  9f, 10f, 11f,
        12f, 13f, 14f, 15f
    ))

    // Helper function to test Mat4 functions that return a Mat4
    private fun testMat4(
        func: (dst: Mat4f) -> Mat4f,
        expected: Mat4f,
        message: String? = null
    ) {
        // Test with destination
        val dest = Mat4f() // Create a new destination matrix
        val resultWithDest = func(dest)
        assertStrictEquals(resultWithDest, dest, "$message - with dest: returned object is not the destination")
        assertMat4EqualApproximately(resultWithDest, expected, "$message - with dest")
    }

    // Helper function to test Vec3 functions
    private fun testVec3(
        func: (dst: Vec3f) -> Vec3f,
        expected: Vec3f,
        message: String? = null,
        epsilon: Float = EPSILON
    ) {
        // Test with destination
        val dest = Vec3f() // Create a new destination vector
        val resultWithDest = func(dest)
        assertStrictEquals(resultWithDest, dest, "$message - with dest: returned object is not the destination")
        assertVec3EqualApproximately(resultWithDest, expected, epsilon, "$message - with dest")
    }

    @Test
    fun testNegate() {
        val expected = Mat4f.copyOf(floatArrayOf(
            -0f,  -1f,  -2f,  -3f,
            -4f,  -5f,  -6f,  -7f,
            -8f,  -9f, -10f, -11f,
            -12f, -13f, -14f, -15f
        ))
        testMat4({ dst -> m.negate(dst) }, expected)
    }

    @Test
    fun testAdd() {
        val expected = Mat4f.copyOf(floatArrayOf(
            0f,  2f,  4f,  6f,
            8f, 10f, 12f, 14f,
            16f, 18f, 20f, 22f,
            24f, 26f, 28f, 30f
        ))
        testMat4({ dst -> m.add(m, dst) }, expected)
    }

    @Test
    fun testMultiplyScalar() {
        val expected = Mat4f.copyOf(floatArrayOf(
            0f,  2f,  4f,  6f,
            8f, 10f, 12f, 14f,
            16f, 18f, 20f, 22f,
            24f, 26f, 28f, 30f
        ))
        testMat4({ dst -> m.multiplyScalar(2f, dst) }, expected)
    }

    @Test
    fun testCopy() {
        val expected = m.clone() // Expected is a copy of m
        testMat4({ dst ->
            val result = m.copy(dst)
            assertNotSame(result, m, "Result should not be the same object as the source")
            result
        }, expected)
    }

    @Test
    fun testEqualsApproximately() {
        // Helper to generate a matrix with slightly different values
        fun genAlmostEqualMat(ignoreIndex: Int) = FloatArray(16) { ndx ->
            if (ndx == ignoreIndex) ndx.toFloat() else ndx.toFloat() + EPSILON * 0.5f
        }

        // Helper to generate a matrix with significantly different values
        fun genNotAlmostEqualMat(diffIndex: Int) = FloatArray(16) { ndx ->
            if (ndx == diffIndex) ndx.toFloat() else ndx.toFloat() + 1.0001f
        }

        for (i in 0..15) {
            assertTrue(
                Mat4f.copyOf(genAlmostEqualMat(-1)).equalsApproximately(
                    Mat4f.copyOf(genAlmostEqualMat(i))
                ),
                "Should be approximately equal when differing by small amount at index $i"
            )
            assertTrue(
                !Mat4f.copyOf(genNotAlmostEqualMat(-1)).equalsApproximately(
                    Mat4f.copyOf(genNotAlmostEqualMat(i))
                ),
                 "Should not be approximately equal when differing by large amount at index $i"
            )
        }
    }

    @Test
    fun testEquals() {
        // Helper to generate a matrix with significantly different values
        fun genNotEqualMat(diffIndex: Int) = FloatArray(16) { ndx ->
            if (ndx == diffIndex) ndx.toFloat() else ndx.toFloat() + 1.0001f
        }

        for (i in 0..15) {
            assertTrue(
                Mat4f.copyOf(genNotEqualMat(i)) == // Uses the overridden equals operator
                        Mat4f.copyOf(genNotEqualMat(i)),
                 "Should be exactly equal when values are the same at index $i"
            )
            assertTrue(
                Mat4f.copyOf(genNotEqualMat(-1)) != // Uses the overridden equals operator
                        Mat4f.copyOf(genNotEqualMat(i)),
                 "Should not be exactly equal when values are different at index $i"
            )
        }
    }

    @Test
    fun testClone() {
        val expected = m.clone() // Expected is a clone of m
        testMat4({ dst ->
            val result = m.clone(dst)
            assertNotSame(result, m, "Result should not be the same object as the source")
            result
        }, expected)
    }

    @Test
    fun testSet() {
        val expected = Mat4f.copyOf(floatArrayOf(
            2f, 3f, 4f, 5f, 
            22f, 33f, 44f, 55f, 
            222f, 333f, 444f, 555f, 
            2222f, 3333f, 4444f, 5555f
        ))
        testMat4({ dst ->
            val targetMat = dst ?: Mat4f()
            targetMat.set(
                2f, 3f, 4f, 5f,
                22f, 33f, 44f, 55f,
                222f, 333f, 444f, 555f,
                2222f, 3333f, 4444f, 5555f
            )
        }, expected)
    }

    @Test
    fun testIdentity() {
        val expected = Mat4f.copyOf(floatArrayOf(
            1f, 0f, 0f, 0f,
            0f, 1f, 0f, 0f,
            0f, 0f, 1f, 0f,
            0f, 0f, 0f, 1f
        ))
        testMat4({ dst -> Mat4f.identity(dst) }, expected)
    }

    @Test
    fun testTranspose() {
        val expected = Mat4f.copyOf(floatArrayOf(
            0f, 4f, 8f, 12f,
            1f, 5f, 9f, 13f,
            2f, 6f, 10f, 14f,
            3f, 7f, 11f, 15f
        ))
        testMat4({ dst -> m.transpose(dst) }, expected)
    }

    private fun testMultiply(fn: (a: Mat4f, b: Mat4f, dst: Mat4f) -> Mat4f) {
        val m2 = Mat4f.copyOf(floatArrayOf(
            4f, 5f, 6f, 7f,
            1f, 2f, 3f, 4f,
            9f, 10f, 11f, 12f,
            -1f, -2f, -3f, -4f
        ))

        // Calculate expected result using the formula from the JS test
        val expected = Mat4f.copyOf(floatArrayOf(
            m2[0] * m[0] + m2[1] * m[4] + m2[2] * m[8] + m2[3] * m[12],
            m2[0] * m[1] + m2[1] * m[5] + m2[2] * m[9] + m2[3] * m[13],
            m2[0] * m[2] + m2[1] * m[6] + m2[2] * m[10] + m2[3] * m[14],
            m2[0] * m[3] + m2[1] * m[7] + m2[2] * m[11] + m2[3] * m[15],

            m2[4] * m[0] + m2[5] * m[4] + m2[6] * m[8] + m2[7] * m[12],
            m2[4] * m[1] + m2[5] * m[5] + m2[6] * m[9] + m2[7] * m[13],
            m2[4] * m[2] + m2[5] * m[6] + m2[6] * m[10] + m2[7] * m[14],
            m2[4] * m[3] + m2[5] * m[7] + m2[6] * m[11] + m2[7] * m[15],

            m2[8] * m[0] + m2[9] * m[4] + m2[10] * m[8] + m2[11] * m[12],
            m2[8] * m[1] + m2[9] * m[5] + m2[10] * m[9] + m2[11] * m[13],
            m2[8] * m[2] + m2[9] * m[6] + m2[10] * m[10] + m2[11] * m[14],
            m2[8] * m[3] + m2[9] * m[7] + m2[10] * m[11] + m2[11] * m[15],

            m2[12] * m[0] + m2[13] * m[4] + m2[14] * m[8] + m2[15] * m[12],
            m2[12] * m[1] + m2[13] * m[5] + m2[14] * m[9] + m2[15] * m[13],
            m2[12] * m[2] + m2[13] * m[6] + m2[14] * m[10] + m2[15] * m[14],
            m2[12] * m[3] + m2[13] * m[7] + m2[14] * m[11] + m2[15] * m[15]
        ))

        testMat4({ dst -> fn(m, m2, dst) }, expected)
    }

    @Test
    fun testMultiply() {
        testMultiply { a, b, dst -> a.multiply(b, dst) }
    }

    @Test
    fun testMul() {
        testMultiply { a, b, dst -> a.mul(b, dst) }
    }

    private fun testInverse(fn: (m: Mat4f, dst: Mat4f) -> Mat4f) {
        val testMatrix = Mat4f.copyOf(floatArrayOf(
            2f, 1f, 3f, 0f,
            1f, 2f, 1f, 0f,
            3f, 1f, 2f, 0f,
            4f, 5f, 6f, 1f
        ))

        val expected = Mat4f.copyOf(floatArrayOf(
            -0.375f, -0.125f, 0.625f, 0f,
            -0.125f, 0.625f, -0.125f, 0f,
            0.625f, -0.125f, -0.375f, 0f,
            -1.625f, -1.875f, 0.375f, 1f
        ))

        testMat4({ dst -> fn(testMatrix, dst) }, expected)
    }

    @Test
    fun testInverse() {
        testInverse { m, dst -> m.inverse(dst) }
    }

    @Test
    fun testInvert() {
        testInverse { m, dst -> m.invert(dst) }
    }

    @Test
    fun testDeterminant() {
        val tests = listOf(
            Mat4f.copyOf(floatArrayOf(
                2f, 1f, 3f, 0f,
                1f, 2f, 1f, 0f,
                3f, 1f, 2f, 0f,
                4f, 5f, 6f, 1f
            )) to -8f,
            Mat4f.copyOf(floatArrayOf(
                2f, 0f, 0f, 0f,
                0f, 3f, 0f, 0f,
                0f, 0f, 4f, 0f,
                5f, 6f, 7f, 1f
            )) to 24f // 2 * 3 * 4 = 24
        )
        for ((inputM, expectedDet) in tests) {
            assertEquals(inputM.determinant(), expectedDet, EPSILON)
        }
    }

    @Test
    fun testSetTranslation() {
        val expected = Mat4f.copyOf(floatArrayOf(
            0f,  1f,  2f,  3f,
            4f,  5f,  6f,  7f,
            8f,  9f, 10f, 11f,
            11f, 22f, 33f, 15f
        ))
        testMat4({ dst -> m.setTranslation(Vec3f(11f, 22f, 33f), dst) }, expected)
    }

    @Test
    fun testGetTranslation() {
        val expected = Vec3f(12f, 13f, 14f)
        testVec3({ dst -> m.getTranslation(dst) }, expected)
    }

    @Test
    fun testGetAxis() {
        val tests = listOf(
            0 to Vec3f(0f, 1f, 2f),
            1 to Vec3f(4f, 5f, 6f),
            2 to Vec3f(8f, 9f, 10f)
        )
        for ((axis, expected) in tests) {
            testVec3({ dst -> m.getAxis(axis, dst) }, expected, "getAxis($axis)")
        }
    }

    @Test
    fun testSetAxis() {
        val tests = listOf(
            0 to Mat4f.copyOf(floatArrayOf(
                11f, 22f, 33f,  3f,
                4f,  5f,  6f,  7f,
                8f,  9f, 10f, 11f,
                12f, 13f, 14f, 15f
            )),
            1 to Mat4f.copyOf(floatArrayOf(
                0f,  1f,  2f,  3f,
                11f, 22f, 33f,  7f,
                8f,  9f, 10f, 11f,
                12f, 13f, 14f, 15f
            )),
            2 to Mat4f.copyOf(floatArrayOf(
                0f,  1f,  2f,  3f,
                4f,  5f,  6f,  7f,
                11f, 22f, 33f, 11f,
                12f, 13f, 14f, 15f
            ))
        )
        for ((axis, expected) in tests) {
            testMat4({ dst -> m.setAxis(Vec3f(11f, 22f, 33f), axis, dst) }, expected, "setAxis($axis)")
        }
    }

    @Test
    fun testGetScaling() {
        val testM = Mat4f.copyOf(floatArrayOf(
            1f, 2f, 3f, 4f,
            5f, 6f, 7f, 8f,
            9f, 10f, 11f, 12f,
            13f, 14f, 15f, 16f
        ))
        val expected = Vec3f(
            sqrt(1f * 1f + 2f * 2f + 3f * 3f),
            sqrt(5f * 5f + 6f * 6f + 7f * 7f),
            sqrt(9f * 9f + 10f * 10f + 11f * 11f)
        )
        testVec3({ dst -> testM.getScaling(dst) }, expected)
    }

    @Test
    fun testPerspective() {
        val fov = 2f
        val aspect = 4f
        val zNear = 10f
        val zFar = 30f
        val f = tan(PI * 0.5 - 0.5 * fov).toFloat()
        val rangeInv = 1.0f / (zNear - zFar)
        val expected = Mat4f.copyOf(floatArrayOf(
            f / aspect,
            0f,
            0f,
            0f,

            0f,
            f,
            0f,
            0f,

            0f,
            0f,
            zFar * rangeInv,
            -1f,

            0f,
            0f,
            zNear * zFar * rangeInv,
            0f
        ))
        testMat4({ dst -> Mat4f.perspective(fov, aspect, zNear, zFar, dst) }, expected)
    }

    @Test
    fun testPerspectiveWithZFarAtInfinity() {
        val fov = 2f
        val aspect = 4f
        val zNear = 10f
        val zFar = Float.POSITIVE_INFINITY
        val f = tan(PI * 0.5 - 0.5 * fov).toFloat()
        val expected = Mat4f.copyOf(floatArrayOf(
            f / aspect,
            0f,
            0f,
            0f,

            0f,
            f,
            0f,
            0f,

            0f,
            0f,
            -1f,
            -1f,

            0f,
            0f,
            -zNear,
            0f
        ))
        testMat4({ dst -> Mat4f.perspective(fov, aspect, zNear, zFar, dst) }, expected)
    }
    
    @Test
    fun testCorrectPerspective() {
        val fov = FloatPi / 4
        val aspect = 2f
        val zNear = 0.1f
        val zFar = 10.0f
        val m = Mat4f.perspective(fov, aspect, zNear, zFar)
        
        // Test that near plane maps to z=0
        val nearPoint = Vec3f(0f, 0f, -zNear)
        val transformedNear = nearPoint.transformMat4(m)
        assertVec3EqualApproximately(transformedNear, Vec3f(0f, 0f, 0f), 0.000001f)
        
        // Test that far plane maps to z=1
        val farPoint = Vec3f(0f, 0f, -zFar)
        val transformedFar = farPoint.transformMat4(m)
        assertVec3EqualApproximately(transformedFar, Vec3f(0f, 0f, 1f), 0.000001f)
    }
    
    @Test
    fun testCorrectPerspectiveWithZFarAtInfinity() {
        val fov = FloatPi / 4
        val aspect = 2f
        val zNear = 10f
        val zFar = Float.POSITIVE_INFINITY
        val m = Mat4f.perspective(fov, aspect, zNear, zFar)
        
        // Test that near plane maps correctly
        val nearPoint = Vec3f(0f, 0f, -zNear)
        val transformedNear = nearPoint.transformMat4(m)
        assertVec3EqualApproximately(transformedNear, Vec3f(0f, 0f, 0f), 0.000001f)
        
        // Test various depths - we should get increasingly closer to z=1
        val depths = listOf(-1000f, -1000000f, -1000000000f)
        for (depth in depths) {
            val point = Vec3f(0f, 0f, depth)
            val transformed = point.transformMat4(m)
            assertTrue(transformed.z > 0.9f, "Depth $depth should transform to z close to 1")
        }
    }

    @Test
    fun testOrtho() {
        val left = 2f
        val right = 4f
        val bottom = 30f
        val top = 10f
        val near = 15f
        val far = 25f
        val expected = Mat4f.copyOf(floatArrayOf(
            2f / (right - left),
            0f,
            0f,
            0f,

            0f,
            2f / (top - bottom),
            0f,
            0f,

            0f,
            0f,
            1f / (near - far),
            0f,

            (right + left) / (left - right),
            (top + bottom) / (bottom - top),
            near / (near - far),
            1f
        ))
        testMat4({ dst -> Mat4f.ortho(left, right, bottom, top, near, far, dst) }, expected)
    }
    
    @Test
    fun testCorrectOrtho() {
        val left = -2f
        val right = 4f
        val bottom = 30f
        val top = 10f
        val near = 15f
        val far = 25f
        val m = Mat4f.ortho(left, right, bottom, top, near, far)
        
        // Test that corners map properly
        val bottomLeftNear = Vec3f(left, bottom, -near)
        val transformedBLN = bottomLeftNear.transformMat4(m)
        assertVec3EqualApproximately(transformedBLN, Vec3f(-1f, -1f, 0f), 0.000001f)
        
        val topRightFar = Vec3f(right, top, -far)
        val transformedTRF = topRightFar.transformMat4(m)
        assertVec3EqualApproximately(transformedTRF, Vec3f(1f, 1f, 1f), 0.000001f)
    }

    @Test
    fun testFrustum() {
        val left = 2f
        val right = 4f
        val bottom = 30f
        val top = 10f
        val near = 15f
        val far = 25f

        val dx = (right - left)
        val dy = (top - bottom)
        val dz = (near - far)

        val expected = Mat4f.copyOf(floatArrayOf(
            2f * near / dx,
            0f,
            0f,
            0f,
            
            0f,
            2f * near / dy,
            0f,
            0f,
            
            (left + right) / dx,
            (top + bottom) / dy,
            far / dz,
            -1f,
            
            0f,
            0f,
            near * far / dz,
            0f
        ))
        testMat4({ dst -> Mat4f.frustum(left, right, bottom, top, near, far, dst) }, expected)
    }
    
    @Test
    fun testCorrectFrustum() {
        val left = -2f
        val right = 4f
        val bottom = 30f
        val top = 10f
        val near = 15f
        val far = 25f
        val m = Mat4f.frustum(left, right, bottom, top, near, far)
        
        // Test that corners map properly
        val bottomLeftNear = Vec3f(left, bottom, -near)
        val transformedBLN = bottomLeftNear.transformMat4(m)
        assertVec3EqualApproximately(transformedBLN, Vec3f(-1f, -1f, 0f), 0.000001f)
        
        // Test that center point at far depth maps to z=1
        val centerX = (left + right) * 0.5f
        val centerY = (top + bottom) * 0.5f
        val centerFar = Vec3f(centerX, centerY, -far)
        val transformedCF = centerFar.transformMat4(m)
        assertEquals(1f, transformedCF.z, 0.000001f)
    }
    
    @Test
    fun testFrustumReverseZ() {
        val left = 2f
        val right = 4f
        val bottom = 30f
        val top = 10f
        val near = 15f
        val far = 25f

        val dx = (right - left)
        val dy = (top - bottom)
        val dz = (far - near)

        val expected = Mat4f.copyOf(floatArrayOf(
            2f * near / dx,
            0f,
            0f,
            0f,
            
            0f,
            2f * near / dy,
            0f,
            0f,
            
            (left + right) / dx,
            (top + bottom) / dy,
            near / dz,
            -1f,
            
            0f,
            0f,
            near * far / dz,
            0f
        ))
        testMat4({ dst -> Mat4f.frustumReverseZ(left, right, bottom, top, near, far, dst) }, expected)
    }
    
    @Test
    fun testCorrectFrustumReverseZ() {
        val left = -2f
        val right = 4f
        val bottom = 30f
        val top = 10f
        val near = 15f
        val far = 25f
        val m = Mat4f.frustumReverseZ(left, right, bottom, top, near, far)
        
        // Test that corners map properly
        val bottomLeftNear = Vec3f(left, bottom, -near)
        val transformedBLN = bottomLeftNear.transformMat4(m)
        assertVec3EqualApproximately(transformedBLN, Vec3f(-1f, -1f, 1f), 0.000001f)
        
        // Test that center points map properly
        val centerX = (left + right) * 0.5f
        val centerY = (top + bottom) * 0.5f
        
        // Center at near should map to z=1
        val centerNear = Vec3f(centerX, centerY, -near)
        val transformedCN = centerNear.transformMat4(m)
        assertEquals(1f, transformedCN.z, 0.000001f)
        
        // Center at far should map to z=0
        val centerFar = Vec3f(centerX, centerY, -far)
        val transformedCF = centerFar.transformMat4(m)
        assertEquals(0f, transformedCF.z, 0.000001f)
    }

    @Test
    fun testLookAt() {
        val eye = Vec3f(1f, 2f, 3f)
        val target = Vec3f(11f, 22f, 33f)
        val up = Vec3f(-4f, -5f, -6f)
        
        // The expected matrix based on the JavaScript test
        val expected = Mat4f.copyOf(floatArrayOf(
            0.40824833f, -0.8728715f, -0.26726124f, 0f,
            -0.8164966f, -0.2182179f, -0.5345225f, 0f,
            0.40824824f, 0.4364358f, -0.8017837f, 0f,
            0f, 0f, 3.7416575f, 1f
        ))
        
        testMat4({ dst -> Mat4f.lookAt(eye, target, up, dst) }, expected)
    }
    
    @Test
    fun testAim() {
        // Test cases exactly matching the JavaScript tests
        val testCases = listOf(
            // Case 0
            Triple(
                Vec3f(11f, 12f, 13f),
                Vec3f(11f, 12f, 13f + 5f),
                Vec3f(0f, 1f, 0f)
            ) to Mat4f.copyOf(floatArrayOf(
                1f, 0f, 0f, 0f,
                0f, 1f, 0f, 0f,
                0f, 0f, 1f, 0f,
                11f, 12f, 13f, 1f
            )),
            // Case 1
            Triple(
                Vec3f(11f, 12f, 13f),
                Vec3f(11f, 12f, 13f - 5f),
                Vec3f(0f, 1f, 0f)
            ) to Mat4f.copyOf(floatArrayOf(
                -1f, 0f, 0f, 0f,
                0f, 1f, 0f, 0f,
                0f, 0f, -1f, 0f,
                11f, 12f, 13f, 1f
            )),
            // Case 2
            Triple(
                Vec3f(11f, 12f, 13f),
                Vec3f(11f + 5f, 12f, 13f),
                Vec3f(0f, 1f, 0f)
            ) to Mat4f.copyOf(floatArrayOf(
                0f, 0f, -1f, 0f,
                0f, 1f, 0f, 0f,
                1f, 0f, 0f, 0f,
                11f, 12f, 13f, 1f
            )),
            // Case 3
            Triple(
                Vec3f(1f, 2f, 3f),
                Vec3f(11f, 22f, 33f),
                Vec3f(-4f, -5f, -6f)
            ) to Mat4f.copyOf(floatArrayOf(
                -0.40824833512306213f,
                0.8164966106414795f,
                -0.40824824571609497f,
                0f,
                -0.8728715181350708f,
                -0.21821792423725128f,
                0.4364357888698578f,
                0f,
                0.26726123690605164f,
                0.5345224738121033f,
                0.8017837405204773f,
                0f,
                1f,
                2f,
                3f,
                1f
            ))
        )
        
        for ((index, testCase) in testCases.withIndex()) {
            val (inputs, expected) = testCase
            val (position, target, up) = inputs
            testMat4(
                { dst -> Mat4f.aim(position, target, up, dst) },
                expected,
                "aim test case $index"
            )
        }
    }
    
    @Test
    fun testCameraAim() {
        // Test cases exactly matching the JavaScript tests
        val testCases = listOf(
            // Case 0
            Triple(
                Vec3f(11f, 12f, 13f),
                Vec3f(11f, 12f, 13f + 5f),
                Vec3f(0f, 1f, 0f)
            ) to Mat4f.copyOf(floatArrayOf(
                -1f, 0f, 0f, 0f,
                0f, 1f, 0f, 0f,
                0f, 0f, -1f, 0f,
                11f, 12f, 13f, 1f
            )),
            // Case 1
            Triple(
                Vec3f(11f, 12f, 13f),
                Vec3f(11f, 12f, 13f - 5f),
                Vec3f(0f, 1f, 0f)
            ) to Mat4f.copyOf(floatArrayOf(
                1f, 0f, 0f, 0f,
                0f, 1f, 0f, 0f,
                0f, 0f, 1f, 0f,
                11f, 12f, 13f, 1f
            )),
            // Case 2
            Triple(
                Vec3f(11f, 12f, 13f),
                Vec3f(11f + 5f, 12f, 13f),
                Vec3f(0f, 1f, 0f)
            ) to Mat4f.copyOf(floatArrayOf(
                0f, 0f, 1f, 0f,
                0f, 1f, 0f, 0f,
                -1f, 0f, 0f, 0f,
                11f, 12f, 13f, 1f
            )),
            // Case 3
            Triple(
                Vec3f(1f, 2f, 3f),
                Vec3f(11f, 22f, 33f),
                Vec3f(-4f, -5f, -6f)
            ) to Mat4f.copyOf(floatArrayOf(
                0.40824833512306213f,
                -0.8164966106414795f,
                0.40824824571609497f,
                0f,
                -0.8728715181350708f,
                -0.21821792423725128f,
                0.4364357888698578f,
                0f,
                -0.26726123690605164f,
                -0.5345224738121033f,
                -0.8017837405204773f,
                0f,
                1f,
                2f,
                3f,
                1f
            ))
        )
        
        for ((index, testCase) in testCases.withIndex()) {
            val (inputs, expected) = testCase
            val (position, target, up) = inputs
            testMat4(
                { dst -> Mat4f.cameraAim(position, target, up, dst) },
                expected,
                "cameraAim test case $index"
            )
        }
    }

    @Test
    fun testSameFrustumAsPerspective() {
        val lr = 4f
        val tb = 2f
        val near = 10f
        val far = 20f
        val m1 = Mat4f.frustum(-lr, lr, -tb, tb, near, far)
        val fov = atan(tb / near) * 2
        val aspect = lr / tb
        val m2 = Mat4f.perspective(fov, aspect, near, far)
        
        // The matrices should be the same
        assertTrue(m1.equalsApproximately(m2), "Frustum matrix should be the same as perspective matrix")
    }

    @Test
    fun testFromQuat() {
        // Test cases for rotation around different axes
        val testCases = listOf(
            // X-axis rotation
            Quatf.fromEuler(FloatPi, 0f, 0f, "xyz") to Mat4f.rotationX(FloatPi),
            // Y-axis rotation
            Quatf.fromEuler(0f, FloatPi, 0f, "xyz") to Mat4f.rotationY(FloatPi),
            // Z-axis rotation 
            Quatf.fromEuler(0f, 0f, FloatPi, "xyz") to Mat4f.rotationZ(FloatPi),
            // 90 degree rotations
            Quatf.fromEuler(FloatPi / 2, 0f, 0f, "xyz") to Mat4f.rotationX(FloatPi / 2),
            Quatf.fromEuler(0f, FloatPi / 2, 0f, "xyz") to Mat4f.rotationY(FloatPi / 2),
            Quatf.fromEuler(0f, 0f, FloatPi / 2, "xyz") to Mat4f.rotationZ(FloatPi / 2)
        )
        
        for ((index, testCase) in testCases.withIndex()) {
            val (quat, expected) = testCase
            testMat4(
                { dst -> Mat4f.fromQuat(quat, dst) },
                expected,
                "fromQuat test case $index"
            )
        }
    }

    @Test
    fun testCorrectPerspectiveReverseZ() {
        val fov = FloatPi / 4
        val aspect = 2f
        val zNear = 10f
        val zFar = 20f
        val m = Mat4f.perspectiveReverseZ(fov, aspect, zNear, zFar)
        
        // Test that near plane maps to z=1
        val nearPoint = Vec3f(0f, 0f, -zNear)
        val transformedNear = nearPoint.transformMat4(m)
        assertVec3EqualApproximately(transformedNear, Vec3f(0f, 0f, 1f), 0.000001f)
        
        // Test that middle point maps to appropriate z value
        val midPoint = Vec3f(0f, 0f, -15f)
        val transformedMid = midPoint.transformMat4(m)
        assertVec3EqualApproximately(transformedMid, Vec3f(0f, 0f, 0.3333333f), 0.000001f)
        
        // Test that far plane maps to z=0
        val farPoint = Vec3f(0f, 0f, -zFar)
        val transformedFar = farPoint.transformMat4(m)
        assertVec3EqualApproximately(transformedFar, Vec3f(0f, 0f, 0f), 0.000001f)
    }
    
    @Test
    fun testCorrectPerspectiveReverseZWithZFarAtInfinity() {
        val fov = FloatPi / 4
        val aspect = 2f
        val zNear = 10f
        val zFar = Float.POSITIVE_INFINITY
        val m = Mat4f.perspectiveReverseZ(fov, aspect, zNear, zFar)
        
        // Test that near plane maps to z=1
        val nearPoint = Vec3f(0f, 0f, -zNear)
        val transformedNear = nearPoint.transformMat4(m)
        assertVec3EqualApproximately(transformedNear, Vec3f(0f, 0f, 1f), 0.000001f)
        
        // Test various depths - we should get increasingly closer to z=0
        val testPoints = listOf(
            -1000f to 0.01f,
            -1000000f to 0.00001f,
            -1000000000f to 0.00000001f
        )
        
        for ((depth, expectedZ) in testPoints) {
            val point = Vec3f(0f, 0f, depth)
            val transformed = point.transformMat4(m)
            assertTrue(abs(transformed.z - expectedZ) < 0.000001f, 
                      "Depth $depth should transform to z approximately $expectedZ, got ${transformed.z}")
        }
    }
}
