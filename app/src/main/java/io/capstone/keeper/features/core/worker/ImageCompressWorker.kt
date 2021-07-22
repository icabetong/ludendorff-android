package io.capstone.keeper.features.core.worker

import android.content.Context
import android.net.Uri
import androidx.core.net.toFile
import androidx.hilt.work.HiltWorker
import androidx.work.*
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import id.zelory.compressor.Compressor
import kotlinx.coroutines.*
import org.apache.commons.io.IOUtils
import java.io.File

@HiltWorker
class ImageCompressWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted workerParameters: WorkerParameters
): CoroutineWorker(context, workerParameters) {

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        return@withContext try {
            val uri = Uri.parse(inputData.getString(EXTRA_SOURCE))

            val file = File(applicationContext.applicationInfo.dataDir, FILE_TEMP_NAME)
            applicationContext.contentResolver.openInputStream(uri).use {
                IOUtils.copy(it, file.outputStream())
            }

            val compressed = Compressor.compress(applicationContext, file)

            val data = Data.Builder()
                .putString(EXTRA_IMAGE, compressed.path)
                .build()

            Result.success(data)
        } catch (exception: Exception) {
            Result.failure()
        }
    }

    companion object {
        const val WORKER_TAG = "worker:compress"
        const val EXTRA_SOURCE = "extra:source"
        const val EXTRA_IMAGE = "extra:image"
        const val FILE_TEMP_NAME = "temp.jpg"
    }
}