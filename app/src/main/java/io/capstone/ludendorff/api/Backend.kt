package io.capstone.ludendorff.api

import io.capstone.ludendorff.api.request.CreateUserRequest
import io.capstone.ludendorff.api.request.RemoveUserRequest
import io.capstone.ludendorff.api.request.NotificationRequest
import okhttp3.*
import org.json.JSONObject
import ru.gildor.coroutines.okhttp.await

class Backend {

    private val client by lazy { OkHttpClient() }

    private fun parse(jsonObject: JSONObject): RequestBody {
        val type = MediaType.get("application/json; charset=utf-8")
        return RequestBody.create(type, jsonObject.toString())
    }

    private suspend fun start(request: Request): Response {
        return client.newCall(request).await()
    }

    suspend fun newRemoveUserPost(removeUserRequest: RemoveUserRequest): Response {
        val request = Request.Builder()
            .url("${SERVER_URL}${REQUEST_REMOVE_USER}")
            .post(parse(removeUserRequest.toJSONObject()))
            .build()
        return start(request)
    }

    suspend fun newCreateUserPost(createUserRequest: CreateUserRequest): Response {
        val request = Request.Builder()
            .url("${SERVER_URL}${REQUEST_CREATE_USER}")
            .post(parse(createUserRequest.toJSONObject()))
            .build()
        return start(request)
    }

    suspend fun newNotificationPost(notificationRequest: NotificationRequest): Response {
        val request = Request.Builder()
            .url("${SERVER_URL}${REQUEST_NOTIFICATION}")
            .post(parse(notificationRequest.toJSONObject()))
            .build()

        return start(request)
    }

    companion object {
        const val SERVER_URL = "https://deshi-production.up.railway.app/"
        const val REQUEST_NOTIFICATION = "send-notification"
        const val REQUEST_CREATE_USER = "create-user"
        const val REQUEST_REMOVE_USER = "remove-user"

    }
}