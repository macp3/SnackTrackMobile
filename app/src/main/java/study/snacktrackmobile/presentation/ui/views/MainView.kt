package study.snacktrackmobile.presentation.ui.views

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.navigation.NavController
import study.snacktrackmobile.presentation.ui.components.SnackTrackTopBar
import study.snacktrackmobile.presentation.ui.components.SnackTrackTopBarWithIcons

@Composable
fun MainView(navController: NavController)
{
    SnackTrackTopBarWithIcons()
}