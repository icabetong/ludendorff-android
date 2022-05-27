package io.capstone.ludendorff.features.user

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.functions.FirebaseFunctions
import io.capstone.ludendorff.api.Deshi
import io.capstone.ludendorff.api.DeshiException
import io.capstone.ludendorff.api.DeshiRequest
import io.capstone.ludendorff.components.persistence.UserProperties
import io.capstone.ludendorff.features.core.backend.Response
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserRepository @Inject constructor(
    private val firebaseAuth: FirebaseAuth,
    private val firestore: FirebaseFirestore,
    private val functions: FirebaseFunctions,
    private val userProperties: UserProperties,
    private val deshi: Deshi
){

    suspend fun create(user: User): Response<Response.Action> {
        return try {
            if (user.email.isNullOrBlank())
                throw NullPointerException()

            val token = firebaseAuth.currentUser?.getIdToken(false)?.await()?.token
                ?: throw DeshiException(DeshiException.Code.UNAUTHORIZED)
            val data = hashMapOf(
                DATA_TOKEN to token,
                User.FIELD_EMAIL to user.email,
                User.FIELD_FIRST_NAME to user.firstName,
                User.FIELD_LAST_NAME to user.lastName,
                User.FIELD_POSITION to user.position,
                User.FIELD_PERMISSIONS to user.permissions,
            )

            functions.getHttpsCallable(CREATE_CALLABLE_NAME)
                .call(data).await()
            Response.Success(Response.Action.CREATE)
        } catch (exception: Exception) {
            Response.Error(exception, Response.Action.CREATE)
        }
    }

    suspend fun update(user: User, statusChanged: Boolean): Response<Response.Action> {
        return try {
            val token = firebaseAuth?.currentUser?.getIdToken(false)?.await()?.token
            val data = hashMapOf(
                DATA_TOKEN to token,
                User.FIELD_ID to user.userId,
                User.FIELD_DISABLED to statusChanged
            )

            functions.getHttpsCallable(MODIFY_CALLABLE_NAME)
                .call(data).await()
            Response.Success(Response.Action.UPDATE)
        } catch (exception: FirebaseFirestoreException) {
            Response.Error(exception, Response.Action.UPDATE)
        } catch (exception: Exception) {
            Response.Error(exception, Response.Action.REMOVE)
        }
    }

    suspend fun update(id: String, fields: Map<String, Any?>): Response<Response.Action> {
        return try {
            val batchWrite = firestore.batch()
            batchWrite.update(firestore.collection(User.COLLECTION)
                .document(id), fields)
            batchWrite.commit().await()

            firebaseAuth.currentUser?.uid?.let {
                if (it == id) {
                    fields.forEach { (field, value) ->
                        if (value is String)
                            userProperties.set(field, value)
                        else if (value is Int)
                            userProperties.set(field, value)
                    }
                }
            }

            Response.Success(Response.Action.UPDATE)
        } catch (exception: FirebaseFirestoreException) {
            Response.Error(exception, Response.Action.UPDATE)
        } catch (exception: Exception) {
            Response.Error(exception, Response.Action.UPDATE)
        }
    }

    suspend fun remove(user: User): Response<Response.Action> {
        return try {
            val token = firebaseAuth.currentUser?.getIdToken(false)?.await()?.token
                ?: throw DeshiException(DeshiException.Code.UNAUTHORIZED)
            val data = hashMapOf(
                DATA_TOKEN to token,
                User.FIELD_ID to user.userId,
            )

            functions.getHttpsCallable(DELETE_CALLABLE_NAME)
                .call(data).await()
            Response.Success(Response.Action.REMOVE)
        } catch (exception: Exception) {
            Response.Error(exception, Response.Action.REMOVE)
        }
    }

    companion object {
        const val CREATE_CALLABLE_NAME = "createUser"
        const val MODIFY_CALLABLE_NAME = "modifyUser"
        const val DELETE_CALLABLE_NAME = "deleteUser"
        const val DATA_TOKEN = "token"
    }
}