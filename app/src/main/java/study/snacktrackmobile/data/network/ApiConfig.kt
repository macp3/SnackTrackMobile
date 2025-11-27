package study.snacktrackmobile.data.network

object ApiConfig {
    const val BASE_URL = "http://192.168.33.4:8080"

    const val LOGIN_URL = "$BASE_URL/auth/login"
    const val REGISTER_URL = "$BASE_URL/auth/register"
    const val DEVICE_TOKEN_URL = "$BASE_URL/users/device-token"
}