package study.snacktrackmobile.presentation.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import kotlinx.coroutines.launch
import study.snacktrackmobile.data.model.Meal
import study.snacktrackmobile.data.model.dto.RegisteredAlimentationResponse
import study.snacktrackmobile.data.storage.TokenStorage
import study.snacktrackmobile.presentation.ui.views.montserratFont
import study.snacktrackmobile.viewmodel.RegisteredAlimentationViewModel

@Composable
fun MealCard(
    meal: Meal,
    viewModel: RegisteredAlimentationViewModel,
    onEditProduct: (RegisteredAlimentationResponse) -> Unit,
    onAddProductClick: (String) -> Unit,
    selectedDate: String
) {
    var expanded by remember { mutableStateOf(true) }
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var showCopyDialog by remember { mutableStateOf(false) }
    val mealOptions = listOf("Breakfast", "Lunch", "Dinner", "Supper", "Snack")
    val summary = meal.calculateTotalMacros()

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color(0xffF3F0F0))
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    meal.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontFamily = montserratFont,
                    color = Color.Black
                )
                Row {
                    IconButton(onClick = { showCopyDialog = true }) {
                        Icon(Icons.Default.ContentCopy, contentDescription = "Copy meal", tint = Color.Black)
                    }
                    IconButton(onClick = { onAddProductClick(meal.name) }) {
                        Icon(Icons.Default.Add, contentDescription = "Add product", tint = Color.Black)
                    }
                    IconButton(onClick = { expanded = !expanded }) {
                        Icon(
                            imageVector = if (expanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                            contentDescription = if (expanded) "Collapse" else "Expand",
                            tint = Color.Black
                        )
                    }
                }
            }

            if (showCopyDialog) {
                CopyMealDialog(
                    title = "Copy meal",
                    mealOptions = mealOptions,
                    onDismiss = { showCopyDialog = false },
                    onConfirm = { fromDate, fromMealName ->
                        coroutineScope.launch {
                            viewModel.copyMeal(
                                context = context,
                                fromDate = fromDate,
                                fromMealName = fromMealName,
                                toDate = selectedDate,
                                toMealName = meal.name
                            )
                        }
                        showCopyDialog = false
                    }
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    "${String.format("%.0f", summary.kcal)} kcal",
                    style = MaterialTheme.typography.bodyMedium,
                    fontFamily = montserratFont,
                    textAlign = TextAlign.Start,
                    color = Color.Black
                )
                Text(
                    "${String.format("%.1f", summary.protein)}P ${String.format("%.1f", summary.fat)}F ${String.format("%.1f", summary.carbs)}C",
                    style = MaterialTheme.typography.bodySmall,
                    fontFamily = montserratFont,
                    textAlign = TextAlign.End,
                    color = Color.Black
                )
            }

            if (expanded) {
                if (meal.alimentations.isEmpty()) {
                    Text(
                        "No products added yet",
                        style = MaterialTheme.typography.bodySmall,
                        fontFamily = montserratFont,
                        color = Color.Gray
                    )
                } else {
                    Column {
                        meal.alimentations.forEach { alimentation ->
                            ProductRow(
                                alimentation = alimentation,
                                onDelete = { id ->
                                    coroutineScope.launch {
                                        val token = TokenStorage.getToken(context) ?: return@launch
                                        viewModel.deleteEntry(token, id)
                                    }
                                },
                                onEdit = { selectedProduct -> onEditProduct(selectedProduct) }
                            )
                        }
                    }
                }
            }
        }
    }
}
fun calcNutrient(
    entry: RegisteredAlimentationResponse,
    baseValue: Float? // Ten parametr jest używany tylko dla essentialFood, dla meal jest ignorowany
): Double {
    // PRZYPADEK B: To jest PRODUKT (EssentialFood / MealApi)
    val per100 = (baseValue ?: 0f).toDouble()
    val defaultWeight = (entry.essentialFood?.defaultWeight ?: 100f).toDouble()
    val pieces = (entry.pieces ?: 0).toDouble()
    val grams = (entry.amount ?: 0f).toDouble()
    val totalGrams = if (pieces > 0.0) pieces * defaultWeight else grams
    return per100 * (totalGrams / 100.0)
}

data class MacroSummary(
    val kcal: Double = 0.0,
    val protein: Double = 0.0,
    val fat: Double = 0.0,
    val carbs: Double = 0.0
)

fun Meal.calculateTotalMacros(): MacroSummary {
    var kcalSum = 0.0
    var proteinSum = 0.0
    var fatSum = 0.0
    var carbsSum = 0.0

    this.alimentations.forEach { alimentation ->
        // 1. OBSŁUGA PRZEPISU (Recipe)
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
        }
        // 2. OBSŁUGA POJEDYNCZEGO PRODUKTU (EssentialFood lub MealApi)
        else {
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

    return MacroSummary(kcalSum, proteinSum, fatSum, carbsSum)
}