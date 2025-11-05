package study.snacktrackmobile.data.api

import okhttp3.ResponseBody
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import study.snacktrackmobile.data.model.LoginRequest
import study.snacktrackmobile.data.model.LoginResponse

interface ApiService {

    @POST("auth/login")
    suspend fun login(
        @Body body: LoginRequest
    ): ResponseBody
}

