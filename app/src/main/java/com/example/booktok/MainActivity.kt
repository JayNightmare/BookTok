package com.example.booktok

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.viewmodel.compose.viewModel
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

@Composable
fun BookTokApp() {
    val navController = rememberNavController()
    val context = LocalContext.current

    // Initialize Database and Repository
    val database = remember { BookDatabase.getDatabase(context.applicationContext) }
    val repository = remember { BookRepository(database.bookDao()) }

    // Create ViewModel and pass the repository
    val viewModel: BookViewModel = remember {
        BookViewModel(repository)
    }

    NavHost(
        navController = navController,
        startDestination = "bookList"
    ) {
        composable("bookList") {
            BookListScreen(
                onBookClick = { bookId ->
                    navController.navigate("bookDetail/$bookId")
                },
                onAddBook = {
                    navController.navigate("bookEdit")
                },
                viewModel = viewModel
            )
        }
        composable("bookDetail/{bookId}") { backStackEntry ->
            val bookId = backStackEntry.arguments?.getString("bookId")?.toLongOrNull()
            BookDetailScreen(
                bookId = bookId ?: 0L,
                onBackClick = { navController.popBackStack() },
                viewModel = viewModel
            )
        }
        composable("bookEdit") {
            BookEditScreen(
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
