package study.snacktrackmobile.presentation.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import study.snacktrackmobile.data.model.dto.RecipeResponse
import study.snacktrackmobile.presentation.ui.views.montserratFont // Zakładam, że masz tę czcionkę

@Composable
fun RecipeRow(
    recipe: RecipeResponse,
    onClick: (Int) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier
                // 🔹 Kliknięcie w cały wiersz aktywuje onClick, np. przejście do detali przepisu
                .fillMaxWidth()
                .clickable { onClick(recipe.id) }
                .padding(16.dp), // Zwiększamy padding dla czystszej estetyki
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Lewa strona: Nazwa i opis
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.Center
            ) {
                // 🔹 Nazwa Przepisu
                Text(
                    text = recipe.name,
                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                    fontFamily = montserratFont,
                    color = Color.Black
                )
                // 🔹 Opis Przepisu
                Text(
                    text = if (recipe.description.isNullOrBlank()) "No description provided." else recipe.description,
                    style = MaterialTheme.typography.bodyMedium,
                    fontFamily = montserratFont,
                    color = Color.Gray
                )
            }

            // Prawa strona (możesz dodać tu strzałkę lub ikonę, np. Icons.Default.KeyboardArrowRight)
            // Zostawiamy puste lub dodajemy małą ikonę dla estetyki klikalności.
            // Icon(
            //     imageVector = Icons.Default.KeyboardArrowRight,
            //     contentDescription = "Details",
            //     tint = Color.LightGray,
            //     modifier = Modifier.size(24.dp)
            // )
        }
    }
}