package com.example.aviationweather.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.automirrored.filled.HelpOutline
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.aviationweather.data.AirportsData
import com.example.aviationweather.data.AirportInfo
import com.example.aviationweather.data.model.DecodedMetar
import kotlinx.coroutines.launch
import kotlin.math.absoluteValue

// ── Flight-category colors and descriptions ──────────────────────────

private val VfrGreen  = Color(0xFF2E7D32)
private val MvfrBlue  = Color(0xFF1565C0)
private val IfrRed    = Color(0xFFC62828)
private val LifrPurple = Color(0xFF6A1B9A)

private fun flightCategoryColor(cat: String): Color = when (cat.trim().uppercase()) {
    "VFR"  -> VfrGreen
    "MVFR" -> MvfrBlue
    "IFR"  -> IfrRed
    "LIFR" -> LifrPurple
    else   -> Color.Gray
}

private fun flightCategoryExplanation(cat: String): String = when (cat.trim().uppercase()) {
    "VFR"  -> "Visual Flight Rules: Ceiling > 3,000 ft and visibility > 5 SM. Excellent visual flight conditions. Perfect for student flights."
    "MVFR" -> "Marginal Visual Flight Rules: Ceiling 1,000 to 3,000 ft and/or visibility 3 to 5 SM. Marginal conditions. Student pilots exercise caution!"
    "IFR"  -> "Instrument Flight Rules: Ceiling 500 to 1,000 ft and/or visibility 1 to 3 SM. Low visibility and ceilings. Instrument rating required."
    "LIFR" -> "Low Instrument Flight Rules: Ceiling < 500 ft and/or visibility < 1 SM. Extremely hazardous conditions. High instrument proficiency required."
    else   -> "Unknown flight rules category. Exercise caution."
}

// ── HomeScreen ──────────────────────────────────────────────────────

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
    val keyboardController = LocalSoftwareKeyboardController.current
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

                // Suggestions and Input Bar container
                var isTextFieldFocused by remember { mutableStateOf(false) }
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
                val showSuggestions = isTextFieldFocused && suggestions.isNotEmpty()

                Column(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
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
                                                icaoInput = airport.icao
                                                keyboardController?.hide()
                                                viewModel.searchAirport(airport.icao)
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
                                    icaoInput = it.trim().uppercase().filter { !it.isWhitespace() }.take(4)
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
                                            viewModel.searchAirport(icaoInput)
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
                                    icaoInput = "" 
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
                                    viewModel.searchAirport(icaoInput)
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

// ── Left Side Airport Directory Drawer ───────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AirportDirectoryDrawer(
    onAirportSelected: (AirportInfo) -> Unit,
    modifier: Modifier = Modifier
) {
    var searchQuery by remember { mutableStateOf("") }
    val colorScheme = MaterialTheme.colorScheme
    
    val filteredAirports = remember(searchQuery) {
        if (searchQuery.isBlank()) {
            AirportsData.list
        } else {
            val query = searchQuery.trim().lowercase()
            AirportsData.list.filter {
                it.icao.lowercase().contains(query) ||
                it.name.lowercase().contains(query) ||
                it.city.lowercase().contains(query) ||
                it.country.lowercase().contains(query)
            }
        }
    }
    
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(vertical = 8.dp)
        ) {
            Icon(
                imageVector = Icons.Default.LocalAirport,
                contentDescription = null,
                tint = colorScheme.primary,
                modifier = Modifier.size(28.dp)
            )
            Spacer(Modifier.width(8.dp))
            Text(
                text = "Airport Directory",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = colorScheme.onSurface
            )
        }
        
        Text(
            text = "Tap an airport below to get its weather report immediately.",
            style = MaterialTheme.typography.bodySmall,
            color = colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(bottom = 12.dp)
        )
        
        // Search field inside drawer
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = { Text("Filter by ICAO, city, or name") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = { searchQuery = "" }) {
                        Icon(Icons.Default.Close, contentDescription = "Clear Search")
                    }
                }
            },
            singleLine = true,
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp)
        )
        
        HorizontalDivider(modifier = Modifier.padding(bottom = 8.dp))
        
        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            if (filteredAirports.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No airports match your search.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                items(filteredAirports) { airport ->
                    AirportItemRow(
                        airport = airport,
                        onClick = { onAirportSelected(airport) }
                    )
                }
            }
        }
    }
}

