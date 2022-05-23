package io.capstone.ludendorff.features.inventory

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.Data
import androidx.work.WorkerParameters
import com.google.firebase.auth.FirebaseAuth
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import io.capstone.ludendorff.api.Deshi
import io.capstone.ludendorff.api.DeshiException
import io.capstone.ludendorff.api.DeshiRequest
import io.capstone.ludendorff.components.extensions.toJSONArray
import io.capstone.ludendorff.features.core.backend.Response
import io.capstone.ludendorff.features.inventory.item.InventoryItem
import kotlinx.coroutines.tasks.await
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

@HiltWorker
class InventoryReportWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val firebaseAuth: FirebaseAuth,
    private val deshi: Deshi
): CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        return try {
            val id = inputData.getString(EXTRA_INVENTORY_REPORT_ID)
                ?: throw DeshiException(DeshiException.Code.PRECONDITION_FAILED)
            val jsonItems = inputData.getStringArray(EXTRA_INVENTORY_REPORT_ITEMS)
                ?: throw DeshiException(DeshiException.Code.PRECONDITION_FAILED)

            val items = jsonItems.map { Json.decodeFromString<InventoryItem>(it) }
            val token = firebaseAuth?.currentUser?.getIdToken(false)?.await()?.token
                ?: throw DeshiException(DeshiException.Code.UNAUTHORIZED)

            val request = DeshiRequest(token)
            request.put(Deshi.EXTRA_ID, id)
            request.putArray(InventoryReport.FIELD_ITEMS,
                items.map { it.toJSONObject() }.toJSONArray())
            val response = deshi.requestInventoryItemsUpdate(request)
            response.close()
            if (response.code == 200)
                return Result.success()
            else throw DeshiException(response.code)

        } catch (exception: Exception) {
            return Result.failure()
        }
    }

    companion object {
        const val EXTRA_INVENTORY_REPORT_ID = "extra:inventoryReportId"
        const val EXTRA_INVENTORY_REPORT_ITEMS = "extra:inventoryReportItems"

        fun convert(report: InventoryReport): Data {
            val inventoryItems = report.items.map { Json.encodeToString(it) }.toTypedArray()
            return Data.Builder()
                .putString(EXTRA_INVENTORY_REPORT_ID, report.inventoryReportId)
                .putStringArray(EXTRA_INVENTORY_REPORT_ITEMS, inventoryItems)
                .build()
        }
    }
}