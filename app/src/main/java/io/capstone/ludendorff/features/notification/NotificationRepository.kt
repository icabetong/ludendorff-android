package io.capstone.ludendorff.features.notification

import com.google.firebase.firestore.FirebaseFirestore
import io.capstone.ludendorff.features.core.backend.Response
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NotificationRepository @Inject constructor(
    private val firestore: FirebaseFirestore
) {

    suspend fun remove(notification: Notification): Response<Response.Action> {
        return try {
            firestore.collection(Notification.COLLECTION)
                .document(notification.notificationId)
                .delete()
                .await()

            Response.Success(Response.Action.REMOVE)
        } catch (exception: Exception) {
            Response.Error(exception, Response.Action.REMOVE)
        }
    }
}