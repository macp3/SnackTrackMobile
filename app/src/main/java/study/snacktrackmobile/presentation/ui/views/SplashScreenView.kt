package study.snacktrackmobile.presentation.ui.views

import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.navigation.NavController
import kotlinx.coroutines.delay
import study.snacktrackmobile.R

@Composable
fun SplashScreenView(navController: NavController) {
    var visible by remember { mutableStateOf(false) }
    var fadeOut by remember { mutableStateOf(false) }

    // Animacja przezroczystości loga
    val alphaAnim by animateFloatAsState(
        targetValue = when {
            fadeOut -> 0f
            visible -> 1f
            else -> 0f
        },
        animationSpec = tween(durationMillis = 700, easing = FastOutSlowInEasing),
        label = "alphaAnim"
    )

    // Animacja skali loga (delikatny efekt powiększenia)
    val scaleAnim by animateFloatAsState(
        targetValue = when {
            fadeOut -> 0.85f
            visible -> 1f
            else -> 0.85f
        },
        animationSpec = tween(durationMillis = 700, easing = FastOutSlowInEasing),
        label = "scaleAnim"
    )

    // Animacja przezroczystości tła (fade-out)
    val backgroundAlpha by animateFloatAsState(
        targetValue = if (fadeOut) 0f else 1f,
        animationSpec = tween(durationMillis = 700, easing = LinearOutSlowInEasing),
        label = "backgroundAlpha"
    )

    LaunchedEffect(Unit) {
        visible = true
        delay(2200) // ile czasu logo ma pozostać widoczne
        fadeOut = true
        delay(1000) // czas animacji znikania
        navController.navigate("StartView") {
            popUpTo("SplashScreen") { inclusive = true }
        }
    }

    Surface(
        modifier = Modifier
            .fillMaxSize()
            .alpha(backgroundAlpha),
        color = Color.White
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.fillMaxSize()
        ) {
            Image(
                painter = painterResource(id = R.drawable.logo_vector),
                contentDescription = "App logo",
                modifier = Modifier
                    .fillMaxWidth(0.5f)
                    .aspectRatio(1f)
                    .graphicsLayer(
                        scaleX = scaleAnim,
                        scaleY = scaleAnim
                    )
                    .alpha(alphaAnim),
                contentScale = ContentScale.Fit
            )
        }
    }
}
