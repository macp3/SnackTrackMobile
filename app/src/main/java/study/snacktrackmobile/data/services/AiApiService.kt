package study.snacktrackmobile.data.services

import com.google.gson.annotations.SerializedName
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST

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
}