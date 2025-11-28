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
import study.snacktrackmobile.data.api.FoodApi
import study.snacktrackmobile.data.model.dto.ApiFoodResponseDetailed
import study.snacktrackmobile.data.model.dto.EssentialFoodRequest
import study.snacktrackmobile.data.model.dto.EssentialFoodResponse
import study.snacktrackmobile.data.model.dto.RegisteredAlimentationResponse
import study.snacktrackmobile.data.storage.TokenStorage

class FoodViewModel(
    private val api: FoodApi,
    private val context: Context
) : ViewModel() {

    val errorMessage = mutableStateOf<String?>(null)
    val success = mutableStateOf(false)
    private var authToken: String? = null

    init {
        viewModelScope.launch {
            try {
                authToken = TokenStorage.getToken(context)
            } catch (e: Exception) {
                errorMessage.value = "Failed to read token: ${e.localizedMessage}"
            }
        }
    }

    fun resetSuccess() { success.value = false }
    fun setError(message: String?) { errorMessage.value = message }

    fun addFood(request: EssentialFoodRequest) {
        val tokenNow = authToken
        if (tokenNow != null) {
            viewModelScope.launch { callAddFood("Bearer $tokenNow", request) }
            return
        }
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

    private suspend fun callAddFood(bearerToken: String, request: EssentialFoodRequest) {
        success.value = false
        try {
            val response = api.addFood(bearerToken, request)
            if (response.isSuccessful) {
                errorMessage.value = null
                success.value = true
            } else {
                val body = try { response.errorBody()?.string() } catch (e: Exception) { null }
                errorMessage.value = body ?: "Backend error: ${response.code()}"
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
                e.printStackTrace()
            }
        }
    }

    private val _combinedResults = MutableStateFlow<List<FoodUiItem>>(emptyList())
    val combinedResults: StateFlow<List<FoodUiItem>> = _combinedResults

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private var searchJob: Job? = null

    fun onSearchQueryChanged(token: String, query: String) {
        searchJob?.cancel()
        if (query.length < 2) {
            _combinedResults.value = emptyList()
            return
        }

        searchJob = viewModelScope.launch {
            delay(600)
            _isLoading.value = true
            try {
                val response = api.searchFood("Bearer $token", query)
                val localItems = response.localResults.map { FoodUiItem.Local(it) }
                val apiItems = response.apiResults.map { FoodUiItem.Api(it) }
                _combinedResults.value = localItems + apiItems
            } catch (e: Exception) {
                e.printStackTrace()
                _combinedResults.value = emptyList()
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun fetchProductDetailsAndNavigate(
        item: FoodUiItem,
        selectedDate: String,
        selectedMeal: String,
        onSuccess: (RegisteredAlimentationResponse) -> Unit
    ) {
        viewModelScope.launch {
            val token = TokenStorage.getToken(context) ?: return@launch
            val isCustomPiece = (item.defaultWeight != null && item.defaultWeight!! > 0f && item.defaultWeight != 100f)
            val amountVal = if (isCustomPiece) 0f else 100f
            val piecesVal = if (isCustomPiece) 1f else 0f

            when (item) {
                is FoodUiItem.Local -> {
                    val response = RegisteredAlimentationResponse(
                        id = -1,
                        userId = 0,
                        essentialFood = item.data,
                        mealApi = null,
                        meal = null,
                        timestamp = selectedDate,
                        amount = amountVal,
                        pieces = piecesVal,
                        mealName = selectedMeal
                    )
                    onSuccess(response)
                }
                is FoodUiItem.Api -> {
                    _isLoading.value = true
                    try {
                        val apiResponse = api.getFoodFromApiById("Bearer $token", item.data.id)

                        if (apiResponse.isSuccessful && apiResponse.body() != null) {
                            val fullDetails = apiResponse.body()!!

                            val response = RegisteredAlimentationResponse(
                                id = -1,
                                userId = 0,
                                essentialFood = null,
                                mealApi = fullDetails,
                                meal = null,
                                timestamp = selectedDate,
                                amount = amountVal,
                                pieces = piecesVal,
                                mealName = selectedMeal
                            )
                            onSuccess(response)
                        } else {
                            val response = RegisteredAlimentationResponse(
                                id = -1,
                                userId = 0,
                                essentialFood = null,
                                mealApi = item.data,
                                meal = null,
                                timestamp = selectedDate,
                                amount = amountVal,
                                pieces = piecesVal,
                                mealName = selectedMeal
                            )
                            onSuccess(response)
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                        val response = RegisteredAlimentationResponse(
                            id = -1,
                            userId = 0,
                            essentialFood = null,
                            mealApi = item.data,
                            meal = null,
                            timestamp = selectedDate,
                            amount = amountVal,
                            pieces = piecesVal,
                            mealName = selectedMeal
                        )
                        onSuccess(response)
                    } finally {
                        _isLoading.value = false
                    }
                }
            }
        }
    }
}

sealed class FoodUiItem {
    abstract val name: String
    abstract val kcal: Float
    abstract val description: String
    abstract val quantityLabel: String
    abstract val defaultWeight: Float?

    protected fun extractWeight(text: String?): Float? {
        if (text.isNullOrBlank()) return null
        val regex = Regex("(\\d+(?:\\.\\d+)?)\\s*(g|ml|l|kg)", RegexOption.IGNORE_CASE)
        val match = regex.find(text) ?: return null
        val valueStr = match.groupValues[1]
        val unitStr = match.groupValues[2].lowercase()
        var value = valueStr.toFloatOrNull() ?: return null
        if (unitStr == "l" || unitStr == "kg") value *= 1000
        if (value < 5f) return null
        return value
    }

    data class Local(val data: EssentialFoodResponse) : FoodUiItem() {
        override val name: String = data.name ?: "Unknown"
        override val description: String = data.description ?: "User Database"

        override val defaultWeight: Float? = data.defaultWeight
            ?: extractWeight(data.servingSizeUnit)
            ?: extractWeight(data.name)

        private val isPiece = (defaultWeight != null && defaultWeight > 0)

        override val kcal: Float = if (isPiece) {
            val cal = data.calories ?: 0.0
            val weight = defaultWeight!!.toDouble()
            val result = (cal / 100.0) * weight
            result.toFloat()
        } else {
            (data.calories ?: 0.0).toFloat()
        }

        override val quantityLabel: String = if (isPiece) {
            "1 piece (${defaultWeight!!.toInt()} ${data.servingSizeUnit ?: "g"})"
        } else {
            "100 ${data.servingSizeUnit ?: "g"}"
        }
    }

    data class Api(val data: ApiFoodResponseDetailed) : FoodUiItem() {
        override val name: String = data.name ?: "Unknown"
        override val description: String = "${data.brandName ?: "Generic"} (Global DB)"

        override val defaultWeight: Float? = data.defaultWeight
            ?: extractWeight(data.name)
            ?: extractWeight(data.quantity)
            ?: extractWeight(data.servingSizeUnit)

        private val isPiece = (defaultWeight != null && defaultWeight > 0 && defaultWeight != 100f)
        private val baseCalorie = (data.calorie ?: 0).toFloat()

        override val kcal: Float = if (isPiece) {
            (baseCalorie / 100f) * defaultWeight!!
        } else {
            baseCalorie
        }

        override val quantityLabel: String = if (isPiece) {
            val unitLabel = if(data.quantity?.contains("ml", true) == true || name.contains("ml", true)) "ml" else "g"
            "1 piece (${defaultWeight!!.toInt()} $unitLabel)"
        } else {
            data.quantity?.replace("Per ", "", ignoreCase = true) ?: "100 g"
        }
    }
}