package io.capstone.ludendorff.features.issued

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import io.capstone.ludendorff.features.core.backend.Response
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class IssuedReportRepository @Inject constructor(
    private val firestore: FirebaseFirestore
) {

    suspend fun create(issuedReport: IssuedReport): Response<Response.Action> {
        return try {
            val collection = firestore.collection(IssuedReport.COLLECTION)
            firestore.runBatch {
                val reference = collection.document(issuedReport.issuedReportId)
                it.set(reference, issuedReport)

                val itemsCollection = reference.collection(IssuedReport.FIELD_ITEMS)
                issuedReport.items.forEach { item ->
                    it.set(itemsCollection.document(item.stockNumber), item)
                }
            }.await()

            Response.Success(Response.Action.CREATE)
        } catch (exception: FirebaseFirestoreException) {
            Response.Error(exception, Response.Action.CREATE)
        } catch (exception: Exception) {
            Response.Error(exception, Response.Action.CREATE)
        }
    }

    suspend fun update(issuedReport: IssuedReport): Response<Response.Action> {
        return try {
            val collection = firestore.collection(IssuedReport.COLLECTION)
            firestore.runBatch {
                val reference = collection.document(issuedReport.issuedReportId)
                it.set(reference, issuedReport)

                val itemsCollection = reference.collection(IssuedReport.FIELD_ITEMS)
                issuedReport.items.forEach { item ->
                    it.set(itemsCollection.document(item.stockNumber), item)
                }
            }.await()

            Response.Success(Response.Action.CREATE)
        } catch (exception: FirebaseFirestoreException) {
            Response.Error(exception, Response.Action.CREATE)
        } catch (exception: Exception) {
            Response.Error(exception, Response.Action.CREATE)
        }
    }

    suspend fun remove(issuedReport: IssuedReport): Response<Response.Action> {
        return try {
            val collection = firestore.collection(IssuedReport.COLLECTION)
            firestore.runBatch {
                val reference = collection.document(issuedReport.issuedReportId)
                it.delete(reference)

                val itemsCollection = reference.collection(IssuedReport.FIELD_ITEMS)
                issuedReport.items.forEach { item ->
                    it.delete(itemsCollection.document(item.stockNumber))
                }
            }.await()

            Response.Success(Response.Action.CREATE)
        } catch (exception: FirebaseFirestoreException) {
            Response.Error(exception, Response.Action.CREATE)
        } catch (exception: Exception) {
            Response.Error(exception, Response.Action.CREATE)
        }
    }
}