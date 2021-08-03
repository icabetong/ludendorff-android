package io.capstone.keeper.features.scan.image

import android.content.ContentUris
import android.content.Context
import android.net.Uri
import android.provider.MediaStore
import io.capstone.keeper.features.core.backend.Response
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class ImageRepository(private val context: Context) {

    suspend fun fetchImages(): Response<List<Uri>> = withContext(Dispatchers.IO) {
        return@withContext try {
            val galleryImagesUris = mutableListOf<Uri>()
            val columns = arrayOf(MediaStore.Images.Media._ID)
            val orderBy = MediaStore.Images.Media.DATE_ADDED

            context.contentResolver.query(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI, columns,
                null, null, "$orderBy DESC"
            )?.use {
                val idColumn = it.getColumnIndex(MediaStore.Images.Media._ID)

                while(it.moveToNext()) {
                    val id = it.getLong(idColumn)
                    galleryImagesUris.add(
                        ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id)
                    )
                }
            }
            Response.Success(galleryImagesUris.toList())
        } catch (exception: Exception) {
            Response.Error(exception)
        }
    }
}