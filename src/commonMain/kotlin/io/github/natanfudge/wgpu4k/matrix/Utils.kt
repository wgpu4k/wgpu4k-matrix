package io.github.natanfudge.wgpu4k.matrix

import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.roundToLong

/**
 * Epsilon value for floating-point comparisons
 */
internal const val EPSILON = 0.000001f

internal const val FloatPi = PI.toFloat()

/**
 * Converts a Float to a readable string:
 * • Rounds to **at most six** digits after the decimal point.
 * • Removes any trailing “0” characters and an orphan “.”.
 * • Leaves **NaN**, **Infinity** and **‑Infinity** untouched.
 */
internal val Float.ns: String
    get() {
        // Special IEEE‑754 values
        if (!isFinite()) return toString()

        // -------- 1. Round to 6 decimal places --------
        val factor = 1_000_000L                // 10⁶
        val d = toDouble()

        /*  For huge magnitudes (|d| > ~9223372036854) the scaling step would
         *  overflow a Long.  At such sizes fractional digits are irrelevant
         *  anyway, so we just return the default representation.
         */
        if (abs(d) > Long.MAX_VALUE / factor.toDouble()) return d.toString()

        val scaled = (d * factor).roundToLong()   // HALF_UP rounding
        val rounded = scaled.toDouble() / factor   // still full double precision

        // -------- 2. Convert to text & strip cruft --------
        var s = rounded.toString()                  // e.g. "3.140000" or "2.0"

        if (s.contains('.')) {
            s = s.trimEnd('0').trimEnd('.')         // -> "3.14" or "2"
        }

        if (s == "-0") s = "0"                      // normalise negative zero
        return s
    }


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