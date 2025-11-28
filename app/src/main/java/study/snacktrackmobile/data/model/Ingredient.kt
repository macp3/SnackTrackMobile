package study.snacktrackmobile.data.model

data class Ingredient(
    val id: Int,
    val essentialFood: EssentialFood?,
    val essentialApiId: Int?,
    val amount: Float?,
    val pieces: Float?
)
