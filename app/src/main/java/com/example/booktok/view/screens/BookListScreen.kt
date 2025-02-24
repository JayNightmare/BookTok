package com.example.booktok.view.screens

import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.booktok.ui.components.BackgroundWithContent
import com.example.booktok.ui.components.BookGrid
import com.example.booktok.ui.components.BookSearchBar
import com.example.booktok.ui.components.EmailInputDialog
import com.example.booktok.viewmodel.BookViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookListScreen(
    onBookClick: (Long) -> Unit,
    onAddBook: () -> Unit,
    viewModel: BookViewModel,
    backgroundImageUri: String? = null
) {
    val books by viewModel.searchBooks().collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val genres by viewModel.genres.collectAsState(initial = emptyList())
    val selectedGenre by viewModel.selectedGenre.collectAsState()
    val selectedProgress by viewModel.selectedProgress.collectAsState()
    val progressSortOrder by viewModel.progressSortOrder.collectAsState()

    val context = LocalContext.current
    var showGenreDropdown by remember { mutableStateOf(false) }
    var showProgressDropdown by remember { mutableStateOf(false) }
    var showEmailDialog by remember { mutableStateOf(false) }

    Log.d("BookListScreen", ">> Using books: $books")

    // Apply Background Image using the fetched URI
    BackgroundWithContent(backgroundImageUri = backgroundImageUri) {
        Scaffold(
            topBar = {
                CenterAlignedTopAppBar(
                    title = { Text("BookTok") },
                    actions = {
                        IconButton(onClick = { showEmailDialog = true }) {
                            Icon(Icons.Default.Share, contentDescription = "Share Book List")
                        }
                    }
                )
            },
            floatingActionButton = {
                Column {
                    FloatingActionButton(onClick = { onAddBook() }) {
                        Icon(Icons.Default.Add, contentDescription = "Add Book")
                    }
                }
            }
        ) { padding ->
            Column(
                modifier = Modifier
                    .padding(padding)
                    .fillMaxSize()
            ) {
                // Search bar
                BookSearchBar(
                    query = searchQuery,
                    onQueryChange = { query -> viewModel.setSearchQuery(query) },
                    modifier = Modifier.padding(16.dp)
                )

                // Genre & Progress Filters
                Row(
                    modifier = Modifier
                        .padding(horizontal = 16.dp)
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally)
                ) {
                    // Genre Filter Dropdown
                    Box {
                        FilterChip(
                            selected = selectedGenre != null,
                            onClick = { showGenreDropdown = true },
                            label = { Text(selectedGenre ?: "Select Genre") }
                        )

                        DropdownMenu(
                            expanded = showGenreDropdown,
                            onDismissRequest = { showGenreDropdown = false }
                        ) {
                            genres.forEach { genre ->
                                DropdownMenuItem(
                                    text = { Text(genre) },
                                    onClick = {
                                        viewModel.setSelectedGenre(genre)
                                        showGenreDropdown = false
                                    }
                                )
                            }
                            DropdownMenuItem(
                                text = { Text("Clear Genre Filter") },
                                onClick = {
                                    viewModel.setSelectedGenre(null)
                                    showGenreDropdown = false
                                }
                            )
                        }
                    }

                    // Progress Filter Dropdown
                    Box {
                        FilterChip(
                            selected = selectedProgress != null || progressSortOrder != null,
                            onClick = { showProgressDropdown = true },
                            label = { Text("Progress Filter") }
                        )

                        DropdownMenu(
                            expanded = showProgressDropdown,
                            onDismissRequest = { showProgressDropdown = false }
                        ) {
                            listOf(
                                -0.25f to "Less Than 25%",
                                0.25f to "25% Progress",
                                0.5f to "50% Progress",
                                0.75f to "75% Progress",
                                1.0f to "Completed"
                            ).forEach { (value, label) ->
                                DropdownMenuItem(
                                    text = { Text(label) },
                                    onClick = {
                                        viewModel.setSelectedProgress(value)
                                        viewModel.setProgressSortOrder(null)
                                        showProgressDropdown = false
                                    }
                                )
                            }
                            HorizontalDivider()
                            DropdownMenuItem(
                                text = { Text("Sort: Lowest to Highest") },
                                onClick = {
                                    viewModel.setProgressSortOrder(BookViewModel.SortOrder.ASCENDING)
                                    viewModel.setSelectedProgress(null)
                                    showProgressDropdown = false
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Sort: Highest to Lowest") },
                                onClick = {
                                    viewModel.setProgressSortOrder(BookViewModel.SortOrder.DESCENDING)
                                    viewModel.setSelectedProgress(null)
                                    showProgressDropdown = false
                                }
                            )
                            HorizontalDivider()
                            DropdownMenuItem(
                                text = { Text("Clear Progress Filter") },
                                onClick = {
                                    viewModel.setSelectedProgress(null)
                                    viewModel.setProgressSortOrder(null)
                                    showProgressDropdown = false
                                }
                            )
                        }
                    }

                    // Clear All Filters
                    Box {
                        FilterChip(
                            selected = selectedGenre != null || selectedProgress != null || progressSortOrder != null,
                            onClick = {
                                viewModel.setSelectedGenre(null)
                                viewModel.setSelectedProgress(null)
                                viewModel.setProgressSortOrder(null)
                            },
                            label = { Icon(Icons.Default.Clear, contentDescription = "Clear Filters") }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // List of Books in Grid Layout
                BookGrid(
                    books = books,
                    onBookClick = { bookId -> onBookClick(bookId) },
                )
            }
        }

        // Email Dialog
        if (showEmailDialog) {
            EmailInputDialog(
                title = "Share Book List",
                onConfirm = { email ->
                    viewModel.shareBookList(context, books, email)
                    showEmailDialog = false
                },
                onDismiss = { showEmailDialog = false }
            )
        }
    }
}
