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

    var limitKcal by mutableStateOf(1f)
    var limitProtein by mutableStateOf(1f)
    var limitFat by mutableStateOf(1f)
    var limitCarbs by mutableStateOf(1f)

    fun update(meals: List<Meal>) {
        var kcalSum = 0.0
        var proteinSum = 0.0
        var fatSum = 0.0
        var carbsSum = 0.0

        meals.forEach { meal ->
            meal.alimentations.forEach { alimentation ->
                if (alimentation.meal != null) {
                    val recipe = alimentation.meal
                    val servings = alimentation.pieces ?: 1f

                    recipe.ingredients.forEach { ing ->
                        val ef = ing.essentialFood
                        val api = ing.essentialApi

                        val baseKcal = (ef?.calories ?: api?.calorie?.toFloat() ?: 0f).toDouble()
                        val baseP = (ef?.protein ?: api?.protein ?: 0f).toDouble()
                        val baseF = (ef?.fat ?: api?.fat ?: 0f).toDouble()
                        val baseC = (ef?.carbohydrates ?: api?.carbohydrates ?: 0f).toDouble()

                        val baseWeight = (ef?.defaultWeight ?: 100f).toDouble()
                        val iAmount = ing.amount ?: 0f
                        val iPieces = ing.pieces ?: 0f

                        val ratio = when {
                            iPieces > 0 -> (iPieces * baseWeight) / 100.0
                            iAmount > 0 -> iAmount.toDouble() / 100.0
                            else -> 0.0
                        }

                        kcalSum += baseKcal * ratio * servings
                        proteinSum += baseP * ratio * servings
                        fatSum += baseF * ratio * servings
                        carbsSum += baseC * ratio * servings
                    }
                } else {
                    val ef = alimentation.essentialFood
                    val api = alimentation.mealApi

                    val baseKcal = (ef?.calories ?: api?.calorie?.toFloat() ?: 0f).toDouble()
                    val baseP = (ef?.protein ?: api?.protein ?: 0f).toDouble()
                    val baseF = (ef?.fat ?: api?.fat ?: 0f).toDouble()
                    val baseC = (ef?.carbohydrates ?: api?.carbohydrates ?: 0f).toDouble()

                    val baseWeight = (ef?.defaultWeight ?: 100f).toDouble()
                    val userAmount = alimentation.amount ?: 0f
                    val userPieces = alimentation.pieces ?: 0f

                    val ratio = when {
                        userPieces > 0 -> (userPieces * baseWeight) / 100.0
                        userAmount > 0 -> userAmount.toDouble() / 100.0
                        else -> 0.0
                    }

                    kcalSum += baseKcal * ratio
                    proteinSum += baseP * ratio
                    fatSum += baseF * ratio
                    carbsSum += baseC * ratio
                }
            }
        }

        totalKcal = kcalSum.toFloat()
        totalProtein = proteinSum.toFloat()
        totalFat = fatSum.toFloat()
        totalCarbs = carbsSum.toFloat()
    }

    fun setLimits(kcal: Float?, protein: Float?, fat: Float?, carbs: Float?) {
        limitKcal = if (kcal != null && kcal > 0) kcal else 1f
        limitProtein = if (protein != null && protein > 0) protein else 1f
        limitFat = if (fat != null && fat > 0) fat else 1f
        limitCarbs = if (carbs != null && carbs > 0) carbs else 1f
    }
}
