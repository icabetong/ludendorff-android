package io.capstone.ludendorff.features.core.entity

import com.google.firebase.firestore.FirebaseFirestore
import io.capstone.ludendorff.features.core.backend.Response
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class EntityRepository @Inject constructor(
    private val firestore: FirebaseFirestore
) {

    suspend fun fetch(): Response<Entity> {
        return try {
            val task = firestore.collection(Response.CORE_COLLECTION)
                .document(Entity.DOCUMENT_KEY)
                .get()
                .await()

            if (task != null) {
                val entity = task.toObject(Entity::class.java)
                if (entity != null) Response.Success(entity)
                else Response.Error(Exception())
            } else Response.Error(Exception())
        } catch (exception: Exception) {
            Response.Error(exception)
        }
    }

    suspend fun update(entity: Entity): Response<Response.Action> {
        return try {
            firestore.collection(Response.CORE_COLLECTION)
                .document(Entity.DOCUMENT_KEY)
                .set(entity)
                .await()

            Response.Success(Response.Action.UPDATE)
        } catch (exception: Exception) {
            Response.Error(exception)
        }
    }
}