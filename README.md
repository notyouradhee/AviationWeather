# ✈️ Aviation Weather Decoder

An Android app that fetches and decodes **METAR** weather reports from the [Aviation Weather API](https://aviationweather.gov/api/data/metar).
Built with **Kotlin**, **Jetpack Compose**, and **MVVM** architecture, featuring advanced runway wind calculations.

---

## Features
- **METAR Decoding:** Real-time METAR fetching and parsing into human-readable formats.
- **Runway Wind Calculation:** Automatically calculates headwind, tailwind, and crosswind components based on user-provided runway headings and current METAR wind data.
- **Airport Data:** Integrated dictionary of ICAO codes for quick reference.
- **Modern UI:** Built fully in Jetpack Compose following Material 3 guidelines.

---

## Architecture Overview

```text
┌──────────────────────────────────────────────────────────────┐
│                      UI  (Jetpack Compose)                   │
│  HomeScreen.kt  ←  collects StateFlow from ViewModel         │
└──────────────────────────┬───────────────────────────────────┘
                           │ UiState (Idle / Loading / Success / Error)
┌──────────────────────────┴───────────────────────────────────┐
│                     MetarViewModel.kt                        │
│  Launches coroutines · manages UiState                       │
└──────────────────────────┬───────────────────────────────────┘
                           │ DecodedMetar
┌──────────────────────────┴───────────────────────────────────┐
│                     MetarRepository.kt                       │
│  Calls API · maps MetarRaw → DecodedMetar                    │
└──────────────────────────┬───────────────────────────────────┘
                           │ List<MetarRaw>
┌──────────────────────────┴───────────────────────────────────┐
│                   AviationWeatherApi.kt                      │
│  Retrofit interface · GET /metar?ids={ICAO}&format=json      │
└──────────────────────────────────────────────────────────────┘
```

---

## Project Structure

```text
app/src/main/java/com/example/aviationweather/
├── MainActivity.kt                  # App entry point
├── data/
│   ├── model/
│   │   ├── MetarRaw.kt              # JSON data class
│   │   ├── DecodedMetar.kt          # Clean domain model
│   │   └── RunwayWindCalculation.kt # Wind components calculator
│   ├── AviationWeatherApi.kt        # Retrofit service interface
│   ├── MetarRepository.kt           # Data-fetching + mapping layer
│   └── AirportsData.kt              # Airport code dictionary
└── ui/
    ├── MetarViewModel.kt            # StateFlow-based ViewModel
    ├── HomeScreen.kt                # Compose UI orchestrator
    ├── components/                  # Modular UI elements
    │   ├── AirportDrawer.kt         # Slide-out airport directory
    │   ├── RunwayCard.kt            # Crosswind/headwind calculators
    │   ├── SearchBar.kt             # Search & suggestion logic
    │   └── WeatherCard.kt           # METAR display & conditions
    └── theme/                       # Material 3 theming

app/src/test/java/com/example/aviationweather/
└── MetarRepositoryTest.kt           # Unit tests for data layer
```

---

## File Responsibilities

### `data/model/MetarRaw.kt`
> **Role:** JSON deserialization target.
- Maps **1-to-1** to the JSON returned by `aviationweather.gov`.

### `data/model/DecodedMetar.kt`
> **Role:** Clean, UI-ready domain model.
- Non-null fields with sensible defaults — the UI never handles raw nulls.
- Cloud layers are pre-formatted as human-readable strings (e.g. *"Scattered at 2500 ft"*).

### `data/model/RunwayWindCalculation.kt`
> **Role:** Calculates aviation wind components.
- Computes headwind, tailwind, and crosswind components for a specific runway heading based on the METAR's current wind direction and speed.

### `data/AirportsData.kt`
> **Role:** Static database of airports.
- Provides a lookup table of popular ICAO codes.

### `data/AviationWeatherApi.kt`
> **Role:** Retrofit service definition.
- Single `suspend fun getMetar(ids, format)` endpoint fetching from `https://aviationweather.gov/api/data/`.

### `data/MetarRepository.kt`
> **Role:** Single source of truth for weather data.
- Calls `AviationWeatherApi.getMetar()`.
- Maps `MetarRaw` → `DecodedMetar` via a private extension function.

### `ui/MetarViewModel.kt`
> **Role:** Presentation logic + state management.
- Exposes a `StateFlow<UiState>` matching the network states (`Idle`, `Loading`, `Success`, `Error`).

### `ui/HomeScreen.kt`
> **Role:** The main Compose screen orchestrator.
- Manages state injection, layout scaffolding, and navigation drawer interactions.
- Delegates rendering to modular UI elements in the `components/` directory (e.g., `WeatherCard`, `RunwayCard`).

### `ui/components/`
> **Role:** Modular Jetpack Compose UI elements.
- Extracted elements like `SearchBar`, `AirportDrawer`, `RunwayCard`, and `WeatherCard` to maintain clean separation of concerns and keep files small.

---

## Getting Started

### Prerequisites
- Android Studio Ladybug or later
- JDK 11+
- Android SDK 36

### Build & Run
```bash
# Clone the repo
git clone <repo-url> && cd AviationWeather

# Open in Android Studio → Run ▶️
# Or from the command line:
./gradlew installDebug
```

---

## License

This project is for educational purposes.
