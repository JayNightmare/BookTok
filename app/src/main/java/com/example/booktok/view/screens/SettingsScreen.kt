package com.example.booktok.view.screens

import android.content.Context
import android.content.Intent
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onBackgroundSelected: (String) -> Unit,
    onBackClick: () -> Unit
) {
    val context = LocalContext.current

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Settings") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
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
            Text(
                text = "Customize Your App",
                style = MaterialTheme.typography.headlineSmall
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Use BackgroundPicker here
            BackgroundPicker(
                onImageSelected = { imageUri ->
                    // Take persistable URI permission
                    val takeFlags = Intent.FLAG_GRANT_READ_URI_PERMISSION
                    context.contentResolver.takePersistableUriPermission(
                        android.net.Uri.parse(imageUri),
                        takeFlags
                    )

                    // Save the selected background URI to SharedPreferences
                    val prefs = context.getSharedPreferences("AppPrefs", Context.MODE_PRIVATE)
                    prefs.edit().putString("background_image_uri", imageUri).apply()

                    // Notify parent
                    onBackgroundSelected(imageUri)
                },
                context = context,
                saveBackgroundUri = { ctx, uri ->
                    // Save the selected background URI to SharedPreferences
                    val prefs = ctx.getSharedPreferences("AppPrefs", Context.MODE_PRIVATE)
                    prefs.edit().putString("background_image_uri", uri).apply()
                }
            )
        }
    }
}

