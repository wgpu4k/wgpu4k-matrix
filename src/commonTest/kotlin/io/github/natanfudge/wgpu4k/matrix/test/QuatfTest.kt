package io.github.natanfudge.wgpu4k.matrix.test

import io.github.natanfudge.wgpu4k.matrix.*
import kotlin.math.PI
import kotlin.math.sqrt
import kotlin.test.*

// Helper for comparing floats with a tolerance
private const val QUAT_TEST_EPSILON: Float = 0.001f

internal fun assertQuatEqualsApproximately(expected: Quatf, actual: Quatf, message: String? = null) {
    assertTrue(expected.equalsApproximately(actual, QUAT_TEST_EPSILON),
        (message ?: "") + "\nExpected: $expected\nActual:   $actual (Epsilon: $QUAT_TEST_EPSILON)")
}

internal fun assertQuatNotEqualsApproximately(unexpected: Quatf, actual: Quatf, message: String? = null) {
    assertFalse(unexpected.equalsApproximately(actual, QUAT_TEST_EPSILON),
        (message ?: "") + "\nUnexpected: $unexpected\nActual:     $actual (Epsilon: $QUAT_TEST_EPSILON)")
}

class QuatfTest {

    @Test
    fun testCreateAndFromValues() {
        val q1 = Quatf.create(1f, 2f, 3f, 4f)
        assertEquals(1f, q1.x)
        assertEquals(2f, q1.y)
        assertEquals(3f, q1.z)
        assertEquals(4f, q1.w)

        val q2 = Quatf.fromValues(5f, 6f, 7f, 8f)
        assertEquals(5f, q2.x)
        assertEquals(6f, q2.y)
        assertEquals(7f, q2.z)
        assertEquals(8f, q2.w)

        val qDefault = Quatf.create()
        assertQuatEqualsApproximately(Quatf(0f, 0f, 0f, 1f), qDefault, "Default create")
    }

    @Test
    fun testIdentity() {
        val q = Quatf.identity()
        assertQuatEqualsApproximately(Quatf(0f, 0f, 0f, 1f), q, "Static identity")

        val q2 = Quatf(1f, 2f, 3f, 4f)
        Quatf.identity(q2)
        assertQuatEqualsApproximately(Quatf(0f, 0f, 0f, 1f), q2, "Static identity with dst")
    }

    @Test
    fun testSetIdentity() {
        val q = Quatf(1f, 2f, 3f, 4f)
        q.setIdentity()
        assertQuatEqualsApproximately(Quatf(0f, 0f, 0f, 1f), q)
    }

    @Test
    fun testSet() {
        val q = Quatf()
        q.set(1f, 2f, 3f, 4f)
        assertQuatEqualsApproximately(Quatf(1f, 2f, 3f, 4f), q)
    }

    @Test
    fun testFromAxisAngle() {
        val axis = Vec3f(1f, 0f, 0f)
        val angle = PI.toFloat() / 2f // 90 degrees
        val q = Quatf.fromAxisAngle(axis, angle)
        // For 90 deg around X: x=sin(45)=sqrt(2)/2, y=0, z=0, w=cos(45)=sqrt(2)/2
        val expectedX = sqrt(0.5f)
        assertQuatEqualsApproximately(Quatf(expectedX, 0f, 0f, expectedX), q, "From X axis 90 deg")

        val axisY = Vec3f(0f, 1f, 0f)
        val qY = Quatf.fromAxisAngle(axisY, PI.toFloat()) // 180 degrees
        // For 180 deg around Y: x=0, y=sin(90)=1, z=0, w=cos(90)=0
        assertQuatEqualsApproximately(Quatf(0f, 1f, 0f, 0f), qY, "From Y axis 180 deg")

        val axisZ = Vec3f(0f, 0f, 1f)
        val qZ = Quatf.fromAxisAngle(axisZ, 0f)
        assertQuatEqualsApproximately(Quatf(0f, 0f, 0f, 1f), qZ, "From Z axis 0 deg (identity)")

        val axisCustom = Vec3f(1f, 1f, 1f).normalize()
        val qCustom = Quatf.fromAxisAngle(axisCustom, PI.toFloat() / 3f) // 60 degrees
        val s = kotlin.math.sin(PI.toFloat() / 6f)
        val c = kotlin.math.cos(PI.toFloat() / 6f)
        assertQuatEqualsApproximately(Quatf(axisCustom.x * s, axisCustom.y * s, axisCustom.z * s, c), qCustom, "From custom axis 60 deg")
    }

