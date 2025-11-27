package study.snacktrackmobile.data.model.dto

import kotlinx.serialization.Serializable

@Serializable
data class EssentialFoodResponse(
    val id: Int,
    val name: String? = null,
    val description: String? = null,
    val calories: Double? = null,
    val protein: Double? = null,
    val fat: Double? = null,
    val carbohydrates: Double? = null,
    val brandName: String? = null,
    val defaultWeight: Float? = null, // To pole może zostać Float, bo jest używane do wagi.
    val servingSizeUnit: String? = null
)