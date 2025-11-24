package study.snacktrackmobile.data.model.dto

import kotlinx.serialization.Serializable

@Serializable
data class CommentRequest(
    val mealId: Int,
    val content: String?
)