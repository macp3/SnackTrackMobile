package study.snacktrackmobile.data.model


data class EssentialFood(
    val id: Int? = null,              // w backendzie generowane automatycznie
    val name: String,
    val authorId: Int? = null,        // ustawiane po stronie backendu
    val description: String,
    val calories: Float,
    val protein: Float,
    val fat: Float,
    val carbohydrates: Float,
    val servingSizeUnit: String,
    val defaultWeight: Float,
    val brandName: String? = null     // opcjonalne pole
)
