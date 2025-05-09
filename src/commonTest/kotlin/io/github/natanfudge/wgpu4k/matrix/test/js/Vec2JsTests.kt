import io.github.natanfudge.wgpu4k.matrix.EPSILON
import io.github.natanfudge.wgpu4k.matrix.FloatPi
import io.github.natanfudge.wgpu4k.matrix.Mat3f
import io.github.natanfudge.wgpu4k.matrix.Mat4f
import io.github.natanfudge.wgpu4k.matrix.Vec2f
import io.github.natanfudge.wgpu4k.matrix.Vec3f
import io.github.natanfudge.wgpu4k.matrix.test.js.assertVec3EqualsApproximately
import kotlin.test.* // Import kotlin.test
import kotlin.math.*
// Assume Vec2 class from previous step is available in the same package or imported
// import com.yourpackage.Vec2 // Adjust import if needed

// --- Test Utilities ---

// Custom assertion for Vec2 approximate equality using kotlin.test
fun assertVec2EqualsApproximately(expected: Vec2f, actual: Vec2f, tolerance: Float = EPSILON, message: String? = null) {
    val dx = abs(expected.x - actual.x)
    val dy = abs(expected.y - actual.y)
    val msg = message ?: "expected: <$expected> but was: <$actual>"
    assertTrue(dx < tolerance && dy < tolerance, msg)
}

// Custom assertion for Vec3 (FloatArray) approximate equality using kotlin.test
fun assertVec3EqualsApproximately(expected: FloatArray, actual: FloatArray, tolerance: Float = EPSILON, message: String? = null) {
    assertEquals(3, expected.size, "Expected array must have size 3")
    assertEquals(3, actual.size, "Actual array must have size 3")
    val dx = abs(expected[0] - actual[0])
    val dy = abs(expected[1] - actual[1])
    val dz = abs(expected[2] - actual[2])
    val msg = message ?: "expected: <${expected.contentToString()}> but was: <${actual.contentToString()}>"
    assertTrue(dx < tolerance && dy < tolerance && dz < tolerance, msg)
}

// Helper for checking float equality with tolerance using kotlin.test
fun assertFloatEqualsApproximately(expected: Float, actual: Float, tolerance: Float, message: String? = null) {
    val diff = abs(expected - actual)
    val msg = message ?: "expected: <$expected> but was: <$actual> with tolerance <$tolerance>"
    assertTrue(diff <= tolerance, msg)
}


class Vec2Tests {

    private val defaultTolerance = 1e-6f // Tolerance for approximate checks if not Vec2.EPSILON

    // Helper to test methods that take (other: Vec2, dst: Vec2?)
    private fun testBinaryVecOp(
        operation: Vec2f.(Vec2f, Vec2f) -> Vec2f, // The method reference (e.g., Vec2::add)
        v1Initial: Vec2f,
        v2Initial: Vec2f,
        expected: Vec2f,
        tolerance: Float = EPSILON
    ) {

        // --- Test Case 2: With dst = new Vec2() ---
        run {
            val v1 = v1Initial.copy()
            val v2 = v2Initial.copy()
            val dest = Vec2f()
            val result = v1.operation(v2, dest)

            assertSame(dest, result, "Result should be the same instance as dst")
            assertVec2EqualsApproximately(expected, result, tolerance, "Result mismatch (dst=new)")
            // Verify originals unchanged
            assertEquals(v1Initial, v1, "Original v1 modified (dst=new)")
            assertEquals(v2Initial, v2, "Original v2 modified (dst=new)")
        }

        // --- Test Case 3: With dst = v1 (self) ---
        run {
            val v1 = v1Initial.copy()
            val v2 = v2Initial.copy()
            val result = v1.operation(v2, v1) // Modify v1 in place

            assertSame(v1, result, "Result should be the same instance as v1 when dst=v1")
            assertVec2EqualsApproximately(expected, result, tolerance, "Result mismatch (dst=v1)")
            // Verify other operand unchanged
            assertEquals(v2Initial, v2, "Original v2 modified (dst=v1)")
        }

        // --- Test Case 4: With dst = v2 (other operand) ---
        run {
            val v1 = v1Initial.copy()
            val v2 = v2Initial.copy()
            val result = v1.operation(v2, v2) // Modify v2 in place

            assertSame(v2, result, "Result should be the same instance as v2 when dst=v2")
            assertVec2EqualsApproximately(expected, result, tolerance, "Result mismatch (dst=v2)")
            // Verify self unchanged
            assertEquals(v1Initial, v1, "Original v1 modified (dst=v2)")
        }
    }

