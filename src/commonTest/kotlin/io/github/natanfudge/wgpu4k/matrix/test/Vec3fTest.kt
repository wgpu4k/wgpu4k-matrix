package io.github.natanfudge.wgpu4k.matrix.test

import io.github.natanfudge.wgpu4k.matrix.*
import kotlin.math.*
import kotlin.random.Random
import kotlin.test.*

// Helper for comparing floats with a tolerance
private const val VEC_TEST_EPSILON: Float = 0.00001f // Adjusted epsilon for Vec3f tests





class Vec3fTest {

    @Test
    fun testConstructorsAndGetSet() {
        val v1 = Vec3f(1f, 2f, 3f)
        assertEquals(1f, v1.x)
        assertEquals(2f, v1.y)
        assertEquals(3f, v1.z)

        assertEquals(1f, v1[0])
        assertEquals(2f, v1[1])
        assertEquals(3f, v1[2])
        assertFailsWith<IndexOutOfBoundsException> { v1[3] }
        assertFailsWith<IndexOutOfBoundsException> { v1[-1] }

        v1[0] = 4f
        v1[1] = 5f
        v1[2] = 6f
        assertEquals(4f, v1.x)
        assertEquals(5f, v1.y)
        assertEquals(6f, v1.z)

        assertFailsWith<IndexOutOfBoundsException> { v1[3] = 7f }

        val vDefault = Vec3f()
        assertVec3EqualsApproximately(Vec3f(0f, 0f, 0f), vDefault, message = "Default constructor")
    }

    @Test
    fun testOperatorOverloads() {
        val a = Vec3f(1f, 2f, 3f)
        val b = Vec3f(4f, 5f, 6f)

        assertVec3EqualsApproximately(Vec3f(5f, 7f, 9f), a + b, message = "Plus operator")
        assertVec3EqualsApproximately(Vec3f(-3f, -3f, -3f), a - b, message = "Minus operator")
        assertVec3EqualsApproximately(Vec3f(2f, 4f, 6f), a * 2f, message = "Times operator")
        assertVec3EqualsApproximately(Vec3f(0.5f, 1f, 1.5f), a / 2f, message = "Div operator")
        assertVec3EqualsApproximately(Vec3f(-1f, -2f, -3f), -a, message = "Unary minus operator")
    }

    @Test
    fun testSetZero() {
        val v = Vec3f(1f, 2f, 3f)
        v.setZero()
        assertVec3EqualsApproximately(Vec3f(0f, 0f, 0f), v)

        val vStatic = Vec3f(1f, 2f, 3f)
        Vec3f.zero(vStatic)
        assertVec3EqualsApproximately(Vec3f(0f, 0f, 0f), vStatic, message = "Static zero")
    }

    @Test
    fun testAbsoluteValue() {
        val v = Vec3f(-1f, 2f, -3f)
        assertVec3EqualsApproximately(Vec3f(1f, 2f, 3f), v.absoluteValue())
        val dst = Vec3f()
        v.absoluteValue(dst)
        assertVec3EqualsApproximately(Vec3f(1f, 2f, 3f), dst)
    }

    @Test
    fun testCeil() {
        val v = Vec3f(1.2f, -2.8f, 3.0f)
        assertVec3EqualsApproximately(Vec3f(2f, -2f, 3f), v.ceil())
        val dst = Vec3f()
        v.ceil(dst)
        assertVec3EqualsApproximately(Vec3f(2f, -2f, 3f), dst)
    }

    @Test
    fun testFloor() {
        val v = Vec3f(1.2f, -2.8f, 3.0f)
        assertVec3EqualsApproximately(Vec3f(1f, -3f, 3f), v.floor())
        val dst = Vec3f()
        v.floor(dst)
        assertVec3EqualsApproximately(Vec3f(1f, -3f, 3f), dst)
    }

