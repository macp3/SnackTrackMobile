package study.snacktrackmobile.data.repository

import retrofit2.Response
import study.snacktrackmobile.data.api.Request
import study.snacktrackmobile.data.model.LoginRequest
import study.snacktrackmobile.data.model.LoginResponse
import study.snacktrackmobile.data.model.RegisterRequest

class UserRepository {

    suspend fun login(email: String, password: String): LoginResponse {
        // Retrofit automatically deserializes response
        val response: Response<LoginResponse> =
            Request.api.login(LoginRequest(email, password))

        if (response.isSuccessful) {
            return response.body() ?: throw Exception("Empty response")
        }

        // Pobranie komunikatu z backendu (Account not activated, Invalid credentials, etc.)
        val message = response.errorBody()?.string() ?: "Unknown error"
        throw Exception(message)
    }

    suspend fun register(email: String, password: String, name: String, surname: String): Result<String> {
        return try {
            val response = Request.api.register(RegisterRequest(email, password, name, surname))

            if (response.isSuccessful) {
                Result.success(response.body()?.string() ?: "Registration successful")
            } else {
                val errorMessage = response.errorBody()?.string() ?: "Unknown error"
                Result.failure(Exception(errorMessage))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
