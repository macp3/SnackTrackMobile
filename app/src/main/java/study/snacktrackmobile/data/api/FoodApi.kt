package study.snacktrackmobile.data.api

import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import study.snacktrackmobile.data.model.dto.EssentialFoodRequest
import study.snacktrackmobile.data.model.dto.EssentialFoodResponse

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

}
