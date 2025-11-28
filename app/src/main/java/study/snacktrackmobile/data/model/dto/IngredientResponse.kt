package study.snacktrackmobile.data.model.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import study.snacktrackmobile.data.model.Ingredient

@Serializable
data class IngredientResponse(
    @SerialName("id") val id: Int,
    @SerialName("amount") val amount: Float? = null,
    @SerialName("pieces") val pieces: Float? = null,
    @SerialName("essentialApi")
    val essentialApi: ApiFoodResponseDetailed? = null,
    @SerialName("essentialFood")
    val essentialFood: EssentialFoodResponse? = null
)