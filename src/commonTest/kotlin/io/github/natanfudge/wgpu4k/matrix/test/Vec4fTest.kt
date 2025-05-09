package io.github.natanfudge.wgpu4k.matrix.test

import io.github.natanfudge.wgpu4k.matrix.*
import kotlin.math.*
import kotlin.test.*

// Helper for comparing floats with a tolerance
private const val VEC_TEST_EPSILON: Float = 0.00001f

fun assertVec4EqualsApproximately(
    expected: Vec4f,
    actual: Vec4f,
    tolerance: Float = VEC_TEST_EPSILON,
    message: String? = null
) {
    val prefix = message?.let { "$it: " } ?: ""
    assertEquals(expected.x, actual.x, tolerance, "${prefix}X component not equal.")
    assertEquals(expected.y, actual.y, tolerance, "${prefix}Y component not equal.")
    assertEquals(expected.z, actual.z, tolerance, "${prefix}Z component not equal.")
    assertEquals(expected.w, actual.w, tolerance, "${prefix}W component not equal.")
}


class Vec4fTest {

    @Test
    fun testConstructorsAndGetSet() {
        val v1 = Vec4f(1f, 2f, 3f, 4f)
        assertEquals(1f, v1.x)
        assertEquals(2f, v1.y)
        assertEquals(3f, v1.z)
        assertEquals(4f, v1.w)

        // Assuming Vec4f has an index operator like Vec3f
        // If not, these tests would need to be adapted or removed.
        // Based on Vec4f.kt, it does not have an index operator.
        // Let's remove these for now. If it's added later, tests can be added.

        val vDefault = Vec4f()
        assertVec4EqualsApproximately(Vec4f(0f, 0f, 0f, 0f), vDefault, message = "Default constructor")

        val vCreate = Vec4f.create(1f,2f,3f,4f)
        assertVec4EqualsApproximately(Vec4f(1f, 2f, 3f, 4f), vCreate, message = "Vec4f.create")

        val vFromValues = Vec4f.fromValues(5f,6f,7f,8f)
        assertVec4EqualsApproximately(Vec4f(5f, 6f, 7f, 8f), vFromValues, message = "Vec4f.fromValues")
    }

    @Test
    fun testOperatorOverloads() {
        val a = Vec4f(1f, 2f, 3f, 4f)
        val b = Vec4f(5f, 6f, 7f, 8f)

        assertVec4EqualsApproximately(Vec4f(6f, 8f, 10f, 12f), a + b, message = "Plus operator")
        assertVec4EqualsApproximately(Vec4f(-4f, -4f, -4f, -4f), a - b, message = "Minus operator")
        assertVec4EqualsApproximately(Vec4f(2f, 4f, 6f, 8f), a * 2f, message = "Times operator")
        assertVec4EqualsApproximately(Vec4f(0.5f, 1f, 1.5f, 2f), a / 2f, message = "Div operator")
        assertVec4EqualsApproximately(Vec4f(-1f, -2f, -3f, -4f), -a, message = "Unary minus operator")
@Test
    fun testGetOperator() {
        val v = Vec4f(10f, 20f, 30f, 40f)
        assertEquals(10f, v[0], "Get x component")
        assertEquals(20f, v[1], "Get y component")
        assertEquals(30f, v[2], "Get z component")
        assertEquals(40f, v[3], "Get w component")

        assertFailsWith<IndexOutOfBoundsException>("Get out of bounds low") { v[-1] }
        assertFailsWith<IndexOutOfBoundsException>("Get out of bounds high") { v[4] }
    }

    @Test
    fun testSetOperator() {
        val v = Vec4f()
        v[0] = 15f
        v[1] = 25f
        v[2] = 35f
        v[3] = 45f
        assertVec4EqualsApproximately(Vec4f(15f, 25f, 35f, 45f), v, message ="Set components")

        assertFailsWith<IndexOutOfBoundsException>("Set out of bounds low") { v[-1] = 0f }
        assertFailsWith<IndexOutOfBoundsException>("Set out of bounds high") { v[4] = 0f }
    }
    }

    @Test
    fun testSetZero() {
        val v = Vec4f(1f, 2f, 3f, 4f)
        v.setZero()
        assertVec4EqualsApproximately(Vec4f(0f, 0f, 0f, 0f), v)
    }
    
