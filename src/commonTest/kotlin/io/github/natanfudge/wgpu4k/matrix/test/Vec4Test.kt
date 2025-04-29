package io.github.natanfudge.wgpu4k.matrix.test

import io.github.natanfudge.wgpu4k.matrix.Mat4
import io.github.natanfudge.wgpu4k.matrix.Vec4
import kotlin.math.*
import kotlin.test.*

// --- Test Suite ---

class Vec4Tests {

    // Helper to mimic the JS 'clone' for testing
    private fun clone(v: Any?): Any? {
        return when (v) {
            is Vec4 -> v.copy() // Use the data class copy for Vec4
            is DoubleArray -> v.copyOf()
            // Add other types like Mat4 if needed and available
            else -> v // Assume immutable primitives or objects handled by value/reference correctly
        }
    }

    // Helper testing function result without explicit destination
    private fun testV4WithoutDest(
        operation: (args: Array<out Any?>, dst: Vec4?) -> Any?, // Lambda representing the Vec4 operation
        expected: Any?,
        vararg args: Any? // Original arguments for the operation
    ) {
        val clonedArgs = args.map { clone(it) }.toTypedArray()
        val d = operation(clonedArgs, null) // Operation should create a new Vec4 internally

        when (expected) {
            is Vec4 -> assertVec4EqualsApproximately(expected, d as Vec4)
            is DoubleArray -> {
                if (d is Vec4) {
                    assertVec4EqualsApproximately(expected, d)
                } else {
                    fail("Expected Vec4 result when expected is DoubleArray, but got ${d?.let { it::class.simpleName }}")
                }
            }
            is Double -> assertEqualsApproximately(expected, d as Double)
            is Boolean -> assertEquals(expected, d as Boolean)
            else -> assertEquals(expected, d) // Fallback direct comparison
        }

        // Check the original arguments were not modified
        args.zip(clonedArgs).forEachIndexed { index, pair ->
             if (pair.first is Vec4) {
                 assertVec4Equals(pair.first as Vec4, pair.second as Vec4, "Source vector (arg $index) modified unexpectedly in testV4WithoutDest")
             }
             // Add checks for other mutable types if needed (e.g., Mat4)
        }
    }

