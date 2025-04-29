package io.github.natanfudge.wgpu4k.matrix

import kotlin.math.PI
import kotlin.math.abs

/**
 * Epsilon value for floating-point comparisons
 */
internal const val EPSILON = 0.000001f

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
 * Convert degrees to radians
 * @param degrees Angle in degrees
 * @return angle converted to radians
 */
fun degToRad(degrees: Float): Float {
    return degrees * PI.toFloat() / 180f
}

/**
 * Convert radians to degrees
 * @param radians Angle in radians
 * @return angle converted to degrees
 */
fun radToDeg(radians: Float): Float {
    return radians * 180f / PI.toFloat()
}

/**
 * Lerps between a and b via t
 * @param a starting value
 * @param b ending value
 * @param t value where 0 = a and 1 = b
 * @return a + (b - a) * t
 */
fun lerp(a: Float, b: Float, t: Float): Float {
    return a + (b - a) * t
}

/**
 * Compute the opposite of lerp. Given a and b and a value between
 * a and b returns a value between 0 and 1. 0 if a, 1 if b.
 * Note: no clamping is done.
 * @param a start value
 * @param b end value
 * @param v value between a and b
 * @return (v - a) / (b - a)
 */
fun inverseLerp(a: Float, b: Float, v: Float): Float {
    val d = b - a
    return if (abs(b - a) < EPSILON) a else (v - a) / d
}

/**
 * Compute the euclidean modulo
 *
 * ```
 * // table for n / 3
 * -5, -4, -3, -2, -1,  0,  1,  2,  3,  4,  5   <- n
 * ------------------------------------
 * -2  -1  -0  -2  -1   0,  1,  2,  0,  1,  2   <- n % 3
 *  1   2   0   1   2   0,  1,  2,  0,  1,  2   <- euclideanModule(n, 3)
 * ```
 *
 * @param n dividend
 * @param m divisor
 * @return the euclidean modulo of n / m
 */
fun euclideanModulo(n: Float, m: Float): Float {
    return ((n % m) + m) % m
}