package study.snacktrackmobile.data.model

import android.os.Build
import kotlinx.serialization.Serializable
import study.snacktrackmobile.data.model.enums.Status
import java.time.Instant
import java.time.format.DateTimeParseException

@Serializable
data class User(
    val id: Int,
    val name: String,
    val surname: String,
    val email: String,
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

fun isUserPremium(expirationDateString: String?): Boolean {
    if (expirationDateString == null) return false

    return try {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val expirationDate = Instant.parse(expirationDateString)
            val now = Instant.now()
            expirationDate.isAfter(now)
        } else {
            false
        }
    } catch (e: DateTimeParseException) {
        false
    }
}