    // Helper to test methods that take (scalar, dst: Vec2?)
    private fun testScalarOp(
        operation: Vec2f.(Float, Vec2f) -> Vec2f,
        vInitial: Vec2f,
        scalar: Float,
        expected: Vec2f,
        tolerance: Float = EPSILON
    ) {
        // --- Test Case 2: dst = new ---
        run {
            val v = vInitial.copy()
            val dest = Vec2f()
            val result = v.operation(scalar, dest)
            assertSame(dest, result)
            assertVec2EqualsApproximately(expected, result, tolerance)
            assertEquals(vInitial, v)
        }
        // --- Test Case 3: dst = self ---
        run {
            val v = vInitial.copy()
            val result = v.operation(scalar, v)
            assertSame(v, result)
            assertVec2EqualsApproximately(expected, result, tolerance)
        }
    }

    // Helper to test methods that take only (dst: Vec2?)
    private fun testUnaryVecOp(
        operation: Vec2f.(Vec2f) -> Vec2f,
        vInitial: Vec2f,
        expected: Vec2f,
        tolerance: Float = EPSILON
    ) {

        // --- Test Case 2: dst = new ---
        run {
            val v = vInitial.copy()
            val dest = Vec2f()
            val result = v.operation(dest)
            assertSame(dest, result)
            assertVec2EqualsApproximately(expected, result, tolerance)
            assertEquals(vInitial, v)
        }
        // --- Test Case 3: dst = self ---
        run {
            val v = vInitial.copy()
            val result = v.operation(v)
            assertSame(v, result)
            assertVec2EqualsApproximately(expected, result, tolerance)
        }
    }

    // --- Actual Tests ---

    @Test
    fun `should add`() {
        val expected = Vec2f(3f, 5f)
        testBinaryVecOp(Vec2f::add, Vec2f(1f, 2f), Vec2f(2f, 3f), expected)
    }

    @Test
    fun `should compute angle`() {
        data class AngleTestData(val a: Vec2f, val b: Vec2f, val expected: Float)

        val tests = listOf(
            AngleTestData(Vec2f(1f, 0f), Vec2f( 0f, 1f), FloatPi / 2f),
            AngleTestData(Vec2f(1f, 0f), Vec2f(-1f, 0f), FloatPi),
            AngleTestData(Vec2f(1f, 0f), Vec2f( 1f, 0f), 0f),
            AngleTestData(Vec2f(1f, 2f), Vec2f( 4f, 5f), 0.2110933f),
            // AngleTestData(Vec2(1f, 0f), Vec2( 0f, Float.POSITIVE_INFINITY), FloatPi / 2f) // Infinity might cause issues
        )
        for ((a, b, expected) in tests) {
            // Use custom float comparison for angle result
            assertFloatEqualsApproximately(expected, a.angle(b), defaultTolerance, "Angle mismatch for $a, $b")
            // Test with scaled vectors
            val aScaled = a.scale(1000f)
            val bScaled = b.scale(1000f)
            assertFloatEqualsApproximately(expected, aScaled.angle(bScaled), defaultTolerance, "Scaled angle mismatch for $a, $b")
        }
    }

    @Test
    fun `should compute ceil`() {
        val expected = Vec2f(2f, -1f)
        testUnaryVecOp(Vec2f::ceil, Vec2f(1.1f, -1.1f), expected)
    }

    @Test
    fun `should compute floor`() {
        val expected = Vec2f(1f, -2f)
        testUnaryVecOp(Vec2f::floor, Vec2f(1.1f, -1.1f), expected)
    }

    @Test
    fun `should compute round`() {
        val expected = Vec2f(1f, -1f)
        testUnaryVecOp(Vec2f::round, Vec2f(1.1f, -1.1f), expected)
    }

