package study.snacktrackmobile.integrationTest

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import io.mockk.*
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import study.snacktrackmobile.data.api.TrainingApi
import study.snacktrackmobile.data.model.dto.*
import study.snacktrackmobile.presentation.ui.views.TrainingView
import study.snacktrackmobile.viewmodel.TrainingViewModel
import java.time.LocalDate

class TrainingViewTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private val mockApi = mockk<TrainingApi>(relaxed = true)
    private lateinit var viewModel: TrainingViewModel
    private val mockAuthToken = "dummy_token"

    private val mockTrainingInfo = TrainingInfoDTO(1, "Full Body Workout", "Intense plan", 7)
    private val mockExercise = ExerciseDTO(101, "Push-ups", "Chest and biceps", "STRENGTH", 3, 3, 10)
    private val mockTrainingDetails = TrainingDetailsResponseDTO(
        trainingInfo = mockTrainingInfo,
        exercises = listOf(
            ExerciseDayDTO(dayOfExercise = 1, exercise = mockExercise),
            ExerciseDayDTO(dayOfExercise = 1, exercise = mockExercise.copy(id = 102, name = "Squats"))
        )
    )
    private val mockAvailableTrainings = listOf(
        mockTrainingInfo,
        TrainingInfoDTO(2, "Cardio Blast", "Fat burning", 5)
    )

    @Before
    fun setup() {
        viewModel = TrainingViewModel(mockApi)
    }

    @Test
    fun trainingView_whenNoTrainingAssigned_showsAvailableTrainingsList() = runTest {
        coEvery { mockApi.getUserTraining(any()) } throws IllegalStateException("No training assigned")
        coEvery { mockApi.getAllTrainings(any()) } returns mockAvailableTrainings

        viewModel.initialize(mockAuthToken)
        composeTestRule.waitForIdle()

        composeTestRule.setContent {
            TrainingView(
                viewModel = viewModel,
                selectedDate = LocalDate.now().toString(),
                authToken = mockAuthToken,
                onDateSelected = {}
            )
        }

        composeTestRule.onNodeWithText("Full Body Workout").assertIsDisplayed()
        composeTestRule.onNodeWithText("Cardio Blast").assertIsDisplayed()

        val assignButtons = composeTestRule.onAllNodes(hasText("Assign Training") and hasClickAction())
        assignButtons[0].assertIsDisplayed()
        assignButtons[1].assertIsDisplayed()
    }

    @Test
    fun trainingView_whenTrainingIsAssigned_showsTrainingDetails() = runTest {
        coEvery { mockApi.getUserTraining(any()) } returns TrainingInfoDTO(
            1,
            "Full Body Beginner",
            "Beginner-friendly full body workout for all muscle groups",
            30
        )
        coEvery { mockApi.getUserTrainingDetails(any()) } returns TrainingDetailsResponseDTO(
            trainingInfo = TrainingInfoDTO(1, "Full Body Beginner", "Beginner-friendly full body workout for all muscle groups", 30),
            exercises = listOf(
                ExerciseDayDTO(1, ExerciseDTO(1, "Push-ups", "Classic push-ups", "Strength", 2, 3, 12)),
                ExerciseDayDTO(1, ExerciseDTO(2, "Squats", "Body weight squats", "Strength", 2, 3, 15))
            )
        )

        viewModel.initialize(mockAuthToken)

        val monday = LocalDate.now()
            .with(java.time.temporal.TemporalAdjusters.previousOrSame(java.time.DayOfWeek.MONDAY))

        composeTestRule.setContent {
            TrainingView(
                viewModel = viewModel,
                selectedDate = monday.toString(),
                authToken = mockAuthToken,
                onDateSelected = {}
            )
        }

        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithText("Full Body Beginner", useUnmergedTree = true).assertExists()
        composeTestRule.onNodeWithText("Push-ups", useUnmergedTree = true).assertExists()
        composeTestRule.onNodeWithText("Squats", useUnmergedTree = true).assertExists()
        composeTestRule.onNodeWithText("Detach Training", useUnmergedTree = true).assertExists()
    }
}
