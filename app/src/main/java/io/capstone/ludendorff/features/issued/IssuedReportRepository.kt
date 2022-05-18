package io.capstone.ludendorff.features.issued

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import io.capstone.ludendorff.api.Deshi
import io.capstone.ludendorff.api.DeshiException
import io.capstone.ludendorff.api.DeshiRequest
import io.capstone.ludendorff.components.extensions.toJSONArray
import io.capstone.ludendorff.features.core.backend.Response
import io.capstone.ludendorff.features.issued.item.IssuedItem
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class IssuedReportRepository @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val firebaseAuth: FirebaseAuth,
    private val deshi: Deshi,
) {

    suspend fun fetch(issuedReport: String): Response<List<IssuedItem>> {
        return try {
            val task = firestore.collection(IssuedReport.COLLECTION)
                .document(issuedReport)
                .collection(IssuedReport.FIELD_ITEMS)
                .get()
                .await()

            if (task != null) {
                val items = task.toObjects(IssuedItem::class.java)
                Response.Success(items)
            } else Response.Error(Exception())
        } catch (e: Exception) {
            Response.Error(e)
        }
    }

    suspend fun create(issuedReport: IssuedReport): Response<Response.Action> {
        return try {
            val reportsReference = firestore.collection(IssuedReport.COLLECTION)
            val documentReference = reportsReference.document(issuedReport.issuedReportId)
            val itemsReference = documentReference.collection(IssuedReport.FIELD_ITEMS)

            firestore.runBatch {
                it.set(documentReference, issuedReport)

                for (item in issuedReport.items) {
                    it.set(itemsReference.document(item.issuedReportItemId), item)
                }
            }.await()

            val token = firebaseAuth.currentUser?.getIdToken(false)?.await()?.toString()
                ?: throw DeshiException(DeshiException.Code.UNAUTHORIZED)

            val request = DeshiRequest(token)
            request.put(Deshi.EXTRA_ID, issuedReport.issuedReportId)
            request.putArray(IssuedReport.FIELD_ITEMS,
                issuedReport.items.map { it.toJSONObject() }.toJSONArray())
            val response = deshi.requestIssuedItemsUpdate(request)
            response.close()
            if (response.code == 200)
                Response.Success(Response.Action.CREATE)
            else throw DeshiException(response.code)

        } catch (exception: FirebaseFirestoreException) {
            Response.Error(exception, Response.Action.CREATE)
        } catch (exception: Exception) {
            Response.Error(exception, Response.Action.CREATE)
        }
    }

    suspend fun update(issuedReport: IssuedReport): Response<Response.Action> {
        return try {
            val reportsReference = firestore.collection(IssuedReport.COLLECTION)
            val documentReference = reportsReference.document(issuedReport.issuedReportId)
            val itemsReference = documentReference.collection(IssuedReport.FIELD_ITEMS)
            val snapshot = itemsReference.get().await()
            for (doc in snapshot.documents) {
                doc.reference.delete().await()
            }

            firestore.runBatch {
                it.set(documentReference, issuedReport)
                for (item in issuedReport.items) {
                    it.set(itemsReference.document(item.issuedReportItemId), item)
                }
            }.await()

            val token = firebaseAuth.currentUser?.getIdToken(false)?.await()?.toString()
                ?: throw DeshiException(DeshiException.Code.UNAUTHORIZED)

            val request = DeshiRequest(token)
            request.put(Deshi.EXTRA_ID, issuedReport.issuedReportId)
            request.putArray(IssuedReport.FIELD_ITEMS,
                issuedReport.items.map{ it.toJSONObject() }.toJSONArray())
            val response = deshi.requestIssuedItemsUpdate(request)
            response.close()
            if (response.code == 200)
                Response.Success(Response.Action.UPDATE)
            else throw DeshiException(response.code)

        } catch (exception: FirebaseFirestoreException) {
            Response.Error(exception, Response.Action.UPDATE)
        } catch (exception: Exception) {
            Response.Error(exception, Response.Action.UPDATE)
        }
    }

    suspend fun remove(issuedReport: IssuedReport): Response<Response.Action> {
        return try {
            val reportsReference = firestore.collection(IssuedReport.COLLECTION)
            firestore.runBatch {
                val reference = reportsReference.document(issuedReport.issuedReportId)
                val itemsCollection = reference.collection(IssuedReport.FIELD_ITEMS)
                for (item in issuedReport.items) {
                    it.delete(itemsCollection.document(item.issuedReportItemId))
                }

                it.delete(reference)
            }.await()

            Response.Success(Response.Action.REMOVE)
        } catch (exception: FirebaseFirestoreException) {
            Response.Error(exception, Response.Action.REMOVE)
        } catch (exception: Exception) {
            Response.Error(exception, Response.Action.REMOVE)
        }
    }
}