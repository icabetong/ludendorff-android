package io.capstone.ludendorff.api.exception

class UnauthorizedException: Exception(MESSAGE) {

    companion object {
        private const val MESSAGE = "The server returned an 401 (Unauthorized) Status"
    }
}