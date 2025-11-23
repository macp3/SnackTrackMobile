package study.snacktrackmobile.presentation.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp


// ----------------------------------------------------------------------
// WIDOK GŁÓWNY (Wrapper dla nawigacji)
// ----------------------------------------------------------------------

/**
 * Pełny ekran 'O nas', używany do nawigacji (np. z szuflady DrawerContent).
 * Zawiera własny Scaffold i TopBar z przyciskiem powrotu.
 */

@Composable
fun AboutUsScreen(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp, vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        // Duży nagłówek
        Text(
            text = "Track your snacks, achieve your goals.",
            style = MaterialTheme.typography.headlineMedium,
            fontFamily = montserratFont,
            fontSize = 28.sp,
            textAlign = TextAlign.Center,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 24.dp)
        )

        // Karta z misją
        MissionCard(
            title = "Our Mission",
            content = "We believe that informed choices lead to sustainable health changes. SnackTrack is designed to provide you with the most accurate and easily accessible nutritional data, empowering you to take full control of your diet and fitness journey."
        )

        Spacer(modifier = Modifier.height(20.dp))

        // Karta z technologią
        MissionCard(
            title = "Powered by Science",
            content = "Our system utilizes advanced algorithms to calculate your personalized caloric and macronutrient needs, ensuring you receive recommendations tailored specifically to your body parameters, activity level, and weight goals."
        )

        Spacer(modifier = Modifier.height(20.dp))

        // Karta z danymi
        MissionCard(
            title = "Data Sources",
            content = "SnackTrack uses a comprehensive database of food products, combining public, proprietary, and user-generated data (essential foods) to provide a wide range of tracking options."
        )

        Spacer(modifier = Modifier.height(30.dp))

        // Stopka
        Text(
            text = "Version 1.0. Developed by the SnackTrack Team.",
            style = MaterialTheme.typography.bodySmall,
            color = Color.Gray,
            fontFamily = montserratFont,
            modifier = Modifier.padding(vertical = 8.dp)
        )
    }
}

// ----------------------------------------------------------------------
// WSPÓLNA KARTA MISJI
// ----------------------------------------------------------------------

@Composable
fun MissionCard(title: String, content: String) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                fontFamily = montserratFont,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            Text(
                text = content,
                style = MaterialTheme.typography.bodyMedium,
                fontFamily = montserratFont,
                textAlign = TextAlign.Justify
            )
        }
    }
}