package study.snacktrackmobile.data.api

import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.*
import study.snacktrackmobile.data.model.dto.CommentRequest
import study.snacktrackmobile.data.model.dto.CommentResponse
import study.snacktrackmobile.data.model.dto.ReportedCommentRequest

interface CommentApi {

    @GET("comments/meal/{mealId}")
    suspend fun getCommentsForMeal(
        @Header("Authorization") token: String, // Authorization is required to check 'isLiked'
        @Path("mealId") mealId: Int
    ): List<CommentResponse>

    @POST("comments/add")
    suspend fun addComment(
        @Header("Authorization") token: String,
        @Body request: CommentRequest
    ): CommentResponse

    @PUT("comments/edit")
    suspend fun editComment(
        @Header("Authorization") token: String,
        @Body request: CommentRequest
    ): CommentResponse

    @DELETE("comments/delete")
    suspend fun deleteComment(
        @Header("Authorization") token: String,
        @Query("mealId") mealId: Int
    ): Response<ResponseBody>

    @POST("comments/reports/add")
    suspend fun reportComment(
        @Header("Authorization") token: String,
        @Body request: ReportedCommentRequest
    ): Response<ResponseBody>

    @POST("comments/{commentId}/like")
    suspend fun likeComment(
        @Header("Authorization") token: String,
        @Path("commentId") commentId: Int
    ): Response<ResponseBody>
}