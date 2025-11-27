package study.snacktrackmobile.data.model.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Zaktualizowany DTO dla wpisów z rejestru żywieniowego.
 *
 * Upewniamy się, że nazwy pól odpowiadają dokładnie nazwom z JSON-a
 * i że wszystkie potencjalnie brakujące (null) pola mają '?' i wartość domyślną 'null'.
 */
@Serializable
data class RegisteredAlimentationResponse(
    // Pola proste
    val id: Int,
    val userId: Int,

    // --- POLA ZŁOŻONE (SĄ null, jeśli nie jest to ten typ wpisu) ---

    // Klucz 'essentialFood' z backendu (nasz banan)
    @SerialName("essentialFood")
    val essentialFood: EssentialFoodResponse? = null,

    // Klucz 'mealApi' z backendu (zewnętrzne API)
    @SerialName("mealApi")
    val mealApi: ApiFoodResponseDetailed? = null,

    // Klucz 'meal' z backendu (nasz przepis/meal)
    @SerialName("meal")
    val meal: RecipeResponse? = null,

    // --- DANE REJESTRU ---
    val timestamp: String, // YYYY-MM-DD

    // Pola ilościowe
    val amount: Float? = null,
    val pieces: Float? = null,

    // Nazwa posiłku (np. "dinner", "snack")
    val mealName: String? = null
)


fun RegisteredAlimentationResponse.getDisplayName(): String {
    return essentialFood?.name
        ?: mealApi?.name
        ?: "Unknown Product"
}

// -----------------------------------------------------------------------------
// FUNKCJE ROZSZERZAJĄCE (EXTENSION FUNCTIONS)
// Tutaj następuje bezpieczna konwersja Double (z JSON) na Float (dla UI)
// -----------------------------------------------------------------------------

// Pobiera kalorie na 100g/porcję bazową
fun RegisteredAlimentationResponse.getBaseCalories(): Float {
    // essentialFood.calories jest teraz Double?, więc rzutujemy na Float
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

// Pobiera domyślną wagę (Api często nie ma wagi, więc zakładamy 100g lub 0 jeśli to sztuki)
fun RegisteredAlimentationResponse.getDefaultWeight(): Float {
    // defaultWeight w EssentialFoodResponse pozostał Float?, więc tu bez zmian
    return essentialFood?.defaultWeight
        ?: mealApi?.defaultWeight
        ?: 100f
}