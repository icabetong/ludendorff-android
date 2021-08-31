package io.capstone.ludendorff.features.assignment

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.Query
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.messaging.RemoteMessage
import com.google.firebase.messaging.RemoteMessageCreator
import io.capstone.ludendorff.features.asset.Asset
import io.capstone.ludendorff.features.core.backend.Response
import kotlinx.coroutines.withContext
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AssignmentRepository @Inject constructor(
    private val firebaseAuth: FirebaseAuth,
    private val firestore: FirebaseFirestore,
    private val firebaseMessaging: FirebaseMessaging
) {

    suspend fun create(assignment: Assignment): Response<Response.Action> {
        return try {
            firestore.runBatch {
                it.set(firestore.collection(Assignment.COLLECTION)
                    .document(assignment.assignmentId), assignment)

                assignment.asset?.assetId?.let { id ->
                    it.update(firestore.collection(Asset.COLLECTION)
                        .document(id), Asset.FIELD_STATUS, Asset.Status.OPERATIONAL)
                }
            }.await()

            Response.Success(Response.Action.CREATE)
        } catch (exception: FirebaseFirestoreException) {
            Response.Error(exception)
        } catch (exception: Exception) {
            Response.Error(exception)
        }
    }

    suspend fun update(assignment: Assignment): Response<Response.Action> {
        return try {
            firestore.collection(Assignment.COLLECTION)
                .document(assignment.assignmentId)
                .set(assignment)
                .await()

            Response.Success(Response.Action.UPDATE)
        } catch (exception: FirebaseFirestoreException) {
            Response.Error(exception)
        } catch (exception: Exception) {
            Response.Error(exception)
        }
    }

    suspend fun remove(assignment: Assignment): Response<Response.Action> {
        return try {
            firestore.runBatch {
                it.delete(firestore.collection(Assignment.COLLECTION)
                    .document(assignment.assignmentId))

                assignment.asset?.assetId?.let { id ->
                    it.update(firestore.collection(Asset.COLLECTION)
                        .document(id), Asset.FIELD_STATUS, Asset.Status.IDLE)
                }
            }.await()

            Response.Success(Response.Action.REMOVE)
        } catch (exception: FirebaseFirestoreException) {
            Response.Error(exception)
        } catch (exception: Exception) {
            Response.Error(exception)
        }
    }

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