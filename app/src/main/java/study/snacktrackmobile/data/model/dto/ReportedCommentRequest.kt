package study.snacktrackmobile.data.model.dto

import kotlinx.serialization.Serializable

@Serializable
data class ReportedCommentRequest(
    val commentId: Int,
    val content: String
)