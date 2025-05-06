package io.github.natanfudge.wgpu4k.matrix.test.js

fun assertStrictEquals(actual: Any, expected: Any?, msg: String = "") {
    if (actual != expected) {
        throw AssertionError("${formatMsg(msg)}expected: ${expected} to equal actual: ${actual}");
    }
}

fun formatMsg(msg: String?): String {
    return "${msg ?: ""}${if(msg == "" || msg == null) "" else ": "}"
}

