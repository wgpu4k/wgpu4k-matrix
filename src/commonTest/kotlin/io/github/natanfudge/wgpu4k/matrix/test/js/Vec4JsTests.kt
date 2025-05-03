package io.github.natanfudge.wgpu4k.matrix.test.js

import io.github.natanfudge.wgpu4k.matrix.Mat4f
import io.github.natanfudge.wgpu4k.matrix.Vec4f
import kotlin.math.*
import kotlin.test.*

// --- Test Suite ---

class Vec4Tests {

    // Helper to mimic the JS 'clone' for testing
    private fun clone(v: Any?): Any? {
        return when (v) {
            is Vec4f -> v.copy() // Use the data class copy for Vec4
            is FloatArray -> v.copyOf()
            // Add other types like Mat4 if needed and available
            else -> v // Assume immutable primitives or objects handled by value/reference correctly
        }
    }

    // Helper testing function result without explicit destination
    private fun testV4WithoutDest(
        operation: (args: Array<out Any?>, dst: Vec4f?) -> Any?, // Lambda representing the Vec4 operation
        expected: Any?,
        vararg args: Any? // Original arguments for the operation
    ) {
        val clonedArgs = args.map { clone(it) }.toTypedArray()
        val d = operation(clonedArgs, null) // Operation should create a new Vec4 internally

        when (expected) {
            is Vec4f -> assertVec4EqualsApproximately(expected, d as Vec4f)
            is FloatArray -> {
                if (d is Vec4f) {
                    assertVec4EqualsApproximately(expected, d)
                } else {
                    fail("Expected Vec4 result when expected is FloatArray, but got ${d?.let { it::class.simpleName }}")
                }
            }
            is Float -> assertEqualsApproximately(expected, d as Float)
            is Boolean -> assertEquals(expected, d as Boolean)
            else -> assertEquals(expected, d) // Fallback direct comparison
        }

        // Check the original arguments were not modified
        args.zip(clonedArgs).forEachIndexed { index, pair ->
             if (pair.first is Vec4f) {
                 assertVec4Equals(pair.first as Vec4f, pair.second as Vec4f, "Source vector (arg $index) modified unexpectedly in testV4WithoutDest")
             }
             // Add checks for other mutable types if needed (e.g., Mat4)
        }
    }

