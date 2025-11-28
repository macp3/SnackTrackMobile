package study.snacktrackmobile.integrationTest

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.*
import androidx.navigation.NavGraph
import androidx.navigation.NavGraphNavigator
import androidx.navigation.compose.ComposeNavigator
import androidx.navigation.testing.TestNavHostController
import androidx.test.core.app.ApplicationProvider
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Rule
import org.junit.Test
import study.snacktrackmobile.data.model.LoginResponse
import study.snacktrackmobile.presentation.ui.state.UiState
import study.snacktrackmobile.presentation.ui.views.LoginView
import study.snacktrackmobile.viewmodel.UserViewModel

class LoginViewTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private val mockViewModel = mockk<UserViewModel>(relaxed = true)
    private val navController = TestNavHostController(ApplicationProvider.getApplicationContext())
    private val loginStateFlow = MutableStateFlow<UiState<LoginResponse>>(UiState.Idle)

    private companion object {
        const val LOGIN_ID = 1
        const val SURVEY_ID = 2
        const val MAIN_ID = 3
    }

    private fun setupLoginView(initialState: UiState<LoginResponse> = UiState.Idle) {
        loginStateFlow.value = initialState
        every { mockViewModel.loginState } returns loginStateFlow

        val composeNavigator = ComposeNavigator()
        navController.navigatorProvider.addNavigator(composeNavigator)
        val navGraphNavigator = navController.navigatorProvider.getNavigator(NavGraphNavigator::class.java)

        val testGraph = NavGraph(navGraphNavigator).apply {
            val loginDestination = composeNavigator.createDestination().apply {
                id = LOGIN_ID
                route = "login"
            }
            val surveyDestination = composeNavigator.createDestination().apply {
                id = SURVEY_ID
                route = "InitialSurveyView"
            }
            val mainDestination = composeNavigator.createDestination().apply {
                id = MAIN_ID
                route = "MainView"
            }

            addDestination(loginDestination)
            addDestination(surveyDestination)
            addDestination(mainDestination)

            this.setStartDestination(loginDestination.id)
        }

        composeTestRule.runOnUiThread {
            navController.setGraph(testGraph, null)
            navController.navigate("login")
        }

        composeTestRule.setContent {
            LoginView(navController = navController, viewModel = mockViewModel)
        }
    }

    @Test
    fun loginView_emptyFields_showsValidationErrors() {
        setupLoginView()

        composeTestRule.onNodeWithText("Login").performClick()
        composeTestRule.onNodeWithText("Email cannot be empty").assertIsDisplayed()
    }

    @Test
    fun loginView_successfulLogin_navigatesToMainView_ifSurveyCompleted() {
        setupLoginView()

        composeTestRule.onNodeWithText("Email").performTextInput("test@test.com")
        composeTestRule.onNodeWithText("Password").performTextInput("securepassword")

        composeTestRule.onNodeWithText("Login").performClick()

        val successResponse = LoginResponse(
            token = "mock_token",
            message = "Login successful",
            showSurvey = false
        )
        loginStateFlow.value = UiState.Success(successResponse)

        composeTestRule.waitForIdle()
        val currentRoute = navController.currentBackStackEntry?.destination?.route
        assert(currentRoute == "MainView") { "Expected MainView, but was $currentRoute" }
    }

    @Test
    fun loginView_successfulLogin_navigatesToSurvey_ifSurveyRequired() {
        setupLoginView()

        composeTestRule.onNodeWithText("Email").performTextInput("test@test.com")
        composeTestRule.onNodeWithText("Password").performTextInput("securepassword")

        composeTestRule.onNodeWithText("Login").performClick()

        val successResponse = LoginResponse(
            token = "mock_token",
            message = "Login successful",
            showSurvey = true
        )
        loginStateFlow.value = UiState.Success(successResponse)

        composeTestRule.waitForIdle()
        val currentRoute = navController.currentBackStackEntry?.destination?.route
        assert(currentRoute == "InitialSurveyView") { "Expected InitialSurveyView, but was $currentRoute" }
    }

    @Test
    fun loginView_backendError_showsErrorMessage() {
        setupLoginView()

        composeTestRule.onNodeWithText("Email").performTextInput("test@test.com")
        composeTestRule.onNodeWithText("Password").performTextInput("securepassword")

        composeTestRule.onNodeWithText("Login").performClick()

        val errorMessage = "Invalid credentials"
        loginStateFlow.value = UiState.Error(errorMessage)

        composeTestRule.onNodeWithText(errorMessage).assertIsDisplayed()
        val currentRoute = navController.currentBackStackEntry?.destination?.route
        assert(currentRoute == "login") { "Expected to remain on login, but was $currentRoute" }
    }
}
