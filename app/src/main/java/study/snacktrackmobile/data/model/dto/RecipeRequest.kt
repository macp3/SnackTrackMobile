package study.snacktrackmobile.data.model.dto

data class RecipeRequest(
    val name: String,
    val description: String,
    val ingredients: List<IngredientRequest>
)