package com.example.aviationweather.data

import com.example.aviationweather.data.model.MetarRaw
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query

/**
 * Retrofit service interface for the Aviation Weather API.
 */
interface AviationWeatherApi {

    /**
     * Fetch METAR data for one or more ICAO station identifiers.
     *
     * @param ids    Comma-separated ICAO codes (e.g. "KJFK" or "KJFK,KLAX").
     * @param format Response format — always "json" for this app.
     * @return A list of [MetarRaw] objects (the API returns a JSON array).
     */
    @GET("api/data/metar")
    suspend fun getMetar(
        @Query("ids") ids: String,
        @Query("format") format: String = "json",
    ): List<MetarRaw>
}

/**
 * Singleton that provides a ready-to-use [AviationWeatherApi] instance.
 *
 * Usage:
 * ```
 * val api = RetrofitInstance.api
 * val metar = api.getMetar("KJFK")
 * ```
 */
object RetrofitInstance {

    private const val BASE_URL = "https://aviationweather.gov/"

    val api: AviationWeatherApi by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(AviationWeatherApi::class.java)
    }
}

