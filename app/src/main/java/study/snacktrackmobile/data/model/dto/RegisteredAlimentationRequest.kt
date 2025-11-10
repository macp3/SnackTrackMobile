package study.snacktrackmobile.data.model.dto

data class RegisteredAlimentationRequest(
    val essentialId: Int,
    val mealApiId: Int? = null,
    val mealId: Int? = null,
    val timestamp: String,
    val mealName: String,
    val amount: Float,
    val pieces: Int
)
