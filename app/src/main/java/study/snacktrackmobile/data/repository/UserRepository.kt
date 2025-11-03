package study.snacktrackmobile.data.repository

import okhttp3.ResponseBody
import study.snacktrackmobile.data.api.Request
import study.snacktrackmobile.data.model.LoginRequest
import study.snacktrackmobile.data.model.LoginResponse


class UserRepository {
    suspend fun login(email: String, password: String): LoginResponse
    {
        val response: ResponseBody = Request.api.login(LoginRequest(email, password))
        val token = response.string()
        return LoginResponse(token = token)
    }
}
