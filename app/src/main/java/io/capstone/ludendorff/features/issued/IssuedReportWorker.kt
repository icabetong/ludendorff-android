package io.capstone.ludendorff.features.issued

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
import io.capstone.ludendorff.features.issued.item.IssuedItem
import kotlinx.coroutines.tasks.await
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

@HiltWorker
class IssuedReportWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val firebaseAuth: FirebaseAuth,
    private val deshi: Deshi
): CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        return try {
            val id = inputData.getString(EXTRA_ISSUED_REPORT_ID)
                ?: throw DeshiException(DeshiException.Code.PRECONDITION_FAILED)
            val jsonItems = inputData.getStringArray(EXTRA_ISSUED_REPORT_ITEMS)
                ?: throw DeshiException(DeshiException.Code.PRECONDITION_FAILED)

            val items = jsonItems.map { Json.decodeFromString<IssuedItem>(it) }
            val token = firebaseAuth?.currentUser?.getIdToken(false)?.await()?.token
                ?: throw DeshiException(DeshiException.Code.UNAUTHORIZED)

            val request = DeshiRequest(token)
            request.put(Deshi.EXTRA_ID, id)
            request.putArray(IssuedReport.FIELD_ITEMS,
                items.map { it.toJSONObject() }.toJSONArray())
            val response = deshi.requestIssuedItemsUpdate(request)
            response.close()
            if (response.code == 200)
                return Result.success()
            else throw DeshiException(response.code)

        } catch (exception: Exception) {
            return Result.failure()
        }
    }

    companion object {
        const val EXTRA_ISSUED_REPORT_ID = "extra:issuedReportId"
        const val EXTRA_ISSUED_REPORT_ITEMS = "extra:issuedReportItems"

        fun convert(report: IssuedReport): Data {
            val issuedItems = report.items.map { Json.encodeToString(it) }.toTypedArray()
            return Data.Builder()
                .putString(EXTRA_ISSUED_REPORT_ID, report.issuedReportId)
                .putStringArray(EXTRA_ISSUED_REPORT_ITEMS, issuedItems)
                .build()
        }
    }
}