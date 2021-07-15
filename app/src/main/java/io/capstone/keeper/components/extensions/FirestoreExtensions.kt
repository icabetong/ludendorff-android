package io.capstone.keeper.components.extensions

import kotlin.math.floor

fun autoGenerateId(): String {
    val characters = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789"
    var autoId = ""
    for (i in 0 until 20) {
        autoId += characters[floor(Math.random() * characters.length).toInt()]
    }
    assert(autoId.length == 20)
    return autoId
}