    // Helper testing function result with explicit destination
    private fun testV4WithDest(
        operation: (args: Array<out Any?>, dst: Vec4f?) -> Any?, // Lambda representing the Vec4 operation
        expected: Any?,
        vararg args: Any? // Original arguments for the operation
    ) {
        val expectedCloned = clone(expected) // Clone expected value for comparison
        val destVector = Vec4f() // Create the destination vector

        // --- Test with standard destination ---
        run {
            val clonedArgs = args.map { clone(it) }.toTypedArray() // Clone inputs for this run
            val c = operation(clonedArgs, destVector)

            assertSame(c, destVector, "Function with dest should return the dest instance")

            when (expectedCloned) {
                is Vec4f -> assertVec4EqualsApproximately(expectedCloned, c as Vec4f)
                is FloatArray -> assertVec4EqualsApproximately(expectedCloned, c as Vec4f)
                else -> fail("testV4WithDest expects Vec4 or FloatArray for 'expected' value")
            }

            // Ensure original inputs were not modified
            args.zip(clonedArgs).forEachIndexed { index, pair ->
                if (pair.first is Vec4f) {
                    assertVec4Equals(pair.first as Vec4f, pair.second as Vec4f, "Source vector (arg $index) modified unexpectedly in testV4WithDest (standard dest)")
                }
                 // Add checks for other mutable types if needed
            }
        }

        // --- Test aliasing: first argument is destination ---
        if (args.isNotEmpty() && args[0] is Vec4f) {
            val firstArgAlias = clone(args[0]) as Vec4f
            val clonedRemainingArgs = args.drop(1).map { clone(it) }.toTypedArray()
            val allArgsForAlias1 = arrayOf(firstArgAlias, *clonedRemainingArgs)

            val cAlias1 = operation(allArgsForAlias1, firstArgAlias)

            assertSame(cAlias1, firstArgAlias, "Aliasing test (firstArg == dest) should return the dest instance")
            when (expectedCloned) {
                is Vec4f -> assertVec4EqualsApproximately(expectedCloned, cAlias1 as Vec4f, message="Aliasing test (firstArg == dest) result mismatch")
                is FloatArray -> assertVec4EqualsApproximately(expectedCloned, cAlias1 as Vec4f, message="Aliasing test (firstArg == dest) result mismatch")
            }
            // Check other original args were not modified
            args.drop(1).zip(clonedRemainingArgs).forEachIndexed { index, pair ->
                if (pair.first is Vec4f) {
                    assertVec4Equals(pair.first as Vec4f, pair.second as Vec4f, "Aliasing test (firstArg == dest): Source vector (arg ${index + 1}) modified unexpectedly")
                }
                 // Add checks for other mutable types if needed
            }
        }


        // --- Test aliasing: another Vec4 argument is destination ---
        val firstOperandIndex = args.indexOfFirst { it is Vec4f && it !== args[0] }
        if (firstOperandIndex != -1) {
            val operandAlias = clone(args[firstOperandIndex]) as Vec4f
            val clonedArgsForAlias2 = args.mapIndexed { index, arg ->
                if (index == firstOperandIndex) operandAlias else clone(arg)
            }.toTypedArray()

            val cAlias2 = operation(clonedArgsForAlias2, operandAlias)

            assertSame(cAlias2, operandAlias, "Aliasing test (operand == dest) should return the dest instance")
            when (expectedCloned) {
                is Vec4f -> assertVec4EqualsApproximately(expectedCloned, cAlias2 as Vec4f, message="Aliasing test (operand == dest) result mismatch")
                is FloatArray -> assertVec4EqualsApproximately(expectedCloned, cAlias2 as Vec4f, message="Aliasing test (operand == dest) result mismatch")
            }
            // Check original args (that were not the alias dest) were not modified
            args.zip(clonedArgsForAlias2).forEachIndexed { index, pair ->
                 if (index != firstOperandIndex) {
                     if (pair.first is Vec4f) {
                         assertVec4Equals(pair.first as Vec4f, pair.second as Vec4f, "Aliasing test (operand == dest): Source vector (arg $index) modified unexpectedly")
                     }
                     // Add checks for other mutable types if needed
                 }
            }
        }
    }

    // Combined test helper
    private fun testV4WithAndWithoutDest(
        operation: (args: Array<out Any?>, dst: Vec4f?) -> Any?,
        expected: Any?,
        vararg args: Any?
    ) {
        testV4WithoutDest(operation, expected, *args)

        if (expected is Vec4f || expected is FloatArray) {
             testV4WithDest(operation, expected, *args)
        }
    }


    // --- Actual Tests ---

    @Test
    fun `should add`() {
        val expected = Vec4f(3.0f, 5.0f, 7.0f, 9.0f)
        val addOperation = { args: Array<out Any?>, dst: Vec4f? ->
            val v1 = args[0] as Vec4f
            val v2 = args[1] as Vec4f
            v1.add(v2, dst ?: Vec4f())
        }
        testV4WithAndWithoutDest(addOperation, expected, Vec4f(1.0f, 2.0f, 3.0f, 4.0f), Vec4f(2.0f, 3.0f, 4.0f, 5.0f))
    }

    @Test
    fun `should compute ceil`() {
        val expected = Vec4f(2.0f, -1.0f, 3.0f, -4.0f)
        val ceilOperation = { args: Array<out Any?>, dst: Vec4f? ->
            val v1 = args[0] as Vec4f
            v1.ceil(dst ?: Vec4f())
        }
        testV4WithAndWithoutDest(ceilOperation, expected, Vec4f(1.1f, -1.1f, 2.9f, -4.2f))
    }

    @Test
    fun `should compute floor`() {
        val expected = Vec4f(1.0f, -2.0f, 2.0f, -4.0f)
        val floorOperation = { args: Array<out Any?>, dst: Vec4f? ->
            val v1 = args[0] as Vec4f
            v1.floor(dst ?: Vec4f())
        }
        testV4WithAndWithoutDest(floorOperation, expected, Vec4f(1.1f, -1.1f, 2.9f, -3.1f))
    }

    @Test
    fun `should compute round`() {
        val expected = Vec4f(1.0f, -1.0f, 3.0f, 0.0f)
        val roundOperation = { args: Array<out Any?>, dst: Vec4f? ->
            val v1 = args[0] as Vec4f
            v1.round(dst ?: Vec4f())
        }
        testV4WithAndWithoutDest(roundOperation, expected, Vec4f(1.1f, -1.1f, 2.9f, 0.1f))
    }

