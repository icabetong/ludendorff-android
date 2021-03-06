package io.capstone.ludendorff.api

import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.Response
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

    @Deprecated("Endpoint moved to Firebase Functions")
    suspend fun requestInventoryItemsUpdate(deshiRequest: DeshiRequest): Response {
        val request = Request.Builder()
            .url("${SERVER_URL}${REQUEST_UPDATE_INVENTORY}")
            .patch(parse(deshiRequest.toJSONObject()))
            .build()
        return start(request)
    }

    @Deprecated("Endpoint moved to Firebase Functions")
    suspend fun requestIssuedItemsUpdate(deshiRequest: DeshiRequest): Response {
        val request = Request.Builder()
            .url("${SERVER_URL}${REQUEST_UPDATE_ISSUED}")
            .patch(parse(deshiRequest.toJSONObject()))
            .build()
        return start(request)
    }

    @Deprecated("Endpoint moved to Firebase Functions")
    suspend fun requestStockCardEntryUpdate(deshiRequest: DeshiRequest): Response {
        val request = Request.Builder()
            .url("${SERVER_URL}${REQUEST_UPDATE_STOCK_CARDS}")
            .patch(parse(deshiRequest.toJSONObject()))
            .build()
        return start(request)
    }

    @Deprecated("Endpoint moved to Firebase Functions")
    suspend fun requestUserCreate(deshiRequest: DeshiRequest): Response {
        val request = Request.Builder()
            .url("${SERVER_URL}${REQUEST_CREATE_USER}")
            .post(parse(deshiRequest.toJSONObject()))
            .build()
        return start(request)
    }

    @Deprecated("Endpoint moved to Firebase Functions")
    suspend fun requestUserRemove(deshiRequest: DeshiRequest): Response {
        val request = Request.Builder()
            .url("${SERVER_URL}${REQUEST_REMOVE_USER}")
            .delete(parse(deshiRequest.toJSONObject()))
            .build()
        return start(request)
    }

    @Deprecated("Endpoint moved to Firebase Functions")
    suspend fun requestUserModify(deshiRequest: DeshiRequest): Response {
        val request = Request.Builder()
            .url("${SERVER_URL}${REQUEST_MODIFY_USER}")
            .patch(parse(deshiRequest.toJSONObject()))
            .build()
        return start(request)
    }

    companion object {
        const val SERVER_URL = "https://deshi-production.up.railway.app/"
        const val REQUEST_CREATE_USER = "create-user"
        const val REQUEST_REMOVE_USER = "remove-user"
        const val REQUEST_MODIFY_USER = "modify-user"
        const val REQUEST_UPDATE_INVENTORY = "inventory-items"
        const val REQUEST_UPDATE_ISSUED = "issued-items"
        const val REQUEST_UPDATE_STOCK_CARDS = "stock-card-entries";
    }
}