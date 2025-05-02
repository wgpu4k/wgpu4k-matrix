package io.github.natanfudge.wgpu4k.matrix

import kotlin.math.PI
import kotlin.math.abs

/**
 * Epsilon value for floating-point comparisons
 */
internal const val EPSILON = 0.000001f

internal const val FloatPi = PI.toFloat()

///**
// * Set the value for EPSILON for various checks
// * @param v Value to use for EPSILON.
// * @return previous value of EPSILON
// */
//fun setEpsilon(v: Float): Float {
//    val old = EPSILON
//    EPSILON = v
//    return old
//}

/**
 * Converts angle [degrees] to radians.
 */
fun degToRad(degrees: Float): Float {
    return degrees * FloatPi / 180f
}

/**
 * Converts angle [radians] to degrees.
 */
fun radToDeg(radians: Float): Float {
    return radians * 180f / FloatPi
}

/**
 * Linearly interpolates between [a] and [b] using [t] (0 = [a], 1 = [b]).
 */
fun lerp(a: Float, b: Float, t: Float): Float {
    return a + (b - a) * t
}

/**
 * Computes the inverse of lerp. Given a starting value [a], an ending value [b],
 * and an intermediate value [v], returns the interpolation factor between 0 and 1.
 * Note: no clamping is done.
 */
fun inverseLerp(a: Float, b: Float, v: Float): Float {
    val d = b - a
    return if (abs(b - a) < EPSILON) a else (v - a) / d
}

/**
 * Computes the Euclidean modulo of [n] / [m].
 *
 * ```
 * // table for n / 3
 * -5, -4, -3, -2, -1,  0,  1,  2,  3,  4,  5   <- n
 * ------------------------------------
 * -2  -1  -0  -2  -1   0,  1,  2,  0,  1,  2   <- n % 3
 *  1   2   0   1   2   0,  1,  2,  0,  1,  2   <- euclideanModulo(n, 3)
 * ```
 */
fun euclideanModulo(n: Float, m: Float): Float {
    return ((n % m) + m) % m
}