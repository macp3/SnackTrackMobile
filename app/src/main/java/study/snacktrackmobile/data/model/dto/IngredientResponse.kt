package study.snacktrackmobile.data.model.dto

import study.snacktrackmobile.data.model.Ingredient

data class IngredientResponse(
    val id: Int,
    val amount: Float?,
    val pieces: Float?,
    val essentialApi: ApiFoodResponseDetailed?,
    val essentialFood: EssentialFoodResponse?
)


