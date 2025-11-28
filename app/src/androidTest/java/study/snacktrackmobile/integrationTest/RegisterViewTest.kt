package study.snacktrackmobile.integrationTest

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
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
import study.snacktrackmobile.presentation.ui.state.UiState
import study.snacktrackmobile.presentation.ui.views.RegisterView
import study.snacktrackmobile.viewmodel.UserViewModel

class RegisterViewTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private val mockViewModel = mockk<UserViewModel>(relaxed = true)
    private val navController = TestNavHostController(ApplicationProvider.getApplicationContext())
    private val registerStateFlow = MutableStateFlow<UiState<String>>(UiState.Idle)

    private fun setupRegisterView() {
        every { mockViewModel.registerState } returns registerStateFlow

        val destinationNavigator = ComposeNavigator()
        navController.navigatorProvider.addNavigator(destinationNavigator)
        val navGraphNavigator = navController.navigatorProvider.getNavigator(NavGraphNavigator::class.java)

        val testGraph = NavGraph(navGraphNavigator).apply {
            val registerDestination = destinationNavigator.createDestination().apply {
                id = 1
                route = "RegisterView"
            }
            val loginDestination = destinationNavigator.createDestination().apply {
                id = 2
                route = "LoginView"
            }

            addDestination(registerDestination)
            addDestination(loginDestination)
            setStartDestination(registerDestination.id)
        }

        composeTestRule.runOnUiThread {
            navController.setGraph(testGraph, null)
            navController.navigate("RegisterView")
        }

        composeTestRule.setContent {
            RegisterView(navController = navController, viewModel = mockViewModel)
        }
    }

    @Test
    fun registerView_invalidPasswords_showsFrontendError() {
        setupRegisterView()
        composeTestRule.onNodeWithText("Name").performTextInput("Jan")
        composeTestRule.onNodeWithText("Surname").performTextInput("Kowalski")
        composeTestRule.onNodeWithText("Email").performTextInput("jan.kowalski@test.com")
        composeTestRule.onNodeWithText("Password").performTextInput("password123")
        composeTestRule.onNodeWithText("Confirm Password").performTextInput("password321")

        composeTestRule.onNodeWithText("Register").performClick()

        composeTestRule.onNodeWithText("Passwords do not match").assertIsDisplayed()
        assert(navController.currentBackStackEntry?.destination?.route == "RegisterView")
    }

    @Test
    fun registerView_successfulRegistration_navigatesToLogin() {
        setupRegisterView()

        composeTestRule.onNodeWithText("Name").performTextInput("Anna")
        composeTestRule.onNodeWithText("Surname").performTextInput("Nowak")
        composeTestRule.onNodeWithText("Email").performTextInput("anna.nowak@test.com")
        composeTestRule.onNodeWithText("Password").performTextInput("securepassword")
        composeTestRule.onNodeWithText("Confirm Password").performTextInput("securepassword")

        composeTestRule.onNodeWithText("Register").performClick()

        registerStateFlow.value = UiState.Success("Registration successful")

        composeTestRule.waitForIdle()
        assert(navController.currentBackStackEntry?.destination?.route == "LoginView")
    }

    @Test
    fun registerView_backendError_showsErrorMessage() {
        setupRegisterView()

        composeTestRule.onNodeWithText("Name").performTextInput("Error")
        composeTestRule.onNodeWithText("Surname").performTextInput("User")
        composeTestRule.onNodeWithText("Email").performTextInput("error@test.com")
        composeTestRule.onNodeWithText("Password").performTextInput("securepassword")
        composeTestRule.onNodeWithText("Confirm Password").performTextInput("securepassword")

        composeTestRule.onNodeWithText("Register").performClick()

        val errorMessage = "Email already in use"
        registerStateFlow.value = UiState.Error(errorMessage)

        composeTestRule.onNodeWithText(errorMessage).assertIsDisplayed()
    }
}
