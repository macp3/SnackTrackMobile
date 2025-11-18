package study.snacktrackmobile.presentation.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import study.snacktrackmobile.data.model.Product
import study.snacktrackmobile.data.model.dto.RegisteredAlimentationResponse
import study.snacktrackmobile.presentation.ui.views.montserratFont
import study.snacktrackmobile.viewmodel.RegisteredAlimentationViewModel

@Composable
fun ProductRow(
    alimentation: RegisteredAlimentationResponse,
    onDelete: (Int) -> Unit,
    onEdit: (RegisteredAlimentationResponse) -> Unit
) {
    val food = alimentation.essentialFood ?: return

    // 🔹 Wyświetlanie: Sztuki vs Gramy
    val amountText = when {
        alimentation.pieces != null && alimentation.pieces > 0 ->
            "${alimentation.pieces} piece${if (alimentation.pieces > 1) "s" else ""}"

        alimentation.amount != null && alimentation.amount > 0f ->
            "${String.format("%.1f", alimentation.amount)} g"

        else -> "-"
    }

    // 🔹 Obliczanie makro (helper function na dole pliku)
    val kcal = calcNutrient(alimentation, food.calories)
    val protein = calcNutrient(alimentation, food.protein)
    val fat = calcNutrient(alimentation, food.fat)
    val carbs = calcNutrient(alimentation, food.carbohydrates)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp) // Lekki cień dla estetyki
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onEdit(alimentation) } // Kliknięcie w cały wiersz edytuje
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Lewa strona: Nazwa i ilość
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = food.name ?: "-",
                    style = MaterialTheme.typography.bodyLarge,
                    fontFamily = montserratFont,
                )
                Text(
                    text = amountText,
                    style = MaterialTheme.typography.bodySmall,
                    fontFamily = montserratFont,
                    color = Color.Gray
                )
            }

            // Prawa strona: Makro i usuwanie
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "${String.format("%.0f", kcal)} kcal",
                        style = MaterialTheme.typography.bodySmall,
                        fontFamily = montserratFont,
                    )
                    Text(
                        text = "${String.format("%.1f", protein)}P ${String.format("%.1f", fat)}F ${String.format("%.1f", carbs)}C",
                        style = MaterialTheme.typography.bodySmall,
                        fontFamily = montserratFont,
                        color = Color.Gray
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                IconButton(
                    onClick = { onDelete(alimentation.id) },
                    modifier = Modifier.size(24.dp) // Mniejszy przycisk usuwania
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Delete product",
                        tint = Color.Red
                    )
                }
            }
        }
    }
}