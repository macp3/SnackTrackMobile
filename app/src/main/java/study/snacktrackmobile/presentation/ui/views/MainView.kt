package study.snacktrackmobile.presentation.ui.views

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import study.snacktrackmobile.presentation.ui.components.BottomNavigationBar
import study.snacktrackmobile.presentation.ui.components.SnackTrackTopBarCalendar

@Composable
fun MainView(navController: NavController) {
    var selectedDate by remember { mutableStateOf("") }
    var selectedTab by remember { mutableStateOf("Meals") }

    // Cały układ aplikacji
    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        // 🔝 Top bar z kalendarzem (nie fillMaxSize)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()
        ) {
            SnackTrackTopBarCalendar(
                onDateSelected = { date ->
                    selectedDate = date
                }
            )
        }

        // 📦 Zawartość środka
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            contentAlignment = Alignment.Center
        ) {
            when (selectedTab) {
                "Meals" -> Text("Zawartość posiłków na dzień $selectedDate")
                "Training" -> Text("Treningi dla daty $selectedDate")
                "Recipes" -> Text("Przepisy dnia $selectedDate")
                "Shopping" -> Text("Lista zakupów na $selectedDate")
                "Profile" -> Text("Twój profil (data: $selectedDate)")
                else -> Text("Wybierz sekcję i datę")
            }
        }

        // 🔻 Bottom navigation bar
        BottomNavigationBar(
            selectedItem = selectedTab,
            onItemSelected = { tab ->
                selectedTab = tab
            }
        )
    }
}
