package study.snacktrackmobile.data.model.dto

import kotlinx.serialization.Serializable
import study.snacktrackmobile.data.model.Ingredient

@Serializable
data class IngredientResponse(
    val id: Int,
    val amount: Float?,
    val pieces: Float?,
    val essentialApi: ApiFoodResponseDetailed? = null,
    val essentialFood: EssentialFoodResponse? = null
)