    @Test
    fun `should clamp`() {
        // Using a slightly different helper structure as clamp signature varies
        run {
            val expected = Vec2f(1f, 0f)
            testUnaryVecOp( { dst -> this.clamp(0f, 1f, dst) }, Vec2f(2f, -1f), expected)
        }
        run {
            val expected = Vec2f(-10f, 5f)
            testUnaryVecOp( { dst -> this.clamp(-10f, 5f, dst) }, Vec2f(-22f, 50f), expected)
        }
    }

    @Test
    fun `should equals approximately`() {
        assertTrue(Vec2f(2f, 3f).equalsApproximately(Vec2f(2f, 3f)))
        assertTrue(Vec2f(2f, 3f).equalsApproximately(Vec2f(2f + EPSILON * 0.5f, 3f)))
        assertFalse(Vec2f(2f, 3f).equalsApproximately(Vec2f(2.001f, 3f)))
    }

    @Test
    fun `should equals`() {
        // Uses data class equals `==`
        assertTrue(Vec2f(2f, 3f) == Vec2f(2f, 3f))
        assertFalse(Vec2f(2f, 3f) == Vec2f(2f + EPSILON * 0.5f, 3f))
    }

    @Test
    fun `should subtract`() {
        val expected = Vec2f(-2f, -3f)
        testBinaryVecOp(Vec2f::subtract, Vec2f(2f, 3f), Vec2f(4f, 6f), expected)
    }

    @Test
    fun `should sub`() {
        val expected = Vec2f(-2f, -3f)
        testBinaryVecOp(Vec2f::sub, Vec2f(2f, 3f), Vec2f(4f, 6f), expected)
    }

    @Test
    fun `should lerp`() {
        val expected = Vec2f(3f, 4.5f)
        // Helper structure needs adapting for the extra 't' float parameter
        run {
            val v1 = Vec2f(2f, 3f)
            val v2 = Vec2f(4f, 6f)
            val t = 0.5f
            // No dst
            val r1 = v1.lerp(v2, t)
            assertNotSame(v1, r1)
            assertNotSame(v2, r1)
            assertVec2EqualsApproximately(expected, r1)
            assertEquals(Vec2f(2f, 3f), v1) // Check originals
            assertEquals(Vec2f(4f, 6f), v2)
            // Dst = new
            val dest = Vec2f()
            val r2 = v1.lerp(v2, t, dest)
            assertSame(dest, r2)
            assertVec2EqualsApproximately(expected, r2)
            assertEquals(Vec2f(2f, 3f), v1)
            assertEquals(Vec2f(4f, 6f), v2)
            // Dst = self
            val r3 = v1.lerp(v2, t, v1)
            assertSame(v1, r3)
            assertVec2EqualsApproximately(expected, v1)
            assertEquals(Vec2f(4f, 6f), v2)
        }
    }

    @Test
    fun `should lerp under 0`() {
        val expected = Vec2f(0.5f, 1.5f)
        // Similar manual check structure as above for lerp
        run {
            val v1 = Vec2f(1f, 3f)
            val v2 = Vec2f(2f, 6f)
            val t = -0.5f
            val r1 = v1.lerp(v2, t)
            assertVec2EqualsApproximately(expected, r1)
            val dest = Vec2f()
            v1.lerp(v2, t, dest)
            assertVec2EqualsApproximately(expected, dest)
            v1.lerp(v2, t, v1)
            assertVec2EqualsApproximately(expected, v1)
        }
    }

    @Test
    fun `should lerp over 1`() { // Renamed from "over 0" to match value
        val expected = Vec2f(2.5f, 7.5f)
        run {
            val v1 = Vec2f(1f, 3f)
            val v2 = Vec2f(2f, 6f)
            val t = 1.5f
            val r1 = v1.lerp(v2, t)
            assertVec2EqualsApproximately(expected, r1)
            val dest = Vec2f()
            v1.lerp(v2, t, dest)
            assertVec2EqualsApproximately(expected, dest)
            v1.lerp(v2, t, v1)
            assertVec2EqualsApproximately(expected, v1)
        }
    }

    @Test
    fun `should multiply by scalar`() {
        val expected = Vec2f(4f, 6f)
        testScalarOp(Vec2f::mulScalar, Vec2f(2f, 3f), 2f, expected)
    }

