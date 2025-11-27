package study.snacktrackmobile.data.model.dto
import kotlinx.serialization.Serializable

@Serializable
data class AiDietItem(
    val meal: String,
    val productName: String,
    val amount: Float,
    val unit: String
)