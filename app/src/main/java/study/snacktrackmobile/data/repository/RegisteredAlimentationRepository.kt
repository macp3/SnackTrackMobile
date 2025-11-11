package study.snacktrackmobile.data.repository

import androidx.compose.ui.text.toLowerCase
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
        essentialId: Int,
        mealName: String,
        date: String,
        amount: Float,
        pieces: Int
    ): Boolean {
        val body = RegisteredAlimentationRequest(
            essentialId = essentialId,
            timestamp = date,
            mealName = mealName.lowercase(),
            amount = amount,
            pieces = pieces
        )

        val res = api.addEntry("Bearer $token", body, date)
        return res.isSuccessful
    }

    suspend fun updateEntry(token: String, id: Int, dto: RegisteredAlimentationRequest) {
        api.updateEntry("Bearer $token", id, dto)
    }

}

