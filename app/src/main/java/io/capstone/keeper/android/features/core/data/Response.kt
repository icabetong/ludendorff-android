package io.capstone.keeper.android.features.core.data

sealed class Response<out T> {

    data class Success<out R>(
        val value: R
    ): Response<R>()

    data class InProgress(
        val inProgress: Boolean = true
    ): Response<Nothing>()

    data class Error(
        val throwable: Throwable?
    ): Response<Nothing>()

}