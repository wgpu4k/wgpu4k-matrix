package io.github.natanfudge.wgpu4k.matrix.test

import io.github.natanfudge.wgpu4k.matrix.Mat3
import io.github.natanfudge.wgpu4k.matrix.Mat4
import io.github.natanfudge.wgpu4k.matrix.Quat
import io.github.natanfudge.wgpu4k.matrix.Vec3
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt
import kotlin.test.*

// --- Test Suite ---

class QuatTest {

    // Helper to safely copy a Quat without using the problematic copy() method
    private fun safeQuatCopy(q: Quat): Quat {
        return Quat(q.x, q.y, q.z, q.w)
    }

    // Helper to mimic the JS 'clone' for testing (creates a new instance)
    private fun clone(v: Any?): Any? {
        return when (v) {
            is Quat -> safeQuatCopy(v) // Use our safe copy method
            is Vec3 -> v.copy()
            is Mat3 -> Mat3.fromFloatArray(v.arrays.copyOf()) // Corrected: Use public factory
            is FloatArray -> v.copyOf()
            is DoubleArray -> v.copyOf()
            else -> v // Assume immutable primitives (like Double) or objects handled correctly
        }
    }

    // Helper testing function result without explicit destination
    private fun testQuatWithoutDest(
        operation: (args: Array<out Any?>, dst: Quat?) -> Any?, // Lambda representing the Quat operation
        expected: Any?,
        vararg args: Any?, // Original arguments for the operation
    ) {
        val clonedArgs = args.map { clone(it) }.toTypedArray()
        val d = operation(clonedArgs, null) // Operation should create a new Quat/Vec3/Double internally

        when (expected) {
            is Quat -> assertQuatEqualsApproximately(expected, d as Quat)
            is DoubleArray -> { // Expect DoubleArray for Quat results
                if (d is Quat) {
                    assertQuatEqualsApproximately(expected, d)
                } else {
                    fail("Expected Quat result when expected is DoubleArray")
                }
            }

            is Double -> assertEqualsApproximately(expected, d as Double)
            is Boolean -> assertEquals(expected, d as Boolean)
            is Vec3 -> assertVec3EqualsApproximately(expected, d as Vec3) // For transformVector
            else -> assertEquals(expected, d) // Fallback
        }

        // Check original args were not modified
        args.zip(clonedArgs).forEachIndexed { index, pair ->
            if (pair.first is Quat) {
                assertQuatEquals(pair.first as Quat, pair.second as Quat, "Source quat (arg $index) modified unexpectedly in testQuatWithoutDest")
            }
            if (pair.first is Vec3) {
                assertVec3Equals(pair.first as Vec3, pair.second as Vec3, "Source vector (arg $index) modified unexpectedly in testQuatWithoutDest")
            }
            if (pair.first is Mat3) {
                // Corrected: Compare Mat3 elements using indexer
                val mat1 = pair.first as Mat3
                val mat2 = pair.second as Mat3
                var equal = true
                for (i in 0 until 12) { // Compare all 12 elements
                    if (mat1[i] != mat2[i]) {
                        equal = false
                        break
                    }
                }
                assertTrue(equal, "Source matrix (arg $index) modified unexpectedly in testQuatWithoutDest")
            }
        }
    }

