package study.snacktrackmobile.data.model

data class Recipe(
    val id: Int,
    val authorId: Int,
    val name: String,
    val description: String?,
    val ingredients: List<Ingredient>,
    val imageUrl: String?
)
