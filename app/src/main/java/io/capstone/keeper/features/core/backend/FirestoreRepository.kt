package io.capstone.keeper.features.core.backend


interface FirestoreRepository<T> {

    enum class Action {
        CREATE, UPDATE, REMOVE
    }

    companion object {
        const val QUERY_LIMIT = 15L
    }
}