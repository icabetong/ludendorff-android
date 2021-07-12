package io.capstone.keeper.android.features.category

import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.Query
import io.capstone.keeper.android.features.core.data.FirestoreLiveData
import io.capstone.keeper.android.features.core.data.Response
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CategoryRepository @Inject constructor(
    firestore: FirebaseFirestore
):  FirestoreLiveData.FirestoreRepository<Category>,
    FirestoreLiveData.OnLastCategoryReachedCallback,
    FirestoreLiveData.OnLastVisibleCategoryCallback {

    private val collection = firestore.collection(COLLECTION_NAME)

    private var query: Query = collection.orderBy(Category.FIELD_NAME, Query.Direction.ASCENDING)
        .limit(FirestoreLiveData.QUERY_SIZE_LIMIT)
    private var lastVisibleCategory: DocumentSnapshot? = null
    private var isLastCategoryReached: Boolean = false

    override suspend fun insert(t: Category): Response<Unit> {
        return try {
            collection.document(t.categoryId)
                .set(t).await()
            return Response.Success(Unit)
        } catch (firestoreException: FirebaseFirestoreException) {
            Response.Error(firestoreException)
        } catch (exception: Exception) {
            Response.Error(exception)
        }
    }

    override suspend fun update(t: Category): Response<Unit> {
        return try {
            collection.document(t.categoryId)
                .set(t).await()

            Response.Success(Unit)
        } catch (firestoreException: FirebaseFirestoreException) {
            Response.Error(firestoreException)
        } catch (exception: Exception) {
            Response.Error(exception)
        }
    }

    override suspend fun remove(id: String): Response<Unit> {
        return try {
            collection.document(id).delete().await()
            Response.Success(Unit)
        } catch (firestoreException: FirebaseFirestoreException) {
            Response.Error(firestoreException)
        } catch (exception: Exception) {
            Response.Error(exception)
        }
    }

    override fun fetch(): FirestoreLiveData<Category>? {
        if (isLastCategoryReached) return null

        if (lastVisibleCategory != null)
            query = query.startAfter(lastVisibleCategory)

        return FirestoreLiveData(Category::class.java, query, this, this)
    }

    override fun setLastVisibleCategoryCallback(lastVisibleProduct: DocumentSnapshot?) {
        this.lastVisibleCategory = lastVisibleProduct
    }

    override fun setOnLastCategoryReached(isLastCategoryReached: Boolean) {
        this.isLastCategoryReached = isLastCategoryReached
    }

    companion object  {
        const val COLLECTION_NAME = "categories"
    }
}