package com.example.booktok

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.booktok.model.BookDatabase
import com.example.booktok.view.screens.BookDetailScreen
import com.example.booktok.view.screens.BookEditScreen
import com.example.booktok.view.screens.BookListScreen
import com.example.booktok.ui.theme.BookTokTheme
import com.example.booktok.viewmodel.BookViewModel
import androidx.compose.ui.platform.LocalContext
import com.example.booktok.model.BookRepository

/*! Main Activity class that serves as the entry point for the BookTok application
 * Initializes the UI components and sets up the Jetpack Compose environment
 * Enables edge-to-edge display for better visual experience
 */
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            BookTokTheme {
                BookTokApp()
            }
        }
    }
}

/*! Core composable function that sets up the application's navigation and dependency injection
 * Manages the entire app's navigation structure and view model initialization
 * * Uses NavController for handling screen navigation
 * * Implements remember for maintaining state across recompositions
 */
@Composable
fun BookTokApp() {
    val navController = rememberNavController()
    val context = LocalContext.current

    /*! Database and Repository initialization
     * Creates singleton instances of the database and repository
     * * Uses remember to prevent recreation on recomposition
     */
    val database = remember { BookDatabase.getDatabase(context.applicationContext) }
    val repository = remember { BookRepository(database.bookDao()) }

    /*! ViewModel initialization
     * Creates and remembers the BookViewModel instance
     * * Passes repository dependency for data operations
     */
    val viewModel: BookViewModel = remember {
        BookViewModel(repository)
    }

    /*! Navigation setup using NavHost
     * Defines the navigation graph for the entire application
     * * Sets bookList as the starting destination
     * ? Implements type-safe navigation arguments for bookId
     */
    NavHost(
        navController = navController,
        startDestination = "bookList"
    ) {
        /*! Book List Screen Route
         * Entry point of the application showing all books
         * * Provides navigation to book details and adding new books
         */
        composable("bookList") {
            BookListScreen(
                onBookClick = { bookId ->
                    navController.navigate("bookDetail/$bookId")
                },
                onAddBook = { navController.navigate("bookEdit") },
                viewModel = viewModel,
            )
        }

        /*! Book Detail Screen Route
         * Shows detailed information about a specific book
         * * Handles null safety for bookId parameter
         */
        composable("bookDetail/{bookId}") { backStackEntry ->
            val bookId = backStackEntry.arguments?.getString("bookId")?.toLongOrNull()
            BookDetailScreen(
                bookId = bookId ?: 0L,
                onBackClick = { navController.popBackStack() },
                onEditClick = { navController.navigate("bookEdit/$bookId") },
                viewModel = viewModel
            )
        }

        /*! Book Edit Screen Route
         * Handles both adding new books and editing existing ones
         * ? Can be accessed from list
         */
        composable("bookEdit") {
            BookEditScreen(
                onBackClick = { navController.popBackStack() },
                viewModel = viewModel
            )
        }

        composable("bookEdit/{bookId}") { backStackEntry ->
            val bookId = backStackEntry.arguments?.getString("bookId")?.toLongOrNull()
            BookEditScreen(
                bookId = bookId,
                onBackClick = { navController.popBackStack() },
                viewModel = viewModel
            )
        }

    }
}

@Preview(showBackground = true)
@Composable
fun BookTokAppPreview() {
    BookTokTheme {
        BookTokApp()
    }
}
