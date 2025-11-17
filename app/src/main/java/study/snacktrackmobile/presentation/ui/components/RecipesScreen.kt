package study.snacktrackmobile.presentation.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import study.snacktrackmobile.viewmodel.RecipeViewModel

@Composable
fun RecipesScreen(
    viewModel: RecipeViewModel,
    onRecipeClick: (Int) -> Unit
) {
    val recipes by viewModel.recipes.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {

        Text(
            text = "Your Recipes",
            style = MaterialTheme.typography.headlineSmall
        )

        Spacer(modifier = Modifier.height(16.dp))

        LazyColumn {
            items(recipes.size) { i ->
                val r = recipes[i]

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp)
                        .clickable { onRecipeClick(r.id) }
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(text = r.name, style = MaterialTheme.typography.titleMedium)
                        Spacer(Modifier.height(4.dp))
                        Text(text = r.description ?: "-", style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }
        }
    }
}
