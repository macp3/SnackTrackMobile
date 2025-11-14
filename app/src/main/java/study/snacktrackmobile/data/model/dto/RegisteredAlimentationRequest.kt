package study.snacktrackmobile.data.model.dto

import kotlinx.serialization.Serializable

@Serializable
data class RegisteredAlimentationRequest(
    val essentialId: Int?,
    val mealApiId: Int? = null,
    val mealId: Int? = null,
    val timestamp: String,        // wysyłamy jako ISO string "yyyy-MM-dd"
    val mealName: String,         // enum name np. "BREAKFAST"
    val amount: Float? = null,
    val pieces: Int? = null
)


