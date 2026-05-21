package com.example.aviationweather.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.aviationweather.data.model.DecodedMetar

// ── Flight-category colours ─────────────────────────────────────────

private val VfrGreen  = Color(0xFF2E7D32)
private val MvfrBlue  = Color(0xFF1565C0)
private val IfrRed    = Color(0xFFC62828)
private val LifrPurple = Color(0xFF6A1B9A)

private fun flightCategoryColor(cat: String): Color = when (cat.uppercase()) {
    "VFR"  -> VfrGreen
    "MVFR" -> MvfrBlue
    "IFR"  -> IfrRed
    "LIFR" -> LifrPurple
    else   -> Color.Gray
}

// ── HomeScreen ──────────────────────────────────────────────────────

/**
 * Main screen: search bar → state-driven content (Loading / Error / cards).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(viewModel: MetarViewModel, modifier: Modifier = Modifier) {
    val uiState by viewModel.uiState.collectAsState()
    var icaoInput by remember { mutableStateOf("") }
    val keyboardController = LocalSoftwareKeyboardController.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Aviation Weather")
                        Text(
                            text = "Decode METARs fast",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f),
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                ),
            )
        },
        modifier = modifier,
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Spacer(Modifier.height(4.dp))

            // Premium header card
            ElevatedCard(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Navigation,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = "Student Pilot Toolkit",
                            style = MaterialTheme.typography.titleMedium,
                        )
                    }
                    Text(
                        text = "Enter an airport ICAO to get a clean, flight-ready briefing.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            // ── Search bar ──────────────────────────────────────────
            OutlinedTextField(
                value = icaoInput,
                onValueChange = { icaoInput = it.uppercase().take(4) },
                label = { Text("Airport ICAO code") },
                placeholder = { Text("KJFK, KLAX, EGLL, RKSI") },
                singleLine = true,
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.Characters,
                    imeAction = ImeAction.Search,
                ),
                keyboardActions = KeyboardActions(
                    onSearch = {
                        keyboardController?.hide()
                        viewModel.searchAirport(icaoInput)
                    },
                ),
                modifier = Modifier.fillMaxWidth(),
            )

            Button(
                onClick = {
                    keyboardController?.hide()
                    viewModel.searchAirport(icaoInput)
                },
                enabled = icaoInput.length in 3..4,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Icon(Icons.Default.Search, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("Search METAR")
            }

            // Help bar for ICAO codes
            ElevatedCard(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = "Need an airport code?",
                            style = MaterialTheme.typography.titleSmall,
                        )
                    }
                    Text(
                        text = "ICAO codes are 4-letter identifiers used in flight planning.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.LocationOn,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.secondary,
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = "Examples: KJFK (New York), KLAX (Los Angeles), EGLL (London)",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }

            // ── State-driven content ────────────────────────────────
            when (val state = uiState) {
                is UiState.Idle -> {
                    OutlinedCard(modifier = Modifier.fillMaxWidth()) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp),
                        ) {
                            Text(
                                text = "No airport selected yet",
                                style = MaterialTheme.typography.titleSmall,
                            )
                            Text(
                                text = "Type an ICAO code above to get the latest METAR.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                }

                is UiState.Loading -> {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 32.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        CircularProgressIndicator()
                    }
                }

                is UiState.Error -> {
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer,
                        ),
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Text(
                            text = state.message,
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            modifier = Modifier.padding(16.dp),
                        )
                    }
                }

                is UiState.Success -> {
                    WeatherContent(metar = state.data)
                }
            }

            Spacer(Modifier.height(8.dp))
        }
    }
}

// ── Success content ─────────────────────────────────────────────────

@Composable
private fun WeatherContent(metar: DecodedMetar) {
    // Flight category badge
    FlightCategoryBadge(metar.flightCategory)

    // Individual cards
    WeatherCard(title = "💨 Wind") {
        InfoRow("Direction", "${metar.windDirection}°")
        InfoRow("Speed", "${metar.windSpeed} kt")
        if (metar.windGust > 0) {
            InfoRow("Gusts", "${metar.windGust} kt")
        }
    }

    WeatherCard(title = "👁 Visibility") {
        Text(
            text = metar.visibility,
            style = MaterialTheme.typography.headlineSmall,
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center,
        )
    }

    WeatherCard(title = "☁ Cloud Layers") {
        if (metar.cloudLayers.isEmpty()) {
            Text("Clear skies", style = MaterialTheme.typography.bodyLarge)
        } else {
            metar.cloudLayers.forEach { layer ->
                Text("  •  $layer", style = MaterialTheme.typography.bodyLarge)
            }
        }
    }

    WeatherCard(title = "🌡 Temperature & Dewpoint") {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
        ) {
            LabeledValue(label = "Temp", value = metar.temperature)
            LabeledValue(label = "Dewpoint", value = metar.dewpoint)
        }
    }

    WeatherCard(title = "📊 Altimeter") {
        Text(
            text = metar.altimeter,
            style = MaterialTheme.typography.headlineSmall,
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center,
        )
    }

    WeatherCard(title = "🌦 Present Weather") {
        Text(
            text = metar.presentWeather,
            style = MaterialTheme.typography.bodyLarge,
        )
    }

    // Raw METAR string
    RawMetarCard(metar.rawMetar)
}

// ── Reusable components ─────────────────────────────────────────────

@Composable
private fun FlightCategoryBadge(category: String) {
    val bgColor = flightCategoryColor(category)
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(bgColor)
            .padding(vertical = 14.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = category.uppercase(),
            color = Color.White,
            fontWeight = FontWeight.Bold,
            fontSize = 20.sp,
        )
    }
}

@Composable
private fun WeatherCard(
    title: String,
    content: @Composable ColumnScope.() -> Unit,
) {
    ElevatedCard(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(text = title, style = MaterialTheme.typography.titleMedium)
            HorizontalDivider()
            content()
        }
    }
}

@Composable
private fun RawMetarCard(raw: String) {
    OutlinedCard(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Raw METAR",
                style = MaterialTheme.typography.titleSmall,
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = raw,
                fontFamily = FontFamily.Monospace,
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun InfoRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(text = label, style = MaterialTheme.typography.labelLarge)
        Text(text = value, style = MaterialTheme.typography.bodyLarge)
    }
}

@Composable
private fun LabeledValue(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = value, style = MaterialTheme.typography.headlineSmall)
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}
