package com.example.booktok.view.screens

import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.booktok.ui.components.ProgressTracker
import com.example.booktok.viewmodel.BookViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookDetailScreen(
    bookId: Long,
    onBackClick: () -> Unit,
    viewModel: BookViewModel,
) {
    val book by viewModel.getBookById(bookId).collectAsState(initial = null)

    LaunchedEffect(bookId) {
        viewModel.loadBook(bookId)
    }

    val context = LocalContext.current
    var isEditing by remember { mutableStateOf(false) }
    var editedTitle by remember { mutableStateOf("") }
    var editedAuthor by remember { mutableStateOf("") }
    var editedGenre by remember { mutableStateOf("") }
    var editedTotalPages by remember { mutableStateOf("") }
    var showDeleteDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(book?.title ?: "Book Details") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                actions = {
                    IconButton(onClick = {
                        book?.let { viewModel.shareBookSummary(context, it) }
                    }) {
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
                .padding(16.dp)
        ) {
            book?.let { book ->
                if (isEditing) {
                    OutlinedTextField(
                        value = editedTitle,
                        onValueChange = { editedTitle = it },
                        label = { Text("Title") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = editedAuthor,
                        onValueChange = { editedAuthor = it },
                        label = { Text("Author") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = editedGenre,
                        onValueChange = { editedGenre = it },
                        label = { Text("Genre") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = editedTotalPages,
                        onValueChange = { editedTotalPages = it },
                        label = { Text("Total Pages") },
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions.Default.copy(
                            keyboardType = KeyboardType.Number
                        )
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = {
                            val updatedBook = book.copy(
                                title = editedTitle,
                                author = editedAuthor,
                                genre = editedGenre.ifEmpty { null },
                                totalPages = editedTotalPages.toIntOrNull() ?: book.totalPages
                            )
                            viewModel.updateBook(updatedBook)
                            isEditing = false
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Save Changes")
                    }
                } else {
                    Text(
                        text = "Title: ${book.title}",
                        style = MaterialTheme.typography.titleLarge
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Author: ${book.author}",
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

                    ProgressTracker(
                        pagesRead = book.pagesRead,
                        totalPages = book.totalPages,
                        onProgressChange = { pages ->
                            viewModel.updateBook(book.copy(pagesRead = pages))
                        }
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        // Edit Button with Icon
                        IconButton(
                            onClick = {
                                editedTitle = book.title
                                editedAuthor = book.author
                                editedGenre = book.genre ?: ""
                                editedTotalPages = book.totalPages.toString()
                                isEditing = true
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = "Edit Book",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }

                        // Delete Button with Icon
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
    }

    // Delete Confirmation Dialog
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
                            onBackClick()  // Navigate back after deletion
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
}