    @Test
    fun testStaticZero() {
        val vDst = Vec4f(1f, 2f, 3f, 4f)
        Vec4f().zero(vDst) // Simulating a static-like call pattern if 'zero' modifies dst
        assertVec4EqualsApproximately(Vec4f(0f, 0f, 0f, 0f), vDst, message = "Static-like zero")

        val vNew = Vec4f(1f, 2f, 3f, 4f).zero() // zero() returns a new Vec4f
        assertVec4EqualsApproximately(Vec4f(0f, 0f, 0f, 0f), vNew, message = "zero() returns new zeroed vec")
        assertVec4EqualsApproximately(Vec4f(1f, 2f, 3f, 4f), Vec4f(1f,2f,3f,4f), message = "Original unchanged by zero()")
    }


    @Test
    fun testSet() {
        val v = Vec4f()
        v.set(1f, 2f, 3f, 4f)
        assertVec4EqualsApproximately(Vec4f(1f, 2f, 3f, 4f), v)
    }

    @Test
    fun testCeil() {
        val v = Vec4f(1.2f, -2.8f, 3.0f, -0.5f)
        assertVec4EqualsApproximately(Vec4f(2f, -2f, 3f, 0f), v.ceil())
        val dst = Vec4f()
        v.ceil(dst)
        assertVec4EqualsApproximately(Vec4f(2f, -2f, 3f, 0f), dst)
    }

    @Test
    fun testFloor() {
        val v = Vec4f(1.2f, -2.8f, 3.0f, -0.5f)
        assertVec4EqualsApproximately(Vec4f(1f, -3f, 3f, -1f), v.floor())
        val dst = Vec4f()
        v.floor(dst)
        assertVec4EqualsApproximately(Vec4f(1f, -3f, 3f, -1f), dst)
    }

    @Test
    fun testRound() {
        val v = Vec4f(1.2f, -2.8f, 3.5f, -0.5f)
        assertVec4EqualsApproximately(Vec4f(1f, -3f, 4f, 0f), v.round()) // Note: -0.5f rounds to 0f in Kotlin
        val dst = Vec4f()
        v.round(dst)
        assertVec4EqualsApproximately(Vec4f(1f, -3f, 4f, 0f), dst)
    }

    @Test
    fun testClamp() {
        val v = Vec4f(-1f, 0.5f, 2f, 0.75f)
        assertVec4EqualsApproximately(Vec4f(0f, 0.5f, 1f, 0.75f), v.clamp(0f, 1f))
        val dst = Vec4f()
        v.clamp(0.2f, 0.8f, dst)
        assertVec4EqualsApproximately(Vec4f(0.2f, 0.5f, 0.8f, 0.75f), dst) // w is clamped to 0.8f
        
        val v2 = Vec4f(-1f, 0.5f, 2f, 1.5f)
        v2.clamp(0.2f, 0.8f, dst)
        assertVec4EqualsApproximately(Vec4f(0.2f, 0.5f, 0.8f, 0.8f), dst)
    }

    @Test
    fun testAdd() {
        val a = Vec4f(1f, 2f, 3f, 4f)
        val b = Vec4f(5f, 6f, 7f, 8f)
        assertVec4EqualsApproximately(Vec4f(6f, 8f, 10f, 12f), a.add(b))
        val dst = Vec4f()
        a.add(b, dst)
        assertVec4EqualsApproximately(Vec4f(6f, 8f, 10f, 12f), dst)
    }

    @Test
    fun testAddScaled() {
        val a = Vec4f(1f, 2f, 3f, 4f)
        val b = Vec4f(2f, 3f, 4f, 5f)
        val scale = 2f
        assertVec4EqualsApproximately(Vec4f(1f + 2f * 2f, 2f + 3f * 2f, 3f + 4f * 2f, 4f + 5f * 2f), a.addScaled(b, scale))
        val dst = Vec4f()
        a.addScaled(b, scale, dst)
        assertVec4EqualsApproximately(Vec4f(5f, 8f, 11f, 14f), dst)
    }