    // Helper testing function result with explicit destination
    private fun testV4WithDest(
        operation: (args: Array<out Any?>, dst: Vec4?) -> Any?, // Lambda representing the Vec4 operation
        expected: Any?,
        vararg args: Any? // Original arguments for the operation
    ) {
        val expectedCloned = clone(expected) // Clone expected value for comparison
        val destVector = Vec4() // Create the destination vector

        // --- Test with standard destination ---
        run {
            val clonedArgs = args.map { clone(it) }.toTypedArray() // Clone inputs for this run
            val c = operation(clonedArgs, destVector)

            assertSame(c, destVector, "Function with dest should return the dest instance")

            when (expectedCloned) {
                is Vec4 -> assertVec4EqualsApproximately(expectedCloned, c as Vec4)
                is DoubleArray -> assertVec4EqualsApproximately(expectedCloned, c as Vec4)
                else -> fail("testV4WithDest expects Vec4 or DoubleArray for 'expected' value")
            }

            // Ensure original inputs were not modified
            args.zip(clonedArgs).forEachIndexed { index, pair ->
                if (pair.first is Vec4) {
                    assertVec4Equals(pair.first as Vec4, pair.second as Vec4, "Source vector (arg $index) modified unexpectedly in testV4WithDest (standard dest)")
                }
                 // Add checks for other mutable types if needed
            }
        }

        // --- Test aliasing: first argument is destination ---
        if (args.isNotEmpty() && args[0] is Vec4) {
            val firstArgAlias = clone(args[0]) as Vec4
            val clonedRemainingArgs = args.drop(1).map { clone(it) }.toTypedArray()
            val allArgsForAlias1 = arrayOf(firstArgAlias, *clonedRemainingArgs)

            val cAlias1 = operation(allArgsForAlias1, firstArgAlias)

            assertSame(cAlias1, firstArgAlias, "Aliasing test (firstArg == dest) should return the dest instance")
            when (expectedCloned) {
                is Vec4 -> assertVec4EqualsApproximately(expectedCloned, cAlias1 as Vec4, message="Aliasing test (firstArg == dest) result mismatch")
                is DoubleArray -> assertVec4EqualsApproximately(expectedCloned, cAlias1 as Vec4, message="Aliasing test (firstArg == dest) result mismatch")
            }
            // Check other original args were not modified
            args.drop(1).zip(clonedRemainingArgs).forEachIndexed { index, pair ->
                if (pair.first is Vec4) {
                    assertVec4Equals(pair.first as Vec4, pair.second as Vec4, "Aliasing test (firstArg == dest): Source vector (arg ${index + 1}) modified unexpectedly")
                }
                 // Add checks for other mutable types if needed
            }
        }


        // --- Test aliasing: another Vec4 argument is destination ---
        val firstOperandIndex = args.indexOfFirst { it is Vec4 && it !== args[0] }
        if (firstOperandIndex != -1) {
            val operandAlias = clone(args[firstOperandIndex]) as Vec4
            val clonedArgsForAlias2 = args.mapIndexed { index, arg ->
                if (index == firstOperandIndex) operandAlias else clone(arg)
            }.toTypedArray()

            val cAlias2 = operation(clonedArgsForAlias2, operandAlias)

            assertSame(cAlias2, operandAlias, "Aliasing test (operand == dest) should return the dest instance")
            when (expectedCloned) {
                is Vec4 -> assertVec4EqualsApproximately(expectedCloned, cAlias2 as Vec4, message="Aliasing test (operand == dest) result mismatch")
                is DoubleArray -> assertVec4EqualsApproximately(expectedCloned, cAlias2 as Vec4, message="Aliasing test (operand == dest) result mismatch")
            }
            // Check original args (that were not the alias dest) were not modified
            args.zip(clonedArgsForAlias2).forEachIndexed { index, pair ->
                 if (index != firstOperandIndex) {
                     if (pair.first is Vec4) {
                         assertVec4Equals(pair.first as Vec4, pair.second as Vec4, "Aliasing test (operand == dest): Source vector (arg $index) modified unexpectedly")
                     }
                     // Add checks for other mutable types if needed
                 }
            }
        }
    }

    // Combined test helper
    private fun testV4WithAndWithoutDest(
        operation: (args: Array<out Any?>, dst: Vec4?) -> Any?,
        expected: Any?,
        vararg args: Any?
    ) {
        testV4WithoutDest(operation, expected, *args)

        if (expected is Vec4 || expected is DoubleArray) {
             testV4WithDest(operation, expected, *args)
        }
    }


    // --- Actual Tests ---

    @Test
    fun `should add`() {
        val expected = Vec4(3.0, 5.0, 7.0, 9.0)
        val addOperation = { args: Array<out Any?>, dst: Vec4? ->
            val v1 = args[0] as Vec4
            val v2 = args[1] as Vec4
            v1.add(v2, dst ?: Vec4())
        }
        testV4WithAndWithoutDest(addOperation, expected, Vec4(1.0, 2.0, 3.0, 4.0), Vec4(2.0, 3.0, 4.0, 5.0))
    }

    @Test
    fun `should compute ceil`() {
        val expected = Vec4(2.0, -1.0, 3.0, -4.0)
        val ceilOperation = { args: Array<out Any?>, dst: Vec4? ->
            val v1 = args[0] as Vec4
            v1.ceil(dst ?: Vec4())
        }
        testV4WithAndWithoutDest(ceilOperation, expected, Vec4(1.1, -1.1, 2.9, -4.2))
    }

    @Test
    fun `should compute floor`() {
        val expected = Vec4(1.0, -2.0, 2.0, -4.0)
        val floorOperation = { args: Array<out Any?>, dst: Vec4? ->
            val v1 = args[0] as Vec4
            v1.floor(dst ?: Vec4())
        }
        testV4WithAndWithoutDest(floorOperation, expected, Vec4(1.1, -1.1, 2.9, -3.1))
    }