    @Test
    fun testToAxisAngle() {
        val q = Quatf(sqrt(0.5f), 0f, 0f, sqrt(0.5f)) // 90 deg around X
        val (angle, axis) = q.toAxisAngle()
        assertEquals(PI.toFloat() / 2f, angle, QUAT_TEST_EPSILON)
        assertVec3EqualsApproximately(Vec3f(1f, 0f, 0f), axis, "Axis for 90 deg X rotation")

        val qIdentity = Quatf.identity()
        val (angleIdentity, axisIdentity) = qIdentity.toAxisAngle()
        assertEquals(0f, angleIdentity, QUAT_TEST_EPSILON)
        // Axis can be arbitrary for 0 angle, often (1,0,0) by convention in implementations
        assertTrue(axisIdentity.x.isFinite() && axisIdentity.y.isFinite() && axisIdentity.z.isFinite(), "Axis for identity should be finite")


        val qY180 = Quatf(0f, 1f, 0f, 0f) // 180 deg around Y
        val (angleY, axisY) = qY180.toAxisAngle()
        assertEquals(PI.toFloat(), angleY, QUAT_TEST_EPSILON)
        assertVec3EqualsApproximately(Vec3f(0f, 1f, 0f), axisY, "Axis for 180 deg Y rotation")

        // Test with dst
        val dstAxis = Vec3f()
        val (angleDst, axisDst) = q.toAxisAngle(dstAxis)
        assertEquals(PI.toFloat() / 2f, angleDst, QUAT_TEST_EPSILON)
        assertSame(dstAxis, axisDst, "dstAxis should be the returned axis")
        assertVec3EqualsApproximately(Vec3f(1f, 0f, 0f), axisDst, "Axis for 90 deg X rotation with dst")
    }

    @Test
    fun testFromMat() {
        // Test with Mat3f
        val mat3X90 = Mat3f.rotationX(PI.toFloat() / 2f) // 90 deg around X
        val qFromMat3 = Quatf.fromMat(mat3X90)
        val expectedX90 = Quatf(sqrt(0.5f), 0f, 0f, sqrt(0.5f))
        assertQuatEqualsApproximately(expectedX90, qFromMat3.normalize(), "From Mat3 X 90 deg")

        val mat3Y180 = Mat3f.rotationY(PI.toFloat()) // 180 deg around Y
        val qFromMat3Y = Quatf.fromMat(mat3Y180)
        val expectedY180 = Quatf(0f, 1f, 0f, 0f)
        assertQuatEqualsApproximately(expectedY180, qFromMat3Y.normalize(), "From Mat3 Y 180 deg")

        // Test with Mat4f
        val mat4Z90 = Mat4f.rotationZ(PI.toFloat() / 2f) // 90 deg around Z
        val qFromMat4 = Quatf.fromMat(mat4Z90)
        val expectedZ90 = Quatf(0f, 0f, sqrt(0.5f), sqrt(0.5f))
        assertQuatEqualsApproximately(expectedZ90, qFromMat4.normalize(), "From Mat4 Z 90 deg")

        // Test identity matrix
        val mat4Identity = Mat4f.identity()
        val qFromMat4Identity = Quatf.fromMat(mat4Identity)
        assertQuatEqualsApproximately(Quatf.identity(), qFromMat4Identity.normalize(), "From Mat4 identity")

      }

    @Test
    fun complexQuatTest() {
        // More complex rotation (from glMatrix quat.fromMat3 test)
        // mat3(
        //   0.23369404077530, 0.96451095938683, 0.12425044178963,
        //  -0.94583004713058, 0.20093007385731, 0.25468680262566,
        //   0.22143001854420, -0.17101006210000, 0.95981693267822
        // )
        // results in quat(0.10206088423728943, 0.18957087397575378, 0.480040043592453, 0.8509108424186707)
        val m = Mat3f( // Directly use constructor for column-major values
            0.23369404077530f, -0.94583004713058f,  0.22143001854420f, // v0, v1, v2 (col 0)
            0.96451095938683f,  0.20093007385731f, -0.17101006210000f, // v3, v4, v5 (col 1)
            0.12425044178963f,  0.25468680262566f,  0.95981693267822f  // v6, v7, v8 (col 2)
        )
        val qComplex = Quatf.fromMat(m)
        val expectedComplex = Quatf(-0.13758372646672176f, -0.03140809673326828f, -0.61741548100955601f, 0.77387490816078441f)
        assertQuatEqualsApproximately(expectedComplex, qComplex.normalize(qComplex), "From complex Mat3") // fromMat might not produce normalized

    }

