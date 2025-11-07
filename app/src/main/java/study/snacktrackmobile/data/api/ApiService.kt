package study.snacktrackmobile.data.api

import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Query
import study.snacktrackmobile.data.model.LoginRequest
import study.snacktrackmobile.data.model.LoginResponse
import study.snacktrackmobile.data.model.RegisterRequest
import study.snacktrackmobile.data.model.dto.RegisteredAlimentationResponse

interface ApiService {

    @POST("auth/login")
    suspend fun login(
        @Body body: LoginRequest
    ): Response<LoginResponse>

    @POST("auth/register")
    suspend fun register(@Body body: RegisterRequest): Response<ResponseBody>

    @GET("/registered/my")
    suspend fun getMyEntries(
        @Header("Authorization") token: String,
        @Query("date") date: String
    ): List<RegisteredAlimentationResponse>
}

