package io.capstone.ludendorff.features.user

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import io.capstone.ludendorff.api.Deshi
import io.capstone.ludendorff.api.DeshiException
import io.capstone.ludendorff.api.DeshiRequest
import io.capstone.ludendorff.components.persistence.UserProperties
import io.capstone.ludendorff.features.assignment.Assignment
import io.capstone.ludendorff.features.core.backend.Response
import io.capstone.ludendorff.features.department.Department
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserRepository @Inject constructor(
    private val firebaseAuth: FirebaseAuth,
    private val firestore: FirebaseFirestore,
    private val userProperties: UserProperties,
    private val deshi: Deshi
){

    suspend fun create(user: User): Response<Response.Action> {
        return try {
            if (user.email.isNullOrBlank())
                throw NullPointerException()

            val token = firebaseAuth.currentUser?.getIdToken(false)?.await()?.token
                ?: throw DeshiException(DeshiException.Code.UNAUTHORIZED)

            val request = DeshiRequest(token, user.toJSON())
            val response = deshi.requestUserCreate(request)
            response.close()
            if (response.code == 200)
                Response.Success(Response.Action.CREATE)
            else throw DeshiException(response.code)

        } catch (exception: Exception) {
            Response.Error(exception, Response.Action.CREATE)
        }
    }

    suspend fun update(user: User, statusChanged: Boolean): Response<Response.Action> {
        return try {
            val batchWrite = firestore.batch()

            batchWrite.set(firestore.collection(User.COLLECTION)
                .document(user.userId), user)

            if (user.department != null) {
                firestore.collection(Department.COLLECTION)
                    .whereEqualTo(Department.FIELD_MANAGER_ID, user.userId)
                    .get().await()
                    .documents.forEach {
                        val department = it.toObject(Department::class.java)

                        if (department?.departmentId != user.department?.departmentId)
                            batchWrite.update(it.reference, Department.FIELD_MANAGER, null)
                    }
            }

            firestore.collection(Assignment.COLLECTION)
                .whereEqualTo(Assignment.FIELD_USER_ID, user.userId)
                .get().await()
                .documents.forEach {
                    batchWrite.update(it.reference, Assignment.FIELD_USER, user.minimize())
                }

            firestore.collection(Request.COLLECTION)
                .whereEqualTo(Request.FIELD_PETITIONER_ID, user.userId)
                .get().await()
                .documents.forEach {
                    batchWrite.update(it.reference, Request.FIELD_PETITIONER, user.minimize())
                }

            if (user.hasPermission(User.PERMISSION_ADMINISTRATIVE)) {
                firestore.collection(Request.COLLECTION)
                    .whereEqualTo(Request.FIELD_ENDORSER_ID, user.userId)
                    .get().await()
                    .documents.forEach {
                        batchWrite.update(it.reference, Request.FIELD_ENDORSER, user.minimize())
                    }
            }

            batchWrite.commit().await()

            if (statusChanged) {
                val token = firebaseAuth.currentUser?.getIdToken(false)?.await()?.token
                    ?: throw DeshiException(DeshiException.Code.UNAUTHORIZED)

                val request = DeshiRequest(token).apply {
                    put(User.FIELD_ID, user.userId)
                    put(User.FIELD_DISABLED, user.disabled)
                }
                val response = deshi.requestUserModify(request)
                response.close()
                if (response.code != 200) {
                    throw DeshiException(response.code)
                }

                firebaseAuth.currentUser?.uid?.let {
                    if (it == user.userId) {
                        userProperties.set(user)
                    }
                }
            }

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

            firestore.collection(Assignment.COLLECTION)
                .whereEqualTo(Assignment.FIELD_USER_ID, id)
                .get().await()
                .documents.forEach {
                    batchWrite.update(it.reference, Assignment.FIELD_USER, fields)
                }

            firestore.collection(Request.COLLECTION)
                .whereEqualTo(Request.FIELD_PETITIONER_ID, id)
                .get().await()
                .documents.forEach {
                    batchWrite.update(it.reference, Request.FIELD_PETITIONER, fields)
                }

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

            val request = DeshiRequest(token).apply {
                put(User.FIELD_ID, user.userId)
            }

            val response = deshi.requestUserRemove(request)
            response.close()
            if (response.code == 200)
                Response.Success(Response.Action.REMOVE)
            else throw DeshiException(response.code)

        } catch (exception: Exception) {
            Response.Error(exception, Response.Action.REMOVE)
        }
    }
}