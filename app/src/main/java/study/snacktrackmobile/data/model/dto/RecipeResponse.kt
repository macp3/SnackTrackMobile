package study.snacktrackmobile.data.model.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class RecipeResponse(
    val id: Int,
    val name: String,
    val description: String?,
    val authorId: Int,

    // Upewnij się, że nazwa pola to "ingredients"
    @SerialName("ingredients")
    val ingredients: List<IngredientResponse> = emptyList(),

    val imageUrl: String?
)