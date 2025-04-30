# wgpu4k-matrix

Kotlin Multiplatform matrix implementation for WebGPU, inspired by [wgpu-matrix](https://github.com/greggman/wgpu-matrix). This library provides a comprehensive set of matrix and vector operations optimized for WebGPU applications across multiple platforms.

## Features
- Multiplatform support (JVM, JS, Native)
- WebGPU-optimized matrix operations
- Comprehensive vector and quaternion math
- Immutable and mutable operation variants


## Usage
```kotlin
// Example matrix operations
val mat4 = Mat4.identity()
val rotated = mat4.rotateX(Math.PI / 4)
val transformed = rotated.translate(Vec3(1.0, 2.0, 3.0))

// Vector operations
val vec4 = Vec4(1.0, 0.0, 0.0, 1.0)
val normalized = vec4.normalize()
```

## Project Status
Actively maintained with full test coverage for core operations. See [potential_improvements.md](src/commonMain/kotlin/io/github/natanfudge/wgpu4k/matrix/potential_improvements.md) for current development priorities.