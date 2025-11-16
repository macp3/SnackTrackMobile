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
import study.snacktrackmobile.data.storage.TokenStorage
import study.snacktrackmobile.presentation.ui.views.montserratFont
import study.snacktrackmobile.viewmodel.RegisteredAlimentationViewModel


@Composable
fun MealCard(
    meal: Meal,
    viewModel: RegisteredAlimentationViewModel,
    navController: NavController,
    selectedDate: String
) {
    var expanded by remember { mutableStateOf(true) }
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color(0xffF3F0F0))
    ) {
        Column(modifier = Modifier.padding(top = 2.dp, bottom = 12.dp, start = 12.dp, end = 12.dp)) {
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
                    // przycisk dodaj produkt
                    IconButton(onClick = {
                        navController.navigate("MainView?tab=AddProduct&meal=${meal.name}&date=$selectedDate") {
                            popUpTo("MainView") { inclusive = true }
                        }
                    }) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Add product",
                            tint = Color.Black
                        )
                    }

                    // przycisk rozwiń/zwiń
                    IconButton(onClick = { expanded = !expanded }) {
                        Icon(
                            imageVector = if (expanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                            contentDescription = if (expanded) "Collapse" else "Expand",
                            tint = Color.Black
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    "${meal.kcal} kcal",
                    style = MaterialTheme.typography.bodyMedium,
                    fontFamily = montserratFont,
                    textAlign = TextAlign.Start,
                    color = Color.Black
                )

                Text(
                    "${String.format("%.1f", meal.products.sumOf { it.protein.toDouble() })}P  " +
                            "${String.format("%.1f", meal.products.sumOf { it.fat.toDouble() })}F  " +
                            "${String.format("%.1f", meal.products.sumOf { it.carbohydrates.toDouble() })}C",
                    style = MaterialTheme.typography.bodySmall,
                    fontFamily = montserratFont,
                    textAlign = TextAlign.End,
                    color = Color.Black
                )
            }

            if (expanded) {
                if (meal.products.isEmpty()) {
                    Text(
                        "No products added yet",
                        style = MaterialTheme.typography.bodySmall,
                        fontFamily = montserratFont,
                        color = Color.Gray
                    )
                } else {
                    Column {
                        meal.products.forEach { product ->
                            ProductRow(
                                product = product,
                                onDelete = { id ->
                                    coroutineScope.launch {
                                        val token = TokenStorage.getToken(context) ?: return@launch
                                        viewModel.deleteEntry(token, id)
                                    }
                                },
                                onEdit = { selectedProduct ->
                                    navController.currentBackStackEntry?.savedStateHandle?.set("product", selectedProduct)
                                    navController.navigate("productEdit?date=$selectedDate")
                                },
                            )
                        }
                    }
                }
            }
        }
    }
}