    @Test
    fun `should compute round`() {
        val expected = Vec4(1.0, -1.0, 3.0, 0.0)
        val roundOperation = { args: Array<out Any?>, dst: Vec4? ->
            val v1 = args[0] as Vec4
            v1.round(dst ?: Vec4())
        }
        testV4WithAndWithoutDest(roundOperation, expected, Vec4(1.1, -1.1, 2.9, 0.1))
    }

    @Test
    fun `should clamp`() {
        run {
            val expected = Vec4(1.0, 0.0, 0.5, 0.0)
            val clampOperation = { args: Array<out Any?>, dst: Vec4? ->
                val v1 = args[0] as Vec4
                val min = args[1] as Double
                val max = args[2] as Double
                v1.clamp(min, max, dst ?: Vec4())
            }
            testV4WithAndWithoutDest(clampOperation, expected, Vec4(2.0, -1.0, 0.5, -4.0), 0.0, 1.0)
        }
        run {
            val expected = Vec4(-10.0, 5.0, 2.9, -9.0)
            val clampOperation = { args: Array<out Any?>, dst: Vec4? ->
                val v1 = args[0] as Vec4
                val min = args[1] as Double
                val max = args[2] as Double
                v1.clamp(min, max, dst ?: Vec4())
            }
            testV4WithAndWithoutDest(clampOperation, expected, Vec4(-22.0, 50.0, 2.9, -9.0), -10.0, 5.0)
        }
    }

    @Test
    fun `should equals approximately`() {
        assertTrue(Vec4(1.0, 2.0, 3.0, 4.0).equalsApproximately(Vec4(1.0, 2.0, 3.0, 4.0)))
        assertTrue(Vec4(1.0, 2.0, 3.0, 4.0).equalsApproximately(Vec4(1.0 + Vec4.EPSILON * 0.5, 2.0, 3.0, 4.0)))
        assertTrue(Vec4(1.0, 2.0, 3.0, 4.0).equalsApproximately(Vec4(1.0, 2.0 + Vec4.EPSILON * 0.5, 3.0, 4.0)))
        assertTrue(Vec4(1.0, 2.0, 3.0, 4.0).equalsApproximately(Vec4(1.0, 2.0, 3.0 + Vec4.EPSILON * 0.5, 4.0)))
        assertTrue(Vec4(1.0, 2.0, 3.0, 4.0).equalsApproximately(Vec4(1.0, 2.0, 3.0, 4.0 + Vec4.EPSILON * 0.5)))
        assertFalse(Vec4(1.0, 2.0, 3.0, 4.0).equalsApproximately(Vec4(1.0001, 2.0, 3.0, 4.0)))
        assertFalse(Vec4(1.0, 2.0, 3.0, 4.0).equalsApproximately(Vec4(1.0, 2.0001, 3.0, 4.0)))
        assertFalse(Vec4(1.0, 2.0, 3.0, 4.0).equalsApproximately(Vec4(1.0, 2.0, 3.0001, 4.0)))
        assertFalse(Vec4(1.0, 2.0, 3.0, 4.0).equalsApproximately(Vec4(1.0, 2.0, 3.0, 4.0001)))
    }

    @Test
    fun `should equals`() {
        assertTrue(Vec4(1.0, 2.0, 3.0, 4.0).equals(Vec4(1.0, 2.0, 3.0, 4.0)))
        assertFalse(Vec4(1.0, 2.0, 3.0, 4.0).equals(Vec4(1.0 + Vec4.EPSILON * 0.5, 2.0, 3.0, 4.0)))
        assertFalse(Vec4(1.0, 2.0, 3.0, 4.0).equals(Vec4(1.0, 2.0 + Vec4.EPSILON * 0.5, 3.0, 4.0)))
        assertFalse(Vec4(1.0, 2.0, 3.0, 4.0).equals(Vec4(1.0, 2.0, 3.0 + Vec4.EPSILON * 0.5, 4.0)))
        assertFalse(Vec4(1.0, 2.0, 3.0, 4.0).equals(Vec4(1.0, 2.0, 3.0, 4.0 + Vec4.EPSILON * 0.5)))
    }

