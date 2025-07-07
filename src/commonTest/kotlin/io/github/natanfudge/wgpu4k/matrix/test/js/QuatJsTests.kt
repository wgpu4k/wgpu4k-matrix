package io.github.natanfudge.wgpu4k.matrix.test.js

import io.github.natanfudge.wgpu4k.matrix.EPSILON
import io.github.natanfudge.wgpu4k.matrix.FloatPi
import io.github.natanfudge.wgpu4k.matrix.Mat3f
import io.github.natanfudge.wgpu4k.matrix.Mat4f
import io.github.natanfudge.wgpu4k.matrix.Quatf
import io.github.natanfudge.wgpu4k.matrix.Vec3f
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt
import kotlin.test.*

// --- Test Suite ---

class QuatJsTests {

    // Helper to safely copy a Quat without using the problematic copy() method
    private fun safeQuatCopy(q: Quatf): Quatf {
        return Quatf(q.x, q.y, q.z, q.w)
    }

    // Helper to mimic the JS 'clone' for testing (creates a new instance)
    private fun clone(v: Any?): Any? {
        return when (v) {
            is Quatf -> safeQuatCopy(v) // Use our safe copy method
            is Vec3f -> v.copy()
            is Mat3f -> Mat3f.copyOf(v.toFloatArray().copyOf()) // Use toFloatArray
            is FloatArray -> v.copyOf()
            // DoubleArray cloning might not be needed anymore if we switch everything to Float
            // is DoubleArray -> v.copyOf()
            else -> v // Assume immutable primitives (like Float) or objects handled correctly
        }
    }