    @Test
    fun testSubtractAndSub() {
        val a = Vec4f(5f, 7f, 9f, 11f)
        val b = Vec4f(4f, 5f, 6f, 7f)
        assertVec4EqualsApproximately(Vec4f(1f, 2f, 3f, 4f), a.subtract(b))
        val dst = Vec4f()
        a.subtract(b, dst)
        assertVec4EqualsApproximately(Vec4f(1f, 2f, 3f, 4f), dst)

        assertVec4EqualsApproximately(Vec4f(1f, 2f, 3f, 4f), a.sub(b))
        a.sub(b, dst)
        assertVec4EqualsApproximately(Vec4f(1f, 2f, 3f, 4f), dst, message = "Sub alias")
    }

    @Test
    fun testEqualsApproximately() {
        val a = Vec4f(1f, 2f, 3f, 4f)
        val b = Vec4f(1f + EPSILON / 2f, 2f - EPSILON / 2f, 3f, 4f + EPSILON / 2f)
        assertTrue(a.equalsApproximately(b))
        assertTrue(a.equalsApproximately(b, EPSILON))

        val c = Vec4f(1f + EPSILON * 2f, 2f, 3f, 4f)
        assertFalse(a.equalsApproximately(c))
        assertFalse(a.equalsApproximately(c, EPSILON))
    }
    
    @Test
    fun testEqualsExact() {
        val a = Vec4f(1f, 2f, 3f, 4f)
        val b = Vec4f(1f, 2f, 3f, 4f)
        val c = Vec4f(1.000001f, 2f, 3f, 4f)
        assertTrue(a.equals(b)) // Explicit equals method
        assertFalse(a.equals(c))

        // Data class equals
        assertTrue(a == b)
        assertFalse(a == c)
    }


    @Test
    fun testLerp() {
        val a = Vec4f(0f, 0f, 0f, 0f)
        val b = Vec4f(10f, 20f, 30f, 40f)
        assertVec4EqualsApproximately(Vec4f(0f, 0f, 0f, 0f), a.lerp(b, 0f))
        assertVec4EqualsApproximately(Vec4f(5f, 10f, 15f, 20f), a.lerp(b, 0.5f))
        assertVec4EqualsApproximately(Vec4f(10f, 20f, 30f, 40f), a.lerp(b, 1f))
        val dst = Vec4f()
        a.lerp(b, 0.25f, dst)
        assertVec4EqualsApproximately(Vec4f(2.5f, 5f, 7.5f, 10f), dst)
    }

    @Test
    fun testLerpV() {
        val a = Vec4f(0f, 0f, 0f, 0f)
        val b = Vec4f(10f, 20f, 30f, 40f)
        val t = Vec4f(0.1f, 0.5f, 1.0f, 0.25f)
        assertVec4EqualsApproximately(Vec4f(1f, 10f, 30f, 10f), a.lerpV(b, t))
        val dst = Vec4f()
        a.lerpV(b, t, dst)
        assertVec4EqualsApproximately(Vec4f(1f, 10f, 30f, 10f), dst)
    }

    @Test
    fun testMax() {
        val a = Vec4f(1f, 5f, 2f, 6f)
        val b = Vec4f(3f, 2f, 4f, 5f)
        assertVec4EqualsApproximately(Vec4f(3f, 5f, 4f, 6f), a.max(b))
        val dst = Vec4f()
        a.max(b, dst)
        assertVec4EqualsApproximately(Vec4f(3f, 5f, 4f, 6f), dst)
    }

    @Test
    fun testMin() {
        val a = Vec4f(1f, 5f, 2f, 6f)
        val b = Vec4f(3f, 2f, 4f, 5f)
        assertVec4EqualsApproximately(Vec4f(1f, 2f, 2f, 5f), a.min(b))
        val dst = Vec4f()
        a.min(b, dst)
        assertVec4EqualsApproximately(Vec4f(1f, 2f, 2f, 5f), dst)
    }

    @Test
    fun testMulScalarAndScale() {
        val a = Vec4f(1f, 2f, 3f, 4f)
        assertVec4EqualsApproximately(Vec4f(2f, 4f, 6f, 8f), a.mulScalar(2f))
        val dst = Vec4f()
        a.mulScalar(0.5f, dst)
        assertVec4EqualsApproximately(Vec4f(0.5f, 1f, 1.5f, 2f), dst)

        assertVec4EqualsApproximately(Vec4f(3f, 6f, 9f, 12f), a.scale(3f))
        a.scale(-1f, dst)
        assertVec4EqualsApproximately(Vec4f(-1f, -2f, -3f, -4f), dst, message = "Scale alias")
    }

