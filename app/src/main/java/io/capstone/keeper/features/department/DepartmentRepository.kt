package io.capstone.keeper.features.department

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import io.capstone.keeper.features.core.backend.FirestoreRepository
import io.capstone.keeper.features.core.data.Response
import io.capstone.keeper.features.user.User
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DepartmentRepository @Inject constructor(
    private val firestore: FirebaseFirestore
): FirestoreRepository<Department> {

    suspend fun create(data: Department): Response<Unit> {
        return try {
            firestore.collection(Department.COLLECTION)
                .document(data.departmentId)
                .set(data)
                .await()

            Response.Success(Unit)
        } catch (firestoreException: FirebaseFirestoreException) {
            Response.Error(firestoreException)
        } catch (exception: Exception) {
            Response.Error(exception)
        }
    }

    suspend fun update(data: Department): Response<Unit> {
        return try {
            firestore.collection(Department.COLLECTION)
                .document(data.departmentId)
                .set(data)
                .await()

            val batchWrite = firestore.batch()
            firestore.collection(User.COLLECTION)
                .whereEqualTo(User.FIELD_DEPARTMENT_ID, data.departmentId)
                .get().await()
                .documents.forEach {
                    batchWrite.update(it.reference, User.FIELD_DEPARTMENT, data.toDepartmentCore())
                }
            batchWrite.commit()

            Response.Success(Unit)
        } catch (firestore: FirebaseFirestoreException) {
            Response.Error(firestore)
        } catch (exception: Exception) {
            Response.Error(exception)
        }
    }

    suspend fun remove(department: Department): Response<Unit> {
        return try {
            firestore.collection(Department.COLLECTION)
                .document(department.departmentId)
                .delete()
                .await()

            Response.Success(Unit)
        } catch (firestoreException: FirebaseFirestoreException) {
            Response.Error(firestoreException)
        } catch (exception: Exception) {
            Response.Error(exception)
        }
    }
}