package study.snacktrackmobile.data.model.dto

import com.google.gson.annotations.SerializedName
import kotlinx.serialization.Serializable

@Serializable
data class RegisteredAlimentationResponse(
    val id: Int,
    val userId: Int,
    val essentialFood: EssentialFoodResponse? = null,
    val mealApi: ApiFoodResponseDetailed? = null,
    val meal: RecipeResponse? = null,
    val timestamp: String,
    val amount: Float?,
    val pieces: Float? = null,
    val mealName: String? = null
)
