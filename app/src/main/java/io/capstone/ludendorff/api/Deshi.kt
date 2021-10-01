package io.capstone.ludendorff.api

import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import org.json.JSONObject
import ru.gildor.coroutines.okhttp.await

class Deshi {

    private val client by lazy { OkHttpClient() }

    private fun parse(jsonObject: JSONObject): RequestBody {
        val type = "application/json; charset=utf-8".toMediaType()
        return RequestBody.create(type, jsonObject.toString())
    }

    private suspend fun start(request: Request): Response {
        return client.newCall(request).await()
    }

    suspend fun requestUserCreate(deshiRequest: DeshiRequest): Response {
        val request = Request.Builder()
            .url("${SERVER_URL}${REQUEST_CREATE_USER}")
            .post(parse(deshiRequest.toJSONObject()))
            .build()
        return start(request)
    }

    suspend fun requestUserRemove(deshiRequest: DeshiRequest): Response {
        val request = Request.Builder()
            .url("${SERVER_URL}${REQUEST_REMOVE_USER}")
            .delete(parse(deshiRequest.toJSONObject()))
            .build()
        return start(request)
    }

    suspend fun requestUserModify(deshiRequest: DeshiRequest): Response {
        val request = Request.Builder()
            .url("${SERVER_URL}${REQUEST_MODIFY_USER}")
            .patch(parse(deshiRequest.toJSONObject()))
            .build()
        return start(request)
    }

    suspend fun newNotificationPost(deshiRequest: DeshiRequest): Response {
        val request = Request.Builder()
            .url("${SERVER_URL}${REQUEST_NOTIFICATION}")
            .post(parse(deshiRequest.toJSONObject()))
            .build()

        return start(request)
    }

    companion object {
        const val SERVER_URL = "https://deshi-production.up.railway.app/"
        const val REQUEST_NOTIFICATION = "send-notification"
        const val REQUEST_CREATE_USER = "create-user"
        const val REQUEST_REMOVE_USER = "remove-user"
        const val REQUEST_MODIFY_USER = "modify-user"

    }
}