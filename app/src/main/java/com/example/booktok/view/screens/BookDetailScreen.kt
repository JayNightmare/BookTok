package com.example.booktok.view.screens

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import com.example.booktok.R
import com.example.booktok.ui.components.EmailInputDialog
import com.example.booktok.ui.components.ProgressTracker
import com.example.booktok.viewmodel.BookViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookDetailScreen(
    bookId: Long,
    onBackClick: () -> Unit,
    onEditClick: (Long) -> Unit,
    viewModel: BookViewModel,
) {
    val book by viewModel.getBookById(bookId).collectAsState(initial = null)

    LaunchedEffect(bookId) {
        viewModel.loadBook(bookId)
    }

    val context = LocalContext.current
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showEmailDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Book Details") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },

                // Share Button
                actions = {
                    IconButton(onClick = { showEmailDialog = true }) {
                        Icon(Icons.Default.Share, contentDescription = "Share Book Summary")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            book?.let { book ->
                val bitmap = remember(book.coverImage) {
                    decodeImage(book.coverImage)
                }

                if (bitmap != null) {
                    Image(
                        painter = remember { BitmapPainter(bitmap.asImageBitmap()) },
                        contentDescription = "Book Cover",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .size(150.dp)
                            .clip(RoundedCornerShape(12.dp))
                    )
                } else {
                    Image(
                        painter = rememberAsyncImagePainter(model = R.drawable.default_background),
                        contentDescription = "Default Cover",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .size(150.dp)
                            .clip(RoundedCornerShape(12.dp))
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = book.title,
                    style = MaterialTheme.typography.titleLarge,
                    fontSize = 24.sp
                )
                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "By ${book.author}",
                    style = MaterialTheme.typography.bodyLarge
                )
                Spacer(modifier = Modifier.height(8.dp))

                if (book.genre != null) {
                    Text(
                        text = "Genre: ${book.genre}",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }

                Text(
                    text = "Date Added: ${book.dateAdded}",
                    style = MaterialTheme.typography.bodySmall
                )
                Spacer(modifier = Modifier.height(16.dp))

                ProgressTracker(
                    pagesRead = book.pagesRead,
                    totalPages = book.totalPages,
                    onProgressChange = { pages ->
                        viewModel.updateBook(book.copy(pagesRead = pages))
                    },
                    keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number)
                )

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    IconButton(
                        onClick = { onEditClick(book.id) }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Edit Book",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }

                    IconButton(
                        onClick = { showDeleteDialog = true }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete Book",
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
        }
    }

    // Show the delete dialog
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Delete Book") },
            text = { Text("Are you sure you want to delete this book? This action cannot be undone.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        book?.let {
                            viewModel.deleteBook(it)
                            showDeleteDialog = false
                            onBackClick()
                        }
                    }
                ) {
                    Text("Delete", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Show the email dialog
    if (showEmailDialog) {
        EmailInputDialog(
            title = "Share Book Summary",
            onConfirm = { email ->
                book?.let {
                    viewModel.shareBookSummary(context, it, email)
                }
                showEmailDialog = false
            },
            selectedBook = book,
            onDismiss = { showEmailDialog = false }
        )
    }
}

fun decodeImage(data: ByteArray?): Bitmap? {
    return data?.takeIf { it.isNotEmpty() }?.let {
        BitmapFactory.decodeByteArray(it, 0, it.size)
    }
}
