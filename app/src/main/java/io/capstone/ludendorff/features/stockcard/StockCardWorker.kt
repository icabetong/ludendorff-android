package io.capstone.ludendorff.features.stockcard

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
import io.capstone.ludendorff.features.stockcard.entry.StockCardEntry
import kotlinx.coroutines.tasks.await
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

@HiltWorker
class StockCardWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val firebaseAuth: FirebaseAuth,
    private val deshi: Deshi
): CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        return try {
            val id = inputData.getString(EXTRA_STOCK_CARD_ID)
                ?: throw DeshiException(DeshiException.Code.PRECONDITION_FAILED)
            val jsonItems = inputData.getStringArray(EXTRA_STOCK_CARD_ENTRIES)
                ?: throw DeshiException(DeshiException.Code.PRECONDITION_FAILED)

            val entries = jsonItems.map { Json.decodeFromString<StockCardEntry>(it) }
            val token = firebaseAuth.currentUser?.getIdToken(false)?.await()?.toString()
                ?: throw DeshiException(DeshiException.Code.UNAUTHORIZED)

            val request = DeshiRequest(token)
            request.put(Deshi.EXTRA_ID, id)
            request.putArray(StockCard.FIELD_ENTRIES,
                entries.map { it.toJSONObject() }.toJSONArray())
            val response = deshi.requestStockCardEntryUpdate(request)
            response.close()
            if (response.code == 200)
                return Result.success()
            else throw DeshiException(response.code)

        } catch (exception: Exception) {
            return Result.failure()
        }
    }

    companion object {
        const val EXTRA_STOCK_CARD_ID = "extra:stockCardId"
        const val EXTRA_STOCK_CARD_ENTRIES = "extra:stockCardEntries"

        fun convert(stockCard: StockCard): Data {
            val entries = stockCard.entries.map { Json.encodeToString(it) }.toTypedArray()
            return Data.Builder()
                .putString(EXTRA_STOCK_CARD_ID, stockCard.stockCardId)
                .putStringArray(EXTRA_STOCK_CARD_ENTRIES, entries)
                .build()
        }
    }
}