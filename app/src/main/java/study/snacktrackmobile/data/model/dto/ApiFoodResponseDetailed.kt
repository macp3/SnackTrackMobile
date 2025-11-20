package study.snacktrackmobile.data.model.dto

import com.google.gson.annotations.SerializedName
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ApiFoodResponseDetailed(
    val id: Int,
    val name: String? = null,

    @SerialName("calorie")
    val calorie: Int? = null,

    val protein: Float? = null,
    val carbohydrates: Float? = null,
    val fat: Float? = null,

    val defaultWeight: Float? = null,
    val servingSizeUnit: String? = null,

    val brandName: String? = null,

    val quantity: String? = null
)