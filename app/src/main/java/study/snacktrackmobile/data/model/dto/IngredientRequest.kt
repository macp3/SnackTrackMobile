package study.snacktrackmobile.data.model.dto

import kotlinx.serialization.Serializable

@Serializable
data class IngredientRequest(
    val essentialFoodId: Int?,
    val essentialApiId: Int?,
    val amount: Float?,
    val pieces: Float?,
    val defaultUnit: String?
)