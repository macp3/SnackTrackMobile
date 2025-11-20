package study.snacktrackmobile.data.repository

import androidx.compose.ui.text.toLowerCase
import study.snacktrackmobile.data.api.ApiService
import study.snacktrackmobile.data.model.dto.RegisteredAlimentationRequest
import study.snacktrackmobile.data.model.dto.RegisteredAlimentationResponse
import java.time.LocalDate

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
        mealId: Int? = null,      // <--- ID Przepisu (Meal w backendzie)
        mealApiId: Int? = null,
        mealName: String,         // <--- np. "BREAKFAST"
        date: String,
        amount: Float? = null,
        pieces: Float? = null
    ): Boolean {
        val body = RegisteredAlimentationRequest(
            essentialId = essentialId,
            mealId = mealId,
            mealApiId = mealApiId,
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
        // pobierz wszystkie wpisy dla dnia źródłowego
        val allEntries = api.getMyEntries("Bearer $token", fromDate)

        // przefiltruj tylko te z wybranego posiłku
        val sourceEntries = allEntries.filter { it.mealName.equals(fromMealName, ignoreCase = true) }

        sourceEntries.forEach { entry ->
            val essentialId = entry.essentialFood?.id ?: return@forEach
            val dto = RegisteredAlimentationRequest(
                essentialId = essentialId,
                mealApiId = entry.mealApi?.id,
                mealId = entry.meal?.id,
                timestamp = toDate,
                mealName = toMealName,
                amount = entry.amount,
                pieces = entry.pieces
            )
            api.addEntry("Bearer $token", dto, toDate)
        }
    }
}