    // Helper testing function result with explicit destination (for Quat returning functions)
    private fun testQuatWithDest(
        operation: (args: Array<out Any?>, dst: Quat?) -> Quat?, // Lambda returns Quat?
        expected: Any?, // Can be Quat or DoubleArray
        vararg args: Any?, // Original arguments for the operation
    ) {
        val expectedCloned = clone(expected) // Clone expected value (Quat or DoubleArray)
        val destQuat = Quat() // Create the destination quat

        // --- Test with standard destination ---
        run {
            val clonedArgs = args.map { clone(it) }.toTypedArray()
            val c = operation(clonedArgs, destQuat)

            assertSame(c, destQuat, "Function with dest should return the dest instance")

            when (expectedCloned) {
                is Quat -> assertQuatEqualsApproximately(expectedCloned, c as Quat)
                is DoubleArray -> assertQuatEqualsApproximately(expectedCloned, c as Quat)
                else -> fail("testQuatWithDest expects Quat or DoubleArray for 'expected' value")
            }

            // Ensure original inputs were not modified
            args.zip(clonedArgs).forEachIndexed { index, pair ->
                if (pair.first is Quat) {
                    assertQuatEquals(
                        pair.first as Quat,
                        pair.second as Quat,
                        "Source quat (arg $index) modified unexpectedly in testQuatWithDest (standard dest)"
                    )
                }
                if (pair.first is Vec3) {
                    assertVec3Equals(
                        pair.first as Vec3,
                        pair.second as Vec3,
                        "Source vector (arg $index) modified unexpectedly in testQuatWithDest (standard dest)"
                    )
                }
                if (pair.first is Mat3) {
                    // Corrected: Compare Mat3 elements using indexer
                    val mat1 = pair.first as Mat3
                    val mat2 = pair.second as Mat3
                    var equal = true
                    for (i in 0 until 12) {
                        if (mat1[i] != mat2[i]) {
                            equal = false
                            break
                        }
                    }
                    assertTrue(equal, "Source matrix (arg $index) modified unexpectedly in testQuatWithDest (standard dest)")
                }
            }
        }

        // --- Test aliasing: first Quat argument is destination ---
        if (args.isNotEmpty() && args[0] is Quat) {
            val firstArgAlias = clone(args[0]) as Quat
            val clonedRemainingArgs = args.drop(1).map { clone(it) }.toTypedArray()
            val allArgsForAlias1 = arrayOf(firstArgAlias, *clonedRemainingArgs)

            val cAlias1 = operation(allArgsForAlias1, firstArgAlias)

            assertSame(cAlias1, firstArgAlias, "Aliasing test (firstArg == dest) should return the dest instance")
            when (expectedCloned) {
                is Quat -> assertQuatEqualsApproximately(expectedCloned, cAlias1 as Quat, message = "Aliasing test (firstArg == dest) result mismatch")
                is DoubleArray -> assertQuatEqualsApproximately(expectedCloned, cAlias1 as Quat, message = "Aliasing test (firstArg == dest) result mismatch")
            }
            // Check other original args were not modified
            args.drop(1).zip(clonedRemainingArgs).forEachIndexed { index, pair ->
                if (pair.first is Quat) {
                    assertQuatEquals(
                        pair.first as Quat,
                        pair.second as Quat,
                        "Aliasing test (firstArg == dest): Source quat (arg ${index + 1}) modified unexpectedly"
                    )
                }
                if (pair.first is Vec3) {
                    assertVec3Equals(
                        pair.first as Vec3,
                        pair.second as Vec3,
                        "Aliasing test (firstArg == dest): Source vector (arg ${index + 1}) modified unexpectedly"
                    )
                }
                if (pair.first is Mat3) {
                    // Corrected: Compare Mat3 elements using indexer
                    val mat1 = pair.first as Mat3
                    val mat2 = pair.second as Mat3
                    var equal = true
                    for (i in 0 until 12) {
                        if (mat1[i] != mat2[i]) {
                            equal = false
                            break
                        }
                    }
                    assertTrue(equal, "Aliasing test (firstArg == dest): Source matrix (arg ${index + 1}) modified unexpectedly")
                }
            }
        }

        // --- Test aliasing: another Quat argument is destination ---
        val quatOperandIndex = args.indexOfFirst { it is Quat && it !== args[0] } // Find first Quat operand *not* the first arg
        if (quatOperandIndex != -1) {
            val operandAlias = clone(args[quatOperandIndex]) as Quat // Clone the operand to use as dest
            val clonedArgsForAlias2 = args.mapIndexed { index, arg ->
                if (index == quatOperandIndex) operandAlias else clone(arg) // Use alias at its index, clone others
            }.toTypedArray()

            val cAlias2 = operation(clonedArgsForAlias2, operandAlias)

            assertSame(cAlias2, operandAlias, "Aliasing test (operand == dest) should return the dest instance")
            when (expectedCloned) {
                is Quat -> assertQuatEqualsApproximately(expectedCloned, cAlias2 as Quat, message = "Aliasing test (operand == dest) result mismatch")
                is DoubleArray -> assertQuatEqualsApproximately(expectedCloned, cAlias2 as Quat, message = "Aliasing test (operand == dest) result mismatch")
            }
            // Check original args (that were not the alias dest) were not modified
            args.zip(clonedArgsForAlias2).forEachIndexed { index, pair ->
                if (index != quatOperandIndex) { // Only check non-aliased args
                    if (pair.first is Quat) {
                        assertQuatEquals(
                            pair.first as Quat,
                            pair.second as Quat,
                            "Aliasing test (operand == dest): Source quat (arg $index) modified unexpectedly"
                        )
                    }
                    if (pair.first is Vec3) {
                        assertVec3Equals(
                            pair.first as Vec3,
                            pair.second as Vec3,
                            "Aliasing test (operand == dest): Source vector (arg $index) modified unexpectedly"
                        )
                    }
                    if (pair.first is Mat3) {
                        // Corrected: Compare Mat3 elements using indexer
                        val mat1 = pair.first as Mat3
                        val mat2 = pair.second as Mat3
                        var equal = true
                        for (i in 0 until 12) {
                            if (mat1[i] != mat2[i]) {
                                equal = false
                                break
                            }
                        }
                        assertTrue(equal, "Aliasing test (operand == dest): Source matrix (arg $index) modified unexpectedly")
                    }
                }
            }
        }
    }

    // Combined test helper for operations returning Quat
    private fun testQuatWithAndWithoutDest(
        // Operation must take Array<Any?> and Quat? dst, return Quat
        operation: (args: Array<out Any?>, dst: Quat?) -> Quat,
        expected: Any?, // Expected result (Quat or DoubleArray)
        vararg args: Any?, // Arguments for the operation
    ) {
        // Test without explicit destination
        testQuatWithoutDest({ a, d -> operation(a, d) as Any? }, expected, *args)
        // Test with explicit destination
        testQuatWithDest(operation, expected, *args)
    }

