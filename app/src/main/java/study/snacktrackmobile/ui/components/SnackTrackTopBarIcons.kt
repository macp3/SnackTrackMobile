package study.snacktrackmobile.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.*
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import study.snacktrackmobile.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SnackTrackTopBarWithIcons() {
    val montserratFont = FontFamily(Font(R.font.montserrat, weight = FontWeight.Normal))

    // Stany drawerów
    val leftDrawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    var rightDrawerOpen by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    ModalNavigationDrawer(
        drawerState = leftDrawerState,
        drawerContent = {
            Column(
                modifier = Modifier
                    .fillMaxHeight()
                    .width(280.dp)
                    .background(Color.White)
                    .padding(vertical = 24.dp, horizontal = 16.dp)
            ) {
                Text(
                    text = "Menu",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color(0xFF4CAF50),
                    modifier = Modifier.padding(bottom = 24.dp)
                )

                @Composable
                fun renderOption(title: String, action: () -> Unit) {
                    Text(
                        text = title,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF333333),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 12.dp)
                            .clickable {
                                action()
                                scope.launch { leftDrawerState.close() }
                            }
                    )
                }

                // Przykładowe opcje
                renderOption("Home") { println("Nawigacja do Home") }
                renderOption("Profile") { println("Nawigacja do Profile") }
                renderOption("Progress") { println("Wyświetlanie progresu") }
                renderOption("Settings") { println("Otwieranie ustawień") }
            }
        },
        content = {
            // Główny ekran z TopBar
            Box(modifier = Modifier.fillMaxSize()) {

                // TopBar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFFBFFF99))
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // Lewy hamburger
                    Icon(
                        imageVector = Icons.Filled.Menu,
                        contentDescription = "Menu",
                        modifier = Modifier
                            .size(28.dp)
                            .clickable { scope.launch { leftDrawerState.open() } },
                        tint = Color.Black
                    )

                    // Tytuł w centrum
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "SnackTrack",
                            fontFamily = montserratFont,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.Black
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Track your progress",
                            fontFamily = montserratFont,
                            fontSize = 14.sp,
                            color = Color.Black
                        )
                    }

                    // Prawy drawer (powiadomienia)
                    Icon(
                        imageVector = Icons.Filled.Notifications,
                        contentDescription = "Notifications",
                        modifier = Modifier
                            .size(28.dp)
                            .clickable { rightDrawerOpen = true },
                        tint = Color.Black
                    )
                }

                // Prawy drawer wysuwany z prawej
                RightDrawer(drawerState = rightDrawerOpen, onClose = { rightDrawerOpen = false }) {
                    Text(
                        text = "Notifications",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color(0xFF4CAF50),
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                    HorizontalDivider(color = Color.LightGray, thickness = 1.dp)
                    Spacer(modifier = Modifier.height(12.dp))

                    // Przykładowe powiadomienia
                    repeat(10) { index ->
                        Text(
                            text = "Powiadomienie #${index + 1}",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color.Black,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp)
                        )
                    }
                }
            }
        }
    )
}

@Composable
fun RightDrawer(
    drawerState: Boolean,
    onClose: () -> Unit,
    content: @Composable ColumnScope.() -> Unit
) {
    Box(modifier = Modifier.fillMaxSize()) {
        if (drawerState) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0x80000000))
                    .clickable { onClose() }
            )
        }

        AnimatedVisibility(
            visible = drawerState,
            modifier = Modifier.align(Alignment.CenterEnd)
        ) {
            Column(
                modifier = Modifier
                    .width(280.dp)
                    .fillMaxHeight()
                    .background(Color.White)
                    .padding(16.dp),
                content = content
            )
        }
    }
}
