package study.snacktrackmobile.data.repository

import org.json.JSONObject
import retrofit2.Response
import study.snacktrackmobile.data.api.Request
import study.snacktrackmobile.data.model.LoginRequest
import study.snacktrackmobile.data.model.LoginResponse
import study.snacktrackmobile.data.model.RegisterRequest
import android.content.Context
import android.util.Log
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.RequestBody.Companion.toRequestBody
import study.snacktrackmobile.data.storage.TokenStorage
import study.snacktrackmobile.data.network.ApiConfig

class UserRepository {

    suspend fun login(email: String, password: String, context: Context): LoginResponse {
        val response: Response<LoginResponse> = Request.api.login(LoginRequest(email, password))

        if (!response.isSuccessful) {
            val errorBody = response.errorBody()?.string()
            val message = try {
                JSONObject(errorBody ?: "").optString("message", "Unknown error")
            } catch (e: Exception) {
                "Unknown error"
            }
            throw Exception(message)
        }

        val body = response.body() ?: throw Exception("Empty response")

        withContext(Dispatchers.IO) {
            body.token?.let { TokenStorage.saveToken(context, it) }
        }

        Log.d("Auth", "JWT token saved: ${body.token}")

        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val fcmToken = task.result
                Log.d("Auth", "FCM token obtained: $fcmToken")
                sendDeviceTokenToServer(context, fcmToken)
            } else {
                Log.e("Auth", "Failed to fetch FCM token", task.exception)
            }
        }

        return body
    }

    private fun sendDeviceTokenToServer(context: Context, fcmToken: String) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val jwt = TokenStorage.getToken(context) ?: return@launch

                val json = JSONObject().apply {
                    put("token", fcmToken)
                }

                val body = json.toString().toRequestBody("application/json".toMediaType())
                val request = okhttp3.Request.Builder()
                    .url("${ApiConfig.BASE_URL}/users/device-token")
                    .post(body)
                    .addHeader("Authorization", "Bearer $jwt")
                    .build()

                val client = OkHttpClient()
                val response = client.newCall(request).execute()

                if (response.isSuccessful) {
                    Log.d("Auth", "FCM token sent successfully")
                } else {
                    Log.e("Auth", "Failed to send FCM token: ${response.message}")
                }

            } catch (e: Exception) {
                Log.e("Auth", "Error sending FCM token", e)
            }
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

    // Pobiera ID zalogowanego użytkownika
    suspend fun getUserId(token: String): Result<Int> {
        return try {
            val res = Request.api.getUserID("Bearer $token")
            if (res.isSuccessful) {
                val id = res.body()
                if (id != null) {
                    Result.success(id)
                } else {
                    Result.failure(Exception("Empty body"))
                }
            } else {
                val errorBody = res.errorBody()?.string()
                val msg = if (errorBody.isNullOrBlank()) "Error ${res.code()}" else errorBody
                Result.failure(Exception(msg))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

}
