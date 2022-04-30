package io.capstone.ludendorff.features.stockcard

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import io.capstone.ludendorff.api.Deshi
import io.capstone.ludendorff.api.DeshiException
import io.capstone.ludendorff.api.DeshiRequest
import io.capstone.ludendorff.components.extensions.toJSONArray
import io.capstone.ludendorff.features.core.backend.Response
import io.capstone.ludendorff.features.stockcard.entry.StockCardEntry
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class StockCardRepository @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val firebaseAuth: FirebaseAuth,
    private val deshi: Deshi,
) {

    suspend fun fetch(stockCard: String): Response<List<StockCardEntry>> {
        return try {
            val task = firestore.collection(StockCard.COLLECTION)
                .document(stockCard)
                .collection(StockCard.FIELD_ENTRIES)
                .get()
                .await()

            if (task != null) {
                val items = task.toObjects(StockCardEntry::class.java)
                Response.Success(items)
            } else Response.Error(Exception())
        } catch (e: Exception) {
            Response.Error(e)
        }
    }

    suspend fun create(stockCard: StockCard): Response<Response.Action> {
        return try {
            val collection = firestore.collection(StockCard.COLLECTION)
            firestore.runBatch {
                val reference = collection.document(stockCard.stockCardId)
                val entriesReference = reference.collection(StockCard.FIELD_ENTRIES)
                it.set(reference, stockCard)

                stockCard.entries.forEach { entry ->
                    it.set(entriesReference.document(entry.stockCardEntryId), entry)
                }
            }.await()

            val token = firebaseAuth.currentUser?.getIdToken(false)?.await()?.toString()
                ?: throw DeshiException(DeshiException.Code.UNAUTHORIZED)

            val request = DeshiRequest(token)
            request.put(Deshi.EXTRA_ID, stockCard.stockCardId)
            request.putArray(StockCard.FIELD_ENTRIES,
                stockCard.entries.map { it.toJSONObject() }.toJSONArray())
            val response = deshi.requestStockCardEntryUpdate(request)
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

    suspend fun update(stockCard: StockCard): Response<Response.Action> {
        return try {
            val collection = firestore.collection(StockCard.COLLECTION)
            val reference = collection.document(stockCard.stockCardId)
            val entriesReference = reference.collection(StockCard.FIELD_ENTRIES)
            val snapshot = entriesReference.get().await()
            for (doc in snapshot.documents) {
                doc.reference.delete().await()
            }

            firestore.runBatch {
                for (entry in stockCard.entries) {
                    it.set(entriesReference.document(entry.stockCardEntryId), entry)
                }
                it.set(reference, stockCard)
            }.await()

            val token = firebaseAuth.currentUser?.getIdToken(false)?.await()?.toString()
                ?: throw DeshiException(DeshiException.Code.UNAUTHORIZED)

            val request = DeshiRequest(token)
            request.put(Deshi.EXTRA_ID, stockCard.stockCardId)
            request.putArray(StockCard.FIELD_ENTRIES,
                stockCard.entries.map { it.toJSONObject() }.toJSONArray() )
            val response = deshi.requestStockCardEntryUpdate(request)
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

    suspend fun remove(stockCard: StockCard): Response<Response.Action> {
        return try {
            val cardReference = firestore.collection(StockCard.COLLECTION)
            val documentReference = cardReference.document(stockCard.stockCardId)
            val entriesReference = documentReference.collection(StockCard.FIELD_ENTRIES)

            firestore.runBatch {
                for (entry in stockCard.entries) {
                    it.delete(entriesReference.document(entry.stockCardEntryId))
                }
                it.delete(documentReference)
            }.await()

            Response.Success(Response.Action.REMOVE)
        } catch (exception: FirebaseFirestoreException) {
            Response.Error(exception, Response.Action.REMOVE)
        } catch (exception: Exception) {
            Response.Error(exception, Response.Action.REMOVE)
        }
    }
}