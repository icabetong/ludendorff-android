package io.capstone.keeper.features.asset

import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import io.capstone.keeper.features.category.Category
import io.capstone.keeper.features.core.data.Response
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

    suspend fun insert(asset: Asset): Response<Unit> {
        return try {
            firestore.runBatch { writeBatch ->
                writeBatch.set(firestore.collection(Asset.COLLECTION).document(asset.assetId),
                    asset)

                asset.category?.categoryId?.let {
                    writeBatch.update(firestore.collection(Category.COLLECTION).document(it),
                        mapOf(Category.FIELD_COUNT to FieldValue.increment(1)))
                }
            }.await()

            Response.Success(Unit)
        } catch (e: Exception) {
            Response.Error(e)
        }
    }

    suspend fun update(asset: Asset, categoryId: String? = null): Response<Unit> {
        return try {
            firestore.runBatch { writeBatch ->
                writeBatch.set(firestore.collection(Asset.COLLECTION)
                    .document(asset.assetId), asset)

                /**
                 *  Increment the count of the
                 *  new category.
                 */
                asset.category?.categoryId?.let {
                    writeBatch.update(firestore.collection(Category.COLLECTION).document(it),
                        mapOf(Category.FIELD_COUNT to FieldValue.increment(1)))
                }

                /**
                 *  At the same time, decrement the
                 *  count of the old category
                 */
                categoryId?.let {
                    writeBatch.update(firestore.collection(Category.COLLECTION).document(it),
                        mapOf(Category.FIELD_COUNT to FieldValue.increment(-1)))
                }
            }.await()

            Response.Success(Unit)
        } catch (firestoreException: FirebaseFirestoreException) {
            Response.Error(firestoreException)
        } catch (exception: Exception) {
            Response.Error(exception)
        }
    }

    suspend fun remove(asset: Asset): Response<Unit> {
        return try {
            firestore.runBatch { writeBatch ->
                writeBatch.delete(firestore.collection(Asset.COLLECTION)
                    .document(asset.assetId))

                asset.category?.categoryId?.let {
                    writeBatch.update(firestore.collection(Category.COLLECTION).document(it),
                        mapOf(Category.FIELD_COUNT to FieldValue.increment(-1)))
                }
            }.await()

            Response.Success(Unit)
        } catch (firestoreException: FirebaseFirestoreException) {
            Response.Error(firestoreException)
        } catch (exception: Exception) {
            Response.Error(exception)
        }
    }
}