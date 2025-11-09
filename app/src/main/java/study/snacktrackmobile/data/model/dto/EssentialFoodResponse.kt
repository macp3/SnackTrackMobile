package study.snacktrackmobile.data.model.dto

import kotlinx.serialization.Serializable

@Serializable
data class EssentialFoodResponse(
    val id: Int,
    val name: String?,
    val description: String?,
    val calories: Float?,
    val protein: Float?,
    val fat: Float?,
    val carbohydrates: Float?,
    val brandName: String?,
    val defaultWeight: Float?,
    val servingSizeUnit: String?
)