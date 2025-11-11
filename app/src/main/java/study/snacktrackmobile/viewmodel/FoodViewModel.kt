package study.snacktrackmobile.viewmodel

import android.content.Context
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import retrofit2.Response
import study.snacktrackmobile.data.api.FoodApi
import study.snacktrackmobile.data.model.dto.EssentialFoodRequest
import study.snacktrackmobile.data.model.dto.EssentialFoodResponse
import study.snacktrackmobile.data.storage.TokenStorage

class FoodViewModel(
    private val api: FoodApi,
    private val context: Context
) : ViewModel() {

    // UI state
    val errorMessage = mutableStateOf<String?>(null)
    val success = mutableStateOf(false)

    // lokalne przechowanie tokena pobranego asynchronicznie
    private var authToken: String? = null

    init {
        // POBIERANIE TOKENA TYLKO W KORUTYNIE
        viewModelScope.launch {
            try {
                authToken = TokenStorage.getToken(context)
            } catch (e: Exception) {
                // jeśli DataStore coś zwróci nie tak — ustaw błąd widoczny w UI
                errorMessage.value = "Failed to read token: ${e.localizedMessage}"
            }
        }
    }

    fun setError(message: String?) {
        errorMessage.value = message
    }

    /**
     * Wywoływane z UI (nie-suspend). Tutaj token już powinien być w polu
     * authToken (pobrany w init). Jeśli go jeszcze nie ma, robimy fetch w korutinie
     * i dopiero wtedy wysyłamy request.
     */
    fun addFood(request: EssentialFoodRequest) {
        // jeśli token już jest — użyjemy go bez czekania
        val tokenNow = authToken
        if (tokenNow != null) {
            // wykonaj request w korutinie
            viewModelScope.launch {
                callAddFood("Bearer $tokenNow", request)
            }
            return
        }

        // token jeszcze nie pobrany — pobierz i wykonaj request
        viewModelScope.launch {
            try {
                authToken = TokenStorage.getToken(context)
                val fetched = authToken
                if (fetched == null) {
                    setError("No auth token. Please login.")
                    return@launch
                }
                callAddFood("Bearer $fetched", request)
            } catch (e: Exception) {
                setError("Failed to get token: ${e.localizedMessage}")
            }
        }
    }

    /**
     * Wspólna wewnętrzna funkcja robiąca wywołanie sieciowe — wywoływana tylko z korutyny
     */
    private suspend fun callAddFood(bearerToken: String, request: EssentialFoodRequest) {
        try {
            val response = api.addFood(bearerToken, request)

            if (response.isSuccessful) {
                errorMessage.value = null
                success.value = true
            } else {
                // postaraj się odczytać body z błędem
                val body = try {
                    response.errorBody()?.string()
                } catch (e: Exception) {
                    null
                }
                errorMessage.value = body ?: "Backend error: ${response.code()} ${response.message()}"
                success.value = false
            }
        } catch (e: Exception) {
            errorMessage.value = "Network error: ${e.localizedMessage}"
            success.value = false
        }
    }

    private val _foods = MutableStateFlow<List<EssentialFoodResponse>>(emptyList())
    val foods: StateFlow<List<EssentialFoodResponse>> = _foods

    fun fetchAllFoods(token: String) {
        viewModelScope.launch {
            try {
                val result = api.getAllFoods("Bearer $token")
                _foods.value = result
            } catch (e: Exception) {
                println("❌ Error fetching foods: ${e.message}")
            }
        }
    }
}
