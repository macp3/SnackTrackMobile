package study.snacktrackmobile.presentation.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import study.snacktrackmobile.data.model.dto.CommentResponse
import study.snacktrackmobile.data.network.ApiConfig
import study.snacktrackmobile.presentation.ui.views.montserratFont
import study.snacktrackmobile.viewmodel.CommentViewModel

@Composable
fun CommentSection(
    mealId: Int,
    viewModel: CommentViewModel,
    modifier: Modifier = Modifier
) {
    val comments by viewModel.comments.collectAsState()
    val currentUserId by viewModel.currentUserId.collectAsState()
    val context = LocalContext.current

    var newCommentText by remember { mutableStateOf("") }

    // Dialogs
    var showReportDialog by remember { mutableStateOf<CommentResponse?>(null) }
    var showEditDialog by remember { mutableStateOf<CommentResponse?>(null) }

    LaunchedEffect(mealId) {
        viewModel.loadComments(context, mealId)
    }

    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            "Comments (${comments.size})",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            fontFamily = montserratFont,
            modifier = Modifier.padding(vertical = 8.dp)
        )

        // Comment List
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            comments.forEach { comment ->
                CommentItem(
                    comment = comment,
                    isAuthor = (currentUserId != null && currentUserId == comment.authorId),
                    onDelete = { viewModel.deleteComment(context, comment.mealId) },
                    onEdit = { showEditDialog = comment },
                    onReport = { showReportDialog = comment },
                    // Hooking up the like action to the ViewModel
                    onLike = { viewModel.toggleLike(context, comment.id) }
                )
            }

            if (comments.isEmpty()) {
                Text("No comments yet. Be the first!", color = Color.Gray, fontSize = 14.sp)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Add Comment Input
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = newCommentText,
                onValueChange = {
                    if (it.length <= 255) newCommentText = it // 🔹 LIMIT ZNAKÓW
                },
                placeholder = { Text("Add a comment...") },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(24.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFF2E7D32),
                    cursorColor = Color(0xFF2E7D32)
                )
            )
            IconButton(
                onClick = {
                    if (newCommentText.isNotBlank()) {
                        viewModel.addComment(context, mealId, newCommentText)
                        newCommentText = ""
                    }
                },
                enabled = newCommentText.isNotBlank()
            ) {
                Icon(Icons.Default.Send, contentDescription = "Send", tint = Color(0xFF2E7D32))
            }
        }
    }

    // --- REPORT DIALOG ---
    if (showReportDialog != null) {
        var reason by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { showReportDialog = null },
            title = { Text("Report Comment") },
            text = {
                Column {
                    Text("Why are you reporting this comment?")
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = reason,
                        onValueChange = { reason = it },
                        label = { Text("Reason") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.reportComment(context, showReportDialog!!.id, reason)
                    showReportDialog = null
                }) { Text("Report", color = Color.Red) }
            },
            dismissButton = {
                TextButton(onClick = { showReportDialog = null }) { Text("Cancel") }
            }
        )
    }

    // --- EDIT DIALOG ---
    if (showEditDialog != null) {
        var editText by remember { mutableStateOf(showEditDialog!!.content ?: "") }
        AlertDialog(
            onDismissRequest = { showEditDialog = null },
            title = { Text("Edit Comment") },
            text = {
                OutlinedTextField(
                    value = editText,
                    onValueChange = { editText = it },
                    label = { Text("Content") },
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    if (editText.isNotBlank()) {
                        viewModel.editComment(context, showEditDialog!!.mealId, editText)
                        showEditDialog = null
                    }
                }) { Text("Save") }
            },
            dismissButton = {
                TextButton(onClick = { showEditDialog = null }) { Text("Cancel") }
            }
        )
    }
}

@Composable
fun CommentItem(
    comment: CommentResponse,
    isAuthor: Boolean,
    onDelete: () -> Unit,
    onEdit: () -> Unit,
    onReport: () -> Unit,
    onLike: () -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }

    Card(
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF9F9F9)),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(12.dp)) {

            // --- HEADER (Avatar + Imię + Menu) ---
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // LEWA STRONA: Avatar + Imię
                Row(verticalAlignment = Alignment.CenterVertically) {
                    val imageUrl = comment.authorImageUrl
                    val fullUrl = if (!imageUrl.isNullOrBlank()) {
                        if (imageUrl.startsWith("http")) imageUrl
                        else "${ApiConfig.BASE_URL.removeSuffix("/")}/${imageUrl.removePrefix("/")}"
                    } else {
                        "${ApiConfig.BASE_URL}/images/profiles/default_profile_picture.png"
                    }

                    AsyncImage(
                        model = fullUrl,
                        contentDescription = "Avatar",
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(Color.LightGray),
                        contentScale = ContentScale.Crop
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    Text(
                        text = comment.authorName,
                        style = MaterialTheme.typography.labelMedium,
                        color = Color.Black,
                        fontWeight = FontWeight.Bold
                    )
                }

                // PRAWA STRONA: Menu
                Box {
                    IconButton(
                        onClick = { showMenu = true },
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(Icons.Default.MoreVert, contentDescription = "Options", tint = Color.Gray)
                    }

                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false }
                    ) {
                        if (isAuthor) {
                            DropdownMenuItem(
                                text = { Text("Edit") },
                                onClick = { showMenu = false; onEdit() }
                            )
                            DropdownMenuItem(
                                text = { Text("Delete", color = Color.Red) },
                                onClick = { showMenu = false; onDelete() }
                            )
                        } else {
                            DropdownMenuItem(
                                text = { Text("Report") },
                                onClick = { showMenu = false; onReport() }
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // --- CONTENT (Treść komentarza) ---
            comment.content?.let {
                Text(
                    text = it,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Black,
                    maxLines = 5,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // --- LIKE SECTION ---
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .clickable { onLike() }
                    .padding(4.dp)
            ) {
                Icon(
                    imageVector = if (comment.isLiked) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                    contentDescription = "Like",
                    tint = if (comment.isLiked) Color.Red else Color.Gray,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "${comment.likesCount}",
                    style = MaterialTheme.typography.labelMedium,
                    color = if (comment.isLiked) Color.Red else Color.Gray
                )
            }
        }
    }
}