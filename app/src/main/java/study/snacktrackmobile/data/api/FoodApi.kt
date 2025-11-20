package study.snacktrackmobile.data.api

import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Query
import study.snacktrackmobile.data.model.dto.EssentialFoodRequest
import study.snacktrackmobile.data.model.dto.EssentialFoodResponse
import study.snacktrackmobile.data.model.dto.UnifiedSearchResponse

interface FoodApi {
    @POST("food/add")
    suspend fun addFood(
        @Header("Authorization") token: String,
        @Body food: EssentialFoodRequest
    ): Response<ResponseBody>

    @GET("food/all")
    suspend fun getAllFoods(
        @Header("Authorization") token: String
    ): List<EssentialFoodResponse>

    @GET("food/search/unified")
    suspend fun searchFood(
        @Header("Authorization") token: String,
        @Query("query") query: String
    ): UnifiedSearchResponse
}
