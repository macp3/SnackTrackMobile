package study.snacktrackmobile.presentation.ui.views

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.*
import androidx.compose.ui.text.font.*
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import study.snacktrackmobile.R
import study.snacktrackmobile.presentation.ui.components.DisplayButton
import study.snacktrackmobile.presentation.ui.components.SnackTrackTopBar

val montserratFont = FontFamily(
    Font(R.font.montserrat, weight = FontWeight.Normal)
)

@Composable
fun StartView(navController: NavController) {
    Surface(color = Color.White) {
        Column(
            modifier = Modifier
                .fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // TopBar
            SnackTrackTopBar()

            Spacer(modifier = Modifier.height(300.dp))
            // Środkowy tekst
            Text(
                text = "WELCOME",
                fontSize = 35.sp,
                fontFamily = montserratFont
            )

            // Dolne przyciski
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 250.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                DisplayButton(text = "Login", onClick = {
                    navController.navigate("LoginView")
                }, modifier = Modifier.fillMaxWidth(0.5f))
                Spacer(modifier = Modifier.height(16.dp))
                DisplayButton(text = "Register", onClick = {
                    navController.navigate("RegisterView")
                }, modifier = Modifier.fillMaxWidth(0.5f))
                Spacer(modifier = Modifier.height(20.dp))

                OutlinedButton(
                    onClick = { /* TODO: Google Sign-In */ },
                    shape = CircleShape,
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                    modifier = Modifier.size(50.dp),
                    border = BorderStroke(1.dp, Color.Black),
                    contentPadding = PaddingValues(6.dp)
                ) {
                    Image(
                        painter = painterResource(R.drawable.google_icon),
                        contentDescription = "Google icon",
                        modifier = Modifier.fillMaxSize().clip(CircleShape),
                        contentScale = ContentScale.Crop
                    )
                }
            }
        }
    }
}