    @Test
    fun `should subtract`() {
        val expected = Vec4(-1.0, -2.0, -3.0, -4.0)
        val subtractOperation = { args: Array<out Any?>, dst: Vec4? ->
            val v1 = args[0] as Vec4
            val v2 = args[1] as Vec4
            v1.subtract(v2, dst ?: Vec4())
        }
        testV4WithAndWithoutDest(subtractOperation, expected, Vec4(1.0, 2.0, 3.0, 4.0), Vec4(2.0, 4.0, 6.0, 8.0))
    }

    @Test
    fun `should sub`() {
        val expected = Vec4(-1.0, -2.0, -3.0, -4.0)
        val subOperation = { args: Array<out Any?>, dst: Vec4? ->
            val v1 = args[0] as Vec4
            val v2 = args[1] as Vec4
            v1.sub(v2, dst ?: Vec4())
        }
        testV4WithAndWithoutDest(subOperation, expected, Vec4(1.0, 2.0, 3.0, 4.0), Vec4(2.0, 4.0, 6.0, 8.0))
    }

    @Test
    fun `should lerp`() {
        val expected = Vec4(1.5, 3.0, 4.5, 6.0)
        val lerpOperation = { args: Array<out Any?>, dst: Vec4? ->
            val v1 = args[0] as Vec4
            val v2 = args[1] as Vec4
            val t = args[2] as Double
            v1.lerp(v2, t, dst ?: Vec4())
        }
        testV4WithAndWithoutDest(lerpOperation, expected, Vec4(1.0, 2.0, 3.0, 4.0), Vec4(2.0, 4.0, 6.0, 8.0), 0.5)
    }

    @Test
    fun `should lerp under 0`() {
        val expected = Vec4(0.5, 1.0, 1.5, 2.0)
        val lerpOperation = { args: Array<out Any?>, dst: Vec4? ->
            val v1 = args[0] as Vec4
            val v2 = args[1] as Vec4
            val t = args[2] as Double
            v1.lerp(v2, t, dst ?: Vec4())
        }
        testV4WithAndWithoutDest(lerpOperation, expected, Vec4(1.0, 2.0, 3.0, 4.0), Vec4(2.0, 4.0, 6.0, 8.0), -0.5)
    }

    @Test
    fun `should lerp over 1`() { // Renamed from 'lerp over 0'
        val expected = Vec4(2.5, 5.0, 7.5, 10.0)
        val lerpOperation = { args: Array<out Any?>, dst: Vec4? ->
            val v1 = args[0] as Vec4
            val v2 = args[1] as Vec4
            val t = args[2] as Double
            v1.lerp(v2, t, dst ?: Vec4())
        }
        testV4WithAndWithoutDest(lerpOperation, expected, Vec4(1.0, 2.0, 3.0, 4.0), Vec4(2.0, 4.0, 6.0, 8.0), 1.5)
    }

    @Test
    fun `should multiply by scalar`() {
        val expected = Vec4(2.0, 4.0, 6.0, 8.0)
        val mulScalarOperation = { args: Array<out Any?>, dst: Vec4? ->
            val v1 = args[0] as Vec4
            val k = args[1] as Double
            v1.mulScalar(k, dst ?: Vec4())
        }
        testV4WithAndWithoutDest(mulScalarOperation, expected, Vec4(1.0, 2.0, 3.0, 4.0), 2.0)
    }

    @Test
    fun `should scale`() {
        val expected = Vec4(2.0, 4.0, 6.0, 8.0)
        val scaleOperation = { args: Array<out Any?>, dst: Vec4? ->
            val v1 = args[0] as Vec4
            val k = args[1] as Double
            v1.scale(k, dst ?: Vec4())
        }
        testV4WithAndWithoutDest(scaleOperation, expected, Vec4(1.0, 2.0, 3.0, 4.0), 2.0)
    }

