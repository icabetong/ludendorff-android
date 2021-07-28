package io.capstone.keeper.features.core.data

sealed class Response<out T> {

    data class Success<out R>(
        val data: R
    ): Response<R>()

    data class Error<out R>(
        val throwable: Throwable?,
        val data: R? = null
    ): Response<R>()

}