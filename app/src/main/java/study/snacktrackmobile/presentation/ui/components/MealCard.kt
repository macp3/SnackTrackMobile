package study.snacktrackmobile.presentation.ui.components

import DropdownField
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.PopupProperties
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import kotlinx.coroutines.launch
import study.snacktrackmobile.data.model.Meal
import study.snacktrackmobile.data.model.Product
import study.snacktrackmobile.data.model.dto.RegisteredAlimentationResponse
import study.snacktrackmobile.data.storage.TokenStorage
import study.snacktrackmobile.presentation.ui.views.montserratFont
import study.snacktrackmobile.viewmodel.RegisteredAlimentationViewModel
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.util.Locale
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DatePicker
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.rememberDatePickerState


@OptIn(ExperimentalMaterial3Api::class)
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

    var showCopyDialog by remember { mutableStateOf(false) }
    var fromMealName by remember { mutableStateOf("breakfast") }
    val mealOptions = listOf("breakfast", "lunch", "dinner", "supper", "snack")
    val datePickerState = rememberDatePickerState()

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
                        Icon(
                            imageVector = Icons.Default.ContentCopy,
                            contentDescription = "Copy meal",
                            tint = Color.Black
                        )
                    }

                    IconButton(onClick = {
                        navController.navigate("MainView?tab=AddProduct&meal=${meal.name}&date=$selectedDate") {
                            popUpTo("MainView") { inclusive = true }
                        }
                    }) {
                        Icon(imageVector = Icons.Default.Add, contentDescription = "Add product", tint = Color.Black)
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

            // 🔹 dialog kopiowania – identycznie jak w ShoppingListScreen
            if (showCopyDialog) {
                CopyMealDialog(
                    // ➡️ DODANE ARGUMENTY WYMAGANE PRZEZ NOWY DIALOG:
                    title = "Copy meal",
                    mealOptions = mealOptions,
                    // ----------------------------------------------------

                    onDismiss = { showCopyDialog = false },
                    onConfirm = { fromDate, fromMealName ->
                        coroutineScope.launch {
                            val token = TokenStorage.getToken(context) ?: return@launch
                            viewModel.copyMeal(
                                context = context,
                                fromDate = fromDate,
                                fromMealName = fromMealName.lowercase(),
                                toDate = selectedDate,
                                toMealName = meal.name.lowercase()
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
                    "${String.format("%.0f", kcal)} kcal",
                    style = MaterialTheme.typography.bodyMedium,
                    fontFamily = montserratFont,
                    textAlign = TextAlign.Start,
                    color = Color.Black
                )

                Text(
                    "${String.format("%.1f", protein)}P ${String.format("%.1f", fat)}F ${String.format("%.1f", carbs)}C",
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
                        fontFamily = montserratFont
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
                                onEdit = { selectedProduct ->
                                    onEditProduct(selectedProduct)
                                }
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
    baseValue: Float?
): Double {
    val per100 = (baseValue ?: 0f).toDouble()
    val defaultWeight = (entry.essentialFood?.defaultWeight ?: 100f).toDouble()
    val pieces = (entry.pieces ?: 0).toDouble()
    val grams = (entry.amount ?: 0f).toDouble()
    val totalGrams = if (pieces > 0.0) pieces * defaultWeight else grams
    return per100 * (totalGrams / 100.0)
}
