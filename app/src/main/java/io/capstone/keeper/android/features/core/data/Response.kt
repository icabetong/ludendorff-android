package io.capstone.keeper.android.features.core.data

sealed class Response<out T> {

    data class Success<out R>(
        val data: R
    ): Response<R>()

    data class Error(
        val throwable: Throwable?
    ): Response<Nothing>()

}