    @Test
    fun testFromEuler() {
        val halfPi = PI.toFloat() / 2f
        // XYZ order
        var q = Quatf.fromEuler(halfPi, 0f, 0f, "xyz") // 90 deg around X
        assertQuatEqualsApproximately(Quatf(sqrt(0.5f), 0f, 0f, sqrt(0.5f)), q, "Euler XYZ: 90 deg X")

        q = Quatf.fromEuler(0f, halfPi, 0f, "xyz") // 90 deg around Y
        assertQuatEqualsApproximately(Quatf(0f, sqrt(0.5f), 0f, sqrt(0.5f)), q, "Euler XYZ: 90 deg Y")

        q = Quatf.fromEuler(0f, 0f, halfPi, "xyz") // 90 deg around Z
        assertQuatEqualsApproximately(Quatf(0f, 0f, sqrt(0.5f), sqrt(0.5f)), q, "Euler XYZ: 90 deg Z")

        // ZYX order (common)
        q = Quatf.fromEuler(halfPi, 0f, 0f, "zyx") // 90 deg around X
        assertQuatEqualsApproximately(Quatf(sqrt(0.5f), 0f, 0f, sqrt(0.5f)), q, "Euler ZYX: 90 deg X")

        q = Quatf.fromEuler(0f, halfPi, 0f, "zyx") // 90 deg around Y
        assertQuatEqualsApproximately(Quatf(0f, sqrt(0.5f), 0f, sqrt(0.5f)), q, "Euler ZYX: 90 deg Y")

        // Test other orders briefly
        val sVal = sqrt(0.5f)
        val expected90X = Quatf(sVal, 0f, 0f, sVal)
        val expected90Y = Quatf(0f, sVal, 0f, sVal)
        val expected90Z = Quatf(0f, 0f, sVal, sVal)

        // Order: XZY
        q = Quatf.fromEuler(halfPi, 0f, 0f, "xzy")
        assertQuatEqualsApproximately(expected90X, q, "Euler XZY: 90 deg X")
        q = Quatf.fromEuler(0f, halfPi, 0f, "xzy")
        assertQuatEqualsApproximately(expected90Y, q, "Euler XZY: 90 deg Y")
        q = Quatf.fromEuler(0f, 0f, halfPi, "xzy")
        assertQuatEqualsApproximately(expected90Z, q, "Euler XZY: 90 deg Z")

        // Order: YXZ
        q = Quatf.fromEuler(halfPi, 0f, 0f, "yxz")
        assertQuatEqualsApproximately(expected90X, q, "Euler YXZ: 90 deg X")
        q = Quatf.fromEuler(0f, halfPi, 0f, "yxz")
        assertQuatEqualsApproximately(expected90Y, q, "Euler YXZ: 90 deg Y")
        q = Quatf.fromEuler(0f, 0f, halfPi, "yxz")
        assertQuatEqualsApproximately(expected90Z, q, "Euler YXZ: 90 deg Z")

        // Order: YZX
        q = Quatf.fromEuler(halfPi, 0f, 0f, "yzx")
        assertQuatEqualsApproximately(expected90X, q, "Euler YZX: 90 deg X")
        q = Quatf.fromEuler(0f, halfPi, 0f, "yzx")
        assertQuatEqualsApproximately(expected90Y, q, "Euler YZX: 90 deg Y")
        q = Quatf.fromEuler(0f, 0f, halfPi, "yzx")
        assertQuatEqualsApproximately(expected90Z, q, "Euler YZX: 90 deg Z")

        // Order: ZXY
        q = Quatf.fromEuler(halfPi, 0f, 0f, "zxy")
        assertQuatEqualsApproximately(expected90X, q, "Euler ZXY: 90 deg X")
        q = Quatf.fromEuler(0f, halfPi, 0f, "zxy")
        assertQuatEqualsApproximately(expected90Y, q, "Euler ZXY: 90 deg Y")
        q = Quatf.fromEuler(0f, 0f, halfPi, "zxy")
        assertQuatEqualsApproximately(expected90Z, q, "Euler ZXY: 90 deg Z")


        assertFailsWith<Error> { Quatf.fromEuler(0f,0f,0f, "unknown") }
    }

    @Test
    fun testCombinedRotation() {
        val halfPi = PI.toFloat() / 2f
        val qCombined = Quatf.fromEuler(halfPi, halfPi, 0f, "zyx")
        assertQuatEqualsApproximately(Quatf(0.5f, 0.5f, -0.5f, 0.5f), qCombined, "Euler ZYX: 90 deg X then 90 deg Y")
    }

