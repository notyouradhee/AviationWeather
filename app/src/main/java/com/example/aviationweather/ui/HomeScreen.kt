package com.example.aviationweather.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.HelpOutline
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Flight
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.aviationweather.ui.components.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: MetarViewModel,
    isDarkTheme: Boolean,
    onToggleTheme: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    var icaoInput by remember { mutableStateOf("") }
    var showHelpDialog by remember { mutableStateOf(false) }
    val colorScheme = MaterialTheme.colorScheme
    
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val coroutineScope = rememberCoroutineScope()
    
    val backgroundBrush = Brush.verticalGradient(
        colors = listOf(colorScheme.background, colorScheme.surfaceVariant),
    )

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet(
                modifier = Modifier
                    .width(320.dp)
                    .fillMaxHeight(),
                drawerContainerColor = colorScheme.surface,
                drawerShape = RoundedCornerShape(topEnd = 16.dp, bottomEnd = 16.dp)
            ) {
                AirportDirectoryDrawer(
                    onAirportSelected = { airport ->
                        icaoInput = airport.icao
                        viewModel.searchAirport(airport.icao)
                        coroutineScope.launch {
                            drawerState.close()
                        }
                    }
                )
            }
        }
    ) {
        Scaffold(
            modifier = modifier,
            containerColor = Color.Transparent,
        ) { padding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(backgroundBrush)
                    .padding(padding),
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                ) {
                    Spacer(Modifier.height(8.dp))

                    // Premium header row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        IconButton(
                            onClick = {
                                coroutineScope.launch {
                                    drawerState.open()
                                }
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Menu,
                                contentDescription = "Open Airport Directory Menu",
                                tint = colorScheme.primary
                            )
                        }
                        
                        Surface(
                            color = colorScheme.surface,
                            shape = RoundedCornerShape(24.dp),
                            tonalElevation = 1.dp,
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Flight,
                                    contentDescription = null,
                                    tint = colorScheme.primary,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(Modifier.width(8.dp))
                                Text(
                                    text = "AeroBrief",
                                    fontWeight = FontWeight.Bold,
                                    style = MaterialTheme.typography.titleMedium,
                                )
                            }
                        }

                        // Dark Theme Toggle and Help/Info Button
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            IconButton(onClick = onToggleTheme) {
                                Icon(
                                    imageVector = if (isDarkTheme) Icons.Default.LightMode else Icons.Default.DarkMode,
                                    contentDescription = "Toggle Dark/Light Mode",
                                    tint = colorScheme.primary
                                )
                            }
                            IconButton(onClick = { showHelpDialog = true }) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.HelpOutline,
                                    contentDescription = "How to Search Help",
                                    tint = colorScheme.primary
                                )
                            }
                        }
                    }

                    Spacer(Modifier.height(8.dp))

                    // ── Main Body Content (Conditional Scrolling / Centering) ──
                    if (uiState is UiState.Success) {
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth()
                                .verticalScroll(rememberScrollState()),
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                        ) {
                            Spacer(Modifier.height(8.dp))
                            
                            HeroPromptSection()

                            Spacer(Modifier.height(10.dp))

                            WeatherContent(metar = (uiState as UiState.Success).data)

                            Spacer(Modifier.height(110.dp))
                        }
                    } else {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth()
                                .padding(bottom = 80.dp), // offset bottom search bar for optical centering
                            contentAlignment = Alignment.Center,
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(20.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                HeroPromptSection()

                                when (val state = uiState) {
                                    is UiState.Loading -> {
                                        CircularProgressIndicator()
                                    }

                                    is UiState.Error -> {
                                        Card(
                                            colors = CardDefaults.cardColors(
                                                containerColor = colorScheme.errorContainer,
                                            ),
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(horizontal = 16.dp),
                                        ) {
                                            Text(
                                                text = state.message,
                                                color = colorScheme.onErrorContainer,
                                                modifier = Modifier.padding(16.dp),
                                                textAlign = TextAlign.Center
                                            )
                                        }
                                    }

                                    else -> {
                                        // Idle state has no loading spinner
                                    }
                                }
                            }
                        }
                    }
                }

                SearchBar(
                    icaoInput = icaoInput,
                    onIcaoInputChange = { icaoInput = it },
                    onSearch = { viewModel.searchAirport(it) },
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .imePadding()
                )

                // Help/Search Guide Dialog
                if (showHelpDialog) {
                    AlertDialog(
                        onDismissRequest = { showHelpDialog = false },
                        title = {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.HelpOutline,
                                    contentDescription = null,
                                    tint = colorScheme.primary,
                                    modifier = Modifier.size(24.dp)
                                )
                                Text(
                                    text = "Search & Briefing Guide",
                                    fontWeight = FontWeight.Bold,
                                    style = MaterialTheme.typography.titleMedium
                                )
                            }
                        },
                        text = {
                            Column(
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Text(
                                    text = "AeroBrief fetches and decodes real-time aviation weather reports (METAR) for global airports. Here is how you can use the app:",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = colorScheme.onSurfaceVariant
                                )
                                
                                Card(
                                    colors = CardDefaults.cardColors(
                                        containerColor = colorScheme.primaryContainer.copy(alpha = 0.4f)
                                    ),
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(
                                        modifier = Modifier.padding(12.dp),
                                        verticalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Text(
                                            text = "1. Search Directly",
                                            style = MaterialTheme.typography.titleSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = colorScheme.onPrimaryContainer
                                        )
                                        Text(
                                            text = "Enter any 4-character ICAO code in the bottom search bar (e.g. KLAX for Los Angeles) and press the search arrow.",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                                        )
                                    }
                                }
                                
                                Card(
                                    colors = CardDefaults.cardColors(
                                        containerColor = colorScheme.secondaryContainer.copy(alpha = 0.4f)
                                    ),
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(
                                        modifier = Modifier.padding(12.dp),
                                        verticalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Text(
                                            text = "2. Use the Directory Menu",
                                            style = MaterialTheme.typography.titleSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = colorScheme.onSecondaryContainer
                                        )
                                        Text(
                                            text = "Tap the top-left menu icon (three horizontal lines) to search, filter, and pick from our built-in registry of over 70 major global hubs.",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = colorScheme.onSecondaryContainer.copy(alpha = 0.8f)
                                        )
                                    }
                                }
                                
                                Column(
                                    verticalArrangement = Arrangement.spacedBy(8.dp),
                                    modifier = Modifier.padding(horizontal = 4.dp)
                                ) {
                                    Text(
                                        text = "Suggested Tryouts (Tap to search):",
                                        style = MaterialTheme.typography.labelLarge,
                                        fontWeight = FontWeight.Bold,
                                        color = colorScheme.onSurface
                                    )
                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        val suggestedCodes = listOf("KJFK" to "NY", "EGLL" to "London", "VNKT" to "KTM")
                                        suggestedCodes.forEach { (code, city) ->
                                            Surface(
                                                color = colorScheme.primaryContainer,
                                                shape = RoundedCornerShape(8.dp),
                                                modifier = Modifier
                                                    .weight(1f)
                                                    .clickable {
                                                        icaoInput = code
                                                        viewModel.searchAirport(code)
                                                        showHelpDialog = false
                                                    }
                                            ) {
                                                Text(
                                                    text = "$code\n($city)",
                                                    style = MaterialTheme.typography.bodySmall,
                                                    fontWeight = FontWeight.Bold,
                                                    color = colorScheme.onPrimaryContainer,
                                                    textAlign = TextAlign.Center,
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .padding(horizontal = 4.dp, vertical = 6.dp)
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        },
                        confirmButton = {
                            TextButton(onClick = { showHelpDialog = false }) {
                                Text(
                                    text = "Got it",
                                    fontWeight = FontWeight.Bold,
                                    color = colorScheme.primary
                                )
                            }
                        },
                        shape = RoundedCornerShape(20.dp),
                        containerColor = colorScheme.surface,
                        tonalElevation = 6.dp
                    )
                }
            }
        }
    }
}
