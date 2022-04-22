package io.capstone.ludendorff.features.category

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import io.capstone.ludendorff.features.core.backend.Response
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CategoryRepository @Inject constructor(
    private val firestore: FirebaseFirestore
) {

    suspend fun fetchSubcategories(categoryId: String): Response<List<String>> {
        return try {
            val snapshot = firestore.collection(Category.COLLECTION)
                .document(categoryId)
                .get()
                .await()

            val category = snapshot.toObject(Category::class.java)
            Response.Success(category?.subcategories ?: emptyList())
        } catch (exception: Exception) {
            Response.Error(exception);
        }
    }

    suspend fun create(data: Category): Response<Response.Action> {
        return try {
            firestore.collection(Category.COLLECTION)
                .document(data.categoryId)
                .set(data)
                .await()

            Response.Success(Response.Action.CREATE)
        } catch (exception: FirebaseFirestoreException) {
            Response.Error(exception, Response.Action.CREATE)
        } catch (exception: Exception) {
            Response.Error(exception, Response.Action.CREATE)
        }
    }

    suspend fun update(data: Category): Response<Response.Action> {
        return try {
            val batchWrite = firestore.batch()
            batchWrite.set(firestore.collection(Category.COLLECTION).document(data.categoryId),
                data)

//            firestore.collection(Asset.COLLECTION)
//                .whereEqualTo(Asset.FIELD_CATEGORY_ID, data.categoryId)
//                .get().await()
//                .documents.forEach {
//                    batchWrite.update(it.reference, Asset.FIELD_CATEGORY, data.minimize())
//                }

            batchWrite.commit()

            Response.Success(Response.Action.UPDATE)
        } catch (exception: FirebaseFirestoreException) {
            Response.Error(exception, Response.Action.UPDATE)
        } catch (exception: Exception) {
            Response.Error(exception, Response.Action.UPDATE)
        }
    }

    suspend fun remove(category: Category): Response<Response.Action> {
        return try {
            firestore.collection(Category.COLLECTION)
                .document(category.categoryId)
                .delete()
                .await()

            Response.Success(Response.Action.REMOVE)
        } catch (exception: FirebaseFirestoreException) {
            Response.Error(exception, Response.Action.REMOVE)
        } catch (exception: Exception) {
            Response.Error(exception, Response.Action.REMOVE)
        }
    }

}