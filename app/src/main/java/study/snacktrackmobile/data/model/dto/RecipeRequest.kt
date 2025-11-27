package study.snacktrackmobile.data.model.dto

import kotlinx.serialization.Serializable

@Serializable
data class RecipeRequest(
    val name: String,
    val description: String,
    val ingredients: List<IngredientRequest>
)