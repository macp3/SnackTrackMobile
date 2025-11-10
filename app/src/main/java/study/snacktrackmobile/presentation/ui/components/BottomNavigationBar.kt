package study.snacktrackmobile.presentation.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Icon
import androidx.compose.material3.Text

import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Book

import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import study.snacktrackmobile.presentation.ui.views.montserratFont // Upewnij się, że ta import jest poprawny

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
        tonalElevation = 0.dp // nie ma podnoszenia całej navbara
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
                            .background(
                                color = if (isSelected) selectedGreen else Color.Transparent,
                                shape = RoundedCornerShape(4.dp) // lekko zaokrąglony prostokąt
                            )
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Icon(
                            imageVector = item.icon,
                            contentDescription = item.label,
                            tint = if (isSelected) Color.White else Color.Black
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = item.label,
                            fontFamily = montserratFont,
                            fontSize = 11.sp,
                            color = if (isSelected) Color.White else Color.Black
                        )
                    }
                },
                label = { },
                alwaysShowLabel = false,
                colors = NavigationBarItemDefaults.colors(
                    // całkowicie wyłączamy Materialowy indicator
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


data class BottomNavItem(val label: String, val icon: ImageVector)