    @Test
    fun testRotationToUnsafe() {
        val v1 = Vec3f(1f, 0f, 0f)
        val v2 = Vec3f(0f, 1f, 0f)
        // Rotation from +X to +Y is 90 deg around +Z
        val q = Quatf.rotationToUnsafe(v1, v2)
        val expected = Quatf.fromAxisAngle(Vec3f(0f, 0f, 1f), PI.toFloat() / 2f)
        assertQuatEqualsApproximately(expected, q.normalize(), "Rotation +X to +Y") // rotationTo might not be normalized

        val v3 = Vec3f(1f, 0f, 0f)
        val v4 = Vec3f(-1f, 0f, 0f)
        // Rotation from +X to -X is 180 deg around any axis orthogonal to X (e.g., Y or Z)
        val q2 = Quatf.rotationToUnsafe(v3, v4)
        // One possible result: 180 deg around Y -> (0,1,0,0)
        // Or 180 deg around Z -> (0,0,1,0)
        // The method picks one, let's check if it's a 180 deg rotation
        val (angle, axis) = q2.toAxisAngle()
        assertEquals(PI.toFloat(), angle, QUAT_TEST_EPSILON, "Angle for opposite vectors should be PI")
        assertEquals(0f, axis.dot(v3), QUAT_TEST_EPSILON, "Rotation axis should be orthogonal to v3")


        val v5 = Vec3f(1f, 2f, 3f).normalize()
        val q3 = Quatf.rotationToUnsafe(v5, v5)
        assertQuatEqualsApproximately(Quatf.identity(), q3, "Rotation from vector to itself")

        // Test opposite vectors where default cross product (with X_UNIT) is zero
        val vX = Vec3f(1f,0f,0f)
        val vNegX = Vec3f(-1f,0f,0f)
        val qXtoNegX = Quatf.rotationToUnsafe(vX, vNegX)
        val (angleX, axisX) = qXtoNegX.toAxisAngle()
        assertEquals(PI.toFloat(), angleX, QUAT_TEST_EPSILON)
        // axis should be orthogonal to X, e.g. (0,1,0) or (0,0,1)
        assertEquals(0f, axisX.dot(vX), QUAT_TEST_EPSILON)
        assertTrue(axisX.y != 0f || axisX.z != 0f, "Axis for X to -X should be in YZ plane")
    }

    @Test
    fun testSqlerpUnsafe() {
        val q1 = Quatf.identity()
        val q2 = Quatf.fromAxisAngle(Vec3f(1f,0f,0f), PI.toFloat() / 2f) // 90 deg X
        val q3 = Quatf.fromAxisAngle(Vec3f(0f,1f,0f), PI.toFloat() / 2f) // 90 deg Y
        val q4 = Quatf.fromAxisAngle(Vec3f(0f,0f,1f), PI.toFloat() / 2f) // 90 deg Z

        // t = 0 should be slerp(q1, q2, 0) = q1
        var res = Quatf.sqlerpUnsafe(q1, q2, q3, q4, 0f)
        assertQuatEqualsApproximately(q1.slerp(q2, 0f), res, "Sqlerp t=0")

        // t = 1 should be slerp(q2, q3, 1) = q3 (this is based on how three.js does it, formula is Slerp(Slerp(a,d,t), Slerp(b,c,t), u))
        // The formula given in Quatf.kt is: Slerp(Slerp(a, d, t), Slerp(b, c, t), 2t(1-t))
        // If t=0, u=0. Slerp(Slerp(a,d,0), Slerp(b,c,0), 0) = Slerp(a,b,0) = a
        // If t=1, u=0. Slerp(Slerp(a,d,1), Slerp(b,c,1), 0) = Slerp(d,c,0) = d
        // This seems to match the comment in three.js:
        //   interpolates between q1 and q2, using q_aux1 and q_aux2 as intermediate controls
        //   q(t) = Slerp(Slerp(q1, q_aux2, t), Slerp(q_aux1, q2, t), 2t(1-t))
        // Let's use the formula from the code: q(t) = Slerp(Slerp(a, d, t), Slerp(b, c, t), 2t(1-t))
        // For t=0: Slerp(Slerp(a,d,0), Slerp(b,c,0), 0) = Slerp(a,b,0) = a.
        res = Quatf.sqlerpUnsafe(q1, q2, q3, q4, 0f)
        assertQuatEqualsApproximately(q1, res, "Sqlerp t=0 (a)")

        // For t=1: Slerp(Slerp(a,d,1), Slerp(b,c,1), 0) = Slerp(d,c,0) = d.
        res = Quatf.sqlerpUnsafe(q1, q2, q3, q4, 1f)
        assertQuatEqualsApproximately(q4, res, "Sqlerp t=1 (d)")

        // For t=0.5: u = 2*0.5*(0.5) = 0.5
        // Slerp(Slerp(a,d,0.5), Slerp(b,c,0.5), 0.5)
        val slerpAD_05 = q1.slerp(q4, 0.5f)
        val slerpBC_05 = q2.slerp(q3, 0.5f)
        val expected_05 = slerpAD_05.slerp(slerpBC_05, 0.5f)
        res = Quatf.sqlerpUnsafe(q1, q2, q3, q4, 0.5f)
        assertQuatEqualsApproximately(expected_05, res, "Sqlerp t=0.5")
    }

