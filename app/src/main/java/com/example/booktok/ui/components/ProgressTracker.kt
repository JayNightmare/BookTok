package com.example.booktok.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun ProgressTracker(
    pagesRead: Int,
    totalPages: Int,
    onProgressChange: (Int) -> Unit,
    keyboardOptions: KeyboardOptions,
    modifier: Modifier = Modifier
) {
    var currentPages by remember { mutableIntStateOf(pagesRead) }

    Column(modifier = modifier) {
        Text(
            text = "Reading Progress",
            style = MaterialTheme.typography.titleMedium
        )
        Spacer(modifier = Modifier.height(8.dp))

        LinearProgressIndicator(
            progress = { if (totalPages > 0) currentPages.toFloat() / totalPages else 0f },
            modifier = Modifier.fillMaxWidth(),
        )
        Spacer(modifier = Modifier.height(8.dp))
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("Pages read: $currentPages")
            Text("Total pages: $totalPages")
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        OutlinedTextField(
            value = currentPages.toString(),
            onValueChange = { value ->
                val pages = value.toIntOrNull() ?: 0
                if (pages in 0..totalPages) {
                    currentPages = pages
                    onProgressChange(pages)
                }
            },
            label = { Text("Pages read") },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = keyboardOptions
        )
    }
}
