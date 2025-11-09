package study.snacktrackmobile.data.model.dto

import com.google.gson.annotations.SerializedName
import kotlinx.serialization.Serializable

@Serializable
data class RegisteredAlimentationResponse(
    val id: Int,
    val userId: Int,
    val essentialFood: EssentialFoodResponse? = null,
    val mealApi: ApiFoodResponseDetailed? = null,
    val meal: MealResponse? = null,
    val timestamp: String,
    val amount: Double,
    val pieces: Int,
    val mealName: String? = null
)