    @Test
    fun testRound() {
        val v = Vec3f(1.2f, -2.8f, 3.5f)
        assertVec3EqualsApproximately(Vec3f(1f, -3f, 4f), v.round())
        val dst = Vec3f()
        v.round(dst)
        assertVec3EqualsApproximately(Vec3f(1f, -3f, 4f), dst)
    }

    @Test
    fun testClamp() {
        val v = Vec3f(-1f, 0.5f, 2f)
        assertVec3EqualsApproximately(Vec3f(0f, 0.5f, 1f), v.clamp(0f, 1f))
        val dst = Vec3f()
        v.clamp(0.2f, 0.8f, dst)
        assertVec3EqualsApproximately(Vec3f(0.2f, 0.5f, 0.8f), dst)
    }

    @Test
    fun testAdd() {
        val a = Vec3f(1f, 2f, 3f)
        val b = Vec3f(4f, 5f, 6f)
        assertVec3EqualsApproximately(Vec3f(5f, 7f, 9f), a.add(b))
        val dst = Vec3f()
        a.add(b, dst)
        assertVec3EqualsApproximately(Vec3f(5f, 7f, 9f), dst)
    }

    @Test
    fun testAddScaled() {
        val a = Vec3f(1f, 2f, 3f)
        val b = Vec3f(2f, 3f, 4f)
        val scale = 2f
        assertVec3EqualsApproximately(Vec3f(1f + 2f * 2f, 2f + 3f * 2f, 3f + 4f * 2f), a.addScaled(b, scale))
        val dst = Vec3f()
        a.addScaled(b, scale, dst)
        assertVec3EqualsApproximately(Vec3f(5f, 8f, 11f), dst)
    }

    @Test
    fun testAngle() {
        val v1 = Vec3f(1f, 0f, 0f)
        val v2 = Vec3f(0f, 1f, 0f)
        assertEquals(PI.toFloat() / 2f, v1.angle(v2), VEC_TEST_EPSILON)

        val v3 = Vec3f(1f, 1f, 0f)
        assertEquals(PI.toFloat() / 4f, v1.angle(v3), VEC_TEST_EPSILON)

        val v4 = Vec3f(-1f, 0f, 0f)
        assertEquals(PI.toFloat(), v1.angle(v4), VEC_TEST_EPSILON)

        val v5 = Vec3f(2f, 0f, 0f) // Same direction, different magnitude
        assertEquals(0f, v1.angle(v5), VEC_TEST_EPSILON)


    }

    @Test
    fun testSubtractAndSub() {
        val a = Vec3f(5f, 7f, 9f)
        val b = Vec3f(4f, 5f, 6f)
        assertVec3EqualsApproximately(Vec3f(1f, 2f, 3f), a.subtract(b))
        val dst = Vec3f()
        a.subtract(b, dst)
        assertVec3EqualsApproximately(Vec3f(1f, 2f, 3f), dst)

        assertVec3EqualsApproximately(Vec3f(1f, 2f, 3f), a.sub(b))
        a.sub(b, dst)
        assertVec3EqualsApproximately(Vec3f(1f, 2f, 3f), dst, message = "Sub alias")
    }

    @Test
    fun testEqualsApproximately() {
        val a = Vec3f(1f, 2f, 3f)
        val b = Vec3f(1f + EPSILON / 2f, 2f - EPSILON / 2f, 3f)
        assertTrue(a.equalsApproximately(b))
        assertTrue(a.equalsApproximately(b, EPSILON))

        val c = Vec3f(1f + EPSILON * 2f, 2f, 3f)
        assertFalse(a.equalsApproximately(c))
        assertFalse(a.equalsApproximately(c, EPSILON))
    }

    @Test
    fun testLerp() {
        val a = Vec3f(0f, 0f, 0f)
        val b = Vec3f(10f, 20f, 30f)
        assertVec3EqualsApproximately(Vec3f(0f, 0f, 0f), a.lerp(b, 0f))
        assertVec3EqualsApproximately(Vec3f(5f, 10f, 15f), a.lerp(b, 0.5f))
        assertVec3EqualsApproximately(Vec3f(10f, 20f, 30f), a.lerp(b, 1f))
        val dst = Vec3f()
        a.lerp(b, 0.25f, dst)
        assertVec3EqualsApproximately(Vec3f(2.5f, 5f, 7.5f), dst)
    }

