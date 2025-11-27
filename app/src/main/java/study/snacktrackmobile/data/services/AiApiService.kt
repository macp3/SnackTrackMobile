package study.snacktrackmobile.data.services

import com.google.gson.annotations.SerializedName
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST
import study.snacktrackmobile.data.model.dto.AiDietItem

data class AiShoppingRequest(
    val prompt: String,
    val productContext: List<String>
)

data class AiGeneratedItem(
    @SerializedName("name") val name: String,
    @SerializedName("quantity") val quantity: String,
    @SerializedName("description") val description: String?
)

interface AiApiService {
    @POST("/ai/shopping-list/generate")
    suspend fun generateShoppingList(
        @Header("Authorization") token: String,
        @Body request: AiShoppingRequest
    ): List<AiGeneratedItem>

    @POST("ai/diet/generate")
    suspend fun generateDiet(
        @Header("Authorization") token: String,
        @Body request: AiShoppingRequest
    ): List<AiDietItem>
}