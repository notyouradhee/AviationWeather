package com.example.aviationweather

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.example.aviationweather.data.MetarRepository
import com.example.aviationweather.data.RetrofitInstance
import com.example.aviationweather.ui.HomeScreen
import com.example.aviationweather.ui.MetarViewModel
import com.example.aviationweather.ui.theme.AviationWeatherTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val repository = MetarRepository(RetrofitInstance.api)
        val viewModel  = MetarViewModel(repository)

        setContent {
            val systemInDark = androidx.compose.foundation.isSystemInDarkTheme()
            var userPreferredDark by remember { mutableStateOf<Boolean?>(null) }
            val dark = userPreferredDark ?: systemInDark

            AviationWeatherTheme(darkTheme = dark) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    HomeScreen(
                        viewModel = viewModel,
                        isDarkTheme = dark,
                        onToggleTheme = {
                            userPreferredDark = !dark
                        },
                        modifier  = Modifier.fillMaxSize(),
                    )
                }
            }
        }
    }
}