    @Test
    fun testAngle() {
        val q1 = Quatf.identity()
        val q2 = Quatf.fromAxisAngle(Vec3f(1f,0f,0f), PI.toFloat() / 2f) // 90 deg X
        assertEquals(PI.toFloat() / 2f, q1.angle(q2), QUAT_TEST_EPSILON, "Angle between identity and 90 deg X")

        val q3 = Quatf.fromAxisAngle(Vec3f(1f,0f,0f), PI.toFloat()) // 180 deg X
        assertEquals(PI.toFloat(), q1.angle(q3), QUAT_TEST_EPSILON, "Angle between identity and 180 deg X")

        // Angle between q and -q should be 0 as they represent the same rotation
        val qNeg = q2.negate()
        assertEquals(0f, q2.angle(qNeg), QUAT_TEST_EPSILON, "Angle between q and -q")

        val q4 = Quatf.fromAxisAngle(Vec3f(0f,1f,0f), PI.toFloat() / 3f) // 60 deg Y
        // angle(q2, q4)
        // q2 = (s,0,0,c) s=c=sqrt(0.5)
        // q4 = (0,sin(pi/6),0,cos(pi/6)) = (0, 0.5, 0, sqrt(0.75))
        // dot = s*0 + 0*0.5 + 0*0 + c*sqrt(0.75) = sqrt(0.5)*sqrt(0.75) = sqrt(0.375)
        // angle = acos(2*dot^2 - 1) = acos(2*0.375 - 1) = acos(0.75 - 1) = acos(-0.25)
        val expectedAngle = kotlin.math.acos(2f * q2.dot(q4).let { it*it } - 1f)
        assertEquals(expectedAngle, q2.angle(q4), QUAT_TEST_EPSILON, "Angle between 90X and 60Y")
    }

    @Test
    fun testMultiplyAndMul() {
        val q90X = Quatf.fromAxisAngle(Vec3f(1f, 0f, 0f), PI.toFloat() / 2f)
        val q90Y = Quatf.fromAxisAngle(Vec3f(0f, 1f, 0f), PI.toFloat() / 2f)

        // Compose: R_y(90°) ⊗ R_x(90°) → applies X then Y
        val res = q90Y.multiply(q90X)
        // Let s = sin(π/4) = √0.5, c = cos(π/4) = √0.5
        // q90Y = ( 0,  s, 0, c )
        // q90X = ( s,  0, 0, c )
        // x =  c*s + 0 + 0   - 0    =  s·c = 0.5
        // y =  c*0 - 0 + s·c + 0    =  s·c = 0.5
        // z =  c*0 + 0   - s·s + 0  = -s²  = -0.5
        // w =  c·c - 0   - 0   - 0   =  c²  = 0.5
        val expected = Quatf(0.5f, 0.5f, -0.5f, 0.5f)
        assertQuatEqualsApproximately(expected, res, "Multiply 90Y * 90X")

        // alias
        val resAlias = q90Y.mul(q90X)
        assertQuatEqualsApproximately(expected, resAlias, "Mul alias 90Y * 90X")

        // identity
        val identity = Quatf.identity()
        assertQuatEqualsApproximately(q90X,          q90X.multiply(identity),  "Multiply by identity (right)")
        assertQuatEqualsApproximately(q90X, identity.multiply(q90X),          "Multiply by identity (left)")

        // dst‐parameter overload
        val qDst = Quatf()
        q90Y.multiply(q90X, qDst)
        assertQuatEqualsApproximately(expected, qDst, "Multiply with dst")
    }

