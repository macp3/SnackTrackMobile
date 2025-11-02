package study.snacktrackmobile.ui.views

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.*
import androidx.compose.ui.text.font.*
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import study.snacktrackmobile.R

@Composable
fun LoginScreen(
    onLoginClick: () -> Unit,
    onRegisterClick: () -> Unit,
    onGoogleSignInClick: () -> Unit
) {
    val montserratFont = FontFamily(
        Font(R.font.montserrat, weight = FontWeight.Normal)
    )
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Color.White
    ) {
        Box(modifier = Modifier.fillMaxSize()) {

            Text(
                text = "WELCOME",
                fontSize = 26.sp,
                fontFamily = montserratFont,
                modifier = Modifier.align(Alignment.Center)
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 60.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                Button(
                    onClick = { },
                    modifier = Modifier
                        .fillMaxWidth(0.6f)
                        .padding(vertical = 6.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xffB7F999)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(text = "Login", fontSize = 18.sp, fontFamily = montserratFont, color = Color.Black)
                }

                Button(
                    onClick = { },
                    modifier = Modifier
                        .fillMaxWidth(0.6f)
                        .padding(vertical = 6.dp)
                        .border(width = 1.dp, color = Color.Black, shape = RoundedCornerShape(11.dp)),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xffB7F999)),
                    shape = RoundedCornerShape(12.dp),
                ) {
                    Text(text = "Register", fontSize = 18.sp, fontFamily = montserratFont, color = Color.Black)
                }

                Spacer(modifier = Modifier.height(25.dp))

                Button(
                    onClick = { },
                    shape = CircleShape,
                    colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                    modifier = Modifier.size(55.dp)
                ) {
                    Text(text = "G", fontSize = 20.sp, fontFamily = montserratFont,)
                }
            }
        }
    }
}


