package study.snacktrackmobile.viewmodel

import android.content.Context
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import retrofit2.Response
import study.snacktrackmobile.data.api.FoodApi
import study.snacktrackmobile.data.api.Request.foodApi
import study.snacktrackmobile.data.model.dto.ApiFoodResponseDetailed
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

    // Przechowujemy połączoną listę, żeby UI miało łatwiej
    private val _combinedResults = MutableStateFlow<List<FoodUiItem>>(emptyList())
    val combinedResults: StateFlow<List<FoodUiItem>> = _combinedResults

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private var searchJob: Job? = null

    fun onSearchQueryChanged(token: String, query: String) {
        searchJob?.cancel()
        if (query.length < 2) { // Nie szukaj dla 1 litery
            _combinedResults.value = emptyList()
            return
        }

        searchJob = viewModelScope.launch {
            delay(600) // Debounce
            _isLoading.value = true
            try {
                val response = foodApi.searchFood("Bearer $token", query)

                // Mapujemy wszystko na wspólny model UI
                val localItems = response.localResults.map { FoodUiItem.Local(it) }
                val apiItems = response.apiResults.map { FoodUiItem.Api(it) }

                // Łączymy listy (lokalne najpierw)
                _combinedResults.value = localItems + apiItems

            } catch (e: Exception) {
                e.printStackTrace()
                _combinedResults.value = emptyList()
            } finally {
                _isLoading.value = false
            }
        }
    }
}

sealed class FoodUiItem {
    abstract val name: String
    abstract val kcal: Float
    abstract val description: String
    abstract val quantityLabel: String

    data class Local(val data: EssentialFoodResponse) : FoodUiItem() {
        override val name: String = data.name ?: "Unknown"
        override val description: String = data.description ?: "User Database"

        // Logic: If defaultWeight exists, calculate for 1 piece. Else 100g.
        private val hasDefaultWeight = (data.defaultWeight != null && data.defaultWeight > 0)

        override val kcal: Float = if (hasDefaultWeight) {
            // Formula: (kcal per 100g / 100) * weight of 1 piece
            ((data.calories ?: 0f) / 100f) * data.defaultWeight!!
        } else {
            data.calories ?: 0f
        }

        override val quantityLabel: String = if (hasDefaultWeight) {
            "1 piece (${data.defaultWeight!!.toInt()} ${data.servingSizeUnit ?: "g"})"
        } else {
            "100 ${data.servingSizeUnit ?: "g"}"
        }
    }

    data class Api(val data: ApiFoodResponseDetailed) : FoodUiItem() {
        override val name: String = data.name ?: "Unknown"
        override val description: String = "${data.brandName ?: "Generic"} (Global DB)"

        // Check if API explicitly says "piece" or if we have a weight to convert
        private val isPieceDefinedInApi = data.quantity?.contains("piece", ignoreCase = true) == true
        private val hasDefaultWeight = (data.defaultWeight != null && data.defaultWeight > 0f)
        private val baseCalorie = (data.calorie ?: 0).toFloat()

        override val kcal: Float = when {
            isPieceDefinedInApi -> baseCalorie // API already gave kcal for 1 piece
            hasDefaultWeight -> (baseCalorie / 100f) * data.defaultWeight!! // Convert 100g to 1 piece
            else -> baseCalorie // Fallback (usually 100g)
        }

        override val quantityLabel: String = when {
            isPieceDefinedInApi -> "1 piece"
            hasDefaultWeight -> "1 piece (${data.defaultWeight!!.toInt()} ${data.servingSizeUnit ?: "g"})"
            else -> data.quantity?.replace("Per ", "", ignoreCase = true) ?: "100 g"
        }
    }
}