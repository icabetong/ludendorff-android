package io.capstone.keeper.features.core.worker

import android.content.Context
import android.net.Uri
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageException
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.io.File

@HiltWorker
class ProfileUploadWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted workerParameters: WorkerParameters,
    private val storage: FirebaseStorage,
    private val auth: FirebaseAuth
): CoroutineWorker(context, workerParameters) {

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        return@withContext try {
            val path = inputData.getString(EXTRA_SOURCE)

            if (auth.uid != null && path != null) {
                val image = File(path)

                val ref = storage.reference.child(BUCKET_NAME)
                    .child("${auth.uid!!}.${image.extension}")
                    .putFile(Uri.fromFile(image))
                    .addOnProgressListener {
                        val currentProgress = (100 * it.bytesTransferred) / it.totalByteCount

                        setProgressAsync(workDataOf(TASK_PROGRESS to currentProgress))
                    }.await()

                val url = ref.storage.downloadUrl.await().toString()

                Result.success(workDataOf(EXTRA_URL to url))
            } else Result.failure()
        } catch (storageException: StorageException) {
            Result.failure()
        } catch (exception: Exception) {
            Result.failure()
        }
    }

    companion object {
        const val BUCKET_NAME = "profiles"
        const val WORKER_TAG = "worker:upload"
        const val TASK_PROGRESS = "task:progress"
        const val EXTRA_SOURCE = "extra:source"
        const val EXTRA_URL = "extra:url"
    }
}