    @Test
    fun `should add scaled`() {
        val expected = Vec4(5.0, 10.0, 15.0, 20.0)
        val addScaledOperation = { args: Array<out Any?>, dst: Vec4? ->
            val v1 = args[0] as Vec4
            val v2 = args[1] as Vec4
            val scale = args[2] as Double
            v1.addScaled(v2, scale, dst ?: Vec4())
        }
        testV4WithAndWithoutDest(addScaledOperation, expected, Vec4(1.0, 2.0, 3.0, 4.0), Vec4(2.0, 4.0, 6.0, 8.0), 2.0)
    }

    @Test
    fun `should divide by scalar`() {
        val expected = Vec4(0.5, 1.0, 1.5, 2.0)
        val divScalarOperation = { args: Array<out Any?>, dst: Vec4? ->
            val v1 = args[0] as Vec4
            val k = args[1] as Double
            v1.divScalar(k, dst ?: Vec4())
        }
        testV4WithAndWithoutDest(divScalarOperation, expected, Vec4(1.0, 2.0, 3.0, 4.0), 2.0)
    }

    @Test
    fun `should inverse`() {
        val expected = Vec4(1.0 / 2.0, 1.0 / 3.0, 1.0 / -4.0, 1.0 / -8.0)
        val inverseOperation = { args: Array<out Any?>, dst: Vec4? ->
            val v1 = args[0] as Vec4
            v1.inverse(dst ?: Vec4())
        }
        testV4WithAndWithoutDest(inverseOperation, expected, Vec4(2.0, 3.0, -4.0, -8.0))
    }

     @Test
    fun `should invert`() { // Alias test
        val expected = Vec4(1.0 / 2.0, 1.0 / 3.0, 1.0 / -4.0, 1.0 / -8.0)
        val invertOperation = { args: Array<out Any?>, dst: Vec4? ->
            val v1 = args[0] as Vec4
            v1.invert(dst ?: Vec4())
        }
        testV4WithAndWithoutDest(invertOperation, expected, Vec4(2.0, 3.0, -4.0, -8.0))
    }

    @Test
    fun `should compute dot product`() {
        val expected = 1.0 * 2.0 + 2.0 * 4.0 + 3.0 * 6.0 + 4.0 * 8.0
        val value = Vec4(1.0, 2.0, 3.0, 4.0).dot(Vec4(2.0, 4.0, 6.0, 8.0))
        assertEqualsApproximately(expected, value)
    }

    @Test
    fun `should compute length`() {
        val expected = sqrt(1.0 * 1.0 + 2.0 * 2.0 + 3.0 * 3.0 + 4.0 * 4.0)
        val value = Vec4(1.0, 2.0, 3.0, 4.0).length
        assertEqualsApproximately(expected, value)
    }

    @Test
    fun `should compute length squared`() {
        val expected = 1.0 * 1.0 + 2.0 * 2.0 + 3.0 * 3.0 + 4.0 * 4.0
        val value = Vec4(1.0, 2.0, 3.0, 4.0).lengthSq
        assertEqualsApproximately(expected, value)
    }

    @Test
    fun `should compute len`() {
        val expected = sqrt(1.0 * 1.0 + 2.0 * 2.0 + 3.0 * 3.0 + 4.0 * 4.0)
        val value = Vec4(1.0, 2.0, 3.0, 4.0).len
        assertEqualsApproximately(expected, value)
    }

    @Test
    fun `should compute lenSq`() {
        val expected = 1.0 * 1.0 + 2.0 * 2.0 + 3.0 * 3.0 + 4.0 * 4.0
        val value = Vec4(1.0, 2.0, 3.0, 4.0).lenSq
        assertEqualsApproximately(expected, value)
    }

    @Test
    fun `should compute distance`() {
        val expected = sqrt(2.0 * 2.0 + 3.0 * 3.0 + 4.0 * 4.0 + 5.0 * 5.0)
        val value = Vec4(1.0, 2.0, 3.0, 4.0).distance(Vec4(3.0, 5.0, 7.0, 9.0))
        assertEqualsApproximately(expected, value)
    }