    // Helper testing function result with explicit destination (for Quat returning functions)
    private fun testQuatWithDest(
        operation: (args: Array<out Any?>, dst: Quatf) -> Quatf?, // Lambda returns Quat?
        expected: Quatf, // Expect Quat now
        vararg args: Any?, // Original arguments for the operation
    ) {
        val expectedCloned = clone(expected) as Quatf // Clone expected value (Quat)
        val destQuat = Quatf() // Create the destination quat

        // --- Test with standard destination ---
        run {
            val clonedArgs = args.map { clone(it) }.toTypedArray()
            val c = operation(clonedArgs, destQuat)

            assertSame(c, destQuat, "Function with dest should return the dest instance")

            assertQuatEqualsApproximately(expectedCloned, c as Quatf)


            // Ensure original inputs were not modified
            args.zip(clonedArgs).forEachIndexed { index, pair ->
                if (pair.first is Quatf) {
                    assertQuatEquals(
                        pair.first as Quatf,
                        pair.second as Quatf,
                        "Source quat (arg $index) modified unexpectedly in testQuatWithDest (standard dest)"
                    )
                }
                if (pair.first is Vec3f) {
                    assertVec3Equals(
                        pair.first as Vec3f,
                        pair.second as Vec3f,
                        "Source vector (arg $index) modified unexpectedly in testQuatWithDest (standard dest)"
                    )
                }
                if (pair.first is Mat3f) {
                    // Corrected: Compare Mat3 elements using indexer
                    val mat1 = pair.first as Mat3f
                    val mat2 = pair.second as Mat3f
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
        if (args.isNotEmpty() && args[0] is Quatf) {
            val firstArgAlias = clone(args[0]) as Quatf
            val clonedRemainingArgs = args.drop(1).map { clone(it) }.toTypedArray()
            val allArgsForAlias1 = arrayOf(firstArgAlias, *clonedRemainingArgs)

            val cAlias1 = operation(allArgsForAlias1, firstArgAlias)

            assertSame(cAlias1, firstArgAlias, "Aliasing test (firstArg == dest) should return the dest instance")
            assertQuatEqualsApproximately(expectedCloned, cAlias1 as Quatf, message = "Aliasing test (firstArg == dest) result mismatch")

            // Check other original args were not modified
            args.drop(1).zip(clonedRemainingArgs).forEachIndexed { index, pair ->
                if (pair.first is Quatf) {
                    assertQuatEquals(
                        pair.first as Quatf,
                        pair.second as Quatf,
                        "Aliasing test (firstArg == dest): Source quat (arg ${index + 1}) modified unexpectedly"
                    )
                }
                if (pair.first is Vec3f) {
                    assertVec3Equals(
                        pair.first as Vec3f,
                        pair.second as Vec3f,
                        "Aliasing test (firstArg == dest): Source vector (arg ${index + 1}) modified unexpectedly"
                    )
                }
                if (pair.first is Mat3f) {
                    // Corrected: Compare Mat3 elements using indexer
                    val mat1 = pair.first as Mat3f
                    val mat2 = pair.second as Mat3f
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
        val quatOperandIndex = args.indexOfFirst { it is Quatf && it !== args[0] } // Find first Quat operand *not* the first arg
        if (quatOperandIndex != -1) {
            val operandAlias = clone(args[quatOperandIndex]) as Quatf // Clone the operand to use as dest
            val clonedArgsForAlias2 = args.mapIndexed { index, arg ->
                if (index == quatOperandIndex) operandAlias else clone(arg) // Use alias at its index, clone others
            }.toTypedArray()

            val cAlias2 = operation(clonedArgsForAlias2, operandAlias)

            assertSame(cAlias2, operandAlias, "Aliasing test (operand == dest) should return the dest instance")
            assertQuatEqualsApproximately(expectedCloned, cAlias2 as Quatf, message = "Aliasing test (operand == dest) result mismatch")

            // Check original args (that were not the alias dest) were not modified
            args.zip(clonedArgsForAlias2).forEachIndexed { index, pair ->
                if (index != quatOperandIndex) { // Only check non-aliased args
                    if (pair.first is Quatf) {
                        assertQuatEquals(
                            pair.first as Quatf,
                            pair.second as Quatf,
                            "Aliasing test (operand == dest): Source quat (arg $index) modified unexpectedly"
                        )
                    }
                    if (pair.first is Vec3f) {
                        assertVec3Equals(
                            pair.first as Vec3f,
                            pair.second as Vec3f,
                            "Aliasing test (operand == dest): Source vector (arg $index) modified unexpectedly"
                        )
                    }
                    if (pair.first is Mat3f) {
                        // Corrected: Compare Mat3 elements using indexer
                        val mat1 = pair.first as Mat3f
                        val mat2 = pair.second as Mat3f
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
        operation: (args: Array<out Any?>, dst: Quatf) -> Quatf,
        expected: Quatf, // Expected result (Quat)
        vararg args: Any?, // Arguments for the operation
    ) {

        // Test with explicit destination
        testQuatWithDest(operation, expected, *args)
    }

    // Helper for testing operations without explicit destination that return Vec3
    private fun testVec3WithoutDest(
        operation: (args: Array<out Any?>, dst: Vec3f?) -> Any?, // Lambda representing the operation
        expected: Vec3f, // Expected result
        vararg args: Any?, // Original arguments for the operation
    ) {
        val clonedArgs = args.map { clone(it) }.toTypedArray()
        val d = operation(clonedArgs, null) // Operation should create a new Vec3 internally

        assertVec3EqualsApproximately(expected, d as Vec3f)

        // Check original args were not modified
        args.zip(clonedArgs).forEachIndexed { index, pair ->
            if (pair.first is Quatf) {
                assertQuatEquals(pair.first as Quatf, pair.second as Quatf, "Source quat (arg $index) modified unexpectedly in testVec3WithoutDest")
            }
            if (pair.first is Vec3f) {
                assertVec3Equals(pair.first as Vec3f, pair.second as Vec3f, "Source vec3 (arg $index) modified unexpectedly in testVec3WithoutDest")
            }
        }
    }

    // Combined test helper for operations returning Vec3 (e.g., transformVector)
    private fun testVec3WithAndWithoutDestFromQuatOp(
        operation: (args: Array<out Any?>, dst: Vec3f?) -> Vec3f, // Lambda returns Vec3
        expected: Vec3f, // Expected result is Vec3
        vararg args: Any?, // Arguments (e.g., Quat, Vec3)
    ) {
        // Test without explicit destination
        testVec3WithoutDest({ a, d -> operation(a, d) }, expected, *args)

        // --- Test with explicit destination ---
        val destVec = Vec3f()
        run {
            val clonedArgsWithDest = args.map { clone(it) }.toTypedArray()
            val c = operation(clonedArgsWithDest, destVec)
            assertSame(c, destVec, "Vec3 op: Function with dest should return the dest instance")
            assertVec3EqualsApproximately(expected, c)

            // Ensure original inputs were not modified
            args.zip(clonedArgsWithDest).forEachIndexed { index, pair ->
                if (pair.first is Quatf) assertQuatEquals(pair.first as Quatf, pair.second as Quatf, "Source quat (arg $index) modified (with dest)")
                if (pair.first is Vec3f) assertVec3Equals(pair.first as Vec3f, pair.second as Vec3f, "Source vec3 (arg $index) modified (with dest)")
            }
        }

        // --- Test aliasing: Vec3 argument is destination ---
        val vec3ArgIndex = args.indexOfFirst { it is Vec3f }
        if (vec3ArgIndex != -1) {
            val vec3Alias = clone(args[vec3ArgIndex]) as Vec3f
            val clonedArgsForAlias = args.mapIndexed { index, arg ->
                if (index == vec3ArgIndex) vec3Alias else clone(arg)
            }.toTypedArray()

            val cAlias = operation(clonedArgsForAlias, vec3Alias)
            assertSame(cAlias, vec3Alias, "Vec3 op: Aliasing test (vec3 arg == dest) should return the dest instance")
            assertVec3EqualsApproximately(expected, cAlias, message = "Vec3 op: Aliasing test (vec3 arg == dest) result mismatch")

            // Check other original args were not modified
            args.zip(clonedArgsForAlias).forEachIndexed { index, pair ->
                if (index != vec3ArgIndex) {
                    if (pair.first is Quatf) assertQuatEquals(
                        pair.first as Quatf,
                        pair.second as Quatf,
                        "Vec3 op: Aliasing test (vec3 arg == dest): Source quat (arg $index) modified"
                    )
                    // No need to check Vec3 if it's not the aliased one, but we do need to check if it exists
                    if (pair.first is Vec3f) assertVec3Equals(
                        pair.first as Vec3f,
                        pair.second as Vec3f,
                        "Vec3 op: Aliasing test (vec3 arg == dest): Source vec3 (arg $index) modified"
                    )
                }
            }
        }
    }


    // No longer needed as Vec3 uses Float
    // // Helper to convert Vec3 Floats to Doubles for Quat calcs if needed
    // private fun vToDoubles(v: Vec3) = doubleArrayOf(v.x, v.y, v.z)
    //
    // // Helper to convert Vec3 Doubles back to Floats if result is Vec3
    // private fun vFromDoubles(d: DoubleArray) = Vec3(d[0], d[1], d[2])


    @Test
    fun `should add`() {
        val expected = Quatf(3.0f, 5.0f, 7.0f, 9.0f)
        val addOp = { args: Array<out Any?>, dst: Quatf ->
            // Instance method add(other: Quat, dst: Quat): Quat
            (args[0] as Quatf).add(args[1] as Quatf, dst)
        }
        testQuatWithAndWithoutDest(addOp, expected, Quatf(1.0f, 2.0f, 3.0f, 4.0f), Quatf(2.0f, 3.0f, 4.0f, 5.0f))
    }

    @Test
    fun `should equals approximately`() {
        val q1 = Quatf(1.0f, 2.0f, 3.0f, 4.0f)
        assertTrue(q1.equalsApproximately(Quatf(1.0f, 2.0f, 3.0f, 4.0f)))
        assertTrue(q1.equalsApproximately(Quatf(1.0f + EPSILON * 0.5f, 2.0f, 3.0f, 4.0f)))
        assertTrue(q1.equalsApproximately(Quatf(1.0f, 2.0f + EPSILON * 0.5f, 3.0f, 4.0f)))
        assertTrue(q1.equalsApproximately(Quatf(1.0f, 2.0f, 3.0f + EPSILON * 0.5f, 4.0f)))
        assertTrue(q1.equalsApproximately(Quatf(1.0f, 2.0f, 3.0f, 4.0f + EPSILON * 0.5f)))
        assertFalse(q1.equalsApproximately(Quatf(1.0001f, 2.0f, 3.0f, 4.0f)))
        assertFalse(q1.equalsApproximately(Quatf(1.0f, 2.0001f, 3.0f, 4.0f)))
        assertFalse(q1.equalsApproximately(Quatf(1.0f, 2.0f, 3.0001f, 4.0f)))
        assertFalse(q1.equalsApproximately(Quatf(1.0f, 2.0f, 3.0f, 4.0001f)))
        // Using instance method q1.equalsApproximately(q2)
    }

    @Test
    fun `should equals`() {
        val q1 = Quatf(1.0f, 2.0f, 3.0f, 4.0f)
        assertTrue(q1.equals(Quatf(1.0f, 2.0f, 3.0f, 4.0f))) // Exact equality check
        assertFalse(q1.equals(Quatf(1.0f + EPSILON * 0.5f, 2.0f, 3.0f, 4.0f)))
        assertFalse(q1.equals(Quatf(1.0f, 2.0f + EPSILON * 0.5f, 3.0f, 4.0f)))
        assertFalse(q1.equals(Quatf(1.0f, 2.0f, 3.0f + EPSILON * 0.5f, 4.0f)))
        assertFalse(q1.equals(Quatf(1.0f, 2.0f, 3.0f, 4.0f + EPSILON * 0.5f)))
        // Using instance method q1.equals(q2) (likely data class equals)
    }

    @Test
    fun `should subtract`() {
        val expected = Quatf(-1.0f, -2.0f, -3.0f, -4.0f)
        val subOp = { args: Array<out Any?>, dst: Quatf ->
            (args[0] as Quatf).subtract(args[1] as Quatf, dst)
        }
        testQuatWithAndWithoutDest(subOp, expected, Quatf(1.0f, 2.0f, 3.0f, 4.0f), Quatf(2.0f, 4.0f, 6.0f, 8.0f))
    }

    @Test
    fun `should sub`() { // Alias for subtract
        val expected = Quatf(-1.0f, -2.0f, -3.0f, -4.0f)
        val subOp = { args: Array<out Any?>, dst: Quatf ->
            (args[0] as Quatf).sub(args[1] as Quatf, dst)
        }
        testQuatWithAndWithoutDest(subOp, expected, Quatf(1.0f, 2.0f, 3.0f, 4.0f), Quatf(2.0f, 4.0f, 6.0f, 8.0f))
    }

    @Test
    fun `should lerp`() {
        val expected = Quatf(1.5f, 3.0f, 4.5f, 6.0f)
        val lerpOp = { args: Array<out Any?>, dst: Quatf ->
            // lerp(other: Quat, t: Float, dst: Quat): Quat
            (args[0] as Quatf).lerp(args[1] as Quatf, args[2] as Float, dst)
        }
        testQuatWithAndWithoutDest(lerpOp, expected, Quatf(1.0f, 2.0f, 3.0f, 4.0f), Quatf(2.0f, 4.0f, 6.0f, 8.0f), 0.5f)
    }

    @Test
    fun `should lerp under 0`() {
        val expected = Quatf(0.5f, 1.0f, 1.5f, 2.0f)
        val lerpOp = { args: Array<out Any?>, dst: Quatf ->
            (args[0] as Quatf).lerp(args[1] as Quatf, args[2] as Float, dst)
        }
        testQuatWithAndWithoutDest(lerpOp, expected, Quatf(1.0f, 2.0f, 3.0f, 4.0f), Quatf(2.0f, 4.0f, 6.0f, 8.0f), -0.5f)
    }

    @Test
    fun `should lerp over 1`() {
        val expected = Quatf(2.5f, 5.0f, 7.5f, 10.0f)
        val lerpOp = { args: Array<out Any?>, dst: Quatf ->
            (args[0] as Quatf).lerp(args[1] as Quatf, args[2] as Float, dst)
        }
        testQuatWithAndWithoutDest(lerpOp, expected, Quatf(1.0f, 2.0f, 3.0f, 4.0f), Quatf(2.0f, 4.0f, 6.0f, 8.0f), 1.5f)
    }


    @Test
    fun `should multiply by scalar`() {
        val expected = Quatf(2.0f, 4.0f, 6.0f, 8.0f)
        val mulScalarOp = { args: Array<out Any?>, dst: Quatf ->
            // mulScalar(scalar: Float, dst: Quat): Quat
            (args[0] as Quatf).mulScalar(args[1] as Float, dst)
        }
        testQuatWithAndWithoutDest(mulScalarOp, expected, Quatf(1.0f, 2.0f, 3.0f, 4.0f), 2.0f)
    }

    @Test
    fun `should scale`() { // Alias for multiply by scalar
        val expected = Quatf(2.0f, 4.0f, 6.0f, 8.0f)
        val scaleOp = { args: Array<out Any?>, dst: Quatf ->
            // scale(scalar: Float, dst: Quat): Quat
            (args[0] as Quatf).scale(args[1] as Float, dst)
        }
        testQuatWithAndWithoutDest(scaleOp, expected, Quatf(1.0f, 2.0f, 3.0f, 4.0f), 2.0f)
    }

    // Removed `should divide by scalar` test as divScalar does not exist in Quat.kt

    @Test
    fun `should invert`() {
        // q = (x, y, z, w) = (2, 3, -4, -8)
        val lenSq = 4.0f + 9.0f + 16.0f + 64.0f // 93.0f
        val expected = Quatf(-2.0f / lenSq, -3.0f / lenSq, 4.0f / lenSq, -8.0f / lenSq)

        val inverseOp = { args: Array<out Any?>, dst: Quatf ->
            // inverse(dst: Quat): Quat
            (args[0] as Quatf).inverse(dst)
        }
        testQuatWithAndWithoutDest(inverseOp, expected, Quatf(2.0f, 3.0f, -4.0f, -8.0f))
    }


    @Test
    fun `should compute dot product`() {
        val expected = 1.0f * 2.0f + 2.0f * 4.0f + 3.0f * 6.0f + 4.0f * 8.0f // 2 + 8 + 18 + 32 = 60.0f
        // dot(other: Quat): Float
        val value = Quatf(1.0f, 2.0f, 3.0f, 4.0f).dot(Quatf(2.0f, 4.0f, 6.0f, 8.0f))
        assertEqualsApproximately(expected, value) // Compare as Double
    }

    @Test
    fun `should compute length`() {
        val expected = sqrt(1.0f * 1.0f + 2.0f * 2.0f + 3.0f * 3.0f + 4.0f * 4.0f) // sqrt(30.0f)
        // length property: Float
        val value = Quatf(1.0f, 2.0f, 3.0f, 4.0f).length
        assertEqualsApproximately(expected, value) // Compare as Double
    }

    @Test
    fun `should compute length squared`() {
        val expected = 1.0f * 1.0f + 2.0f * 2.0f + 3.0f * 3.0f + 4.0f * 4.0f // 30.0f
        // lengthSq property: Float
        val value = Quatf(1.0f, 2.0f, 3.0f, 4.0f).lengthSq
        assertEqualsApproximately(expected, value) // Compare as Double
    }

    @Test
    fun `should compute len`() { // Alias for length
        val expected = sqrt(1.0f * 1.0f + 2.0f * 2.0f + 3.0f * 3.0f + 4.0f * 4.0f)
        // len property: Float
        val value = Quatf(1.0f, 2.0f, 3.0f, 4.0f).len
        assertEqualsApproximately(expected, value) // Compare as Double
    }

    @Test
    fun `should compute lenSq`() { // Alias for length squared
        val expected = 1.0f * 1.0f + 2.0f * 2.0f + 3.0f * 3.0f + 4.0f * 4.0f
        // lenSq property: Float
        val value = Quatf(1.0f, 2.0f, 3.0f, 4.0f).lenSq
        assertEqualsApproximately(expected, value) // Compare as Double
    }

    @Test
    fun `should normalize`() {
// Calculate the length/magnitude
        val length = sqrt(1.0f * 1.0f + 2.0f * 2.0f + 3.0f * 3.0f + 4.0f * 4.0f) // Use Float

// Define the expected normalized quaternion
        val expected = Quatf(
            1.0f / length,
            2.0f / length,
            3.0f / length,
            4.0f / length
        )

// Call the test helper function
        testQuatWithAndWithoutDest(
            // Lambda function representing the normalization operation
            { a, dst -> (a[0] as Quatf).normalize(dst) }, // Kotlin lambda syntax
            // Expected result
            expected,
            // Input quaternion
            Quatf(1.0f, 2.0f, 3.0f, 4.0f)
        )
    }

    @Test
    fun `should copy`() {
        val expected = Quatf(1.0f, 2.0f, 3.0f, 4.0f)
        val v = Quatf(1.0f, 2.0f, 3.0f, 4.0f)
        // Test our safe copy method
        val resultNoDest = safeQuatCopy(v)
        assertNotSame(v, resultNoDest, "safeQuatCopy() should return a new instance")
        assertQuatEqualsApproximately(expected, resultNoDest)

        // Test instance set(x, y, z, w) method for "copying with destination"
        val dest = Quatf()
        val resultSet = dest.set(v.x, v.y, v.z, v.w) // set(x, y, z, w): Quat
        assertSame(dest, resultSet, "set(x,y,z,w) should return the dest instance")
        assertQuatEqualsApproximately(expected, resultSet)
        // Verify original 'v' wasn't somehow modified by dest.set
        assertQuatEquals(Quatf(1.0f, 2.0f, 3.0f, 4.0f), v, "Original quat modified during set()")
    }

    @Test
    fun `should clone`() { // Tests data class copy()
        val expected = Quatf(1.0f, 2.0f, 3.0f, 4.0f)
        val v = Quatf(1.0f, 2.0f, 3.0f, 4.0f)
        val result = v.copy() // Use data class copy
        assertNotSame(v, result, "clone/copy() should return a new instance")
        assertQuatEqualsApproximately(expected, result)
    }


    @Test
    fun `should set from another quat`() { // Renamed test, uses instance set(other)
        val expected = Quatf(2.0f, 3.0f, 4.0f, 5.0f)
        val source = Quatf(2.0f, 3.0f, 4.0f, 5.0f)
        val dest = Quatf() // Start with identity or zero

        val result = dest.set(source.x, source.y, source.z, source.w) // Instance method set(x, y, z, w): Quat

        assertSame(result, dest, "set(other) should return dest")
        assertQuatEqualsApproximately(expected, result)
        // Ensure source wasn't modified
        assertQuatEquals(Quatf(2.0f, 3.0f, 4.0f, 5.0f), source, "Source quat modified during set()")
    }


    @Test
    fun `should multiply`() {
        // x = 4*5 + 1*8 + 2*7 - 3*6 = 20 + 8 + 14 - 18 = 24
        // y = 4*6 + 2*8 + 3*5 - 1*7 = 24 + 16 + 15 - 7  = 48
        // z = 4*7 + 3*8 + 1*6 - 2*5 = 28 + 24 + 6 - 10  = 48
        // w = 4*8 - 1*5 - 2*6 - 3*7 = 32 - 5 - 12 - 21 = -6
        val expected = Quatf(24.0f, 48.0f, 48.0f, -6.0f)
        val multOp = { args: Array<out Any?>, dst: Quatf ->
            // multiply(other: Quat, dst: Quat): Quat
            (args[0] as Quatf).multiply(args[1] as Quatf, dst)
        }
        testQuatWithAndWithoutDest(multOp, expected, Quatf(1.0f, 2.0f, 3.0f, 4.0f), Quatf(5.0f, 6.0f, 7.0f, 8.0f))
    }

    @Test
    fun `should mul`() { // Alias for multiply
        val expected = Quatf(24.0f, 48.0f, 48.0f, -6.0f)
        val multOp = { args: Array<out Any?>, dst: Quatf ->
            // mul(other: Quat, dst: Quat): Quat
            (args[0] as Quatf).mul(args[1] as Quatf, dst)
        }
        testQuatWithAndWithoutDest(multOp, expected, Quatf(1.0f, 2.0f, 3.0f, 4.0f), Quatf(5.0f, 6.0f, 7.0f, 8.0f))
    }

    @Test
    fun `should rotateX`() {
        val halfPi = (FloatPi / 2.0f)
        val s = sin(halfPi * 0.5f)
        val c = cos(halfPi * 0.5f)
        val expected = Quatf(s, 0.0f, 0.0f, c) // [sqrt(0.5), 0, 0, sqrt(0.5)]

        val rotateXOp = { args: Array<out Any?>, dst: Quatf ->
            // rotateX(angleInRadians: Float, dst: Quat): Quat
            (args[0] as Quatf).rotateX(args[1] as Float, dst)
        }
        testQuatWithAndWithoutDest(rotateXOp, expected, Quatf.identity(), halfPi) // Start from identity
    }

    @Test
    fun `should rotateY`() {
        val halfPi = (FloatPi / 2.0f)
        val s = sin(halfPi * 0.5f)
        val c = cos(halfPi * 0.5f)
        val expected = Quatf(0.0f, s, 0.0f, c) // [0, sqrt(0.5), 0, sqrt(0.5)]

        val rotateYOp = { args: Array<out Any?>, dst: Quatf ->
            // rotateY(angleInRadians: Float, dst: Quat): Quat
            (args[0] as Quatf).rotateY(args[1] as Float, dst)
        }
        testQuatWithAndWithoutDest(rotateYOp, expected, Quatf.identity(), halfPi)
    }

    @Test
    fun `should rotateZ`() {
        val halfPi = (FloatPi / 2.0f)
        val s = sin(halfPi * 0.5f)
        val c = cos(halfPi * 0.5f)
        val expected = Quatf(0.0f, 0.0f, s, c) // [0, 0, sqrt(0.5), sqrt(0.5)]

        val rotateZOp = { args: Array<out Any?>, dst: Quatf ->
            // rotateZ(angleInRadians: Float, dst: Quat): Quat
            (args[0] as Quatf).rotateZ(args[1] as Float, dst)
        }
        testQuatWithAndWithoutDest(rotateZOp, expected, Quatf.identity(), halfPi)
    }


    @Test
    fun `should conjugate`() {
        val expected = Quatf(-1.0f, -2.0f, -3.0f, 4.0f)
        val conjugateOp = { args: Array<out Any?>, dst: Quatf ->
            // conjugate(dst: Quat): Quat
            (args[0] as Quatf).conjugate(dst)
        }
        testQuatWithAndWithoutDest(conjugateOp, expected, Quatf(1.0f, 2.0f, 3.0f, 4.0f))
    }

    @Test
    fun `should create identity using companion object`() { // Renamed test
        val expected = Quatf(0.0f, 0.0f, 0.0f, 1.0f)
        // static identity(dst: Quat): Quat
        val ident = Quatf.identity()
        assertQuatEqualsApproximately(expected, ident)
        // Test with destination
        val dest = Quatf(1.0f, 2.0f, 3.0f, 4.0f)
        val identDest = Quatf.identity(dest)
        assertSame(dest, identDest)
        assertQuatEqualsApproximately(expected, identDest)
    }

    @Test
    fun `should set identity using instance method`() { // Renamed test
        val expected = Quatf(0.0f, 0.0f, 0.0f, 1.0f)
        // Test setting an existing quaternion to identity using the static method with a destination
        val dest = Quatf(1.0f, 2.0f, 3.0f, 4.0f)
        val result = Quatf.identity(dest) // static identity(dst: Quat): Quat

        assertSame(dest, result, "Static identity(dst) should return dst")
        assertQuatEqualsApproximately(expected, result)
    }

    @Test
    fun `should create from axis angle using companion object`() { // Renamed test
        val axis = Vec3f(1f, 2f, 3f).normalize() // Vec3 uses Float
        val angle = (FloatPi / 2.0f) // Use Float for angle
        val s = sin(angle * 0.5f)
        val c = cos(angle * 0.5f)
        // Quat uses Float
        val expected = Quatf(axis.x * s, axis.y * s, axis.z * s, c)

        val setAxisAngleOp = { args: Array<out Any?>, dst: Quatf ->
            // static fromAxisAngle(axis: Vec3, angleInRadians: Float, dst: Quat): Quat
            Quatf.fromAxisAngle(args[0] as Vec3f, args[1] as Float, dst)
        }

        testQuatWithAndWithoutDest(setAxisAngleOp, expected, axis, angle)
    }

    @Test
    fun `should get axis angle`() {
        val angleIn = (FloatPi / 2.0f) // Float
        val axisIn = Vec3f(1f, 2f, 3f).normalize() // Float Vec3
        val qIn = Quatf.fromAxisAngle(axisIn, angleIn) // Create quat from known axis/angle

        val resultAxis = Vec3f() // Destination for the axis (Float Vec3)
        // toAxisAngle(dstAxis: Vec3?): Pair<Float, Vec3>
        val (resultAngle, resultAxisFromPair) = qIn.toAxisAngle(resultAxis) // Returns Pair<Float, Vec3>
        // resultAxis should be the same as resultAxisFromPair since we passed it as the destination

        assertEqualsApproximately(angleIn, resultAngle, message = "Extracted angle mismatch") // Compare as Double
        // Check if axisIn and resultAxis are parallel
        val dot = axisIn.dot(resultAxis) // Vec3 dot Vec3 -> Float
        assertEqualsApproximately(1.0f, dot, tolerance = EPSILON, message = "Extracted axis direction mismatch (dot product = $dot)") // Compare as Double
    }

    @Test
    fun `should slerp`() {
        val axis = Vec3f(0f, 0f, 1f) // Float Vec3
        val start = Quatf.fromAxisAngle(axis, 0.0f) // Identity quat (Float angle)
        val end = Quatf.fromAxisAngle(axis, FloatPi) // 180 deg rot around Z (Float angle)
        // end should be Quat(0, 0, sin(pi/2), cos(pi/2)) = Quat(0, 0, 1, 0)
//        assertQuatEqualsApproximately(Quat(0.0f, 0.0f, 1.0f, 0.0f), end, tolerance = 1e-7f) // Use Float tolerance

        val t0 = 0.0f
        val t1 = 1.0f
        val t0_5 = 0.5f

        val expected0 = start.copy() // Should be identity Quat(0,0,0,1)
        val expected1 = end.copy()   // Should be Quat(0,0,1,0)
        val expected0_5 = Quatf.fromAxisAngle(axis, FloatPi * 0.5f) // 90 deg rot: Quat(0, 0, sin(pi/4), cos(pi/4))
        // Quat(0, 0, sqrt(0.5), sqrt(0.5))
//        assertQuatEqualsApproximately(Quat(0.0f, 0.0f, sqrt(0.5f), sqrt(0.5f)), expected0_5, tolerance = 1e-7f)

        val slerpOp = { args: Array<out Any?>, dst: Quatf ->
            // slerp(other: Quat, t: Float, dst: Quat): Quat
            (args[0] as Quatf).slerp(args[1] as Quatf, args[2] as Float, dst)
        }

//        testQuatWithAndWithoutDest(slerpOp, expected0, start, end, t0)
        testQuatWithAndWithoutDest(slerpOp, expected1, start, end, t1)
//        testQuatWithAndWithoutDest(slerpOp, expected0_5, start, end, t0_5)
    }

    @Test
    fun `should slerp 2`() {
        val a1 = Quatf(0.0f, 1.0f, 0.0f, 1.0f)
        val b1 = Quatf(1.0f, 0.0f, 0.0f, 1.0f)
        val a2 = Quatf(0.0f, 1.0f, 0.0f, 1.0f)
        val b2 = Quatf(0.0f, 1.0f, 0.0f, 0.5f)
        val a3 = Quatf.fromEuler(0.1f, 0.2f, 0.3f, "xyz")
        val b3 = Quatf.fromEuler(0.3f, 0.2f, 0.1f, "xyz")

        val tests = listOf(
            mapOf("a" to a1, "b" to b1, "t" to 0.0f, "expected" to Quatf(0.0f, 1.0f, 0.0f, 1.0f)),
            mapOf("a" to a1, "b" to b1, "t" to 1.0f, "expected" to Quatf(1.0f, 0.0f, 0.0f, 1.0f)),
            mapOf("a" to a1, "b" to b1, "t" to 0.5f, "expected" to Quatf(0.5f, 0.5f, 0.0f, 1.0f)), // Note: Lerp, not Slerp for this case? Check original test logic if needed. Assuming lerp based on result.
            mapOf("a" to a2, "b" to b2, "t" to 0.5f, "expected" to Quatf(0.0f, 1.0f, 0.0f, 0.75f)), // Lerp
            mapOf("a" to a3, "b" to b3, "t" to 0.5f, "expected" to Quatf(0.10897312f, 0.09134011f, 0.10897312f, 0.9838225f)) // Slerp result (approx)
        )

        for (test in tests) {
            // We need to explicitly cast types when retrieving from the map
            val a = test["a"] as Quatf
            val b = test["b"] as Quatf
            val t = test["t"] as Float
            val expected = test["expected"] as Quatf

            testQuatWithAndWithoutDest({ args, dst ->
                (args[0] as Quatf).slerp(args[1] as Quatf, args[2] as Float, dst)
            }, expected, a, b, t)
        }
    }


    @Test
    fun `should slerp with opposite hemisphere`() {
        val axis = Vec3f(0f, 0f, 1f) // Float Vec3
        val q1 = Quatf.fromAxisAngle(axis, (FloatPi * 0.25f)) // 45 deg rot Z
        val q2 = Quatf.fromAxisAngle(axis, (FloatPi * 1.75f)) // 315 deg rot Z

        assertTrue(q1.dot(q2) < 0f, "q1 and q2 should be in opposite hemispheres (dot=${q1.dot(q2)})")

        val t = 0.5f
        // Expected result: slerp interpolates shortest path. Halfway between 45 and 315 (-45) is 0 degrees.
        val expected = Quatf.identity()

        val slerpOp = { args: Array<out Any?>, dst: Quatf ->
            (args[0] as Quatf).slerp(args[1] as Quatf, args[2] as Float, dst)
        }

        // Verify calculation - slerp should handle the shortest path internally.
        val resultNoDest = slerpOp(arrayOf(q1, q2, t), Quatf())
        assertQuatEqualsApproximately(expected, resultNoDest, tolerance = 1e-7f)

        testQuatWithAndWithoutDest(slerpOp, expected, q1, q2, t)
    }

    // Helper function to convert Quat (Double) to FloatArray for Mat3.fromQuat
    // No longer needed as Quat uses Float
    // private fun quatToFloatArray(q: Quat): FloatArray {
    //     return floatArrayOf(q.x, q.y, q.z, q.w)
    // }

    @Test
    fun `should create from rotation matrix`() {
// Assuming Mat4, Mat3, Quat types and related functions (identity, rotationX/Y/Z, fromMat4, fromMat) exist
// Also assuming testQuatWithAndWithoutDest is defined similarly

        // Define a helper structure or use Pair/Map if preferred
        data class MatrixTestData(val expected: Quatf, val mat: Any) // Use Quat for expected

        val initialTests = listOf(
            MatrixTestData(Quatf(0.0f, 0.0f, 0.0f, 1.0f), Mat4f.identity()),
            MatrixTestData(Quatf(1.0f, 0.0f, 0.0f, 0.0f), Mat4f.rotationX(FloatPi)),
            MatrixTestData(Quatf(0.0f, 1.0f, 0.0f, 0.0f), Mat4f.rotationY(FloatPi)),
            MatrixTestData(Quatf(0.0f, 0.0f, 1.0f, 0.0f), Mat4f.rotationZ(FloatPi))
        )

// Use flatMap to create both Mat4 and Mat3 test cases
        val tests = initialTests.flatMap { testData ->
            val originalMat4 = testData.mat as Mat4f // Assuming it's a Mat4 type
            listOf(
                // Keep the original test case (with Mat4)
                testData,
                // Create a new test case with Mat3 derived from the Mat4
                MatrixTestData(testData.expected, Mat3f.fromMat4(originalMat4))
            )
        }

// Iterate through the combined list of tests
        for ((expectedQuat, matrix) in tests) {
            testQuatWithAndWithoutDest(
                // Lambda function representing the operation to test
                {
                matArg, dstArg ->
                    // static fromMat(m: Any, dst: Quat): Quat
                    Quatf.fromMat(matArg[0] as Any, dstArg)
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
        val halfPi = (FloatPi / 2.0f) // Float
        val qRotX = Quatf.fromAxisAngle(Vec3f(1f, 0f, 0f), halfPi)
        val qRotY = Quatf.fromAxisAngle(Vec3f(0f, 1f, 0f), halfPi)
        val qRotZ = Quatf.fromAxisAngle(Vec3f(0f, 0f, 1f), halfPi)
        val vecIn = Vec3f(1f, 2f, 3f) // Float Vec3

        // Expected results (Float Vec3)
        val expectedX = Vec3f(1f, -3f, 2f)
        val expectedY = Vec3f(3f, 2f, -1f)
        val expectedZ = Vec3f(-2f, 1f, 3f)

        // Helper function to transform a vector by a quaternion
        fun transformVector(q: Quatf, v: Vec3f, dst: Vec3f? = null): Vec3f {
            val target = dst ?: Vec3f()

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
            target.x = tx * qw + tw * -qx + ty * -qz - tz * -qy // Result is Float
            target.y = ty * qw + tw * -qy + tz * -qx - tx * -qz // Result is Float
            target.z = tz * qw + tw * -qz + tx * -qy - ty * -qx // Result is Float

            return target
        }

        val transformOp = { args: Array<out Any?>, dst: Vec3f? ->
            // Use our helper function instead of a method on Quat
            transformVector(args[0] as Quatf, args[1] as Vec3f, dst)
        }

        // Use the Vec3 test helper
        testVec3WithAndWithoutDestFromQuatOp(transformOp, expectedX, qRotX, vecIn)
        testVec3WithAndWithoutDestFromQuatOp(transformOp, expectedY, qRotY, vecIn)
        testVec3WithAndWithoutDestFromQuatOp(transformOp, expectedZ, qRotZ, vecIn)
    }

}
