package study.snacktrackmobile.data.model.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class UnifiedSearchResponse(
    @SerialName("localResults")
    val localResults: List<EssentialFoodResponse> = emptyList(),

    @SerialName("apiResults")
    val apiResults: List<ApiFoodResponseDetailed> = emptyList()
)