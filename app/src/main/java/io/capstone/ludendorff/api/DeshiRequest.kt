package io.capstone.ludendorff.api

import org.json.JSONArray
import org.json.JSONObject

class DeshiRequest (
    var token: String,
) {
    private var payload: JSONObject? = null

    constructor(token: String, payload: JSONObject): this(token) {
        this.payload = payload;
    }

    init {
        this.payload = JSONObject()
    }

    fun put(key: String, value: String?) {
        payload?.put(key, value)
    }
    fun put(key: String, value: Boolean) {
        payload?.put(key, value)
    }
    fun putArray(key: String, value: JSONArray) {
        payload?.put(key, value)
    }

    fun toJSONObject(): JSONObject {
        return payload
            ?.put(TOKEN, token)
            ?: JSONObject()
    }

    companion object {
        private const val TOKEN = "token"
        private const val EXTRAS = "extras"
    }
}