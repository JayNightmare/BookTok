package com.example.booktok.view.screens

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.booktok.model.Book
import com.example.booktok.ui.components.BookForm
import com.example.booktok.viewmodel.BookViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookEditScreen(
    bookId: Long? = null,
    viewModel: BookViewModel,
    onBackClick: () -> Unit
) {
    val book by viewModel.currentBook.collectAsState()
    var showError by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
        onResult = { uri: Uri? ->
            uri?.let { selectedUri ->
                // Persist permission if needed
                context.contentResolver.takePersistableUriPermission(
                    selectedUri,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
                // Convert the URI to a ByteArray
                val byteArray = uriToByteArray(context, selectedUri)
                byteArray?.let { data ->
                    book?.let { currentBook ->
                        viewModel.updateCurrentBook(currentBook.copy(coverImage = data))
                    }
                }
            }
        }
    )

    LaunchedEffect(bookId) {
        if (bookId == null) {
            viewModel.updateCurrentBook(Book(title = "", author = ""))
        } else {
            viewModel.loadBook(bookId)
        }
    }

    // Determine if editing based on bookId and loaded book data
    val isEditing = bookId

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
                }
            )
        },
        floatingActionButton = {
            Column {
                FloatingActionButton(onClick = {
                    if (book?.title?.isNotEmpty() == true &&
                        book?.author?.isNotEmpty() == true &&
                        (book?.totalPages ?: 0) > 0
                    ) {
                        if (viewModel.isBookUnique(book!!)) {
                            book?.let {
                                if (bookId == null) viewModel.addBook(it)
                                else viewModel.updateBook(it)
                            }
                            onBackClick()
                        } else {
                            Toast.makeText(context, "A book with this title and author already exists", Toast.LENGTH_LONG).show()
                        }
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
                showError = showError,
                onSelectImage = { imagePickerLauncher.launch("image/*") },
                isEditing = isEditing
            )
        }
    }
}