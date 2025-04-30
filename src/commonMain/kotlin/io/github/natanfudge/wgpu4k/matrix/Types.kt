package io.github.natanfudge.wgpu4k.matrix

/**
 * A type similar to a number array, providing basic array-like functionality
 * for mathematical operations.
 */
interface MutableNumberArray {
    val size: Int
    operator fun get(index: Int): Float
    operator fun set(index: Int, value: Float)
}

/**
 * Base types that can be used for mathematical operations.
 */
typealias BaseArgType = FloatArray

/**
 * Creates a zero-filled float array of the specified [size].
 */
fun zeroArray(size: Int): FloatArray = FloatArray(size) { 0f }