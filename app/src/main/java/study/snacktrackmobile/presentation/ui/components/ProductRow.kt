package study.snacktrackmobile.presentation.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
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

    // 🔹 Tekst ilości – sztuki lub gramatura
    val amountText = when {
        alimentation.pieces != null && alimentation.pieces > 0 ->
            "${alimentation.pieces} piece${if (alimentation.pieces > 1) "s" else ""}"

        alimentation.amount != null && alimentation.amount > 0f ->
            "${String.format("%.1f", alimentation.amount)} g"

        else -> "-"
    }

    // 🔹 Makro
    val kcal = calcNutrient(alimentation, food.calories)
    val protein = calcNutrient(alimentation, food.protein)
    val fat = calcNutrient(alimentation, food.fat)
    val carbs = calcNutrient(alimentation, food.carbohydrates)


    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clickable { onEdit(alimentation) }, // 🔹 Klikalny wiersz
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Row(
            modifier = Modifier
                .padding(12.dp)
                .clickable { onEdit(alimentation) } // 🔹 callback do MainView
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    food.name ?: "-",
                    style = MaterialTheme.typography.bodyLarge,
                    fontFamily = montserratFont
                )
                Text(
                    amountText,
                    style = MaterialTheme.typography.bodySmall,
                    fontFamily = montserratFont
                )
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        "${String.format("%.0f", kcal)} kcal",
                        style = MaterialTheme.typography.bodySmall,
                        fontFamily = montserratFont
                    )
                    Text(
                        "${String.format("%.1f", protein)}P ${
                            String.format(
                                "%.1f",
                                fat
                            )
                        }F ${String.format("%.1f", carbs)}C",
                        style = MaterialTheme.typography.bodySmall,
                        fontFamily = montserratFont
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                IconButton(onClick = { onDelete(alimentation.id) }) {
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