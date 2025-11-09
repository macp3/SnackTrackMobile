package study.snacktrackmobile.presentation.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
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
import kotlinx.coroutines.launch
import study.snacktrackmobile.data.model.Meal
import study.snacktrackmobile.data.storage.TokenStorage
import study.snacktrackmobile.presentation.ui.views.montserratFont
import study.snacktrackmobile.viewmodel.RegisteredAlimentationViewModel


@Composable
fun MealCard(meal: Meal, viewModel: RegisteredAlimentationViewModel)
{
    var expanded by remember { mutableStateOf(true) }
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

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
                )
                IconButton(onClick = { expanded = !expanded }) {
                    Icon(
                        imageVector = if (expanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                        contentDescription = null
                    )
                }
            }

            // Kalorie
            Text(
                "${meal.kcal} kcal",
                style = MaterialTheme.typography.bodyMedium,
                fontFamily = montserratFont,
            )

            // ➡️ Podsumowanie makroskładników (wyrównane do prawej)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                Text(
                    "${meal.products.sumOf { it.protein.toInt() }}P  " +
                            "${meal.products.sumOf { it.fat.toInt() }}F  " +
                            "${meal.products.sumOf { it.carbohydrates.toInt() }}C",
                    style = MaterialTheme.typography.bodySmall,
                    fontFamily = montserratFont,
                    textAlign = TextAlign.End
                )
            }

            if (expanded) {
                if (meal.products.isEmpty()) {
                    Text(
                        "No products added yet",
                        style = MaterialTheme.typography.bodySmall,
                        fontFamily = montserratFont,
                    )
                } else {
                    Column {
                        meal.products.forEach { product ->
                            ProductRow(product) { id ->
                                coroutineScope.launch {
                                    val token = TokenStorage.getToken(context) ?: return@launch
                                    viewModel.deleteEntry(token, id)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}