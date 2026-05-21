package com.example.aviationweather.data.model

import com.google.gson.annotations.SerializedName

/**
 * Raw JSON response from the Aviation Weather API.
 *
 * Each field is nullable to tolerate partial API responses.
 * [SerializedName] is only used where the Kotlin property name
 * differs from the JSON key.
 *
 * Example endpoint:
 *   https://aviationweather.gov/api/data/metar?ids=KJFK&format=json
 */
data class MetarRaw(
    val wdir: Int?,
    val wspd: Int?,
    val wgst: Int?,
    val visib: String?,
    val temp: Double?,
    val dewp: Double?,
    val altim: Double?,
    val wxString: String?,
    @SerializedName("fltcat") val flightCategory: String?,
    val rawOb: String?,
    val name: String?,
    val clouds: List<Cloud>?,
)

/**
 * Individual cloud-layer object nested inside a METAR response.
 */
data class Cloud(
    val cover: String?,
    val base: Int?,
)