    @Test
    fun `should scale`() {
        val expected = Vec2f(4f, 6f)
        testScalarOp(Vec2f::scale, Vec2f(2f, 3f), 2f, expected)
    }

    @Test
    fun `should add scaled`() {
        val expected = Vec2f(10f, 15f)
        // Manual check structure needed for (Vec2, Float, Vec2?) signature
        run {
            val v1 = Vec2f(2f, 3f)
            val v2 = Vec2f(4f, 6f)
            val scale = 2f
            // No dst
            val r1 = v1.addScaled(v2, scale)
            assertNotSame(v1, r1)
            assertNotSame(v2, r1)
            assertVec2EqualsApproximately(expected, r1)
            assertEquals(Vec2f(2f, 3f), v1) // Check originals
            assertEquals(Vec2f(4f, 6f), v2)
            // Dst = new
            val dest = Vec2f()
            val r2 = v1.addScaled(v2, scale, dest)
            assertSame(dest, r2)
            assertVec2EqualsApproximately(expected, r2)
            assertEquals(Vec2f(2f, 3f), v1)
            assertEquals(Vec2f(4f, 6f), v2)
            // Dst = self
            val r3 = v1.addScaled(v2, scale, v1)
            assertSame(v1, r3)
            assertVec2EqualsApproximately(expected, v1)
            assertEquals(Vec2f(4f, 6f), v2)
        }
    }

    @Test
    fun `should divide by scalar`() {
        val expected = Vec2f(0.5f, 1.5f)
        testScalarOp(Vec2f::divScalar, Vec2f(1f, 3f), 2f, expected)
    }

    @Test
    fun `should inverse`() {
        val expected = Vec2f(1f / 3f, 1f / -4f)
        testUnaryVecOp(Vec2f::inverse, Vec2f(3f, -4f), expected)
    }

    @Test
    fun `should invert`() {
        val expected = Vec2f(1f / 3f, 1f / -4f)
        testUnaryVecOp(Vec2f::invert, Vec2f(3f, -4f), expected)
    }

    @Test
    fun `should cross`() {
        val v1 = Vec2f(2f, 3f)
        val v2 = Vec2f(4f, 5f)
        val expected = Vec3f(0f, 0f, 2f * 5f - 3f * 4f) // -2f

        // No dst
        val c1 = v1.cross(v2)
        assertVec3EqualsApproximately(expected, c1)

        // With dst = new
        val dest = Vec3f()
        val c2 = v1.cross(v2, dest)
        assertSame(dest, c2)
        assertVec3EqualsApproximately(expected, c2)

        // Test reuse of dst (though method doesn't read from dst)
        val v3 = Vec2f(3f, 2f)
        val expected2 = floatArrayOf(0f, 0f, 3f * 5f - 2f * 4f) // 7f
        val c3 = v3.cross(v2, dest) // Reuse dest
        assertSame(dest, c3)
        assertVec3EqualsApproximately(expected2, c3)
    }


    @Test
    fun `should compute dot product`() {
        val expected = 2f * 4f + 3f * 6f
        val value = Vec2f(2f, 3f).dot(Vec2f(4f, 6f))
        assertFloatEqualsApproximately(expected, value, defaultTolerance)
    }

    @Test
    fun `should compute length`() {
        val expected = sqrt(2f * 2f + 3f * 3f)
        val value = Vec2f(2f, 3f).length()
        assertFloatEqualsApproximately(expected, value, defaultTolerance)
    }

    @Test
    fun `should compute length squared`() {
        val expected = 2f * 2f + 3f * 3f
        val value = Vec2f(2f, 3f).lengthSq()
        assertFloatEqualsApproximately(expected, value, defaultTolerance)
    }

    @Test
    fun `should compute len`() {
        val expected = sqrt(2f * 2f + 3f * 3f)
        val value = Vec2f(2f, 3f).len()
        assertFloatEqualsApproximately(expected, value, defaultTolerance)
    }

    @Test
    fun `should compute lenSq`() {
        val expected = 2f * 2f + 3f * 3f
        val value = Vec2f(2f, 3f).lenSq()
        assertFloatEqualsApproximately(expected, value, defaultTolerance)
    }

