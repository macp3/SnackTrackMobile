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

@Composable
fun SummaryBar() {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(70.dp),
        shadowElevation = 10.dp,
        color = Color(0xFFE7FADA)
    ) {
        Row(
            modifier = Modifier.fillMaxSize(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            SummaryInfo("kcal", SummaryBarState.totalKcal.toString())
            SummaryInfo("P", "${SummaryBarState.totalProtein.toInt()}g")
            SummaryInfo("F", "${SummaryBarState.totalFat.toInt()}g")
            SummaryInfo("C", "${SummaryBarState.totalCarbs.toInt()}g")
        }
    }
}

@Composable
fun SummaryInfo(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(label, style = MaterialTheme.typography.labelSmall)
        Text(value, style = MaterialTheme.typography.titleMedium)
    }
}