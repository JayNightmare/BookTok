package com.example.booktok.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.booktok.data.Book
import com.example.booktok.ui.viewmodels.BookViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookEditScreen(
    bookId: Long? = null,
    viewModel: BookViewModel,
    onBackClick: () -> Unit
) {
    val book by viewModel.currentBook.collectAsState()
    var showError by remember { mutableStateOf(false) }

    LaunchedEffect(bookId) {
        if (bookId != null) {
            viewModel.loadBook(bookId)
        }
    }


    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(if (bookId == null) "Add Book" else "Edit Book") },
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
                        if (book?.title?.isNotEmpty() == true &&
                            book?.author?.isNotEmpty() == true &&
                            (book?.totalPages ?: 0) > 0
                        ) {
                            book?.let {
                                if (bookId == null) viewModel.addBook(it)
                                else viewModel.updateBook(it)
                            }
                            onBackClick()
                        } else {
                            showError = true
                        }
                    }) {
                        Icon(
                            imageVector = Icons.Filled.Save,
                            contentDescription = "Save"
                        )
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
            // Use your form and pass callbacks to the viewModel
            BookForm(
                book = book ?: Book(title = "", author = ""),
                onBookChange = { updatedBook ->
                    viewModel.updateCurrentBook(updatedBook)
                },
                showError = showError
            )
        }
    }
}

@Composable
private fun BookForm(
    book: Book,
    onBookChange: (Book) -> Unit,
    modifier: Modifier = Modifier,
    showError: Boolean
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        if (showError) {
            Text(
                text = "Please fill all required fields correctly",
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
        }

        OutlinedTextField(
            value = book.title,
            onValueChange = { onBookChange(book.copy(title = it)) },
            label = { Text("Title *") },
            modifier = Modifier.fillMaxWidth(),
            isError = showError && book.title.isEmpty()
        )
        
        OutlinedTextField(
            value = book.author,
            onValueChange = { onBookChange(book.copy(author = it)) },
            label = { Text("Author *") },
            modifier = Modifier.fillMaxWidth(),
            isError = showError && book.author.isEmpty()
        )
        
        OutlinedTextField(
            value = book.genre ?: "",
            onValueChange = { onBookChange(book.copy(genre = it.ifEmpty { null })) },
            label = { Text("Genre (optional)") },
            modifier = Modifier.fillMaxWidth()
        )
        
        OutlinedTextField(
            value = book.totalPages.toString(),
            onValueChange = { value ->
                val pages = value.toIntOrNull() ?: 0
                onBookChange(book.copy(totalPages = pages))
            },
            label = { Text("Total Pages *") },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions.Default.copy(
                keyboardType = KeyboardType.Number
            ),
            isError = showError && (book.totalPages <= 0)
        )
        
        OutlinedTextField(
            value = book.pagesRead.toString(),
            onValueChange = { value ->
                val pages = value.toIntOrNull() ?: 0
                onBookChange(book.copy(pagesRead = pages))
            },
            label = { Text("Pages Read") },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions.Default.copy(
                keyboardType = KeyboardType.Number
            )
        )
    }
}
