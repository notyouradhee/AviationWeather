package com.example.aviationweather.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.aviationweather.data.MetarRepository
import com.example.aviationweather.data.model.DecodedMetar
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * Exposes a [StateFlow] of [UiState] that the Compose UI collects.
 *
 * Call [searchAirport] with an ICAO code to trigger a network request.
 */
class MetarViewModel(private val repository: MetarRepository) : ViewModel() {

    private val _uiState = MutableStateFlow<UiState>(UiState.Idle)
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    /**
     * Fetch and decode METAR for [icao], updating [uiState] through
     * Idle → Loading → Success / Error.
     */
    fun searchAirport(icao: String) {
        if (icao.isBlank()) return
        _uiState.value = UiState.Loading

        viewModelScope.launch {
            val result = repository.fetchMetar(icao)
            _uiState.value = result.fold(
                onSuccess = { UiState.Success(data = it) },
                onFailure = { UiState.Error(it.localizedMessage ?: "Unknown error") },
            )
        }
    }
}

/**
 * Sealed class representing all possible screen states.
 */
sealed class UiState {
    /** Initial state — nothing has been requested yet. */
    data object Idle : UiState()

    /** A network request is in flight. */
    data object Loading : UiState()

    /** Data was fetched and decoded successfully. */
    data class Success(val data: DecodedMetar) : UiState()

    /** Something went wrong (network, empty response, etc.). */
    data class Error(val message: String) : UiState()
}

