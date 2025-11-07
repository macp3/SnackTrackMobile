package study.snacktrackmobile.data.model.dto

import com.google.gson.annotations.SerializedName

data class RegisteredAlimentationResponse(
    val id: Int,
    val userId: Int?,
    val essentialFood: EssentialFoodResponse?,
    // backend field name in DTO is mealApi -> ApiFoodResponseDetailed
    val mealApi: ApiFoodResponseDetailed?,
    // backend returns full Meal entity -> map to MealDto
    val meal: MealResponse?,
    // LocalDate on backend will be serialized as ISO string -> map to String
    @SerializedName("timestamp") val timestamp: String?,
    val amount: Float?,
    val pieces: Int?
)