@Composable
fun AirportItemRow(
    airport: AirportInfo,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colorScheme = MaterialTheme.colorScheme
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = colorScheme.surfaceVariant.copy(alpha = 0.5f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Distinct ICAO badge
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(colorScheme.primaryContainer)
                    .padding(horizontal = 10.dp, vertical = 6.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = airport.icao,
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.bodyMedium,
                    color = colorScheme.onPrimaryContainer
                )
            }
            
            Spacer(Modifier.width(12.dp))
            
            Column(
                modifier = Modifier.weight(1f)
            ) {
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
            
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
            )
        }
    }
}

// ── Premium Weather Content ──────────────────────────────────────────

@Composable
private fun WeatherContent(metar: DecodedMetar) {
    val colorScheme = MaterialTheme.colorScheme
    
    // Dynamic banner header for active airport
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(60.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.LocalAirport,
                    contentDescription = null,
                    tint = colorScheme.onPrimaryContainer,
                    modifier = Modifier.size(32.dp)
                )
            }
            Spacer(Modifier.width(16.dp))
            Column {
                Text(
                    text = metar.airportCode.ifBlank { "Unknown Airport" },
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = colorScheme.onSurface
                )
                Text(
                    text = "Live METAR Briefing & Safety Alerts",
                    style = MaterialTheme.typography.bodySmall,
                    color = colorScheme.onSurfaceVariant
                )
            }
        }
    }

    // Gorgeous Flight Category Badge & Educational Info
    FlightCategoryBadgeCard(metar.flightCategory)

    // 2x2 Grid for Key METAR Elements
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Wind Card with rotating directional arrow
            Box(modifier = Modifier.weight(1f)) {
                WindCard(
                    direction = metar.windDirection,
                    speed = metar.windSpeed,
                    gust = metar.windGust
                )
            }
            // Visibility Card with description
            Box(modifier = Modifier.weight(1f)) {
                VisibilityCard(visStr = metar.visibility)
            }
        }
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Temp & Dewpoint spread with warning alert
            Box(modifier = Modifier.weight(1f)) {
                TempDewpointSpreadCard(
                    tempStr = metar.temperature,
                    dewpStr = metar.dewpoint
                )
            }
            // Dual Pressure Units Altimeter Card
            Box(modifier = Modifier.weight(1f)) {
                PressureAltimeterCard(altimStr = metar.altimeter)
            }
        }
    }

    Spacer(Modifier.height(12.dp))

    if (metar.runways.isNotEmpty()) {
        ActiveRunwayAdvisorCard(metar = metar)
    } else {
        InteractiveRunwayCalculatorCard(
            windDirection = metar.windDirection,
            windSpeed = metar.windSpeed,
            isVariableOrCalm = metar.isWindVariableOrCalm
        )
    }

    Spacer(Modifier.height(12.dp))

    // Cloud Layers pills list
    WeatherCard(title = "Cloud Layers", icon = Icons.Default.Cloud) {
        if (metar.cloudLayers.isEmpty()) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(vertical = 4.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.WbSunny,
                    contentDescription = null,
                    tint = Color(0xFFFFB300),
                    modifier = Modifier.size(20.dp)
                )
                Spacer(Modifier.width(8.dp))
                Text("Clear Skies (SKC/CLR)", style = MaterialTheme.typography.bodyLarge)
            }
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                metar.cloudLayers.forEach { layer ->
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(colorScheme.surfaceVariant.copy(alpha = 0.5f))
                            .padding(10.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.FilterDrama,
                                contentDescription = null,
                                tint = colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(Modifier.width(10.dp))
                            Text(
                                text = layer,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }
        }
    }

    // Present weather occurrences
    if (metar.presentWeather.isNotBlank() && metar.presentWeather.lowercase() != "no significant weather") {
        WeatherCard(title = "Active Weather Hazards", icon = Icons.Default.Warning, headerColor = colorScheme.error) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Thunderstorm,
                    contentDescription = null,
                    tint = colorScheme.error,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(Modifier.width(12.dp))
                Text(
                    text = metar.presentWeather,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold,
                    color = colorScheme.error
                )
            }
        }
    }

    // Raw METAR string with copy button
    RawMetarCard(metar.rawMetar)
}

// ── Enhanced Reusable Components ─────────────────────────────────────

