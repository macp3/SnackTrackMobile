package study.snacktrackmobile.data.model.dto

import com.google.gson.annotations.SerializedName
import kotlinx.serialization.Serializable

@Serializable
data class ApiFoodResponseDetailed(
    val id: Int,
    val name: String?,
    @SerializedName("calorie") val calories: Int?,
    val protein: Float?,
    val carbohydrates: Float?,
    val fat: Float?,
    val defaultWeight: Float?,
    val servingSizeUnit: String?,
    val brandName: String?
)
