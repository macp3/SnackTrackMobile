package study.snacktrackmobile.presentation.ui.views

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.res.painterResource


val montserratFont = FontFamily(
    Font(R.font.montserrat, weight = FontWeight.Normal)
)

@Composable
fun StartView(navController: NavController) {
    var visible by remember { mutableStateOf(false) }

    val offsetY by animateDpAsState(
        targetValue = if (visible) 0.dp else 60.dp,
        animationSpec = tween(durationMillis = 800, easing = FastOutSlowInEasing),
        label = "offsetY"
    )

    val alpha by animateFloatAsState(
        targetValue = if (visible) 1f else 0f,
        animationSpec = tween(durationMillis = 900, easing = LinearOutSlowInEasing),
        label = "alpha"
    )

    LaunchedEffect(Unit) { visible = true }

    Surface(color = Color.White, modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            SnackTrackTopBar()

            Spacer(modifier = Modifier.height(32.dp))

            // GÓRNA CZĘŚĆ
            Column(
                modifier = Modifier
                    .weight(1f)
                    .offset(y = offsetY)
                    .alpha(alpha),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Image(
                    painter = painterResource(id = R.drawable.logo_vector),
                    contentDescription = "App logo",
                    modifier = Modifier
                        .fillMaxWidth(0.35f)
                        .aspectRatio(1f)
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "WELCOME",
                    fontSize = 32.sp,
                    fontFamily = montserratFont
                )
            }

            // DOLNE PRZYCISKI — ZAWSZE NA EKRANIE
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 40.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                DisplayButton(
                    text = "Login",
                    onClick = { navController.navigate("LoginView") },
                    modifier = Modifier.fillMaxWidth(0.5f)
                )

                Spacer(modifier = Modifier.height(16.dp))

                DisplayButton(
                    text = "Register",
                    onClick = { navController.navigate("RegisterView") },
                    modifier = Modifier.fillMaxWidth(0.5f)
                )

                Spacer(modifier = Modifier.height(25.dp))

                OutlinedButton(
                    onClick = { /* Google */ },
                    shape = CircleShape,
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                    modifier = Modifier.size(50.dp),
                    border = BorderStroke(1.dp, Color.Black),
                    contentPadding = PaddingValues(6.dp)
                ) {
                    Image(
                        painter = painterResource(R.drawable.google_icon),
                        contentDescription = "Google icon",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                }
            }
        }
    }
}
