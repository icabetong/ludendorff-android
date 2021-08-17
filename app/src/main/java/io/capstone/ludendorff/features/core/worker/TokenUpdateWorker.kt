package io.capstone.ludendorff.features.core.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.google.firebase.auth.FirebaseAuth
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import io.capstone.ludendorff.features.core.backend.Response
import io.capstone.ludendorff.features.user.User
import io.capstone.ludendorff.features.user.UserRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@HiltWorker
class TokenUpdateWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted workerParameters: WorkerParameters,
    private val firebaseAuth: FirebaseAuth,
    private val userRepository: UserRepository
) : CoroutineWorker(context, workerParameters) {

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        val tokenId = inputData.getString(EXTRA_TOKEN_ID)

        firebaseAuth.currentUser?.uid?.let {
            val response = userRepository.update(it, mapOf(User.FIELD_TOKEN_ID to tokenId))

            if (response is Response.Success)
                return@withContext Result.success()
        }

        return@withContext Result.failure()
    }

    companion object {
        const val WORKER_TAG = "worker:token"
        const val EXTRA_TOKEN_ID = "extra:token_id"
    }
}