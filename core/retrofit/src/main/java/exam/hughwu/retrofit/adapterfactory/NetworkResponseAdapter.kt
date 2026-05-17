//https://github.com/mlegy/retrofit2-kotlin-coroutines-call-adapter/tree/master

package exam.hughwu.retrofit.adapterfactory

import exam.hughwu.retrofit.ApiError
import exam.hughwu.retrofit.NetworkResponse
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.CallAdapter
import retrofit2.Converter
import java.lang.reflect.Type

class NetworkResponseAdapter<S : Any>(
    private val successType: Type,
    private val errorBodyConverter: Converter<ResponseBody, ApiError>,
    ) : CallAdapter<S, Call<NetworkResponse<S, ApiError>>> {

    override fun responseType(): Type = successType

    override fun adapt(call: Call<S>): Call<NetworkResponse<S, ApiError>> {
        return NetworkResponseCall(call, errorBodyConverter)
    }
}