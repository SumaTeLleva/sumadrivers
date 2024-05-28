package mx.suma.drivers.network

import retrofit2.HttpException
import java.net.SocketTimeoutException


enum class ErrorCodes(val code: Int) {
    SocketTimeOut(-1)
}

open class ResponseHandler {
    companion object {
        fun <T : Any> handleSuccess(data: T): Resource<T> {
            return Resource.success(data)
        }

        fun <T : Any> handleException(e: Exception): Resource<T> {
            return when (e) {
                is HttpException -> Resource.error(getErrorMessage(e.code()), null)
                is SocketTimeoutException -> Resource.error(getErrorMessage(ErrorCodes.SocketTimeOut.code), null)
                else -> Resource.error(getErrorMessage(Int.MAX_VALUE), null)
            }
        }

        private fun getErrorMessage(code: Int): String {
            return when (code) {
                ErrorCodes.SocketTimeOut.code -> "Timeout"
                401 -> "Unauthorised"
                404 -> "Not found"
                403 -> "Forbidden"
                422 -> "Unprocessable Entity"
                else -> "Something went wrong"
            }
        }
    }
}