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
}