    @Test
    fun testLerpV() {
        val a = Vec3f(0f, 0f, 0f)
        val b = Vec3f(10f, 20f, 30f)
        val t = Vec3f(0.1f, 0.5f, 1.0f)
        assertVec3EqualsApproximately(Vec3f(1f, 10f, 30f), a.lerpV(b, t))
        val dst = Vec3f()
        a.lerpV(b, t, dst)
        assertVec3EqualsApproximately(Vec3f(1f, 10f, 30f), dst)
    }

    @Test
    fun testMax() {
        val a = Vec3f(1f, 5f, 2f)
        val b = Vec3f(3f, 2f, 4f)
        assertVec3EqualsApproximately(Vec3f(3f, 5f, 4f), a.max(b))
        val dst = Vec3f()
        a.max(b, dst)
        assertVec3EqualsApproximately(Vec3f(3f, 5f, 4f), dst)
    }

    @Test
    fun testMin() {
        val a = Vec3f(1f, 5f, 2f)
        val b = Vec3f(3f, 2f, 4f)
        assertVec3EqualsApproximately(Vec3f(1f, 2f, 2f), a.min(b))
        val dst = Vec3f()
        a.min(b, dst)
        assertVec3EqualsApproximately(Vec3f(1f, 2f, 2f), dst)
    }

    @Test
    fun testMulScalarAndScale() {
        val a = Vec3f(1f, 2f, 3f)
        assertVec3EqualsApproximately(Vec3f(2f, 4f, 6f), a.mulScalar(2f))
        val dst = Vec3f()
        a.mulScalar(0.5f, dst)
        assertVec3EqualsApproximately(Vec3f(0.5f, 1f, 1.5f), dst)

        assertVec3EqualsApproximately(Vec3f(3f, 6f, 9f), a.scale(3f))
        a.scale(-1f, dst)
        assertVec3EqualsApproximately(Vec3f(-1f, -2f, -3f), dst, message = "Scale alias")
    }

    @Test
    fun testDivScalar() {
        val a = Vec3f(2f, 4f, 6f)
        assertVec3EqualsApproximately(Vec3f(1f, 2f, 3f), a.divScalar(2f))
        val dst = Vec3f()
        a.divScalar(0.5f, dst)
        assertVec3EqualsApproximately(Vec3f(4f, 8f, 12f), dst)
    }

    @Test
    fun testInverseAndInvert() {
        val a = Vec3f(1f, 2f, 4f)
        assertVec3EqualsApproximately(Vec3f(1f, 0.5f, 0.25f), a.inverse())
        val dst = Vec3f()
        a.inverse(dst)
        assertVec3EqualsApproximately(Vec3f(1f, 0.5f, 0.25f), dst)

        assertVec3EqualsApproximately(Vec3f(1f, 0.5f, 0.25f), a.invert())
        a.invert(dst)
        assertVec3EqualsApproximately(Vec3f(1f, 0.5f, 0.25f), dst, message = "Invert alias")
    }

    @Test
    fun testCross() {
        val x = Vec3f(1f, 0f, 0f)
        val y = Vec3f(0f, 1f, 0f)
        val z = Vec3f(0f, 0f, 1f)

        assertVec3EqualsApproximately(z, x.cross(y))
        assertVec3EqualsApproximately(x, y.cross(z))
        assertVec3EqualsApproximately(y, z.cross(x))

        assertVec3EqualsApproximately(Vec3f.zero(), x.cross(x)) // Cross product with self is zero

        val a = Vec3f(1f, 2f, 3f)
        val b = Vec3f(4f, 5f, 6f)
        // (2*6 - 3*5, 3*4 - 1*6, 1*5 - 2*4) = (12-15, 12-6, 5-8) = (-3, 6, -3)
        assertVec3EqualsApproximately(Vec3f(-3f, 6f, -3f), a.cross(b))
        val dst = Vec3f()
        a.cross(b, dst)
        assertVec3EqualsApproximately(Vec3f(-3f, 6f, -3f), dst)
    }

