package study.snacktrackmobile.data.model.dto

data class RecipeResponse(
    val id: Int,
    val name: String,
    val description: String,
    val authorId: Int,
    val ingredients: List<IngredientResponse>,
    val imageUrl: String?
)