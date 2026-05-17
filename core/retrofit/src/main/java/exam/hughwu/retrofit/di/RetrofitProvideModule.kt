package exam.hughwu.retrofit.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import exam.hughwu.retrofit.BuildConfig
import exam.hughwu.retrofit.adapterfactory.NetworkResponseAdapterFactory
import exam.hughwu.retrofit.twse.api.ExchangeReportAPI
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Singleton


@Module
@InstallIn(SingletonComponent::class)
class RetrofitProvideModule {

    @Singleton
    @Provides
    fun provideDefaultRetrofitClient(
    ): Retrofit {
        val baseUrl: String = BuildConfig.BASE_URL
        val retrofitBuilder = Retrofit.Builder()
            .baseUrl(baseUrl)
            .addConverterFactory(GsonConverterFactory.create())

        val clientBuilder = OkHttpClient
            .Builder()
            .followRedirects(false)
            .followSslRedirects(false)
            .apply {
                if (BuildConfig.DEBUG) {
                    addInterceptor(HttpLoggingInterceptor().apply {
                        level = HttpLoggingInterceptor.Level.BODY
                    })
                }
            }
            .callTimeout(10, TimeUnit.SECONDS)
            .connectTimeout(10, TimeUnit.SECONDS)
            .readTimeout(10, TimeUnit.SECONDS)
            .writeTimeout(10, TimeUnit.SECONDS)

        return retrofitBuilder
            .addCallAdapterFactory(
                NetworkResponseAdapterFactory()
            )
            .client(clientBuilder.build())
            .build()
    }

    @Singleton
    @Provides
    fun provideTWSEExchangeReportApi(retrofit: Retrofit): ExchangeReportAPI =
        retrofit.create(ExchangeReportAPI::class.java)
}
