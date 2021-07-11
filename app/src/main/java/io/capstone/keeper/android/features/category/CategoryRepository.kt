package io.capstone.keeper.android.features.category

import com.google.firebase.firestore.FirebaseFirestore
import io.capstone.keeper.android.features.core.Response
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class CategoryRepository @Inject constructor(
    private val firestore: FirebaseFirestore
) {

    suspend fun insert(category: Category): Response<Unit> {
        return try {
            firestore.collection(COLLECTION_NAME).document(category.categoryId)
                .set(category).await()
            Response.Success(Unit)
        } catch (e: Exception) {
            Response.Error(e)
        }
    }

    companion object  {
        const val COLLECTION_NAME = "categories"
    }
}