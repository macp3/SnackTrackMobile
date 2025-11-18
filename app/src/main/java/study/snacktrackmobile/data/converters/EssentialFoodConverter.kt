package study.snacktrackmobile.data.converters

import study.snacktrackmobile.data.model.EssentialFood
import study.snacktrackmobile.data.model.dto.EssentialFoodResponse

fun EssentialFoodResponse.toDomain(): EssentialFood {
    return EssentialFood(
        id = id,
        name = name ?: "",
        description = description ?: "",
        calories = calories ?: 0f,
        protein = protein ?: 0f,
        fat = fat ?: 0f,
        carbohydrates = carbohydrates ?: 0f,
        servingSizeUnit = servingSizeUnit ?: "",
        defaultWeight = defaultWeight ?: 0f,
        brandName = brandName
    )
}
