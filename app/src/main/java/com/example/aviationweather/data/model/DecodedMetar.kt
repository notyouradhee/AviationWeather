package com.example.aviationweather.data.model

/**
 * Clean domain model that the UI layer displays.
 *
 * Every field is non-nullable with a sensible default so the UI
 * never needs to handle nulls or raw API quirks.
 */
data class DecodedMetar(
    val airportCode: String       = "",
    val windDirection: Int        = 0,
    val windSpeed: Int            = 0,
    val windGust: Int             = 0,
    val visibility: String        = "N/A",
    val cloudLayers: List<String> = emptyList(),
    val temperature: String       = "N/A",
    val dewpoint: String          = "N/A",
    val altimeter: String         = "N/A",
    val presentWeather: String    = "None",
    val flightCategory: String    = "Unknown",
    val rawMetar: String          = "",
)

