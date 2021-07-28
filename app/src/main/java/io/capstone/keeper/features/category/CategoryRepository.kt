package io.capstone.keeper.features.category

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import io.capstone.keeper.features.asset.Asset
import io.capstone.keeper.features.core.backend.FirestoreRepository
import io.capstone.keeper.features.core.data.Response
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CategoryRepository @Inject constructor(
    private val firestore: FirebaseFirestore
): FirestoreRepository<Category> {

    suspend fun create(data: Category): Response<FirestoreRepository.Action> {
        return try {
            firestore.collection(Category.COLLECTION)
                .document(data.categoryId)
                .set(data)
                .await()

            Response.Success(FirestoreRepository.Action.CREATE)
        } catch (firestoreException: FirebaseFirestoreException) {
            Response.Error(firestoreException, FirestoreRepository.Action.CREATE)
        } catch (exception: Exception) {
            Response.Error(exception, FirestoreRepository.Action.CREATE)
        }
    }

    suspend fun update(data: Category): Response<FirestoreRepository.Action> {
        return try {
            val batchWrite = firestore.batch()
            batchWrite.set(firestore.collection(Category.COLLECTION).document(data.categoryId),
                data)

            firestore.collection(Asset.COLLECTION)
                .whereEqualTo(Asset.FIELD_CATEGORY_ID, data.categoryId)
                .get().await()
                .documents.forEach {
                    batchWrite.update(it.reference, Asset.FIELD_CATEGORY, data)
                }
            batchWrite.commit()

            Response.Success(FirestoreRepository.Action.UPDATE)
        } catch (firestoreException: FirebaseFirestoreException) {
            Response.Error(firestoreException, FirestoreRepository.Action.UPDATE)
        } catch (exception: Exception) {
            Response.Error(exception, FirestoreRepository.Action.UPDATE)
        }
    }

    suspend fun remove(category: Category): Response<FirestoreRepository.Action> {
        return try {
            firestore.collection(Category.COLLECTION)
                .document(category.categoryId)
                .delete()
                .await()

            Response.Success(FirestoreRepository.Action.REMOVE)
        } catch (firestoreException: FirebaseFirestoreException) {
            Response.Error(firestoreException, FirestoreRepository.Action.REMOVE)
        } catch (exception: Exception) {
            Response.Error(exception, FirestoreRepository.Action.REMOVE)
        }
    }

}