@Composable
private fun FlightCategoryBadgeCard(category: String) {
    val bgColor = flightCategoryColor(category)
    val explanation = flightCategoryExplanation(category)
    val colorScheme = MaterialTheme.colorScheme
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = bgColor
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "FLIGHT CATEGORY",
                    color = Color.White.copy(alpha = 0.8f),
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.labelMedium
                )
                Surface(
                    color = Color.White.copy(alpha = 0.25f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = category.uppercase(),
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }
            
            HorizontalDivider(color = Color.White.copy(alpha = 0.2f))
            
            Text(
                text = explanation,
                color = Color.White,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
private fun WindCard(direction: Int, speed: Int, gust: Int) {
    val colorScheme = MaterialTheme.colorScheme
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = colorScheme.surface)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Air,
                    contentDescription = null,
                    tint = colorScheme.primary,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(Modifier.width(6.dp))
                Text(
                    text = "Wind",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = colorScheme.onSurfaceVariant
                )
            }
            
            HorizontalDivider()
            
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column {
                    Text(
                        text = "$speed kt",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Dir: $direction°",
                        style = MaterialTheme.typography.bodySmall,
                        color = colorScheme.onSurfaceVariant
                    )
                    if (gust > 0) {
                        Surface(
                            color = colorScheme.errorContainer,
                            shape = RoundedCornerShape(4.dp),
                            modifier = Modifier.padding(top = 4.dp)
                        ) {
                            Text(
                                text = "Gusts: $gust kt",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = colorScheme.onErrorContainer,
                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                            )
                        }
                    }
                }
                
                // Rotating wind compass arrow!
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(colorScheme.primaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Navigation,
                        contentDescription = "Wind Arrow pointing to $direction degrees",
                        tint = colorScheme.onPrimaryContainer,
                        modifier = Modifier
                            .size(24.dp)
                            .rotate(direction.toFloat())
                    )
                }
            }
        }
    }
}

@Composable
private fun VisibilityCard(visStr: String) {
    val colorScheme = MaterialTheme.colorScheme
    val visMiles = remember(visStr) {
        visStr.removeSuffix("SM").trim().toDoubleOrNull() ?: 10.0
    }
    
    val (ratingText, ratingColor) = when {
        visMiles >= 10.0 -> "Excellent" to VfrGreen
        visMiles >= 5.0 -> "Good" to VfrGreen
        visMiles >= 3.0 -> "Marginal" to MvfrBlue
        visMiles >= 1.0 -> "Low" to IfrRed
        else -> "Very Low" to LifrPurple
    }
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = colorScheme.surface)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Visibility,
                    contentDescription = null,
                    tint = colorScheme.primary,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(Modifier.width(6.dp))
                Text(
                    text = "Visibility",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = colorScheme.onSurfaceVariant
                )
            }
            
            HorizontalDivider()
            
            Text(
                text = visStr,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
            
            Surface(
                color = ratingColor.copy(alpha = 0.15f),
                shape = RoundedCornerShape(6.dp),
                modifier = Modifier.padding(top = 2.dp)
            ) {
                Text(
                    text = ratingText,
                    color = ratingColor,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                )
            }
        }
    }
}