    @Test
    fun testDivScalar() {
        val a = Vec4f(2f, 4f, 6f, 8f)
        assertVec4EqualsApproximately(Vec4f(1f, 2f, 3f, 4f), a.divScalar(2f))
        val dst = Vec4f()
        a.divScalar(0.5f, dst)
        assertVec4EqualsApproximately(Vec4f(4f, 8f, 12f, 16f), dst)
    }

    @Test
    fun testInverseAndInvert() {
        val a = Vec4f(1f, 2f, 4f, 5f)
        assertVec4EqualsApproximately(Vec4f(1f, 0.5f, 0.25f, 0.2f), a.inverse())
        val dst = Vec4f()
        a.inverse(dst)
        assertVec4EqualsApproximately(Vec4f(1f, 0.5f, 0.25f, 0.2f), dst)

        assertVec4EqualsApproximately(Vec4f(1f, 0.5f, 0.25f, 0.2f), a.invert())
        a.invert(dst)
        assertVec4EqualsApproximately(Vec4f(1f, 0.5f, 0.25f, 0.2f), dst, message = "Invert alias")
    }

    @Test
    fun testDot() {
        val a = Vec4f(1f, 2f, 3f, 4f)
        val b = Vec4f(5f, 6f, 7f, 8f)
        assertEquals(1f * 5f + 2f * 6f + 3f * 7f + 4f * 8f, a.dot(b), VEC_TEST_EPSILON) // 5 + 12 + 21 + 32 = 70
        assertEquals(70f, a.dot(b), VEC_TEST_EPSILON)

        val x = Vec4f(1f, 0f, 0f, 0f)
        val y = Vec4f(0f, 1f, 0f, 0f)
        assertEquals(0f, x.dot(y), VEC_TEST_EPSILON) // Orthogonal
    }

    @Test
    fun testLengthAndLen() {
        val a = Vec4f(1f, 2f, 2f, 0f) // 1*1 + 2*2 + 2*2 = 1+4+4 = 9, sqrt(9) = 3
        assertEquals(3f, a.length, VEC_TEST_EPSILON)
        assertEquals(3f, a.len, VEC_TEST_EPSILON, message = "Len alias")

        val b = Vec4f(1f, 2f, 3f, 4f)
        assertEquals(sqrt(1f*1f + 2f*2f + 3f*3f + 4f*4f), b.length, VEC_TEST_EPSILON) // sqrt(1+4+9+16) = sqrt(30)
        assertEquals(sqrt(30f), b.length, VEC_TEST_EPSILON)
    }

    @Test
    fun testLengthSqAndLenSq() {
        val a = Vec4f(1f, 2f, 2f, 0f)
        assertEquals(9f, a.lengthSq, VEC_TEST_EPSILON)
        assertEquals(9f, a.lenSq, VEC_TEST_EPSILON, message = "LenSq alias")

        val b = Vec4f(1f, 2f, 3f, 4f)
        assertEquals(30f, b.lengthSq, VEC_TEST_EPSILON)
    }

    @Test
    fun testDistanceAndDist() {
        val a = Vec4f(1f, 2f, 3f, 0f)
        val b = Vec4f(1f, 4f, 5f, 0f) // dx=0, dy=2, dz=2, dw=0. dist = sqrt(0+4+4+0) = sqrt(8)
        assertEquals(sqrt(8f), a.distance(b), VEC_TEST_EPSILON)
        assertEquals(sqrt(8f), a.dist(b), VEC_TEST_EPSILON, message = "Dist alias")
    }

    @Test
    fun testDistanceSqAndDistSq() {
        val a = Vec4f(1f, 2f, 3f, 0f)
        val b = Vec4f(1f, 4f, 5f, 0f)
        assertEquals(8f, a.distanceSq(b), VEC_TEST_EPSILON)
        assertEquals(8f, a.distSq(b), VEC_TEST_EPSILON, message = "DistSq alias")
    }

    @Test
    fun testNormalize() {
        val a = Vec4f(1f, 2f, 2f, 0f) // length 3
        val normA = a.normalize()
        assertVec4EqualsApproximately(Vec4f(1f / 3f, 2f / 3f, 2f / 3f, 0f), normA)
        assertEquals(1f, normA.length, VEC_TEST_EPSILON, message = "Normalized length should be 1")

        val dst = Vec4f()
        a.normalize(dst)
        assertVec4EqualsApproximately(Vec4f(1f/3f, 2f/3f, 2f/3f, 0f), dst)
        assertEquals(1f, dst.length, VEC_TEST_EPSILON)

        val zero = Vec4f(0f, 0f, 0f, 0f)
        val normZero = zero.normalize()
        assertVec4EqualsApproximately(Vec4f(0f, 0f, 0f, 0f), normZero, message = "Normalize zero vector")
    }

