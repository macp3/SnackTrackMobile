@file:OptIn(ExperimentalCoroutinesApi::class)

package study.snacktrackmobile.integrationTest

import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.semantics.ProgressBarRangeInfo
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.navigation.compose.rememberNavController
import io.mockk.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Rule
import org.junit.Test
import study.snacktrackmobile.presentation.ui.components.MealsDailyView
import study.snacktrackmobile.presentation.ui.components.MealCard
import study.snacktrackmobile.presentation.ui.components.AddProductToDatabaseScreen
import study.snacktrackmobile.viewmodel.RegisteredAlimentationViewModel
import study.snacktrackmobile.viewmodel.FoodViewModel
import study.snacktrackmobile.data.model.Meal
import study.snacktrackmobile.data.model.dto.EssentialFoodResponse
import study.snacktrackmobile.data.model.dto.RegisteredAlimentationResponse
import study.snacktrackmobile.presentation.ui.components.SummaryBar
import study.snacktrackmobile.presentation.ui.state.SummaryBarState

class RegisteredAlimentationScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private val viewModel = mockk<RegisteredAlimentationViewModel>(relaxed = true)
    private val foodViewModel = mockk<FoodViewModel>(relaxed = true)
    private val navController = mockk<androidx.navigation.NavController>(relaxed = true)

    @Test
    fun mealsDailyView_whenLoading_showsProgressIndicator() {
        every { viewModel.isLoading } returns MutableStateFlow(true)
        every { viewModel.meals } returns MutableStateFlow(emptyList())

        composeTestRule.setContent {
            MealsDailyView(
                selectedDate = "2025-11-28",
                viewModel = viewModel,
                navController = rememberNavController(),
                isUserPremium = false,
                onNavigateToPremium = {},
                onEditProduct = {},
                onAddProductClick = {}
            )
        }

        composeTestRule.onNode(hasProgressBarRangeInfo(ProgressBarRangeInfo.Indeterminate)).assertExists()
    }

    @Test
    fun mealsDailyView_whenNotLoading_showsMealCards() {
        val meals = listOf(Meal(name = "Breakfast", alimentations = emptyList(), kcal = 0))
        every { viewModel.isLoading } returns MutableStateFlow(false)
        every { viewModel.meals } returns MutableStateFlow(meals)

        composeTestRule.setContent {
            MealsDailyView(
                selectedDate = "2025-11-28",
                viewModel = viewModel,
                navController = rememberNavController(),
                isUserPremium = false,
                onNavigateToPremium = {},
                onEditProduct = {},
                onAddProductClick = {}
            )
        }

        composeTestRule.onNodeWithText("Breakfast").assertExists()
    }

    @Test
    fun mealCard_copyMeal_callsViewModel() {
        val meal = Meal(name = "Lunch", alimentations = emptyList(), kcal = 0)

        composeTestRule.setContent {
            MealCard(
                meal = meal,
                viewModel = viewModel,
                selectedDate = "2025-11-28",
                onEditProduct = {},
                onAddProductClick = {}
            )
        }

        composeTestRule.onNodeWithContentDescription("Copy meal").performClick()
        composeTestRule.onNodeWithText("Copy meal").assertExists()
        composeTestRule.onNodeWithText("Confirm").performClick()

        verify { viewModel.copyMeal(any(), any(), any(), any(), any()) }
    }

    @Test
    fun mealsDailyView_premiumUser_opensAiDialog_andCallsGeneratePlan() {
        every { viewModel.isLoading } returns MutableStateFlow(false)
        every { viewModel.meals } returns MutableStateFlow(emptyList())

        composeTestRule.setContent {
            MealsDailyView(
                selectedDate = "2025-11-28",
                viewModel = viewModel,
                navController = rememberNavController(),
                isUserPremium = true,
                onNavigateToPremium = {},
                onEditProduct = {},
                onAddProductClick = {}
            )
        }

        composeTestRule.onNodeWithText("Auto Plan").performClick()
        composeTestRule.onNodeWithText("AI Diet Planner").assertExists()

        composeTestRule.onNode(hasSetTextAction()).performTextInput("High protein")
        composeTestRule.onNodeWithText("Generate Plan").performClick()

        verify { viewModel.generateAiDiet(any(), "2025-11-28", "High protein") }
    }

    @Test
    fun mealsDailyView_nonPremiumUser_opensUpsellDialog_andCallsNavigate() {
        every { viewModel.isLoading } returns MutableStateFlow(false)
        every { viewModel.meals } returns MutableStateFlow(emptyList())

        var navigated = false

        composeTestRule.setContent {
            MealsDailyView(
                selectedDate = "2025-11-28",
                viewModel = viewModel,
                navController = rememberNavController(),
                isUserPremium = false,
                onNavigateToPremium = { navigated = true },
                onEditProduct = {},
                onAddProductClick = {}
            )
        }

        composeTestRule.onNodeWithText("Auto Plan (Pro)").performClick()
        composeTestRule.onNodeWithText("Premium Feature").assertExists()
        composeTestRule.onNodeWithText("Go Premium").performClick()

        assert(navigated)
    }

    @Test
    fun mealCard_addProduct_callsOnAddProductClick() {
        var clickedMeal: String? = null
        val meal = Meal(name = "Dinner", alimentations = emptyList(), kcal = 0)

        composeTestRule.setContent {
            MealCard(
                meal = meal,
                viewModel = viewModel,
                selectedDate = "2025-11-28",
                onEditProduct = {},
                onAddProductClick = { clickedMeal = it }
            )
        }

        composeTestRule.onNodeWithContentDescription("Add product").performClick()
        assert(clickedMeal == "Dinner")
    }

    @Test
    fun addProductToDatabaseScreen_emptyForm_showsValidationError() {
        every { foodViewModel.errorMessage } returns mutableStateOf("Field 'name' is required")
        every { foodViewModel.success } returns mutableStateOf(false)

        composeTestRule.setContent {
            AddProductToDatabaseScreen(
                navController = navController,
                foodViewModel = foodViewModel
            )
        }

        composeTestRule.onNodeWithText("Save").performClick()
        verify { foodViewModel.setError("Field 'name' is required") }
    }

    @Test
    fun summaryBar_updatesWhenEssentialFoodAdded() {
        val apple = EssentialFoodResponse(
            id = 1,
            name = "Apple",
            description = "Fresh apple",
            calories = 52.0,
            protein = 0.3,
            fat = 0.2,
            carbohydrates = 14.0,
            defaultWeight = 100f,
            servingSizeUnit = "g"
        )

        val alimentation = RegisteredAlimentationResponse(
            id = 1,
            userId = 1,
            essentialFood = apple,
            mealApi = null,
            meal = null,
            timestamp = "2025-11-28",
            amount = 100f,
            pieces = 0f,
            mealName = "Breakfast"
        )

        val meal = Meal(name = "Breakfast", alimentations = listOf(alimentation), kcal = 0)

        SummaryBarState.update(listOf(meal))
        SummaryBarState.setLimits(kcal = 2000f, protein = 100f, fat = 70f, carbs = 250f)

        composeTestRule.setContent {
            SummaryBar()
        }

        composeTestRule.onNodeWithText("52").assertExists()
        composeTestRule.onNodeWithText("14").assertExists()
        composeTestRule.onAllNodesWithText("0").assertCountEquals(2)
    }
}
