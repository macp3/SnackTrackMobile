package study.snacktrackmobile.data.network

object ApiConfig {
    const val BASE_URL = "https://projekt-inzynierski-production.up.railway.app"

    const val LOGIN_URL = "$BASE_URL/auth/login"
    const val REGISTER_URL = "$BASE_URL/auth/register"
    const val DEVICE_TOKEN_URL = "$BASE_URL/users/device-token"
}