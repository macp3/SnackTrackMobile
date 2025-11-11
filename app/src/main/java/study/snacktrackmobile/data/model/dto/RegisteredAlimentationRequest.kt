package study.snacktrackmobile.data.model.dto

import kotlinx.serialization.Serializable

@Serializable
data class RegisteredAlimentationRequest(
    val essentialId: Int? = null,
    val mealApiId: Int? = null,
    val mealId: Int? = null,
    val timestamp: String? = null, // LocalDate jako String "YYYY-MM-DD"
    val mealName: String,          // np. "Breakfast", "Lunch"
    val amount: Float? = null,
    val pieces: Int? = null
)

