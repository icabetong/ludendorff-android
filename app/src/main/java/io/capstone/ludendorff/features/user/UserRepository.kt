package io.capstone.ludendorff.features.user

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import io.capstone.ludendorff.components.persistence.UserProperties
import io.capstone.ludendorff.features.core.backend.Response
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserRepository @Inject constructor(
    private val firebaseAuth: FirebaseAuth,
    private val firestore: FirebaseFirestore,
    private val userProperties: UserProperties
){

    suspend fun create(user: User, password: String?): Response<Response.Action> {
        return try {
            if (user.email.isNullOrBlank() || password.isNullOrBlank())
                throw NullPointerException()

            firebaseAuth.createUserWithEmailAndPassword(user.email!!, password)
                .addOnCompleteListener {
                    if (!it.isSuccessful)
                        Response.Error(it.exception, Response.Action.CREATE)
                }.await()

            firestore.collection(User.COLLECTION)
                .document(user.userId)
                .set(user)
                .await()

            Response.Success(Response.Action.CREATE)
        } catch (firestoreException: FirebaseFirestoreException) {
            Response.Error(firestoreException, Response.Action.CREATE)
        } catch (nullPointerException: NullPointerException) {
            Response.Error(nullPointerException, Response.Action.CREATE)
        } catch (exception: Exception) {
            Response.Error(exception, Response.Action.CREATE)
        }
    }

    suspend fun update(user: User): Response<Response.Action> {
        return try {
            firestore.collection(User.COLLECTION)
                .document(user.userId)
                .set(user)
                .await()

            firebaseAuth.currentUser?.uid?.let {
                if (it == user.userId) {
                    userProperties.set(user)
                }
            }

            Response.Success(Response.Action.UPDATE)
        } catch (firestoreException: FirebaseFirestoreException) {
            Response.Error(firestoreException, Response.Action.UPDATE)
        } catch (exception: Exception) {
            Response.Error(exception, Response.Action.REMOVE)
        }
    }

    suspend fun update(id: String, fields: Map<String, Any?>): Response<Response.Action> {
        return try {
            firestore.collection(User.COLLECTION)
                .document(id)
                .update(fields)
                .await()

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
        } catch (firestoreException: FirebaseFirestoreException) {
            Response.Error(firestoreException, Response.Action.UPDATE)
        } catch (exception: Exception) {
            Response.Error(exception, Response.Action.UPDATE)
        }
    }

}