    @Test
    fun testDot() {
        val a = Vec3f(1f, 2f, 3f)
        val b = Vec3f(4f, 5f, 6f)
        assertEquals(1f * 4f + 2f * 5f + 3f * 6f, a.dot(b), VEC_TEST_EPSILON) // 4 + 10 + 18 = 32
        assertEquals(32f, a.dot(b), VEC_TEST_EPSILON)

        val x = Vec3f(1f, 0f, 0f)
        val y = Vec3f(0f, 1f, 0f)
        assertEquals(0f, x.dot(y), VEC_TEST_EPSILON) // Orthogonal
    }

    @Test
    fun testLengthAndLen() {
        val a = Vec3f(3f, 4f, 0f) // Pythagorean triple 3-4-5
        assertEquals(5f, a.length(), VEC_TEST_EPSILON)
        assertEquals(5f, a.len(), VEC_TEST_EPSILON, message = "Len alias")

        val b = Vec3f(1f, 2f, 3f)
        assertEquals(sqrt(1f*1f + 2f*2f + 3f*3f), b.length(), VEC_TEST_EPSILON) // sqrt(1+4+9) = sqrt(14)
        assertEquals(sqrt(14f), b.length(), VEC_TEST_EPSILON)
    }

    @Test
    fun testLengthSqAndLenSq() {
        val a = Vec3f(3f, 4f, 0f)
        assertEquals(25f, a.lengthSq(), VEC_TEST_EPSILON)
        assertEquals(25f, a.lenSq(), VEC_TEST_EPSILON, message = "LenSq alias")

        val b = Vec3f(1f, 2f, 3f)
        assertEquals(14f, b.lengthSq(), VEC_TEST_EPSILON)
    }

    @Test
    fun testDistanceAndDist() {
        val a = Vec3f(1f, 2f, 3f)
        val b = Vec3f(4f, 6f, 3f) // dx=3, dy=4, dz=0. dist = 5
        assertEquals(5f, a.distance(b), VEC_TEST_EPSILON)
        assertEquals(5f, a.dist(b), VEC_TEST_EPSILON, message = "Dist alias")
    }

    @Test
    fun testDistanceSqAndDistSq() {
        val a = Vec3f(1f, 2f, 3f)
        val b = Vec3f(4f, 6f, 3f)
        assertEquals(25f, a.distanceSq(b), VEC_TEST_EPSILON)
        assertEquals(25f, a.distSq(b), VEC_TEST_EPSILON, message = "DistSq alias")
    }

    @Test
    fun testNormalize() {
        val a = Vec3f(3f, 4f, 0f)
        val normA = a.normalize()
        assertVec3EqualsApproximately(Vec3f(3f / 5f, 4f / 5f, 0f), normA)
        assertEquals(1f, normA.length(), VEC_TEST_EPSILON, message = "Normalized length should be 1")

        val dst = Vec3f()
        a.normalize(dst)
        assertVec3EqualsApproximately(Vec3f(0.6f, 0.8f, 0f), dst)
        assertEquals(1f, dst.length(), VEC_TEST_EPSILON)

        val zero = Vec3f(0f, 0f, 0f)
        val normZero = zero.normalize()
        assertVec3EqualsApproximately(Vec3f(0f, 0f, 0f), normZero, message = "Normalize zero vector")
    }

    @Test
    fun testNegate() {
        val a = Vec3f(1f, -2f, 3f)
        assertVec3EqualsApproximately(Vec3f(-1f, 2f, -3f), a.negate())
        val dst = Vec3f()
        a.negate(dst)
        assertVec3EqualsApproximately(Vec3f(-1f, 2f, -3f), dst)
    }

