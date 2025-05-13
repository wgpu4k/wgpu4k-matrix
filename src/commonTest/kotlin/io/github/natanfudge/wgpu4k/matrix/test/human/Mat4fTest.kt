package io.github.natanfudge.wgpu4k.matrix.test.human

import io.github.natanfudge.wgpu4k.matrix.Mat4f
import io.github.natanfudge.wgpu4k.matrix.Vec4f
import io.github.natanfudge.wgpu4k.matrix.test.assertMat4EqualsApproximately
import io.github.natanfudge.wgpu4k.matrix.test.assertVec4EqualsApproximately
import kotlin.math.PI
import kotlin.test.Test

class Mat4fHumanTest {
    @Test
    fun testProjection() {
        val perspective = Mat4f.perspective(
            fieldOfViewYInRadians = PI.toFloat() /3,
            aspect = 4/3f,
            zNear = 0.01f,
            zFar = 100f
        )

        assertMat4EqualsApproximately(
            Mat4f.rowMajor(1.299038f, 0f, 0f ,0f,
                0f, 1.732051f, 0f, 0f,
                0f,0f, -1.0001f, -0.010001f,
                0f,0f,-1f, 0f),
            perspective
        )

        val result = perspective * Vec4f(1f, 1f, -10f, 1f)

        assertVec4EqualsApproximately(Vec4f(1.299038f,1.732051f,9.990999f,10f), result)
    }
}