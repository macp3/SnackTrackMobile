package study.snacktrackmobile.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import study.snacktrackmobile.data.model.Meal
import study.snacktrackmobile.data.model.Product
import study.snacktrackmobile.data.model.dto.RegisteredAlimentationResponse
import study.snacktrackmobile.data.repository.RegisteredAlimentationRepository
import study.snacktrackmobile.presentation.ui.state.SummaryBarState

class RegisteredAlimentationViewModel(
    private val repository: RegisteredAlimentationRepository
) : ViewModel() {

    private val _raw = MutableStateFlow<List<RegisteredAlimentationResponse>>(emptyList())
    val raw: StateFlow<List<RegisteredAlimentationResponse>> = _raw

    private val _meals = MutableStateFlow<List<Meal>>(emptyList())
    val meals: StateFlow<List<Meal>> = _meals

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
        return input.groupBy { it.meal?.name ?: "Other" }
            .map { (mealName, entries) ->
                val products = entries.map { entry ->
                    val ef = entry.essentialFood
                    val api = entry.mealApi

                    Product(
                        name = ef?.name ?: api?.name ?: "Unknown",
                        amount = entry.amount?.toString()
                            ?: entry.pieces?.toString() ?: "",
                        kcal = ef?.calories?.toInt() ?: api?.calorie ?: 0,
                        protein = ef?.protein ?: api?.protein ?: 0f,
                        fat = ef?.fat ?: api?.fat ?: 0f,
                        carbohydrates = ef?.carbohydrates ?: api?.carbohydrates ?: 0f
                    )
                }
                Meal(name = mealName, kcal = products.sumOf { it.kcal }, products = products)
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

