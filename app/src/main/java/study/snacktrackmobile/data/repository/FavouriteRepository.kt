package study.snacktrackmobile.data.repository

import study.snacktrackmobile.data.api.RecipeApi

class FavouriteRepository(private val api: RecipeApi) {

    suspend fun getMyFavourites(token: String) =
        api.getMyFavourites("Bearer $token")

    suspend fun addFavourite(token: String, recipeId: Int) =
        api.addFavourite("Bearer $token", recipeId)

    suspend fun removeFavourite(token: String, recipeId: Int) =
        api.removeFavourite("Bearer $token", recipeId)
}