    @Test
    fun `should clamp`() {
        run {
            val expected = Vec4f(1.0f, 0.0f, 0.5f, 0.0f)
            val clampOperation = { args: Array<out Any?>, dst: Vec4f? ->
                val v1 = args[0] as Vec4f
                val min = args[1] as Float
                val max = args[2] as Float
                v1.clamp(min, max, dst ?: Vec4f())
            }
            testV4WithAndWithoutDest(clampOperation, expected, Vec4f(2.0f, -1.0f, 0.5f, -4.0f), 0.0f, 1.0f)
        }
        run {
            val expected = Vec4f(-10.0f, 5.0f, 2.9f, -9.0f)
            val clampOperation = { args: Array<out Any?>, dst: Vec4f? ->
                val v1 = args[0] as Vec4f
                val min = args[1] as Float
                val max = args[2] as Float
                v1.clamp(min, max, dst ?: Vec4f())
            }
            testV4WithAndWithoutDest(clampOperation, expected, Vec4f(-22.0f, 50.0f, 2.9f, -9.0f), -10.0f, 5.0f)
        }
    }

    @Test
    fun `should equals approximately`() {
        assertTrue(Vec4f(1.0f, 2.0f, 3.0f, 4.0f).equalsApproximately(Vec4f(1.0f, 2.0f, 3.0f, 4.0f)))
        assertTrue(Vec4f(1.0f, 2.0f, 3.0f, 4.0f).equalsApproximately(Vec4f(1.0f + Vec4f.EPSILON * 0.5f, 2.0f, 3.0f, 4.0f)))
        assertTrue(Vec4f(1.0f, 2.0f, 3.0f, 4.0f).equalsApproximately(Vec4f(1.0f, 2.0f + Vec4f.EPSILON * 0.5f, 3.0f, 4.0f)))
        assertTrue(Vec4f(1.0f, 2.0f, 3.0f, 4.0f).equalsApproximately(Vec4f(1.0f, 2.0f, 3.0f + Vec4f.EPSILON * 0.5f, 4.0f)))
        assertTrue(Vec4f(1.0f, 2.0f, 3.0f, 4.0f).equalsApproximately(Vec4f(1.0f, 2.0f, 3.0f, 4.0f + Vec4f.EPSILON * 0.5f)))
        assertFalse(Vec4f(1.0f, 2.0f, 3.0f, 4.0f).equalsApproximately(Vec4f(1.0001f, 2.0f, 3.0f, 4.0f)))
        assertFalse(Vec4f(1.0f, 2.0f, 3.0f, 4.0f).equalsApproximately(Vec4f(1.0f, 2.0001f, 3.0f, 4.0f)))
        assertFalse(Vec4f(1.0f, 2.0f, 3.0f, 4.0f).equalsApproximately(Vec4f(1.0f, 2.0f, 3.0001f, 4.0f)))
        assertFalse(Vec4f(1.0f, 2.0f, 3.0f, 4.0f).equalsApproximately(Vec4f(1.0f, 2.0f, 3.0f, 4.0001f)))
    }

    @Test
    fun `should equals`() {
        assertTrue(Vec4f(1.0f, 2.0f, 3.0f, 4.0f).equals(Vec4f(1.0f, 2.0f, 3.0f, 4.0f)))
        assertFalse(Vec4f(1.0f, 2.0f, 3.0f, 4.0f).equals(Vec4f(1.0f + Vec4f.EPSILON * 0.5f, 2.0f, 3.0f, 4.0f)))
        assertFalse(Vec4f(1.0f, 2.0f, 3.0f, 4.0f).equals(Vec4f(1.0f, 2.0f + Vec4f.EPSILON * 0.5f, 3.0f, 4.0f)))
        assertFalse(Vec4f(1.0f, 2.0f, 3.0f, 4.0f).equals(Vec4f(1.0f, 2.0f, 3.0f + Vec4f.EPSILON * 0.5f, 4.0f)))
        assertFalse(Vec4f(1.0f, 2.0f, 3.0f, 4.0f).equals(Vec4f(1.0f, 2.0f, 3.0f, 4.0f + Vec4f.EPSILON * 0.5f)))
    }

