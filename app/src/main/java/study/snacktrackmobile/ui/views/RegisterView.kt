package study.snacktrackmobile.ui.views

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import study.snacktrackmobile.ui.components.montserratFont

@Composable
fun RegisterView(navController: NavController)
{
    Text(
        text = "login view",
        fontSize = 18.sp,
        fontFamily = montserratFont,
        color = Color.Black,
    )
}