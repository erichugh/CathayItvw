//https://github.com/mlegy/retrofit2-kotlin-coroutines-call-adapter/tree/master

package exam.hughwu.retrofit

import okhttp3.Headers

typealias GenericResponse<S> = NetworkResponse<S, ApiError>

sealed class NetworkResponse<out T : Any, out U : Any> {
    /**
     * Success response with body
     */
    data class Success<T : Any>(val body: T?, val headers: Headers? = null) :
        NetworkResponse<T, Nothing>()

    /**
     * Failure response with body
     */
    data class Failure(val errorBody: ApiError? = null, val headers: Headers? = null) :
        NetworkResponse<Nothing, ApiError>()

}

sealed class UseCaseResponse<out T> {
    data class Success<out T>(val data: T?) : UseCaseResponse<T>()
    data class Failure(val errorBody: ApiError?) : UseCaseResponse<Nothing>()
}
