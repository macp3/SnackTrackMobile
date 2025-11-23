package study.snacktrackmobile.data.model.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import study.snacktrackmobile.data.model.Ingredient

@Serializable
data class IngredientResponse(
    @SerialName("id") val id: Int,
    @SerialName("amount") val amount: Float? = null,
    @SerialName("pieces") val pieces: Float? = null,

    // To musi pasować do JSON: "essentialApi"
    @SerialName("essentialApi")
    val essentialApi: ApiFoodResponseDetailed? = null,

    // To musi pasować do JSON: "essentialFood"
    @SerialName("essentialFood")
    val essentialFood: EssentialFoodResponse? = null
)