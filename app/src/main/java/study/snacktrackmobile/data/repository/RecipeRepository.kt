package study.snacktrackmobile.data.repository

import study.snacktrackmobile.data.api.RecipeApi
import study.snacktrackmobile.data.model.dto.RecipeRequest
import study.snacktrackmobile.data.model.dto.RecipeResponse

class RecipeRepository(private val api: RecipeApi) {

    suspend fun getMyRecipes(token: String): List<RecipeResponse> {
        return api.getMyRecipes("Bearer $token")
    }

    suspend fun addRecipe(token: String, dto: RecipeRequest): Boolean {
        val res = api.addRecipe("Bearer $token", dto)
        return res.isSuccessful
    }

    suspend fun updateRecipe(token: String, id: Int, dto: RecipeRequest): RecipeResponse {
        return api.updateRecipe("Bearer $token", id, dto)
    }

    suspend fun deleteRecipe(token: String, id: Int) {
        val res = api.deleteRecipe("Bearer $token", id)
        if (!res.isSuccessful) {
            throw Exception("Delete failed: ${res.code()} ${res.message()}")
        }
    }
}
