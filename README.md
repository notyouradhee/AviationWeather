# ✈️ Aviation Weather Decoder

An Android app that fetches and decodes **METAR** weather reports from the
[Aviation Weather API](https://aviationweather.gov/api/data/metar).
Built with **Kotlin**, **Jetpack Compose**, and **MVVM** architecture.

---

## Architecture Overview

```
┌──────────────────────────────────────────────────────────────┐
│                      UI  (Jetpack Compose)                   │
│  HomeScreen.kt  ←  collects StateFlow from ViewModel        │
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
│                   AviationWeatherApi.kt                       │
│  Retrofit interface · GET /metar?ids={ICAO}&format=json      │
└──────────────────────────────────────────────────────────────┘
```

---

## Project Structure

```
app/src/main/java/com/example/aviationweather/
├── MainActivity.kt                  # App entry point
├── data/
│   ├── model/
│   │   ├── MetarRaw.kt              # JSON data class
│   │   └── DecodedMetar.kt          # Clean domain model
│   ├── AviationWeatherApi.kt        # Retrofit service interface
│   └── MetarRepository.kt           # Data-fetching + mapping layer
└── ui/
    ├── MetarViewModel.kt            # StateFlow-based ViewModel
    ├── HomeScreen.kt                # Compose UI screen
    └── theme/                       # Material 3 theming (auto-generated)
```

---

## File Responsibilities

### `data/model/MetarRaw.kt`
> **Role:** JSON deserialization target.

- Maps **1-to-1** to the JSON returned by `aviationweather.gov`.
- Uses `@SerializedName` annotations for Gson.
- All fields are nullable to tolerate partial API responses.
- Includes a nested `Cloud` data class for the `clouds` array.

### `data/model/DecodedMetar.kt`
> **Role:** Clean, UI-ready domain model.

- Non-null fields with sensible defaults — the UI never handles raw nulls.
- Cloud layers are pre-formatted as human-readable strings (e.g. *"Scattered at 2500 ft"*).
- Completely decoupled from the serialization library.

### `data/AviationWeatherApi.kt`
> **Role:** Retrofit service definition.

- Single `suspend fun getMetar(ids, format)` endpoint.
- Base URL: `https://aviationweather.gov/api/data/`
- Returns `List<MetarRaw>` (the API always wraps results in an array).

### `data/MetarRepository.kt`
> **Role:** Single source of truth for weather data.

- Calls `AviationWeatherApi.getMetar()`.
- Maps `MetarRaw` → `DecodedMetar` via a private extension function.
- Translates cloud cover codes (`FEW`, `SCT`, `BKN`, `OVC`, etc.) into
  readable text.
- Throws `IllegalStateException` when the API returns an empty list.

### `ui/MetarViewModel.kt`
> **Role:** Presentation logic + state management.

- Exposes a `StateFlow<UiState>` with four states:
  | State     | Meaning                          |
  |-----------|----------------------------------|
  | `Idle`    | Nothing requested yet            |
  | `Loading` | Network request in progress      |
  | `Success` | Contains a `DecodedMetar`        |
  | `Error`   | Contains an error message string |
- `fetchMetar(icaoCode)` launches a coroutine in `viewModelScope`.

### `ui/HomeScreen.kt`
> **Role:** The single Compose screen the user sees.

- **Search bar** — `OutlinedTextField` that auto-uppercases input and limits
  to 4 characters. Supports IME search action.
- **Decode button** — triggers `viewModel.fetchMetar()`.
- **State rendering** — `when (uiState)` maps each sealed state to the
  appropriate composable (`CircularProgressIndicator`, error text,
  or a `MetarCard`).
- **MetarCard** — `ElevatedCard` displaying all decoded fields in
  label–value rows.

### `MainActivity.kt`
> **Role:** App entry point.

- Sets up edge-to-edge display.
- Applies the Material 3 theme.
- Hosts the Compose content tree.

---

## API Reference

| Field            | Type       | Example               |
|------------------|------------|------------------------|
| `icaoId`         | `String`   | `"KJFK"`              |
| `temp`           | `Double`   | `22.0`                |
| `dewp`           | `Double`   | `14.0`                |
| `wdir`           | `Int`      | `210`                 |
| `wspd`           | `Int`      | `12`                  |
| `wgst`           | `Int?`     | `22`                  |
| `visib`          | `String`   | `"10+"`               |
| `altim`          | `Double`   | `29.92`               |
| `fltcat`         | `String`   | `"VFR"`               |
| `wxString`       | `String?`  | `"-RA"`               |
| `rawOb`          | `String`   | `"KJFK 211856Z ..."`  |
| `clouds`         | `List`     | `[{cover:"SCT",base:25}]` |

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

### Dependencies to Add
Before building, add Retrofit and Gson to `app/build.gradle.kts`:

```kotlin
// Networking
implementation("com.squareup.retrofit2:retrofit:2.11.0")
implementation("com.squareup.retrofit2:converter-gson:2.11.0")

// Coroutines (if not already present)
implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.9.0")

// ViewModel Compose integration
implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.9.0")
```

Also ensure your `AndroidManifest.xml` includes the internet permission:

```xml
<uses-permission android:name="android.permission.INTERNET" />
```

---

## Next Steps

- [ ] Add Retrofit instance creation (e.g. via a singleton or dependency injection)
- [ ] Wire `MetarViewModel` into `MainActivity` / `HomeScreen`
- [ ] Add `INTERNET` permission to `AndroidManifest.xml`
- [ ] Add error handling for no-network scenarios
- [ ] Add unit tests for `MetarRepository` mapping logic
- [ ] Consider Hilt/Koin for dependency injection

---

## License

This project is for educational purposes.
