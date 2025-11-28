package study.snacktrackmobile.data.model.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class RegisteredAlimentationResponse(
    val id: Int,
    val userId: Int,
    @SerialName("essentialFood")
    val essentialFood: EssentialFoodResponse? = null,

    @SerialName("mealApi")
    val mealApi: ApiFoodResponseDetailed? = null,

    @SerialName("meal")
    val meal: RecipeResponse? = null,

    val timestamp: String,

    val amount: Float? = null,
    val pieces: Float? = null,

    val mealName: String? = null
)


fun RegisteredAlimentationResponse.getDisplayName(): String {
    return essentialFood?.name
        ?: mealApi?.name
        ?: "Unknown Product"
}

fun RegisteredAlimentationResponse.getBaseCalories(): Float {
    return essentialFood?.calories?.toFloat()
        ?: mealApi?.calorie?.toFloat()
        ?: 0f
}

fun RegisteredAlimentationResponse.getBaseProtein(): Float {
    return essentialFood?.protein?.toFloat()
        ?: mealApi?.protein?.toFloat()
        ?: 0f
}

fun RegisteredAlimentationResponse.getBaseFat(): Float {
    return essentialFood?.fat?.toFloat()
        ?: mealApi?.fat?.toFloat()
        ?: 0f
}

fun RegisteredAlimentationResponse.getBaseCarbs(): Float {
    return essentialFood?.carbohydrates?.toFloat()
        ?: mealApi?.carbohydrates?.toFloat()
        ?: 0f
}

fun RegisteredAlimentationResponse.getDefaultWeight(): Float {
    return essentialFood?.defaultWeight
        ?: mealApi?.defaultWeight
        ?: 100f
}