package study.snacktrackmobile.presentation.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import study.snacktrackmobile.presentation.ui.state.SummaryBarState
import study.snacktrackmobile.presentation.ui.views.montserratFont
import kotlin.math.min

@Composable
fun SummaryBar(
    modifier: Modifier = Modifier
) {
    // 🔹 BEZPIECZNE TŁO ("Flat Style")
    // Zamiast cienia (shadow), używamy tła o wysokiej nieprzezroczystości i ramki.
    // To zapewnia czytelność nad przewijaną listą, ale nie psuje renderowania na telefonach.

    val shape = RoundedCornerShape(24.dp) // Mocno zaokrąglone rogi (pigułka)
    val backgroundColor = Color(0xB3FFFFFF).copy(alpha = 0.95f) // Lekko przeźroczysta zieleń
    val borderColor = Color(0xFFC5E1A5) // Ciemniejsza obwódka dla kontrastu

    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 8.dp) // Margines od krawędzi ekranu
            .background(color = backgroundColor, shape = shape) // Tło
            .border(width = 1.dp, color = borderColor, shape = shape) // Ramka zamiast cienia
            .padding(vertical = 10.dp) // Wewnętrzny odstęp
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            val itemModifier = Modifier.weight(1f)

            NutrientIndicator(
                label = "Kcal",
                current = SummaryBarState.totalKcal.toFloat(),
                limit = SummaryBarState.limitKcal.toFloat(),
                unit = "",
                modifier = itemModifier
            )

            NutrientIndicator(
                label = "Protein",
                current = SummaryBarState.totalProtein.toFloat(),
                limit = SummaryBarState.limitProtein.toFloat(),
                unit = "g",
                modifier = itemModifier
            )

            NutrientIndicator(
                label = "Fat",
                current = SummaryBarState.totalFat.toFloat(),
                limit = SummaryBarState.limitFat.toFloat(),
                unit = "g",
                modifier = itemModifier
            )

            NutrientIndicator(
                label = "Carbs",
                current = SummaryBarState.totalCarbs.toFloat(),
                limit = SummaryBarState.limitCarbs.toFloat(),
                unit = "g",
                modifier = itemModifier
            )
        }
    }
}

@Composable
fun NutrientIndicator(
    label: String,
    current: Float,
    limit: Float,
    unit: String,
    modifier: Modifier = Modifier,
    size: Dp = 55.dp, // Lekko mniejsze, żeby pasowało do paska
    strokeWidth: Dp = 5.dp
) {
    val progress = if (limit > 0) current / limit else 0f
    val isOverLimit = progress > 1f

    val baseColor = Color(0xFF2E7D32)
    val exceedColor = Color(0xFFD32F2F)
    val trackColor = Color(0xFFE0E0E0)

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
    ) {
        Box(contentAlignment = Alignment.Center) {
            Canvas(modifier = Modifier.size(size)) {
                // Tło paska (szare kółko)
                drawArc(
                    color = trackColor,
                    startAngle = 0f,
                    sweepAngle = 360f,
                    useCenter = false,
                    style = Stroke(width = strokeWidth.toPx(), cap = StrokeCap.Round)
                )

                // Progres
                val sweepAngle = if (isOverLimit) 360f else (progress * 360f)
                drawArc(
                    color = baseColor,
                    startAngle = -90f,
                    sweepAngle = sweepAngle,
                    useCenter = false,
                    style = Stroke(width = strokeWidth.toPx(), cap = StrokeCap.Round)
                )

                // Nadwyżka
                if (isOverLimit) {
                    val excessProgress = progress - 1f
                    val excessSweep = min(excessProgress * 360f, 360f)
                    drawArc(
                        color = exceedColor,
                        startAngle = -90f,
                        sweepAngle = excessSweep,
                        useCenter = false,
                        style = Stroke(width = strokeWidth.toPx(), cap = StrokeCap.Round)
                    )
                }
            }

            // Tekst w środku
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = String.format("%.0f", current),
                    style = MaterialTheme.typography.labelLarge,
                    fontFamily = montserratFont,
                    fontWeight = FontWeight.Bold,
                    color = if (isOverLimit) exceedColor else Color.Black,
                    fontSize = 11.sp
                )
                Surface(
                    modifier = Modifier.width(14.dp).height(1.dp),
                    color = Color.Gray
                ) {}
                Text(
                    text = String.format("%.0f", limit),
                    style = MaterialTheme.typography.labelSmall,
                    fontFamily = montserratFont,
                    color = Color.Gray,
                    fontSize = 9.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        // Etykieta pod kółkiem
        Text(
            text = "$label${if(unit.isNotEmpty()) " ($unit)" else ""}",
            style = MaterialTheme.typography.labelMedium,
            fontFamily = montserratFont,
            color = Color.Black, // Czarny tekst na jasnym tle paska
            fontWeight = FontWeight.Medium,
            fontSize = 11.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}