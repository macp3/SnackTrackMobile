package study.snacktrackmobile.data.repository

import study.snacktrackmobile.data.api.ApiService
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
}

