package study.snacktrackmobile.data.model.dto

import kotlinx.serialization.Serializable

@Serializable
data class ReportedMealRequest(
    val mealId: Int,
    val content: String // Powód zgłoszenia
)