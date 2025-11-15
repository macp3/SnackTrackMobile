package study.snacktrackmobile.presentation.ui.components

import android.net.Uri
import android.util.Log
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Icon
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import kotlinx.coroutines.launch
import study.snacktrackmobile.data.network.ApiConfig

@Composable
fun ProfileAvatar(
    imageUrl: String?,
    imagePicker: ManagedActivityResultLauncher<String, Uri?>
) {
    val scope = rememberCoroutineScope()
    val iconScale = remember { androidx.compose.animation.core.Animatable(1f) }

    val resolvedUrl = imageUrl
        ?: (ApiConfig.BASE_URL + "/images/profiles/default_profile_picture.png")
    Log.d("PROFILE_IMG_URL", "Resolved URL: $resolvedUrl")


    val painter = rememberAsyncImagePainter(resolvedUrl, onError = { Log.e("COIL_ERR", "Error loading image", it.result.throwable) })

    Box(
        modifier = Modifier.size(130.dp),
        contentAlignment = Alignment.Center
    ) {

        Box(
            modifier = Modifier
                .size(130.dp)
                .clip(CircleShape)
                .background(Color.White)
                .padding(5.dp),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painter,
                contentDescription = "Profile image",
                modifier = Modifier
                    .size(120.dp)
                    .clip(CircleShape),
                contentScale = ContentScale.Crop
            )
        }

        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .offset(x = 6.dp, y = 6.dp)
                .size(40.dp)
                .clip(CircleShape)
                .background(Color(0xFF2E7D32))
                .graphicsLayer {
                    scaleX = iconScale.value
                    scaleY = iconScale.value
                    shadowElevation = 10.dp.toPx()
                }
                .clickable {
                    scope.launch {
                        iconScale.animateTo(0.85f)
                        iconScale.animateTo(1f)
                    }
                    imagePicker.launch("image/*")
                },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = "Add Photo",
                tint = Color.White
            )
        }
    }
}
