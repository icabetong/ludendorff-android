package io.capstone.keeper.android.features.core.data

import androidx.lifecycle.LiveData
import com.google.firebase.firestore.*
import io.capstone.keeper.android.features.category.Category
import java.lang.NullPointerException
import javax.inject.Singleton

class FirestoreLiveData<T>(
    var typeClass: Class<T>,
    var query: Query,
    var onLastCategoryReachedCallback: OnLastCategoryReachedCallback,
    var onLastVisibleCategoryCallback: OnLastVisibleCategoryCallback
): LiveData<Response<List<T?>>?>(), EventListener<QuerySnapshot> {

    private lateinit var listenerRegistration: ListenerRegistration

    init {
        value = Response.InProgress()
    }

    override fun onActive() {
        super.onActive()
        listenerRegistration = query.addSnapshotListener(this)
    }

    override fun onInactive() {
        super.onInactive()
        listenerRegistration.remove()
    }

    interface OnLastVisibleCategoryCallback {
        fun setLastVisibleCategoryCallback(lastVisibleProduct: DocumentSnapshot?)
    }
    interface OnLastCategoryReachedCallback {
        fun setOnLastCategoryReached(isLastCategoryReached: Boolean)
    }

    @Singleton
    interface FirestoreRepository<T> {
        fun fetch(): FirestoreLiveData<T>?
        suspend fun insert(t: T): Response<Unit>
        suspend fun update(t: T): Response<Unit>
        suspend fun remove(id: String): Response<Unit>
    }

    override fun onEvent(querySnapshot: QuerySnapshot?, error: FirebaseFirestoreException?) {
        if (error != null) {
            value = Response.Error(error)
            return
        }

        val items: List<T?> = querySnapshot?.documents?.map {
            it.toObject(typeClass)
        } ?: emptyList()
        value = Response.Success(items)

        val querySnapshotSize: Int = querySnapshot?.size() ?: 0
        if (querySnapshotSize < QUERY_SIZE_LIMIT) {
            onLastCategoryReachedCallback.setOnLastCategoryReached(true)
        } else {
            val lastVisibleCategory = querySnapshot?.documents?.get(querySnapshotSize - 1)
            onLastVisibleCategoryCallback.setLastVisibleCategoryCallback(lastVisibleCategory)
        }
    }

    companion object {
        const val QUERY_SIZE_LIMIT = 15L
    }
}