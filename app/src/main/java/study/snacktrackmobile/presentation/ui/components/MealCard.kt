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
import androidx.compose.ui.unit.dp
import study.snacktrackmobile.data.model.Meal
import study.snacktrackmobile.presentation.ui.views.montserratFont

@Composable
fun MealCard(meal: Meal) {
    var expanded by remember { mutableStateOf(true) }

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
                Text(meal.name, style = MaterialTheme.typography.titleMedium, fontFamily = montserratFont,)
                IconButton(onClick = { expanded = !expanded }) {
                    Icon(imageVector = if (expanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown, contentDescription = null)
                }
            }

            Text("${meal.kcal} kcal", style = MaterialTheme.typography.bodyMedium, fontFamily = montserratFont,)

            if (expanded) {
                if (meal.products.isEmpty()) {
                    Text("Brak dodanych produktów", style = MaterialTheme.typography.bodySmall, fontFamily = montserratFont,)
                } else {
                    Column {
                        meal.products.forEach { product ->
                            ProductRow(product)
                        }
                    }
                }
            }
        }
    }
}
