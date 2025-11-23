package study.snacktrackmobile.data.model.dto

import com.google.gson.annotations.SerializedName
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class RegisteredAlimentationResponse(
    val id: Int,
    val userId: Int,

    @SerialName("essentialFood")
    val essentialFood: EssentialFoodResponse? = null,

    // Tutaj w widoku mapujemy 'essentialApi' na 'mealApi',
    // więc typ musi być ApiFoodResponseDetailed
    @SerialName("mealApi")
    val mealApi: ApiFoodResponseDetailed? = null,

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

// Pobiera kalorie na 100g/porcję bazową
fun RegisteredAlimentationResponse.getBaseCalories(): Float {
    return essentialFood?.calories
        ?: mealApi?.calorie?.toFloat()
        ?: 0f
}

fun RegisteredAlimentationResponse.getBaseProtein(): Float {
    return essentialFood?.protein
        ?: mealApi?.protein
        ?: 0f
}

fun RegisteredAlimentationResponse.getBaseFat(): Float {
    return essentialFood?.fat
        ?: mealApi?.fat
        ?: 0f
}

fun RegisteredAlimentationResponse.getBaseCarbs(): Float {
    return essentialFood?.carbohydrates
        ?: mealApi?.carbohydrates
        ?: 0f
}

// Pobiera domyślną wagę (Api często nie ma wagi, więc zakładamy 100g lub 0 jeśli to sztuki)
fun RegisteredAlimentationResponse.getDefaultWeight(): Float {
    return essentialFood?.defaultWeight
        ?: mealApi?.defaultWeight
        ?: 100f
}