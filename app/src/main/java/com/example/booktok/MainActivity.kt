package com.example.booktok

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import com.example.booktok.view.screens.SettingsScreen

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

    // Retrieve background image URI from SharedPreferences
    // Retrieve background image URI from SharedPreferences on startup
    val prefs = context.getSharedPreferences("AppPrefs", Context.MODE_PRIVATE)
    var backgroundImageUri by remember { mutableStateOf<String?>(null) }
//    println(">> Retrieved Background Image URI: $backgroundImageUri")  // Debugging

    LaunchedEffect(context) {
        backgroundImageUri = prefs.getString("background_image_uri", null)
        Log.d("App", ">> Loaded backgroundImageUri: $backgroundImageUri")
    }


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
                viewModel = viewModel,
                backgroundImageUri = backgroundImageUri
            )
        }
        composable("settings") {
            SettingsScreen(
                onBackgroundSelected = { imageUri ->
                    // Save the URI using ViewModel
                    viewModel.saveBackgroundUri(context, imageUri)

                    // Update the mutableState to trigger recomposition
                    backgroundImageUri = imageUri
                },
                onBackClick = { navController.popBackStack() }
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
