package study.snacktrackmobile.data.model


data class EssentialFood(
    val id: Int? = null,
    val name: String,
    val authorId: Int? = null,
    val description: String,
    val calories: Float,
    val protein: Float,
    val fat: Float,
    val carbohydrates: Float,
    val servingSizeUnit: String,
    val defaultWeight: Float,
    val brandName: String? = null
)
