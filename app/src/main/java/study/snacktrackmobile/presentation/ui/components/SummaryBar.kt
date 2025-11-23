package study.snacktrackmobile.presentation.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import study.snacktrackmobile.presentation.ui.state.SummaryBarState
import study.snacktrackmobile.presentation.ui.views.montserratFont

@Composable
fun SummaryBar() {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(70.dp),
        shadowElevation = 8.dp,
        color = Color(0xFFDCFFCC)
    ) {
        Row(
            modifier = Modifier.fillMaxSize(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            SummaryInfo(
                label = "kcal",
                value = String.format(
                    "%.0f/%.0f",
                    SummaryBarState.totalKcal,
                    SummaryBarState.limitKcal
                )
            )
            SummaryInfo(
                label = "Protein",
                value = String.format(
                    "%.1f/%.1fg",
                    SummaryBarState.totalProtein,
                    SummaryBarState.limitProtein
                )
            )

            SummaryInfo(
                label = "Fat",
                value = String.format(
                    "%.1f/%.1fg",
                    SummaryBarState.totalFat,
                    SummaryBarState.limitFat
                )
            )

            SummaryInfo(
                label = "Carbs",
                value = String.format(
                    "%.1f/%.1fg",
                    SummaryBarState.totalCarbs,
                    SummaryBarState.limitCarbs
                )
            )
        }
    }
}


@Composable
fun SummaryInfo(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            fontFamily = montserratFont,
            color = Color.Gray
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            fontFamily = montserratFont,
            color = Color(0xFF2E7D32), // ciemnozielony dla wartości
        )
    }
}
