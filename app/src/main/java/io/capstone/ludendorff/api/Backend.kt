package io.capstone.ludendorff.api

import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import ru.gildor.coroutines.okhttp.await

class Backend {

    private val client by lazy { OkHttpClient() }

    private suspend fun start(request: Request): Response {
        return client.newCall(request).await()
    }

    suspend fun newCreateUserPost(createUserRequest: CreateUserRequest): Response {
        val request = Request.Builder()
            .url("${SERVER_URL}${REQUEST_CREATE_USER}")
            .post(createUserRequest.toRequestBody())
            .build()
        return start(request)
    }

    suspend fun newNotificationPost(notificationRequest: NotificationRequest): Response {
        val request = Request.Builder()
            .url("${SERVER_URL}${REQUEST_NOTIFICATION}")
            .post(notificationRequest.toRequestBody())
            .build()

        return start(request)
    }

    companion object {
        const val SERVER_URL = "https://deshi-production.up.railway.app/"
        const val REQUEST_NOTIFICATION = "send-notification"
        const val REQUEST_CREATE_USER = "create-user"

    }
}