package com.example.aviationweather.data.model

/**
 * Data class representing wind calculations for a specific runway.
 */
data class RunwayWindCalculation(
    val runwayLabel: String,         // e.g. "Runway 13L"
    val heading: Int,                // e.g. 130
    val headwindComponent: Double,   // positive = headwind, negative = tailwind (knots)
    val crosswindComponent: Double,  // absolute value (knots)
    val isLeftCrosswind: Boolean,    // true = crosswind from left, false = from right
    val isRecommended: Boolean = false
)
