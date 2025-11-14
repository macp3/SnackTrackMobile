package study.snacktrackmobile.data.model.dto

import kotlinx.serialization.Serializable

@Serializable
data class UserResponse(
    val id: Int,
    val name: String,
    val surname: String,
    val email: String,
    val imageUrl: String?,
    val premiumExpiration: String?,
    val status: String,
    val streak: Int,
)