@Composable
private fun TempDewpointSpreadCard(tempStr: String, dewpStr: String) {
    val colorScheme = MaterialTheme.colorScheme
    
    val tempVal = tempStr.removeSuffix("°C").trim().toIntOrNull()
    val dewpVal = dewpStr.removeSuffix("°C").trim().toIntOrNull()
    
    val spread = if (tempVal != null && dewpVal != null) {
        (tempVal - dewpVal).absoluteValue
    } else {
        null
    }
    
    val isNarrowSpread = spread != null && spread <= 3
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = colorScheme.surface)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Thermostat,
                    contentDescription = null,
                    tint = colorScheme.primary,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(Modifier.width(6.dp))
                Text(
                    text = "Temp & Dewp",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = colorScheme.onSurfaceVariant
                )
            }
            
            HorizontalDivider()
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = tempStr,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Dewpoint: $dewpStr",
                        style = MaterialTheme.typography.bodySmall,
                        color = colorScheme.onSurfaceVariant
                    )
                }
            }
            
            if (isNarrowSpread) {
                Surface(
                    color = colorScheme.errorContainer,
                    shape = RoundedCornerShape(6.dp),
                    modifier = Modifier.padding(top = 4.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = "Fog risk warning",
                            tint = colorScheme.error,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(Modifier.width(4.dp))
                        Text(
                            text = "Fog Risk (Spread ${spread}°C)",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = colorScheme.onErrorContainer
                        )
                    }
                }
            } else if (spread != null) {
                Text(
                    text = "Spread: ${spread}°C (Safe)",
                    style = MaterialTheme.typography.labelSmall,
                    color = colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

@Composable
private fun PressureAltimeterCard(altimStr: String) {
    val colorScheme = MaterialTheme.colorScheme
    
    // Parse altimeter like "29.92 inHg" to compute Hectopascals
    val inHg = remember(altimStr) {
        altimStr.removeSuffix("inHg").trim().toDoubleOrNull()
    }
    val hPa = remember(inHg) {
        if (inHg != null) {
            (inHg * 33.8639).toInt()
        } else {
            null
        }
    }
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = colorScheme.surface)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Speed,
                    contentDescription = null,
                    tint = colorScheme.primary,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(Modifier.width(6.dp))
                Text(
                    text = "Altimeter",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = colorScheme.onSurfaceVariant
                )
            }
            
            HorizontalDivider()
            
            Text(
                text = altimStr,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
            
            if (hPa != null) {
                Text(
                    text = "$hPa hPa / mb",
                    style = MaterialTheme.typography.bodySmall,
                    color = colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

@Composable
private fun WeatherCard(
    title: String,
    icon: ImageVector,
    headerColor: Color = MaterialTheme.colorScheme.primary,
    content: @Composable ColumnScope.() -> Unit,
) {
    val colorScheme = MaterialTheme.colorScheme
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.elevatedCardColors(containerColor = colorScheme.surface)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = headerColor,
                    modifier = Modifier.size(22.dp)
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = colorScheme.onSurface
                )
            }
            HorizontalDivider()
            content()
        }
    }
}

