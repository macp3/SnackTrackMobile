package study.snacktrackmobile.presentation.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
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
fun SummaryBar() {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight() // 🔹 ZMIANA: Dopasuj wysokość do zawartości
            .padding(horizontal = 8.dp, vertical = 8.dp),
        shadowElevation = 6.dp,
        shape = RoundedCornerShape(16.dp),
        color = Color(0xFFF1F8E9)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp), // 🔹 ZMIANA: Większy oddech góra/dół
            horizontalArrangement = Arrangement.SpaceBetween, // Rozłożenie przestrzeni
            verticalAlignment = Alignment.Top // Wyrównanie do góry, żeby teksty na dole były w linii
        ) {
            // Używamy weight(1f) aby każdy element miał dokładnie 25% szerokości
            // Zapobiegnie to nachodzeniu na siebie
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
    modifier: Modifier = Modifier, // 🔹 Dodano modifier
    size: Dp = 60.dp, // 🔹 Lekko mniejsze kółko dla bezpieczeństwa
    strokeWidth: Dp = 5.dp
) {
    val progress = if (limit > 0) current / limit else 0f
    val isOverLimit = progress > 1f

    val baseColor = Color(0xFF2E7D32)
    val exceedColor = Color(0xFFD32F2F)
    val trackColor = Color(0xFFE0E0E0)

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier // 🔹 Użycie modifiera z wagi (weight)
    ) {
        Box(contentAlignment = Alignment.Center) {
            Canvas(modifier = Modifier.size(size)) {
                // Tło
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
                    fontSize = 12.sp // Lekko mniejsza czcionka
                )
                Surface(
                    modifier = Modifier.width(16.dp).height(1.dp),
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

        // Etykieta pod spodem
        Text(
            text = "$label${if(unit.isNotEmpty()) " ($unit)" else ""}",
            style = MaterialTheme.typography.labelMedium,
            fontFamily = montserratFont,
            color = Color.Black,
            fontWeight = FontWeight.Medium,
            fontSize = 11.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis // Utnij tekst jeśli się nie mieści w kolumnie
        )
    }
}