package io.capstone.keeper.features.core.backend

import io.capstone.keeper.features.core.data.Response

interface FirestoreRepository<T> {

    suspend fun create(data: T): Response<Unit>
    suspend fun update(data: T): Response<Unit>
    suspend fun remove(id: String): Response<Unit>

    companion object {
        const val QUERY_LIMIT = 15L
    }
}