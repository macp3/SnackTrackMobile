package study.snacktrackmobile.presentation.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import study.snacktrackmobile.data.model.dto.RecipeResponse
import study.snacktrackmobile.data.network.ApiConfig
import study.snacktrackmobile.presentation.ui.views.montserratFont

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
            .height(100.dp)
            .clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxSize(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .width(100.dp)
                    .fillMaxHeight()
                    .background(Color.LightGray),
                contentAlignment = Alignment.Center
            ) {
                if (!recipe.imageUrl.isNullOrBlank()) {
                    val fullUrl = if (recipe.imageUrl.startsWith("http")) {
                        recipe.imageUrl
                    } else {
                        val baseUrl = ApiConfig.BASE_URL.removeSuffix("/")
                        val relativeUrl = recipe.imageUrl.removePrefix("/")
                        "$baseUrl/$relativeUrl"
                    }

                    AsyncImage(
                        model = fullUrl,
                        contentDescription = "Recipe Image",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.Restaurant,
                        contentDescription = null,
                        tint = Color.White
                    )
                }
            }

            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(12.dp)
                    .fillMaxHeight(),
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = recipe.name,
                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                    fontFamily = montserratFont,
                    color = Color.Black,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = if (recipe.description.isNullOrBlank()) "No description" else recipe.description,
                    style = MaterialTheme.typography.bodyMedium,
                    fontFamily = montserratFont,
                    color = Color.Gray,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    fontSize = androidx.compose.ui.unit.TextUnit.Unspecified
                )
            }

            Column(
                modifier = Modifier
                    .fillMaxHeight()
                    .padding(end = 8.dp),
                verticalArrangement = Arrangement.SpaceEvenly,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                IconButton(
                    onClick = onToggleFavourite,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = if (isFavourite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                        contentDescription = "Favourite",
                        tint = if (isFavourite) Color(0xFFFF5252) else Color.Gray
                    )
                }

                if (isAuthor) {
                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Delete",
                            tint = Color.LightGray
                        )
                    }
                }
            }
        }
    }
}