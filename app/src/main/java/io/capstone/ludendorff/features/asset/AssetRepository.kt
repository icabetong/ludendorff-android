package io.capstone.ludendorff.features.asset

import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import io.capstone.ludendorff.features.assignment.Assignment
import io.capstone.ludendorff.features.category.Category
import io.capstone.ludendorff.features.core.backend.Response
import io.capstone.ludendorff.features.request.Request
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AssetRepository @Inject constructor(
    private val firestore: FirebaseFirestore
) {

    suspend fun fetch(assetId: String): Response<Asset> {
        return try {
            val task = firestore.collection(Asset.COLLECTION).document(assetId)
                .get().await()
            if (task != null) {
                val asset = task.toObject(Asset::class.java)
                if (asset != null) {
                    Response.Success(asset)
                } else Response.Error(Exception())
            } else Response.Error(Exception())
        } catch (e: Exception) {
            Response.Error(e)
        }
    }

    suspend fun create(asset: Asset): Response<Response.Action> {
        return try {
            firestore.runBatch { writeBatch ->
                writeBatch.set(firestore.collection(Asset.COLLECTION).document(asset.assetId),
                    asset)

                asset.category?.categoryId?.let {
                    writeBatch.update(firestore.collection(Category.COLLECTION).document(it),
                        mapOf(Category.FIELD_COUNT to FieldValue.increment(1)))
                }
            }.await()

            Response.Success(Response.Action.CREATE)
        } catch (exception: FirebaseFirestoreException) {
            Response.Error(exception, Response.Action.CREATE)
        } catch (exception: Exception) {
            Response.Error(exception, Response.Action.CREATE)
        }
    }

    suspend fun update(asset: Asset, categoryId: String? = null): Response<Response.Action> {
        return try {
            val batchWrite = firestore.batch()
            batchWrite.set(firestore.collection(Asset.COLLECTION)
                .document(asset.assetId), asset)

            /**
             *  Increment the count of the
             *  new category.
             */
            categoryId?.let {
                batchWrite.update(firestore.collection(Category.COLLECTION).document(it),
                    mapOf(Category.FIELD_COUNT to FieldValue.increment(-1)))
            }

            /**
             *  At the same time, decrement the
             *  count of the old category
             */
            asset.category?.categoryId?.let {
                batchWrite.update(firestore.collection(Category.COLLECTION).document(it),
                    mapOf(Category.FIELD_COUNT to FieldValue.increment(1)))
            }

            firestore.collection(Assignment.COLLECTION)
                .whereEqualTo(Assignment.FIELD_ASSET_ID, asset.assetId)
                .get().await()
                .documents.forEach {
                    batchWrite.update(it.reference, Assignment.FIELD_ASSET, asset.minimize())
                }

            firestore.collection(Request.COLLECTION)
                .whereEqualTo(Request.FIELD_ASSET_ID, asset.assetId)
                .get().await()
                .documents.forEach {
                    batchWrite.update(it.reference, Request.FIELD_ASSET, asset.minimize())
                }

            batchWrite.commit().await()

            Response.Success(Response.Action.UPDATE)
        } catch (exception: FirebaseFirestoreException) {
            Response.Error(exception, Response.Action.UPDATE)
        } catch (exception: Exception) {
            Response.Error(exception, Response.Action.UPDATE)
        }
    }

    suspend fun remove(asset: Asset): Response<Response.Action> {
        return try {
            firestore.runBatch { writeBatch ->
                writeBatch.delete(firestore.collection(Asset.COLLECTION)
                    .document(asset.assetId))

                asset.category?.categoryId?.let {
                    writeBatch.update(firestore.collection(Category.COLLECTION).document(it),
                        mapOf(Category.FIELD_COUNT to FieldValue.increment(-1)))
                }
            }.await()

            Response.Success(Response.Action.REMOVE)
        } catch (firestoreException: FirebaseFirestoreException) {
            Response.Error(firestoreException, Response.Action.REMOVE)
        } catch (exception: Exception) {
            Response.Error(exception, Response.Action.REMOVE)
        }
    }
}