package io.capstone.keeper.features.department

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import io.capstone.keeper.features.core.backend.Response
import io.capstone.keeper.features.user.User
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DepartmentRepository @Inject constructor(
    private val firestore: FirebaseFirestore
) {

    suspend fun create(department: Department): Response<Response.Action> {
        return try {
            firestore.collection(Department.COLLECTION)
                .document(department.departmentId)
                .set(department)
                .await()

            Response.Success(Response.Action.CREATE)
        } catch (firestoreException: FirebaseFirestoreException) {
            Response.Error(firestoreException, Response.Action.CREATE)
        } catch (exception: Exception) {
            Response.Error(exception, Response.Action.CREATE)
        }
    }

    suspend fun update(department: Department): Response<Response.Action> {
        return try {
            firestore.collection(Department.COLLECTION)
                .document(department.departmentId)
                .set(department)
                .await()

            val batchWrite = firestore.batch()
            firestore.collection(User.COLLECTION)
                .whereEqualTo(User.FIELD_DEPARTMENT_ID, department.departmentId)
                .get().await()
                .documents.forEach {
                    batchWrite.update(it.reference, User.FIELD_DEPARTMENT, department.minimize())
                }
            batchWrite.commit()

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