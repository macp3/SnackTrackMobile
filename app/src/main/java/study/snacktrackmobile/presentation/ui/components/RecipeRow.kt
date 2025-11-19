package study.snacktrackmobile.presentation.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import study.snacktrackmobile.data.model.dto.RecipeResponse
// Upewnij się, że importy czcionek są poprawne
// import study.snacktrackmobile.presentation.ui.views.montserratFont

@Composable
fun RecipeRow(
    recipe: RecipeResponse,
    isFavourite: Boolean,
    isAuthor: Boolean,
    onClick: () -> Unit,
    onToggleFavourite: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }, // Cały wiersz klikalny
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // LEWA STRONA: Tekst (Nazwa i Opis)
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = recipe.name,
                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                    color = Color.Black
                )
                Text(
                    text = if (recipe.description.isNullOrBlank()) "No description" else recipe.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Gray,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }

            // PRAWA STRONA: Ikony (Serce + X)
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Przycisk Serduszka
                IconButton(
                    onClick = onToggleFavourite,
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        imageVector = if (isFavourite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                        contentDescription = "Favourite",
                        tint = if (isFavourite) Color.Red else Color.Gray
                    )
                }

                // Przycisk X - widoczny TYLKO jeśli user jest autorem
                if (isAuthor) {
                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Delete",
                            tint = Color.DarkGray
                        )
                    }
                }
            }
        }
    }
}