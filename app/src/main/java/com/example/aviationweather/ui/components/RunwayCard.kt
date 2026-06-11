package com.example.aviationweather.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.aviationweather.data.model.DecodedMetar
import kotlin.math.absoluteValue

@Composable
fun ActiveRunwayAdvisorCard(metar: DecodedMetar) {
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
fun InteractiveRunwayCalculatorCard(
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
