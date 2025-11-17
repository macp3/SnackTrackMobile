package study.snacktrackmobile.data.api

import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.*
import study.snacktrackmobile.data.model.dto.RecipeRequest
import study.snacktrackmobile.data.model.dto.RecipeResponse

interface RecipeApi {

    @GET("recipes/my")
    suspend fun getMyRecipes(
        @Header("Authorization") token: String
    ): List<RecipeResponse>

    @POST("recipes/add")
    suspend fun addRecipe(
        @Header("Authorization") token: String,
        @Body dto: RecipeRequest
    ): Response<ResponseBody>

    @PUT("recipes/update/{id}")
    suspend fun updateRecipe(
        @Header("Authorization") token: String,
        @Path("id") id: Int,
        @Body dto: RecipeRequest
    ): RecipeResponse

    @DELETE("recipes/delete/{id}")
    suspend fun deleteRecipe(
        @Header("Authorization") token: String,
        @Path("id") id: Int
    ): Response<Unit>
}
