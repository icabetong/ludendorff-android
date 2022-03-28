package io.capstone.ludendorff.features.inventory

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import io.capstone.ludendorff.features.core.backend.Response
import io.capstone.ludendorff.features.inventory.item.InventoryItem
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class InventoryReportRepository @Inject constructor(
    private val firestore: FirebaseFirestore
){

    suspend fun fetch(inventoryReportId: String): Response<List<InventoryItem>> {
        return try {
            val task = firestore.collection(InventoryReport.COLLECTION)
                .document(inventoryReportId)
                .collection(InventoryReport.FIELD_ITEMS)
                .get()
                .await()

            if (task != null) {
                val items = task.toObjects(InventoryItem::class.java)
                Response.Success(items)
            } else Response.Error(Exception())
        } catch (e: Exception) {
            Response.Error(e)
        }
    }

    suspend fun create(inventoryReport: InventoryReport): Response<Response.Action> {
        return try {
            val collection = firestore.collection(InventoryReport.COLLECTION)
            firestore.runBatch {
                val reference = collection.document(inventoryReport.inventoryReportId)
                it.set(reference, inventoryReport)

                val itemsCollection = reference.collection(InventoryReport.FIELD_ITEMS)
                inventoryReport.items.forEach { item ->
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

    suspend fun update(inventoryReport: InventoryReport): Response<Response.Action> {
        return try {
            val collection = firestore.collection(InventoryReport.COLLECTION)
            firestore.runBatch {
                val reference = collection.document(inventoryReport.inventoryReportId)
                it.set(reference, inventoryReport)

                val itemsCollection = reference.collection(InventoryReport.FIELD_ITEMS)
                inventoryReport.items.forEach { item ->
                    it.set(itemsCollection.document(item.stockNumber), item)
                }
            }.await()

            Response.Success(Response.Action.UPDATE)
        } catch (exception: FirebaseFirestoreException) {
            Response.Error(exception, Response.Action.CREATE)
        } catch (exception: Exception) {
            Response.Error(exception, Response.Action.CREATE)
        }
    }

    suspend fun remove(inventoryReport: InventoryReport): Response<Response.Action> {
        return try {
            val collection = firestore.collection(InventoryReport.COLLECTION)
            firestore.runBatch {
                val reference = collection.document(inventoryReport.inventoryReportId)
                it.delete(reference)

                val itemsCollection = reference.collection(InventoryReport.FIELD_ITEMS)
                inventoryReport.items.forEach { item ->
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