    @Test
    fun `should compute distance squared`() {
        val expected = 2.0 * 2.0 + 3.0 * 3.0 + 4.0 * 4.0 + 5.0 * 5.0
        val value = Vec4(1.0, 2.0, 3.0, 4.0).distanceSq(Vec4(3.0, 5.0, 7.0, 9.0))
        assertEqualsApproximately(expected, value)
    }

    @Test
    fun `should compute dist`() {
        val expected = sqrt(2.0 * 2.0 + 3.0 * 3.0 + 4.0 * 4.0 + 5.0 * 5.0)
        val value = Vec4(1.0, 2.0, 3.0, 4.0).dist(Vec4(3.0, 5.0, 7.0, 9.0))
        assertEqualsApproximately(expected, value)
    }

    @Test
    fun `should compute dist squared`() {
        val expected = 2.0 * 2.0 + 3.0 * 3.0 + 4.0 * 4.0 + 5.0 * 5.0
        val value = Vec4(1.0, 2.0, 3.0, 4.0).distSq(Vec4(3.0, 5.0, 7.0, 9.0))
        assertEqualsApproximately(expected, value)
    }

    @Test
    fun `should normalize`() {
        val length = sqrt(1.0 * 1.0 + 2.0 * 2.0 + 3.0 * 3.0 + 4.0 * 4.0)
        val expected = Vec4(
            1.0 / length,
            2.0 / length,
            3.0 / length,
            4.0 / length
        )
        val normalizeOperation = { args: Array<out Any?>, dst: Vec4? ->
            val v1 = args[0] as Vec4
            v1.normalize(dst ?: Vec4())
        }
        testV4WithAndWithoutDest(normalizeOperation, expected, Vec4(1.0, 2.0, 3.0, 4.0))
    }

    @Test
    fun `should negate`() {
        val expected = Vec4(-1.0, -2.0, -3.0, 4.0)
        val negateOperation = { args: Array<out Any?>, dst: Vec4? ->
            val v1 = args[0] as Vec4
            v1.negate(dst ?: Vec4())
        }
        testV4WithAndWithoutDest(negateOperation, expected, Vec4(1.0, 2.0, 3.0, -4.0))
    }

    @Test
    fun `should copy`() {
        val expected = Vec4(1.0, 2.0, 3.0, 4.0)
        val v = Vec4(1.0, 2.0, 3.0, 4.0)
        val copyOperation = { args: Array<out Any?>, dst: Vec4? ->
            val v1 = args[0] as Vec4
            if (dst != null) {
                v1.copy(dst)
                dst
            } else {
                v1.copy() // data class copy
            }
        }
        // Test without dest (uses data class copy)
        val resultNoDest = copyOperation(arrayOf(v), null) as Vec4
        assertNotSame(v, resultNoDest, "copy() without dest should create a new instance")
        assertVec4EqualsApproximately(expected, resultNoDest)

        // Test with dest (uses the instance copy method)
        testV4WithDest(copyOperation, expected, v)
    }

    @Test
    fun `should clone`() { // Alias test
        val expected = Vec4(1.0, 2.0, 3.0, 4.0)
        val v = Vec4(1.0, 2.0, 3.0, 4.0)
        val cloneOperation = { args: Array<out Any?>, dst: Vec4? ->
            val v1 = args[0] as Vec4
            v1.clone(dst ?: Vec4()) // Assumes clone method exists similar to copy
        }
        // Test without dest
        val resultNoDest = cloneOperation(arrayOf(v), null) as Vec4
        assertNotSame(v, resultNoDest, "clone() without dest should create a new instance")
        assertVec4EqualsApproximately(expected, resultNoDest)

        // Test with dest
        testV4WithDest(cloneOperation, expected, v)
    }

