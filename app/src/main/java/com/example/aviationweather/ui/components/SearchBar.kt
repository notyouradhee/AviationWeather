package com.example.aviationweather.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import com.example.aviationweather.data.AirportsData
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.isImeVisible
import androidx.compose.ui.platform.LocalFocusManager

@Composable
fun SearchBar(
    icaoInput: String,
    onIcaoInputChange: (String) -> Unit,
    onSearch: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current
    val colorScheme = MaterialTheme.colorScheme
    
    var isTextFieldFocused by remember { mutableStateOf(false) }
    @OptIn(ExperimentalLayoutApi::class)
    val isImeVisible = WindowInsets.isImeVisible

    LaunchedEffect(isImeVisible) {
        if (!isImeVisible && isTextFieldFocused) {
            focusManager.clearFocus()
        }
    }

    val suggestions = remember(icaoInput) {
        if (icaoInput.length >= 1) {
            val query = icaoInput.trim().lowercase()
            AirportsData.list.filter {
                it.icao.lowercase().contains(query) ||
                it.name.lowercase().contains(query) ||
                it.city.lowercase().contains(query) ||
                it.country.lowercase().contains(query)
            }.take(5)
        } else {
            emptyList()
        }
    }
    val showSuggestions = isTextFieldFocused && isImeVisible && suggestions.isNotEmpty()

    Column(
        modifier = modifier
            .padding(horizontal = 16.dp, vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        AnimatedVisibility(
            visible = showSuggestions,
            modifier = Modifier.fillMaxWidth()
        ) {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                color = colorScheme.surface,
                tonalElevation = 6.dp,
                shadowElevation = 8.dp,
                border = BorderStroke(1.dp, colorScheme.outline.copy(alpha = 0.5f))
            ) {
                Column(
                    modifier = Modifier.padding(8.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = "Search Suggestions",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = colorScheme.primary,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    )
                    
                    HorizontalDivider(color = colorScheme.outline.copy(alpha = 0.3f))
                    
                    suggestions.forEach { airport ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .clickable {
                                    onIcaoInputChange(airport.icao)
                                    keyboardController?.hide()
                                    onSearch(airport.icao)
                                    isTextFieldFocused = false
                                }
                                .padding(horizontal = 12.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(colorScheme.primaryContainer)
                                    .padding(horizontal = 8.dp, vertical = 4.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = airport.icao,
                                    fontWeight = FontWeight.Bold,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = colorScheme.onPrimaryContainer
                                )
                            }
                            
                            Spacer(modifier = Modifier.width(12.dp))
                            
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = airport.name,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    color = colorScheme.onSurface,
                                    maxLines = 1
                                )
                                Text(
                                    text = "${airport.city}, ${airport.country}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = colorScheme.onSurfaceVariant,
                                    maxLines = 1
                                )
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(28.dp),
            color = colorScheme.surface,
            tonalElevation = 6.dp,
            shadowElevation = 8.dp,
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = null,
                    tint = colorScheme.primary,
                    modifier = Modifier.padding(start = 8.dp)
                )
                TextField(
                    value = icaoInput,
                    onValueChange = {
                        val input = it.trim().uppercase().filter { char -> !char.isWhitespace() }.take(4)
                        onIcaoInputChange(input)
                        isTextFieldFocused = true
                    },
                    placeholder = { Text("Search Airport (e.g. KLAX)") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        capitalization = KeyboardCapitalization.Characters,
                        imeAction = ImeAction.Search,
                    ),
                    keyboardActions = KeyboardActions(
                        onSearch = {
                            if (icaoInput.length in 3..4) {
                                keyboardController?.hide()
                                onSearch(icaoInput)
                                isTextFieldFocused = false
                            }
                        },
                    ),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .onFocusChanged { focusState ->
                            isTextFieldFocused = focusState.isFocused
                        },
                )
                if (icaoInput.isNotEmpty()) {
                    IconButton(onClick = { 
                        onIcaoInputChange("") 
                        isTextFieldFocused = false
                    }) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Clear text",
                            tint = colorScheme.onSurfaceVariant
                        )
                    }
                }
                IconButton(
                    onClick = {
                        keyboardController?.hide()
                        onSearch(icaoInput)
                        isTextFieldFocused = false
                    },
                    enabled = icaoInput.length in 3..4,
                    colors = IconButtonDefaults.iconButtonColors(
                        contentColor = colorScheme.primary,
                        disabledContentColor = colorScheme.onSurfaceVariant.copy(alpha = 0.38f)
                    )
                ) {
                    Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = "Search button")
                }
            }
        }
    }
}
