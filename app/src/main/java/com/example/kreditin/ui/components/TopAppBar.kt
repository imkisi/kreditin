package com.example.kreditin.ui.components

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import com.example.kreditin.Destination

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyAppTopAppBar(
    modifier: Modifier = Modifier,
    currentDestination: Destination? // Pass the current Destination object
) {
    TopAppBar(
        title = {
            val titleText: String
            val titleStyle = when (currentDestination) {
                Destination.HOME -> {
                    titleText = "Kreditin"
                    MaterialTheme.typography.headlineLarge
                }
                Destination.PAYMENT -> {
                    titleText = "Payment"
                    MaterialTheme.typography.headlineSmall
                }
                Destination.ABOUT -> {
                    titleText = "About"
                    MaterialTheme.typography.headlineSmall
                }
                else -> {
                    // Default title
                    titleText = currentDestination?.label ?: "Kreditin"
                    MaterialTheme.typography.titleLarge
                }
            }

            Text(
                text = titleText,
                style = titleStyle,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        },
        modifier = modifier,
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.surface,
            titleContentColor = MaterialTheme.colorScheme.onSurface
        )
        // Other navigationIcon and actions
    )
}
