package study.snacktrackmobile.data.model

import study.snacktrackmobile.data.model.dto.EssentialFoodResponse

data class Ingredient(
    val id: Int,
    val meal: Recipe,
    val essentialFood: EssentialFood,
    val essentialApiId: Int?,
    val amount: Float?,
    val pieces: Float?
)