    @Test
    fun `should subtract`() {
        val expected = Vec4f(-1.0f, -2.0f, -3.0f, -4.0f)
        val subtractOperation = { args: Array<out Any?>, dst: Vec4f? ->
            val v1 = args[0] as Vec4f
            val v2 = args[1] as Vec4f
            v1.subtract(v2, dst ?: Vec4f())
        }
        testV4WithAndWithoutDest(subtractOperation, expected, Vec4f(1.0f, 2.0f, 3.0f, 4.0f), Vec4f(2.0f, 4.0f, 6.0f, 8.0f))
    }

    @Test
    fun `should sub`() {
        val expected = Vec4f(-1.0f, -2.0f, -3.0f, -4.0f)
        val subOperation = { args: Array<out Any?>, dst: Vec4f? ->
            val v1 = args[0] as Vec4f
            val v2 = args[1] as Vec4f
            v1.sub(v2, dst ?: Vec4f())
        }
        testV4WithAndWithoutDest(subOperation, expected, Vec4f(1.0f, 2.0f, 3.0f, 4.0f), Vec4f(2.0f, 4.0f, 6.0f, 8.0f))
    }

    @Test
    fun `should lerp`() {
        val expected = Vec4f(1.5f, 3.0f, 4.5f, 6.0f)
        val lerpOperation = { args: Array<out Any?>, dst: Vec4f? ->
            val v1 = args[0] as Vec4f
            val v2 = args[1] as Vec4f
            val t = args[2] as Float
            v1.lerp(v2, t, dst ?: Vec4f())
        }
        testV4WithAndWithoutDest(lerpOperation, expected, Vec4f(1.0f, 2.0f, 3.0f, 4.0f), Vec4f(2.0f, 4.0f, 6.0f, 8.0f), 0.5f)
    }

    @Test
    fun `should lerp under 0`() {
        val expected = Vec4f(0.5f, 1.0f, 1.5f, 2.0f)
        val lerpOperation = { args: Array<out Any?>, dst: Vec4f? ->
            val v1 = args[0] as Vec4f
            val v2 = args[1] as Vec4f
            val t = args[2] as Float
            v1.lerp(v2, t, dst ?: Vec4f())
        }
        testV4WithAndWithoutDest(lerpOperation, expected, Vec4f(1.0f, 2.0f, 3.0f, 4.0f), Vec4f(2.0f, 4.0f, 6.0f, 8.0f), -0.5f)
    }

    @Test
    fun `should lerp over 1`() { // Renamed from 'lerp over 0'
        val expected = Vec4f(2.5f, 5.0f, 7.5f, 10.0f)
        val lerpOperation = { args: Array<out Any?>, dst: Vec4f? ->
            val v1 = args[0] as Vec4f
            val v2 = args[1] as Vec4f
            val t = args[2] as Float
            v1.lerp(v2, t, dst ?: Vec4f())
        }
        testV4WithAndWithoutDest(lerpOperation, expected, Vec4f(1.0f, 2.0f, 3.0f, 4.0f), Vec4f(2.0f, 4.0f, 6.0f, 8.0f), 1.5f)
    }

    @Test
    fun `should multiply by scalar`() {
        val expected = Vec4f(2.0f, 4.0f, 6.0f, 8.0f)
        val mulScalarOperation = { args: Array<out Any?>, dst: Vec4f? ->
            val v1 = args[0] as Vec4f
            val k = args[1] as Float
            v1.mulScalar(k, dst ?: Vec4f())
        }
        testV4WithAndWithoutDest(mulScalarOperation, expected, Vec4f(1.0f, 2.0f, 3.0f, 4.0f), 2.0f)
    }

    @Test
    fun `should scale`() {
        val expected = Vec4f(2.0f, 4.0f, 6.0f, 8.0f)
        val scaleOperation = { args: Array<out Any?>, dst: Vec4f? ->
            val v1 = args[0] as Vec4f
            val k = args[1] as Float
            v1.scale(k, dst ?: Vec4f())
        }
        testV4WithAndWithoutDest(scaleOperation, expected, Vec4f(1.0f, 2.0f, 3.0f, 4.0f), 2.0f)
    }

