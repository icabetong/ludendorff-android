package io.capstone.keeper.android.features.category

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import io.capstone.keeper.android.features.core.backend.FirestoreRepository
import io.capstone.keeper.android.features.core.data.Response
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CategoryRepository @Inject constructor(
    private val firestore: FirebaseFirestore
): FirestoreRepository<Category> {

    override suspend fun create(data: Category): Response<Unit> {
        return try {
            firestore.collection(Category.COLLECTION)
                .document(data.categoryId)
                .set(data)
                .await()
            Response.Success(Unit)
        } catch (firestoreException: FirebaseFirestoreException) {
            Response.Error(firestoreException)
        } catch (exception: Exception) {
            Response.Error(exception)
        }
    }

    override suspend fun update(data: Category): Response<Unit> {
        return try {
            firestore.collection(Category.COLLECTION)
                .document(data.categoryId)
                .set(data)
                .await()
            Response.Success(Unit)
        } catch (firestoreException: FirebaseFirestoreException) {
            Response.Error(firestoreException)
        } catch (exception: Exception) {
            Response.Error(exception)
        }
    }

    override suspend fun remove(id: String): Response<Unit> {
        return try {
            firestore.collection(Category.COLLECTION)
                .document(id)
                .delete()
                .await()
            Response.Success(Unit)
        } catch (firestoreException: FirebaseFirestoreException) {
            Response.Error(firestoreException)
        } catch (exception: Exception) {
            Response.Error(exception)
        }
    }

}