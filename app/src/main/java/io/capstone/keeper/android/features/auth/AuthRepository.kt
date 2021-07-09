package io.capstone.keeper.android.features.auth

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import io.capstone.keeper.android.components.exceptions.EmptyCredentialsException
import io.capstone.keeper.android.features.core.Response
import io.capstone.keeper.android.features.user.User
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepository @Inject constructor(
    private val firebaseAuth: FirebaseAuth,
    private val firestore: FirebaseFirestore
){

    private suspend fun fetchUserProperties(id: String): Response<User> {
        return try {
            val task = firestore.collection(User.COLLECTION_NAME).document(id).get().await()
            if (task != null) {
                val user = task.toObject(User::class.java)
                if (user != null)
                    Response.Success(user)
                else Response.Error(NullPointerException())
            } else Response.Error(NullPointerException())
        } catch (e: Exception) {
            Response.Error(e)
        }
    }

    suspend fun authenticate(email: String, password: String): Response<User> {
        return try {
            return if (email.isNotBlank() && password.isNotBlank()) {

                val task = firebaseAuth.signInWithEmailAndPassword(email, password).await()
                return if (task.user != null) {
                    fetchUserProperties(task.user!!.uid)
                } else Response.Error(Exception())

            } else Response.Error(EmptyCredentialsException())

        } catch (invalidUserException: FirebaseAuthInvalidUserException) {
            Response.Error(invalidUserException)

        } catch (invalidCredentialsException: FirebaseAuthInvalidCredentialsException) {
            Response.Error(invalidCredentialsException)

        } catch (e: Exception) {
            Response.Error(e)
        }
    }

    fun endSession() = firebaseAuth.signOut()
    fun checkCurrentUser(): FirebaseUser? = firebaseAuth.currentUser

}