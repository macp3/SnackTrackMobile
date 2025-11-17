package study.snacktrackmobile.data.model.dto

data class IngredientResponse(
    val id: Int,
    val amount: Float?,
    val pieces: Float?,
    val essentialApi: ApiFoodResponseDetailed?,
    val essentialFood: EssentialFoodResponse?
)