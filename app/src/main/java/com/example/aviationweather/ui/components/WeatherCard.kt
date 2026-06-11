package com.example.aviationweather.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.aviationweather.data.model.DecodedMetar
import kotlin.math.absoluteValue

val VfrGreen  = Color(0xFF2E7D32)
val MvfrBlue  = Color(0xFF1565C0)
val IfrRed    = Color(0xFFC62828)
val LifrPurple = Color(0xFF6A1B9A)

fun flightCategoryColor(cat: String): Color = when (cat.trim().uppercase()) {
    "VFR"  -> VfrGreen
    "MVFR" -> MvfrBlue
    "IFR"  -> IfrRed
    "LIFR" -> LifrPurple
    else   -> Color.Gray
}

fun flightCategoryExplanation(cat: String): String = when (cat.trim().uppercase()) {
    "VFR"  -> "Visual Flight Rules: Ceiling > 3,000 ft and visibility > 5 SM. Excellent visual flight conditions. Perfect for student flights."
    "MVFR" -> "Marginal Visual Flight Rules: Ceiling 1,000 to 3,000 ft and/or visibility 3 to 5 SM. Marginal conditions. Student pilots exercise caution!"
    "IFR"  -> "Instrument Flight Rules: Ceiling 500 to 1,000 ft and/or visibility 1 to 3 SM. Low visibility and ceilings. Instrument rating required."
    "LIFR" -> "Low Instrument Flight Rules: Ceiling < 500 ft and/or visibility < 1 SM. Extremely hazardous conditions. High instrument proficiency required."
    else   -> "Unknown flight rules category. Exercise caution."
}

@Composable
fun HeroPromptSection() {
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

@Composable
fun WeatherContent(metar: DecodedMetar) {
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

    FlightCategoryBadgeCard(metar.flightCategory)

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(modifier = Modifier.weight(1f)) {
                WindCard(
                    direction = metar.windDirection,
                    speed = metar.windSpeed,
                    gust = metar.windGust
                )
            }
            Box(modifier = Modifier.weight(1f)) {
                VisibilityCard(visStr = metar.visibility)
            }
        }
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(modifier = Modifier.weight(1f)) {
                TempDewpointSpreadCard(
                    tempStr = metar.temperature,
                    dewpStr = metar.dewpoint
                )
            }
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

    RawMetarCard(metar.rawMetar)
}

@Composable
fun FlightCategoryBadgeCard(category: String) {
    val bgColor = flightCategoryColor(category)
    val explanation = flightCategoryExplanation(category)
    
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
fun WindCard(direction: Int, speed: Int, gust: Int) {
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
fun VisibilityCard(visStr: String) {
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
fun TempDewpointSpreadCard(tempStr: String, dewpStr: String) {
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
fun PressureAltimeterCard(altimStr: String) {
    val colorScheme = MaterialTheme.colorScheme
    
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
fun WeatherCard(
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
fun RawMetarCard(raw: String) {
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
