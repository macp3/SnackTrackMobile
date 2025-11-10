package study.snacktrackmobile.data.model.dto

// TrainingInfoDTO.kt
data class TrainingInfoDTO(
    val id: Int,
    val name: String,
    val description: String,
    val durationTime: Int
)

// ExerciseDTO.kt
data class ExerciseDTO(
    val id: Int,
    val name: String,
    val description: String,
    val type: String,
    val difficulty: Int,
    val numberOfSets: Int,
    val repetitionsPerSet: Int
)


data class TrainingDetailsResponseDTO(
    val trainingInfo: TrainingInfoDTO,
    val exercises: List<ExerciseDayDTO>
) {
    fun exercisesForDay(dayIndex: Int): List<ExerciseDTO> {
        // Grupowanie listy po dniu ćwiczeń
        val grouped = exercises.groupBy { it.dayOfExercise }
        return grouped[dayIndex]?.map { it.exercise } ?: emptyList()
    }
}

data class ExerciseDayDTO(
    val dayOfExercise: Int,
    val exercise: ExerciseDTO
)