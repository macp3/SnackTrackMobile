package study.snacktrackmobile.presentation.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material3.Icon
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedButtonDefaults.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import study.snacktrackmobile.data.model.Product
import study.snacktrackmobile.presentation.ui.views.montserratFont
import study.snacktrackmobile.viewmodel.RegisteredAlimentationViewModel

@Composable
fun ProductRow(
    product: Product,
    onDelete: (Int) -> Unit,
    onEdit: (Product) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onEdit(product) }
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
                Text(product.name, style = MaterialTheme.typography.bodyLarge, fontFamily = montserratFont)
                Text(product.amount, style = MaterialTheme.typography.bodySmall, fontFamily = montserratFont)
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(horizontalAlignment = Alignment.End) {
                    Text("${product.kcal} kcal", style = MaterialTheme.typography.bodySmall, fontFamily = montserratFont)
                    Text("${product.protein}P ${product.fat}F ${product.carbohydrates}C", style = MaterialTheme.typography.bodySmall, fontFamily = montserratFont)
                }
                IconButton(onClick = { onDelete(product.id) }) {
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