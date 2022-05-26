package io.capstone.ludendorff.features.stockcard

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
import io.capstone.ludendorff.features.stockcard.entry.StockCardEntry
import kotlinx.coroutines.tasks.await
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

@HiltWorker
class StockCardWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val functions: FirebaseFunctions
): CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        return try {
            val id = inputData.getString(EXTRA_STOCK_CARD_ID)
                ?: throw DeshiException(DeshiException.Code.PRECONDITION_FAILED)
            val jsonItems = inputData.getStringArray(EXTRA_STOCK_CARD_ENTRIES)
                ?: throw DeshiException(DeshiException.Code.PRECONDITION_FAILED)

            val entries = jsonItems.map { Json.decodeFromString<StockCardEntry>(it) }
            val data = hashMapOf(DATA_ID to id, DATA_ENTRIES to entries)

            functions.getHttpsCallable(CALLABLE_NAME)
                .call(data).await()
            Result.success()
        } catch (exception: Exception) {
            Result.failure()
        }
    }

    companion object {
        const val EXTRA_STOCK_CARD_ID = "extra:stockCardId"
        const val EXTRA_STOCK_CARD_ENTRIES = "extra:stockCardEntries"
        const val CALLABLE_NAME = "indexStockCard"
        const val DATA_ID = "id"
        const val DATA_ENTRIES = "entries"

        fun convert(stockCard: StockCard): Data {
            val entries = stockCard.entries.map { Json.encodeToString(it) }.toTypedArray()
            return Data.Builder()
                .putString(EXTRA_STOCK_CARD_ID, stockCard.stockCardId)
                .putStringArray(EXTRA_STOCK_CARD_ENTRIES, entries)
                .build()
        }
    }
}