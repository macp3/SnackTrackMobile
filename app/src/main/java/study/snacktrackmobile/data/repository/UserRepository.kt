package study.snacktrackmobile.data.repository

import org.json.JSONObject
import retrofit2.Response
import study.snacktrackmobile.data.api.Request
import study.snacktrackmobile.data.model.LoginRequest
import study.snacktrackmobile.data.model.LoginResponse
import study.snacktrackmobile.data.model.RegisterRequest

class UserRepository {

    suspend fun login(email: String, password: String): LoginResponse {
        val response: Response<LoginResponse> = Request.api.login(LoginRequest(email, password))

        return if (response.isSuccessful) {
            response.body() ?: throw Exception("Empty response")
        } else {
            val errorBody = response.errorBody()?.string()
            // parsujemy JSON żeby wyciągnąć dokładny message
            val message = try {
                JSONObject(errorBody ?: "").optString("message", "Unknown error")
            } catch (e: Exception) {
                "Unknown error"
            }
            throw Exception(message)
        }
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