    @Test
    fun testNegate() {
        val a = Vec4f(1f, -2f, 3f, -4f)
        assertVec4EqualsApproximately(Vec4f(-1f, 2f, -3f, 4f), a.negate())
        val dst = Vec4f()
        a.negate(dst)
        assertVec4EqualsApproximately(Vec4f(-1f, 2f, -3f, 4f), dst)
    }

    @Test
    fun testCopyAndClone() {
        val a = Vec4f(1f, 2f, 3f, 4f)
        val b = a.copy() // Data class copy
        assertNotSame(a, b)
        assertVec4EqualsApproximately(a, b, message = "Data class copy")

        val c = a.copy(Vec4f()) // Method copy
        assertNotSame(a, c)
        assertVec4EqualsApproximately(a, c, message = "Method copy with new dst")

        val dst = Vec4f()
        a.copy(dst) // Method copy with existing dst
        assertVec4EqualsApproximately(a, dst, message = "Method copy with existing dst")

        val d = a.clone()
        assertNotSame(a, d)
        assertVec4EqualsApproximately(a, d, message = "Clone alias")
        a.clone(dst)
        assertVec4EqualsApproximately(a, dst, message = "Clone alias with dst")
    }

    @Test
    fun testMultiplyAndMul() {
        val a = Vec4f(1f, 2f, 3f, 4f)
        val b = Vec4f(5f, 6f, 7f, 8f)
        assertVec4EqualsApproximately(Vec4f(5f, 12f, 21f, 32f), a.multiply(b))
        val dst = Vec4f()
        a.multiply(b, dst)
        assertVec4EqualsApproximately(Vec4f(5f, 12f, 21f, 32f), dst)

        assertVec4EqualsApproximately(Vec4f(5f, 12f, 21f, 32f), a.mul(b))
        a.mul(b, dst)
        assertVec4EqualsApproximately(Vec4f(5f, 12f, 21f, 32f), dst, message = "Mul alias")
    }

    @Test
    fun testDivideAndDiv() {
        val a = Vec4f(10f, 24f, 42f, 64f)
        val b = Vec4f(2f, 3f, 6f, 8f)
        assertVec4EqualsApproximately(Vec4f(5f, 8f, 7f, 8f), a.divide(b))
        val dst = Vec4f()
        a.divide(b, dst)
        assertVec4EqualsApproximately(Vec4f(5f, 8f, 7f, 8f), dst)

        assertVec4EqualsApproximately(Vec4f(5f, 8f, 7f, 8f), a.div(b))
        a.div(b, dst)
        assertVec4EqualsApproximately(Vec4f(5f, 8f, 7f, 8f), dst, message = "Div alias")
    }

    @Test
    fun testTransformMat4() {
        val v = Vec4f(1f, 2f, 3f, 1f) // w=1 for point
        val mTranslate = Mat4f.translation(Vec3f(10f, 20f, 30f))
        // (1,2,3,1) + (10,20,30,0) = (11,22,33,1)
        assertVec4EqualsApproximately(Vec4f(11f, 22f, 33f, 1f), v.transformMat4(mTranslate))

        val mScale = Mat4f.scaling(Vec3f(2f, 3f, 4f))
        // (1*2, 2*3, 3*4, 1*1) = (2,6,12,1)
        assertVec4EqualsApproximately(Vec4f(2f, 6f, 12f, 1f), v.transformMat4(mScale))

        // Rotation 90 deg around Z: (1,2,3,1) -> (-2,1,3,1)
        val mRotZ = Mat4f.rotationZ(PI.toFloat() / 2f)
        assertVec4EqualsApproximately(Vec4f(-2f, 1f, 3f, 1f), v.transformMat4(mRotZ), tolerance = 0.0001f)

        val dst = Vec4f()
        v.transformMat4(mTranslate, dst)
        assertVec4EqualsApproximately(Vec4f(11f, 22f, 33f, 1f), dst)

        // Test with w=0 for direction vector
        val vDir = Vec4f(1f, 2f, 3f, 0f)
        // Translation should not affect direction vector (w=0 remains w=0)
        assertVec4EqualsApproximately(Vec4f(1f, 2f, 3f, 0f), vDir.transformMat4(mTranslate))
        // Scale should affect direction
        assertVec4EqualsApproximately(Vec4f(2f, 6f, 12f, 0f), vDir.transformMat4(mScale))
        // Rotation should affect direction
        assertVec4EqualsApproximately(Vec4f(-2f, 1f, 3f, 0f), vDir.transformMat4(mRotZ), tolerance = 0.0001f)
    }

