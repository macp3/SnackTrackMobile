package study.snacktrackmobile.viewmodel

import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import retrofit2.HttpException
import study.snacktrackmobile.data.model.Meal
import study.snacktrackmobile.data.model.Product
import study.snacktrackmobile.data.model.dto.RecipeResponse
import study.snacktrackmobile.data.model.dto.RegisteredAlimentationRequest
import study.snacktrackmobile.data.model.dto.RegisteredAlimentationResponse
import study.snacktrackmobile.data.repository.RegisteredAlimentationRepository
import study.snacktrackmobile.data.storage.TokenStorage
import study.snacktrackmobile.presentation.ui.state.SummaryBarState

class RegisteredAlimentationViewModel(private val repository: RegisteredAlimentationRepository) :
    ViewModel() {

    private val _raw = MutableStateFlow<List<RegisteredAlimentationResponse>>(emptyList())
    val raw: StateFlow<List<RegisteredAlimentationResponse>> = _raw

    private val _meals = MutableStateFlow<List<Meal>>(emptyList())
    val meals: StateFlow<List<Meal>> = _meals

    private val _products = MutableStateFlow<List<Product>>(emptyList())
    val products: StateFlow<List<Product>> = _products.asStateFlow()

    private val _errorMessage = mutableStateOf<String?>(null)
    val errorMessage: State<String?> = _errorMessage

    // tymczasowo
    private val allProducts = listOf(
        Product(1, "Bread", "100 g", 247, 13f, 4.2f, 41f),
        Product(2, "Milk", "200 ml", 60, 3.2f, 3.3f, 5f),
        Product(3, "Cheese", "30 g", 402, 25f, 33f, 1.3f),
        Product(4, "Tomato", "100 g", 18, 0.9f, 0.2f, 3.9f),
        Product(5, "Yogurt", "150 g", 59, 10f, 0.4f, 3.6f)
    )

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
        viewModelScope.launch {
            try {
                val list = repository.getMealsForDate(token, date)
                _raw.value = list
                _meals.value = mapToUi(list)
                SummaryBarState.update(_meals.value)

            } catch (e: Exception) {
                e.printStackTrace()
                _raw.value = emptyList()
                _meals.value = emptyList()
                SummaryBarState.update(emptyList())
            }
        }
    }

    private fun mapToUi(input: List<RegisteredAlimentationResponse>): List<Meal> {
        return input
            // Grupowanie po porze dnia (mealName)
            .groupBy { normalizeMealName(it.mealName ?: "Other") }
            .map { (dailyMealName, entries) ->

                var kcalSum = 0.0

                val recalculatedEntries = entries.map { entry ->
                    // SPRAWDZAMY CZY TO PRZEPIS (Backend: Meal)
                    if (entry.meal != null) {
                        // Mamy cały obiekt przepisu w entry.meal
                        val recipeData = entry.meal

                        // Sumujemy kcal ze wszystkich składników przepisu
                        val totalRecipeKcal = recipeData.ingredients.sumOf { ing ->
                            val ef = ing.essentialFood
                            val api = ing.essentialApi

                            // Logika wyciągania kalorii z EssentialFood lub Api
                            val baseCal = (ef?.calories ?: api?.calorie?.toFloat() ?: 0f).toDouble()
                            val baseWeight = (ef?.defaultWeight ?: 100f).toDouble()

                            val iAmount = ing.amount ?: 0f
                            val iPieces = ing.pieces ?: 0f

                            val ratio = when {
                                iPieces > 0 -> (iPieces * baseWeight) / 100.0
                                iAmount > 0 -> iAmount.toDouble() / 100.0
                                else -> 0.0
                            }
                            baseCal * ratio
                        }

                        // Mnożymy przez ilość porcji zapisaną w dzienniku
                        val servings = entry.pieces ?: 1f
                        kcalSum += (totalRecipeKcal * servings)

                        entry // Zwracamy wpis (ProductRow go wyświetli)
                    }
                    // SPRAWDZAMY CZY TO ZWYKŁY PRODUKT
                    else {
                        val ef = entry.essentialFood
                        val api = entry.mealApi
                        val baseCal = (ef?.calories ?: api?.calorie?.toFloat() ?: 0f).toDouble()
                        val baseWeight = (ef?.defaultWeight ?: 100f).toDouble()

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

    fun updateMealProduct(
        context: Context,
        productId: Int,
        dto: RegisteredAlimentationRequest,
        date: String
    ) {
        viewModelScope.launch {
            val token = TokenStorage.getToken(context)
            if (token != null) {
                try {
                    repository.updateEntry(token, productId, dto)
                    loadMeals(token, date) // odświeżenie UI
                } catch (e: Exception) {
                    _errorMessage.value = e.message
                }
            }
        }
    }

    fun deleteEntry(token: String, id: Int) {
        viewModelScope.launch {
            try {
                repository.deleteEntry(token, id)
                _raw.value = _raw.value.filterNot { it.id == id }
                _meals.value = mapToUi(_raw.value)
                SummaryBarState.update(_meals.value)

            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun searchProducts(query: String) {
        _products.value = if (query.isBlank()) allProducts
        else allProducts.filter { it.name.contains(query, ignoreCase = true) }
    }

    // 🔹 ZAKTUALIZOWANA FUNKCJA ADD
    fun addMealProduct(
        context: Context,
        essentialId: Int? = null, // Teraz opcjonalne
        mealApiId: Int? = null,   // Nowe pole
        mealName: String,
        date: String,
        amount: Float? = null,
        pieces: Float? = null
    ) {
        viewModelScope.launch {
            val token = TokenStorage.getToken(context) ?: return@launch

            try {
                // UWAGA: Musisz upewnić się, że w Repository funkcja addEntry też przyjmuje mealApiId!
                repository.addEntry(
                    token = token,
                    essentialId = essentialId,
                    mealApiId = mealApiId, // Przekazujemy ID z API
                    mealName = mealName,
                    date = date,
                    amount = amount,
                    pieces = pieces
                )
                loadMeals(token, date)
            } catch (e: Exception) {
                _errorMessage.value = "Error adding product: ${e.message}"
                e.printStackTrace()
            }
        }
    }

    fun copyMeal(
        context: Context,
        fromDate: String,
        fromMealName: String,
        toDate: String,
        toMealName: String
    ) {
        viewModelScope.launch {
            val token = TokenStorage.getToken(context) ?: return@launch
            try {
                repository.copyMeal(token, fromDate, fromMealName.lowercase(), toDate, toMealName.lowercase())
                loadMeals(token, toDate) // odśwież dane
            } catch (e: HttpException) {
                Log.e("CopyMeal", "HTTP error: ${e.code()} ${e.message()}")
                Toast.makeText(context, "Copy failed: ${e.code()}", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                Log.e("CopyMeal", "Unexpected error: ${e.message}")
                Toast.makeText(context, "Unexpected error", Toast.LENGTH_SHORT).show()
            }
        }
    }

    fun addRecipeToMeal(
        context: Context,
        recipe: RecipeResponse, // Przepis z UI
        date: String,
        mealName: String,
        servings: Float
    ) {
        viewModelScope.launch {
            val token = TokenStorage.getToken(context) ?: return@launch

            try {
                // Wysyłamy JEDEN request z ID przepisu (recipe.id -> mealId)
                // Używamy nullable dla essentialId
                val success = repository.addEntry(
                    token = token,
                    mealId = recipe.id,
                    essentialId = null,
                    mealApiId = null,
                    mealName = mealName,
                    date = date,
                    pieces = servings,
                    amount = null
                )

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
        fun provideFactory(repository: RegisteredAlimentationRepository): ViewModelProvider.Factory {
            return object : ViewModelProvider.Factory {
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    if (modelClass.isAssignableFrom(RegisteredAlimentationViewModel::class.java)) {
                        @Suppress("UNCHECKED_CAST")
                        return RegisteredAlimentationViewModel(repository) as T
                    }
                    throw IllegalArgumentException("Unknown ViewModel class")
                }
            }
        }
    }
}