    @Test
    fun `should add scaled`() {
        val expected = Vec4f(5.0f, 10.0f, 15.0f, 20.0f)
        val addScaledOperation = { args: Array<out Any?>, dst: Vec4f? ->
            val v1 = args[0] as Vec4f
            val v2 = args[1] as Vec4f
            val scale = args[2] as Float
            v1.addScaled(v2, scale, dst ?: Vec4f())
        }
        testV4WithAndWithoutDest(addScaledOperation, expected, Vec4f(1.0f, 2.0f, 3.0f, 4.0f), Vec4f(2.0f, 4.0f, 6.0f, 8.0f), 2.0f)
    }

    @Test
    fun `should divide by scalar`() {
        val expected = Vec4f(0.5f, 1.0f, 1.5f, 2.0f)
        val divScalarOperation = { args: Array<out Any?>, dst: Vec4f? ->
            val v1 = args[0] as Vec4f
            val k = args[1] as Float
            v1.divScalar(k, dst ?: Vec4f())
        }
        testV4WithAndWithoutDest(divScalarOperation, expected, Vec4f(1.0f, 2.0f, 3.0f, 4.0f), 2.0f)
    }

    @Test
    fun `should inverse`() {
        val expected = Vec4f(1.0f / 2.0f, 1.0f / 3.0f, 1.0f / -4.0f, 1.0f / -8.0f)
        val inverseOperation = { args: Array<out Any?>, dst: Vec4f? ->
            val v1 = args[0] as Vec4f
            v1.inverse(dst ?: Vec4f())
        }
        testV4WithAndWithoutDest(inverseOperation, expected, Vec4f(2.0f, 3.0f, -4.0f, -8.0f))
    }

     @Test
    fun `should invert`() { // Alias test
        val expected = Vec4f(1.0f / 2.0f, 1.0f / 3.0f, 1.0f / -4.0f, 1.0f / -8.0f)
        val invertOperation = { args: Array<out Any?>, dst: Vec4f? ->
            val v1 = args[0] as Vec4f
            v1.invert(dst ?: Vec4f())
        }
        testV4WithAndWithoutDest(invertOperation, expected, Vec4f(2.0f, 3.0f, -4.0f, -8.0f))
    }

    @Test
    fun `should compute dot product`() {
        val expected = 1.0f * 2.0f + 2.0f * 4.0f + 3.0f * 6.0f + 4.0f * 8.0f
        val value = Vec4f(1.0f, 2.0f, 3.0f, 4.0f).dot(Vec4f(2.0f, 4.0f, 6.0f, 8.0f))
        assertEqualsApproximately(expected, value)
    }

    @Test
    fun `should compute length`() {
        val expected = sqrt(1.0f * 1.0f + 2.0f * 2.0f + 3.0f * 3.0f + 4.0f * 4.0f)
        val value = Vec4f(1.0f, 2.0f, 3.0f, 4.0f).length
        assertEqualsApproximately(expected, value)
    }

    @Test
    fun `should compute length squared`() {
        val expected = 1.0f * 1.0f + 2.0f * 2.0f + 3.0f * 3.0f + 4.0f * 4.0f
        val value = Vec4f(1.0f, 2.0f, 3.0f, 4.0f).lengthSq
        assertEqualsApproximately(expected, value)
    }

    @Test
    fun `should compute len`() {
        val expected = sqrt(1.0f * 1.0f + 2.0f * 2.0f + 3.0f * 3.0f + 4.0f * 4.0f)
        val value = Vec4f(1.0f, 2.0f, 3.0f, 4.0f).len
        assertEqualsApproximately(expected, value)
    }

    @Test
    fun `should compute lenSq`() {
        val expected = 1.0f * 1.0f + 2.0f * 2.0f + 3.0f * 3.0f + 4.0f * 4.0f
        val value = Vec4f(1.0f, 2.0f, 3.0f, 4.0f).lenSq
        assertEqualsApproximately(expected, value)
    }

    @Test
    fun `should compute distance`() {
        val expected = sqrt(2.0f * 2.0f + 3.0f * 3.0f + 4.0f * 4.0f + 5.0f * 5.0f)
        val value = Vec4f(1.0f, 2.0f, 3.0f, 4.0f).distance(Vec4f(3.0f, 5.0f, 7.0f, 9.0f))
        assertEqualsApproximately(expected, value)
    }