    @Test
    fun testRotateXYZ() {
        val qInitial = Quatf.fromAxisAngle(Vec3f(0f,1f,0f), PI.toFloat() / 2f) // 90 deg around Y
        val halfPi = PI.toFloat() / 2f

        // RotateX
        // Initial: (0, s, 0, c) where s=c=sqrt(0.5)
        // Rotate by 90 deg X: bx=s, bw=c
        // x = 0*c + c*s = cs = 0.5
        // y = s*c + 0*s = cs = 0.5
        // z = 0*c - s*s = -ss = -0.5
        // w = c*c - 0*s = cc = 0.5
        val qRotX = qInitial.rotateX(halfPi)
        assertQuatEqualsApproximately(Quatf(0.5f, 0.5f, -0.5f, 0.5f), qRotX, "RotateX")

        // RotateY
        // Initial: (0, s, 0, c)
        // Rotate by 90 deg Y: by=s, bw=c
        // x = 0*c - 0*s = 0
        // y = s*c + c*s = 2cs = 1
        // z = 0*c + 0*s = 0
        // w = c*c - s*s = 0
        // Result is (0,1,0,0) which is 180 deg around Y
        val qRotY = qInitial.rotateY(halfPi)
        assertQuatEqualsApproximately(Quatf(0f, 1f, 0f, 0f), qRotY, "RotateY")

        // RotateZ
        // Initial: (0, s, 0, c)
        // Rotate by 90 deg Z: bz=s, bw=c
        // x = 0*c + s*s = ss = 0.5
        // y = s*c - 0*s = cs = 0.5
        // z = 0*c + c*s = cs = 0.5
        // w = c*c - 0*s = cc = 0.5
        val qRotZ = qInitial.rotateZ(halfPi)
        assertQuatEqualsApproximately(Quatf(0.5f, 0.5f, 0.5f, 0.5f), qRotZ, "RotateZ")

        val qDst = Quatf()
        qInitial.rotateX(halfPi, qDst)
        assertQuatEqualsApproximately(Quatf(0.5f, 0.5f, -0.5f, 0.5f), qDst, "RotateX with dst")
    }

    @Test
    fun testSlerp() {
        val q1 = Quatf.identity()
        val q2 = Quatf.fromAxisAngle(Vec3f(1f,0f,0f), PI.toFloat() / 2f) // 90 deg X

        var res = q1.slerp(q2, 0f)
        assertQuatEqualsApproximately(q1, res, "Slerp t=0")

        res = q1.slerp(q2, 1f)
        assertQuatEqualsApproximately(q2, res, "Slerp t=1")

        res = q1.slerp(q2, 0.5f) // 45 deg X
        val expectedMid = Quatf.fromAxisAngle(Vec3f(1f,0f,0f), PI.toFloat() / 4f)
        assertQuatEqualsApproximately(expectedMid, res, "Slerp t=0.5")

        // Test shortest path (q2 and -q2 are same rotation)
        val q2Neg = q2.negate()
        res = q1.slerp(q2Neg, 0.5f)
        assertQuatEqualsApproximately(expectedMid, res, "Slerp t=0.5 with negated target (shortest path)")

        // Test very close quaternions (should use lerp)
        val qAlmostQ1 = Quatf(QUAT_TEST_EPSILON*0.1f, 0f,0f, sqrt(1f - (QUAT_TEST_EPSILON*0.1f)*(QUAT_TEST_EPSILON*0.1f)))
        res = q1.slerp(qAlmostQ1, 0.5f)
        val expectedLerp = q1.lerp(qAlmostQ1, 0.5f) // Lerp should be a good approximation
        assertQuatEqualsApproximately(expectedLerp, res, "Slerp with very close quaternions")

        val qDst = Quatf()
        q1.slerp(q2, 0.5f, qDst)
        assertQuatEqualsApproximately(expectedMid, qDst, "Slerp with dst")
    }

    @Test
    fun testInverse() {
        // Non-normalized quaternion
        val q = Quatf(1f, 2f, 3f, 4f)
        val lenSq = 1f*1f + 2f*2f + 3f*3f + 4f*4f  // 30
        val qInv = q.inverse()
        val expectedInv = Quatf(-1f/lenSq, -2f/lenSq, -3f/lenSq,  4f/lenSq)
        assertQuatEqualsApproximately(
            expectedInv,
            qInv,
            "inverse(q) should be conjugate(q)/lengthSq(q)"
        )

        // q * inverse(q) == identity exactly
        val prod = q.multiply(qInv)
        assertQuatEqualsApproximately(
            Quatf.identity(),
            prod,
            "q * inverse(q) == identity"
        )

        // Normalize and test unit-quat properties
        val qNorm    = q.normalize()
        val qNormInv = qNorm.inverse()
        val qNormCon = qNorm.conjugate()

        // For a unit quaternion, inverse == conjugate
        assertQuatEqualsApproximately(
            qNormCon,
            qNormInv,
            "inverse(normalized q) should equal conjugate(normalized q)"
        )

        // And unit * inverse(unit) == identity
        val unitProd = qNorm.multiply(qNormInv)
        assertQuatEqualsApproximately(
            Quatf.identity(),
            unitProd,
            "normalized_q * inverse(normalized_q) == identity"
        )

        // Inverse into dst parameter
        val dst = Quatf()
        qNorm.inverse(dst)
        assertQuatEqualsApproximately(
            qNormCon,
            dst,
            "inverse(normalized q, dst) should write conjugate into dst"
        )

        // Edge case: zero quaternion
        val zero     = Quatf(0f, 0f, 0f, 0f)
        val zeroInv  = zero.inverse()
        assertQuatEqualsApproximately(
            zero,
            zeroInv,
            "inverse(zero quaternion) should be zero quaternion"
        )
    }



