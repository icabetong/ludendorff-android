package io.capstone.keeper.features.core.backend

sealed class Operation<out T> {
    data class Success<R>(val data: R?): Operation<R>()
    data class Error(val error: Throwable?): Operation<Nothing>()
}