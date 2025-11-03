package study.snacktrackmobile.model

import kotlinx.serialization.Serializable
import study.snacktrackmobile.model.enums.Sex

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
