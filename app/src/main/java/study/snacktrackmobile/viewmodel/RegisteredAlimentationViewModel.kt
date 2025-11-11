package study.snacktrackmobile.viewmodel

import android.content.Context
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import study.snacktrackmobile.data.model.Meal
import study.snacktrackmobile.data.model.Product
import study.snacktrackmobile.data.model.dto.RegisteredAlimentationRequest
import study.snacktrackmobile.data.model.dto.RegisteredAlimentationResponse
import study.snacktrackmobile.data.repository.RegisteredAlimentationRepository
import study.snacktrackmobile.presentation.ui.state.SummaryBarState
import study.snacktrackmobile.data.storage.TokenStorage
import androidx.compose.runtime.State

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

    //tymczasowo
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
        return input.groupBy { normalizeMealName(it.mealName ?: "Other") }.map { (mealName, entries) ->

            val products = entries.map { entry ->
                val ef = entry.essentialFood
                val api = entry.mealApi

                // jawne rzutowanie na Float
                val baseWeight: Float = ef?.defaultWeight ?: 0f
                val baseCalories: Float = ef?.calories ?: api?.calorie?.toFloat() ?: 0f
                val baseProtein: Float = ef?.protein ?: api?.protein ?: 0f
                val baseFat: Float = ef?.fat ?: api?.fat ?: 0f
                val baseCarbs: Float = ef?.carbohydrates ?: api?.carbohydrates ?: 0f

                val piecesCount: Float = (entry.pieces ?: 0).toFloat()
                val amountInUnit: Float = entry.amount.toFloat() ?: 0f

                // 🔽 Oblicz wagę i wartości odżywcze
                val totalWeight: Float
                val kcal: Float
                val protein: Float
                val fat: Float
                val carbohydrates: Float

                if (piecesCount > 0f) {
                    // przypadek: sztuki
                    totalWeight = baseWeight * piecesCount
                    kcal = baseCalories * piecesCount
                    protein = baseProtein * piecesCount
                    fat = baseFat * piecesCount
                    carbohydrates = baseCarbs * piecesCount
                } else if (amountInUnit > 0f && baseWeight > 0f) {
                    // przypadek: servingSizeUnit (np. gramy)
                    totalWeight = amountInUnit
                    val ratio = amountInUnit / baseWeight
                    kcal = baseCalories * ratio
                    protein = baseProtein * ratio
                    fat = baseFat * ratio
                    carbohydrates = baseCarbs * ratio
                } else {
                    // fallback
                    totalWeight = baseWeight
                    kcal = baseCalories
                    protein = baseProtein
                    fat = baseFat
                    carbohydrates = baseCarbs
                }

                val amountText = if (piecesCount > 0f) {
                    "${piecesCount.toInt()}x piece"
                } else {
                    "$totalWeight ${ef?.servingSizeUnit ?: ""}"
                }


                Product(
                    id = entry.id,
                    name = ef?.name ?: api?.name ?: "Unknown",
                    amount = amountText,
                    kcal = kcal.toInt(),
                    protein = protein,
                    fat = fat,
                    carbohydrates = carbohydrates
                )
            }

            Meal(
                name = mealName,
                kcal = products.sumOf { it.kcal },
                products = products
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
            val token = TokenStorage.getToken(context) // ✅ teraz OK
            if (token != null) {
                try {
                    repository.updateEntry(token, productId, dto)
                    loadMeals(token, date)
                } catch (e: Exception) {
                    _errorMessage.value = e.message
                }
            } else {
                _errorMessage.value = "No auth token. Log in again"
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

    fun addMealProduct(
        context: Context,
        essentialId: Int,
        mealName: String,
        date: String,
        amount: Float = 100f,
        pieces: Int = 1
    ) {
        viewModelScope.launch {
            val token = TokenStorage.getToken(context) ?: return@launch
            repository.addEntry(token, essentialId, mealName, date, amount, pieces)
            loadMeals(token, date)
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



