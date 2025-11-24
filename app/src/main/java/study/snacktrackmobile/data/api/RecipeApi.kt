package study.snacktrackmobile.data.api

import com.google.android.gms.common.internal.safeparcel.SafeParcelable
import com.google.android.gms.common.internal.safeparcel.SafeParcelable.Param
import okhttp3.MultipartBody
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
    ): Response<Int>

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

    @Multipart
    @POST("meals/{id}/image")
    suspend fun uploadImage(
        @Header("Authorization") token: String,
        @Path("id") id: Int,
        @Part image: MultipartBody.Part
    ): Response<ResponseBody>

    @GET("meals/{id}/details")
    suspend fun getRecipeDetails(
        @Header("Authorization") token: String,
        @Path("id") id: Int
    ): RecipeResponse

    @GET("meals/search")
    suspend fun searchMeals(
        @Header("Authorization") token: String,
        @Query("name") query: String
    ): List<RecipeResponse>
}
