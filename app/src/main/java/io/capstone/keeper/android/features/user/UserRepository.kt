package io.capstone.keeper.android.features.user

import com.google.firebase.firestore.FirebaseFirestore
import io.capstone.keeper.android.features.core.Response
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserRepository @Inject constructor(
    val firestore: FirebaseFirestore
){

    suspend fun fetch(): Response<List<User>> {
        return try {
            val users = firestore.collection(COLLECTION_NAME).get().await()
            if (users != null) {
                val items = users.toObjects(User::class.java)
                Response.Success(items)
            } else Response.Error(Exception())
        } catch (e: Exception) {
            Response.Error(e)
        }
    }

    suspend fun fetchSpecificUser(id: String): Response<User> {
        return try {
            val task = firestore.collection(COLLECTION_NAME).document(id).get().await()
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

    companion object {
        const val COLLECTION_NAME = "users"
    }
}