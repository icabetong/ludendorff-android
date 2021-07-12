package io.capstone.keeper.android.features.core.backend

import io.capstone.keeper.android.features.core.data.Response

interface FirestoreRepository<T> {

    suspend fun create(data: T): Response<Unit>
    suspend fun update(data: T): Response<Unit>
    suspend fun remove(id: String): Response<Unit>

    companion object {
        const val QUERY_LIMIT = 15L
    }
}