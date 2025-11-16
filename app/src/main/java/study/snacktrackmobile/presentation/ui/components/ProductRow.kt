package study.snacktrackmobile.presentation.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
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

    // 🔽 Tekst ilości – albo gram, albo sztuki
    val amountText = when {
        alimentation.pieces != null && alimentation.pieces > 0 ->
            "${alimentation.pieces.toInt()} piece"
        alimentation.amount != null && alimentation.amount > 0f ->
            "${String.format("%.1f", alimentation.amount)} g"
        else -> "-"
    }

    // 🔽 Formatowanie makro
    val kcal = String.format("%.0f", food.calories ?: 0f)
    val protein = String.format("%.1f", food.protein ?: 0f)
    val fat = String.format("%.1f", food.fat ?: 0f)
    val carbs = String.format("%.1f", food.carbohydrates ?: 0f)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onEdit(alimentation) }
            .padding(top = 6.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Row(
            modifier = Modifier
                .padding(12.dp)
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
                        "$kcal kcal",
                        style = MaterialTheme.typography.bodySmall,
                        fontFamily = montserratFont
                    )
                    Text(
                        "${protein}P ${fat}F ${carbs}C",
                        style = MaterialTheme.typography.bodySmall,
                        fontFamily = montserratFont
                    )
                }
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