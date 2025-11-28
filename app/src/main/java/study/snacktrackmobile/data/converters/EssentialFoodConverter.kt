package study.snacktrackmobile.data.converters

import study.snacktrackmobile.data.model.EssentialFood
import study.snacktrackmobile.data.model.dto.EssentialFoodResponse

fun EssentialFoodResponse.toDomain(): EssentialFood {
    return EssentialFood(
        id = id,
        name = name ?: "",
        description = description ?: "",
        calories = calories?.toFloat() ?: 0f,
        protein = protein?.toFloat() ?: 0f,
        fat = fat?.toFloat() ?: 0f,
        carbohydrates = carbohydrates?.toFloat() ?: 0f,

        servingSizeUnit = servingSizeUnit ?: "",
        defaultWeight = defaultWeight ?: 0f,
        brandName = brandName
    )
}