    @Test
    fun `should set`() { // Tests Vec4 instance set method
        val expected = Vec4(2.0, 3.0, 4.0, 5.0)
        // Test without dest (modifies instance)
        val vSet1 = Vec4()
        vSet1.set(2.0, 3.0, 4.0, 5.0)
        assertVec4EqualsApproximately(expected, vSet1)

        // Test with dest (should modify dest, but set returns `this`)
        val vOrig = Vec4(1.0, 1.0, 1.0, 1.0)
        val vDest = Vec4() // Not used by instance set
        val vSet2 = vOrig.set(2.0, 3.0, 4.0, 5.0) // Modifies vOrig
        assertSame(vOrig, vSet2)
        assertVec4EqualsApproximately(expected, vOrig)
    }

    @Test
    fun `should multiply`() {
        val expected = Vec4(2.0, 8.0, 18.0, 32.0)
        val multiplyOperation = { args: Array<out Any?>, dst: Vec4? ->
            val v1 = args[0] as Vec4
            val v2 = args[1] as Vec4
            v1.multiply(v2, dst ?: Vec4())
        }
        testV4WithAndWithoutDest(multiplyOperation, expected, Vec4(1.0, 2.0, 3.0, 4.0), Vec4(2.0, 4.0, 6.0, 8.0))
    }

    @Test
    fun `should mul`() {
        val expected = Vec4(2.0, 8.0, 18.0, 32.0)
        val mulOperation = { args: Array<out Any?>, dst: Vec4? ->
            val v1 = args[0] as Vec4
            val v2 = args[1] as Vec4
            v1.mul(v2, dst ?: Vec4())
        }
        testV4WithAndWithoutDest(mulOperation, expected, Vec4(1.0, 2.0, 3.0, 4.0), Vec4(2.0, 4.0, 6.0, 8.0))
    }

    @Test
    fun `should divide`() {
        val expected = Vec4(1.0 / 2.0, 2.0 / 3.0, 3.0 / 4.0, 4.0 / 5.0)
        val divideOperation = { args: Array<out Any?>, dst: Vec4? ->
            val v1 = args[0] as Vec4
            val v2 = args[1] as Vec4
            v1.divide(v2, dst ?: Vec4())
        }
        testV4WithAndWithoutDest(divideOperation, expected, Vec4(1.0, 2.0, 3.0, 4.0), Vec4(2.0, 3.0, 4.0, 5.0))
    }

    @Test
    fun `should div`() {
        val expected = Vec4(1.0 / 2.0, 2.0 / 3.0, 3.0 / 4.0, 4.0 / 5.0)
        val divOperation = { args: Array<out Any?>, dst: Vec4? ->
            val v1 = args[0] as Vec4
            val v2 = args[1] as Vec4
            v1.div(v2, dst ?: Vec4())
        }
        testV4WithAndWithoutDest(divOperation, expected, Vec4(1.0, 2.0, 3.0, 4.0), Vec4(2.0, 3.0, 4.0, 5.0))
    }

    @Test
    fun `should fromValues`() { // Tests Vec4.Companion.fromValues
        val expected = Vec4(1.0, 2.0, 3.0, 4.0)
        val v1 = Vec4.fromValues(1.0, 2.0, 3.0, 4.0)
        assertEquals(expected, v1) // Exact comparison ok here
    }

    @Test
    fun `should transform by 4x4`() {
        // Requires Mat4 class which is not available.
        // Follows pattern from Vec3Test.kt
         val expected = Vec4(17.0, 24.0, 33.0, 4.0)
         val m = Mat4(
             1f, 0.0f, 0.0f, 0.0f, // Col 0
             0f, 2f, 0.0f, 0.0f, // Col 1
             0f, 0.0f, 3.0f, 0.0f, // Col 2
             4f, 5.0f, 6.0f, 1.0f  // Col 3
         )


         val transformOperation = { args: Array<out Any?>, dst: Vec4? ->
             val v = args[0] as Vec4
             val mat = args[1] as Mat4
             v.transformMat4(mat, dst ?: Vec4())
         }
         testV4WithAndWithoutDest(transformOperation, expected, Vec4(1.0, 2.0, 3.0, 4.0), m)
    }