    // Helper for testing operations without explicit destination that return Vec3
    private fun testVec3WithoutDest(
        operation: (args: Array<out Any?>, dst: Vec3?) -> Any?, // Lambda representing the operation
        expected: Vec3, // Expected result
        vararg args: Any?, // Original arguments for the operation
    ) {
        val clonedArgs = args.map { clone(it) }.toTypedArray()
        val d = operation(clonedArgs, null) // Operation should create a new Vec3 internally

        assertVec3EqualsApproximately(expected, d as Vec3)

        // Check original args were not modified
        args.zip(clonedArgs).forEachIndexed { index, pair ->
            if (pair.first is Quat) {
                assertQuatEquals(pair.first as Quat, pair.second as Quat, "Source quat (arg $index) modified unexpectedly in testVec3WithoutDest")
            }
            if (pair.first is Vec3) {
                assertVec3Equals(pair.first as Vec3, pair.second as Vec3, "Source vec3 (arg $index) modified unexpectedly in testVec3WithoutDest")
            }
        }
    }

    // Combined test helper for operations returning Vec3 (e.g., transformVector)
    private fun testVec3WithAndWithoutDestFromQuatOp(
        operation: (args: Array<out Any?>, dst: Vec3?) -> Vec3, // Lambda returns Vec3
        expected: Vec3, // Expected result is Vec3
        vararg args: Any?, // Arguments (e.g., Quat, Vec3)
    ) {
        // Test without explicit destination
        testVec3WithoutDest({ a, d -> operation(a, d) }, expected, *args)

        // --- Test with explicit destination ---
        val destVec = Vec3()
        run {
            val clonedArgsWithDest = args.map { clone(it) }.toTypedArray()
            val c = operation(clonedArgsWithDest, destVec)
            assertSame(c, destVec, "Vec3 op: Function with dest should return the dest instance")
            assertVec3EqualsApproximately(expected, c)

            // Ensure original inputs were not modified
            args.zip(clonedArgsWithDest).forEachIndexed { index, pair ->
                if (pair.first is Quat) assertQuatEquals(pair.first as Quat, pair.second as Quat, "Source quat (arg $index) modified (with dest)")
                if (pair.first is Vec3) assertVec3Equals(pair.first as Vec3, pair.second as Vec3, "Source vec3 (arg $index) modified (with dest)")
            }
        }

        // --- Test aliasing: Vec3 argument is destination ---
        val vec3ArgIndex = args.indexOfFirst { it is Vec3 }
        if (vec3ArgIndex != -1) {
            val vec3Alias = clone(args[vec3ArgIndex]) as Vec3
            val clonedArgsForAlias = args.mapIndexed { index, arg ->
                if (index == vec3ArgIndex) vec3Alias else clone(arg)
            }.toTypedArray()

            val cAlias = operation(clonedArgsForAlias, vec3Alias)
            assertSame(cAlias, vec3Alias, "Vec3 op: Aliasing test (vec3 arg == dest) should return the dest instance")
            assertVec3EqualsApproximately(expected, cAlias, message = "Vec3 op: Aliasing test (vec3 arg == dest) result mismatch")

            // Check other original args were not modified
            args.zip(clonedArgsForAlias).forEachIndexed { index, pair ->
                if (index != vec3ArgIndex) {
                    if (pair.first is Quat) assertQuatEquals(
                        pair.first as Quat,
                        pair.second as Quat,
                        "Vec3 op: Aliasing test (vec3 arg == dest): Source quat (arg $index) modified"
                    )
                    // No need to check Vec3 if it's not the aliased one, but we do need to check if it exists
                    if (pair.first is Vec3) assertVec3Equals(
                        pair.first as Vec3,
                        pair.second as Vec3,
                        "Vec3 op: Aliasing test (vec3 arg == dest): Source vec3 (arg $index) modified"
                    )
                }
            }
        }
    }


    // --- Actual Tests (Following JS structure) ---

    // Create Quat instances using Double
    private fun q(x: Double, y: Double, z: Double, w: Double) = Quat(x, y, z, w)
    private fun q(da: DoubleArray): Quat {
        assertEquals(4, da.size)
        return Quat(da[0], da[1], da[2], da[3])
    }

    // Create Vec3 instances (assuming Float)
    private fun v(x: Float, y: Float, z: Float) = Vec3(x, y, z)
    private fun v(fa: FloatArray): Vec3 {
        assertEquals(3, fa.size)
        return Vec3(fa[0], fa[1], fa[2])
    }

    // Helper to convert Vec3 Floats to Doubles for Quat calcs if needed
    private fun vToDoubles(v: Vec3) = doubleArrayOf(v.x.toDouble(), v.y.toDouble(), v.z.toDouble())

    // Helper to convert Vec3 Doubles back to Floats if result is Vec3
    private fun vFromDoubles(d: DoubleArray) = Vec3(d[0].toFloat(), d[1].toFloat(), d[2].toFloat())


    @Test
    fun `should add`() {
        val expected = q(3.0, 5.0, 7.0, 9.0)
        val addOp = { args: Array<out Any?>, dst: Quat? ->
            // Instance method add(other: Quat, dst: Quat?): Quat
            (args[0] as Quat).add(args[1] as Quat, dst)
        }
        testQuatWithAndWithoutDest(addOp, expected, q(1.0, 2.0, 3.0, 4.0), q(2.0, 3.0, 4.0, 5.0))
    }

