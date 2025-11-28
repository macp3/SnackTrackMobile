package study.snacktrackmobile.integrationTest

import androidx.compose.ui.semantics.ProgressBarRangeInfo
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import io.mockk.*
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Rule
import org.junit.Test
import study.snacktrackmobile.presentation.ui.components.ShoppingListScreen
import study.snacktrackmobile.viewmodel.ShoppingListViewModel
import study.snacktrackmobile.data.model.ShoppingList
import study.snacktrackmobile.data.model.ShoppingListItem

class ShoppingScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private val viewModel = mockk<ShoppingListViewModel>(relaxed = true)

    @Test
    fun shoppingListScreen_showsList() {
        every { viewModel.shoppingLists } returns MutableStateFlow(
            listOf(ShoppingList(id = 1, name = "Groceries", date = "2025-11-28", userEmail = "bartosz@example.com"))
        )
        every { viewModel.isLoading } returns MutableStateFlow(false)

        composeTestRule.setContent {
            ShoppingListScreen(viewModel, "2025-11-28", false, {})
        }

        composeTestRule.onNodeWithText("Groceries").assertExists()
    }

    @Test
    fun shoppingListScreen_addNewList_callsViewModel() {
        every { viewModel.shoppingLists } returns MutableStateFlow(emptyList())
        every { viewModel.isLoading } returns MutableStateFlow(false)

        composeTestRule.setContent {
            ShoppingListScreen(viewModel, "2025-11-28", false, {})
        }

        composeTestRule.onNodeWithText("New").performClick()
        composeTestRule.onNodeWithText("New Shopping List").assertExists()
        composeTestRule.onNode(hasSetTextAction()).performTextInput("My List")
        composeTestRule.onNodeWithText("Create").performClick()

        verify { viewModel.addNewList("2025-11-28", "My List", any()) }
    }

    @Test
    fun shoppingListScreen_copyList_opensDialog() {
        every { viewModel.shoppingLists } returns MutableStateFlow(emptyList())
        every { viewModel.isLoading } returns MutableStateFlow(false)

        composeTestRule.setContent {
            ShoppingListScreen(viewModel, "2025-11-28", false, {})
        }

        composeTestRule.onNodeWithText("Copy").performClick()
        composeTestRule.onNodeWithText("OK").assertExists()
    }

    @Test
    fun shoppingListScreen_aiPremium_callsGenerateAiShoppingList() {
        every { viewModel.shoppingLists } returns MutableStateFlow(emptyList())
        every { viewModel.isLoading } returns MutableStateFlow(false)

        composeTestRule.setContent {
            ShoppingListScreen(viewModel, "2025-11-28", true, {})
        }

        composeTestRule.onNodeWithText("AI").performClick()
        composeTestRule.onNodeWithText("AI Assistant").assertExists()
        composeTestRule.onNode(hasSetTextAction()).performTextInput("Pizza ingredients")
        composeTestRule.onNodeWithText("Generate").performClick()

        verify { viewModel.generateAiShoppingList("Pizza ingredients", "2025-11-28", any(), any()) }
    }

    @Test
    fun shoppingListScreen_aiNonPremium_opensUpsellDialog() {
        every { viewModel.shoppingLists } returns MutableStateFlow(emptyList())
        every { viewModel.isLoading } returns MutableStateFlow(false)

        var navigated = false

        composeTestRule.setContent {
            ShoppingListScreen(viewModel, "2025-11-28", false, { navigated = true })
        }

        composeTestRule.onNodeWithText("AI").performClick()
        composeTestRule.onNodeWithText("Premium Feature").assertExists()
        composeTestRule.onNodeWithText("Go Premium").performClick()

        assert(navigated)
    }

    @Test
    fun shoppingListScreen_deleteList_callsViewModel() {
        val list = ShoppingList(id = 1, name = "Groceries", date = "2025-11-28", userEmail = "bartosz@example.com")
        every { viewModel.shoppingLists } returns MutableStateFlow(listOf(list))
        every { viewModel.isLoading } returns MutableStateFlow(false)

        composeTestRule.setContent {
            ShoppingListScreen(viewModel, "2025-11-28", false, {})
        }

        composeTestRule.onNodeWithContentDescription("Delete list").performClick()
        composeTestRule.onNodeWithText("Delete List").assertExists()
        composeTestRule.onNodeWithText("Delete").performClick()

        verify { viewModel.deleteList(list) }
    }

    @Test
    fun shoppingListScreen_addItem_callsViewModel() {
        val list = ShoppingList(id = 1, name = "Groceries", date = "2025-11-28", userEmail = "bartosz@example.com")
        every { viewModel.shoppingLists } returns MutableStateFlow(listOf(list))
        every { viewModel.isLoading } returns MutableStateFlow(false)

        composeTestRule.setContent {
            ShoppingListScreen(viewModel, "2025-11-28", false, {})
        }

        composeTestRule.onNodeWithContentDescription("Add item").performClick()
        composeTestRule.onNodeWithText("Add Item").assertExists()
        composeTestRule.onNodeWithText("Item name").performTextInput("Milk")
        composeTestRule.onNodeWithText("Quantity").performTextInput("1L")
        composeTestRule.onNodeWithText("Add").performClick()

        verify { viewModel.addItemToList(list, "Milk", "1L", any()) }
    }

    @Test
    fun shoppingListScreen_editItem_callsViewModel() {
        val item = ShoppingListItem(name = "Milk", quantity = "1L")
        val list = ShoppingList(
            id = 1,
            name = "Groceries",
            date = "2025-11-28",
            userEmail = "bartosz@example.com",
            items = listOf(item)
        )
        every { viewModel.shoppingLists } returns MutableStateFlow(listOf(list))
        every { viewModel.isLoading } returns MutableStateFlow(false)

        composeTestRule.setContent {
            ShoppingListScreen(viewModel, "2025-11-28", false, {})
        }

        composeTestRule.onNodeWithContentDescription("Edit item").performClick()
        composeTestRule.onNodeWithText("Edit Item").assertExists()
        composeTestRule.onNodeWithText("Item name").performTextClearance()
        composeTestRule.onNodeWithText("Item name").performTextInput("Milk 2L")
        composeTestRule.onNodeWithText("Save").performClick()

        verify { viewModel.editItemInList(list, item, "Milk 2L", any(), any()) }
    }

    @Test
    fun shoppingListScreen_editList_callsViewModel() {
        val list = ShoppingList(id = 1, name = "Groceries", date = "2025-11-28", userEmail = "bartosz@example.com")
        every { viewModel.shoppingLists } returns MutableStateFlow(listOf(list))
        every { viewModel.isLoading } returns MutableStateFlow(false)

        composeTestRule.setContent {
            ShoppingListScreen(viewModel, "2025-11-28", false, {})
        }

        composeTestRule.onNodeWithContentDescription("Edit list name").performClick()
        composeTestRule.onNodeWithText("Edit List Name").assertExists()
        composeTestRule.onNodeWithText("List name").performTextClearance()
        composeTestRule.onNodeWithText("List name").performTextInput("Supermarket")
        composeTestRule.onNodeWithText("Save").performClick()

        verify { viewModel.updateListName(list, "Supermarket") }
    }

    @Test
    fun shoppingListScreen_loading_showsOverlay() {
        every { viewModel.shoppingLists } returns MutableStateFlow(emptyList())
        every { viewModel.isLoading } returns MutableStateFlow(true)

        composeTestRule.setContent {
            ShoppingListScreen(viewModel, "2025-11-28", false, {})
        }

        composeTestRule.onNode(hasProgressBarRangeInfo(ProgressBarRangeInfo.Indeterminate)).assertExists()
    }
}
