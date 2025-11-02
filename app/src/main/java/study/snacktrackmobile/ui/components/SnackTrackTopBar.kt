package study.snacktrackmobile.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import study.snacktrackmobile.R

@Composable
fun SnackTrackTopBar() {
    val montserratFont = FontFamily(
        Font(R.font.montserrat, weight = FontWeight.Normal)
    )
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFFBFFF99))
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "SnackTrack",
            fontFamily = montserratFont,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Black
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "Track your progress",
            fontFamily = montserratFont,
            fontSize = 14.sp,
            color = Color.Black
        )
    }
}