    @Test
    fun `should zero`() { // Tests Vec4 instance zero method
        val vInstance = Vec4(1.0, 2.0, 3.0, 4.0)
        val resultInstance = vInstance.zero() // Modifies vInstance and returns it
        assertSame(vInstance, resultInstance)
        assertVec4Equals(Vec4(0.0, 0.0, 0.0, 0.0), vInstance)

        // Test with destination (should modify destination)
        val vDest = Vec4()
        val vOrig = Vec4(1.0, 2.0, 3.0, 4.0)
        val resultDest = vOrig.zero(vDest) // Pass dest
        assertSame(vDest, resultDest)
        assertVec4Equals(Vec4(0.0, 0.0, 0.0, 0.0), vDest)
        assertVec4Equals(Vec4(1.0, 2.0, 3.0, 4.0), vOrig) // Original should be unchanged
    }

    @Test
    fun `should setLength`() {
        // JS test expected [7.3, 7.3, 7.3, 7.3] for input [1,1,1,1] len 14.6
        // Length of [1,1,1,1] is sqrt(4) = 2.
        // Normalized is [0.5, 0.5, 0.5, 0.5].
        // Scaled by 14.6 is [7.3, 7.3, 7.3, 7.3].
        val expected = Vec4(7.3, 7.3, 7.3, 7.3)
        val setLengthOperation = { args: Array<out Any?>, dst: Vec4? ->
            val v = args[0] as Vec4
            val len = args[1] as Double
            v.setLength(len, dst ?: Vec4())
        }
        testV4WithAndWithoutDest(setLengthOperation, expected, Vec4(1.0, 1.0, 1.0, 1.0), 14.6)
    }

    @Test
    fun `should truncate - shorten when too long`() {
        // JS test expected [2.721655, 4.082483, 5.443310, 6.804138] for input [20,30,40,50] maxLen 10
        // Length of [20,30,40,50] = sqrt(400 + 900 + 1600 + 2500) = sqrt(5400) approx 73.48
        // Normalized approx [0.272, 0.408, 0.544, 0.680]
        // Scaled by 10 approx [2.72, 4.08, 5.44, 6.80]
        val expected = Vec4(2.721655269759087, 4.08248290463863, 5.443310539518174, 6.804138174397717)
        val truncateOperation = { args: Array<out Any?>, dst: Vec4? ->
            val v = args[0] as Vec4
            val maxLen = args[1] as Double
            v.truncate(maxLen, dst ?: Vec4())
        }
        testV4WithAndWithoutDest(truncateOperation, expected, Vec4(20.0, 30.0, 40.0, 50.0), 10.0)
    }

    @Test
    fun `should truncate - preserve the vector when shorter than maxLen`() {
       val expected = Vec4(20.0, 30.0, 40.0, 50.0)
       val truncateOperation = { args: Array<out Any?>, dst: Vec4? ->
           val v = args[0] as Vec4
           val maxLen = args[1] as Double
           v.truncate(maxLen, dst ?: Vec4())
       }
       testV4WithAndWithoutDest(truncateOperation, expected, Vec4(20.0, 30.0, 40.0, 50.0), 100.0)
    }

    @Test
    fun `should midpoint`() {
        val expected = Vec4(6.0, 12.0, 18.0, 24.0)
        val midpointOperation = { args: Array<out Any?>, dst: Vec4? ->
            val v1 = args[0] as Vec4
            val v2 = args[1] as Vec4
            v1.midpoint(v2, dst ?: Vec4())
        }
        testV4WithAndWithoutDest(midpointOperation, expected, Vec4(1.0, 2.0, 3.0, 4.0), Vec4(11.0, 22.0, 33.0, 44.0))
    }

    @Test
    fun `should midpoint - handle negatives`() {
        val expected = Vec4(-5.0, -10.0, -15.0, -20.0)
        val midpointOperation = { args: Array<out Any?>, dst: Vec4? ->
            val v1 = args[0] as Vec4
            val v2 = args[1] as Vec4
            v1.midpoint(v2, dst ?: Vec4())
        }
        testV4WithAndWithoutDest(midpointOperation, expected, Vec4(1.0, 2.0, 3.0, 4.0), Vec4(-11.0, -22.0, -33.0, -44.0))
    }
}