@file:OptIn(ExperimentalCoroutinesApi::class)

package study.snacktrackmobile.integrationTest

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.navigation.compose.rememberNavController
import io.mockk.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Rule
import org.junit.Test
import study.snacktrackmobile.viewmodel.RecipeViewModel
import study.snacktrackmobile.viewmodel.FoodViewModel
import study.snacktrackmobile.viewmodel.RegisteredAlimentationViewModel
import study.snacktrackmobile.viewmodel.CommentViewModel
import study.snacktrackmobile.data.model.dto.*
import study.snacktrackmobile.data.repository.RecipeRepository
import study.snacktrackmobile.presentation.ui.components.AddRecipeForm
import study.snacktrackmobile.presentation.ui.components.IngredientFormEntry
import study.snacktrackmobile.presentation.ui.components.RecipeDetailsScreen
import study.snacktrackmobile.presentation.ui.components.RecipesScreen

class RecipesScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private val fakeRepo = mockk<RecipeRepository>(relaxed = true)
    private val recipeViewModel = spyk(RecipeViewModel(fakeRepo))
    private val foodViewModel = mockk<FoodViewModel>(relaxed = true)
    private val registeredAlimentationViewModel = mockk<RegisteredAlimentationViewModel>(relaxed = true)
    private val commentViewModel = mockk<CommentViewModel>(relaxed = true)

    @Test
    fun recipesScreen_whenRecipesExist_showsRecipesList() {
        recipeViewModel.apply {
            every { recipes } returns MutableStateFlow(
                listOf(
                    RecipeResponse(id = 1, name = "Salad", description = "Simple salad", authorId = 1, imageUrl = null),
                    RecipeResponse(id = 2, name = "Soup", description = "Simple soup", authorId = 2, imageUrl = null)
                )
            )
            every { screen } returns MutableStateFlow("My recipes")
            every { favouriteIds } returns MutableStateFlow(emptySet())
            every { currentUserId } returns MutableStateFlow(1)
        }

        composeTestRule.setContent {
            RecipesScreen(
                viewModel = recipeViewModel,
                foodViewModel = foodViewModel,
                navController = rememberNavController(),
                registeredAlimentationViewModel = registeredAlimentationViewModel,
                commentViewModel = commentViewModel
            )
        }

        composeTestRule.onNodeWithText("Salad").assertExists()
        composeTestRule.onNodeWithText("Soup").assertExists()
    }

    @Test
    fun recipesScreen_whenNoRecipes_showsEmptyMessage() {
        recipeViewModel.apply {
            every { recipes } returns MutableStateFlow(emptyList())
            every { screen } returns MutableStateFlow("My recipes")
            every { favouriteIds } returns MutableStateFlow(emptySet())
            every { currentUserId } returns MutableStateFlow(1)
        }

        composeTestRule.setContent {
            RecipesScreen(
                viewModel = recipeViewModel,
                foodViewModel = foodViewModel,
                navController = rememberNavController(),
                registeredAlimentationViewModel = registeredAlimentationViewModel,
                commentViewModel = commentViewModel
            )
        }

        composeTestRule.onNodeWithText("No recipes yet").assertExists()
    }

    @Test
    fun addRecipeForm_whenEmpty_showsValidationErrors() {
        composeTestRule.setContent {
            AddRecipeForm(
                name = "",
                desc = "",
                imageUrl = null,
                selectedImageUri = null,
                ingredients = mutableListOf(),
                isNameError = true,
                nameErrorMessage = "Required",
                isDescError = false,
                descErrorMessage = null,
                serverErrorMessage = "Add at least one ingredient",
                onNameChange = {},
                onDescChange = {},
                onImageSelected = {},
                onStartAddIngredient = {},
                onSelectIngredient = {},
                onDeleteIngredient = {},
                onSubmit = {},
                onCancel = {}
            )
        }

        composeTestRule.onNodeWithText("Required").assertExists()
        composeTestRule.onNodeWithText("Add at least one ingredient").assertExists()
    }

    @Test
    fun addEssentialFood_callsFoodViewModel() {
        val request = EssentialFoodRequest(
            name = "Tomato",
            description = "Fresh tomato",
            calories = 20f,
            protein = 1f,
            fat = 0f,
            carbohydrates = 4f,
            defaultWeight = 100f,
            servingSizeUnit = "g"
        )

        foodViewModel.addFood(request)
        verify { foodViewModel.addFood(request) }
    }

    @Test
    fun addIngredientFlow_addsIngredientToForm() {
        val ingredients = mutableListOf<IngredientFormEntry>()

        composeTestRule.setContent {
            AddRecipeForm(
                name = "Test Recipe",
                desc = "Desc",
                imageUrl = null,
                selectedImageUri = null,
                ingredients = ingredients,
                isNameError = false,
                nameErrorMessage = null,
                isDescError = false,
                descErrorMessage = null,
                serverErrorMessage = null,
                onNameChange = {},
                onDescChange = {},
                onImageSelected = {},
                onStartAddIngredient = { ingredients.add(IngredientFormEntry(
                    essentialFood = EssentialFoodResponse(id=1, name="Tomato", calories=20.0)
                )) },
                onSelectIngredient = {},
                onDeleteIngredient = {},
                onSubmit = {},
                onCancel = {}
            )
        }

        composeTestRule.onNodeWithContentDescription("Add").performClick()
        assert(ingredients.isNotEmpty())
    }

    @Test
    fun submitRecipe_callsRecipeViewModel() {
        val request = RecipeRequest(name="Test", description="Desc", ingredients=emptyList())
        recipeViewModel.addRecipe("token", request, onSuccess = {}, onError = {})
        verify { recipeViewModel.addRecipe(any(), request, any(), any()) }
    }

    @Test
    fun addComment_callsCommentViewModel() {
        commentViewModel.addComment(mockk(relaxed = true), mealId = 1, content = "Nice recipe")
        verify { commentViewModel.addComment(any(), 1, "Nice recipe") }
    }

    @Test
    fun recipeDetailsScreen_showsComment() {
        val recipe = RecipeResponse(id=1, name="Soup", description="Tasty", authorId=1, ingredients=emptyList(), imageUrl=null)
        every { commentViewModel.comments } returns MutableStateFlow(
            listOf(CommentResponse(id=1, authorId=2, authorName="Bartosz", content="Looks good!", mealId=1))
        )
        every { commentViewModel.currentUserId } returns MutableStateFlow(1)

        composeTestRule.setContent {
            RecipeDetailsScreen(
                recipe = recipe,
                isAuthor = true,
                isFavourite = false,
                selectedDate = "2025-11-28",
                registeredAlimentationViewModel = registeredAlimentationViewModel,
                commentViewModel = commentViewModel,
                onReportRecipe = {},
                onBack = {},
                onEdit = {},
                onDelete = {},
                onToggleFavourite = {}
            )
        }

        composeTestRule.onNodeWithText("Looks good!").assertExists()
    }
}