    @Test
    fun testConjugate() {
        val q = Quatf(1f, 2f, 3f, 4f)
        val conj = q.conjugate()
        assertQuatEqualsApproximately(Quatf(-1f, -2f, -3f, 4f), conj)

        val qDst = Quatf()
        q.conjugate(qDst)
        assertQuatEqualsApproximately(Quatf(-1f, -2f, -3f, 4f), qDst, "Conjugate with dst")
    }

    @Test
    fun testCopyAndClone() {
        val q1 = Quatf(1f, 2f, 3f, 4f)
        val q2 = q1.copy()
        assertNotSame(q1, q2)
        assertQuatEqualsApproximately(q1, q2, "Copied quaternion should be equal")

        val q3 = q1.clone()
        assertNotSame(q1, q3)
        assertQuatEqualsApproximately(q1, q3, "Cloned quaternion should be equal")

        val qDst = Quatf()
        q1.copy(qDst)
        assertQuatEqualsApproximately(q1, qDst, "Copy with dst")
    }

    @Test
    fun testAddAndPlus() {
        val q1 = Quatf(1f, 2f, 3f, 1f)
        val q2 = Quatf(4f, 5f, 6f, 2f)
        val expected = Quatf(5f, 7f, 9f, 3f)

        assertQuatEqualsApproximately(expected, q1.add(q2), "Add method")
        assertQuatEqualsApproximately(expected, q1 + q2, "Plus operator")

        val qDst = Quatf()
        q1.add(q2, qDst)
        assertQuatEqualsApproximately(expected, qDst, "Add with dst")
    }

    @Test
    fun testSubtractAndMinusAndSub() {
        val q1 = Quatf(5f, 7f, 9f, 3f)
        val q2 = Quatf(1f, 2f, 3f, 1f)
        val expected = Quatf(4f, 5f, 6f, 2f)

        assertQuatEqualsApproximately(expected, q1.subtract(q2), "Subtract method")
        assertQuatEqualsApproximately(expected, q1 - q2, "Minus operator")
        assertQuatEqualsApproximately(expected, q1.sub(q2), "Sub alias")

        val qDst = Quatf()
        q1.subtract(q2, qDst)
        assertQuatEqualsApproximately(expected, qDst, "Subtract with dst")
    }

    @Test
    fun testMulScalarAndTimesAndScale() {
        val q = Quatf(1f, 2f, 3f, 4f)
        val scalar = 2f
        val expected = Quatf(2f, 4f, 6f, 8f)

        assertQuatEqualsApproximately(expected, q.mulScalar(scalar), "MulScalar method")
        assertQuatEqualsApproximately(expected, q * scalar, "Times operator (scalar)")
        assertQuatEqualsApproximately(expected, q.scale(scalar), "Scale alias")

        val qDst = Quatf()
        q.mulScalar(scalar, qDst)
        assertQuatEqualsApproximately(expected, qDst, "MulScalar with dst")
    }

    @Test
    fun testDivScalarAndDiv() {
        val q = Quatf(2f, 4f, 6f, 8f)
        val scalar = 2f
        val expected = Quatf(1f, 2f, 3f, 4f)

        assertQuatEqualsApproximately(expected, q.divScalar(scalar), "DivScalar method")
        assertQuatEqualsApproximately(expected, q / scalar, "Div operator")

        val qDst = Quatf()
        q.divScalar(scalar, qDst)
        assertQuatEqualsApproximately(expected, qDst, "DivScalar with dst")

    }

    @Test
    fun testNegateAndUnaryMinus() {
        val q = Quatf(1f, -2f, 3f, -4f)
        val expected = Quatf(-1f, 2f, -3f, 4f)

        assertQuatEqualsApproximately(expected, q.negate(), "Negate method")
        assertQuatEqualsApproximately(expected, -q, "Unary minus operator")

        val qDst = Quatf()
        q.negate(qDst)
        assertQuatEqualsApproximately(expected, qDst, "Negate with dst")
    }

    @Test
    fun testDot() {
        val q1 = Quatf(1f, 2f, 3f, 4f)
        val q2 = Quatf(5f, 6f, 7f, 8f)
        // 1*5 + 2*6 + 3*7 + 4*8 = 5 + 12 + 21 + 32 = 70
        assertEquals(70f, q1.dot(q2), QUAT_TEST_EPSILON)
    }

