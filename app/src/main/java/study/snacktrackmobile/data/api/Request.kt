package study.snacktrackmobile.data.api

import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import okhttp3.Interceptor
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import study.snacktrackmobile.data.network.ApiConfig
import java.net.CookieManager
import java.net.CookiePolicy

object Request {

    private const val BASE_URL = ApiConfig.BASE_URL

    private val jsonSerializer = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
    }

    private val logging = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    private val cookieManager = CookieManager().apply {
        setCookiePolicy(CookiePolicy.ACCEPT_ALL)
    }

    private val headerInterceptor = Interceptor { chain ->
        val original = chain.request()

        val newHeaders = original.headers.newBuilder()
            .add("Accept", "application/json")
            .build()

        val newRequest = original.newBuilder()
            .headers(newHeaders)
            .build()

        chain.proceed(newRequest)
    }


    private val client = OkHttpClient.Builder()
        .addInterceptor(headerInterceptor)
        .addInterceptor(logging)
        .build()

    @OptIn(ExperimentalSerializationApi::class)
    private val retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .addConverterFactory(jsonSerializer.asConverterFactory("application/json".toMediaType()))
        .client(client)
        .build()

    val api: ApiService = retrofit.create(ApiService::class.java)
    val foodApi: FoodApi = retrofit.create(FoodApi::class.java)
}
