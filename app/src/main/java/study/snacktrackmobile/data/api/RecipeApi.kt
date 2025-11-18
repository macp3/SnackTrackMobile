package study.snacktrackmobile.data.api

import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.*
import study.snacktrackmobile.data.model.Recipe
import study.snacktrackmobile.data.model.dto.RecipeRequest
import study.snacktrackmobile.data.model.dto.RecipeResponse

interface RecipeApi {

    @GET("/meals/my")
    suspend fun getMyRecipes(
        @Header("Authorization") token: String
    ): List<RecipeResponse>

    @POST("meals/create")
    suspend fun addRecipe(
        @Header("Authorization") token: String,
        @Body request: RecipeRequest
    ): Response<Unit>

    @PUT("meals/my/edit/{id}")
    suspend fun updateRecipe(
        @Header("Authorization") token: String,
        @Path("id") id: Int,
        @Body dto: RecipeRequest
    ): Response<Unit>

    @DELETE("meals/my/delete/{id}")
    suspend fun deleteRecipe(
        @Header("Authorization") token: String,
        @Path("id") id: Int
    ): Response<Unit>

    @GET("users/favourite")
    suspend fun getMyFavourites(
        @Header("Authorization") token: String
    ): List<RecipeResponse>

    @POST("users/favourite/add")
    suspend fun addFavourite(
        @Header("Authorization") token: String,
        @Query("mealId") mealId: Int
    ): Response<Unit>

    @DELETE("users/favourite/remove/{mealId}")
    suspend fun removeFavourite(
        @Header("Authorization") token: String,
        @Path("mealId") mealId: Int
    ): Response<Unit>

    @GET("meals")
    suspend fun getAllRecipes(
        @Header("Authorization") token: String
    ): List<RecipeResponse>

    @GET("meals/{mealId}/details")
    suspend fun getMealWithIngredients(
        @Header("Authorization") token: String,
        @Path("mealId") mealId: Int
    ): List<RecipeResponse>
}