    @Test
    fun `should equals approximately`() {
        val q1 = q(1.0, 2.0, 3.0, 4.0)
        assertTrue(q1.equalsApproximately(q(1.0, 2.0, 3.0, 4.0)))
        assertTrue(q1.equalsApproximately(q(1.0 + TEST_EPSILON_D * 0.5, 2.0, 3.0, 4.0)))
        assertTrue(q1.equalsApproximately(q(1.0, 2.0 + TEST_EPSILON_D * 0.5, 3.0, 4.0)))
        assertTrue(q1.equalsApproximately(q(1.0, 2.0, 3.0 + TEST_EPSILON_D * 0.5, 4.0)))
        assertTrue(q1.equalsApproximately(q(1.0, 2.0, 3.0, 4.0 + TEST_EPSILON_D * 0.5)))
        assertFalse(q1.equalsApproximately(q(1.0001, 2.0, 3.0, 4.0)))
        assertFalse(q1.equalsApproximately(q(1.0, 2.0001, 3.0, 4.0)))
        assertFalse(q1.equalsApproximately(q(1.0, 2.0, 3.0001, 4.0)))
        assertFalse(q1.equalsApproximately(q(1.0, 2.0, 3.0, 4.0001)))
        // Using instance method q1.equalsApproximately(q2)
    }

    @Test
    fun `should equals`() {
        val q1 = q(1.0, 2.0, 3.0, 4.0)
        assertTrue(q1.equals(q(1.0, 2.0, 3.0, 4.0))) // Exact equality check
        assertFalse(q1.equals(q(1.0 + TEST_EPSILON_D * 0.5, 2.0, 3.0, 4.0)))
        assertFalse(q1.equals(q(1.0, 2.0 + TEST_EPSILON_D * 0.5, 3.0, 4.0)))
        assertFalse(q1.equals(q(1.0, 2.0, 3.0 + TEST_EPSILON_D * 0.5, 4.0)))
        assertFalse(q1.equals(q(1.0, 2.0, 3.0, 4.0 + TEST_EPSILON_D * 0.5)))
        // Using instance method q1.equals(q2) (likely data class equals)
    }

    @Test
    fun `should subtract`() {
        val expected = q(-1.0, -2.0, -3.0, -4.0)
        val subOp = { args: Array<out Any?>, dst: Quat? ->
            (args[0] as Quat).subtract(args[1] as Quat, dst)
        }
        testQuatWithAndWithoutDest(subOp, expected, q(1.0, 2.0, 3.0, 4.0), q(2.0, 4.0, 6.0, 8.0))
    }

    @Test
    fun `should sub`() { // Alias for subtract
        val expected = q(-1.0, -2.0, -3.0, -4.0)
        val subOp = { args: Array<out Any?>, dst: Quat? ->
            (args[0] as Quat).sub(args[1] as Quat, dst)
        }
        testQuatWithAndWithoutDest(subOp, expected, q(1.0, 2.0, 3.0, 4.0), q(2.0, 4.0, 6.0, 8.0))
    }

    @Test
    fun `should lerp`() {
        val expected = q(1.5, 3.0, 4.5, 6.0)
        val lerpOp = { args: Array<out Any?>, dst: Quat? ->
            // lerp(other: Quat, t: Double, dst: Quat?): Quat
            (args[0] as Quat).lerp(args[1] as Quat, args[2] as Double, dst)
        }
        testQuatWithAndWithoutDest(lerpOp, expected, q(1.0, 2.0, 3.0, 4.0), q(2.0, 4.0, 6.0, 8.0), 0.5)
    }

    @Test
    fun `should lerp under 0`() {
        val expected = q(0.5, 1.0, 1.5, 2.0)
        val lerpOp = { args: Array<out Any?>, dst: Quat? ->
            (args[0] as Quat).lerp(args[1] as Quat, args[2] as Double, dst)
        }
        testQuatWithAndWithoutDest(lerpOp, expected, q(1.0, 2.0, 3.0, 4.0), q(2.0, 4.0, 6.0, 8.0), -0.5)
    }

    @Test
    fun `should lerp over 1`() {
        val expected = q(2.5, 5.0, 7.5, 10.0)
        val lerpOp = { args: Array<out Any?>, dst: Quat? ->
            (args[0] as Quat).lerp(args[1] as Quat, args[2] as Double, dst)
        }
        testQuatWithAndWithoutDest(lerpOp, expected, q(1.0, 2.0, 3.0, 4.0), q(2.0, 4.0, 6.0, 8.0), 1.5)
    }


    @Test
    fun `should multiply by scalar`() {
        val expected = q(2.0, 4.0, 6.0, 8.0)
        val mulScalarOp = { args: Array<out Any?>, dst: Quat? ->
            // mulScalar(scalar: Double, dst: Quat?): Quat
            (args[0] as Quat).mulScalar(args[1] as Double, dst)
        }
        testQuatWithAndWithoutDest(mulScalarOp, expected, q(1.0, 2.0, 3.0, 4.0), 2.0)
    }

    @Test
    fun `should scale`() { // Alias for multiply by scalar
        val expected = q(2.0, 4.0, 6.0, 8.0)
        val scaleOp = { args: Array<out Any?>, dst: Quat? ->
            // scale(scalar: Double, dst: Quat?): Quat
            (args[0] as Quat).scale(args[1] as Double, dst)
        }
        testQuatWithAndWithoutDest(scaleOp, expected, q(1.0, 2.0, 3.0, 4.0), 2.0)
    }

    // Removed `should divide by scalar` test as divScalar does not exist in Quat.kt

