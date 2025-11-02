package study.snacktrackmobile.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import study.snacktrackmobile.R


    val montserratFont = FontFamily(
        Font(R.font.montserrat, weight = FontWeight.Normal)
    )

    @Composable
    fun DisplayButton(text: String,
                      onClick: () -> Unit,
                      modifier: Modifier = Modifier,
                      )
    {
        OutlinedButton(
            onClick = onClick,
            modifier = Modifier
                .fillMaxWidth(0.6f)
                .shadow(
                    elevation = 8.dp,
                    shape = RoundedCornerShape(16.dp),
                    clip = false
                ),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFB7F999)),
            shape = RoundedCornerShape(12.dp),
            border = BorderStroke(1.dp, Color.Black),
        ) {
            Text(
                text = text,
                fontSize = 18.sp,
                fontFamily = montserratFont,
                color = Color.Black
            )
        }
    }
