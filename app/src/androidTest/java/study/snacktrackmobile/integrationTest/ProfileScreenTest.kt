package study.snacktrackmobile.integrationTest

import androidx.compose.ui.semantics.ProgressBarRangeInfo
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Rule
import org.junit.Test
import study.snacktrackmobile.presentation.ui.components.ProfileScreen
import study.snacktrackmobile.viewmodel.ProfileViewModel
import study.snacktrackmobile.data.model.dto.UserResponse

class ProfileScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private val viewModel = mockk<ProfileViewModel>(relaxed = true)

    @Test
    fun profileScreen_whenLoading_showsProgressIndicator() {
        every { viewModel.loading } returns MutableStateFlow(true)
        every { viewModel.error } returns MutableStateFlow(null)
        every { viewModel.user } returns MutableStateFlow(null)

        composeTestRule.setContent {
            ProfileScreen(viewModel = viewModel, onEditBodyParameters = {})
        }

        composeTestRule.onNode(hasProgressBarRangeInfo(ProgressBarRangeInfo.Indeterminate)).assertExists()
    }

    @Test
    fun profileScreen_whenError_showsErrorMessage() {
        every { viewModel.loading } returns MutableStateFlow(false)
        every { viewModel.error } returns MutableStateFlow("Something went wrong")
        every { viewModel.user } returns MutableStateFlow(null)

        composeTestRule.setContent {
            ProfileScreen(viewModel = viewModel, onEditBodyParameters = {})
        }

        composeTestRule.onNodeWithText("Something went wrong").assertExists()
    }

    @Test
    fun profileScreen_whenUserExists_showsProfileData() {
        val user = UserResponse(
            id = 1,
            name = "Bartosz",
            surname = "Tester",
            email = "bartosz@example.com",
            status = "Active",
            streak = 5,
            premiumExpiration = "2025-12-31",
            imageUrl = null
        )

        every { viewModel.loading } returns MutableStateFlow(false)
        every { viewModel.error } returns MutableStateFlow(null)
        every { viewModel.user } returns MutableStateFlow(user)

        composeTestRule.setContent {
            ProfileScreen(viewModel = viewModel, onEditBodyParameters = {})
        }

        composeTestRule.onNodeWithText("Bartosz Tester").assertExists()
        composeTestRule.onNodeWithText("bartosz@example.com").assertExists()
        composeTestRule.onNodeWithText("Status").assertExists()
        composeTestRule.onNodeWithText("Streak").assertExists()
    }

    @Test
    fun profileScreen_clickChangePassword_opensDialog() {
        val user = UserResponse(
            id = 1,
            name = "Bartosz",
            surname = "Tester",
            email = "bartosz@example.com",
            status = "Active",
            streak = 5,
            premiumExpiration = "2025-12-31",
            imageUrl = null
        )

        every { viewModel.loading } returns MutableStateFlow(false)
        every { viewModel.error } returns MutableStateFlow(null)
        every { viewModel.user } returns MutableStateFlow(user)

        composeTestRule.setContent {
            ProfileScreen(viewModel = viewModel, onEditBodyParameters = {})
        }

        composeTestRule.onNodeWithText("Change Password").performClick()
        composeTestRule.onAllNodesWithText("Change Password").assertCountEquals(2)
    }

    @Test
    fun profileScreen_clickEditBodyParameters_callsCallback() {
        var called = false
        val user = UserResponse(
            id = 1,
            name = "Bartosz",
            surname = "Tester",
            email = "bartosz@example.com",
            status = "Active",
            streak = 5,
            premiumExpiration = "2025-12-31",
            imageUrl = null
        )

        every { viewModel.loading } returns MutableStateFlow(false)
        every { viewModel.error } returns MutableStateFlow(null)
        every { viewModel.user } returns MutableStateFlow(user)

        composeTestRule.setContent {
            ProfileScreen(viewModel = viewModel, onEditBodyParameters = { called = true })
        }

        composeTestRule.onNodeWithText("Edit Body Parameters").performClick()
        assert(called)
    }
}
