package study.snacktrackmobile.data.model.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class CommentResponse(
    @SerialName("id") val id: Int = 0,
    @SerialName("authorId") val authorId: Int = 0,
    @SerialName("authorName") val authorName: String = "Unknown",
    @SerialName("authorImageUrl") val authorImageUrl: String? = null,
    @SerialName("content") val content: String? = "",
    @SerialName("mealId") val mealId: Int = 0,
    @SerialName("likesCount") val likesCount: Int = 0,
    @SerialName("isLiked") val isLiked: Boolean = false
)