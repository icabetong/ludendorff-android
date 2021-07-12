package io.capstone.keeper.android.features.asset

import com.google.firebase.firestore.FirebaseFirestore
import io.capstone.keeper.android.features.core.data.Response
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class AssetRepository @Inject constructor(
    private val firestore: FirebaseFirestore
) {

    suspend fun fetchSpecific(assetId: String): Response<Asset> {
        return try {
            val task = firestore.collection(COLLECTION_NAME).document(assetId)
                .get().await()
            if (task != null) {
                val asset = task.toObject(Asset::class.java)
                if (asset != null) {
                    Response.Success(asset)
                } else Response.Error(Exception())
            } else Response.Error(Exception())
        } catch (e: Exception) {
            Response.Error(e)
        }
    }

    suspend fun insert(asset: Asset): Response<Unit> {
        return try {
            firestore.collection(COLLECTION_NAME).document(asset.assetId)
                .set(asset).await()
            Response.Success(Unit)
        } catch (e: Exception) {
            Response.Error(e)
        }
    }

    companion object {
        const val COLLECTION_NAME = "assets"
    }
}