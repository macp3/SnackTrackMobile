package study.snacktrackmobile.data.model

data class Meal(
    val name: String,
    val time: String,
    val kcal: Int,
    val protein: Float? = null,
    val fat: Float? = null,
    val carbs: Float? = null,
    val foods: List<Food>
)