    @Test
    fun testCopyAndClone() {
        val a = Vec3f(1f, 2f, 3f)
        val b = a.copy()
        assertNotSame(a, b)
        assertVec3EqualsApproximately(a, b)

        val dst = Vec3f()
        a.copy(dst)
        assertVec3EqualsApproximately(a, dst)

        val c = a.clone()
        assertNotSame(a, c)
        assertVec3EqualsApproximately(a, c, message = "Clone alias")
        a.clone(dst)
        assertVec3EqualsApproximately(a, dst, message = "Clone alias with dst")
    }

    @Test
    fun testMultiplyAndMul() {
        val a = Vec3f(1f, 2f, 3f)
        val b = Vec3f(4f, 5f, 6f)
        assertVec3EqualsApproximately(Vec3f(4f, 10f, 18f), a.multiply(b))
        val dst = Vec3f()
        a.multiply(b, dst)
        assertVec3EqualsApproximately(Vec3f(4f, 10f, 18f), dst)

        assertVec3EqualsApproximately(Vec3f(4f, 10f, 18f), a.mul(b))
        a.mul(b, dst)
        assertVec3EqualsApproximately(Vec3f(4f, 10f, 18f), dst, message = "Mul alias")
    }

    @Test
    fun testDivideAndDiv() {
        val a = Vec3f(4f, 10f, 18f)
        val b = Vec3f(2f, 5f, 3f)
        assertVec3EqualsApproximately(Vec3f(2f, 2f, 6f), a.divide(b))
        val dst = Vec3f()
        a.divide(b, dst)
        assertVec3EqualsApproximately(Vec3f(2f, 2f, 6f), dst)

        assertVec3EqualsApproximately(Vec3f(2f, 2f, 6f), a.div(b))
        a.div(b, dst)
        assertVec3EqualsApproximately(Vec3f(2f, 2f, 6f), dst, message = "Div alias")
    }

    @Test
    fun testTransformMat4() {
        val v = Vec3f(1f, 2f, 3f)
        val mTranslate = Mat4f.translation(Vec3f(10f, 20f, 30f))
        assertVec3EqualsApproximately(Vec3f(11f, 22f, 33f), v.transformMat4(mTranslate))

        val mScale = Mat4f.scaling(Vec3f(2f, 3f, 4f))
        assertVec3EqualsApproximately(Vec3f(2f, 6f, 12f), v.transformMat4(mScale))

        // Rotation 90 deg around Z: (1,2,3) -> (-2,1,3)
        val mRotZ = Mat4f.rotationZ(PI.toFloat() / 2f)
        assertVec3EqualsApproximately(Vec3f(-2f, 1f, 3f), v.transformMat4(mRotZ), tolerance = 0.0001f)

        val dst = Vec3f()
        v.transformMat4(mTranslate, dst)
        assertVec3EqualsApproximately(Vec3f(11f, 22f, 33f), dst)
    }

    @Test
    fun testTransformMat4Upper3x3() {
        val vDir = Vec3f(1f, 0f, 0f) // Direction vector
        val mTranslate = Mat4f.translation(Vec3f(10f, 20f, 30f))
        // Translation should not affect direction vectors
        assertVec3EqualsApproximately(Vec3f(1f, 0f, 0f), vDir.transformMat4Upper3x3(mTranslate))

        val mScale = Mat4f.scaling(Vec3f(2f, 3f, 4f))
        assertVec3EqualsApproximately(Vec3f(2f, 0f, 0f), vDir.transformMat4Upper3x3(mScale))

        // Rotation 90 deg around Z: (1,0,0) -> (0,1,0)
        val mRotZ = Mat4f.rotationZ(PI.toFloat() / 2f)
        assertVec3EqualsApproximately(Vec3f(0f, 1f, 0f), vDir.transformMat4Upper3x3(mRotZ), tolerance = 0.0001f)

        val v = Vec3f(1f,2f,3f)
        val dst = Vec3f()
        v.transformMat4Upper3x3(mRotZ, dst)
        // (1,2,3) rotated by 90 deg Z (upper 3x3) -> (-2,1,3)
        assertVec3EqualsApproximately(Vec3f(-2f, 1f, 3f), dst, tolerance = 0.0001f)
    }

