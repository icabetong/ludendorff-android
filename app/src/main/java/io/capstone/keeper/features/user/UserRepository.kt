package io.capstone.keeper.features.user

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import io.capstone.keeper.components.persistence.UserProperties
import io.capstone.keeper.features.core.data.Response
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserRepository @Inject constructor(
    private val firebaseAuth: FirebaseAuth,
    private val firestore: FirebaseFirestore,
    private val userProperties: UserProperties
){

    suspend fun create(user: User): Response<Unit> {
        return try {
            firestore.collection(User.COLLECTION)
                .document(user.userId)
                .set(user)
                .await()

            Response.Success(Unit)
        } catch (firestoreException: FirebaseFirestoreException) {
            Response.Error(firestoreException)
        } catch (exception: Exception) {
            Response.Error(exception)
        }
    }

    suspend fun update(user: User): Response<Unit> {
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

            Response.Success(Unit)
        } catch (firestoreException: FirebaseFirestoreException) {
            Response.Error(firestoreException)
        } catch (exception: Exception) {
            Response.Error(exception)
        }
    }

    suspend fun update(id: String, fields: Map<String, Any?>): Response<Unit> {
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

            Response.Success(Unit)
        } catch (firestoreException: FirebaseFirestoreException) {
            Response.Error(firestoreException)
        } catch (exception: Exception) {
            Response.Error(exception)
        }
    }
}