    @Test
    fun `should invert`() {
        // q = (x, y, z, w) = (2, 3, -4, -8)
        val lenSq = 4.0 + 9.0 + 16.0 + 64.0 // 93.0
        val expected = q(-2.0 / lenSq, -3.0 / lenSq, 4.0 / lenSq, -8.0 / lenSq)

        val inverseOp = { args: Array<out Any?>, dst: Quat? ->
            // inverse(dst: Quat?): Quat
            (args[0] as Quat).inverse(dst)
        }
        testQuatWithAndWithoutDest(inverseOp, expected, q(2.0, 3.0, -4.0, -8.0))
    }


    @Test
    fun `should compute dot product`() {
        val expected = 1.0 * 2.0 + 2.0 * 4.0 + 3.0 * 6.0 + 4.0 * 8.0 // 2 + 8 + 18 + 32 = 60.0
        // dot(other: Quat): Double
        val value = q(1.0, 2.0, 3.0, 4.0).dot(q(2.0, 4.0, 6.0, 8.0))
        assertEqualsApproximately(expected, value)
    }

    @Test
    fun `should compute length`() {
        val expected = sqrt(1.0 * 1.0 + 2.0 * 2.0 + 3.0 * 3.0 + 4.0 * 4.0) // sqrt(30.0)
        // length property
        val value = q(1.0, 2.0, 3.0, 4.0).length
        assertEqualsApproximately(expected, value)
    }

    @Test
    fun `should compute length squared`() {
        val expected = 1.0 * 1.0 + 2.0 * 2.0 + 3.0 * 3.0 + 4.0 * 4.0 // 30.0
        // lengthSq property
        val value = q(1.0, 2.0, 3.0, 4.0).lengthSq
        assertEqualsApproximately(expected, value)
    }

    @Test
    fun `should compute len`() { // Alias for length
        val expected = sqrt(1.0 * 1.0 + 2.0 * 2.0 + 3.0 * 3.0 + 4.0 * 4.0)
        // len property
        val value = q(1.0, 2.0, 3.0, 4.0).len
        assertEqualsApproximately(expected, value)
    }

    @Test
    fun `should compute lenSq`() { // Alias for length squared
        val expected = 1.0 * 1.0 + 2.0 * 2.0 + 3.0 * 3.0 + 4.0 * 4.0
        // lenSq property
        val value = q(1.0, 2.0, 3.0, 4.0).lenSq
        assertEqualsApproximately(expected, value)
    }

    @Test
    fun `should normalize`() {
// Calculate the length/magnitude
        val length = sqrt(1.0 * 1.0 + 2.0 * 2.0 + 3.0 * 3.0 + 4.0 * 4.0) // Use 1.0, 2.0 etc for Double calculation

// Define the expected normalized quaternion
        val expected = Quat(
            1.0 / length,
            2.0 / length,
            3.0 / length,
            4.0 / length
        )

// Call the test helper function
        testQuatWithAndWithoutDest(
            // Lambda function representing the normalization operation
            { a, dst -> (a[0] as Quat).normalize(dst) }, // Kotlin lambda syntax
            // Expected result
            expected,
            // Input quaternion (as an array of Doubles)
            Quat(1.0, 2.0, 3.0, 4.0)
        )
    }

    @Test
    fun `should copy`() {
        val expected = q(1.0, 2.0, 3.0, 4.0)
        val v = q(1.0, 2.0, 3.0, 4.0)
        // Test our safe copy method
        val resultNoDest = safeQuatCopy(v)
        assertNotSame(v, resultNoDest, "safeQuatCopy() should return a new instance")
        assertQuatEqualsApproximately(expected, resultNoDest)

        // Test instance set(x, y, z, w) method for "copying with destination"
        val dest = Quat()
        val resultSet = dest.set(v.x, v.y, v.z, v.w) // set(x, y, z, w): Quat
        assertSame(dest, resultSet, "set(x,y,z,w) should return the dest instance")
        assertQuatEqualsApproximately(expected, resultSet)
        // Verify original 'v' wasn't somehow modified by dest.set
        assertQuatEquals(q(1.0, 2.0, 3.0, 4.0), v, "Original quat modified during set()")
    }

    @Test
    fun `should clone`() { // Tests data class copy()
        val expected = q(1.0, 2.0, 3.0, 4.0)
        val v = q(1.0, 2.0, 3.0, 4.0)
        val result = safeQuatCopy(v) // Using our safe copy method
        assertNotSame(v, result, "clone/safeQuatCopy() should return a new instance")
        assertQuatEqualsApproximately(expected, result)
    }


    @Test
    fun `should set from another quat`() { // Renamed test, uses instance set(other)
        val expected = q(2.0, 3.0, 4.0, 5.0)
        val source = q(2.0, 3.0, 4.0, 5.0)
        val dest = Quat() // Start with identity or zero

        val result = dest.set(source.x, source.y, source.z, source.w) // Instance method set(x, y, z, w): Quat

        assertSame(result, dest, "set(other) should return dest")
        assertQuatEqualsApproximately(expected, result)
        // Ensure source wasn't modified
        assertQuatEquals(q(2.0, 3.0, 4.0, 5.0), source, "Source quat modified during set()")
    }


