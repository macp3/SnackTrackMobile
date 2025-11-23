package study.snacktrackmobile.data.converters

import study.snacktrackmobile.data.model.Ingredient
import study.snacktrackmobile.data.model.Recipe
import study.snacktrackmobile.data.model.dto.IngredientResponse
import study.snacktrackmobile.data.model.dto.RecipeResponse

fun RecipeResponse.toDomain(): Recipe {
    return Recipe(
        id = id,
        authorId = authorId,
        name = name,
        description = description,
        imageUrl = imageUrl,
        ingredients = ingredients.map { it.toDomain() }
    )
}

fun IngredientResponse.toDomain(): Ingredient {
    return Ingredient(
        id = id,
        essentialFood = essentialFood?.toDomain()?:null,
        essentialApiId = essentialApi?.id?: null,
        amount = amount,
        pieces = pieces
    )
}
