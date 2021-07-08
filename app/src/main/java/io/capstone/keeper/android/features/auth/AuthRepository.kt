package io.capstone.keeper.android.features.auth

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.FirebaseUser
import io.capstone.keeper.android.features.core.Response
import io.capstone.keeper.android.features.user.User
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepository @Inject constructor(
    private val firebaseAuth: FirebaseAuth
){

    suspend fun authenticate(email: String, password: String): Response<User> {
        return try {
            val task = firebaseAuth.signInWithEmailAndPassword(email, password).await()
            return if (task.user != null) {
                val user = User(task.user!!.uid, email = task.user!!.email, isAuthenticated = true)
                Response.Success(user)
            } else Response.Error(Exception())

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