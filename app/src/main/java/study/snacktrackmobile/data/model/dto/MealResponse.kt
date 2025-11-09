package study.snacktrackmobile.data.model.dto

import kotlinx.serialization.Serializable

@Serializable
data class MealResponse(
    val id: Int?,
    val name: String?,
    val description: String?,
    val authorId: Int?,
    val imageUrl: String?
)