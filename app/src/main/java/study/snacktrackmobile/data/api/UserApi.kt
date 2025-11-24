package study.snacktrackmobile.data.api

import okhttp3.MultipartBody
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.*
import study.snacktrackmobile.data.model.dto.BodyParametersRequest
import study.snacktrackmobile.data.model.dto.BodyParametersResponse
import study.snacktrackmobile.data.model.dto.LoginResponse
import study.snacktrackmobile.data.model.dto.UserResponse

interface UserApi {

    @GET("/users/profile")
    suspend fun getProfile(
        @Header("Authorization") token: String
    ): Response<UserResponse>

    @PUT("/users/changePassword")
    suspend fun changePassword(
        @Header("Authorization") token: String,
        @Query("password") newPassword: String
    ): Response<ResponseBody>

    @PUT("/users/changeParameters")
    suspend fun changeParameters(
        @Header("Authorization") token: String,
        @Body body: BodyParametersRequest
    ): Response<BodyParametersResponse>

    @GET("/users/refreshSurvey")
    suspend fun refreshSurvey(
        @Header("Authorization") token: String
    ): Response<LoginResponse>


    @Multipart
    @POST("/users/image")
    suspend fun uploadImage(
        @Header("Authorization") token: String,
        @Part image: MultipartBody.Part
    ): Response<ResponseBody>

    @GET("users/bodyParameters")
    suspend fun getBodyParameters(
        @Header("Authorization") token: String
    ): BodyParametersResponse

    @PUT("/users/premium")
    suspend fun updatePremium(
        @Header("Authorization") token: String,
        @Query("expiration") expiration: String
    ): Response<ResponseBody>
}