@Composable
private fun RawMetarCard(raw: String) {
    val colorScheme = MaterialTheme.colorScheme
    @Suppress("DEPRECATION")
    val clipboardManager = LocalClipboardManager.current
    var isCopied by remember { mutableStateOf(false) }
    
    OutlinedCard(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.outlinedCardColors(containerColor = colorScheme.surface.copy(alpha = 0.5f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Raw METAR",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                
                Button(
                    onClick = {
                        clipboardManager.setText(AnnotatedString(raw))
                        isCopied = true
                    },
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                    modifier = Modifier.height(28.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isCopied) VfrGreen else colorScheme.primaryContainer,
                        contentColor = if (isCopied) Color.White else colorScheme.onPrimaryContainer
                    )
                ) {
                    Icon(
                        imageVector = if (isCopied) Icons.Default.Check else Icons.Default.ContentCopy,
                        contentDescription = null,
                        modifier = Modifier.size(12.dp)
                    )
                    Spacer(Modifier.width(4.dp))
                    Text(
                        text = if (isCopied) "Copied" else "Copy",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            
            // Auto-reset the copied status text
            LaunchedEffect(isCopied) {
                if (isCopied) {
                    kotlinx.coroutines.delay(2000)
                    isCopied = false
                }
            }
            
            Spacer(Modifier.height(8.dp))
            
            Surface(
                color = colorScheme.surfaceVariant.copy(alpha = 0.7f),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = raw,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 12.sp,
                    color = colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(12.dp)
                )
            }
        }
    }
}

@Composable
private fun InfoRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
private fun LabeledValue(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun HeroPromptSection() {
    val colorScheme = MaterialTheme.colorScheme
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Box(
            modifier = Modifier
                .size(64.dp)
                .clip(CircleShape)
                .background(
                    Brush.linearGradient(
                        listOf(colorScheme.primary, colorScheme.secondary),
                    ),
                ),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = Icons.Default.FlightTakeoff,
                contentDescription = null,
                tint = colorScheme.onPrimary,
                modifier = Modifier.size(32.dp)
            )
        }
        Text(
            text = "Aviation Weather Brief",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
        )
        Text(
            text = "Student-pilot friendly real-time METAR decoder.",
            style = MaterialTheme.typography.bodyMedium,
            color = colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
    }
}

// ── Active Runway Advisor and Smart Wind Calculator ──────────────────

@Composable
private fun ActiveRunwayAdvisorCard(metar: DecodedMetar) {
    val colorScheme = MaterialTheme.colorScheme
    
    // Find the recommended runway
    val recommended = metar.runways.firstOrNull { it.isRecommended }
    val hasCrosswindHazard = metar.runways.any { it.isRecommended && it.crosswindComponent > 15.0 }
    
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.elevatedCardColors(containerColor = colorScheme.surface)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.FlightTakeoff,
                        contentDescription = null,
                        tint = colorScheme.primary,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = "Runway Advisor",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = colorScheme.onSurface
                    )
                }
                
                Surface(
                    color = colorScheme.primaryContainer,
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = if (metar.isWindVariableOrCalm) "Calm/Variable Winds" else "Active Advisory",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = colorScheme.onPrimaryContainer,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }
            
            HorizontalDivider()
            
            if (metar.isWindVariableOrCalm) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = null,
                            tint = colorScheme.secondary,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = "Winds are calm or variable. No specific runway is favored. Pilots should check local traffic patterns.",
                            style = MaterialTheme.typography.bodySmall,
                            color = colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else if (recommended != null) {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = if (hasCrosswindHazard) colorScheme.errorContainer else colorScheme.primaryContainer.copy(alpha = 0.4f)
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = if (hasCrosswindHazard) Icons.Default.Warning else Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    tint = if (hasCrosswindHazard) colorScheme.error else VfrGreen,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(Modifier.width(8.dp))
                                Text(
                                    text = "${recommended.runwayLabel} Recommended",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = if (hasCrosswindHazard) colorScheme.onErrorContainer else colorScheme.onPrimaryContainer
                                )
                            }
                        }
                        
                        val hwKts = recommended.headwindComponent.toInt()
                        val cwKts = recommended.crosswindComponent.toInt()
                        val windText = buildString {
                            if (hwKts >= 0) {
                                append("$hwKts kt headwind")
                            } else {
                                append("${hwKts.absoluteValue} kt tailwind")
                            }
                            append(", $cwKts kt crosswind")
                            if (cwKts > 0) {
                                append(if (recommended.isLeftCrosswind) " from LEFT" else " from RIGHT")
                            }
                        }
                        
                        Text(
                            text = windText,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium,
                            color = if (hasCrosswindHazard) colorScheme.onErrorContainer.copy(alpha = 0.8f) else colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                        )
                        
                        if (hasCrosswindHazard) {
                            Spacer(Modifier.height(4.dp))
                            Surface(
                                color = colorScheme.error,
                                shape = RoundedCornerShape(6.dp)
                            ) {
                                Text(
                                    text = "⚠️ HIGH CROSSWIND WARNING: Exceeds 15 kt safe threshold!",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }
                }
            }
            
            Text(
                text = "All Runways Comparison",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                color = colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 4.dp)
            )
            
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                metar.runways.forEach { rwy ->
                    val isRec = rwy.isRecommended
                    val hw = rwy.headwindComponent.toInt()
                    val cw = rwy.crosswindComponent.toInt()
                    
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(
                                if (isRec) colorScheme.primaryContainer.copy(alpha = 0.15f)
                                else colorScheme.surfaceVariant.copy(alpha = 0.3f)
                            )
                            .padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(if (isRec) colorScheme.primary else colorScheme.outline.copy(alpha = 0.3f))
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = rwy.runwayLabel.removePrefix("Runway "),
                                    fontWeight = FontWeight.Bold,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = if (isRec) colorScheme.onPrimary else colorScheme.onSurface
                                )
                            }
                            Spacer(Modifier.width(8.dp))
                            Text(
                                text = "Hdg: ${rwy.heading.toString().padStart(3, '0')}°",
                                style = MaterialTheme.typography.bodySmall,
                                color = colorScheme.onSurfaceVariant
                            )
                        }
                        
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = if (hw >= 0) "HW: $hw kt" else "TW: ${hw.absoluteValue} kt",
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.Bold,
                                    color = if (hw >= 0) VfrGreen else colorScheme.error
                                )
                                Text(
                                    text = "CW: $cw kt" + (if (cw > 0) (if (rwy.isLeftCrosswind) " L" else " R") else ""),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = if (cw > 15) colorScheme.error else colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun InteractiveRunwayCalculatorCard(
    windDirection: Int,
    windSpeed: Int,
    isVariableOrCalm: Boolean
) {
    val colorScheme = MaterialTheme.colorScheme
    var selectedRunwayNumber by remember { mutableStateOf(9) }
    
    val runwayHdg = selectedRunwayNumber * 10
    val diffRad = (windDirection - runwayHdg) * Math.PI / 180.0
    val headwind = if (isVariableOrCalm) 0.0 else windSpeed * kotlin.math.cos(diffRad)
    val crosswind = if (isVariableOrCalm) 0.0 else windSpeed * kotlin.math.sin(diffRad)
    
    val hwKts = headwind.toInt()
    val cwKts = kotlin.math.abs(crosswind).toInt()
    val isLeftCrosswind = crosswind < 0
    val hasCrosswindWarning = cwKts > 15
    
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.elevatedCardColors(containerColor = colorScheme.surface)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Calculate,
                        contentDescription = null,
                        tint = colorScheme.primary,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = "Interactive Runway Calculator",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = colorScheme.onSurface
                    )
                }
                
                Surface(
                    color = colorScheme.secondaryContainer,
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = "Manual Entry",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = colorScheme.onSecondaryContainer,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }
            
            Text(
                text = "We don't have this airport's runways in our database. Choose a runway heading below to calculate the wind components in real-time.",
                style = MaterialTheme.typography.bodySmall,
                color = colorScheme.onSurfaceVariant
            )
            
            HorizontalDivider()
            
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = { if (selectedRunwayNumber > 1) selectedRunwayNumber-- else selectedRunwayNumber = 36 },
                        colors = IconButtonDefaults.iconButtonColors(containerColor = colorScheme.surfaceVariant)
                    ) {
                        Icon(Icons.Default.Remove, contentDescription = "Decrease Runway")
                    }
                    
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "Runway ${selectedRunwayNumber.toString().padStart(2, '0')}",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = colorScheme.primary
                        )
                        Text(
                            text = "Heading: ${runwayHdg.toString().padStart(3, '0')}°",
                            style = MaterialTheme.typography.bodySmall,
                            color = colorScheme.onSurfaceVariant,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                    
                    IconButton(
                        onClick = { if (selectedRunwayNumber < 36) selectedRunwayNumber++ else selectedRunwayNumber = 1 },
                        colors = IconButtonDefaults.iconButtonColors(containerColor = colorScheme.surfaceVariant)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "Increase Runway")
                    }
                }
                
                Slider(
                    value = selectedRunwayNumber.toFloat(),
                    onValueChange = { selectedRunwayNumber = it.toInt().coerceIn(1, 36) },
                    valueRange = 1f..36f,
                    steps = 34,
                    colors = SliderDefaults.colors(
                        thumbColor = colorScheme.primary,
                        activeTrackColor = colorScheme.primary,
                        inactiveTrackColor = colorScheme.surfaceVariant
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
            }
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Card(
                    modifier = Modifier.weight(1f),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isVariableOrCalm) colorScheme.surfaceVariant.copy(alpha = 0.4f)
                        else if (hwKts >= 0) VfrGreen.copy(alpha = 0.12f)
                        else colorScheme.errorContainer.copy(alpha = 0.4f)
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = if (isVariableOrCalm) "WIND COMPONENT" else if (hwKts >= 0) "HEADWIND" else "TAILWIND",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = if (isVariableOrCalm) colorScheme.onSurfaceVariant
                            else if (hwKts >= 0) VfrGreen
                            else colorScheme.onErrorContainer
                        )
                        Text(
                            text = if (isVariableOrCalm) "0 kt" else "${hwKts.absoluteValue} kt",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = if (isVariableOrCalm) colorScheme.onSurface
                            else if (hwKts >= 0) VfrGreen
                            else colorScheme.error
                        )
                    }
                }
                
                Card(
                    modifier = Modifier.weight(1f),
                    colors = CardDefaults.cardColors(
                        containerColor = if (hasCrosswindWarning) colorScheme.errorContainer else colorScheme.surfaceVariant.copy(alpha = 0.4f)
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = "CROSSWIND",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = if (hasCrosswindWarning) colorScheme.error else colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = if (isVariableOrCalm) "0 kt" else "$cwKts kt" + (if (cwKts > 0) (if (isLeftCrosswind) " L" else " R") else ""),
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = if (hasCrosswindWarning) colorScheme.error else colorScheme.onSurface
                        )
                    }
                }
            }
            
            if (isVariableOrCalm) {
                Text(
                    text = "Variable or calm winds: components are negligible.",
                    style = MaterialTheme.typography.bodySmall,
                    color = colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            } else if (hasCrosswindWarning) {
                Surface(
                    color = colorScheme.errorContainer,
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = null,
                            tint = colorScheme.error,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = "Warning: Crosswind component exceeds 15 kt safety threshold for student pilots!",
                            style = MaterialTheme.typography.bodySmall,
                            color = colorScheme.onErrorContainer,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}