    @Test
    fun testTransformMat3() {
        val v = Vec3f(1f, 2f, 3f)
        // Rotation 90 deg around Z: (1,2,3) -> (-2,1,3)
        val mRotZ = Mat3f.rotationZ(PI.toFloat() / 2f)
        assertVec3EqualsApproximately(Vec3f(-2f, 1f, 3f), v.transformMat3(mRotZ), tolerance = 0.0001f)

        val mScale = Mat3f.scaling3D(Vec3f(2f, 3f, 1f)) // Changed to scaling3D
        assertVec3EqualsApproximately(Vec3f(2f, 6f, 3f), v.transformMat3(mScale))

        val dst = Vec3f()
        v.transformMat3(mRotZ, dst)
        assertVec3EqualsApproximately(Vec3f(-2f, 1f, 3f), dst, tolerance = 0.0001f)
    }

    @Test
    fun testTransformQuat() {
        val v = Vec3f(1f, 0f, 0f)
        // Rotate 90 deg around Y: (1,0,0) -> (0,0,-1)
        val qRotY = Quatf.fromAxisAngle(Vec3f(0f, 1f, 0f), PI.toFloat() / 2f)
        assertVec3EqualsApproximately(Vec3f(0f, 0f, -1f), v.transformQuat(qRotY), tolerance = 0.0001f)

        // Rotate 90 deg around Z: (1,0,0) -> (0,1,0)
        val qRotZ = Quatf.fromAxisAngle(Vec3f(0f, 0f, 1f), PI.toFloat() / 2f)
        assertVec3EqualsApproximately(Vec3f(0f, 1f, 0f), v.transformQuat(qRotZ), tolerance = 0.0001f)

        val v2 = Vec3f(1f,1f,0f)
        val dst = Vec3f()
        v2.transformQuat(qRotZ, dst) // (1,1,0) rotated 90 deg Z -> (-1,1,0)
        assertVec3EqualsApproximately(Vec3f(-1f, 1f, 0f), dst, tolerance = 0.0001f)
    }

    @Test
    fun testRotateX() {
        val p = Vec3f(0f, 1f, 0f)
        val center = Vec3f(0f, 0f, 0f)
        // Rotate (0,1,0) around X by 90 deg -> (0,0,1)
        assertVec3EqualsApproximately(Vec3f(0f, 0f, 1f), p.rotateX(center, PI.toFloat() / 2f), tolerance = 0.0001f)

        val p2 = Vec3f(1f, 1f, 0f)
        val center2 = Vec3f(1f, 0f, 0f) // Center of rotation is (1,0,0)
        // Point relative to center: (0,1,0)
        // Rotate (0,1,0) around X by 90 deg -> (0,0,1)
        // Add center back: (1,0,1)
        assertVec3EqualsApproximately(Vec3f(1f, 0f, 1f), p2.rotateX(center2, PI.toFloat() / 2f), tolerance = 0.0001f)
        val dst = Vec3f()
        p.rotateX(center, PI.toFloat() / 2f, dst)
        assertVec3EqualsApproximately(Vec3f(0f, 0f, 1f), dst, tolerance = 0.0001f)
    }

    @Test
    fun testRotateY() {
        val p = Vec3f(1f, 0f, 0f)
        val center = Vec3f(0f, 0f, 0f)
        // Rotate (1,0,0) around Y by 90 deg -> (0,0,-1)
        assertVec3EqualsApproximately(Vec3f(0f, 0f, -1f), p.rotateY(center, PI.toFloat() / 2f), tolerance = 0.0001f)
        val dst = Vec3f()
        p.rotateY(center, PI.toFloat() / 2f, dst)
        assertVec3EqualsApproximately(Vec3f(0f, 0f, -1f), dst, tolerance = 0.0001f)
    }