    @Test
    fun `should compute distance squared`() {
        val expected = 2.0f * 2.0f + 3.0f * 3.0f + 4.0f * 4.0f + 5.0f * 5.0f
        val value = Vec4f(1.0f, 2.0f, 3.0f, 4.0f).distanceSq(Vec4f(3.0f, 5.0f, 7.0f, 9.0f))
        assertEqualsApproximately(expected, value)
    }

    @Test
    fun `should compute dist`() {
        val expected = sqrt(2.0f * 2.0f + 3.0f * 3.0f + 4.0f * 4.0f + 5.0f * 5.0f)
        val value = Vec4f(1.0f, 2.0f, 3.0f, 4.0f).dist(Vec4f(3.0f, 5.0f, 7.0f, 9.0f))
        assertEqualsApproximately(expected, value)
    }

    @Test
    fun `should compute dist squared`() {
        val expected = 2.0f * 2.0f + 3.0f * 3.0f + 4.0f * 4.0f + 5.0f * 5.0f
        val value = Vec4f(1.0f, 2.0f, 3.0f, 4.0f).distSq(Vec4f(3.0f, 5.0f, 7.0f, 9.0f))
        assertEqualsApproximately(expected, value)
    }

    @Test
    fun `should normalize`() {
        val length = sqrt(1.0f * 1.0f + 2.0f * 2.0f + 3.0f * 3.0f + 4.0f * 4.0f)
        val expected = Vec4f(
            1.0f / length,
            2.0f / length,
            3.0f / length,
            4.0f / length
        )
        val normalizeOperation = { args: Array<out Any?>, dst: Vec4f? ->
            val v1 = args[0] as Vec4f
            v1.normalize(dst ?: Vec4f())
        }
        testV4WithAndWithoutDest(normalizeOperation, expected, Vec4f(1.0f, 2.0f, 3.0f, 4.0f))
    }

    @Test
    fun `should negate`() {
        val expected = Vec4f(-1.0f, -2.0f, -3.0f, 4.0f)
        val negateOperation = { args: Array<out Any?>, dst: Vec4f? ->
            val v1 = args[0] as Vec4f
            v1.negate(dst ?: Vec4f())
        }
        testV4WithAndWithoutDest(negateOperation, expected, Vec4f(1.0f, 2.0f, 3.0f, -4.0f))
    }

    @Test
    fun `should copy`() {
        val expected = Vec4f(1.0f, 2.0f, 3.0f, 4.0f)
        val v = Vec4f(1.0f, 2.0f, 3.0f, 4.0f)
        val copyOperation = { args: Array<out Any?>, dst: Vec4f? ->
            val v1 = args[0] as Vec4f
            if (dst != null) {
                v1.copy(dst)
                dst
            } else {
                v1.copy() // data class copy
            }
        }
        // Test without dest (uses data class copy)
        val resultNoDest = copyOperation(arrayOf(v), null) as Vec4f
        assertNotSame(v, resultNoDest, "copy() without dest should create a new instance")
        assertVec4EqualsApproximately(expected, resultNoDest)

        // Test with dest (uses the instance copy method)
        testV4WithDest(copyOperation, expected, v)
    }

    @Test
    fun `should clone`() { // Alias test
        val expected = Vec4f(1.0f, 2.0f, 3.0f, 4.0f)
        val v = Vec4f(1.0f, 2.0f, 3.0f, 4.0f)
        val cloneOperation = { args: Array<out Any?>, dst: Vec4f? ->
            val v1 = args[0] as Vec4f
            v1.clone(dst ?: Vec4f()) // Assumes clone method exists similar to copy
        }
        // Test without dest
        val resultNoDest = cloneOperation(arrayOf(v), null) as Vec4f
        assertNotSame(v, resultNoDest, "clone() without dest should create a new instance")
        assertVec4EqualsApproximately(expected, resultNoDest)

        // Test with dest
        testV4WithDest(cloneOperation, expected, v)
    }

    @Test
    fun `should set`() { // Tests Vec4 instance set method
        val expected = Vec4f(2.0f, 3.0f, 4.0f, 5.0f)
        // Test without dest (modifies instance)
        val vSet1 = Vec4f()
        vSet1.set(2.0f, 3.0f, 4.0f, 5.0f)
        assertVec4EqualsApproximately(expected, vSet1)

        // Test with dest (should modify dest, but set returns `this`)
        val vOrig = Vec4f(1.0f, 1.0f, 1.0f, 1.0f)
        val vDest = Vec4f() // Not used by instance set
        val vSet2 = vOrig.set(2.0f, 3.0f, 4.0f, 5.0f) // Modifies vOrig
        assertSame(vOrig, vSet2)
        assertVec4EqualsApproximately(expected, vOrig)
    }

