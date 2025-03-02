package com.example.booktok.ui.components

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier

@Composable
fun ClickableTextField(
    text: String,
    label: String,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    // Trigger the onClick action when pressed
    LaunchedEffect(isPressed) {
        if (isPressed) {
            onClick()
        }
    }

    OutlinedTextField(
        value = text,
        onValueChange = {},
        label = { Text(label) },
        readOnly = true,
        interactionSource = interactionSource,
        modifier = Modifier.fillMaxWidth(),
        trailingIcon = { Icon(
                imageVector = Icons.Default.DateRange,
                contentDescription = "Date Icon"
            )
        }
    )
}