    @Test
    fun testRotateZ() {
        val p = Vec3f(1f, 0f, 0f)
        val center = Vec3f(0f, 0f, 0f)
        // Rotate (1,0,0) around Z by 90 deg -> (0,1,0)
        assertVec3EqualsApproximately(Vec3f(0f, 1f, 0f), p.rotateZ(center, PI.toFloat() / 2f), tolerance = 0.0001f)
        val dst = Vec3f()
        p.rotateZ(center, PI.toFloat() / 2f, dst)
        assertVec3EqualsApproximately(Vec3f(0f, 1f, 0f), dst, tolerance = 0.0001f)
    }

    @Test
    fun testSetLength() {
        val v = Vec3f(3f, 4f, 0f) // length 5
        assertVec3EqualsApproximately(Vec3f(6f, 8f, 0f), v.setLength(10f))
        val dst = Vec3f()
        v.setLength(2.5f, dst)
        assertVec3EqualsApproximately(Vec3f(1.5f, 2f, 0f), dst)

        val zero = Vec3f(0f,0f,0f)
        assertVec3EqualsApproximately(Vec3f(0f,0f,0f), zero.setLength(10f), message = "SetLength on zero vector")
    }

    @Test
    fun testTruncate() {
        val v = Vec3f(3f, 4f, 0f) // length 5
        assertVec3EqualsApproximately(Vec3f(1.5f, 2f, 0f), v.truncate(2.5f)) // Truncate to 2.5
        assertVec3EqualsApproximately(Vec3f(3f, 4f, 0f), v.truncate(10f)) // MaxLen > length, no change

        val dst = Vec3f()
        v.truncate(2.5f, dst)
        assertVec3EqualsApproximately(Vec3f(1.5f, 2f, 0f), dst)

        val zero = Vec3f(0f,0f,0f)
        assertVec3EqualsApproximately(Vec3f(0f,0f,0f), zero.truncate(10f), message = "Truncate on zero vector")
    }

    @Test
    fun testSetInstance() {
        val v = Vec3f()
        v.set(1f, 2f, 3f)
        assertVec3EqualsApproximately(Vec3f(1f, 2f, 3f), v)
    }

    @Test
    fun testZeroInstance() {
        val v = Vec3f(1f, 2f, 3f)
        v.zero()
        assertVec3EqualsApproximately(Vec3f(0f, 0f, 0f), v)
    }

    // Companion Object methods
    @Test
    fun testStaticSet() {
        val dst = Vec3f()
        Vec3f.set(1f, 2f, 3f, dst)
        assertVec3EqualsApproximately(Vec3f(1f, 2f, 3f), dst)
        val res = Vec3f.set(4f,5f,6f)
        assertVec3EqualsApproximately(Vec3f(4f,5f,6f), res)
    }

    @Test
    fun testStaticCreate() {
        val v = Vec3f.create(1f, 2f, 3f)
        assertVec3EqualsApproximately(Vec3f(1f, 2f, 3f), v)
        val vDefault = Vec3f.create()
        assertVec3EqualsApproximately(Vec3f(0f,0f,0f), vDefault) // Assuming create() defaults to 0,0,0
    }

    @Test
    fun testStaticRandom() {
        val r1 = Vec3f.random()
        assertTrue(r1.x >= -1f && r1.x <= 1f)
        assertTrue(r1.y >= -1f && r1.y <= 1f)
        assertTrue(r1.z >= -1f && r1.z <= 1f)
        assertTrue(r1.length() <= sqrt(3f)) // Max length if x,y,z are all 1 or -1

        val r2 = Vec3f.random(5f)
        assertTrue(r2.x >= -5f && r2.x <= 5f)
        assertTrue(r2.y >= -5f && r2.y <= 5f)
        assertTrue(r2.z >= -5f && r2.z <= 5f)

        val dst = Vec3f()
        Vec3f.random(2f, dst)
        assertTrue(dst.x >= -2f && dst.x <= 2f)
    }