    @Test
    fun `should multiply`() {
        val expected = Vec4f(2.0f, 8.0f, 18.0f, 32.0f)
        val multiplyOperation = { args: Array<out Any?>, dst: Vec4f? ->
            val v1 = args[0] as Vec4f
            val v2 = args[1] as Vec4f
            v1.multiply(v2, dst ?: Vec4f())
        }
        testV4WithAndWithoutDest(multiplyOperation, expected, Vec4f(1.0f, 2.0f, 3.0f, 4.0f), Vec4f(2.0f, 4.0f, 6.0f, 8.0f))
    }

    @Test
    fun `should mul`() {
        val expected = Vec4f(2.0f, 8.0f, 18.0f, 32.0f)
        val mulOperation = { args: Array<out Any?>, dst: Vec4f? ->
            val v1 = args[0] as Vec4f
            val v2 = args[1] as Vec4f
            v1.mul(v2, dst ?: Vec4f())
        }
        testV4WithAndWithoutDest(mulOperation, expected, Vec4f(1.0f, 2.0f, 3.0f, 4.0f), Vec4f(2.0f, 4.0f, 6.0f, 8.0f))
    }

    @Test
    fun `should divide`() {
        val expected = Vec4f(1.0f / 2.0f, 2.0f / 3.0f, 3.0f / 4.0f, 4.0f / 5.0f)
        val divideOperation = { args: Array<out Any?>, dst: Vec4f? ->
            val v1 = args[0] as Vec4f
            val v2 = args[1] as Vec4f
            v1.divide(v2, dst ?: Vec4f())
        }
        testV4WithAndWithoutDest(divideOperation, expected, Vec4f(1.0f, 2.0f, 3.0f, 4.0f), Vec4f(2.0f, 3.0f, 4.0f, 5.0f))
    }

    @Test
    fun `should div`() {
        val expected = Vec4f(1.0f / 2.0f, 2.0f / 3.0f, 3.0f / 4.0f, 4.0f / 5.0f)
        val divOperation = { args: Array<out Any?>, dst: Vec4f? ->
            val v1 = args[0] as Vec4f
            val v2 = args[1] as Vec4f
            v1.div(v2, dst ?: Vec4f())
        }
        testV4WithAndWithoutDest(divOperation, expected, Vec4f(1.0f, 2.0f, 3.0f, 4.0f), Vec4f(2.0f, 3.0f, 4.0f, 5.0f))
    }

    @Test
    fun `should fromValues`() { // Tests Vec4.Companion.fromValues
        val expected = Vec4f(1.0f, 2.0f, 3.0f, 4.0f)
        val v1 = Vec4f.fromValues(1.0f, 2.0f, 3.0f, 4.0f)
        assertEquals(expected, v1) // Exact comparison ok here
    }

    @Test
    fun `should transform by 4x4`() {
        // Requires Mat4 class which is not available.
        // Follows pattern from Vec3JsTests.kt
         val expected = Vec4f(17.0f, 24.0f, 33.0f, 4.0f)
         val m = Mat4f(
             1f, 0.0f, 0.0f, 0.0f, // Col 0
             0f, 2f, 0.0f, 0.0f, // Col 1
             0f, 0.0f, 3.0f, 0.0f, // Col 2
             4f, 5.0f, 6.0f, 1.0f  // Col 3
         )


         val transformOperation = { args: Array<out Any?>, dst: Vec4f? ->
             val v = args[0] as Vec4f
             val mat = args[1] as Mat4f
             v.transformMat4(mat, dst ?: Vec4f())
         }
         testV4WithAndWithoutDest(transformOperation, expected, Vec4f(1.0f, 2.0f, 3.0f, 4.0f), m)
    }