    @Test
    fun `should multiply`() {
        // x = 4*5 + 1*8 + 2*7 - 3*6 = 20 + 8 + 14 - 18 = 24
        // y = 4*6 + 2*8 + 3*5 - 1*7 = 24 + 16 + 15 - 7  = 48
        // z = 4*7 + 3*8 + 1*6 - 2*5 = 28 + 24 + 6 - 10  = 48
        // w = 4*8 - 1*5 - 2*6 - 3*7 = 32 - 5 - 12 - 21 = -6
        val expected = q(24.0, 48.0, 48.0, -6.0)
        val multOp = { args: Array<out Any?>, dst: Quat? ->
            // multiply(other: Quat, dst: Quat?): Quat
            (args[0] as Quat).multiply(args[1] as Quat, dst)
        }
        testQuatWithAndWithoutDest(multOp, expected, q(1.0, 2.0, 3.0, 4.0), q(5.0, 6.0, 7.0, 8.0))
    }

    @Test
    fun `should mul`() { // Alias for multiply
        val expected = q(24.0, 48.0, 48.0, -6.0)
        val multOp = { args: Array<out Any?>, dst: Quat? ->
            // mul(other: Quat, dst: Quat?): Quat
            (args[0] as Quat).mul(args[1] as Quat, dst)
        }
        testQuatWithAndWithoutDest(multOp, expected, q(1.0, 2.0, 3.0, 4.0), q(5.0, 6.0, 7.0, 8.0))
    }

    @Test
    fun `should rotateX`() {
        val halfPi = PI / 2.0
        val s = sin(halfPi * 0.5)
        val c = cos(halfPi * 0.5)
        val expected = q(s, 0.0, 0.0, c) // [sqrt(0.5), 0, 0, sqrt(0.5)]

        val rotateXOp = { args: Array<out Any?>, dst: Quat? ->
            // rotateX(angleInRadians: Double, dst: Quat?): Quat
            (args[0] as Quat).rotateX(args[1] as Double, dst)
        }
        testQuatWithAndWithoutDest(rotateXOp, expected, Quat.identity(), halfPi) // Start from identity
    }

    @Test
    fun `should rotateY`() {
        val halfPi = PI / 2.0
        val s = sin(halfPi * 0.5)
        val c = cos(halfPi * 0.5)
        val expected = q(0.0, s, 0.0, c) // [0, sqrt(0.5), 0, sqrt(0.5)]

        val rotateYOp = { args: Array<out Any?>, dst: Quat? ->
            // rotateY(angleInRadians: Double, dst: Quat?): Quat
            (args[0] as Quat).rotateY(args[1] as Double, dst)
        }
        testQuatWithAndWithoutDest(rotateYOp, expected, Quat.identity(), halfPi)
    }

    @Test
    fun `should rotateZ`() {
        val halfPi = PI / 2.0
        val s = sin(halfPi * 0.5)
        val c = cos(halfPi * 0.5)
        val expected = q(0.0, 0.0, s, c) // [0, 0, sqrt(0.5), sqrt(0.5)]

        val rotateZOp = { args: Array<out Any?>, dst: Quat? ->
            // rotateZ(angleInRadians: Double, dst: Quat?): Quat
            (args[0] as Quat).rotateZ(args[1] as Double, dst)
        }
        testQuatWithAndWithoutDest(rotateZOp, expected, Quat.identity(), halfPi)
    }


    @Test
    fun `should conjugate`() {
        val expected = q(-1.0, -2.0, -3.0, 4.0)
        val conjugateOp = { args: Array<out Any?>, dst: Quat? ->
            // conjugate(dst: Quat?): Quat
            (args[0] as Quat).conjugate(dst)
        }
        testQuatWithAndWithoutDest(conjugateOp, expected, q(1.0, 2.0, 3.0, 4.0))
    }

    @Test
    fun `should create identity using companion object`() { // Renamed test
        val expected = q(0.0, 0.0, 0.0, 1.0)
        // static identity(dst: Quat?): Quat
        val ident = Quat.identity()
        assertQuatEqualsApproximately(expected, ident)
        // Test with destination
        val dest = q(1.0, 2.0, 3.0, 4.0)
        val identDest = Quat.identity(dest)
        assertSame(dest, identDest)
        assertQuatEqualsApproximately(expected, identDest)
    }

    @Test
    fun `should set identity using instance method`() { // Renamed test
        val expected = q(0.0, 0.0, 0.0, 1.0)
        // Test setting an existing quaternion to identity using the static method with a destination
        val dest = q(1.0, 2.0, 3.0, 4.0)
        val result = Quat.identity(dest) // static identity(dst: Quat?): Quat

        assertSame(dest, result, "Static identity(dst) should return dst")
        assertQuatEqualsApproximately(expected, result)
    }

    @Test
    fun `should create from axis angle using companion object`() { // Renamed test
        val axis = v(1f, 2f, 3f).normalize() // Vec3 uses Float
        val angle = PI / 2.0 // Use Double for angle
        val s = sin(angle * 0.5)
        val c = cos(angle * 0.5)
        // Convert Vec3 axis components to Double for Quat creation
        val expected = q(axis.x.toDouble() * s, axis.y.toDouble() * s, axis.z.toDouble() * s, c)

        val setAxisAngleOp = { args: Array<out Any?>, dst: Quat? ->
            // static fromAxisAngle(axis: Vec3, angleInRadians: Double, dst: Quat?): Quat
            Quat.fromAxisAngle(args[0] as Vec3, args[1] as Double, dst)
        }

        testQuatWithAndWithoutDest(setAxisAngleOp, expected, axis, angle)
    }

