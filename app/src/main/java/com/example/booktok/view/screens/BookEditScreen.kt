package com.example.booktok.view.screens

import android.icu.text.SimpleDateFormat
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.booktok.model.Book
import com.example.booktok.viewmodel.BookViewModel
import java.util.Calendar
import java.util.Locale

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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun BookForm(
    book: Book,
    onBookChange: (Book) -> Unit,
    modifier: Modifier = Modifier,
    showError: Boolean
) {
    val context = LocalContext.current
    val dateFormatter = remember { SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()) }

    // State to control DatePicker visibility
    var showDatePicker by remember { mutableStateOf(false) }

    // Separate calendar state to prevent recompositions
    val calendar = remember { Calendar.getInstance().apply { time = book.dateAdded } }

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {

        // If showError is true, display an error message
        if (showError) {
            Text(
                text = "Please fill all required fields correctly",
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
        }

        // Enter Title
        OutlinedTextField(
            value = book.title,
            onValueChange = { onBookChange(book.copy(title = it)) },
            label = { Text("Title *") },
            modifier = Modifier.fillMaxWidth(),
            isError = showError && book.title.isEmpty()
        )

        // Enter Author
        OutlinedTextField(
            value = book.author,
            onValueChange = { onBookChange(book.copy(author = it)) },
            label = { Text("Author *") },
            modifier = Modifier.fillMaxWidth(),
            isError = showError && book.author.isEmpty()
        )

        // Enter Genre (optional)
        OutlinedTextField(
            value = book.genre ?: "",
            onValueChange = { onBookChange(book.copy(genre = it.ifEmpty { null })) },
            label = { Text("Genre (optional)") },
            modifier = Modifier.fillMaxWidth()
        )

        // Enter Total Pages
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

        // Enter Pages Read
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

        // Enter Date added
        OutlinedTextField(
            value = dateFormatter.format(book.dateAdded),
            onValueChange = {},
            label = { Text("Date Added") },
            modifier = Modifier.fillMaxWidth(),
            readOnly = true,
            trailingIcon = {
                IconButton(onClick = { showDatePicker = true }) {
                    Icon(Icons.Default.DateRange, contentDescription = "Select Date")
                }
            }
        )

        // DatePickerDialog
        if (showDatePicker) {
            android.app.DatePickerDialog(
                context,
                { _, year, month, dayOfMonth ->
                    val selectedDate = Calendar.getInstance().apply {
                        set(year, month, dayOfMonth)
                    }.time
                    onBookChange(book.copy(dateAdded = selectedDate))
                    showDatePicker = false
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            ).show()
        }
    }
}