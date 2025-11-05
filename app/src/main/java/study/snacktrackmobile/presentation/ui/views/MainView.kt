package study.snacktrackmobile.presentation.ui.views

import android.app.Activity
import androidx.activity.compose.BackHandler
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavController

@Composable
fun MainView(navController: NavController)
{
    val activity = LocalContext.current as Activity

    BackHandler {
        activity.finish()
    }
}