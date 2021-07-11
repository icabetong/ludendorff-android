package io.capstone.keeper.android.features.auth

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import io.capstone.keeper.android.components.exceptions.EmptyCredentialsException
import io.capstone.keeper.android.features.core.Response
import io.capstone.keeper.android.features.user.User
import io.capstone.keeper.android.features.user.UserRepository
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepository @Inject constructor(
    private val firebaseAuth: FirebaseAuth,
    private val userRepository: UserRepository
){

    suspend fun authenticate(email: String, password: String): Response<User> {
        return try {
            return if (email.isNotBlank() && password.isNotBlank()) {
                /**
                 *  Use Kotlin's coroutines to wait for completion of Firebase Auth
                 *  callbacks, that way we can return the result to the viewmodel
                 *  and perform actions with the result
                 */
                val task = firebaseAuth.signInWithEmailAndPassword(email, password).await()
                return if (task.user != null) {
                    /**
                     *  The user has been successfully authenticated,
                     *  proceed in fetching his information that
                     *  will be used by the application.
                     */
                    userRepository.fetchSpecificUser(task.user!!.uid)
                } else Response.Error(Exception())
            } else {
                /**
                 *  Return a custom exception that specifies that
                 *  the user has no credentials inputted in the fields
                 *  provided.
                 */
                Response.Error(EmptyCredentialsException())
            }

        } catch (invalidUserException: FirebaseAuthInvalidUserException) {
            /**
             *  Exception raised when the user doesn't yet exists
             *  or the user account has been disabled.
             */
            Response.Error(invalidUserException)

        } catch (invalidCredentialsException: FirebaseAuthInvalidCredentialsException) {
            /**
             *  Exception raised when invalid credentials are inputted
             *  by the user, either email, username or password
             */
            Response.Error(invalidCredentialsException)

        } catch (e: Exception) {
            Response.Error(e)
        }
    }

    fun endSession() = firebaseAuth.signOut()
    fun checkCurrentUser(): FirebaseUser? = firebaseAuth.currentUser

}