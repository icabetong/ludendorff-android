package io.capstone.keeper.features.core.backend


interface FirestoreRepository<T> {

    companion object {
        const val QUERY_LIMIT = 15L
    }
}