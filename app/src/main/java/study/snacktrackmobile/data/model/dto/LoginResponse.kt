package study.snacktrackmobile.data.model.dto

import kotlinx.serialization.Serializable

@Serializable
data class LoginResponse(
    val token: String?,
    val showSurvey: Boolean,
    val message: String? = null
)