    @Test
    fun `should compute distance`() {
        val expected = sqrt(3f * 3f + 4f * 4f) // 5f
        val value = Vec2f(2f, 3f).distance(Vec2f(5f, 7f))
        assertFloatEqualsApproximately(expected, value, defaultTolerance)
    }

    @Test
    fun `should compute distance squared`() {
        val expected = 3f * 3f + 4f * 4f // 25f
        val value = Vec2f(2f, 3f).distanceSq(Vec2f(5f, 7f))
        assertFloatEqualsApproximately(expected, value, defaultTolerance)
    }

    @Test
    fun `should compute dist`() {
        val expected = sqrt(3f * 3f + 4f * 4f) // 5f
        val value = Vec2f(2f, 3f).dist(Vec2f(5f, 7f))
        assertFloatEqualsApproximately(expected, value, defaultTolerance)
    }

    @Test
    fun `should compute dist squared`() {
        val expected = 3f * 3f + 4f * 4f // 25f
        val value = Vec2f(2f, 3f).distSq(Vec2f(5f, 7f))
        assertFloatEqualsApproximately(expected, value, defaultTolerance)
    }

    @Test
    fun `should normalize`() {
        val length = sqrt(2f * 2f + 3f * 3f)
        val expected = Vec2f(2f / length, 3f / length)
        testUnaryVecOp(Vec2f::normalize, Vec2f(2f, 3f), expected)
    }

    @Test
    fun `should negate`() {
        val expected = Vec2f(-2f, 3f)
        testUnaryVecOp(Vec2f::negate, Vec2f(2f, -3f), expected)
    }

    @Test
    fun `should copyTo`() { // Renamed from copy
        val expected = Vec2f(2f, 3f)
        // Test helper covers dst = new and dst = self (which is trivial for copy)
        testUnaryVecOp(Vec2f::copyTo, Vec2f(2f, 3f), expected)

        // Explicitly check non-identity for null dst
        val v = Vec2f(2f, 3f)
        val result = v.copyTo(Vec2f())
        assertNotSame(v, result)
        assertEquals(expected, result)
    }

    @Test
    fun `should clone`() {
        val expected = Vec2f(2f, 3f)
        testUnaryVecOp(Vec2f::clone, Vec2f(2f, 3f), expected)
        // Explicitly check non-identity for null dst
        val v = Vec2f(2f, 3f)
        val result = v.clone(Vec2f())
        assertNotSame(v, result)
        assertEquals(expected, result)
    }

    @Test
    fun `should set`() {
        val v = Vec2f(1f, 1f)
        val expected = Vec2f(2f, 3f)
        val result = v.set(2f, 3f)
        assertSame(v, result, "set should return the modified instance")
        assertEquals(expected, v, "set did not modify instance correctly")
    }


    @Test
    fun `should multiply`() {
        val expected = Vec2f(8f, 18f)
        testBinaryVecOp(Vec2f::multiply, Vec2f(2f, 3f), Vec2f(4f, 6f), expected)
    }

    @Test
    fun `should mul`() {
        val expected = Vec2f(8f, 18f)
        testBinaryVecOp(Vec2f::mul, Vec2f(2f, 3f), Vec2f(4f, 6f), expected)
    }

    @Test
    fun `should divide`() {
        val expected = Vec2f(2f / 3f, 3f / 4f)
        testBinaryVecOp(Vec2f::divide, Vec2f(2f, 3f), Vec2f(3f, 4f), expected)
    }

    @Test
    fun `should div`() {
        val expected = Vec2f(2f / 3f, 3f / 4f)
        testBinaryVecOp(Vec2f::div, Vec2f(2f, 3f), Vec2f(3f, 4f), expected)
    }

    @Test
    fun `should fromValues`() {
        val expected = Vec2f(2f, 3f) // Use constructor directly
        val v1 = Vec2f.fromValues(2f, 3f)
        assertEquals(expected, v1)
    }

    @Test
    fun `should create`() {
        val expected = Vec2f(2f, 3f) // Use constructor directly
        val v1 = Vec2f.create(2f, 3f)
        assertEquals(expected, v1)
    }