    @Test
    fun `should zero`() { // Tests Vec4 instance zero method
        val vInstance = Vec4f(1.0f, 2.0f, 3.0f, 4.0f)
        val resultInstance = vInstance.zero(vInstance) // Modifies vInstance and returns it
        assertSame(vInstance, resultInstance)
        assertVec4Equals(Vec4f(0.0f, 0.0f, 0.0f, 0.0f), vInstance)

        // Test with destination (should modify destination)
        val vDest = Vec4f()
        val vOrig = Vec4f(1.0f, 2.0f, 3.0f, 4.0f)
        val resultDest = vOrig.zero(vDest) // Pass dest
        assertSame(vDest, resultDest)
        assertVec4Equals(Vec4f(0.0f, 0.0f, 0.0f, 0.0f), vDest)
        assertVec4Equals(Vec4f(1.0f, 2.0f, 3.0f, 4.0f), vOrig) // Original should be unchanged
    }

    @Test
    fun `should setLength`() {
        // JS test expected [7.3, 7.3, 7.3, 7.3] for input [1,1,1,1] len 14.6
        // Length of [1,1,1,1] is sqrt(4) = 2.
        // Normalized is [0.5, 0.5, 0.5, 0.5].
        // Scaled by 14.6 is [7.3, 7.3, 7.3, 7.3].
        val expected = Vec4f(7.3f, 7.3f, 7.3f, 7.3f)
        val setLengthOperation = { args: Array<out Any?>, dst: Vec4f? ->
            val v = args[0] as Vec4f
            val len = args[1] as Float
            v.setLength(len, dst ?: Vec4f())
        }
        testV4WithAndWithoutDest(setLengthOperation, expected, Vec4f(1.0f, 1.0f, 1.0f, 1.0f), 14.6f)
    }

    @Test
    fun `should truncate - shorten when too long`() {
        // JS test expected [2.721655, 4.0f82483, 5.443310, 6.804138] for input [20,30,40,50] maxLen 10
        // Length of [20,30,40,50] = sqrt(400 + 900 + 1600 + 2500) = sqrt(5400) approx 73.48
        // Normalized approx [0.2f72, 0.408, 0.544, 0.680]
        // Scaled by 10 approx [2.72, 4.0f8, 5.44, 6.80]
        val expected = Vec4f(2.721655269759087f, 4.08248290463863f, 5.443310539518174f, 6.804138174397717f)
        val truncateOperation = { args: Array<out Any?>, dst: Vec4f? ->
            val v = args[0] as Vec4f
            val maxLen = args[1] as Float
            v.truncate(maxLen, dst ?: Vec4f())
        }
        testV4WithAndWithoutDest(truncateOperation, expected, Vec4f(20.0f, 30.0f, 40.0f, 50.0f), 10.0f)
    }

    @Test
    fun `should truncate - preserve the vector when shorter than maxLen`() {
       val expected = Vec4f(20.0f, 30.0f, 40.0f, 50.0f)
       val truncateOperation = { args: Array<out Any?>, dst: Vec4f? ->
           val v = args[0] as Vec4f
           val maxLen = args[1] as Float
           v.truncate(maxLen, dst ?: Vec4f())
       }
       testV4WithAndWithoutDest(truncateOperation, expected, Vec4f(20.0f, 30.0f, 40.0f, 50.0f), 100.0f)
    }

    @Test
    fun `should midpoint`() {
        val expected = Vec4f(6.0f, 12.0f, 18.0f, 24.0f)
        val midpointOperation = { args: Array<out Any?>, dst: Vec4f? ->
            val v1 = args[0] as Vec4f
            val v2 = args[1] as Vec4f
            v1.midpoint(v2, dst ?: Vec4f())
        }
        testV4WithAndWithoutDest(midpointOperation, expected, Vec4f(1.0f, 2.0f, 3.0f, 4.0f), Vec4f(11.0f, 22.0f, 33.0f, 44.0f))
    }

    @Test
    fun `should midpoint - handle negatives`() {
        val expected = Vec4f(-5.0f, -10.0f, -15.0f, -20.0f)
        val midpointOperation = { args: Array<out Any?>, dst: Vec4f? ->
            val v1 = args[0] as Vec4f
            val v2 = args[1] as Vec4f
            v1.midpoint(v2, dst ?: Vec4f())
        }
        testV4WithAndWithoutDest(midpointOperation, expected, Vec4f(1.0f, 2.0f, 3.0f, 4.0f), Vec4f(-11.0f, -22.0f, -33.0f, -44.0f))
    }
}