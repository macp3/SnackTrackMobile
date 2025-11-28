package study.snacktrackmobile.data.model.dto

import com.google.gson.annotations.SerializedName
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ApiFoodResponseDetailed(
    @SerialName("id") val id: Int,
    @SerialName("name") val name: String? = null,
    @SerialName("calorie") val calorie: Int? = null,
    @SerialName("protein") val protein: Float? = null,
    @SerialName("carbohydrates") val carbohydrates: Float? = null,
    @SerialName("fat") val fat: Float? = null,
    @SerialName("defaultWeight") val defaultWeight: Float? = null,
    @SerialName("servingSizeUnit") val servingSizeUnit: String? = null,
    @SerialName("brandName") val brandName: String? = null,
    @SerialName("quantity") val quantity: String? = null
)