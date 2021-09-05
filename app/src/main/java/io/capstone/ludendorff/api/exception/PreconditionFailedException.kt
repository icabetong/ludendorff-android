package io.capstone.ludendorff.api.exception

class PreconditionFailedException: Exception(MESSAGE) {

    companion object {
        private const val MESSAGE = "The required data for the POST request was not met."
    }
}