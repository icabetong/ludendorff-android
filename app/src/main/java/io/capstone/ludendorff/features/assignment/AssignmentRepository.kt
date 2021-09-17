package io.capstone.ludendorff.features.assignment

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.Query
import io.capstone.ludendorff.api.Deshi
import io.capstone.ludendorff.api.DeshiException
import io.capstone.ludendorff.api.DeshiRequest
import io.capstone.ludendorff.components.persistence.UserProperties
import io.capstone.ludendorff.features.asset.Asset
import io.capstone.ludendorff.features.core.backend.Response
import io.capstone.ludendorff.features.notification.Notification
import io.capstone.ludendorff.features.user.User
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import okhttp3.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AssignmentRepository @Inject constructor(
    private val userProperties: UserProperties,
    private val firebaseAuth: FirebaseAuth,
    private val firestore: FirebaseFirestore,
    private val deshi: Deshi
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

            val token = firebaseAuth.currentUser?.getIdToken(false)?.await()?.token
            if (token == null)
                throw DeshiException(DeshiException.Code.UNAUTHORIZED)
            else if (assignment.user?.deviceToken == null)
                throw DeshiException(DeshiException.Code.PRECONDITION_FAILED)

            val request = DeshiRequest(token).apply {
                put(User.FIELD_DEVICE_TOKEN, assignment.user?.deviceToken)
                put(Notification.FIELD_TITLE, Notification.NOTIFICATION_ASSIGNED_TITLE)
                put(Notification.FIELD_BODY, Notification.NOTIFICATION_ASSIGNED_BODY)
                put(Notification.FIELD_PAYLOAD, assignment.assignmentId)
                put(Notification.FIELD_SENDER_ID, firebaseAuth.currentUser?.uid)
                put(Notification.FIELD_RECEIVER_ID, assignment.user?.userId)
                putExtras(mapOf(
                    Notification.EXTRA_SENDER to userProperties.getDisplayName(),
                    Notification.EXTRA_TARGET to assignment.asset?.assetName
                ))
            }

            val response = deshi.newNotificationPost(request)
            if (response.code() == 200)
                Response.Success(Response.Action.CREATE)
            else throw DeshiException(response.code())

        } catch (exception: FirebaseFirestoreException) {
            Response.Error(exception, Response.Action.CREATE)
        } catch (exception: Exception) {
            Response.Error(exception, Response.Action.CREATE)
        }
    }

    suspend fun update(assignment: Assignment,
                       previousUserId: String?,
                       previousAssetId: String?): Response<Response.Action> {
        return try {
            firestore.runBatch {
                it.set(firestore.collection(Assignment.COLLECTION)
                    .document(assignment.assignmentId), assignment)

                /**
                 * The assets specified have been changed, we need to change
                 * the status of the previous and the new one accordingly.
                 */
                if (previousAssetId != null &&
                        previousAssetId != assignment.asset?.assetId) {
                    it.update(firestore.collection(Asset.COLLECTION)
                        .document(previousAssetId), Asset.FIELD_STATUS, Asset.Status.IDLE)

                    assignment.asset?.assetId?.let { newAssetId ->
                        it.update(firestore.collection(Asset.COLLECTION)
                            .document(newAssetId), Asset.FIELD_STATUS,
                            Asset.Status.OPERATIONAL)
                    }
                }
            }.await()

            /**
             * The user hasn't been requested to be changed
             * so we'll finish the operation now.
             */
            if (previousUserId == null ||
                    previousUserId == assignment.user?.userId)
                        Response.Success(Response.Action.UPDATE)

            /**
             * The user has been changed, we'll request
             * the Deshi to send a notification to the new user.
             * We'll need the id token of the current user so that
             * he's/she's identity we'll be verified by Deshi
             */
            val token = firebaseAuth.currentUser?.getIdToken(false)?.await()?.token
            if (token == null)
                throw DeshiException(DeshiException.Code.UNAUTHORIZED)
            else if (assignment.user?.deviceToken == null)
                throw DeshiException(DeshiException.Code.PRECONDITION_FAILED)

            val request = DeshiRequest(token).apply {
                put(User.FIELD_DEVICE_TOKEN, assignment.user?.deviceToken)
                put(Notification.FIELD_TITLE, Notification.NOTIFICATION_ASSIGNED_TITLE)
                put(Notification.FIELD_BODY, Notification.NOTIFICATION_ASSIGNED_BODY)
                put(Notification.FIELD_PAYLOAD, assignment.assignmentId)
                put(Notification.FIELD_SENDER_ID, firebaseAuth.currentUser?.uid)
                put(Notification.FIELD_RECEIVER_ID, assignment.user?.userId)
                putExtras(mapOf(
                    Notification.EXTRA_SENDER to userProperties.getDisplayName(),
                    Notification.EXTRA_TARGET to assignment.asset?.assetName
                ))
            }

            val response = deshi.newNotificationPost(request)
            if (response.code() == 200)
                Response.Success(Response.Action.CREATE)
            else throw DeshiException(response.code())

        } catch (exception: FirebaseFirestoreException) {
            Response.Error(exception, Response.Action.UPDATE)
        } catch (exception: Exception) {
            Response.Error(exception, Response.Action.UPDATE)
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