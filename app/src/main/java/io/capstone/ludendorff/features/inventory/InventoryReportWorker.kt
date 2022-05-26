package io.capstone.ludendorff.features.inventory

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.Data
import androidx.work.WorkerParameters
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.functions.FirebaseFunctions
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
    private val functions: FirebaseFunctions
): CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        return try {
            val id = inputData.getString(EXTRA_INVENTORY_REPORT_ID)
                ?: throw DeshiException(DeshiException.Code.PRECONDITION_FAILED)
            val jsonItems = inputData.getStringArray(EXTRA_INVENTORY_REPORT_ITEMS)
                ?: throw DeshiException(DeshiException.Code.PRECONDITION_FAILED)

            val items = jsonItems.map { Json.decodeFromString<InventoryItem>(it) }
            val data = hashMapOf(DATA_ID to id, DATA_ENTRIES to items)

            functions.getHttpsCallable(CALLABLE_NAME)
                .call(data).await()
            Result.success()
        } catch (exception: Exception) {
            Result.failure()
        }
    }

    companion object {
        const val EXTRA_INVENTORY_REPORT_ID = "extra:inventoryReportId"
        const val EXTRA_INVENTORY_REPORT_ITEMS = "extra:inventoryReportItems"
        const val CALLABLE_NAME = "indexInventory"
        const val DATA_ID = "id"
        const val DATA_ENTRIES = "entries"

        fun convert(report: InventoryReport): Data {
            val inventoryItems = report.items.map { Json.encodeToString(it) }.toTypedArray()
            return Data.Builder()
                .putString(EXTRA_INVENTORY_REPORT_ID, report.inventoryReportId)
                .putStringArray(EXTRA_INVENTORY_REPORT_ITEMS, inventoryItems)
                .build()
        }
    }
}