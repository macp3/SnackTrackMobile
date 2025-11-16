package study.snacktrackmobile.presentation.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import study.snacktrackmobile.presentation.ui.views.montserratFont

data class BottomNavItem(val label: String, val icon: ImageVector)

@Composable
fun BottomNavigationBar(
    selectedItem: String,
    onItemSelected: (String) -> Unit
) {
    val selectedGreen = Color(0xFF2E7D32)
    val items = listOf(
        BottomNavItem("Meals", Icons.Default.Restaurant),
        BottomNavItem("Training", Icons.Default.FitnessCenter),
        BottomNavItem("Recipes", Icons.Default.Book),
        BottomNavItem("Shopping", Icons.Default.ShoppingCart),
        BottomNavItem("Profile", Icons.Default.Person)
    )

    NavigationBar(
        containerColor = Color(0xFFBFFF99),
        contentColor = Color.Black,
        tonalElevation = 0.dp
    ) {
        items.forEach { item ->
            val isSelected = selectedItem == item.label
            NavigationBarItem(
                selected = isSelected,
                onClick = { onItemSelected(item.label) },
                icon = {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                        modifier = Modifier
                            .weight(1f) // każdy item zajmuje równą szerokość
                            .background(
                                color = if (isSelected) selectedGreen else Color.Transparent,
                                shape = RoundedCornerShape(4.dp)
                            )
                            .padding(horizontal = 4.dp, vertical = 4.dp)
                    ) {
                        Icon(
                            imageVector = item.icon,
                            contentDescription = item.label,
                            tint = if (isSelected) Color.White else Color.Black,
                            modifier = Modifier.size(20.dp) // ikony trochę mniejsze
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = item.label,
                            fontFamily = montserratFont,
                            fontSize = 11.sp,
                            color = if (isSelected) Color.White else Color.Black,
                            maxLines = 1,
                            softWrap = false,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                },
                label = {},
                alwaysShowLabel = false,
                colors = NavigationBarItemDefaults.colors(
                    indicatorColor = Color.Transparent,
                    selectedIconColor = Color.Unspecified,
                    selectedTextColor = Color.Unspecified,
                    unselectedIconColor = Color.Unspecified,
                    unselectedTextColor = Color.Unspecified
                )
            )
        }
    }
}
