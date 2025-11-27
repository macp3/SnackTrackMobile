package study.snacktrackmobile.viewmodel

import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import retrofit2.HttpException
import study.snacktrackmobile.data.api.FoodApi
import study.snacktrackmobile.data.model.Meal
import study.snacktrackmobile.data.model.Product
import study.snacktrackmobile.data.model.dto.RecipeResponse
import study.snacktrackmobile.data.model.dto.RegisteredAlimentationRequest
import study.snacktrackmobile.data.model.dto.RegisteredAlimentationResponse
import study.snacktrackmobile.data.repository.RegisteredAlimentationRepository
import study.snacktrackmobile.data.services.AiApiService
import study.snacktrackmobile.data.services.AiShoppingRequest
import study.snacktrackmobile.data.storage.TokenStorage
import study.snacktrackmobile.presentation.ui.state.SummaryBarState

class RegisteredAlimentationViewModel(
    private val repository: RegisteredAlimentationRepository,
    private val foodApi: FoodApi,
    private val aiService: AiApiService
) : ViewModel() {

    private val _raw = MutableStateFlow<List<RegisteredAlimentationResponse>>(emptyList())
    val raw: StateFlow<List<RegisteredAlimentationResponse>> = _raw

    private val _meals = MutableStateFlow<List<Meal>>(emptyList())
    val meals: StateFlow<List<Meal>> = _meals

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _products = MutableStateFlow<List<Product>>(emptyList())
    val products: StateFlow<List<Product>> = _products.asStateFlow()

    private val _errorMessage = mutableStateOf<String?>(null)
    val errorMessage: State<String?> = _errorMessage

    private var loadMealsJob: Job? = null

    // 🔹 POMOCNICZA FUNKCJA: Wyciąganie wagi ze stringa (np. "330ml" -> 330.0)
    // To ta sama logika co w FoodViewModel i ProductDetailsScreen
    private fun extractWeight(quantityStr: String?): Float? {
        if (quantityStr == null) return null
        val regex = Regex("(\\d+(?:\\.\\d+)?)\\s*(g|ml|l|kg)", RegexOption.IGNORE_CASE)
        val match = regex.find(quantityStr)

        val valueStr = match?.groupValues?.get(1)
        val unitStr = match?.groupValues?.get(2)?.lowercase()

        var value = valueStr?.toFloatOrNull() ?: return null

        if (unitStr == "l" || unitStr == "kg") {
            value *= 1000
        }
        return value
    }

    fun generateAiDiet(context: Context, date: String, prompt: String = "") {
        viewModelScope.launch {
            val token = TokenStorage.getToken(context) ?: return@launch
            _isLoading.value = true

            try {
                Toast.makeText(context, "Asking AI for a plan...", Toast.LENGTH_SHORT).show()

                val requestBody = AiShoppingRequest(
                    prompt = prompt,
                    productContext = emptyList()
                )

                val dietPlan = aiService.generateDiet("Bearer $token", requestBody)
                var addedCount = 0

                dietPlan.forEach { item ->
                    try {
                        val searchResult = foodApi.searchFood("Bearer $token", item.productName)
                        val bestLocal = searchResult.localResults.firstOrNull()
                        val bestApi = searchResult.apiResults.firstOrNull()
                        val isPiece = item.unit.contains("piece", true) || item.unit.contains("szt", true)

                        if (bestLocal != null) {
                            repository.addEntry(
                                token = token,
                                essentialId = bestLocal.id,
                                mealApiId = null,
                                mealName = item.meal,
                                date = date,
                                amount = if (!isPiece) item.amount else null,
                                pieces = if (isPiece) item.amount else null
                            )
                            addedCount++
                        } else if (bestApi != null) {
                            repository.addEntry(
                                token = token,
                                essentialId = null,
                                mealApiId = bestApi.id,
                                mealName = item.meal,
                                date = date,
                                amount = if (!isPiece) item.amount else null,
                                pieces = if (isPiece) item.amount else null
                            )
                            addedCount++
                        } else {
                            Log.w("AI_DIET", "Product not found for: ${item.productName}")
                        }
                    } catch (e: Exception) {
                        Log.e("AI_DIET", "Failed to add item: ${item.productName}", e)
                    }
                }
                loadMeals(token, date)
                Toast.makeText(context, "Diet generated! Added $addedCount items.", Toast.LENGTH_SHORT).show()

            } catch (e: Exception) {
                Log.e("AI_DIET", "Error generating diet", e)
                Toast.makeText(context, "AI Error: ${e.message}", Toast.LENGTH_LONG).show()
            } finally {
                _isLoading.value = false
            }
        }
    }

    private fun normalizeMealName(name: String): String =
        when (name.lowercase()) {
            "breakfast" -> "Breakfast"
            "lunch" -> "Lunch"
            "dinner" -> "Dinner"
            "supper" -> "Supper"
            "snack" -> "Snack"
            else -> "Other"
        }

    fun loadMeals(token: String, date: String) {
        loadMealsJob?.cancel()

        loadMealsJob = viewModelScope.launch {
            delay(400)
            _isLoading.value = true
            try {
                val list = repository.getMealsForDate(token, date)
                _raw.value = list
                _meals.value = mapToUi(list)
                SummaryBarState.update(_meals.value)
            } catch (e: Exception) {
                Log.e("SnackTrack", "Error loading meals", e)
                _raw.value = emptyList()
                _meals.value = emptyList()
                SummaryBarState.update(emptyList())
            } finally {
                _isLoading.value = false
            }
        }
    }

    // 🔹 FIX: Zaktualizowana logika mapowania z parsowaniem wagi
    private fun mapToUi(input: List<RegisteredAlimentationResponse>): List<Meal> {
        return input
            .groupBy { normalizeMealName(it.mealName ?: "Other") }
            .map { (dailyMealName, entries) ->
                var kcalSum = 0.0

                val recalculatedEntries = entries.map { entry ->
                    if (entry.meal != null) {
                        // --- PRZEPIS (RECIPE) ---
                        val recipeData = entry.meal
                        val totalRecipeKcal = recipeData.ingredients.sumOf { ing ->
                            val ef = ing.essentialFood
                            val api = ing.essentialApi

                            val baseCal = (ef?.calories ?: api?.calorie?.toFloat() ?: 0f).toDouble()

                            // 🔹 FIX: Parsowanie wagi składnika przepisu
                            val determinedWeight = ef?.defaultWeight
                                ?: api?.defaultWeight
                                ?: extractWeight(api?.quantity)
                                ?: extractWeight(api?.servingSizeUnit)
                                ?: 100f

                            val baseWeight = (if(determinedWeight > 0) determinedWeight else 100f).toDouble()

                            val iAmount = ing.amount ?: 0f
                            val iPieces = ing.pieces ?: 0f

                            val ratio = when {
                                iPieces > 0 -> (iPieces * baseWeight) / 100.0
                                iAmount > 0 -> iAmount.toDouble() / 100.0
                                else -> 0.0
                            }
                            baseCal * ratio
                        }
                        val servings = entry.pieces ?: 1f
                        kcalSum += (totalRecipeKcal * servings)
                        entry
                    } else {
                        // --- POJEDYNCZY PRODUKT ---
                        val ef = entry.essentialFood
                        val api = entry.mealApi

                        val baseCal = (ef?.calories ?: api?.calorie?.toFloat() ?: 0f).toDouble()

                        // 🔹 FIX: Parsowanie wagi dla pojedynczego produktu
                        // Teraz szukamy w quantity ("330ml") i servingSizeUnit, zanim użyjemy fallbacku 100f
                        val determinedWeight = ef?.defaultWeight
                            ?: api?.defaultWeight
                            ?: extractWeight(api?.quantity)
                            ?: extractWeight(api?.servingSizeUnit)
                            ?: 100f

                        val baseWeight = (if(determinedWeight > 0) determinedWeight else 100f).toDouble()

                        val ratio = when {
                            (entry.pieces ?: 0f) > 0 -> ((entry.pieces!!.toDouble()) * baseWeight) / 100.0
                            (entry.amount ?: 0f) > 0 -> (entry.amount!!.toDouble()) / 100.0
                            else -> 0.0
                        }
                        kcalSum += (baseCal * ratio)
                        entry
                    }
                }

                Meal(
                    name = dailyMealName,
                    kcal = kcalSum.toInt(),
                    alimentations = recalculatedEntries
                )
            }
    }

    fun updateMealProduct(context: Context, productId: Int, dto: RegisteredAlimentationRequest, date: String) {
        viewModelScope.launch {
            val token = TokenStorage.getToken(context) ?: return@launch
            try {
                repository.updateEntry(token, productId, dto)
                loadMeals(token, date)
            } catch (e: Exception) {
                _errorMessage.value = e.message
            }
        }
    }

    fun deleteEntry(token: String, id: Int) {
        viewModelScope.launch {
            try {
                repository.deleteEntry(token, id)
                val updatedRaw = _raw.value.filterNot { it.id == id }
                _raw.value = updatedRaw
                _meals.value = mapToUi(updatedRaw)
                SummaryBarState.update(_meals.value)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun addMealProduct(context: Context, essentialId: Int? = null, mealApiId: Int? = null, mealName: String, date: String, amount: Float? = null, pieces: Float? = null) {
        viewModelScope.launch {
            val token = TokenStorage.getToken(context) ?: return@launch
            try {
                repository.addEntry(token, essentialId, mealApiId, null, mealName, date, amount, pieces)
                loadMeals(token, date)
            } catch (e: Exception) {
                _errorMessage.value = "Error adding product: ${e.message}"
                e.printStackTrace()
            }
        }
    }

    fun copyMeal(context: Context, fromDate: String, fromMealName: String, toDate: String, toMealName: String) {
        viewModelScope.launch {
            val token = TokenStorage.getToken(context) ?: return@launch
            try {
                repository.copyMeal(token, fromDate, fromMealName.lowercase(), toDate, toMealName.lowercase())
                loadMeals(token, toDate)
                Toast.makeText(context, "Meal copied!", Toast.LENGTH_SHORT).show()
            } catch (e: HttpException) {
                Toast.makeText(context, "Copy failed: ${e.code()}", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                Toast.makeText(context, "Unexpected error", Toast.LENGTH_SHORT).show()
            }
        }
    }

    fun addRecipeToMeal(context: Context, recipe: RecipeResponse, date: String, mealName: String, servings: Float) {
        viewModelScope.launch {
            val token = TokenStorage.getToken(context) ?: return@launch
            try {
                val success = repository.addEntry(token, null, null, recipe.id, mealName, date, null, servings)
                if (success) {
                    loadMeals(token, date)
                    Toast.makeText(context, "Recipe added to $mealName", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(context, "Failed to add recipe", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    companion object {
        fun provideFactory(
            repository: RegisteredAlimentationRepository,
            foodApi: FoodApi,
            aiService: AiApiService
        ): ViewModelProvider.Factory {
            return object : ViewModelProvider.Factory {
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    if (modelClass.isAssignableFrom(RegisteredAlimentationViewModel::class.java)) {
                        @Suppress("UNCHECKED_CAST")
                        return RegisteredAlimentationViewModel(repository, foodApi, aiService) as T
                    }
                    throw IllegalArgumentException("Unknown ViewModel class")
                }
            }
        }
    }
}