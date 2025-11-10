package study.snacktrackmobile.viewmodel

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import retrofit2.Response
import study.snacktrackmobile.data.api.TrainingApi
import study.snacktrackmobile.data.model.dto.ExerciseDTO
import study.snacktrackmobile.data.model.dto.TrainingDetailsResponseDTO
import study.snacktrackmobile.data.model.dto.TrainingInfoDTO
import java.time.LocalDate
import java.time.format.DateTimeParseException

class TrainingViewModel(
    private val api: TrainingApi
) : ViewModel() {

    var userTraining by mutableStateOf<TrainingInfoDTO?>(null)
        private set

    // jeśli backend zwraca info o dacie przypisania, trzymajmy ją tutaj
    // (np. TrainingInfoDTO może mieć pole assignedDate: String? - jeśli nie, ustawiamy null)
    var userTrainingAssignedDate by mutableStateOf<LocalDate?>(null)
        private set

    var trainingDetails by mutableStateOf<TrainingDetailsResponseDTO?>(null)
        private set

    var availableTrainings by mutableStateOf<List<TrainingInfoDTO>>(emptyList())
        private set

    var isLoading by mutableStateOf(false)
        private set

    /**
     * Inicjalizacja: spróbuj pobrać przypisany trening. Jeśli jest — pobierz szczegóły,
     * w przeciwnym razie pobierz listę dostępnych treningów.
     */
    fun initialize(authToken: String?) {
        if (authToken == null) return
        viewModelScope.launch {
            isLoading = true
            try {
                // pobierz aktualny trening użytkownika (jeśli jest)
                val tokenHeader = "Bearer $authToken"
                // jeśli backend zwraca pole timestamp lub assignedDate w getUserTraining,
                // TrainingInfoDTO powinno je zawierać - tutaj próbujemy je sparsować
                userTraining = try {
                    api.getUserTraining(tokenHeader)
                } catch (e: Exception) {
                    null
                }

                // jeśli mamy przypisany trening -> pobierz jego szczegóły i (opcjonalnie) ustaw datę przypisania
                if (userTraining != null) {
                    trainingDetails = try {
                        api.getUserTrainingDetails(tokenHeader)
                    } catch (e: Exception) {
                        null
                    }
                    Log.d("TrainingDebug", "TrainingDetails loaded: ${trainingDetails != null}")
                    trainingDetails?.let { details ->
                        Log.d("TrainingDebug", "Training name: ${details.trainingInfo.name}")
                        Log.d("TrainingDebug", "Exercises count: ${details.exercises.size}")
                        details.exercises.take(5).forEach {
                            Log.d("TrainingDebug", "Day ${it.dayOfExercise}: ${it.exercise.name}")
                        }
                    }

                    // jeżeli TrainingInfoDTO ma pole assignedDate/timestamp (String), obsłużmy je:
                    // np. jeśli TrainingInfoDTO ma field "assignedDate" albo "timestamp", sparuj:
                    // userTrainingAssignedDate = LocalDate.parse(userTraining.assignedDate)
                    // Jeśli nie masz takiego pola, userTrainingAssignedDate pozostanie null.
                    userTrainingAssignedDate = try {
                        // próbka: jeśli ID  zawiera datę w DTO uncomment i dopasuj nazwę pola
                        // LocalDate.parse(userTraining!!.assignedDate)
                        null
                    } catch (e: DateTimeParseException) {
                        null
                    }
                } else {
                    // brak przypisanego treningu -> pobierz wszystkie dostępne
                    availableTrainings = try {
                        api.getAllTrainings(tokenHeader) // jeśli Twój API wymaga headera, użyj api.getAllTrainings(tokenHeader)
                    } catch (e: Exception) {
                        emptyList()
                    }
                }

            } finally {
                isLoading = false
            }
        }
    }

    /**
     * Przypisz trening -> po sukcesie zrób initialize, żeby odświeżyć wszystko poprawnie.
     */
    fun assignTraining(trainingId: Int, authToken: String?) {
        if (authToken == null) return
        viewModelScope.launch {
            val tokenHeader = "Bearer $authToken"
            try {
                val response: Response<Unit> = api.assignTraining(trainingId, tokenHeader)
                if (response.isSuccessful) {
                    // udane przypisanie -> odśwież stan
                    initialize(authToken)
                } else {
                    // obsłuż błąd: możesz dodać log, toast itp.
                }
            } catch (e: Exception) {
                // obsługa błędu
            }
        }
    }

    /**
     * Odpięcie treningu -> po sukcesie odśwież stan
     */
    fun depriveTraining(authToken: String?) {
        if (authToken == null) return
        viewModelScope.launch {
            val tokenHeader = "Bearer $authToken"
            try {
                val response: Response<Unit> = api.depriveTraining(tokenHeader)
                if (response.isSuccessful) {
                    // odczepiono - odśwież (będzie lista dostępnych)
                    initialize(authToken)
                } else {
                    // obsłuż błąd odpowiedzi
                }
            } catch (e: Exception) {
                // obsługa błędu
            }
        }
    }

    /**
     * Utility: zwraca listę ćwiczeń dla danego "training day index".
     * Jeśli backend przechowuje exercises jako Map<Int, List<ExerciseDTO>> (klucz = day number)
     * to metoda ta zwraca odpowiednie ćwiczenia.
     */
    fun exercisesForTrainingDay(dayIndex: Int): List<ExerciseDTO> {
        val details = trainingDetails ?: return emptyList()
        return details.exercisesForDay(dayIndex)
    }
}
