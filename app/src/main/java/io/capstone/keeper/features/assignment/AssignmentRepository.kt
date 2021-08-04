package io.capstone.keeper.features.assignment

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.Query
import io.capstone.keeper.features.core.backend.Response
import kotlinx.coroutines.withContext
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AssignmentRepository @Inject constructor(
    private val firestore: FirebaseFirestore
) {

    suspend fun fetchUsingAssetId(assetId: String): Response<Assignment?> = withContext(IO) {
        return@withContext try {
            val task = firestore.collection(Assignment.COLLECTION)
                .whereEqualTo(Assignment.FIELD_ASSET_ID, assetId)
                .orderBy(Assignment.FIELD_ID, Query.Direction.ASCENDING)
                .get().await()

            if (task.isEmpty)
                Response.Success(null)
            else Response.Success(Assignment.from(task.first()))
        } catch(exception: FirebaseFirestoreException) {
            Response.Error(exception)
        } catch(exception: Exception) {
            Response.Error(exception)
        }
    }
}