package study.snacktrackmobile.presentation.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import study.snacktrackmobile.data.model.dto.EssentialFoodResponse
import study.snacktrackmobile.data.model.dto.RecipeResponse
import study.snacktrackmobile.data.model.dto.RegisteredAlimentationResponse

@Composable
fun RecipeListDisplay(
    items: List<RecipeResponse>,
    favouriteIds: Set<Int>,       // Potrzebne do określenia koloru serca
    currentUserId: Int?,          // Potrzebne do weryfikacji autora (ikona X)
    onClick: (RecipeResponse) -> Unit, // Przekazujemy cały obiekt do ekranu szczegółów
    onToggleFavourite: (RecipeResponse) -> Unit,
    onDelete: (Int) -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(items) { recipe ->

            val isFavourite = favouriteIds.contains(recipe.id)
            val isAuthor = currentUserId != null && recipe.authorId == currentUserId

            RecipeRow(
                recipe = recipe,
                isFavourite = isFavourite,
                isAuthor = isAuthor,
                onClick = { onClick(recipe) },
                onToggleFavourite = { onToggleFavourite(recipe) },
                onDelete = { onDelete(recipe.id) }
            )
        }
    }
}
