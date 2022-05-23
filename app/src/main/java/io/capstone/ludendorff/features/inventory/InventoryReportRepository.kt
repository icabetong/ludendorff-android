package io.capstone.ludendorff.features.inventory

import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import io.capstone.ludendorff.features.core.backend.Response
import io.capstone.ludendorff.features.inventory.item.InventoryItem
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class InventoryReportRepository @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val workManager: WorkManager,
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
            val reportsReference = firestore.collection(InventoryReport.COLLECTION)
            val documentReference = reportsReference.document(inventoryReport.inventoryReportId)
            val itemsReference = documentReference.collection(InventoryReport.FIELD_ITEMS)

            firestore.runBatch {
                it.set(documentReference, inventoryReport)

                for (item in inventoryReport.items) {
                    it.set(itemsReference.document(item.stockNumber), item)
                }
            }.await()

            val data = InventoryReportWorker.convert(inventoryReport)
            val workRequest = OneTimeWorkRequestBuilder<InventoryReportWorker>()
                .addTag(inventoryReport.inventoryReportId)
                .setInputData(data)
                .build()
            workManager.enqueueUniqueWork(inventoryReport.inventoryReportId,
                ExistingWorkPolicy.APPEND, workRequest)
            return Response.Success(Response.Action.CREATE)

        } catch (exception: FirebaseFirestoreException) {
            Response.Error(exception, Response.Action.CREATE)
        } catch (exception: Exception) {
            Response.Error(exception, Response.Action.CREATE)
        }
    }

    suspend fun update(inventoryReport: InventoryReport): Response<Response.Action> {
        return try {
            val reportReference = firestore.collection(InventoryReport.COLLECTION)
            val documentReference = reportReference.document(inventoryReport.inventoryReportId)
            val itemsReference = documentReference.collection(InventoryReport.FIELD_ITEMS)
            val snapshot = itemsReference.get().await()
            for (doc in snapshot.documents) {
                doc.reference.delete().await()
            }

            firestore.runBatch {
                it.set(documentReference, inventoryReport)
                for (item in inventoryReport.items) {
                    it.set(itemsReference.document(item.stockNumber), item)
                }
            }.await()

            val data = InventoryReportWorker.convert(inventoryReport)
            val workRequest = OneTimeWorkRequestBuilder<InventoryReportWorker>()
                .addTag(inventoryReport.inventoryReportId)
                .setInputData(data)
                .build()
            workManager.enqueueUniqueWork(inventoryReport.inventoryReportId,
                ExistingWorkPolicy.APPEND, workRequest)
            return Response.Success(Response.Action.CREATE)

        } catch (exception: FirebaseFirestoreException) {
            Response.Error(exception, Response.Action.CREATE)
        } catch (exception: Exception) {
            Response.Error(exception, Response.Action.CREATE)
        }
    }

    suspend fun remove(inventoryReport: InventoryReport): Response<Response.Action> {
        return try {
            val reportReference = firestore.collection(InventoryReport.COLLECTION)
            val documentReference = reportReference.document(inventoryReport.inventoryReportId)
            val itemReference = documentReference.collection(InventoryReport.FIELD_ITEMS)

            firestore.runBatch {
                for (item in inventoryReport.items) {
                    it.delete(itemReference.document(item.stockNumber))
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