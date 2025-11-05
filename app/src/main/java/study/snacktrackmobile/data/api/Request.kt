package study.snacktrackmobile.data.api

import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit

object Request {

    private const val BASE_URL = "http://10.0.2.2:8080/"
    //

    private val jsonSerializer = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
    }

    private val logging = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    private val client = OkHttpClient.Builder()
        .addInterceptor(logging)
        .build()

    private val retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .addConverterFactory(jsonSerializer.asConverterFactory("application/json".toMediaType()))
        .client(client)
        .build()

    val api: ApiService = retrofit.create(ApiService::class.java)
}