    @Test
    fun testStaticZero() { // Already tested in testSetZero, but good for completeness
        val dst = Vec3f(1f,1f,1f)
        Vec3f.zero(dst)
        assertVec3EqualsApproximately(Vec3f(0f,0f,0f), dst)
        val res = Vec3f.zero()
        assertVec3EqualsApproximately(Vec3f(0f,0f,0f), res)
    }

    @Test
    fun testStaticGetTranslation() {
        val m = Mat4f.translation(Vec3f(10f, 20f, 30f))
        assertVec3EqualsApproximately(Vec3f(10f, 20f, 30f), Vec3f.getTranslation(m))
        val dst = Vec3f()
        Vec3f.getTranslation(m, dst)
        assertVec3EqualsApproximately(Vec3f(10f, 20f, 30f), dst)
    }

    @Test
    fun testStaticGetAxis() {
        val m = Mat4f.identity()
        m[0] = 1f; m[1] = 2f; m[2] = 3f; // X-axis
        m[4] = 4f; m[5] = 5f; m[6] = 6f; // Y-axis
        m[8] = 7f; m[9] = 8f; m[10] = 9f; // Z-axis

        assertVec3EqualsApproximately(Vec3f(1f, 2f, 3f), Vec3f.getAxis(m, 0))
        assertVec3EqualsApproximately(Vec3f(4f, 5f, 6f), Vec3f.getAxis(m, 1))
        assertVec3EqualsApproximately(Vec3f(7f, 8f, 9f), Vec3f.getAxis(m, 2))

        val dst = Vec3f()
        Vec3f.getAxis(m, 0, dst)
        assertVec3EqualsApproximately(Vec3f(1f, 2f, 3f), dst)
    }

    @Test
    fun testStaticGetScaling() {
        val scaleVec = Vec3f(2f, 3f, 4f)
        val m = Mat4f.scaling(scaleVec)
        assertVec3EqualsApproximately(scaleVec, Vec3f.getScaling(m))

        val mRot = Mat4f.rotationX(PI.toFloat() / 4f)
        // Changed order to Rotate then Scale (R * S)
        // So that Vec3f.getScaling, which measures column lengths,
        // will yield the original scaling factors.
        val mCombined = mRot.multiply(m) // Rotate then scale
        // getScaling should still extract the original scaling factors
        assertVec3EqualsApproximately(scaleVec, Vec3f.getScaling(mCombined), tolerance = 0.0001f)

        val dst = Vec3f()
        Vec3f.getScaling(m, dst)
        assertVec3EqualsApproximately(scaleVec, dst)
    }


    @Test
    fun testEquals() {
        val v1 = Vec3f(1f, 2f, 3f)
        val v2 = Vec3f(1f, 2f, 3f)
        val v3 = Vec3f(1f, 2f, 4f)
        val v4 = Vec3f(4f, 2f, 3f)

        assertTrue(v1.equals(v2))
        assertTrue(v2.equals(v1))
        assertFalse(v1.equals(v3))
        assertFalse(v1.equals(v4))
        assertFalse(v1.equals(null))
        assertFalse(v1.equals("Not a Vec3f"))
    }

    @Test
    fun testHashCode() {
        val v1 = Vec3f(1f, 2f, 3f)
        val v2 = Vec3f(1f, 2f, 3f)
        val v3 = Vec3f(1.000001f, 2.000001f, 3.000001f) // Close but not identical for hashcode

        assertEquals(v1.hashCode(), v2.hashCode())
        // Hashcodes for very close but not identical floats might differ, this is expected.
        // No strict requirement for v1.hashCode() != v3.hashCode() but good if they do.
    }

}