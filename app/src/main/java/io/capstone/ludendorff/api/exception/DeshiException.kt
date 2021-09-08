package io.capstone.ludendorff.api.exception

class DeshiException(val code: Code): Exception(code.getMessage()) {

    constructor(code: Int): this(Code.parse(code))

    enum class Code(code: Int) {
        UNAUTHORIZED(401),
        FORBIDDEN(403),
        PRECONDITION_FAILED(412),
        UNPROCESSABLE_ENTITY(422),
        GENERIC(500);

        fun getMessage(): String {
            return when(this) {
                UNAUTHORIZED -> MESSAGE_UNAUTHORIZED
                FORBIDDEN -> MESSAGE_FORBIDDEN
                PRECONDITION_FAILED -> MESSAGE_PRECONDITION_FAILED
                UNPROCESSABLE_ENTITY -> MESSAGE_UNPROCESSABLE_ENTITY
                GENERIC -> MESSAGE_GENERIC
            }
        }

        companion object {
            fun parse(code: Int): Code {
                return when(code) {
                    401 -> UNAUTHORIZED
                    403 -> FORBIDDEN
                    412 -> PRECONDITION_FAILED
                    422 -> UNPROCESSABLE_ENTITY
                    500 -> GENERIC
                    else -> throw IllegalArgumentException()
                }
            }
        }
    }

    companion object {
        const val MESSAGE_UNAUTHORIZED = "Client did not provide any authentication token"
        const val MESSAGE_FORBIDDEN = "Client is not permitted to do the operation"
        const val MESSAGE_PRECONDITION_FAILED = "Required data for the operation is missing"
        const val MESSAGE_UNPROCESSABLE_ENTITY = "Resource already exists"
        const val MESSAGE_GENERIC = "General Server Error"
    }
}