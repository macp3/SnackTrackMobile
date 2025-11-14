package study.snacktrackmobile.presentation.ui.state

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import study.snacktrackmobile.data.model.Meal

object SummaryBarState {

    var totalKcal by mutableStateOf(0f)
    var totalProtein by mutableStateOf(0f)
    var totalFat by mutableStateOf(0f)
    var totalCarbs by mutableStateOf(0f)

    fun update(meals: List<Meal>) {
        var kcalSum = 0.0
        var proteinSum = 0.0
        var fatSum = 0.0
        var carbsSum = 0.0

        meals.forEach { meal ->
            meal.alimentations.forEach { alimentation ->
                val food = alimentation.essentialFood
                val amount = (alimentation.amount ?: 0f).toDouble()

                // ✅ jawne rzutowanie na Double
                kcalSum += ((food?.calories ?: 0f).toDouble() * amount / 100.0)
                proteinSum += ((food?.protein ?: 0f).toDouble() * amount / 100.0)
                fatSum += ((food?.fat ?: 0f).toDouble() * amount / 100.0)
                carbsSum += ((food?.carbohydrates ?: 0f).toDouble() * amount / 100.0)
            }
        }

        totalKcal = kcalSum.toFloat()
        totalProtein = proteinSum.toFloat()
        totalFat = fatSum.toFloat()
        totalCarbs = carbsSum.toFloat()
    }
}

