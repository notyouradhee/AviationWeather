package com.example.aviationweather.data

import com.example.aviationweather.data.model.Cloud
import com.example.aviationweather.data.model.DecodedMetar
import com.example.aviationweather.data.model.MetarRaw

/**
 * Single source of truth for METAR data.
 *
 * Responsibilities:
 *   1. Call [AviationWeatherApi] to fetch raw JSON.
 *   2. Map [MetarRaw] → [DecodedMetar] with human-readable formatting.
 *   3. Return [Result] so the ViewModel never sees raw exceptions.
 */
class MetarRepository(private val api: AviationWeatherApi) {

    /**
     * Fetch and decode a METAR for the given ICAO identifier.
     *
     * @return [Result.success] with a [DecodedMetar], or
     *         [Result.failure] if the list is empty or a network error occurs.
     */
    suspend fun fetchMetar(icaoCode: String): Result<DecodedMetar> {
        return try {
            val rawList = api.getMetar(ids = icaoCode.uppercase())
            val raw = rawList.firstOrNull()
                ?: throw IllegalStateException("No METAR data found for $icaoCode")
            Result.success(raw.toDecoded())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ── MetarRaw → DecodedMetar mapping ─────────────────────────────

    private fun MetarRaw.toDecoded() = DecodedMetar(
        airportCode    = name.orEmpty(),
        windDirection  = wdir ?: 0,
        windSpeed      = wspd ?: 0,
        windGust       = wgst ?: 0,
        visibility     = formatVisibility(visib),
        cloudLayers    = clouds?.map { it.format() } ?: emptyList(),
        temperature    = formatTemp(temp),
        dewpoint       = formatTemp(dewp),
        altimeter      = formatAltimeter(altim),
        presentWeather = decodeWxString(wxString),
        flightCategory = flightCategory ?: computeFlightCategory(visib, clouds),
        rawMetar       = rawOb.orEmpty(),
    )

    // ── Formatting helpers ──────────────────────────────────────────

    private fun formatTemp(celsius: Double?): String =
        if (celsius != null) "${celsius.toInt()}°C" else "N/A"

    private fun formatAltimeter(inHg: Double?): String =
        if (inHg != null) "%.2f inHg".format(inHg) else "N/A"

    private fun formatVisibility(vis: String?): String =
        if (vis != null) "$vis SM" else "N/A"

    private fun Cloud.format(): String {
        val label = when (cover) {
            "FEW" -> "FEW"
            "SCT" -> "SCT"
            "BKN" -> "BKN"
            "OVC" -> "OVC"
            "CLR", "SKC" -> "CLR"
            else  -> cover.orEmpty()
        }
        return if (base != null) "$label at ${base * 100} ft" else label
    }

    // ── wxString decoder ────────────────────────────────────────────

    /**
     * Decode the raw METAR `wxString` (e.g. "-RA", "+TSRA", "BR")
     * into plain English (e.g. "Light Rain", "Heavy Thunderstorm Rain").
     *
     * Handles an intensity prefix (`-` = Light, `+` = Heavy) and
     * concatenated phenomenon codes.
     */
    private fun decodeWxString(wx: String?): String {
        if (wx.isNullOrBlank()) return "No significant weather"

        // Split on spaces for compound weather like "-RA BR"
        return wx.trim().split("\\s+".toRegex()).joinToString(", ") { token ->
            decodeSingleToken(token)
        }
    }

    private fun decodeSingleToken(token: String): String {
        var remaining = token
        // Extract intensity prefix
        val intensity = when {
            remaining.startsWith("-") -> { remaining = remaining.drop(1); "Light " }
            remaining.startsWith("+") -> { remaining = remaining.drop(1); "Heavy " }
            else -> ""
        }
        // Walk through remaining chars in 2-char chunks and decode each
        val parts = mutableListOf<String>()
        while (remaining.length >= 2) {
            val code = remaining.take(2)
            parts += WX_CODES[code] ?: code
            remaining = remaining.drop(2)
        }
        return intensity + parts.joinToString(" ")
    }

    private fun computeFlightCategory(vis: String?, clouds: List<Cloud>?): String {
        val visMiles = parseVisibilityMiles(vis)
        val ceilingFeet = findCeilingFeet(clouds)

        return when {
            ceilingFeet != null && ceilingFeet < 500 -> "LIFR"
            visMiles != null && visMiles < 1.0 -> "LIFR"
            ceilingFeet != null && ceilingFeet < 1000 -> "IFR"
            visMiles != null && visMiles < 3.0 -> "IFR"
            ceilingFeet != null && ceilingFeet < 3000 -> "MVFR"
            visMiles != null && visMiles < 5.0 -> "MVFR"
            ceilingFeet != null || visMiles != null -> "VFR"
            else -> "Unknown"
        }
    }

    private fun findCeilingFeet(clouds: List<Cloud>?): Int? {
        if (clouds.isNullOrEmpty()) return null
        val ceilingCodes = setOf("BKN", "OVC", "VV")
        return clouds
            .asSequence()
            .filter { it.cover in ceilingCodes && it.base != null }
            .map { it.base!! * 100 }
            .minOrNull()
    }

    private fun parseVisibilityMiles(raw: String?): Double? {
        if (raw.isNullOrBlank()) return null
        val cleaned = raw.trim().removeSuffix("SM").removePrefix("P")
        if (cleaned.contains(" ")) {
            val parts = cleaned.split(" ")
            val whole = parts.getOrNull(0)?.toDoubleOrNull() ?: return null
            val frac = parts.getOrNull(1)?.let { parseFraction(it) } ?: 0.0
            return whole + frac
        }
        if (cleaned.contains("/")) {
            return parseFraction(cleaned)
        }
        return cleaned.toDoubleOrNull()
    }

    private fun parseFraction(token: String): Double? {
        val parts = token.split("/")
        if (parts.size != 2) return null
        val numerator = parts[0].toDoubleOrNull() ?: return null
        val denominator = parts[1].toDoubleOrNull() ?: return null
        if (denominator == 0.0) return null
        return numerator / denominator
    }

    companion object {
        /** Common METAR weather phenomenon codes → plain English. */
        private val WX_CODES = mapOf(
            // Precipitation
            "RA" to "Rain",
            "SN" to "Snow",
            "DZ" to "Drizzle",
            "PL" to "Ice Pellets",
            "GR" to "Hail",
            "GS" to "Small Hail",
            "IC" to "Ice Crystals",
            "SG" to "Snow Grains",
            "UP" to "Unknown Precipitation",
            // Obscuration
            "FG" to "Fog",
            "BR" to "Mist",
            "HZ" to "Haze",
            "FU" to "Smoke",
            "DU" to "Dust",
            "SA" to "Sand",
            "VA" to "Volcanic Ash",
            // Severe
            "TS" to "Thunderstorm",
            "SQ" to "Squall",
            "FC" to "Funnel Cloud",
            "SS" to "Sandstorm",
            "DS" to "Duststorm",
            // Descriptors
            "SH" to "Showers",
            "FZ" to "Freezing",
            "MI" to "Shallow",
            "PR" to "Partial",
            "BC" to "Patches",
            "BL" to "Blowing",
            "DR" to "Drifting",
        )
    }
}
