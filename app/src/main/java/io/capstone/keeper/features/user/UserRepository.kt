package io.capstone.keeper.features.user

import com.google.firebase.firestore.FirebaseFirestore
import io.capstone.keeper.features.core.data.Response
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

    companion object {
        const val COLLECTION_NAME = "users"
    }
}