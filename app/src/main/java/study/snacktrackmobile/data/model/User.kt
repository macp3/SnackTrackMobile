package study.snacktrackmobile.data.model

import kotlinx.serialization.Serializable
import study.snacktrackmobile.data.model.enums.Status

@Serializable
data class User(
    val id: Int,
    val name: String,
    val surname: String,
    val email: String,
    val login: String,
    val imageUrl: String? = null,
    val premiumExpiration: String? = null,
    val status: Status,
    val streak: Int
)

@Serializable
data class LoginRequest(
    val email: String,
    val password: String
)

@Serializable
data class LoginResponse(
    val token: String?,
    val showSurvey: Boolean,
    val message: String?
)

@Serializable
data class RegisterRequest(
    val email: String,
    val password: String,
    val name: String,
    val surname: String
)


@Serializable
data class RegisterResponse(
    val message: String
)
