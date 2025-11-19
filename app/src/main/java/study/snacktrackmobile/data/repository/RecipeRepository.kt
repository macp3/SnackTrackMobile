package study.snacktrackmobile.data.repository

import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import study.snacktrackmobile.data.api.RecipeApi
import study.snacktrackmobile.data.model.dto.RecipeRequest
import study.snacktrackmobile.data.model.dto.RecipeResponse
import java.io.File

class RecipeRepository(private val api: RecipeApi) {

    suspend fun getAllRecipes(token: String): List<RecipeResponse> =
        api.getAllRecipes("Bearer $token")

    suspend fun getMyRecipes(token: String): List<RecipeResponse> =
        api.getMyRecipes("Bearer $token")

    // --- SEKCJA ULUBIONYCH ---

    // Pobiera listę ulubionych (potrzebne też do sprawdzenia ID, żeby zapalić serduszka na innych listach)
    suspend fun getMyFavourites(token: String): List<RecipeResponse> =
        api.getMyFavourites("Bearer $token")

    // Dodaje do ulubionych. Zwraca true jeśli sukces.
    suspend fun addFavourite(token: String, recipeId: Int): Boolean {
        return try {
            val res = api.addFavourite("Bearer $token", recipeId)
            res.isSuccessful
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    // Usuwa z ulubionych. Zwraca true jeśli sukces.
    suspend fun removeFavourite(token: String, recipeId: Int): Boolean {
        return try {
            val res = api.removeFavourite("Bearer $token", recipeId)
            res.isSuccessful
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
    // -------------------------

    suspend fun addRecipe(token: String, request: RecipeRequest): Result<Int> {
        return try{
            val res = api.addRecipe("Bearer $token", request)
            if (res.isSuccessful && res.body() != null) {
                // Sukces: zwracamy ID nowego przepisu
                Result.success(res.body()!!)
            } else {
                val errorBody = res.errorBody()?.string()
                val msg = if (errorBody.isNullOrBlank()) "Error ${res.code()}" else errorBody
                Result.failure(Exception(msg))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateRecipe(token: String, id: Int, request: RecipeRequest): Boolean {
        return try {
            val res = api.updateRecipe("Bearer $token", id, request)
            res.isSuccessful
        } catch (e: Exception) {
            false
        }
    }

    suspend fun deleteRecipe(token: String, id: Int): Boolean {
        return try {
            val res = api.deleteRecipe("Bearer $token", id)
            res.isSuccessful
        } catch (e: Exception) {
            false
        }
    }

    suspend fun uploadImage(token: String, recipeId: Int, file: File): Boolean {
        return try {
            // Tworzenie RequestBody z pliku
            val requestFile = file.asRequestBody("image/jpeg".toMediaTypeOrNull())
            // "image" to nazwa parametru w @RequestParam("image") na backendzie
            val body = MultipartBody.Part.createFormData("image", file.name, requestFile)

            val res = api.uploadImage("Bearer $token", recipeId, body)
            res.isSuccessful
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
}