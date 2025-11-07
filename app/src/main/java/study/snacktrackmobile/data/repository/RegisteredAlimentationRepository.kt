package study.snacktrackmobile.data.repository

import study.snacktrackmobile.data.api.ApiService
import study.snacktrackmobile.data.model.dto.RegisteredAlimentationResponse

class RegisteredAlimentationRepository(private val api: ApiService) {

    suspend fun getMealsForDate(token: String, date: String): List<RegisteredAlimentationResponse> {
        return api.getMyEntries("Bearer $token", date)
    }
}