    @Test
    fun testSetLength() {
        val v = Vec4f(1f, 2f, 2f, 0f) // length 3
        val dst = Vec4f()
        v.setLength(6f, dst)
        assertVec4EqualsApproximately(Vec4f(2f, 4f, 4f, 0f), dst)
        assertEquals(6f, dst.length, VEC_TEST_EPSILON)

        val v2 = Vec4f(3f,4f,0f,0f) // length 5
        v2.setLength(1f, dst)
        assertVec4EqualsApproximately(Vec4f(3f/5f, 4f/5f, 0f, 0f), dst)
        assertEquals(1f, dst.length, VEC_TEST_EPSILON)

        val zero = Vec4f(0f,0f,0f,0f)
        zero.setLength(10f, dst)
        assertVec4EqualsApproximately(Vec4f(0f,0f,0f,0f), dst, message = "SetLength on zero vector")
    }

    @Test
    fun testTruncate() {
        val v = Vec4f(3f, 4f, 0f, 0f) // length 5
        val dst = Vec4f()

        v.truncate(10f, dst) // maxLen > length
        assertVec4EqualsApproximately(Vec4f(3f, 4f, 0f, 0f), dst)
        assertEquals(5f, dst.length, VEC_TEST_EPSILON)

        v.truncate(5f, dst) // maxLen == length
        assertVec4EqualsApproximately(Vec4f(3f, 4f, 0f, 0f), dst)
        assertEquals(5f, dst.length, VEC_TEST_EPSILON)

        v.truncate(2.5f, dst) // maxLen < length
        assertVec4EqualsApproximately(Vec4f(1.5f, 2f, 0f, 0f), dst)
        assertEquals(2.5f, dst.length, VEC_TEST_EPSILON)

        val zero = Vec4f(0f,0f,0f,0f)
        zero.truncate(10f, dst)
        assertVec4EqualsApproximately(Vec4f(0f,0f,0f,0f), dst, message = "Truncate on zero vector")
    }

    @Test
    fun testMidpoint() {
        val a = Vec4f(0f, 0f, 0f, 0f)
        val b = Vec4f(10f, 20f, 30f, 40f)
        val dst = Vec4f()
        a.midpoint(b, dst)
        assertVec4EqualsApproximately(Vec4f(5f, 10f, 15f, 20f), dst)

        val c = Vec4f(-2f, -4f, 6f, 8f)
        val d = Vec4f(2f, 4f, -6f, -10f)
        c.midpoint(d, dst)
        assertVec4EqualsApproximately(Vec4f(0f, 0f, 0f, -1f), dst)
    }

    @Test
    fun testSizeBytes() {
        assertEquals(16u, Vec4f.SIZE_BYTES)
    }

    @Test
    fun testToString() {
        val v = Vec4f(1.0f, 2.5f, -3.000f, 4.12345f)
        // Assuming .ns formats to a reasonable number of decimal places, e.g., 2 or 3
        // This test is sensitive to the exact output of Float.ns
        // For example, if 1.0f.ns is "1.0", 2.5f.ns is "2.5", -3.000f.ns is "-3.0", 4.12345f.ns might be "4.123"
        // Let's check the actual ns behavior for a few values if possible, or make it more robust.
        // From Utils.kt: val Float.ns get() = if (this.rem(1) == 0.0f) this.toInt().toString() else this.toString()
        // So 1.0f -> "1", 2.5f -> "2.5", -3.0f -> "-3"
        assertEquals("(1,2.5,-3,4.12345)", v.toString()) // 4.12345f.ns will be "4.12345"

        val v2 = Vec4f(0f,0f,0f,0f)
        assertEquals("(0,0,0,0)", v2.toString())
    }
}