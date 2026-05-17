package exam.hughwu.retrofit

/**
 * @param httpCode : Http response Code
 * @param code : Backend response Code
 * @param message : Backend debug message
 */

data class ApiError(
    val httpCode: Int = UNKNOWN,
    val code: Int = UNKNOWN,
    val throwable: Throwable? = null,
    override val message: String = "",
) : Error() {
    companion object {
        const val UNKNOWN = Int.MAX_VALUE

        // Connection issue
        const val REQUEST_TIME_OUT = 408

        // Gateway has response
        const val JSON_EXCEPTION = Int.MAX_VALUE - 1
        const val UNAUTHORIZED = 401
        const val ROUTER_NOT_FOUND = 404
        const val GATEWAY_TIME_OUT = 504

        const val UNKNOWN_ERROR = "unknown server error"
        const val TIMEOUT_ERROR = "request timeout"
    }
}
