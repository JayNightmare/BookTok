package com.example.booktok.view.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Slider
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Sort
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.booktok.ui.components.BookItem
import com.example.booktok.ui.components.BookSearchBar
import com.example.booktok.viewmodel.BookViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookListScreen(
    onBookClick: (Long) -> Unit,
    onAddBook: () -> Unit,
    viewModel: BookViewModel
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

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("BookTok") },
                actions = {
                    IconButton(onClick = {
                        viewModel.shareBookList(context, books)
                    }) {
                        Icon(Icons.Default.Share, contentDescription = "Share Book List")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onAddBook) {
                Icon(Icons.Default.Add, contentDescription = "Add Book")
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
                horizontalArrangement = Arrangement.spacedBy(8.dp)
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
                            0.25f to "25% >",
                            0.5f to "50% >",
                            0.75f to "75% >",
                            1.0f to "100%"
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
                        Divider()
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
                        Divider()
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
            }

            // Book List
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(books) { book ->
                    BookItem(
                        book = book,
                        onClick = { onBookClick(book.id) }
                    )
                }
            }
        }
    }
}
