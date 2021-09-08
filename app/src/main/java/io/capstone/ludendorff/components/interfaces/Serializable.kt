package io.capstone.ludendorff.components.interfaces

import org.json.JSONObject

interface Serializable {
    fun toJSON(): JSONObject
}