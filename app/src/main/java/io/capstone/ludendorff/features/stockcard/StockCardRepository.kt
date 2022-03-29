package io.capstone.ludendorff.features.stockcard

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import io.capstone.ludendorff.features.core.backend.Response
import io.capstone.ludendorff.features.inventory.InventoryReport
import io.capstone.ludendorff.features.stockcard.entry.StockCardEntry
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class StockCardRepository @Inject constructor(
    private val firestore: FirebaseFirestore
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

            Response.Success(Response.Action.CREATE)
        } catch (exception: FirebaseFirestoreException) {
            Response.Error(exception, Response.Action.CREATE)
        } catch (exception: Exception) {
            Response.Error(exception, Response.Action.CREATE)
        }
    }

    suspend fun update(stockCard: StockCard): Response<Response.Action> {
        return try {
            val collection = firestore.collection(StockCard.COLLECTION)
            firestore.runBatch {
                val reference = collection.document(stockCard.stockCardId)
                val entriesReference = reference.collection(StockCard.FIELD_ENTRIES)
                stockCard.entries.forEach { entry ->
                    it.set(entriesReference.document(entry.stockCardEntryId), entry)
                }

                it.set(reference, stockCard)
            }.await()

            Response.Success(Response.Action.UPDATE)
        } catch (exception: FirebaseFirestoreException) {
            Response.Error(exception, Response.Action.UPDATE)
        } catch (exception: Exception) {
            Response.Error(exception, Response.Action.UPDATE)
        }
    }

    suspend fun remove(stockCard: StockCard): Response<Response.Action> {
        return try {
            val collection = firestore.collection(InventoryReport.COLLECTION)
            firestore.runBatch {
                val reference = collection.document(stockCard.stockCardId)
                val entriesReference = reference.collection(StockCard.FIELD_ENTRIES)
                stockCard.entries.forEach { entry ->
                    it.delete(entriesReference.document(entry.stockCardEntryId))
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