package io.capstone.ludendorff.components.extensions

import org.json.JSONArray

fun <T> List<T>.toJSONArray(): JSONArray {
    val array = JSONArray()
    this.forEach { array.put(it) }
    return array
}