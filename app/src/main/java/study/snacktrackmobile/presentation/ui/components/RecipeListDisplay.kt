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
    favourites: Set<Int>,
    inMyRecipes: Boolean,
    onClick: (Int) -> Unit,
    onDelete: (Int) -> Unit,
    onEdit: (RecipeResponse) -> Unit,
    onFavouriteToggle: (Int) -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(items) { recipe ->
            RecipeRow(
                recipe = recipe,
                inMyRecipes = inMyRecipes,
                isFavourite = favourites.contains(recipe.id),
                onClick = onClick,
                onDelete = onDelete,
                onEdit = onEdit,
                onFavouriteToggle = onFavouriteToggle
            )
        }
    }
}


