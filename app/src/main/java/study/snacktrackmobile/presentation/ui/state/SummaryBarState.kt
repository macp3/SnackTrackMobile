package study.snacktrackmobile.presentation.ui.state

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import study.snacktrackmobile.data.model.Meal

object SummaryBarState {

    var totalKcal by mutableStateOf(0)
    var totalProtein by mutableStateOf(0f)
    var totalFat by mutableStateOf(0f)
    var totalCarbs by mutableStateOf(0f)

    fun update(meals: List<Meal>) {
        totalKcal = meals.sumOf { meal -> meal.products.sumOf { it.kcal } }

        totalProtein = meals.sumOf { meal ->
            meal.products.sumOf { it.protein.toDouble() }
        }.toFloat()

        totalFat = meals.sumOf { meal ->
            meal.products.sumOf { it.fat.toDouble() }
        }.toFloat()

        totalCarbs = meals.sumOf { meal ->
            meal.products.sumOf { it.carbohydrates.toDouble() }
        }.toFloat()
    }
}