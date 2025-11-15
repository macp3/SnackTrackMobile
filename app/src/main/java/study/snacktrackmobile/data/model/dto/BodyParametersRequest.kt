package study.snacktrackmobile.data.model.dto

import kotlinx.serialization.Serializable
import study.snacktrackmobile.data.model.enums.Sex

@Serializable
data class BodyParametersRequest(
    val sex: Sex,
    val height: Float,
    val weight: Float,
    val age: Int,
    val dailyActivityFactor: Float,
    val dailyActivityTrainingFactor: Float,
    val weeklyWeightChangeTempo: Float,
    val goalWeight: Float
)
