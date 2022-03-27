package io.capstone.ludendorff.features.type

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import io.capstone.ludendorff.features.core.backend.Response
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TypeRepository @Inject constructor(
    private val firestore: FirebaseFirestore
) {

    suspend fun create(data: Type): Response<Response.Action> {
        return try {
            firestore.collection(Type.COLLECTION)
                .document(data.typeId)
                .set(data)
                .await()

            Response.Success(Response.Action.CREATE)
        } catch (exception: FirebaseFirestoreException) {
            Response.Error(exception, Response.Action.CREATE)
        } catch (exception: Exception) {
            Response.Error(exception, Response.Action.CREATE)
        }
    }

    suspend fun update(data: Type): Response<Response.Action> {
        return try {
            val batchWrite = firestore.batch()
            batchWrite.set(firestore.collection(Type.COLLECTION).document(data.typeId),
                data)

//            firestore.collection(Asset.COLLECTION)
//                .whereEqualTo(Asset.FIELD_CATEGORY_ID, data.categoryId)
//                .get().await()
//                .documents.forEach {
//                    batchWrite.update(it.reference, Asset.FIELD_CATEGORY, data.minimize())
//                }

            batchWrite.commit()

            Response.Success(Response.Action.UPDATE)
        } catch (exception: FirebaseFirestoreException) {
            Response.Error(exception, Response.Action.UPDATE)
        } catch (exception: Exception) {
            Response.Error(exception, Response.Action.UPDATE)
        }
    }

    suspend fun remove(type: Type): Response<Response.Action> {
        return try {
            firestore.collection(Type.COLLECTION)
                .document(type.typeId)
                .delete()
                .await()

            Response.Success(Response.Action.REMOVE)
        } catch (exception: FirebaseFirestoreException) {
            Response.Error(exception, Response.Action.REMOVE)
        } catch (exception: Exception) {
            Response.Error(exception, Response.Action.REMOVE)
        }
    }

}