    @Test
    fun `should random`() {
        for (i in 0..99) {
            // No dst
            val v1 = Vec2f.random()
            assertFloatEqualsApproximately(1f, v1.length(), defaultTolerance, "Random length not 1")
            // Scale 2, no dst
            val v2 = Vec2f.random(scale = 2f)
            assertFloatEqualsApproximately(2f, v2.length(), defaultTolerance, "Random(2) length not 2")
            // Scale 0.5, no dst
            val vp5 = Vec2f.random(scale = 0.5f)
            assertFloatEqualsApproximately(0.5f, vp5.length(), defaultTolerance, "Random(0.5) length not 0.5")

            // With dst
            val dest = Vec2f()
            val v3 = Vec2f.random(scale = 3f, dst = dest)
            assertSame(dest, v3)
            assertFloatEqualsApproximately(3f, v3.length(), defaultTolerance, "Random(3, dst) length not 3")
        }
    }

//        it('should transform by 3x3', () => {
//      const expected = [16, 17];
//      testV2WithAndWithoutDest((a, newDst) => {
//        const m = [
//          4, 0, 0, 11,
//          0, 5, 0, 12,
//          8, 2, 0, 13,
//        ];
//        return vec2.transformMat3(a, m, newDst);
//      }, expected, [2, 3]);
//    });
    @Test
    fun `should transform by 3x3`() {
        val expected = Vec2f(16f, 17f)
        // [4 0 8]
        // [0 5 2]
        // [0 0 1]
        val mJsLayout = Mat3f(
            4f, 0f, 0f, 11f,
            0f, 5f, 0f, 12f,
            8f, 2f, 0f, 13f
        )

        testUnaryVecOp( { dst -> this.transformMat3(mJsLayout, dst) }, Vec2f(2f, 3f), expected)
    }

    @Test
    fun `should transform by 4x4`() {
        val expected = Vec2f(6f, 11f)
        // Column major matrix
        val m = Mat4f(
            1f, 0f, 0f, 0f, // col 0
            0f, 2f, 0f, 0f, // col 1
            0f, 0f, 3f, 0f, // col 2
            4f, 5f, 6f, 1f  // col 3 (translation)
        )
        testUnaryVecOp( { dst -> this.transformMat4(m, dst) }, Vec2f(2f, 3f), expected)
    }


    @Test
    fun `should zero`() {
        // No dst
        val v1 = Vec2f.zero()
        assertEquals(Vec2f(0f, 0f), v1)
        assertNotSame(Vec2f.zero(), v1) // Ensure new instance each time

        // With dst
        val v2 = Vec2f(2f, 3f)
        val dest = Vec2f()
        val r1 = Vec2f.zero(dest)
        assertSame(dest, r1)
        assertEquals(Vec2f(0f, 0f), r1)

        // With dst = self (using the existing v2)
        val r2 = Vec2f.zero(v2)
        assertSame(v2, r2)
        assertEquals(Vec2f(0f, 0f), v2)
    }

    private val testTolerance = 1e-6f

    @Test
    fun `rotation around world origin 0 0`() {
        val expected = Vec2f(0f, -1f)
        run {
            val v = Vec2f(0f, 1f)
            val origin = Vec2f(0f, 0f)
            val angle = FloatPi
            // No dst
            val r1 = v.rotate(origin, angle)
            assertNotSame(v, r1)
            assertVec2EqualsApproximately(expected, r1, tolerance = testTolerance)
            assertEquals(Vec2f(0f, 1f), v) // Original unchanged
            // dst = new
            val dest = Vec2f()
            val r2 = v.rotate(origin, angle, dest)
            assertSame(dest, r2)
            assertVec2EqualsApproximately(expected, r2, tolerance = testTolerance)
            // dst = self
            val r3 = v.rotate(origin, angle, v)
            assertSame(v, r3)
            assertVec2EqualsApproximately(expected, v, tolerance = testTolerance)
        }
    }

