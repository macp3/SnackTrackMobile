package study.snacktrackmobile.presentation.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import kotlinx.coroutines.launch
import study.snacktrackmobile.data.model.Meal
import study.snacktrackmobile.data.model.Product
import study.snacktrackmobile.data.model.dto.RegisteredAlimentationResponse
import study.snacktrackmobile.data.storage.TokenStorage
import study.snacktrackmobile.presentation.ui.views.montserratFont
import study.snacktrackmobile.viewmodel.RegisteredAlimentationViewModel


@Composable
fun MealCard(
    meal: Meal,
    viewModel: RegisteredAlimentationViewModel,
    onEditProduct: (RegisteredAlimentationResponse) -> Unit,
    navController: NavController,
    selectedDate: String
) {
    var expanded by remember { mutableStateOf(true) }
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    // Obliczenia makro dla całego posiłku
    val protein = meal.alimentations.sumOf {
        calcNutrient(it, (it.essentialFood?.protein ?: it.mealApi?.protein)?.toFloat())
    }
    val fat = meal.alimentations.sumOf {
        calcNutrient(it, (it.essentialFood?.fat ?: it.mealApi?.fat)?.toFloat())
    }
    val carbs = meal.alimentations.sumOf {
        calcNutrient(it, (it.essentialFood?.carbohydrates ?: it.mealApi?.carbohydrates)?.toFloat())
    }
    val kcal = meal.alimentations.sumOf {
        calcNutrient(it, (it.essentialFood?.calories ?: it.mealApi?.calorie)?.toFloat())
    }


    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color(0xffF3F0F0))
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(meal.name, style = MaterialTheme.typography.titleMedium, fontFamily = montserratFont)

                Row {
                    IconButton(onClick = {
                        navController.navigate("MainView?tab=AddProduct&meal=${meal.name}&date=$selectedDate") {
                            popUpTo("MainView") { inclusive = true }
                        }
                    }) {
                        Icon(imageVector = Icons.Default.Add, contentDescription = "Add product")
                    }

                    IconButton(onClick = { expanded = !expanded }) {
                        Icon(
                            imageVector = if (expanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                            contentDescription = null
                        )
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    "${String.format("%.0f", kcal)} kcal",
                    style = MaterialTheme.typography.bodyMedium,
                    fontFamily = montserratFont
                )
                Text(
                    "${String.format("%.1f", protein)}P ${String.format("%.1f", fat)}F ${String.format("%.1f", carbs)}C",
                    style = MaterialTheme.typography.bodySmall,
                    fontFamily = montserratFont
                )
            }

            if (expanded) {
                if (meal.alimentations.isEmpty()) {
                    Text("No products added yet", style = MaterialTheme.typography.bodySmall, fontFamily = montserratFont)
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
                                onEdit = { alimentation ->
                                    navController.currentBackStackEntry
                                        ?.savedStateHandle
                                        ?.set("editAlimentationId", alimentation.id)
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}


// 🔧 poprawione liczenie – wartości są dla defaultWeight, nie dla 100g
private fun calcNutrient(
    entry: RegisteredAlimentationResponse,
    baseValue: Float?
): Double {
    val amount = entry.amount ?: 0f
    val pieces = entry.pieces ?: 0f
    val weight = entry.essentialFood?.defaultWeight ?: 100f
    val valuePerDefaultWeight = baseValue ?: 0f

    return when {
        // jeśli podano sztuki → wartości * liczba sztuk
        pieces > 0f -> valuePerDefaultWeight * pieces

        // jeśli podano gramaturę → wartości * (amount / defaultWeight)
        amount > 0f -> valuePerDefaultWeight * (amount / weight)

        else -> 0.0
    }
}

