package study.snacktrackmobile.data.repository

import study.snacktrackmobile.data.api.RecipeApi
import study.snacktrackmobile.data.converters.toDomain
import study.snacktrackmobile.data.model.Ingredient
import study.snacktrackmobile.data.model.Recipe
import study.snacktrackmobile.data.model.dto.IngredientResponse
import study.snacktrackmobile.data.model.dto.RecipeRequest
import study.snacktrackmobile.data.model.dto.RecipeResponse

class RecipeRepository(private val api: RecipeApi) {

    suspend fun getAllRecipes(token: String): List<RecipeResponse> = api.getAllRecipes("Bearer $token")

    suspend fun getMyRecipes(token: String): List<RecipeResponse> = api.getMyRecipes("Bearer $token")

    suspend fun getMyFavourites(token: String): List<RecipeResponse> = api.getMyFavourites("Bearer $token")

    suspend fun addRecipe(token: String, request: RecipeRequest): Result<Unit> {
        return try {
            val res = api.addRecipe("Bearer $token", request)
            if (res.isSuccessful) {
                Result.success(Unit)
            } else {
                // 🔹 KLUCZOWA ZMIANA: Lepsza obsługa pustego errorBody
                val errorBody = res.errorBody()?.string()
                val errorMessage = if (errorBody.isNullOrBlank()) {
                    "Server returned error code ${res.code()}" // Dodajemy kod HTTP
                } else {
                    errorBody
                }
                Result.failure(Exception(errorMessage))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateRecipe(token: String, id: Int, request: RecipeRequest): Boolean {
        val res = api.updateRecipe("Bearer $token", id, request)
        return res.isSuccessful
    }

    suspend fun deleteRecipe(token: String, id: Int): Boolean {
        val res = api.deleteRecipe("Bearer $token", id)
        return res.isSuccessful
    }
}