    @Test
    fun `should get axis angle`() {
        val angleIn = PI / 2.0 // Double
        val axisIn = v(1f, 2f, 3f).normalize() // Float Vec3
        val qIn = Quat.fromAxisAngle(axisIn, angleIn) // Create quat from known axis/angle

        val resultAxis = Vec3() // Destination for the axis (Float Vec3)
        // toAxisAngle(dstAxis: Vec3?): Pair<Double, Vec3>
        val (resultAngle, resultAxisFromPair) = qIn.toAxisAngle(resultAxis) // Returns Pair<Double, Vec3>
        // resultAxis should be the same as resultAxisFromPair since we passed it as the destination

        assertEqualsApproximately(angleIn, resultAngle, message = "Extracted angle mismatch")
        // Check if axisIn and resultAxis are parallel
        val dot = axisIn.dot(resultAxis) // Vec3 dot Vec3 -> Float
        assertEqualsApproximately(1.0f, dot, tolerance = TEST_EPSILON_F, message = "Extracted axis direction mismatch (dot product = $dot)")
    }

    @Test
    fun `should slerp`() {
        val axis = v(0f, 0f, 1f) // Float Vec3
        val start = Quat.fromAxisAngle(axis, 0.0) // Identity quat
        val end = Quat.fromAxisAngle(axis, PI) // 180 deg rot around Z (Double angle)
        // end should be q(0, 0, sin(pi/2), cos(pi/2)) = q(0, 0, 1, 0)
        assertQuatEqualsApproximately(q(0.0, 0.0, 1.0, 0.0), end, tolerance = 1e-7) // Increase tolerance slightly

        val t0 = 0.0
        val t1 = 1.0
        val t0_5 = 0.5

        val expected0 = start.copy() // Should be identity q(0,0,0,1)
        val expected1 = end.copy()   // Should be q(0,0,1,0)
        val expected0_5 = Quat.fromAxisAngle(axis, PI * 0.5) // 90 deg rot: q(0, 0, sin(pi/4), cos(pi/4))
        // q(0, 0, sqrt(0.5), sqrt(0.5))
        assertQuatEqualsApproximately(q(0.0, 0.0, sqrt(0.5), sqrt(0.5)), expected0_5, tolerance = 1e-7)

        val slerpOp = { args: Array<out Any?>, dst: Quat? ->
            // slerp(other: Quat, t: Double, dst: Quat?): Quat
            (args[0] as Quat).slerp(args[1] as Quat, args[2] as Double, dst)
        }

        testQuatWithAndWithoutDest(slerpOp, expected0, start, end, t0)
        testQuatWithAndWithoutDest(slerpOp, expected1, start, end, t1)
        testQuatWithAndWithoutDest(slerpOp, expected0_5, start, end, t0_5)
    }

    @Test
    fun `should slerp 2`() {
        val a1 = Quat(0.0, 1.0, 0.0, 1.0)
        val b1 = Quat(1.0, 0.0, 0.0, 1.0)
        val a2 = Quat(0.0, 1.0, 0.0, 1.0)
        val b2 = Quat(0.0, 1.0, 0.0, 0.5)
        val a3 = Quat.fromEuler(0.1, 0.2, 0.3, "xyz")
        val b3 = Quat.fromEuler(0.3, 0.2, 0.1, "xyz")

        val tests = listOf(
            mapOf("a" to a1, "b" to b1, "t" to 0.0, "expected" to Quat(0.0, 1.0, 0.0, 1.0)),
            mapOf("a" to a1, "b" to b1, "t" to 1.0, "expected" to Quat(1.0, 0.0, 0.0, 1.0)),
            mapOf("a" to a1, "b" to b1, "t" to 0.5, "expected" to Quat(0.5, 0.5, 0.0, 1.0)),
            mapOf("a" to a2, "b" to b2, "t" to 0.5, "expected" to Quat(0.0, 1.0, 0.0, 0.75)),
            mapOf("a" to a3, "b" to b3, "t" to 0.5, "expected" to Quat(0.1089731245591333, 0.09134010671547867, 0.10897312455913327, 0.9838224947381737))
        )

        for (test in tests) {
            // We need to explicitly cast types when retrieving from the map
            val a = test["a"] as Any // Use Any or the specific Quat type if available
            val b = test["b"] as Any // Use Any or the specific Quat type if available
            val t = test["t"] as Double
            val expected = test["expected"] as Any // Use Any or the specific Quat type if available

            testQuatWithAndWithoutDest({ args, dst ->
                (args[0] as Quat).slerp(args[1] as Quat, args[2] as Double, dst)
            }, expected, a, b, t)
        }
    }


