//https://github.com/mlegy/retrofit2-kotlin-coroutines-call-adapter/tree/master

package exam.hughwu.retrofit.adapterfactory

import exam.hughwu.retrofit.ApiError
import exam.hughwu.retrofit.NetworkResponse
import okhttp3.Headers
import okhttp3.Request
import okhttp3.ResponseBody
import okio.Timeout
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Converter
import retrofit2.Response
import java.io.IOException

internal class NetworkResponseCall<S : Any>(
    private val delegate: Call<S>,
    private val errorConverter: Converter<ResponseBody, ApiError>,
) : Call<NetworkResponse<S, ApiError>> {

    companion object {
        const val RETRY_TIME = 3
        val NOT_RETRY_HTTP_STATUS_CODES = setOf(ApiError.ROUTER_NOT_FOUND)
    }

    private var retryCount = 1

    fun setNoRetry() {
        retryCount = RETRY_TIME
    }

    private fun isRetryOver(): Boolean = (retryCount == RETRY_TIME)

    override fun enqueue(callback: Callback<NetworkResponse<S, ApiError>>) {
        return delegate.enqueue(object : Callback<S> {
            override fun onResponse(call: Call<S>, response: Response<S>) {
                val body = response.body()
                val code = response.code()
                val error = response.errorBody()
                val headers = response.headers()

                if (response.isSuccessful) {
                    // 懷疑點：dispatchPreloadIfEnabled 內部走 ResponseVi
                    callback.onResponse(
                        this@NetworkResponseCall,
                        Response.success(NetworkResponse.Success(body, headers))
                    )
                } else {
                    handleErrorResponse(callback, call, code, error, headers)
                }
            }

            override fun onFailure(call: Call<S>, throwable: Throwable) {
                val networkResponse = generateFailureNetworkResponse(throwable)
                callback.onResponse(this@NetworkResponseCall, Response.success(networkResponse))
            }
        })
    }

    private fun generateFailureNetworkResponse(
        throwable: Throwable,
    ): NetworkResponse<Nothing, ApiError> {
        return when (throwable) {
            is IOException -> NetworkResponse.Failure(
                ApiError(
                    httpCode = ApiError.REQUEST_TIME_OUT,
                    code = ApiError.REQUEST_TIME_OUT,
                    throwable = throwable,
                    message = throwable.message ?: ApiError.TIMEOUT_ERROR
                )
            )

            else -> NetworkResponse.Failure(
                ApiError(
                    httpCode = ApiError.UNKNOWN,
                    code = ApiError.UNKNOWN,
                    throwable = throwable,
                    message = throwable.message ?: ApiError.UNKNOWN_ERROR
                )
            )
        }
    }

    private fun handleErrorResponse(
        callback: Callback<NetworkResponse<S, ApiError>>,
        call: Call<S>,
        httpStatusCode: Int,
        error: ResponseBody?,
        headers: Headers
    ) {
        if (httpStatusCode == ApiError.UNAUTHORIZED) {
            if (isRetryOver()) {
                responseFail(
                    callback = callback,
                    httpStatusCode = httpStatusCode,
                    errorResponseBody = error,
                    headers = headers
                )
            } else {
                retry(
                    callback = callback,
                    retryCall = call,
                    onRetryFail = {
                        responseFail(
                            callback = callback,
                            httpStatusCode = httpStatusCode,
                            errorResponseBody = error,
                            headers = headers
                        )
                    })
            }
        } else {
            if (NOT_RETRY_HTTP_STATUS_CODES.contains(httpStatusCode) || call.request().method != "GET" || isRetryOver()) {
                responseFail(
                    callback = callback,
                    httpStatusCode = httpStatusCode,
                    errorResponseBody = error,
                    headers = headers
                )
            } else {
                retry(callback, call)
            }
        }
    }

    private fun retry(callback: Callback<NetworkResponse<S, ApiError>>, retryCall: Call<S>) {
        retryCount++
        retryCall.clone().enqueue(object : Callback<S> {
            override fun onResponse(call: Call<S>, response: Response<S>) {
                val body = response.body()
                val code = response.code()
                val error = response.errorBody()
                val headers = response.headers()

                if (response.isSuccessful) {
                    if (body != null) {
                        callback.onResponse(
                            this@NetworkResponseCall,
                            Response.success(NetworkResponse.Success(body, headers))
                        )
                    } else {
                        // Response is successful but the body is null
                        @Suppress("UNCHECKED_CAST")
                        callback.onResponse(
                            this@NetworkResponseCall,
                            Response.success(NetworkResponse.Success(Unit as S, headers))
                        )
                    }
                } else {
                    handleErrorResponse(callback, call, code, error, headers)
                }
            }

            override fun onFailure(call: Call<S>, throwable: Throwable) {
                val networkResponse = generateFailureNetworkResponse(throwable)
                callback.onResponse(this@NetworkResponseCall, Response.success(networkResponse))
            }
        })
    }

    private fun retry(
        callback: Callback<NetworkResponse<S, ApiError>>,
        retryCall: Call<S>,
        onRetryFail: (() -> Unit)? = null
    ) {
        retryCount++
        retryCall.clone().enqueue(object : Callback<S> {
            override fun onResponse(call: Call<S>, response: Response<S>) {
                val body = response.body()
                val code = response.code()
                val error = response.errorBody()
                val headers = response.headers()

                if (response.isSuccessful) {
                    if (body != null) {

                        callback.onResponse(
                            this@NetworkResponseCall,
                            Response.success(NetworkResponse.Success(body, headers))
                        )
                    } else {
                        // Response is successful but the body is null
                        @Suppress("UNCHECKED_CAST")
                        callback.onResponse(
                            this@NetworkResponseCall,
                            Response.success(NetworkResponse.Success(Unit as S, headers))
                        )
                    }
                } else {
                    handleErrorResponse(callback, call, code, error, headers)
                }
            }

            override fun onFailure(call: Call<S>, throwable: Throwable) {
                onRetryFail?.invoke()
            }
        })
    }

    private fun responseFail(
        callback: Callback<NetworkResponse<S, ApiError>>,
        httpStatusCode: Int,
        errorResponseBody: ResponseBody?,
        headers: Headers
    ) {
        val errorBody = when {
            errorResponseBody == null -> null
            errorResponseBody.contentLength() == 0L -> null
            else -> try {
                errorConverter.convert(errorResponseBody)
            } catch (ex: Exception) {
                null
            }
        }
        if (errorBody != null) {
            callback.onResponse(
                this@NetworkResponseCall,
                Response.success(
                    NetworkResponse.Failure(
                        errorBody = errorBody.copy(httpCode = httpStatusCode),
                        headers = headers
                    )
                )
            )
        } else {
            callback.onResponse(
                this@NetworkResponseCall,
                Response.success(
                    NetworkResponse.Failure(
                        errorBody = ApiError(
                            httpCode = httpStatusCode,
                            code = ApiError.UNKNOWN,
                            message = errorResponseBody?.string() ?: ApiError.UNKNOWN_ERROR
                        ),
                        headers = headers
                    )
                )
            )
        }
    }

    override fun isExecuted() = delegate.isExecuted

    override fun clone() = NetworkResponseCall(delegate.clone(), errorConverter)

    override fun isCanceled() = delegate.isCanceled

    override fun cancel() = delegate.cancel()

    override fun execute(): Response<NetworkResponse<S, ApiError>> {
        throw UnsupportedOperationException("NetworkResponseCall doesn't support execute")
    }

    override fun request(): Request = delegate.request()

    override fun timeout(): Timeout = delegate.timeout()

}