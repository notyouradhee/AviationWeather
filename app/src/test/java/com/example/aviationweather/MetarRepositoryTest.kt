package com.example.aviationweather

import com.example.aviationweather.data.AviationWeatherApi
import com.example.aviationweather.data.MetarRepository
import com.example.aviationweather.data.model.MetarRaw
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class MetarRepositoryTest {

    // Simple Fake API for testing
    private class FakeAviationWeatherApi(val response: List<MetarRaw>) : AviationWeatherApi {
        override suspend fun getMetar(ids: String, format: String): List<MetarRaw> {
            return response
        }
    }

    @Test
    fun testRunwayAdvisor_windDirectlyAligned() = runBlocking {
        // Wind from 130 degrees at 20 knots
        val rawMetar = MetarRaw(
            wdir = 130,
            wspd = 20,
            wgst = null,
            visib = "10+",
            temp = 15.0,
            dewp = 10.0,
            altim = 29.92,
            wxString = null,
            flightCategory = "VFR",
            rawOb = "KJFK 231200Z 13020KT 10SM CLR 15/10 A2992",
            name = "John F. Kennedy International",
            clouds = emptyList()
        )
        
        val repository = MetarRepository(FakeAviationWeatherApi(listOf(rawMetar)))
        val result = repository.fetchMetar("KJFK")
        
        assertTrue(result.isSuccess)
        val decoded = result.getOrThrow()
        
        // KJFK has runways: 04L/22R, 04R/22L, 13L/31R, 13R/31L
        // Directional labels should include Runway 13L, 31R, 13R, 31L, 04L, 22R, 04R, 22L
        assertEquals(8, decoded.runways.size)
        
        // Runway 13L is heading 130, perfectly aligned with wind 130.
        // It should be marked as recommended!
        val recommended = decoded.runways.first { it.isRecommended }
        assertEquals("Runway 13L", recommended.runwayLabel)
        assertEquals(20.0, recommended.headwindComponent, 0.1)
        assertEquals(0.0, recommended.crosswindComponent, 0.1)
        
        // Runway 31R is heading 310 (reciprocal), perfectly aligned with tailwind.
        val reciprocal = decoded.runways.first { it.runwayLabel == "Runway 31R" }
        assertEquals(-20.0, reciprocal.headwindComponent, 0.1)
        assertEquals(0.0, reciprocal.crosswindComponent, 0.1)
        assertFalse(reciprocal.isRecommended)
    }

    @Test
    fun testRunwayAdvisor_crosswindComponent() = runBlocking {
        // Wind from 220 degrees at 15 knots (Runway 13L is heading 130, diff is 90 degrees)
        val rawMetar = MetarRaw(
            wdir = 220,
            wspd = 15,
            wgst = null,
            visib = "10+",
            temp = 15.0,
            dewp = 10.0,
            altim = 29.92,
            wxString = null,
            flightCategory = "VFR",
            rawOb = "KJFK 231200Z 22015KT 10SM CLR 15/10 A2992",
            name = "John F. Kennedy International",
            clouds = emptyList()
        )
        
        val repository = MetarRepository(FakeAviationWeatherApi(listOf(rawMetar)))
        val result = repository.fetchMetar("KJFK")
        
        assertTrue(result.isSuccess)
        val decoded = result.getOrThrow()
        
        // Runway 22R (heading 220) is perfectly aligned with wind 220.
        val rwy22R = decoded.runways.first { it.runwayLabel == "Runway 22R" }
        assertTrue(rwy22R.isRecommended)
        assertEquals(15.0, rwy22R.headwindComponent, 0.1)
        assertEquals(0.0, rwy22R.crosswindComponent, 0.1)
        
        // Runway 13L (heading 130) has a 90 degree crosswind.
        val rwy13L = decoded.runways.first { it.runwayLabel == "Runway 13L" }
        assertEquals(0.0, rwy13L.headwindComponent, 0.1)
        assertEquals(15.0, rwy13L.crosswindComponent, 0.1)
    }
}
