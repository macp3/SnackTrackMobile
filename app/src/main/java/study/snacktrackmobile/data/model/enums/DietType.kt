package study.snacktrackmobile.data.model.enums

import kotlinx.serialization.Serializable

@Serializable
enum class DietType {
    balanced,
    keto,
    low_carb,
    high_protein,
    low_fat,
    vegan,
    vegetarian,
    gluten_free,
    lactose_free
}