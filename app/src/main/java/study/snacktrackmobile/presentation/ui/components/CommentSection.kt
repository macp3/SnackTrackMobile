package study.snacktrackmobile.presentation.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import study.snacktrackmobile.data.model.dto.CommentResponse
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

    // Dialogi
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

        // Lista komentarzy
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            comments.forEach { comment ->
                CommentItem(
                    comment = comment,
                    isAuthor = (currentUserId != null && currentUserId == comment.authorId),
                    onDelete = { viewModel.deleteComment(context, comment.mealId) },
                    onEdit = { showEditDialog = comment },
                    onReport = { showReportDialog = comment },
                    onLike = { viewModel.toggleLike(context, comment.id) } // 🔹 Podpięcie lajkowania
                )
            }

            if (comments.isEmpty()) {
                Text("No comments yet. Be the first!", color = Color.Gray, fontSize = 14.sp)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Input do dodawania
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = newCommentText,
                onValueChange = { newCommentText = it },
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

    // --- DIALOG ZGŁASZANIA ---
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

    // --- DIALOG EDYCJI ---
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
    onLike: () -> Unit // 🔹 Nowy callback
) {
    var showMenu by remember { mutableStateOf(false) }

    Card(
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF9F9F9)),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            // Header (User + Menu)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Text(
                    text = "User #${comment.authorId}",
                    style = MaterialTheme.typography.labelMedium,
                    color = Color.Gray,
                    fontWeight = FontWeight.Bold
                )

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

            Spacer(modifier = Modifier.height(4.dp))

            // Treść komentarza
            comment.content?.let {
                Text(
                    text = it,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Black
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // 🔹 SEKCJA LAJKÓW
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .clickable { onLike() } // Klikalne całe serduszko z liczbą
                    .padding(4.dp) // Trochę paddingu dla łatwiejszego kliknięcia
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