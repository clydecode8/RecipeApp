package com.example.assignme.ViewModel

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel

open class ThemeViewModel : ViewModel() {
    var isDarkTheme = mutableStateOf(false)

    fun toggleTheme() {
        isDarkTheme.value = !isDarkTheme.value
    }
}
