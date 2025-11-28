package study.snacktrackmobile.data.model.dto

import kotlinx.serialization.Serializable

@Serializable
data class RegisteredAlimentationRequest(
    val essentialId: Int?,
    val mealApiId: Int? = null,
    val mealId: Int? = null,
    val timestamp: String,
    val mealName: String,
    val amount: Float? = null,
    val pieces: Float? = null
)


