package io.capstone.ludendorff.features.request

import com.google.firebase.firestore.FirebaseFirestore
import io.capstone.ludendorff.features.core.backend.Response
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RequestRepository @Inject constructor(
    private val firestore: FirebaseFirestore
) {

    suspend fun create(request: Request): Response<Response.Action> {
        return try {
            firestore.collection(Request.COLLECTION)
                .document(request.requestId)
                .set(request).await()

            Response.Success(Response.Action.CREATE)

        } catch (exception: Exception) {
            Response.Error(exception, Response.Action.CREATE)
        }
    }

    suspend fun update(request: Request): Response<Response.Action> {
        return try {
            firestore.collection(Request.COLLECTION)
                .document(request.requestId)
                .set(request).await()

            Response.Success(Response.Action.UPDATE)

        } catch (exception: Exception) {
            Response.Error(exception, Response.Action.UPDATE)
        }
    }

    suspend fun update(requestId: String, fields: Map<String, Any?>): Response<Response.Action> {
        return try {
            firestore.collection(Request.COLLECTION)
                .document(requestId)
                .update(fields).await()

            Response.Success(Response.Action.UPDATE)

        } catch (exception: Exception) {
            Response.Error(exception, Response.Action.UPDATE)
        }
    }

    suspend fun remove(request: Request): Response<Response.Action> {
        return try {
            firestore.collection(Request.COLLECTION)
                .document(request.requestId)
                .delete().await()

            Response.Success(Response.Action.REMOVE)

        } catch (exception: Exception) {
            Response.Error(exception, Response.Action.REMOVE)
        }
    }
}