    @Test
    fun `should slerp with opposite hemisphere`() {
        val axis = v(0f, 0f, 1f) // Float Vec3
        val q1 = Quat.fromAxisAngle(axis, PI * 0.25) // 45 deg rot Z
        val q2 = Quat.fromAxisAngle(axis, PI * 1.75) // 315 deg rot Z

        assertTrue(q1.dot(q2) < 0, "q1 and q2 should be in opposite hemispheres (dot=${q1.dot(q2)})")

        val t = 0.5
        // Expected result: slerp interpolates shortest path. Halfway between 45 and 315 (-45) is 0 degrees.
        val expected = Quat.identity()

        val slerpOp = { args: Array<out Any?>, dst: Quat? ->
            (args[0] as Quat).slerp(args[1] as Quat, args[2] as Double, dst)
        }

        // Verify calculation - slerp should handle the shortest path internally.
        val resultNoDest = slerpOp(arrayOf(q1, q2, t), null)
        assertQuatEqualsApproximately(expected, resultNoDest, tolerance = 1e-7)

        testQuatWithAndWithoutDest(slerpOp, expected, q1, q2, t)
    }

    // Helper function to convert Quat (Double) to FloatArray for Mat3.fromQuat
    private fun quatToFloatArray(q: Quat): FloatArray {
        return floatArrayOf(q.x.toFloat(), q.y.toFloat(), q.z.toFloat(), q.w.toFloat())
    }

    @Test
    fun `should create from rotation matrix`() {
// Assuming Mat4, Mat3, Quat types and related functions (identity, rotationX/Y/Z, fromMat4, fromMat) exist
// Also assuming testQuatWithAndWithoutDest is defined similarly

        // Define a helper structure or use Pair/Map if preferred
        data class MatrixTestData(val expected: Any, val mat: Any) // Using Any for flexibility, replace with specific types like Quat, Mat4, Mat3 if available

        val initialTests = listOf(
            MatrixTestData(Quat(0.0, 0.0, 0.0, 1.0), Mat4.identity()),
            MatrixTestData(Quat(1.0, 0.0, 0.0, 0.0), Mat4.rotationX(PI.toFloat())),
            MatrixTestData(Quat(0.0, 1.0, 0.0, 0.0), Mat4.rotationY(PI.toFloat())),
            MatrixTestData(Quat(0.0, 0.0, 1.0, 0.0), Mat4.rotationZ(PI.toFloat()))
        )

// Use flatMap to create both Mat4 and Mat3 test cases
        val tests = initialTests.flatMap { testData ->
            val originalMat4 = testData.mat // Assuming it's a Mat4 type
            listOf(
                // Keep the original test case (with Mat4)
                testData,
                // Create a new test case with Mat3 derived from the Mat4
                MatrixTestData(testData.expected, Mat3.fromMat4(originalMat4 as Mat4))
            )
        }

// Iterate through the combined list of tests
        for ((expectedQuat, matrix) in tests) {
            testQuatWithAndWithoutDest(
                // Lambda function representing the operation to test
                {
                matArg, dstArg ->
//                    check(matArg is Mat4)
                    Quat.fromMat(matArg[0] as Any, dstArg)
                },
                // Expected result (quaternion)
                expectedQuat,
                // Argument for the operation (the matrix)
                matrix
            )
        }
    }

    @Test
    fun `should transform vector`() {
        val halfPi = PI / 2.0 // Double
        val qRotX = Quat.fromAxisAngle(v(1f, 0f, 0f), halfPi)
        val qRotY = Quat.fromAxisAngle(v(0f, 1f, 0f), halfPi)
        val qRotZ = Quat.fromAxisAngle(v(0f, 0f, 1f), halfPi)
        val vecIn = v(1f, 2f, 3f) // Float Vec3

        // Expected results (Float Vec3)
        val expectedX = v(1f, -3f, 2f)
        val expectedY = v(3f, 2f, -1f)
        val expectedZ = v(-2f, 1f, 3f)

        // Helper function to transform a vector by a quaternion
        fun transformVector(q: Quat, v: Vec3, dst: Vec3? = null): Vec3 {
            val target = dst ?: Vec3()

            // Implementation of quaternion-vector transformation
            // Formula: v' = q * v * q^-1 (where v is treated as a quaternion with w=0)
            // Optimized implementation:
            val qx = q.x
            val qy = q.y
            val qz = q.z
            val qw = q.w

            // Calculate q * v (treating v as quaternion with w=0)
            val tx = qw * v.x + qy * v.z - qz * v.y
            val ty = qw * v.y + qz * v.x - qx * v.z
            val tz = qw * v.z + qx * v.y - qy * v.x
            val tw = -qx * v.x - qy * v.y - qz * v.z

            // Calculate (q * v) * q^-1
            target.x = (tx * qw + tw * -qx + ty * -qz - tz * -qy).toFloat()
            target.y = (ty * qw + tw * -qy + tz * -qx - tx * -qz).toFloat()
            target.z = (tz * qw + tw * -qz + tx * -qy - ty * -qx).toFloat()

            return target
        }

        val transformOp = { args: Array<out Any?>, dst: Vec3? ->
            // Use our helper function instead of a method on Quat
            transformVector(args[0] as Quat, args[1] as Vec3, dst)
        }

        // Use the Vec3 test helper
        testVec3WithAndWithoutDestFromQuatOp(transformOp, expectedX, qRotX, vecIn)
        testVec3WithAndWithoutDestFromQuatOp(transformOp, expectedY, qRotY, vecIn)
        testVec3WithAndWithoutDestFromQuatOp(transformOp, expectedZ, qRotZ, vecIn)
    }

}
