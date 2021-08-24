package io.capstone.ludendorff.features.department

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import io.capstone.ludendorff.features.core.backend.Response
import io.capstone.ludendorff.features.user.User
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DepartmentRepository @Inject constructor(
    private val firestore: FirebaseFirestore
) {

    suspend fun create(department: Department): Response<Response.Action> {
        return try {
            val batchWrite = firestore.batch()

            batchWrite.set(firestore.collection(Department.COLLECTION)
                .document(department.departmentId), department)

            if (department.manager != null)
                batchWrite.update(firestore.collection(User.COLLECTION)
                    .document(department.manager!!.userId), User.FIELD_DEPARTMENT,
                    department.minimize())

            batchWrite.commit().await()

            Response.Success(Response.Action.CREATE)
        } catch (firestoreException: FirebaseFirestoreException) {
            Response.Error(firestoreException, Response.Action.CREATE)
        } catch (exception: Exception) {
            Response.Error(exception, Response.Action.CREATE)
        }
    }

    suspend fun update(department: Department): Response<Response.Action> {
        return try {
            val batchWrite = firestore.batch()

            batchWrite.set(firestore.collection(Department.COLLECTION)
                .document(department.departmentId), department)

            if (department.manager != null)
                batchWrite.update(firestore.collection(User.COLLECTION)
                    .document(department.manager!!.userId), User.FIELD_DEPARTMENT,
                    department.minimize())

            batchWrite.commit().await()

            Response.Success(Response.Action.UPDATE)
        } catch (firestore: FirebaseFirestoreException) {
            Response.Error(firestore, Response.Action.UPDATE)
        } catch (exception: Exception) {
            Response.Error(exception, Response.Action.UPDATE)
        }
    }

    suspend fun remove(department: Department): Response<Response.Action> {
        return try {
            firestore.collection(Department.COLLECTION)
                .document(department.departmentId)
                .delete()
                .await()

            Response.Success(Response.Action.REMOVE)
        } catch (firestoreException: FirebaseFirestoreException) {
            Response.Error(firestoreException, Response.Action.REMOVE)
        } catch (exception: Exception) {
            Response.Error(exception, Response.Action.REMOVE)
        }
    }
}