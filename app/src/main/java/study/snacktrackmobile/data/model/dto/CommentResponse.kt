package study.snacktrackmobile.data.model.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class CommentResponse(
    @SerialName("id")
    val id: Int = 0,

    @SerialName("authorId")
    val authorId: Int = 0,

    @SerialName("content")
    val content: String? = "", // Zabezpieczenie przed nullem

    @SerialName("mealId")
    val mealId: Int = 0
)