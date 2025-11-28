package study.snacktrackmobile.data.repository

import study.snacktrackmobile.data.api.ApiService
import study.snacktrackmobile.data.model.dto.RegisteredAlimentationRequest
import study.snacktrackmobile.data.model.dto.RegisteredAlimentationResponse

class RegisteredAlimentationRepository(private val api: ApiService) {

    suspend fun getMealsForDate(token: String, date: String): List<RegisteredAlimentationResponse> {
        return api.getMyEntries("Bearer $token", date)
    }

    suspend fun deleteEntry(token: String, id: Int) {
        val response = api.deleteEntry("Bearer $token", id)
        if (!response.isSuccessful) {
            throw Exception("Delete failed: ${response.code()} ${response.message()}")
        }
    }

    suspend fun addEntry(
        token: String,
        essentialId: Int? = null,
        mealApiId: Int? = null,
        mealId: Int? = null,
        mealName: String,
        date: String,
        amount: Float?,
        pieces: Float?
    ): Boolean {
        val body = RegisteredAlimentationRequest(
            essentialId = essentialId,
            mealApiId = mealApiId,
            mealId = mealId,
            timestamp = date,
            mealName = mealName.lowercase(),
            amount = amount,
            pieces = pieces
        )

        val res = api.addEntry("Bearer $token", body, date)
        return res.isSuccessful
    }

    suspend fun updateEntry(token: String, id: Int, dto: RegisteredAlimentationRequest): RegisteredAlimentationResponse {
        return api.updateEntry("Bearer $token", id, dto)
    }

    suspend fun copyMeal(
        token: String,
        fromDate: String,
        fromMealName: String,
        toDate: String,
        toMealName: String
    ) {
        val authToken = if (token.startsWith("Bearer ")) token else "Bearer $token"

        val response = api.copyMeal(
            token = authToken,
            fromDate = fromDate,
            fromMealName = fromMealName,
            toDate = toDate,
            toMealName = toMealName
        )

        if (!response.isSuccessful) {
            throw Exception("Copy failed: ${response.code()} ${response.message()}")
        }
    }
}