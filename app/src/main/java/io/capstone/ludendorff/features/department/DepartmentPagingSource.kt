package io.capstone.ludendorff.features.department

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.QuerySnapshot
import io.capstone.ludendorff.components.exceptions.EmptySnapshotException
import kotlinx.coroutines.tasks.await

class DepartmentPagingSource(
    private val departmentQuery: Query
): PagingSource<QuerySnapshot, Department>() {

    override fun getRefreshKey(state: PagingState<QuerySnapshot, Department>): QuerySnapshot? {
        return null
    }

    override suspend fun load(params: LoadParams<QuerySnapshot>): LoadResult<QuerySnapshot, Department> {
        return try {
            val currentPage = params.key ?: departmentQuery.get().await()

            if (currentPage.documents.isNotEmpty()) {
                val lastVisibleDepartment = currentPage.documents[currentPage.size() - 1]
                val nextPage = departmentQuery.startAfter(lastVisibleDepartment).get().await()
                LoadResult.Page(
                    data = currentPage.toObjects(Department::class.java),
                    prevKey = null,
                    nextKey = nextPage
                )
            } else throw EmptySnapshotException()

        } catch (exception: Exception) {
            LoadResult.Error(exception)
        }
    }
}