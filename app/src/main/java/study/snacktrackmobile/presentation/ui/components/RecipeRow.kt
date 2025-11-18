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
    inMyRecipes: Boolean,
    isFavourite: Boolean,
    onClick: (Int) -> Unit,
    onDelete: (Int) -> Unit,
    onEdit: (RecipeResponse) -> Unit,
    onFavouriteToggle: (Int) -> Unit
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
                .fillMaxWidth()
                .clickable { onClick(recipe.id) }
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = recipe.name,
                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                    color = Color.Black
                )
                Text(
                    text = recipe.description ?: "No description provided.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Gray
                )
            }

            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {

                // ❤️ FAVOURITE ICON
                Text(
                    text = if (isFavourite) "❤" else "♡",
                    color = if (isFavourite) Color.Red else Color.Gray,
                    modifier = Modifier.clickable { onFavouriteToggle(recipe.id) }
                )

                // ✏ & ❌ only in My Recipes
                if (inMyRecipes) {

                    // Edit
                    Text(
                        text = "✏",
                        color = Color.DarkGray,
                        modifier = Modifier.clickable { onEdit(recipe) }
                    )

                    // Delete
                    Text(
                        text = "❌",
                        color = Color.Red,
                        modifier = Modifier.clickable { onDelete(recipe.id) }
                    )
                }
            }
        }
    }
}

