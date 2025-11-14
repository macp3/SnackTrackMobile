package study.snacktrackmobile.data.model

import study.snacktrackmobile.data.model.dto.RegisteredAlimentationResponse

data class Meal(
    val name: String,
    val kcal: Int,
    val alimentations: List<RegisteredAlimentationResponse> = emptyList()
)
