package study.snacktrackmobile.data.model

data class Product(
    val id: Int,
    val name: String,
    val amount: String,
    val kcal: Int,
    val protein: Float,
    val fat: Float,
    val carbohydrates: Float
)