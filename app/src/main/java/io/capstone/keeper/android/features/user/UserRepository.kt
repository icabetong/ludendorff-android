package io.capstone.keeper.android.features.user

import com.google.firebase.firestore.FirebaseFirestore
import io.capstone.keeper.android.features.core.Response
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class UserRepository @Inject constructor(
    val firestore: FirebaseFirestore
){

    suspend fun fetchUsers(): Response<List<User>> {
        return try {
            val users = firestore.collection(User.COLLECTION_NAME).get().await()
            if (users != null) {
                val items = users.toObjects(User::class.java)
                Response.Success(items)
            } else Response.Error(Exception())
        } catch (e: Exception) {
            Response.Error(e)
        }
    }
}