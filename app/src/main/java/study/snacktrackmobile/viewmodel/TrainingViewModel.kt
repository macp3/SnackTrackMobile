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

    var userTrainingAssignedDate by mutableStateOf<LocalDate?>(null)
        private set

    var trainingDetails by mutableStateOf<TrainingDetailsResponseDTO?>(null)
        private set

    var availableTrainings by mutableStateOf<List<TrainingInfoDTO>>(emptyList())
        private set

    var isLoading by mutableStateOf(false)
        private set

    fun initialize(authToken: String?) {
        if (authToken == null) return
        viewModelScope.launch {
            isLoading = true
            try {
                val tokenHeader = "Bearer $authToken"
                userTraining = try {
                    api.getUserTraining(tokenHeader)
                } catch (e: Exception) {
                    null
                }

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

                    userTrainingAssignedDate = try {
                        null
                    } catch (e: DateTimeParseException) {
                        null
                    }
                } else {
                    availableTrainings = try {
                        api.getAllTrainings(tokenHeader)
                    } catch (e: Exception) {
                        emptyList()
                    }
                }

            } finally {
                isLoading = false
            }
        }
    }

    fun assignTraining(trainingId: Int, authToken: String?) {
        if (authToken == null) return
        viewModelScope.launch {
            val tokenHeader = "Bearer $authToken"
            try {
                val response: Response<Unit> = api.assignTraining(trainingId, tokenHeader)
                if (response.isSuccessful) {
                    initialize(authToken)
                }
            } catch (e: Exception) {
            }
        }
    }

    fun depriveTraining(authToken: String?) {
        if (authToken == null) return
        viewModelScope.launch {
            val tokenHeader = "Bearer $authToken"
            try {
                val response: Response<Unit> = api.depriveTraining(tokenHeader)
                if (response.isSuccessful) {
                    initialize(authToken)
                }
            } catch (e: Exception) {
            }
        }
    }

    fun exercisesForTrainingDay(dayIndex: Int): List<ExerciseDTO> {
        val details = trainingDetails ?: return emptyList()
        return details.exercisesForDay(dayIndex)
    }
}
