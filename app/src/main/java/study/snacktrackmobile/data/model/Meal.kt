package study.snacktrackmobile.data.model

data class Meal(
    val name: String,
    val kcal: Int,
    val products: List<Product> = emptyList()
)
