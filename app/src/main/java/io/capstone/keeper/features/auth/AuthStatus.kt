package io.capstone.keeper.features.auth

import io.capstone.keeper.features.user.User

sealed class AuthStatus {
    data class Success(
        val user: User
    ): AuthStatus()
    data class Authenticating(
        val unit: Unit = Unit
    ): AuthStatus()
    data class Error(
        val exception: Exception
    ): AuthStatus()
}
