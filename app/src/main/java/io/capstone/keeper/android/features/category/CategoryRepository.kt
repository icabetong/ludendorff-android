package io.capstone.keeper.android.features.category

import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CategoryRepository @Inject constructor(
    firestore: FirebaseFirestore
) {
    private val collection = firestore.collection(COLLECTION_NAME)

    private var lastDocument: DocumentSnapshot? = null
    private var query: Query = collection.orderBy(Category.FIELD_NAME, Query.Direction.ASCENDING)

    companion object  {
        const val COLLECTION_NAME = "categories"
    }
}