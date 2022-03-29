package io.capstone.ludendorff.features.auth

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.FirebaseUser
import io.capstone.ludendorff.components.exceptions.EmptyCredentialsException
import io.capstone.ludendorff.features.core.backend.Response
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepository @Inject constructor(
    private val firebaseAuth: FirebaseAuth,
){

    suspend fun authenticateAsGuest(): Response<Unit> {
        return try {
            val task = firebaseAuth.signInAnonymously().await()
            if (task != null)
                Response.Success(Unit)
            else throw Exception()
        } catch (exception: Exception) {
            Response.Error(exception);
        }
    }

    suspend fun authenticate(email: String, password: String): Response<Unit> {
        return try {
            return if (email.isNotBlank() && password.isNotBlank()) {
                /**
                 *  Use Kotlin's coroutines to wait for completion of Firebase Auth
                 *  callbacks, that way we can return the result to the viewmodel
                 *  and perform actions with the result
                 */
                val task = firebaseAuth.signInWithEmailAndPassword(email, password).await()
                if (task != null)
                    Response.Success(Unit)
                else throw Exception()

            } else {
                /**
                 *  Return a custom exception that specifies that
                 *  the user has no credentials inputted in the fields
                 *  provided.
                 */
                Response.Error(EmptyCredentialsException())
            }

        } catch (exception: FirebaseAuthInvalidUserException) {
            /**
             *  Exception raised when the user doesn't yet exists
             *  or the user account has been disabled.
             */
            Response.Error(exception)

        } catch (exception: FirebaseAuthInvalidCredentialsException) {
            /**
             *  Exception raised when invalid credentials are inputted
             *  by the user, either email, username or password
             */
            Response.Error(exception)

        } catch (exception: Exception) {
            Response.Error(exception)
        }
    }

    fun checkCurrentUser(): FirebaseUser? = firebaseAuth.currentUser

}