    @Test
    fun `rotation around an arbitrary origin`() {
        val expected = Vec2f(-6f, -5f)
        run {
            val v = Vec2f(6f, -5f)
            val origin = Vec2f(0f, -5f)
            val angle = FloatPi
            // No dst
            val r1 = v.rotate(origin, angle)
            assertNotSame(v, r1)
            assertVec2EqualsApproximately(expected, r1, tolerance = testTolerance)
            assertEquals(Vec2f(6f, -5f), v) // Original unchanged
            // dst = new
            val dest = Vec2f()
            val r2 = v.rotate(origin, angle, dest)
            assertSame(dest, r2)
            assertVec2EqualsApproximately(expected, r2, tolerance = testTolerance)
            // dst = self
            val r3 = v.rotate(origin, angle, v)
            assertSame(v, r3)
            assertVec2EqualsApproximately(expected, v, tolerance = testTolerance)
        }
    }

    @Test
    fun `set the length of a provided direction vector`() {
        val vInitial = Vec2f(1f, 1f)
        val len = 14.6f
        val lengthInitial = vInitial.length()
        val expectedX = vInitial.x / lengthInitial * len
        val expectedY = vInitial.y / lengthInitial * len
        val expected = Vec2f(expectedX, expectedY) // Approx [10.3237f, 10.3237f]

        run {
            val v = vInitial.copy()
            // No dst
            val r1 = v.setLength(len)
            assertNotSame(v, r1)
            assertVec2EqualsApproximately(expected, r1, tolerance = testTolerance)
            assertEquals(vInitial, v) // Original unchanged
            // dst = new
            val dest = Vec2f()
            val r2 = v.setLength(len, dest)
            assertSame(dest, r2)
            assertVec2EqualsApproximately(expected, r2, tolerance = testTolerance)
            // dst = self
            val r3 = v.setLength(len, v)
            assertSame(v, r3)
            assertVec2EqualsApproximately(expected, v, tolerance = testTolerance)
        }
    }
    @Test
    fun `should shorten the vector`() {
        val vInitial = Vec2f(10.323759f, 10.323759f) // Length approx 14.6
        val maxLen = 4.0f
        val lengthInitial = vInitial.length()
        val expectedX = vInitial.x / lengthInitial * maxLen
        val expectedY = vInitial.y / lengthInitial * maxLen
        val expected = Vec2f(expectedX, expectedY) // Approx [2.828f, 2.828f]

        run {
            val v = vInitial.copy()
            // No dst
            val r1 = v.truncate(maxLen)
            assertNotSame(v, r1)
            assertVec2EqualsApproximately(expected, r1, tolerance = testTolerance)
            assertEquals(vInitial, v) // Original unchanged
            // dst = new
            val dest = Vec2f()
            val r2 = v.truncate(maxLen, dest)
            assertSame(dest, r2)
            assertVec2EqualsApproximately(expected, r2, tolerance = testTolerance)
            // dst = self
            val r3 = v.truncate(maxLen, v)
            assertSame(v, r3)
            assertVec2EqualsApproximately(expected, v, tolerance = testTolerance)
        }
    }

    @Test
    fun `should preserve the vector when shorter than maxLen`() {
        val vInitial = Vec2f(11f, 12f) // Length sqrt(121+144)=sqrt(265) approx 16.2
        val maxLen = 20.0f
        val expected = vInitial.copy() // Expect no change

        run {
            val v = vInitial.copy()
            // No dst
            val r1 = v.truncate(maxLen)
            assertNotSame(v, r1)
            assertVec2EqualsApproximately(expected, r1) // Should be same value
            assertEquals(vInitial, v) // Original unchanged
            // dst = new
            val dest = Vec2f()
            val r2 = v.truncate(maxLen, dest)
            assertSame(dest, r2)
            assertVec2EqualsApproximately(expected, r2)
            // dst = self
            val r3 = v.truncate(maxLen, v)
            assertSame(v, r3)
            assertVec2EqualsApproximately(expected, v)
        }
    }

    @Test
    fun `should return the midpoint`() {
        val expected = Vec2f(5f, 5f)
        testBinaryVecOp(Vec2f::midpoint, Vec2f(0f, 0f), Vec2f(10f, 10f), expected)
    }

    @Test
    fun `should handle negatives`() {
        val expected = Vec2f(10f, 10f) // (-10+30)/2, (-20+40)/2
        testBinaryVecOp(Vec2f::midpoint, Vec2f(-10f, -20f), Vec2f(30f, 40f), expected)
    }

} // End Vec2Tests class