    @Test
    fun testLerp() {
        val q1 = Quatf(1f, 1f, 1f, 1f)
        val q2 = Quatf(3f, 3f, 3f, 3f)

        var res = q1.lerp(q2, 0f)
        assertQuatEqualsApproximately(q1, res, "Lerp t=0")

        res = q1.lerp(q2, 1f)
        assertQuatEqualsApproximately(q2, res, "Lerp t=1")

        res = q1.lerp(q2, 0.5f)
        assertQuatEqualsApproximately(Quatf(2f, 2f, 2f, 2f), res, "Lerp t=0.5")

        val qDst = Quatf()
        q1.lerp(q2, 0.5f, qDst)
        assertQuatEqualsApproximately(Quatf(2f, 2f, 2f, 2f), qDst, "Lerp with dst")
    }

    @Test
    fun testLengthLenLengthSqLenSq() {
        val q = Quatf(1f, 2f, 2f, 0f) // length = sqrt(1+4+4) = sqrt(9) = 3
        assertEquals(3f, q.length, QUAT_TEST_EPSILON, "Length")
        assertEquals(3f, q.len, QUAT_TEST_EPSILON, "Len (alias)")
        assertEquals(9f, q.lengthSq, QUAT_TEST_EPSILON, "LengthSq")
        assertEquals(9f, q.lenSq, QUAT_TEST_EPSILON, "LenSq (alias)")

        val qZero = Quatf(0f,0f,0f,0f)
        assertEquals(0f, qZero.length, QUAT_TEST_EPSILON, "Length of zero quat")
        assertEquals(0f, qZero.lengthSq, QUAT_TEST_EPSILON, "LengthSq of zero quat")
    }

    @Test
    fun testNormalize() {
        val q1 = Quatf(1f, 2f, 2f, 0f) // length 3
        val q1Norm = q1.normalize()
        assertQuatEqualsApproximately(Quatf(1f/3f, 2f/3f, 2f/3f, 0f), q1Norm, "Normalize q1")
        assertEquals(1f, q1Norm.length, QUAT_TEST_EPSILON, "Normalized length should be 1")

        val qIdentity = Quatf.identity()
        val qIdentityNorm = qIdentity.normalize()
        assertQuatEqualsApproximately(qIdentity, qIdentityNorm, "Normalize identity")

        val qZero = Quatf(0f,0f,0f,0f)
        val qZeroNorm = qZero.normalize() // Should return identity
        assertQuatEqualsApproximately(Quatf.identity(), qZeroNorm, "Normalize zero quaternion returns identity")

        val qDst = Quatf()
        q1.normalize(qDst)
        assertQuatEqualsApproximately(Quatf(1f/3f, 2f/3f, 2f/3f, 0f), qDst, "Normalize with dst")
    }

    @Test
    fun testEqualsApproximately() {
        val q1 = Quatf(1f, 2f, 3f, 4f)
        val q2 = Quatf(1f + EPSILON/2f, 2f - EPSILON/2f, 3f, 4f)
        val q3 = Quatf(1f + EPSILON*2f, 2f, 3f, 4f)

        assertTrue(q1.equalsApproximately(q2), "q1 approx equals q2")
        assertFalse(q1.equalsApproximately(q3), "q1 not approx equals q3")
        assertTrue(q1.equalsApproximately(q1), "q1 approx equals itself")
    }

    @Test
    fun testEquals() {
        val q1 = Quatf(1f, 2f, 3f, 4f)
        val q2 = Quatf(1f, 2f, 3f, 4f)
        val q3 = Quatf(1.0000001f, 2f, 3f, 4f) // Slightly different due to float precision

        assertTrue(q1.equals(q2), "q1 exact equals q2") // Provided equals method
        assertEquals(q1, q2, "q1 equals q2 (data class)") // Data class equals

        assertFalse(q1.equals(q3), "q1 not exact equals q3")
        assertNotEquals(q1, q3, "q1 not equals q3 (data class)")
    }


    // Helper for Vec3f comparison
    private fun assertVec3EqualsApproximately(expected: Vec3f, actual: Vec3f, message: String? = null) {
        assertTrue(
            kotlin.math.abs(expected.x - actual.x) < QUAT_TEST_EPSILON &&
            kotlin.math.abs(expected.y - actual.y) < QUAT_TEST_EPSILON &&
            kotlin.math.abs(expected.z - actual.z) < QUAT_TEST_EPSILON,
            (message ?: "") + "\nExpected: $expected\nActual:   $actual (Epsilon: $QUAT_TEST_EPSILON)"
        )
    }
}