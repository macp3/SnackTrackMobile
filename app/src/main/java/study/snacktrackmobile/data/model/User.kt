package study.snacktrackmobile.data.model

import kotlinx.serialization.Serializable
import study.snacktrackmobile.data.model.enums.DietTypes
import study.snacktrackmobile.data.model.enums.Status

@Serializable
data class LoginRequest(
    val email: String,
    val password: String
)

@Serializable
data class LoginResponse(
    val token: String
)

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
    val prefferedDiet: DietTypes? = null,
    val streak: Int
)
